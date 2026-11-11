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

## v1.0.2-pre
- BackEnd: Fixed dropped connections when a handler called `Response.setContentLength` without writing a response body.
- BackEnd: Zero-padded single-digit days in `StaticFileHandler` `Last-Modified` headers per RFC 9110 `IMF-fixdate`.
- DevEx: Replaced `scripts/devops.py` with a thin DevopsCli on support-python v3.1.0, adding `lint` and `format` actions.
- DevEx: Made the bootstrap script create `.venv` and install the pinned `scripts/requirements.txt`.
- DevEx: Ran `lint-markdown check` before Gradle in `devops lint` and `devops check`.
- Build: Split the tests into a fast `test` suite and a `slowTest` suite tagged `slow`, run by `check`.
- Build: Applied default per-test timeouts of 30 seconds to `test` and 120 seconds to `slowTest`.
- CI: Installed `scripts/requirements.txt` and ran the devops script tests before `devops check`.
- Docs: Restructured `AGENTS.md`, made `CLAUDE.md` import it, and reordered `docs/devops.md` per dev-guidelines v2.0.0.
- Docs: Removed the dead parsing notes link from `docs/index.md` and the `text` package Javadoc.

---

## v1.0.1 [released: 2026-11-03]
- BackEnd: Fixed dropped connections when a handler only set a status, including `StaticFileHandler` 304 answers.

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
