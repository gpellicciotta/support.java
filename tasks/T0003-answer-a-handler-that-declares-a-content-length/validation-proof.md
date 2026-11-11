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
Affected slow integration test command: `.\gradlew.bat slowTest --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest.testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered" --console=plain`

Exit code: 0

```text
[... 100 lines omitted ...]
JdkHttpServerWebServerTest > testHandlerDeclaringContentLengthWithoutWritingBodyIsAnswered() STANDARD_OUT
    WebServer started on port 64961 with 1 handler(s)
    GET /no-stream-200 -> 200 (61ms)
    GET /closed-empty-stream -> 200 (1ms)
    GET /open-empty-writer -> 200 (0ms)
    GET /zero-length -> 200 (0ms)
    GET /no-stream-204 -> 204 (0ms)
    GET /no-stream-304 -> 304 (2ms)
    GET /normal -> 200 (1ms)
    WebServer on port 64961 stopped

Gradle Test Executor 1 finished executing tests.

> Task :slowTest
Generating HTML test report...
Finished generating test html results (0.119 secs) into: C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\build\reports\tests\slowTest
Finished generating test XML results (0.023 secs) into: C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\build\test-results\slowTest
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\fileHashes\fileHashes.bin
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\fileHashes\resourceHashesCache.bin
Invalidating in-memory cache of C:\Users\giova\.gradle\caches\journal-1\file-access.bin
Stored cache entry for task ':slowTest' with cache key b659930df0ab7deebcb279a637798caa
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\executionHistory\executionHistory.bin
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\buildOutputCleanup\outputFiles.bin

BUILD SUCCESSFUL in 26s
4 actionable tasks: 1 executed, 3 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length]
```

Fast suite and static checks command: `.\gradlew.bat test checkstyleMain checkstyleTest spotlessCheck --console=plain`

Exit code: 0

```text
[... 1804 lines omitted ...]
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\fileHashes\fileHashes.bin
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\fileHashes\resourceHashesCache.bin
Invalidating in-memory cache of C:\Users\giova\.gradle\caches\journal-1\file-access.bin
Stored cache entry for task ':checkstyleTest' with cache key b63bfb8295b82cda2db2007eb0e0f28a
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\9.7.0\executionHistory\executionHistory.bin
Invalidating in-memory cache of C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length\.gradle\buildOutputCleanup\outputFiles.bin

BUILD SUCCESSFUL in 34s
10 actionable tasks: 5 executed, 5 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length, C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2]
Stopped 1 worker daemon(s).
```

XML report summaries (`test` executes fast unit tests; `slowTest` executes the `@Tag("slow")` integration test):

```text
build/test-results/test/TEST-com.pellicciotta.support.http.HttpExchangeResponseTest.xml 6 0 0
build/test-results/test/TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml 11 0 0
build/test-results/slowTest/TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml 1 0 0
```

Version parity check command: `C:\Dev-Projects\support.java\.venv\Scripts\python.exe scripts/devops.py versions`

Exit code: 0

```text
Version file: 1.0.2-pre
CHANGELOG.md: 1.0.2-pre
Versions agree.
```
