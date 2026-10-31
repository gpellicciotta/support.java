<!-- BEGIN plan (managed by plan init) -->
This project tracks its work with plan in `PLAN.md` and `.plan/tasks/`.
Load the `plan-task-usage-guidelines` skill before creating, starting, or changing tasks.
Change `PLAN.md` and task frontmatter only through `plan` commands.
<!-- END plan -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is
`support.java` is a general-purpose Java support library (group `com.pellicciotta`), published as a Maven package
and consumed by several other single-process apps with an embedded HTTP server: `pellicciotta-counters`,
`pellicciotta-auth`, `pellicciotta-todos`. Java 25 toolchain, Gradle wrapper 9.7.0. `gradle/gradle-daemon-jvm.properties`
pins the daemon JVM to 25.

Published bytecode requires **JDK 25 or later to load** (the `toolchain {}` block in `build.gradle` has no
`--release` pin, so it compiles *at* the toolchain version, not just *with* it). If this library's Java baseline
needs to move again, decide explicitly whether to pin `options.release` below the toolchain version (keeps
consumer compatibility) or let it float with the toolchain (another bytecode-version, major-version bump) — see
the semver rule under Conventions.

## Commands
All commands use the Gradle wrapper (`gradlew` / `gradlew.bat` — use `gradlew.bat` on Windows PowerShell, `./gradlew`
under the Bash tool).

- Bootstrap a fresh checkout: `python scripts/bootstrap-dev-environment.py`
- Run every CI validation step: `python scripts/devops.py check` (same as `gradlew build`: Spotless, Checkstyle, tests, jars)
- Build: `gradlew build`
- Run all tests: `gradlew test`
- Run a single test class: `gradlew test --tests "com.pellicciotta.support.text.TokenizerTest"`
- Run a single test method: `gradlew test --tests "com.pellicciotta.support.text.TokenizerTest.testSomething"`
- Test output streams to stdout live (`testLogging.showStandardStreams = true` in `build.gradle`) — no need to dig
  through XML reports to see what a failing test printed.
- Javadoc jar / sources jar are always generated (`withJavadocJar()` / `withSourcesJar()` in `build.gradle`).
- Release: `python scripts/devops.py release` runs the Gradle `doFullRelease` pipeline (publish to GitHub
  Packages, tag, push, GitHub Release, open the next `-pre` version); preview it with `--dry-run`. Credentials
  (`github.username`/`github.token` in `${user.home}/.gradle/gradle.properties`, or `GITHUB_USERNAME`/`GITHUB_TOKEN`)
  stay outside version control — see [devops.md](docs/devops.md). **Never run a release or publish unless the
  user explicitly asks** — it publishes to a shared repository that other repos depend on.

