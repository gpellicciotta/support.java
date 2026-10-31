#!/usr/bin/env python3
# Copyright (c) Giovanni Pellicciotta
"""Development operations runner for support.java.

Runs the same validation steps locally that CI runs, so local and CI checks cannot drift apart,
and gives one entry point to the other typical project operations (build, test, versions, release).
Standard library only.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path

TOOL_NAME = "devops"
COPYRIGHT = "Copyright (c) Giovanni Pellicciotta"
DESCRIPTION = "Runs the repository validation steps that CI runs, plus build, test, versions, and release operations."
REPO_ROOT = Path(__file__).resolve().parent.parent

EXIT_OK = 0
EXIT_ERROR = 1
EXIT_USAGE = 2

ACTIONS = {
    "check": "run every CI validation step: Spotless, Checkstyle, tests, and jar assembly (gradlew build)",
    "build": "compile, test, and assemble the jar, sources jar, and Javadoc jar (gradlew build)",
    "test": "run the unit tests only, optionally narrowed with --tests",
    "clean": "delete Gradle build output (gradlew clean)",
    "versions": "show gradle.properties and CHANGELOG.md versions and fail when they drift apart",
    "release": "cut a release with the Gradle doFullRelease pipeline (publishes and pushes)",
    "version": "show version information",
    "help": "show this help; add --verbose for details on every action",
}

EXIT_CODES = [
    (EXIT_OK, "Success"),
    (EXIT_ERROR, "A step failed or a precondition was not met"),
    (EXIT_USAGE, "Invalid arguments"),
]


def read_property(name: str) -> str:
    text = (REPO_ROOT / "gradle.properties").read_text(encoding="utf-8")
    m = re.search(rf"^{re.escape(name)}\s*=\s*(.+?)\s*$", text, re.MULTILINE)
    return m.group(1) if m else ""


def tool_version() -> str:
    return read_property("version") or "0.0.0+unknown"


def print_version() -> None:
    print(f"{TOOL_NAME} v{tool_version()} - {COPYRIGHT}")


def gradlew() -> str:
    return str(REPO_ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew"))


def run(argv: list[str]) -> int:
    print(f"$ {' '.join(argv)}")
    return subprocess.run(argv, cwd=REPO_ROOT, check=False).returncode


def changelog_top_version() -> str:
    text = (REPO_ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
    m = re.search(r"^## v(\S+)", text, re.MULTILINE)
    return m.group(1) if m else ""


def action_versions(_: argparse.Namespace) -> int:
    props = read_property("version")
    changelog = changelog_top_version()
    print(f"gradle.properties : {props}")
    print(f"CHANGELOG.md top  : {changelog}")
    if props != changelog:
        print("**[ERROR]**: versions have drifted apart; they must match exactly, -pre suffix included", file=sys.stderr)
        return EXIT_ERROR
    return EXIT_OK


def action_release(args: argparse.Namespace) -> int:
    version = read_property("version")
    if args.dry_run:
        return run([gradlew(), "doFullRelease", "--dry-run", "--console=plain"])
    if not version.endswith("-pre"):
        print(f"**[ERROR]**: version {version} has no -pre suffix; nothing to release", file=sys.stderr)
        return EXIT_ERROR
    released = version.removesuffix("-pre")
    print(f"OUTBOUND: releases v{released}: publishes to GitHub Packages, pushes main and the tag, creates a GitHub Release.")
    if not args.yes and input(f"Type 'release v{released}' to proceed: ").strip() != f"release v{released}":
        print("aborted", file=sys.stderr)
        return EXIT_ERROR
    return run([gradlew(), "doFullRelease", "-PreleaseConfirmed=yes", "--console=plain"])


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog=TOOL_NAME, add_help=False, description=DESCRIPTION)
    parser.add_argument("action", nargs="?", default="help", choices=list(ACTIONS))
    parser.add_argument("-h", "--help", action="store_true")
    parser.add_argument("--version", action="store_true")
    parser.add_argument("-v", "--verbose", action="store_true", help="with help: describe every action")
    parser.add_argument("--tests", help="test: Gradle --tests filter, e.g. com.pellicciotta.support.text.TokenizerTest")
    parser.add_argument("--dry-run", action="store_true", help="release: only list the Gradle tasks the pipeline would run")
    parser.add_argument("--yes", action="store_true", help="release: skip the typed confirmation")
    return parser


def print_help(verbose: bool) -> None:
    print_version()
    print(f"\n{DESCRIPTION}\n")
    print(f"Usage:\n  {TOOL_NAME} <action> [options]\n\nActions:")
    for name, text in ACTIONS.items():
        print(f"  {name:<10} {text}")
    print("\nOptions:\n  -h, --help       show this help\n  --version        show version\n  -v, --verbose    with help: details per action")
    print("  --tests FILTER   test: narrow the run to a class or method\n  --dry-run        release: preview only\n  --yes            release: skip the confirmation")
    if verbose:
        print("\nDetails:")
        print("  check/build  Gradle's build runs spotlessCheck, checkstyle, tests, javadoc, and jar tasks - exactly what CI runs.")
        print("  release      Needs a clean main pushed to origin with green CI, the gh CLI logged in, and GitHub Packages")
        print("               credentials (github.username/github.token in ~/.gradle/gradle.properties or GITHUB_USERNAME/GITHUB_TOKEN).")
        print("               Steps: build, verify, mark released, publish, tag, push, GitHub Release, open the next -pre version.")
    print("\nExit codes:")
    for code, meaning in EXIT_CODES:
        print(f"  {code}  {meaning}")


def main(argv: list[str]) -> int:
    try:
        args = build_parser().parse_args(argv)
    except SystemExit:
        return EXIT_USAGE
    if args.version or args.action == "version":
        print_version()
        return EXIT_OK
    if args.help or args.action == "help":
        print_help(args.verbose)
        return EXIT_OK
    if args.action in ("check", "build"):
        return run([gradlew(), "build", "--console=plain"])
    if args.action == "test":
        cmd = [gradlew(), "test", "--console=plain"]
        if args.tests:
            cmd += ["--tests", args.tests]
        return run(cmd)
    if args.action == "clean":
        return run([gradlew(), "clean", "--console=plain"])
    if args.action == "versions":
        return action_versions(args)
    if args.action == "release":
        return action_release(args)
    return EXIT_USAGE


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
