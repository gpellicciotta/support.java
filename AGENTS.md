<!-- BEGIN plan (managed by plan init) -->
This project tracks its work with plan in `PLAN.md` and `.plan/tasks/`.
Load the `plan-task-usage-guidelines` skill before creating, starting, or changing tasks.
Change `PLAN.md` and task frontmatter only through `plan` commands.
<!-- END plan -->

# support.java
General-purpose Java support library (group `com.pellicciotta`), published to GitHub Packages.
Single-process apps with an embedded HTTP server consume it: `pellicciotta-counters`, `pellicciotta-auth`, `pellicciotta-todos`.
Published bytecode needs JDK 25 or later to load.
See [README.md](README.md), [requirements.md](docs/requirements.md), and [devops.md](docs/devops.md).

## Commands
Use `.\gradlew.bat` in Windows PowerShell and `./gradlew` in POSIX shells.

- Bootstrap: `python scripts/bootstrap-dev-environment.py` initializes Git, fills `.venv` from [requirements.txt](scripts/requirements.txt), and builds.
- Fast tests: `.\gradlew.bat test`, or `python scripts/devops.py test com.pellicciotta.support.text.TokenizerTest` for a selection.
- Slow tests (Docker-backed `SqlPagingTest`): `.\gradlew.bat slowTest`.
- Script tests: `.venv/Scripts/python -m unittest discover -s scripts/tests`.
- All checks (Markdown lint, Spotless, Checkstyle, fast and slow tests, jars): `python scripts/devops.py check`.
- Test output streams to stdout live; no need to read XML reports.
- Start: none; this is a library. The bundled CLI tools run through `ToolDispatcher`.

## Architecture
Everything lives under [com.pellicciotta.support](src/main/java/com/pellicciotta/support/), one package per concern.
Most packages split an API half from an SPI half; read each `package-info.java` before working in it.

- `logging`: `LogService` hands out `Log` instances backed by a `spi.LogBackEnd`; `Span` models timed, hierarchical operations.
- `config`: `SettingsFactory` builds a `Config` tree; sources compose with command line over environment over file, last source wins.
- `codecs`: encode and decode data between string, XML, and property representations for `config`.
- `app`: `BasicApp<T extends BasicConfig>` template for CLI apps; `tools/ToolDispatcher` runs the bundled `tools` by name.
- `http`: server-agnostic layer over the JDK `HttpServer`; handlers run in registration order and the first to return `true` wins.
- `sql`: JDBC helpers, dialect-aware generators, paging, a hand-written SQL tokenizer and parser, versioned statements.
- `mail`: `MailService` over JavaMail with a `DummyBackEnd` for tests.
- `diff`: Myers, Histogram, and Patience algorithms behind `DiffAlgorithm`, selected by `StandardDiffAlgorithms`.
- `text`: `Tokenizer`, `TokenMatcher`, and the `Syntax*` tree, plus `TextBuilder` and `HtmlTextBuilder`.
  A separate `TokenMatchContext` redesign lives in the test tree under `src/test/java/com/pellicciotta/support/parsing/`.
- `utils`: general helpers such as `StringUtils`, `Checks`, and `Tuple2` to `Tuple4`; check here before adding a helper.
- `cache`: `Cache` and `MemoryCache`, plus an unrelated `AbstractPool` resource pool.
- `annotations`: markers read by reflection, such as `@CommandLineAction`, `@ConfigSetting`, and `@WebRoute`.
- [scripts/](scripts/): the bootstrap and the thin [devops.py](scripts/devops.py) runner on support-python.
- [gradle/](gradle/): the version catalog and the [release.gradle](gradle/release.gradle) `doFullRelease` pipeline.
- [config/checkstyle/](config/checkstyle/): Checkstyle rules.

## Project Conventions
- Source: every file starts with `// Copyright (c) Giovanni Pellicciotta`; every package has a `package-info.java`.
  Update it when the design changes.
- Checkstyle fails the build on misplaced braces, unbraced control blocks, and public or protected members without Javadoc.
- Tests: JUnit 6 in plain `org.junit.jupiter.api` style, with no parameterized tests and no mocking library.
  Tag Docker-backed or otherwise slow tests with `@Tag("slow")`.
- Versions: [gradle.properties](gradle.properties) holds `version`; the top [CHANGELOG.md](CHANGELOG.md) heading must match it, `-pre` included.
  `python scripts/devops.py versions` checks this.
- Semver: any backward-incompatible change bumps the major version and adds a `**[breaking]**` changelog note.
  That covers API removals, behavior changes callers may rely on, and a minimum JDK bump.
  Never bundle a breaking change into a minor or patch bump.
- Toolchain: `build.gradle` sets no `--release` pin, so bytecode targets the toolchain JDK.
  Decide explicitly before moving the Java baseline.

## Boundaries
- Never run `python scripts/devops.py release` or `doFullRelease` unless the user explicitly asks; both push and publish to a shared repository.
- Never edit finalized [CHANGELOG.md](CHANGELOG.md) sections; only their heading status tags may change.
- Never commit GitHub Packages credentials; they belong in the user's Gradle properties or environment.
- Never add `.plan/` or `PLAN.md` to `main`; plan data lives on the `plan` branch.
