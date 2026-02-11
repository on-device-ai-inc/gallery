# Pre-Commit Verification System

## Overview

This verification system provides comprehensive triple-check validation before committing changes to ensure all modifications are properly implemented and reflected in the codebase.

## Purpose

Based on lessons learned from the rotating logo implementation, this system prevents:
- Files not being updated properly
- Changes not taking effect
- Visual artifacts (like "two ugly lines")
- Incorrect image properties (transparency, size, mode)
- Missing code references
- Inconsistencies across multiple files
- Old/backup files causing confusion

## Usage

### Running the Verification

```bash
# From project root: /home/nashie/Downloads/gallery-1.0.7/Android/src
source /home/nashie/amapiano-research-pipeline/venv/bin/activate
python3 .verification/pre-commit-checklist.py
```

### Before Every Commit

**ALWAYS** run the verification script before committing changes:

```bash
# 1. Make your changes
# 2. Run verification
source /home/nashie/amapiano-research-pipeline/venv/bin/activate
python3 .verification/pre-commit-checklist.py

# 3. Only if ALL checks pass (exit code 0):
git add .
git commit -m "Your commit message"
```

### Exit Codes

- **0**: All checks passed - SAFE TO COMMIT
- **1**: One or more checks failed - DO NOT COMMIT

## Verification Sections

### 1. File Existence Checks (9 checks)
Verifies all required files exist:
- Main logo: `app/src/main/res/drawable/neural_circuit_logo.png`
- Launcher icons in all densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Kotlin source files:
  - `RotatingLogoIcon.kt`
  - `RotationalLoader.kt`
  - `ModelInitializationStatus.kt`

### 2. Image Property Validation (2 checks)
Validates PNG properties:
- **Main logo** (neural_circuit_logo.png):
  - Size: 512x512
  - Mode: RGBA
  - Has transparency: Yes (>70%)
  - Monochrome: Yes (pure black & white)
  - Pure black: RGB max = 0 (no gray pixels)

- **Launcher icon** (sample xxhdpi):
  - Size: 144x144
  - Mode: RGBA
  - Has transparency: Yes

### 3. Code Reference Validation (4 checks)
Ensures correct imports and usages:
- `RotatingLogoIcon.kt` references `R.drawable.neural_circuit_logo`
- `RotationalLoader.kt` references `R.drawable.neural_circuit_logo`
- `ModelInitializationStatus.kt` imports `RotationalLoader`
- `ModelInitializationStatus.kt` uses `RotationalLoader(size = 14.dp)`

### 4. Git Status Validation (2 checks)
Checks git state:
- On correct branch: `test/revert-to-10f8071`
- Detects uncommitted changes

### 5. Cross-File Consistency (2 checks)
Validates consistency across files:
- All launcher icon densities present
- Main logo and launcher icons have matching color scheme (monochrome)

### 6. Edge Case Detection (3 checks)
Prevents common mistakes:
- No old/backup files (neural_circuit_arms.png, neural_circuit_center.png, etc.)
- No duplicate resources (only one neural logo file)
- Correct file permissions (readable)

## Dependencies

Required Python packages:
- `numpy>=2.3.5` - Image array analysis
- `pillow>=12.0.0` - Image processing

These are installed in the virtual environment at `/home/nashie/amapiano-research-pipeline/venv`

## Customization

To add new checks, edit `.verification/pre-commit-checklist.py`:

```python
# Example: Add new file existence check
verify_file_exists(
    report,
    "path/to/new/file.kt",
    "Description of file"
)

# Example: Add new code reference check
verify_code_references(
    report,
    "path/to/file.kt",
    {
        "Check name": "expected string in file"
    }
)
```

## Report Format

The verification generates a detailed colored report showing:
- ✓ **Green** = Passed
- ✗ **Red** = Failed
- ⚠ **Yellow** = Warning

Example output:
```
PRE-COMMIT VERIFICATION REPORT
================================================================================

SUMMARY:
  Total Checks: 22
  ✓ Passed: 22
  ✗ Failed: 0
  ⚠ Warnings: 0

DETAILED RESULTS:

✓ File exists: Main logo
    Path: app/src/main/res/drawable/neural_circuit_logo.png, Size: 43669 bytes
✓ Image properties: neural_circuit_logo.png
    Size: (512, 512) ✓, Mode: RGBA ✓, Transparency: 72.7% ✓, Monochrome: Yes ✓

... (20 more checks)

================================================================================
✓ ALL CHECKS PASSED - SAFE TO COMMIT
================================================================================
```

## Why This Matters

From the rotating logo implementation, we learned that:

1. **Visual inspection isn't enough** - We need automated property validation
2. **Changes must be verified** - Files can fail to update properly
3. **Consistency matters** - Launcher icons must match main logo
4. **Edge cases happen** - Old files can cause confusion
5. **Trust but verify** - Always validate before committing

This system ensures these issues never happen again.

## Current Validation Results

**Latest Run (2025-12-16):**
- ✅ 22/22 checks passed
- Main logo: 512x512, RGBA, 72.7% transparent, pure black monochrome
- All 5 launcher icon densities present and consistent
- All code references correct
- Branch: test/revert-to-10f8071
- No old/duplicate files found

## Future Enhancements

Potential additions:
- Build validation (if local builds needed)
- Performance checks (file size limits)
- Screenshot comparison (visual regression testing)
- Animation property validation (rotation speeds)
- Code style checks (ktlint integration)