Testing uses JUnit 6 (`org.junit.jupiter:junit-jupiter-*`, `org.junit.platform:junit-platform-launcher`) — plain
`org.junit.jupiter.api` style (`@Test`/`@BeforeEach`/`@BeforeAll`/`Assertions`/etc.), no parameterized-test
features and no mocking library in use (Mockito was a dependency but had zero real usage in the test tree, so
it's gone). `SqlPagingTest` is the one exception to "no custom extensions": it uses Testcontainers'
`@Testcontainers`/`@Container` to run a real MariaDB in Docker — see `docs/devops.md` for details.

## Architecture
Everything lives under `com.pellicciotta.support`, organized as one package per concern. Each package generally has an
API half (what consuming code calls) and an SPI half (how to extend/back it) — read the `package-info.java` in a
package before working in it; several (`logging`, `config`) have detailed design notes there.

- **`logging`** — `LogService` (singleton front-end/factory) hands out `Log` instances backed by a configurable
  `spi.LogBackEnd`. `Span` is the API for structured, hierarchical, timed operations; `ContextData`/`ContextDataItem`
  carry the key/value pairs attached to a span and available to log output. Back-ends: `jul` (java.util.logging),
  `stdout`, `noop`, `file` (writes to a configurable file path, append or overwrite), plus a `gcp` structured-log
  formatter. Typical usage is documented in full in `logging/package-info.java`.
- **`config`** — `SettingsFactory` builds a `Config` (a tree of `SettingsGroup`/`Setting`). Settings carry metadata
  (description, default) and (de)serialization to JSON/XML/Map/properties/command-line. Sources compose in a
  defined precedence (command-line overrides env vars overrides config file), "last source wins". See
  `config/package-info.java` for the full design rationale.
- **`codecs`** — encode/decode data between representations (string, XML, property values); used by `config` for
  setting (de)serialization.
- **`app`** — `BasicApp<T extends BasicConfig>` is the template base for a CLI-runnable app: config loading,
  logging setup, `@CommandLineAction`-annotated methods dispatched by name. `ToolDispatcher`
  (`tools/ToolDispatcher.java`) is the multi-tool entry point used by this library's own bundled CLI tools
  (`tools/charsets`, `tools/diff`, `tools/timezones`, `tools/httpd`) — it reflection-scans the `tools` package for
  `BasicApp` subclasses and dispatches by name.
- **`http`** — a server-agnostic HTTP layer with no servlet container or external HTTP server dependency.
  `Request`/`Response` are the API consuming code writes against; `HttpRequestHandler`/`HttpRequestProcessor`/
  `HttpRoute`/`HttpRoutes` route requests to code (`@WebRoute`/`@WebRoutes`-annotated methods, or direct
  registration); `WebServer` is the server abstraction, with `JdkHttpServerWebServer` — built on the JDK's own
  `com.sun.net.httpserver.HttpServer` — as its only backing implementation. `StaticFileHandler` serves files from
  the file system or classpath (including from inside a jar). Registered `HttpRequestHandler`s are tried in
  registration order per request; the first to return `true` wins, mirroring what a servlet container's
  `web.xml`-based dispatch used to provide.
- **`sql`** — JDBC helpers: `SqlExecutor`, dialect-aware `SqlGenerator`/`SqlDialect`, paging (`SqlPager` and its
  `IdColumnBasedSqlPager`/`ColumnValuesBasedSqlPager` implementations), a hand-written `SqlTokenizer`/`SqlParser`
  for statement-syntax detection, versioned statements (`VersionedSqlStatement`) for schema migrations.
- **`mail`** — `MailService`/`MailBackEnd` abstraction over JavaMail (`JMBackEnd`) with a `DummyBackEnd` for tests.
- **`diff`** — a family of diff algorithms (`MyersGreedyDiff`, `MyersLinearSpaceDiff`, `MyersReverseGreedyDiff`,
  `HistogramDiff`, `PatienceDiff`) behind a common `DiffAlgorithm`/`DiffProcessor` interface, selected via
  `StandardDiffAlgorithms`; `tools/diff/DiffTool` is the CLI front-end.
- **`text`** — `Tokenizer`/`TokenMatcher`(s) for lexing, `SyntaxNode`/`SyntaxElement`/`SyntaxToken`/`SyntaxType` for
  the resulting tree, plus `TextBuilder`/`HtmlTextBuilder` for output. Background/design notes on the
  tokenizer/parser approach are linked from `text/package-info.java` (also indexed in `docs/index.md`). A separate,
  `TokenMatchContext`-based redesign of `Parser`/`Tokenizer`/`TokenMatcher`/`Matcher`/`MatchContext`/
  `CharSequenceMatcher`/`TokenMatchers` lives in the test tree under
  `src/test/java/com/pellicciotta/support/parsing/` — kept in its own package deliberately, since it previously
  collided with these main-source classes when both used the `text` package name.
- **`utils`** — large grab-bag of general helpers (`StringUtils`, `CollectionUtils`, `DateTimeUtils`,
  `ReflectionUtils`, `FileUtils`, `Checks`/`Contract` for argument validation, `Tuple2`/`Tuple3`/`Tuple4`, etc.).
  Check here before adding a new low-level helper — it may already exist.
- **`cache`** — `Cache`/`AbstractCache`/`MemoryCache` and a separate `AbstractPool`/`PoolResource` resource-pooling
  abstraction (distinct concepts sharing a package).
- **`annotations`** — declarative markers consumed by reflection elsewhere: `@CommandLineAction` (`app`),
  `@ConfigGroup`/`@ConfigSetting` (`config`), `@WebRoute`/`@WebRoutes` (`http`).

## Development Guidelines
This project follows the cross-project [dev-guidelines](https://github.com/gpellicciotta/dev-guidelines) and
started from [java-project-template](https://github.com/gpellicciotta/java-project-template):
- Standard root files (`LICENSE.md`, `CHANGELOG.md`, `PLAN.md`, `README.md`) plus `docs/index.md`,
  `docs/requirements.md`, `docs/devops.md` — see [index.md](docs/index.md) for the full documentation index.
- Tasks live in `PLAN.md` and `.plan/tasks/` and are managed only through the `plan` CLI (see the block at the
  top of this file).
- Commit messages are one past-tense sentence of at most 20 words, with no prefixes or attribution trailers.

## Conventions
- Every source file starts with `// Copyright (c) Giovanni Pellicciotta`.
- Packages with non-trivial design decisions document them in `package-info.java` (see `logging`, `config`) —
  update these when the design changes, don't just leave them describing the old shape.
- Versioning/release process: `gradle.properties` (`version=`) drives what actually gets published; keep
  `CHANGELOG.md`'s newest heading matching it exactly (`python scripts/devops.py versions` checks this). Versions
  carry a `-pre` suffix while in development; `doFullRelease` strips it, publishes, and opens the next patch.
- **Semver discipline (applies to every change, not just this file):** any backward-incompatible change — a
  public API removal/signature change, a behavior change existing callers could depend on, or a
  minimum-runtime-version bump that breaks previously-working consumers — bumps the **major** version number and
  gets an explicit `**[breaking]**` note in `CHANGELOG.md`'s entry for that version. Don't bundle a breaking
  change into a minor/patch bump even if it started as an unrelated task (e.g. "just match another repo's
  Gradle/JDK version").
- Changelog bullets are one sentence of at most 20 words in the form `Area: Past-tense verb outcome` (areas such
  as `BackEnd`, `DevEx`, `Build`, `Docs`, `CLI`); add one under the active `-pre` heading with every user-visible change.
