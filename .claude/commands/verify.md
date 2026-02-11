Run local verification (lint + unit tests).

```bash
# Lint check
./gradlew ktlintCheck

# Unit tests
./gradlew test

# Or run the verify script
./scripts/verify-local.sh
```

**Must pass before shipping!**

If lint fails, auto-fix with:
```bash
./gradlew ktlintFormat
```
