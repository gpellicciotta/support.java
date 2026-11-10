#!/usr/bin/env python3
# Copyright (c) Giovanni Pellicciotta
"""One-shot dev environment bootstrap: init git if needed, install scripts/requirements.txt into .venv, run a full Gradle build.

Standard library only, so it runs on a bare checkout before support-python is installed.
"""

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VENV_DIR = ROOT / ".venv"
VENV_PYTHON = VENV_DIR / ("Scripts/python.exe" if sys.platform == "win32" else "bin/python")


def run(argv: list) -> None:
    print(f"$ {' '.join(str(arg) for arg in argv)}", flush=True)
    subprocess.run([str(arg) for arg in argv], cwd=ROOT, check=True)


def main() -> int:
    if not (ROOT / ".git").exists():
        run(["git", "init"])

    if not VENV_PYTHON.is_file():
        run([sys.executable, "-m", "venv", VENV_DIR])
    run([VENV_PYTHON, "-m", "pip", "install", "--quiet", "--disable-pip-version-check", "-r", ROOT / "scripts" / "requirements.txt"])

    gradlew = ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
    run([gradlew, "build"])
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except subprocess.CalledProcessError as exc:
        print(f"**[ERROR]** Bootstrap step failed with exit code {exc.returncode}: {' '.join(map(str, exc.cmd))}", file=sys.stderr)
        sys.exit(1)
