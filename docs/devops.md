# DevOps and Operations

Practical guidance on building, testing, developing, and releasing `support.java`.

---

## Development Environment

### Prerequisites
- Java 25 JDK, auto-provisioned by the Gradle toolchain if not present locally.
- Gradle 9.7.0, managed through the included wrapper ([gradlew](../gradlew) / [gradlew.bat](../gradlew.bat)).
- Python 3.9+ for [bootstrap-dev-environment.py](../scripts/bootstrap-dev-environment.py) (standard library only) and [devops.py](../scripts/devops.py).
  The latter runs on [support-python](https://github.com/gpellicciotta/support.python), pinned in [requirements.txt](../scripts/requirements.txt).
- Git 2.30+ supporting worktrees (`git worktree`).
- GitHub CLI `gh`, logged in, for releases only.
- Docker, only for `SqlPagingTest`, a Testcontainers-backed MariaDB test; it is skipped, not failed, when Docker is unavailable.
- PowerShell on Windows (`gradlew.bat`), or a standard POSIX shell on macOS/Linux (`./gradlew`).
- GitHub Packages credentials: `github.username` and `github.token` (a personal access token with
  `write:packages`) in `{{user-home}}/.gradle/gradle.properties`, or the `GITHUB_USERNAME` and `GITHUB_TOKEN`
  environment variables. These stay outside version control. See
  [Configuring Gradle for GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).
- All dependency and plugin versions live in [libs.versions.toml](../gradle/libs.versions.toml).
  Dynamic versions such as `1.+` are not used, so builds resolve the same artifacts everywhere.

### Setup
Clone the repository and run the bootstrap script:
```shell
python scripts/bootstrap-dev-environment.py
```
It initializes Git if needed, creates `.venv`, installs [requirements.txt](../scripts/requirements.txt) into it, and runs `gradlew build`.
A project-local `.venv` keeps the pinned support-python apart from other projects pinning other versions.
Rerun the bootstrap after changing [requirements.txt](../scripts/requirements.txt).

If IntelliJ does not pick up Gradle dependencies automatically, use `View > Tool Windows > Gradle` and refresh.
The module files that import generates are not committed.

---

## Versioning and Releasing

### Updating Versions
[gradle.properties](../gradle.properties) holds the authoritative `version`, without a `v` prefix.
It drives the jar manifest and the Maven coordinates.
The top [CHANGELOG.md](../CHANGELOG.md) heading must match it exactly, `-pre` suffix included, with a `v` prefix.

Bump the version as part of the change that requires it:
- Breaking change: raise the major version and reset minor and patch, unless the `-pre` version already did.
- New functionality: raise the minor version and reset the patch, unless the `-pre` version already did.
- Rename `version` and the top [CHANGELOG.md](../CHANGELOG.md) heading together, in the same commit as the change.

`plan` task completion does not read [gradle.properties](../gradle.properties); the release pipeline's `openNextPreRelease` step raises the patch.
Run `python scripts/devops.py versions` to fail fast when the two versions drift apart.

### Making a Release
A release publishes to GitHub Packages, pushes to `origin`, and creates a GitHub release, so it only runs on explicit request.
Publishing SNAPSHOT versions does not work reliably against GitHub Packages, so this project does not use them.
1. Confirm every change of the version is recorded under the top `-pre` heading of [CHANGELOG.md](../CHANGELOG.md).
2. Run `python scripts/devops.py versions` and `python scripts/devops.py check`, and fix any failure.
3. Commit and push `main`, then wait until CI on `origin/main` is green.
4. Authenticate the [GitHub CLI](https://cli.github.com/) with `gh auth login`, if not done yet.
5. Preview the pipeline with `python scripts/devops.py release --dry-run`.
6. Run `python scripts/devops.py release` and type the requested confirmation; `--yes` skips it.
7. Check the new `v{{version}}` tag, the GitHub release, the published package, and the next `-pre` heading on `origin/main`.

```shell
python scripts/devops.py release --dry-run   # list the pipeline tasks only
python scripts/devops.py release             # asks for a typed confirmation, then runs doFullRelease -PreleaseConfirmed=yes
python scripts/devops.py release --yes       # skips the typed confirmation
```

#### Gradle `doFullRelease` Pipeline
`devops.py release` runs this pipeline, which can also run directly:
```powershell
.\gradlew.bat doFullRelease -PreleaseConfirmed=yes
```
It refuses to start without `-PreleaseConfirmed=yes`. In order, it runs:
- `checkReleasePreconditions`: full `build`, then fails unless confirmed, the tree is clean, the branch is `main`,
  `version` ends in `-pre`, [CHANGELOG.md](../CHANGELOG.md) has the matching heading, `gh` is logged in,
  and HEAD equals `origin/main` with green CI (it waits up to 15 minutes for a running build).
- `finalizeRelease`: strips `-pre` from [gradle.properties](../gradle.properties), marks the heading `[released: {{date}}]`, and commits.
- `publishReleaseArtifacts`: publishes to GitHub Packages in a nested Gradle run, so it uses the released version.
- `tagAndPushRelease`: tags `v{{version}}` and pushes the branch and the tag.
- `createGithubRelease`: creates the GitHub Release with the changelog section as notes.
- `openNextPreRelease`: bumps to the next patch `-pre`, opens its changelog heading, commits, and pushes.

Run any step on its own, such as `.\gradlew.bat checkReleasePreconditions -PreleaseConfirmed=yes`,
to diagnose or resume a failed release without repeating finished steps.
The publication, POM metadata, and repository are configured in [build.gradle](../build.gradle).

---

## Development Workflows

### Operations Runner `devops.py`
[devops.py](../scripts/devops.py) is the single entry point to typical operations; run `python scripts/devops.py help --verbose`.
It is a thin `DevopsCli` over a `GradleProject` from support-python's `support_python.devops`.
When `.venv` exists, it reruns itself with the `.venv` interpreter, so plain `python` works.

```shell
python scripts/devops.py check      # every CI validation step: lint-markdown check, then gradlew build (Spotless, Checkstyle, fast and slow tests, jars)
python scripts/devops.py lint       # lint-markdown check, then spotlessCheck, checkstyleMain, checkstyleTest
python scripts/devops.py format     # spotlessApply
python scripts/devops.py test com.pellicciotta.support.text.TokenizerTest   # each selection becomes a Gradle --tests filter
python scripts/devops.py versions   # fail when gradle.properties and CHANGELOG.md versions drift apart
python scripts/devops.py release --dry-run
```

`lint` and `check` run `lint-markdown check` first and stop when it fails.
[.lint-markdown.json](../.lint-markdown.json) adds [CHANGELOG.md](../CHANGELOG.md), whose finalized entries must stay unedited, to the default excludes.
Add project-specific actions in `build_cli()` with `cli.add_action(name, summary, handler)`.
See support-python's [DevOps scripts tutorial](https://github.com/gpellicciotta/support.python/blob/v2.0.0/docs/tutorials/devops-scripts.md).
Test the script itself with `.venv/Scripts/python -m unittest discover -s scripts/tests` (`.venv/bin/python` outside Windows).
CI installs [requirements.txt](../scripts/requirements.txt), runs those tests, then runs `python scripts/devops.py check`.

### Building and Testing
```powershell
# Compile main sources only
.\gradlew.bat compileJava

# Fast suite: every test not tagged slow, with a 30 second default timeout per test
.\gradlew.bat test

# Slow suite: tests tagged slow, with a 120 second default timeout per test
.\gradlew.bat slowTest

# Full compile, format and style checks, both suites, and jar, sources jar, and Javadoc jar assembly
.\gradlew.bat build

# Run a single test class or method
.\gradlew.bat test --tests "com.pellicciotta.support.text.TokenizerTest"
.\gradlew.bat test --tests "com.pellicciotta.support.text.TokenizerTest.testSomething"
```
`test` streams stdout live per test; see `testLogging` in [build.gradle](../build.gradle).
`check` and `build` also run `slowTest`, so CI and releases run both suites.

`SqlPagingTest` is the only slow test. It starts and tears down its own MariaDB through Testcontainers,
and its `@BeforeAll` creates the `test` database and `score` table from `src/test/resources/sql/mysql/setup-db.sql`.
Docker must be running; otherwise `@Testcontainers(disabledWithoutDocker = true)` reports the class as skipped.
Run it alone with `.\gradlew.bat slowTest --tests "com.pellicciotta.support.sql.SqlPagingTest"`.

### Task Tracking
Tasks live in [PLAN.md](../PLAN.md) and `.plan/tasks/` and are managed only through the `plan` CLI (`plan help`).
Never edit `PLAN.md` or task frontmatter by hand.

---

## Continuous Integration
The GitHub Actions workflow in [ci.yml](../.github/workflows/ci.yml) triggers on push and pull requests, executing:
- Java JDK 25 setup (Temurin distribution) and Python setup.
- `python -m pip install -r scripts/requirements.txt` and `python -m unittest discover -s scripts/tests`.
- `python scripts/devops.py check`: Markdown lint, formatting, style, compilation, fast and slow tests, and packaging.
  GitHub-hosted `ubuntu-latest` runners ship Docker, so the Testcontainers-backed `SqlPagingTest` runs in CI too.
