## Technical Specifications

### Root Cause
`HttpExchangeResponse` sends `this.contentLength` to `HttpExchange.sendResponseHeaders` on the first `getOutputStream()` call or inside `finish()`.
When a handler declares a positive length via `setContentLength` and writes nothing, `FixedLengthOutputStream.close()` aborts and drops the connection.

### Decisions
- **[Decided]** Defer `sendResponseHeaders` on body-capable responses until the first byte write, sending `-1` on empty `close()` or `finish()`.
- **[Decided]** Keep immediate `-1` header transmission and `OutputStream.nullOutputStream()` for protocol body-less responses (`HEAD`, `1xx`, `204`, `304`).
- **[Decided]** Preserve chunked encoding (`0`) when `setContentLength(0)` precedes non-empty writes, while finalizing empty responses with `-1`.
- **[Decided]** Keep version at `v1.0.2-pre` because answering empty-body handlers while preserving existing `setContentLength(0)` writes is backward-compatible.

### Declared Files
- src/main/java/com/pellicciotta/support/http/HttpExchangeResponse.java
- src/test/java/com/pellicciotta/support/http/HttpExchangeResponseTest.java
- src/test/java/com/pellicciotta/support/http/JdkHttpServerWebServerTest.java
- CHANGELOG.md

Size estimate: 4 files, 220 lines

### Tests
- Unit: `HttpExchangeResponseTest` verifies `-1` header length for empty bodies, chunked streaming after `setContentLength(0)` writes, and fixed-length/chunked writes.
- Integration: `JdkHttpServerWebServerTest` (`@Tag("slow")`, run via `slowTest`) verifies real HTTP responses and keep-alive for empty bodies and `setContentLength(0)` writes.

Completion check: .\gradlew.bat build --console=plain
