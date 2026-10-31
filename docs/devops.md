# DevOps and Operations

Practical guidance on building, testing, developing, and releasing `support.java`.

---

## Prerequisites and Environment

- JDK: Java 25, auto-provisioned by the Gradle toolchain if not already installed locally.
- Gradle: Gradle 9.7.0, managed via the included wrapper (`gradlew` / `gradlew.bat`).
- Git: Git 2.30+ supporting worktrees (`git worktree`).
- Docker: only needed to run `SqlPagingTest` (a Testcontainers-backed MariaDB integration test); it's skipped,
  not failed, when Docker is unavailable.
- PowerShell / Bash: PowerShell on Windows (`gradlew.bat`), standard POSIX shell on macOS/Linux (`./gradlew`).

---

## Development Workflows

### Initial Bootstrap
```bash
python scripts/bootstrap-dev-environment.py
```
Initializes git if needed and runs `gradlew build` (`gradlew.bat` on Windows, `./gradlew` on POSIX). No
`scripts/deploy-to-production.py` is provided — this is a library published to a package repository, not a
deployed service.

### Building and Testing
```powershell
# Compile main sources only
.\gradlew.bat compileJava

# Full compile, test, and jar/javadoc-jar/sources-jar assembly
.\gradlew.bat build

# Run all tests (streams stdout live per test - see testLogging in build.gradle)
.\gradlew.bat test

# Run a single test class
.\gradlew.bat test --tests "com.pellicciotta.support.text.TokenizerTest"

# Run a single test method
.\gradlew.bat test --tests "com.pellicciotta.support.text.TokenizerTest.testSomething"

# Run the Docker-backed database integration test
.\gradlew.bat test --tests "com.pellicciotta.support.sql.SqlPagingTest"
```
`SqlPagingTest` starts and tears down its own MariaDB automatically via Testcontainers — no hand-run server or
manual setup needed; its `@BeforeAll` creates the `test` database and `score` table (via
`src/test/resources/sql/mysql/setup-db.sql`) inside the container. Docker must be running; if it isn't,
`@Testcontainers(disabledWithoutDocker = true)` reports the class as skipped rather than failed.

### Task Coordination Protocol
All non-trivial task work follows the protocol in
[Coordinating Work Guidelines](https://github.com/gpellicciotta/dev-guidelines/blob/main/guidelines/coordinating-work-guidelines.md):
1. **Claim**: Update the task line in `TODO.md` on `master` from `[ ]` to `[~] @owner`, commit.
2. **Worktree**: Create an isolated worktree at `./work/Tnnnn-slug` on branch `task/Tnnnn-slug`:
   ```bash
   git worktree add ./work/T0001-task-slug -b task/T0001-task-slug
   ```
3. **Execute**: Work within the isolated worktree and maintain `tasks/Tnnnn-slug.md`.
4. **Finalize**: Pass all tests, update documentation and `CHANGELOG.md`, integrate into mainline, remove the
   worktree and branch, and clear the entry from `TODO.md`.

### IntelliJ Gradle Import
If IntelliJ doesn't pick up Gradle dependencies automatically: `View > Tool Windows > Gradle`, then hit
"Refresh". See
[this Stack Overflow answer](https://stackoverflow.com/questions/27694442/getting-gradle-dependencies-in-intellij-idea-using-gradle-build)
for background.

---

## Release Process

Ongoing work accumulates under the top `CHANGELOG.md` heading while it carries a `-pre` SemVer suffix (e.g.
`## v1.0.0-pre`), which must always match `version` in `gradle.properties` (the single source of truth for the
jar manifest and Maven coordinates) exactly, `-pre` included. Escalate that heading/version together — to the
next major, minor, or patch number — the moment a change lands that needs it (a breaking change escalates to
the next major; don't wait until release time to work out what the version should have been).

1. Confirm the top `CHANGELOG.md` heading's version and `gradle.properties`' `version` already agree (both
   carry the same `-pre` suffix) — this should already be true from ongoing work, not something decided here.
2. Freeze: replace the heading's `-pre` suffix with `[{{date}}]`, and drop `-pre` from `gradle.properties`'
   `version` so both again match exactly (without the suffix). In the same commit, add the next patch
   version's `## vX.Y.Z-pre` heading above it (empty, ready for the next round of ongoing work), but leave
   `gradle.properties`' `version` at the bare frozen version — bumping it to the next `-pre` now would make
   the following publish step (step 5) publish the wrong version. Only bump `gradle.properties` to the next
   `-pre` in the first real commit of the next dev cycle.
3. Perform a clean verification build:
   ```powershell
   .\gradlew.bat clean build
   ```
4. Commit the changes.
5. Once a clean build/test run has also succeeded on CI, publish to GitHub Packages (see 3.1), then verify the
   package is actually resolvable before marking the entry `[released: {{date}}]` and tagging (see 3.2). A
   frozen version that never gets published is an accepted terminal state, not something requiring cleanup.

### Publishing to GitHub Packages
```powershell
gradle publishMavenPublicationToGithubPackagesRepository
```
Needs `github.username`/`github.token` in `${user.home}/.gradle/gradle.properties`, which is deliberately kept
outside version control (see [Creating a personal access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line)
and [Configuring Gradle for GitHub Packages](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages)).
The drawback of keeping credentials out of version control is that they can be lost locally; there is currently
no backup mechanism for them.

Publishing SNAPSHOT versions does not work reliably against GitHub Packages (first publish succeeds, subsequent
publishes/consumption fail with a 400 on `maven-metadata.xml`) — this project does not use SNAPSHOT versions as
a result.

**Never run the publish command unless explicitly asked** — it publishes to a shared repository that other
repos (`pellicciotta-counters`, `pellicciotta-auth`, `pellicciotta-todos`) depend on.

### Tagging a Release
Once a version is actually verified released (published and resolvable):
```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```
Tag pushes are an outbound action and need the same explicit approval as any other push.

### Creating a GitHub Release
After pushing the tag, publish a matching GitHub Release so the version shows up under
[Releases](https://github.com/gpellicciotta/support.java/releases), not just as a bare tag:
```bash
gh release create v1.0.0 --title "v1.0.0" --notes-file <(sed -n '/^## v1.0.0/,/^---$/p' CHANGELOG.md | sed '1d;$d')
```
Use that version's own `CHANGELOG.md` section (between its heading and the next one) as the release notes.
Creating a GitHub Release is an outbound action and needs the same explicit approval as any other push.

---

## Continuous Integration

The GitHub Actions workflow in `.github/workflows/ci.yml` triggers on push and pull requests, executing:
- Java JDK 25 setup (Temurin distribution).
- `./gradlew build` (compilation, unit and integration testing, and packaging). GitHub-hosted `ubuntu-latest`
  runners ship Docker, so the Testcontainers-backed `SqlPagingTest` runs in CI too.
