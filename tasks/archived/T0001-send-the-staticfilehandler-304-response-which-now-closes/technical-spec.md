## Technical Specifications

### Root Cause
`StaticFileHandler.handle` sets status 304 and returns `true` without touching the response body.
`HttpExchangeResponse` sends headers only on first body access, so nothing has gone out.
`JdkHttpServerWebServer.dispatch` then calls `exchange.close()`, which drops a connection with no response started.
The client sees a closed connection with no bytes.
Any handler or `HttpRoute` processor that only sets a status has the same defect.

### Decisions
- **[Decided]** Fix `dispatch`, not `StaticFileHandler`: `HttpRequestHandler.handle` returning `true` means the response is complete and sent.
- **[Decided]** Add package-private `HttpExchangeResponse.finish()`, which sends headers when no handler has.
- **[Decided]** Release as v1.0.1: this is a non-breaking fix and the active heading is already `v1.0.1-pre`.

### Declared Files
- src/main/java/com/pellicciotta/support/http/JdkHttpServerWebServer.java
- src/main/java/com/pellicciotta/support/http/HttpExchangeResponse.java
- src/test/java/com/pellicciotta/support/http/JdkHttpServerWebServerTest.java
- CHANGELOG.md

Size estimate: 4 files, 70 lines

### Tests
- Unit test: none (the defect lives in the JDK exchange lifecycle; `FakeResponse` cannot reproduce it, and `StaticFileHandlerTest` already covers the handler's 304 decision)
- Integration: a real `JdkHttpServerWebServer` with `StaticFileHandler` answers a matching `If-Modified-Since` GET with 304, no body, and `Last-Modified`.
- Integration: a real `JdkHttpServerWebServer` answers handlers that only set status 304, 204, or 200 with that status and no body, without JDK no-body warnings.

Completion check: python scripts/devops.py check
