# Versioned Changes

A summarized overview of all changes, per version of this project.

> Entries are in reverse chronological order, most recent at the top.
>
> An active in-development version carries a `-pre` suffix (e.g. `v1.0.0-pre`) on both this heading and
> `gradle.properties`'s `version`. On release, the suffix is replaced with a status tag:
> - `[{{date}}]` - frozen/finalized on {{date}}
> - `[released: {{date}}]` - released to package manager or production on {{date}}
> - `[broken]` - considered broken and not to be used

## Package Repository
All releases are available on [GitHub Packages](https://github.com/gpellicciotta/support.java/packages).

---

## v1.0.1-pre

---

## v1.0.0 [released: 2026-10-31]
- BackEnd: Added `logging` with `jul`, `stdout`, `file`, `noop`, and `gcp` back-ends plus hierarchical, timed `Span` operations.
- Config: Added `config`, a typed settings tree loaded from command line, environment, and file with JSON, XML, and properties serialization.
- Config: Added `codecs` for encoding settings between string, XML, and property representations.
- CLI: Added `BasicApp` for command-line apps and bundled `charsets`, `diff`, `timezones`, and `httpd` tools behind `ToolDispatcher`.
- BackEnd: Added a server-agnostic `http` layer on the JDK `HttpServer` with routing and `StaticFileHandler` serving files and classpath resources.
- BackEnd: Added `sql` helpers: `SqlExecutor`, dialect-aware generation, paging, a SQL tokenizer and parser, and versioned migrations.
- BackEnd: Added `mail`, a `MailService` over JavaMail with a dummy back-end for tests.
- BackEnd: Added `diff` with Myers (greedy, linear-space, reverse), histogram, and patience algorithms behind one interface.
- BackEnd: Added `text` with a tokenizer, syntax tree, and text and HTML builders.
- BackEnd: Added `utils`, `cache`, `io`, `exceptions`, and `annotations` with shared helpers, caches, CSV I/O, and declarative markers.
- Build: Added a Gradle build with a JDK 25 toolchain, Spotless, JUnit 6, Testcontainers, and publishing to GitHub Packages.
- Docs: Documented the full public API with Javadoc and a `package-info.java` for every package.
- Build: Enforced Stroustrup braces, mandatory control-block braces, and public API Javadoc with Checkstyle.
- DevEx: Added `devops.py` and the `doFullRelease` pipeline for CI-equivalent checks and one-command releases.

---
