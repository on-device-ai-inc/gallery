Full project status check.

## Quick Status

```bash
# Git status
git status
git log --oneline -5

# Current branch
git branch --show-current

# Device status
adb devices

# Check CI
gh run list --limit 3
```

## Project Health

```bash
# Lint status
./gradlew ktlintCheck

# Test status
./gradlew test

# Build status (if local build works)
./gradlew assembleDebug
```

## OpenSpec Status

```bash
ls -la openspec/changes/
```

## Files to Check

- `LESSONS_LEARNED.md` - Recent learnings
- `CODE_INDEX.md` - Capability index
- `.github/workflows/` - CI status
