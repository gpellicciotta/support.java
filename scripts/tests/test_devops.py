# Copyright (c) Giovanni Pellicciotta
"""Tests of scripts/devops.py; run with `python -m unittest discover -s scripts/tests` after the bootstrap."""

from __future__ import annotations

import contextlib
import importlib.util
import io
import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from support_python.devops import DevopsCli, GradleProject

REPO_ROOT = Path(__file__).resolve().parents[2]
DEVOPS_SCRIPT = REPO_ROOT / "scripts" / "devops.py"


def load_devops():
    spec = importlib.util.spec_from_file_location("devops_script", DEVOPS_SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def write_versions(root: Path, gradle_version: str, changelog_version: str) -> None:
    (root / "gradle.properties").write_text(f"version={gradle_version}\n", encoding="utf-8")
    (root / "CHANGELOG.md").write_text(f"# Changelog\n\n## v{changelog_version}\n- Entry.\n", encoding="utf-8")


def recording_project(test: unittest.TestCase, failing: str = ""):
    """Return the recorded commands and a devops project whose runner records commands instead of running them."""
    calls: list[list[str]] = []

    def runner(argv, cwd, logger):
        calls.append(list(argv))
        return 1 if failing and Path(argv[0]).stem == failing else 0

    tmp = tempfile.TemporaryDirectory()
    test.addCleanup(tmp.cleanup)
    root = Path(tmp.name)
    (root / ("gradlew.bat" if sys.platform == "win32" else "gradlew")).write_text("", encoding="utf-8")
    project = load_devops().build_cli(root).project
    project.runner = runner
    return calls, project


def command_names(argv: list[str]) -> list[str]:
    """Return argv with its executable reduced to the bare command name."""
    return [Path(argv[0]).stem, *argv[1:]]


class DevopsScriptTest(unittest.TestCase):
    def setUp(self) -> None:
        """Run every test as on a developer machine; CI sets `CI`, which adds `--offline` to lint-markdown."""
        patcher = mock.patch.dict(os.environ)
        patcher.start()
        self.addCleanup(patcher.stop)
        os.environ.pop("CI", None)

    def test_builds_devops_cli_over_gradle_project_at_repo_root(self) -> None:
        cli = load_devops().build_cli()
        self.assertIsInstance(cli, DevopsCli)
        self.assertIsInstance(cli.project, GradleProject)
        self.assertEqual(REPO_ROOT, cli.project.root)

    def test_release_delegates_to_do_full_release_with_confirmation_property(self) -> None:
        project = load_devops().build_cli().project
        self.assertEqual("doFullRelease", project.release_task)
        self.assertEqual(["-PreleaseConfirmed=yes"], project.release_args)

    def test_lint_runs_lint_markdown_before_gradle_lint_tasks(self) -> None:
        calls, project = recording_project(self)
        self.assertTrue(project.lint())
        self.assertEqual(["lint-markdown", "check"], command_names(calls[0]))
        self.assertEqual(["spotlessCheck", "checkstyleMain", "checkstyleTest"], calls[1][-3:])

    def test_check_runs_lint_markdown_before_gradle_build(self) -> None:
        calls, project = recording_project(self)
        self.assertTrue(project.check())
        self.assertEqual(["lint-markdown", "check"], command_names(calls[0]))
        self.assertEqual(["build"], calls[1][-1:])

    def test_lint_markdown_skips_external_link_checks_only_in_ci(self) -> None:
        for ci, expected in (("true", ["lint-markdown", "check", "--offline"]), ("", ["lint-markdown", "check"])):
            with self.subTest(ci=ci), mock.patch.dict(os.environ, {"CI": ci}):
                calls, project = recording_project(self)
                self.assertTrue(project.lint_markdown())
                self.assertEqual(expected, command_names(calls[0]))

    def test_check_skips_gradle_build_when_lint_markdown_fails(self) -> None:
        calls, project = recording_project(self, failing="lint-markdown")
        self.assertFalse(project.check())
        self.assertEqual([["lint-markdown", "check"]], [command_names(call) for call in calls])

    def test_lint_markdown_config_only_adds_changelog_to_default_excludes(self) -> None:
        config = json.loads((REPO_ROOT / ".lint-markdown.json").read_text(encoding="utf-8"))
        self.assertEqual(["CHANGELOG.md"], config["exclude"])

    def test_help_lists_standard_actions(self) -> None:
        result = subprocess.run(
            [sys.executable, str(DEVOPS_SCRIPT), "help"], capture_output=True, text=True, encoding="utf-8", check=False
        )
        self.assertEqual(0, result.returncode, result.stderr)
        for action in ("check", "build", "test", "clean", "versions", "release", "lint", "format"):
            self.assertRegex(result.stdout, rf"(?m)^\s+{action}\b")

    def test_versions_exits_zero_when_versions_agree(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_versions(root, "1.2.3-pre", "1.2.3-pre")
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(0, load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")]))

    def test_versions_exits_one_when_versions_drift(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_versions(root, "1.2.4-pre", "1.2.3-pre")
            with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(1, load_devops().build_cli(root).run(["versions", "--log-file", str(root / "devops.log")]))


if __name__ == "__main__":
    unittest.main()
