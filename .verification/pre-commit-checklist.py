#!/usr/bin/env python3
"""
PRE-COMMIT VERIFICATION SYSTEM
Comprehensive triple-check system to verify ALL changes before git commit.
Based on lessons learned from project issues.
"""

import os
import sys
import json
from pathlib import Path
from PIL import Image
import numpy as np
import subprocess

# ANSI colors
GREEN = '\033[92m'
RED = '\033[91m'
YELLOW = '\033[93m'
BLUE = '\033[94m'
RESET = '\033[0m'
BOLD = '\033[1m'

class VerificationReport:
    def __init__(self):
        self.checks = []
        self.warnings = []
        self.errors = []
        self.passed = 0
        self.failed = 0

    def add_check(self, name, status, details=""):
        """Add check result"""
        check = {
            "name": name,
            "status": status,  # "PASS", "FAIL", "WARN"
            "details": details
        }
        self.checks.append(check)

        if status == "PASS":
            self.passed += 1
        elif status == "FAIL":
            self.failed += 1
            self.errors.append(f"{name}: {details}")
        elif status == "WARN":
            self.warnings.append(f"{name}: {details}")

    def print_report(self):
        """Print formatted verification report"""
        print(f"\n{BOLD}{'='*80}{RESET}")
        print(f"{BOLD}{BLUE}PRE-COMMIT VERIFICATION REPORT{RESET}")
        print(f"{BOLD}{'='*80}{RESET}\n")

        # Summary
        total = len(self.checks)
        print(f"{BOLD}SUMMARY:{RESET}")
        print(f"  Total Checks: {total}")
        print(f"  {GREEN}✓ Passed: {self.passed}{RESET}")
        print(f"  {RED}✗ Failed: {self.failed}{RESET}")
        print(f"  {YELLOW}⚠ Warnings: {len(self.warnings)}{RESET}\n")

        # Detailed results
        print(f"{BOLD}DETAILED RESULTS:{RESET}\n")
        for check in self.checks:
            status_symbol = {
                "PASS": f"{GREEN}✓{RESET}",
                "FAIL": f"{RED}✗{RESET}",
                "WARN": f"{YELLOW}⚠{RESET}"
            }[check["status"]]

            print(f"{status_symbol} {check['name']}")
            if check["details"]:
                print(f"    {check['details']}")

        # Warnings
        if self.warnings:
            print(f"\n{BOLD}{YELLOW}WARNINGS:{RESET}")
            for warn in self.warnings:
                print(f"  ⚠ {warn}")

        # Errors
        if self.errors:
            print(f"\n{BOLD}{RED}ERRORS:{RESET}")
            for error in self.errors:
                print(f"  ✗ {error}")

        # Final verdict
        print(f"\n{BOLD}{'='*80}{RESET}")
        if self.failed == 0:
            print(f"{BOLD}{GREEN}✓ ALL CHECKS PASSED - SAFE TO COMMIT{RESET}")
        else:
            print(f"{BOLD}{RED}✗ {self.failed} CHECK(S) FAILED - DO NOT COMMIT{RESET}")
        print(f"{BOLD}{'='*80}{RESET}\n")

        return self.failed == 0


def verify_file_exists(report, file_path, description):
    """Check 1: File existence"""
    exists = os.path.exists(file_path)
    if exists:
        size = os.path.getsize(file_path)
        report.add_check(
            f"File exists: {description}",
            "PASS",
            f"Path: {file_path}, Size: {size} bytes"
        )
    else:
        report.add_check(
            f"File exists: {description}",
            "FAIL",
            f"Missing: {file_path}"
        )
    return exists


