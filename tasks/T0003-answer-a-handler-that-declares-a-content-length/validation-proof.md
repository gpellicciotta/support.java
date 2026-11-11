## Walkthrough & Validation

### Red Phase
Unit test command before the fix: `.\gradlew.bat test --rerun --tests "com.pellicciotta.support.http.HttpExchangeResponseTest" --console=plain --quiet`

Exit code: 1

```text
[... 8 lines omitted ...]
6 tests completed, 4 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/support.java/.plan/work/T0003-answer-a-handler-that-declares-a-content-length/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 11s
```

Slow integration test command before the fix: `.\gradlew.bat slowTest --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest.testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered" --console=plain --quiet`

Exit code: 1

```text
[... 6 lines omitted ...]
1 test completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':slowTest'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/support.java/.plan/work/T0003-answer-a-handler-that-declares-a-content-length/build/reports/tests/slowTest/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 6s
```

Red-phase XML failure excerpts (`TEST-com.pellicciotta.support.http.HttpExchangeResponseTest.xml` and `TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml`):

```text
  <testcase name="testClosingOutputStreamWithoutWritingAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.029">
    <failure message="org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;
  <testcase name="testFlushingEmptyWriterAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.004">
    <failure message="org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;
  <testcase name="testZeroContentLengthSendsNoBodyLengthAndRejectsNonEmptyWrite()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.004">
    <failure message="org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;0&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;0&gt;
  <testcase name="testFinishAfterPositiveContentLengthWithNoStreamAccessSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.003">
    <failure message="org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;-1&gt; but was: &lt;5&gt;
  <testcase name="testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.544">
    <failure message="java.io.IOException: HTTP/1.1 header parser received no bytes" type="java.io.IOException">java.io.IOException: HTTP/1.1 header parser received no bytes
Caused by: java.io.EOFException: EOF reached while reading
```

### Green Phase
Affected slow integration test command: `.\gradlew.bat slowTest --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest.testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered" --console=plain --quiet`

Exit code: 0

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader in an unnamed module (file:/C:/Users/giova/.gradle/wrapper/dists/gradle-9.7.0-bin/d4tj7w02tcgubx9zk9hbippn6/gradle-9.7.0/lib/native-platform-0.22-milestone-29.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```

Slow integration test XML report (`build/test-results/slowTest/TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml`):

```text
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.pellicciotta.support.http.JdkHttpServerWebServerTest" tests="1" skipped="0" failures="0" errors="0" timestamp="2026-11-11T09:21:23.950Z" hostname="GAMINGPC" time="0.78">
  <properties/>
  <testcase name="testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.467"/>
  <system-out><![CDATA[WebServer started on port 49925 with 1 handler(s)
GET /no-stream-200 -> 200 (38ms)
GET /closed-empty-stream -> 200 (1ms)
GET /open-empty-writer -> 200 (1ms)
GET /zero-length -> 200 (0ms)
GET /no-stream-204 -> 204 (0ms)
GET /no-stream-304 -> 304 (1ms)
GET /zero-length-written -> 200 (1ms)
GET /normal -> 200 (1ms)
WebServer on port 49925 stopped
]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

Fast suite and static checks command: `.\gradlew.bat test --rerun checkstyleMain checkstyleTest spotlessCheck --console=plain`

Exit code: 0

```text
[... 1920 lines omitted ...]
Stored cache entry for task ':checkstyleMain' with cache key 45be78a92cca39c360a3f4c397d830a2

BUILD SUCCESSFUL in 33s
10 actionable tasks: 5 executed, 5 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length, C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc]
Stopped 1 worker daemon(s).
```

HTTP unit test and static checks command: `.\gradlew.bat test --rerun --tests "com.pellicciotta.support.http.*" checkstyleMain checkstyleTest spotlessCheck --console=plain`

Exit code: 0

```text
[... 560 lines omitted ...]
Stored cache entry for task ':checkstyleTest' with cache key 73101296a0608531aa750ebc2349512c

BUILD SUCCESSFUL in 16s
10 actionable tasks: 5 executed, 5 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length, C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc]
Stopped 1 worker daemon(s).
```

Unit test XML report (`build/test-results/test/TEST-com.pellicciotta.support.http.HttpExchangeResponseTest.xml`):

```text
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.pellicciotta.support.http.HttpExchangeResponseTest" tests="6" skipped="0" failures="0" errors="0" timestamp="2026-11-11T09:20:53.441Z" hostname="GAMINGPC" time="0.245">
  <properties/>
  <testcase name="testWritingBodyAfterPositiveContentLengthSendsDeclaredLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.153"/>
  <testcase name="testClosingOutputStreamWithoutWritingAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.009"/>
  <testcase name="testFlushingEmptyWriterAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.001"/>
  <testcase name="testWritingBodyWithoutContentLengthUsesChunkedEncoding()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.002"/>
  <testcase name="testFinishAfterPositiveContentLengthWithNoStreamAccessSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.001"/>
  <testcase name="testZeroContentLengthFinalizesEmptyOrStreamsChunkedWhenWritten()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.001"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

Version parity check command: `C:\Dev-Projects\support.java\.venv\Scripts\python.exe scripts/devops.py versions`

Exit code: 0

```text
Version file: 1.0.2-pre
CHANGELOG.md: 1.0.2-pre
Versions agree.
```
