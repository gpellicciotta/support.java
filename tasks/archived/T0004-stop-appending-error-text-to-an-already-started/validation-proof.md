## Walkthrough & Validation

### Red Phase
Unit test command before the fix: `.\gradlew.bat test --rerun --tests "com.pellicciotta.support.http.HttpExchangeResponseTest" --console=plain --quiet`

Exit code: 1

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader in an unnamed module (file:/C:/Users/giova/.gradle/wrapper/dists/gradle-9.7.0-bin/d4tj7w02tcgubx9zk9hbippn6/gradle-9.7.0/lib/native-platform-0.22-milestone-29.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.

10 tests completed, 3 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/support.java/.plan/work/T0004-stop-appending-error-text-to-an-already-started/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 10s
```

Slow integration test command before the fix: `.\gradlew.bat slowTest --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest.testHandlerThrowingMidChunkedResponseDoesNotAppendErrorText" --console=plain --quiet`

Exit code: 1

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by net.rubygrapefruit.platform.internal.NativeLibraryLoader in an unnamed module (file:/C:/Users/giova/.gradle/wrapper/dists/gradle-9.7.0-bin/d4tj7w02tcgubx9zk9hbippn6/gradle-9.7.0/lib/native-platform-0.22-milestone-29.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled


1 test completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':slowTest'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/support.java/.plan/work/T0004-stop-appending-error-text-to-an-already-started/build/reports/tests/slowTest/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 5s
```

Red-phase XML failure excerpts (`TEST-com.pellicciotta.support.http.HttpExchangeResponseTest.xml` and `TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml`):

```text
  <testcase name="testSendErrorAfterPartialFixedLengthWriteThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.022">
    <failure message="org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown." type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown.
  <testcase name="testSendErrorAfterFlushedChunkedWriterThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.004">
    <failure message="org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown." type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown.
  <testcase name="testSendErrorAfterUnflushedChunkedStreamWriteThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.004">
    <failure message="org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown." type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: Expected java.io.IOException to be thrown, but nothing was thrown.
  <testcase name="testHandlerThrowingMidChunkedResponseDoesNotAppendErrorText()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.448">
    <failure message="org.opentest4j.AssertionFailedError: expected: &lt;{&quot;items&quot;:[&gt; but was: &lt;{&quot;items&quot;:[Internal server error&gt;" type="org.opentest4j.AssertionFailedError">org.opentest4j.AssertionFailedError: expected: &lt;{&quot;items&quot;:[&gt; but was: &lt;{&quot;items&quot;:[Internal server error&gt;
```

### Green Phase
HTTP unit test, slow server test, and static checks command: `.\gradlew.bat test --rerun --tests "com.pellicciotta.support.http.*" slowTest --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest" checkstyleMain checkstyleTest spotlessCheck --console=plain`

Exit code: 0

```text
[... 923 lines omitted ...]
> Task :checkstyleMain
Stored cache entry for task ':checkstyleMain' with cache key a1c052d5548ec369d3fbc2a85a209d93

BUILD SUCCESSFUL in 23s
11 actionable tasks: 8 executed, 1 from cache, 2 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0004-stop-appending-error-text-to-an-already-started, C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add, C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2, C:\Dev-Projects\todos.server]
Stopped 1 worker daemon(s).
```

Affected slow integration test command: `.\gradlew.bat slowTest --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest.testHandlerThrowingMidChunkedResponseDoesNotAppendErrorText" --console=plain --quiet`

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
<testsuite name="com.pellicciotta.support.http.JdkHttpServerWebServerTest" tests="1" skipped="0" failures="0" errors="0" timestamp="2026-11-11T09:47:20.194Z" hostname="GAMINGPC" time="0.46">
  <properties/>
  <testcase name="testHandlerThrowingMidChunkedResponseDoesNotAppendErrorText()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.267"/>
  <system-out><![CDATA[WebServer started on port 55353 with 1 handler(s)
[... 55 lines omitted ...]
GET /unflushed-writer-then-throw -> 500 (1ms)
GET /unhandled-after-chunked-write -> 200 (0ms)
GET /healthy -> 200 (0ms)
WebServer on port 55353 stopped
]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

Unit test XML report (`build/test-results/test/TEST-com.pellicciotta.support.http.HttpExchangeResponseTest.xml`):

```text
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.pellicciotta.support.http.HttpExchangeResponseTest" tests="10" skipped="0" failures="0" errors="0" timestamp="2026-11-11T09:45:00.355Z" hostname="GAMINGPC" time="0.159">
  <properties/>
  <testcase name="testSendErrorBeforeBytesWrittenReplacesStatusHeadersAndSendsErrorBody()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.083"/>
  <testcase name="testWritingBodyAfterPositiveContentLengthSendsDeclaredLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.01"/>
  <testcase name="testClosingOutputStreamWithoutWritingAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.001"/>
  <testcase name="testFlushingEmptyWriterAfterPositiveContentLengthSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.002"/>
  <testcase name="testSendErrorAfterPartialFixedLengthWriteThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.004"/>
  <testcase name="testWritingBodyWithoutContentLengthUsesChunkedEncoding()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.002"/>
  <testcase name="testSendErrorAfterFlushedChunkedWriterThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.002"/>
  <testcase name="testSendErrorAfterUnflushedChunkedStreamWriteThrowsAndLeavesStartedBodyUntouched()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.003"/>
  <testcase name="testFinishAfterPositiveContentLengthWithNoStreamAccessSendsNoBodyLength()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.002"/>
  <testcase name="testZeroContentLengthFinalizesEmptyOrStreamsChunkedWhenWritten()" classname="com.pellicciotta.support.http.HttpExchangeResponseTest" time="0.001"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

Full fast suite, Javadoc, and static checks command: `.\gradlew.bat test javadoc checkstyleMain checkstyleTest spotlessCheck --console=plain`

Exit code: 0

```text
[... 1641 lines omitted ...]
> Task :spotlessCheck UP-TO-DATE
Skipping task ':spotlessCheck' as it has no actions.

BUILD SUCCESSFUL in 31s
11 actionable tasks: 2 executed, 9 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java\.plan\work\T0004-stop-appending-error-text-to-an-already-started, C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add, C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2, C:\Dev-Projects\todos.server]
```

Version parity check command: `C:\Dev-Projects\support.java\.venv\Scripts\python.exe scripts/devops.py versions`

Exit code: 0

```text
Version file: 1.0.2-pre
CHANGELOG.md: 1.0.2-pre
Versions agree.
```
