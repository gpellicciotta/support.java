#!/usr/bin/env python3
# Copyright (c) Giovanni Pellicciotta
"""Development operations runner for support.java, built on support_python.devops.

Run `python scripts/devops.py help --verbose` for every action. A `.venv` created by
`scripts/bootstrap-dev-environment.py` holds support-python; this script re-runs itself there when present.
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
VENV_DIR = REPO_ROOT / ".venv"
VENV_PYTHON = VENV_DIR / ("Scripts/python.exe" if sys.platform == "win32" else "bin/python")

if __name__ == "__main__" and VENV_PYTHON.is_file() and Path(sys.prefix).resolve() != VENV_DIR.resolve():
    sys.exit(subprocess.run([str(VENV_PYTHON), str(Path(__file__).resolve()), *sys.argv[1:]], check=False).returncode)

try:
    from support_python.devops import DevopsCli, GradleProject
    from support_python.devops.steps import resolve_executable
except ImportError as exc:
    sys.exit(f"**[ERROR]** support-python v2 is missing ({exc}); run `python scripts/bootstrap-dev-environment.py` first.")


class SupportJavaProject(GradleProject):
    """Gradle project whose `lint` and `check` also run `lint-markdown check`, stopping at the first failure."""

    def lint(self) -> bool:
        return self.lint_markdown() and super().lint()

    def check(self) -> bool:
        return self.lint_markdown() and super().check()

    def lint_markdown(self) -> bool:
        """Lint every Markdown file in the repository, honoring `.lint-markdown.json`."""
        return self.run_command([resolve_executable("lint-markdown") or "lint-markdown", "check"])


def build_cli(root: Path = REPO_ROOT) -> DevopsCli:
    """Return the devops command line; `release` runs the Gradle `doFullRelease` pipeline after a typed confirmation."""
    cli = DevopsCli(SupportJavaProject(root, release_task="doFullRelease", release_args=["-PreleaseConfirmed=yes"]))
    cli.harness.set_examples(["devops check", "devops test com.pellicciotta.support.text.TokenizerTest", "devops versions", "devops release --dry-run"])
    return cli


if __name__ == "__main__":
    sys.exit(build_cli().run())