def verify_image_properties(report, image_path, expected_props):
    """Check 2: Image properties validation"""
    try:
        img = Image.open(image_path)
        arr = np.array(img)

        checks = []

        # Size check
        if "size" in expected_props:
            expected_size = expected_props["size"]
            if img.size == expected_size:
                checks.append(f"Size: {img.size} ✓")
            else:
                report.add_check(
                    f"Image size: {os.path.basename(image_path)}",
                    "FAIL",
                    f"Expected {expected_size}, got {img.size}"
                )
                return

        # Mode check
        if "mode" in expected_props:
            expected_mode = expected_props["mode"]
            if img.mode == expected_mode:
                checks.append(f"Mode: {img.mode} ✓")
            else:
                report.add_check(
                    f"Image mode: {os.path.basename(image_path)}",
                    "FAIL",
                    f"Expected {expected_mode}, got {img.mode}"
                )
                return

        # Transparency check
        if "has_transparency" in expected_props:
            if img.mode == "RGBA":
                alpha = arr[:, :, 3]
                has_transparency = np.any(alpha < 255)
                expected = expected_props["has_transparency"]

                if has_transparency == expected:
                    transparent_pct = (np.sum(alpha == 0) / (img.size[0] * img.size[1])) * 100
                    checks.append(f"Transparency: {transparent_pct:.1f}% ✓")
                else:
                    report.add_check(
                        f"Image transparency: {os.path.basename(image_path)}",
                        "FAIL",
                        f"Expected transparency={expected}, found={has_transparency}"
                    )
                    return

        # Monochrome check
        if "is_monochrome" in expected_props:
            rgb = arr[:, :, :3]
            is_mono = (rgb[:,:,0] == rgb[:,:,1]).all() and (rgb[:,:,1] == rgb[:,:,2]).all()
            expected = expected_props["is_monochrome"]

            if is_mono == expected:
                if is_mono:
                    checks.append(f"Monochrome: Yes ✓")
                else:
                    checks.append(f"Color: Yes ✓")
            else:
                report.add_check(
                    f"Image color mode: {os.path.basename(image_path)}",
                    "FAIL",
                    f"Expected monochrome={expected}, is monochrome={is_mono}"
                )
                return

        # Pure black check (for monochrome logos)
        if "is_pure_black" in expected_props and expected_props["is_pure_black"]:
            rgb = arr[:, :, :3]
            max_rgb = rgb.max()

            if max_rgb == 0:
                checks.append(f"Pure black: RGB max=0 ✓")
            else:
                report.add_check(
                    f"Image pure black: {os.path.basename(image_path)}",
                    "FAIL",
                    f"Expected pure black (RGB=0), found RGB max={max_rgb}"
                )
                return

        report.add_check(
            f"Image properties: {os.path.basename(image_path)}",
            "PASS",
            ", ".join(checks)
        )

    except Exception as e:
        report.add_check(
            f"Image validation: {os.path.basename(image_path)}",
            "FAIL",
            f"Error: {str(e)}"
        )


def verify_code_references(report, file_path, expected_references):
    """Check 3: Code contains expected references"""
    try:
        with open(file_path, 'r') as f:
            content = f.read()

        for ref_name, ref_pattern in expected_references.items():
            if ref_pattern in content:
                report.add_check(
                    f"Code reference: {ref_name}",
                    "PASS",
                    f"Found in {os.path.basename(file_path)}"
                )
            else:
                report.add_check(
                    f"Code reference: {ref_name}",
                    "FAIL",
                    f"Missing '{ref_pattern}' in {file_path}"
                )
    except Exception as e:
        report.add_check(
            f"Code validation: {os.path.basename(file_path)}",
            "FAIL",
            f"Error: {str(e)}"
        )


def verify_git_status(report):
    """Check 4: Git status validation"""
    try:
        # Check we're on correct branch
        branch = subprocess.check_output(['git', 'branch', '--show-current'], text=True).strip()

        if branch == "test/revert-to-10f8071":
            report.add_check(
                "Git branch",
                "PASS",
                f"On correct branch: {branch}"
            )
        else:
            report.add_check(
                "Git branch",
                "WARN",
                f"On branch: {branch} (expected: test/revert-to-10f8071)"
            )

        # Check for uncommitted changes
        status = subprocess.check_output(['git', 'status', '--porcelain'], text=True)

        if status.strip():
            modified_files = [line.strip() for line in status.strip().split('\n')]
            report.add_check(
                "Git status",
                "PASS",
                f"{len(modified_files)} file(s) staged/modified"
            )
        else:
            report.add_check(
                "Git status",
                "WARN",
                "No changes detected (working tree clean)"
            )

    except Exception as e:
        report.add_check(
            "Git validation",
            "FAIL",
            f"Error: {str(e)}"
        )


def verify_consistency(report, base_path):
    """Check 5: Cross-file consistency"""

    # Check all launcher icons exist in all densities
    densities = ['mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi']
    all_present = True

    for density in densities:
        path = f"{base_path}/app/src/main/res/mipmap-{density}/ic_launcher_foreground.png"
        if not os.path.exists(path):
            all_present = False
            report.add_check(
                f"Launcher icon consistency",
                "FAIL",
                f"Missing {density} density"
            )
            break

    if all_present:
        report.add_check(
            "Launcher icon consistency",
            "PASS",
            f"All {len(densities)} densities present"
        )

    # Verify main logo matches launcher icons (same source)
    try:
        main_logo = Image.open(f"{base_path}/app/src/main/res/drawable/neural_circuit_logo.png")
        main_arr = np.array(main_logo)

        # Check if launcher icon is derived from main logo
        sample_launcher = Image.open(f"{base_path}/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png")
        launcher_arr = np.array(sample_launcher)

        # Compare properties (should match if same source)
        main_is_mono = (main_arr[:,:,0] == main_arr[:,:,1]).all()
        launcher_is_mono = (launcher_arr[:,:,0] == launcher_arr[:,:,1]).all()

        if main_is_mono == launcher_is_mono:
            report.add_check(
                "Logo-Launcher consistency",
                "PASS",
                f"Color scheme matches (monochrome={main_is_mono})"
            )
        else:
            report.add_check(
                "Logo-Launcher consistency",
                "FAIL",
                f"Main logo monochrome={main_is_mono}, launcher={launcher_is_mono}"
            )
    except Exception as e:
        report.add_check(
            "Logo-Launcher consistency",
            "WARN",
            f"Could not verify: {str(e)}"
        )


