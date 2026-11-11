## Technical Specifications

### Root Cause
`HttpExchangeResponse` sends `this.contentLength` to `HttpExchange.sendResponseHeaders` on the first `getOutputStream()` call or inside `finish()`.
When a handler declares a positive length via `setContentLength` and writes nothing, `FixedLengthOutputStream.close()` aborts and drops the connection.
Additionally, `setContentLength(0)` currently passes `0` to `sendResponseHeaders`, selecting chunked encoding instead of `-1` (`Content-Length: 0`).

### Decisions
- **[Decided]** Defer `sendResponseHeaders` on body-capable responses until the first byte write, sending `-1` on empty `close()` or `finish()`.
- **[Decided]** Keep immediate `-1` header transmission and `OutputStream.nullOutputStream()` for protocol body-less responses (`HEAD`, `1xx`, `204`, `304`).
- **[Decided]** Map `setContentLength(0)` to `-1` in `sendResponseHeaders` and reject non-empty writes when `contentLength == 0` on body-capable responses.
- **[Decided]** Keep version at `v1.0.2-pre` because answering empty-body handlers fixes a connection-drop bug without breaking callers.

### Declared Files
- src/main/java/com/pellicciotta/support/http/HttpExchangeResponse.java
- src/test/java/com/pellicciotta/support/http/HttpExchangeResponseTest.java
- src/test/java/com/pellicciotta/support/http/JdkHttpServerWebServerTest.java
- CHANGELOG.md

Size estimate: 4 files, 220 lines

### Tests
- Unit: `HttpExchangeResponseTest` verifies `-1` header length when handlers declare positive or zero `Content-Length` without writing bytes, plus normal/chunked writes.
- Integration: `JdkHttpServerWebServerTest` (`@Tag("slow")`, run via `slowTest`) verifies real HTTP responses and keep-alive when handlers declare `Content-Length` without writing.

Completion check: .\gradlew.bat build --console=plain
