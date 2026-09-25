#!/usr/bin/env python3
"""Verify that every test failed (skeleton / unimplemented state on main)."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def expected_module_names(repo_root: Path) -> set[str]:
    pom = repo_root / "pom.xml"
    return set(re.findall(r"<module>[^/]+/([^<]+)</module>", pom.read_text()))


def main() -> int:
    repo_root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(".")
    report_files = sorted(repo_root.rglob("target/surefire-reports/TEST-*.xml"))

    if not report_files:
        print("ERROR: No Surefire test reports found. Did tests run?")
        return 1

    total = failed = errors = skipped = 0
    passed_tests: list[str] = []
    modules_with_reports: set[str] = set()

    for report in report_files:
        # .../<module>/target/surefire-reports/TEST-*.xml
        modules_with_reports.add(report.parent.parent.parent.name)
        suite = ET.parse(report).getroot()
        t = int(suite.get("tests", 0))
        f = int(suite.get("failures", 0))
        e = int(suite.get("errors", 0))
        s = int(suite.get("skipped", 0))
        total += t
        failed += f
        errors += e
        skipped += s

        for testcase in suite.findall("testcase"):
            if (
                testcase.findall("failure")
                or testcase.findall("error")
                or testcase.findall("skipped")
            ):
                continue
            classname = testcase.get("classname", "?")
            name = testcase.get("name", "?")
            passed_tests.append(f"{classname}.{name}")

    passed = len(passed_tests)
    expected = expected_module_names(repo_root)
    missing_modules = sorted(expected - modules_with_reports)

    print("Test summary")
    print(f"  Modules with reports : {len(modules_with_reports)} / {len(expected)}")
    print(f"  Total tests          : {total}")
    print(f"  Passed               : {passed}")
    print(f"  Failed               : {failed}")
    print(f"  Errors               : {errors}")
    print(f"  Skipped              : {skipped}")

    if missing_modules:
        print(f"\nERROR: {len(missing_modules)} module(s) did not produce test reports:")
        for module in missing_modules:
            print(f"  - {module}")
        return 1

    if total == 0:
        print("\nERROR: No tests were executed.")
        return 1

    if passed > 0:
        print(
            f"\nERROR: {passed} test(s) passed. "
            "Main must stay unimplemented — implementations must not be merged."
        )
        print("\nPassing tests:")
        for test in passed_tests:
            print(f"  - {test}")
        return 1

    print("\nOK: All tests failed as expected (skeleton state preserved).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