def verify_edge_cases(report, base_path):
    """Check 6: Edge cases and common mistakes"""

    # Edge case 1: Check for old/backup files that might cause confusion
    old_files = [
        "neural_circuit_arms.png",
        "neural_circuit_center.png",
        "neural_circuit_logo_old.png",
        "neural_circuit_logo_backup.png"
    ]

    found_old = []
    for old_file in old_files:
        path = f"{base_path}/app/src/main/res/drawable/{old_file}"
        if os.path.exists(path):
            found_old.append(old_file)

    if found_old:
        report.add_check(
            "Old files cleanup",
            "WARN",
            f"Found old files: {', '.join(found_old)} (should be deleted?)"
        )
    else:
        report.add_check(
            "Old files cleanup",
            "PASS",
            "No old/backup files found"
        )

    # Edge case 2: Verify no duplicate resource names
    drawable_dir = f"{base_path}/app/src/main/res/drawable"
    if os.path.exists(drawable_dir):
        drawables = [f for f in os.listdir(drawable_dir) if f.endswith('.png')]
        neural_files = [f for f in drawables if 'neural' in f.lower()]

        if len(neural_files) == 1:  # Only neural_circuit_logo.png should exist
            report.add_check(
                "No duplicate resources",
                "PASS",
                f"Only {neural_files[0]} found"
            )
        else:
            report.add_check(
                "No duplicate resources",
                "WARN",
                f"Found {len(neural_files)} neural files: {neural_files}"
            )

    # Edge case 3: File permissions (should be readable)
    main_logo = f"{base_path}/app/src/main/res/drawable/neural_circuit_logo.png"
    if os.path.exists(main_logo):
        if os.access(main_logo, os.R_OK):
            report.add_check(
                "File permissions",
                "PASS",
                "Main logo is readable"
            )
        else:
            report.add_check(
                "File permissions",
                "FAIL",
                f"Cannot read {main_logo}"
            )


def run_verification(base_path="/home/nashie/Downloads/gallery-1.0.7/Android/src"):
    """Run complete verification suite"""

    os.chdir(base_path)
    report = VerificationReport()

    print(f"{BOLD}{BLUE}Running Pre-Commit Verification...{RESET}\n")

    # === SECTION 1: FILE EXISTENCE ===
    print(f"{BOLD}[1/6] File Existence Checks{RESET}")

    verify_file_exists(
        report,
        "app/src/main/res/drawable/neural_circuit_logo.png",
        "Main logo"
    )

    for density in ['mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi']:
        verify_file_exists(
            report,
            f"app/src/main/res/mipmap-{density}/ic_launcher_foreground.png",
            f"Launcher icon ({density})"
        )

    verify_file_exists(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/RotatingLogoIcon.kt",
        "RotatingLogoIcon.kt"
    )

    verify_file_exists(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/RotationalLoader.kt",
        "RotationalLoader.kt"
    )

    verify_file_exists(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/chat/ModelInitializationStatus.kt",
        "ModelInitializationStatus.kt"
    )

    # === SECTION 2: IMAGE PROPERTIES ===
    print(f"\n{BOLD}[2/6] Image Property Validation{RESET}")

    verify_image_properties(
        report,
        "app/src/main/res/drawable/neural_circuit_logo.png",
        {
            "size": (512, 512),
            "mode": "RGBA",
            "has_transparency": True,
            "is_monochrome": True,
            "is_pure_black": True
        }
    )

    # Sample launcher icon check
    verify_image_properties(
        report,
        "app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png",
        {
            "size": (144, 144),
            "mode": "RGBA",
            "has_transparency": True
        }
    )

    # === SECTION 3: CODE REFERENCES ===
    print(f"\n{BOLD}[3/6] Code Reference Validation{RESET}")

    verify_code_references(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/RotatingLogoIcon.kt",
        {
            "neural_circuit_logo reference": "R.drawable.neural_circuit_logo"
        }
    )

    verify_code_references(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/RotationalLoader.kt",
        {
            "neural_circuit_logo reference": "R.drawable.neural_circuit_logo"
        }
    )

    verify_code_references(
        report,
        "app/src/main/java/ai/ondevice/app/ui/common/chat/ModelInitializationStatus.kt",
        {
            "RotationalLoader import": "import ai.ondevice.app.ui.common.RotationalLoader",
            "RotationalLoader usage": "RotationalLoader(size = 14.dp)"
        }
    )

    # === SECTION 4: GIT STATUS ===
    print(f"\n{BOLD}[4/6] Git Status Validation{RESET}")
    verify_git_status(report)

    # === SECTION 5: CONSISTENCY CHECKS ===
    print(f"\n{BOLD}[5/6] Cross-File Consistency{RESET}")
    verify_consistency(report, base_path)

    # === SECTION 6: EDGE CASES ===
    print(f"\n{BOLD}[6/6] Edge Case Detection{RESET}")
    verify_edge_cases(report, base_path)

    # Print final report
    passed = report.print_report()

    return 0 if passed else 1


if __name__ == "__main__":
    sys.exit(run_verification())
