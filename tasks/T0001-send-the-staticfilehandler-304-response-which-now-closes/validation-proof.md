## Walkthrough & Validation

### Red Phase
Command, before the fix: `gradlew.bat test --rerun --tests "com.pellicciotta.support.http.JdkHttpServerWebServerTest" --console=plain --quiet`

Exit code: 1

```text
[... 6 lines omitted ...]
11 tests completed, 2 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/support.java/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 6s
```

Failures from `build/test-results/test/TEST-com.pellicciotta.support.http.JdkHttpServerWebServerTest.xml`:

```text
  <testcase name="testMatchingIfModifiedSinceForAStaticFileGets304()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.095">
    <failure message="java.io.IOException: HTTP/1.1 header parser received no bytes" type="java.io.IOException">java.io.IOException: HTTP/1.1 header parser received no bytes
  <testcase name="testAHandlerThatOnlySetsAStatusIsAnswered()" classname="com.pellicciotta.support.http.JdkHttpServerWebServerTest" time="0.048">
    <failure message="java.io.IOException: HTTP/1.1 header parser received no bytes" type="java.io.IOException">java.io.IOException: HTTP/1.1 header parser received no bytes
```

### Green Phase
Command, after the fix: `python scripts/devops.py check`

Exit code: 0

```text
[... 2085 lines omitted ...]
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#168,Execution worker,5,main]) started.
:build (Thread[#168,Execution worker,5,main]) started.

> Task :build
Skipping task ':build' as it has no actions.

BUILD SUCCESSFUL in 41s
17 actionable tasks: 14 executed, 3 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\support.java]
$ C:\Dev-Projects\support.java\gradlew.bat build --console=plain
```

Target test class, from its XML report:

```text
<testsuite name="com.pellicciotta.support.http.JdkHttpServerWebServerTest" tests="11" skipped="0" failures="0" errors="0" timestamp="2026-11-02T22:46:07.875Z" hostname="GAMINGPC" time="1.412">
```

Skipped tests in the run:

```text
HttpUtilsTest > adhocEncodeDecode() SKIPPED
MailBackEndTest > sendWithGmailTest() SKIPPED
MailBackEndTest > sendWithSendGridTest() SKIPPED
MailBackEndTest > sendWithGmailTest2() SKIPPED
SqlPagingTest > testFetchAll() SKIPPED
SqlPagingTest > testColumnValuesBasedPaging() SKIPPED
SqlPagingTest > testIdColumnBasedPaging() SKIPPED
```

Docker is absent on this machine, so `SqlPagingTest` skipped locally; CI runs it.
