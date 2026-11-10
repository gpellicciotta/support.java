## Walkthrough & Validation
Replaced `scripts/devops.py` with a `DevopsCli` on `GradleProject`, pinned support-python v2.0.0, and added fast and slow suites.

Command: `.venv/Scripts/python -m unittest discover -s scripts/tests`, exit code 0.
```text
.........
----------------------------------------------------------------------
Ran 9 tests in 0.603s

OK
exit=0
```

Command: `python scripts/devops.py versions`, exit code 0.
```text
Version file: 1.0.2-pre
CHANGELOG.md: 1.0.2-pre
Versions agree.
exit=0
```

Command: `python scripts/devops.py check`, exit code 0 (final lines; full log has about 1600 lines).
```text
[... 1625 lines omitted ...]
BUILD SUCCESSFUL in 31s
18 actionable tasks: 9 executed, 9 up-to-date
```
