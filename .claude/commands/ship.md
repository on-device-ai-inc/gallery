Commit, push, and create PR.

## Pre-flight Checks

```bash
# Verify first!
./gradlew ktlintCheck
./gradlew test
git status
git diff
```

## Ship It

```bash
# Stage all changes
git add -A

# Commit with message
git commit -m "feat: your description"

# Push
git push -u origin HEAD

# Create PR (requires gh CLI)
gh pr create --fill
```

## Commit Message Format

```
type: short description

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- style: Formatting
- refactor: Code restructuring
- test: Adding tests
- chore: Maintenance
```
