# Versioned Changes

A summarized overview of all changes, per version of this project.

> Entries are in reverse chronological order, most recent at the top.
>
> A version actively being developed carries a `-pre` SemVer pre-release suffix on its heading (e.g.
> `## v5.0.0-pre`) instead of a status tag. Status codes used once a version leaves that state are:
> - `[{{date}}]` - frozen/finalized on {{date}}
> - `[released: {{date}}]` - released to package manager or production on {{date}}
> - `[broken]` - considered broken and not be used

## Package Repository

All releases are available on:
- https://github.com/gpellicciotta/support.java/packages/

---

## v1.0.0-pre

- Initial release of the general-purpose Java support library (`com.pellicciotta:support.java`); requires JDK 25 or later.
- `logging`: `LogService` front-end handing out `Log` instances backed by `jul`, `stdout`, `file`, and `noop` back-ends and a `gcp` structured-log formatter; `Span` for hierarchical, timed operations with `ContextData`.
- `config`: `SettingsFactory` builds a tree of described, defaulted settings with JSON/XML/Map/properties/command-line (de)serialization; command line overrides environment variables, which override the config file.
- `codecs`: encode/decode pairs between string, XML, and property representations.
- `app`: `BasicApp` template for CLI apps with config loading, logging setup, and `@CommandLineAction` dispatch, plus bundled `charsets`, `diff`, `timezones`, and `httpd` tools behind `ToolDispatcher`.
- `http`: server-agnostic `Request`/`Response` API with routing (`@WebRoute`/`@WebRoutes`), a JDK `HttpServer`-based `WebServer`, and `StaticFileHandler` for file system and classpath resources, including `Last-Modified`/`If-Modified-Since` support.
- `sql`: JDBC helpers with `SqlExecutor`, dialect-aware `SqlGenerator`, paging, a hand-written SQL tokenizer/parser, and versioned statements for schema migrations.
- `mail`: `MailService` over JavaMail with a dummy back-end for tests.
- `diff`: Myers (greedy, linear-space, reverse-greedy), histogram, and patience diff algorithms behind one interface.
- `text`: tokenizer, syntax tree, and text/HTML builders.
- `utils`, `cache`, `io`, `exceptions`, `annotations`: general helpers, caches and resource pools, CSV and stream I/O, shared exception types, and declarative markers.

---
