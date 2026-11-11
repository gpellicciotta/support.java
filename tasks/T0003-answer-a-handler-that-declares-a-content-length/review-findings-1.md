---
task: T0003
round: 1
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T09:12:05Z
reviewed_commit: "84e4e56"
base_commit: "91e2cf2"
branch: task/T0003-answer-a-handler-that-declares-a-content-length
---
# Review Findings: Task T0003 (Round 1)

## Summary
**[Decided]** Rejected because the implementation adds an incompatible write restriction without the required major version change.
The deferred headers fix addresses the reported empty-body connection drop.
Affected HTTP tests and static checks passed during review.

Review verification command, exit code 0:

```powershell
.\gradlew.bat test --tests 'com.pellicciotta.support.http.*' slowTest --tests 'com.pellicciotta.support.http.JdkHttpServerWebServerTest' checkstyleMain checkstyleTest spotlessCheck --console=plain --quiet
```

The quiet command emitted JVM native-access warnings and no test or check failures.
The completed slow-test report contains two tests with zero failures and errors.
Recorded red-phase, fast-suite, static-check, and version-parity evidence was also inspected.

## Defects
- P2: `HttpExchangeResponse.ensureHeadersSent()` now maps explicit zero length to `-1`; `BodyOutputStream.prepareWrite()` rejects subsequent nonempty writes.
  Previously, `setContentLength(0)` selected chunked encoding and allowed body writes.
  For example, `setContentLength(0); getOutputStream().write('x')` previously streamed successfully and now throws after committing an empty response.
  This change exceeds the empty-body fix and breaks existing callers relying on that behavior.
  The new zero-length unit test explicitly verifies this incompatible restriction.

## Process Gaps
- `gradle.properties` remains `1.0.2-pre`, and the changelog contains no breaking-change note.
  Project conventions require a major version bump and a `**[breaking]**` note for behavior changes callers may rely on.
  The specification's compatibility claim overlooks the newly rejected zero-length writes.

## Required Rework
- Preserve existing chunked writes when explicit zero length precedes nonempty output, while retaining empty-response finalization.
  Add a regression test for that compatibility case and rerun affected tests and static checks.
- Alternatively, retain the restriction with the required major version bump, breaking-change documentation, and accurate compatibility assessment.
