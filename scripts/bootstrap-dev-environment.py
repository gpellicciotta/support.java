#!/usr/bin/env python3
# Copyright (c) Giovanni Pellicciotta
"""Bootstrap the support.java development environment: init git if needed, build, and test."""

import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent


def run(args: list[str]) -> None:
    print(f"$ {' '.join(args)}")
    subprocess.run(args, cwd=REPO_ROOT, check=True)


def main() -> int:
    if not (REPO_ROOT / ".git").exists():
        run(["git", "init"])

    gradlew = REPO_ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
    if not gradlew.exists():
        print(f"error: Gradle wrapper not found at {gradlew}", file=sys.stderr)
        return 1

    # The toolchain block in build.gradle pins the exact JDK version (auto-provisioned by
    # Gradle if not already installed) - no separate environment bootstrap step is needed
    # the way Python's venv requires one.
    run([str(gradlew), "build"])
    return 0


if __name__ == "__main__":
    sys.exit(main())
