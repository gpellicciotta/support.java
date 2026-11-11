---
id: T0005
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T19:19:22Z"
ended_at: "2026-11-10T20:45:43Z"
review: agent
preview: none
protocol: standard
review_status: approved
review_round: 1
reviewed_by: "@codex"
completion-commit: 73fba8047f5d2a3d5673b820b2ea728f79dc3957
---
# T0005: Replace scripts/devops.py with a thin DevopsCli script on the GradleProject adapter, pinning the released support-python v2.0.0

## Goals
Needs the released support-python v2.0.0; see `docs/migration-v2.md` in support-python for the full mapping.
Replace `scripts/devops.py` with a thin script running `DevopsCli(GradleProject(REPO_ROOT)).run()`.
Pin `support-python @ git+https://github.com/gpellicciotta/support.python.git@v2.0.0` in `scripts/requirements.txt`.
Override the Gradle task lists or set `release_task` where the current script differs from the defaults.
Keep project-specific actions through `DevopsCli.add_action`.

## Task Execution Steps
- [x] **[Implement]** Implement required changes for T0005.
- [x] **[Verify]**    Verify implementation with automated test suites.
- [x] **[Doc]**       Update documentation and CHANGELOG.md.
- [x] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Implement]**
  Replaced devops.py with DevopsCli on support-python v2.0.0; aligned AGENTS.md, devops docs, CI, and test suites.

- [2026-11-10] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-10] **[Review]**
  Round 1 approved by @codex: Codex review of commit 73fba80 found no actionable defects.

- [2026-11-10] **[Verify]**
  Recorded task outcomes.
  - Files touched: 16.
  - Lines touched: 757.
  - Reopenings: 0.

- [2026-11-10] **[Complete]**
  Task completed successfully.

