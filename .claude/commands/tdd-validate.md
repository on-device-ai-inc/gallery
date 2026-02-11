Full TDD validation (lint + all tests).

## Process

```bash
# 1. Lint check
./gradlew ktlintCheck

# 2. All unit tests
./gradlew test

# 3. Check coverage (optional)
./gradlew jacocoTestReport
```

## All Must Pass

- ✅ Lint clean
- ✅ All tests pass
- ✅ No regressions

## If Issues

**Lint fails:**
```bash
./gradlew ktlintFormat
```

**Test fails:**
- Check if new code broke existing tests
- Fix without breaking new functionality
- Re-run validation

## Next Step

Once validated:
```
/verify
/ship "feat: description"
```
