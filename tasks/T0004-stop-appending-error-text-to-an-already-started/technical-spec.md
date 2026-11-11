## Technical Specifications

### Root Cause
`JdkHttpServerWebServer.dispatch` calls `response.sendError(500, "Internal server error")` whenever a handler throws an `Exception`.
`HttpExchangeResponse.sendError` writes the error message to `getOutputStream()`, while `ensureHeadersSent()` skips `sendResponseHeaders` once `headersSent` is `true`.
When a handler throws after starting a chunked body, `sendError` appends `"Internal server error"` to the open stream.

### Decisions
- **[Decided]** Throw `IOException` from `HttpExchangeResponse.sendError` when `headersSent` is `true`, leaving committed status, headers, and body untouched.
- **[Decided]** Expose package-private `HttpExchangeResponse.headersSent()` and guard fallback `404` and `500` `sendError` calls in `JdkHttpServerWebServer.dispatch`.
- **[Decided]** Keep version at `v1.0.2-pre` because stopping late error writes fixes response corruption without breaking valid callers.

### Declared Files
- src/main/java/com/pellicciotta/support/http/HttpExchangeResponse.java
- src/main/java/com/pellicciotta/support/http/JdkHttpServerWebServer.java
- src/test/java/com/pellicciotta/support/http/HttpExchangeResponseTest.java
- src/test/java/com/pellicciotta/support/http/JdkHttpServerWebServerTest.java
- CHANGELOG.md

Size estimate: 5 files, 150 lines

### Tests
- Unit: `HttpExchangeResponseTest` verifies `sendError` throws `IOException` without appending bytes after chunked or partial fixed-length writes, while succeeding before headers are sent.
- Integration: `JdkHttpServerWebServerTest` (`@Tag("slow")`, run via `slowTest`) verifies mid-response chunked failures do not append error text while pre-header failures still return `500`.

Completion check: .\gradlew.bat build --console=plain
