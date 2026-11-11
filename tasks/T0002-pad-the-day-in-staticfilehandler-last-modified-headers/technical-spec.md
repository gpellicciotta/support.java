## Technical Specifications

### Root Cause
`StaticFileHandler.handle` formats `Last-Modified` with `DateTimeFormatter.RFC_1123_DATE_TIME`, which emits single-digit days unpadded (for example, `Sun, 6 Nov 1994 08:49:37 GMT`).
RFC 9110 Section 5.6.7 requires senders to emit `IMF-fixdate` with a zero-padded two-digit day (`Sun, 06 Nov 1994 08:49:37 GMT`).

### Decisions
- **[Decided]** Build a private `IMF-fixdate` `DateTimeFormatter` in `StaticFileHandler` with fixed English weekday/month maps and two-digit day padding.
- **[Decided]** Keep `DateTimeFormatter.RFC_1123_DATE_TIME` in `isNotModifiedSince` so conditional requests accept both padded `IMF-fixdate` and unpadded single-digit days.
- **[Decided]** Keep version at `v1.0.2-pre` because emitting RFC 9110 two-digit days fixes non-compliant output without breaking callers.

### Declared Files
- src/main/java/com/pellicciotta/support/http/StaticFileHandler.java
- src/test/java/com/pellicciotta/support/http/StaticFileHandlerTest.java
- src/test/java/com/pellicciotta/support/http/JdkHttpServerWebServerTest.java
- CHANGELOG.md

Size estimate: 4 files, 75 lines

### Tests
- Unit: `StaticFileHandlerTest` verifies single-digit-day `Last-Modified` formatting (`Sun, 06 Nov 1994 08:49:37 GMT`) and 304 matching for padded and unpadded `If-Modified-Since`.
- Integration: `JdkHttpServerWebServerTest` serves a single-digit-day static file over HTTP, verifying padded `Last-Modified` and 304 conditional GET round-trip.

Completion check: .\gradlew.bat build --console=plain
