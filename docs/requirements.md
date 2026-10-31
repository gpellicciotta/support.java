# Requirements

Functional and technical requirements for the `support.java` project.

---

## High-Level Goals
- Provide a general-purpose Java support library, usable in most Java projects, with as few third-party
  dependencies as possible so downstream consumers aren't burdened by transitive dependencies.
- Serve as the shared foundation consumed by other single-process apps with an embedded HTTP server: `counters`, `auth`, `todos`.
- Publish releases as a Maven package (group `com.pellicciotta`, artifact `support.java`) on GitHub Packages.

---

## Functional Requirements

### Logging
- Provide a singleton front-end/factory (`LogService`) handing out `Log` instances backed by a configurable
  back-end (`java.util.logging`, stdout, no-op, or GCP structured-log formatting).
- Support structured, hierarchical, timed operations (`Span`) with attached key/value context data.

### Configuration
- Build a typed settings tree (`SettingsFactory` → `Config`/`SettingsGroup`/`Setting`) from multiple sources
  (command-line, environment variables, config file) composed with a defined precedence.
- Support (de)serialization of settings to/from JSON, XML, `Map`, properties, and command-line arguments.

### Application Framework
- Provide a template base (`BasicApp`) for CLI-runnable applications: config loading, logging setup, and
  `@CommandLineAction`-annotated method dispatch.
- Provide a multi-tool dispatcher (`ToolDispatcher`) for this library's own bundled CLI tools (`charsets`,
  `diff`, `timezones`, `httpd`).

### HTTP
- Provide a server-agnostic HTTP layer (`Request`/`Response`, `HttpRequestHandler`/`HttpRoute`/`HttpRoutes`,
  `WebServer`) with no servlet container or external HTTP server dependency, backed by the JDK's own
  `com.sun.net.httpserver.HttpServer`.
- Serve static files from the file system or classpath, including from inside a jar (`StaticFileHandler`).

### SQL/JDBC
- Provide JDBC helpers (`SqlExecutor`), dialect-aware SQL generation and paging, statement-syntax tokenizing, and
  versioned statements for schema migrations.

### Mail
- Provide a mail-sending abstraction (`MailService`/`MailBackEnd`) over JavaMail, with a dummy back-end for
  tests.

### Diffing
- Provide a family of diff algorithms (Myers greedy/linear-space/reverse-greedy, histogram, patience) behind a
  common interface, selectable via `StandardDiffAlgorithms`.

### Text Processing
- Provide tokenizing/lexing (`Tokenizer`/`TokenMatcher`), a resulting syntax tree (`SyntaxNode`/`SyntaxElement`/
  `SyntaxToken`), and text output building (`TextBuilder`/`HtmlTextBuilder`).

### General Utilities
- Provide general-purpose helpers (string/collection/date-time/reflection/file utilities, argument validation,
  tuples) usable across the other packages and by consuming projects.

---

## Technical Requirements
- Java Version: explicitly targeted and compiled with the Java 25 toolchain (`java.toolchain`). Published
  bytecode requires JDK 25 or later to load.
- Build System: Gradle with a pinned Gradle wrapper (`gradlew` / `gradlew.bat`).
- Testing: JUnit 6 Jupiter (`org.junit.jupiter.api` style), plus Testcontainers for the one integration test
  that needs a real database (`SqlPagingTest`, MariaDB).
- Dependencies: as few third-party dependencies as possible; each one explicitly evaluated and justified
  (license, maintenance, uniqueness of functionality) per
  [general-guidelines.md](https://github.com/gpellicciotta/dev-guidelines/blob/main/guidelines/general-guidelines.md).
- Packaging: standard `maven-publish` configuration producing a jar, sources jar, and javadoc jar with manifest
  attributes, published to GitHub Packages.
- Code Standards: 2-space indentation, UTF-8 encoding, LF line endings, US English.
- Versioning: [Semantic Versioning](https://semver.org/); backward-incompatible changes bump the major version
  and get an explicit `**[breaking]**` note in `CHANGELOG.md`.
- Releases: one command (`python scripts/devops.py release`, backed by the Gradle `doFullRelease` pipeline) publishes
  to GitHub Packages, tags, pushes, creates the GitHub Release, and opens the next `-pre` version.
- Dependencies: versions are declared once in [libs.versions.toml](../gradle/libs.versions.toml), with no dynamic version ranges.
