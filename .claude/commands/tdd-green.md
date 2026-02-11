Make the test pass (TDD Green phase).

## Process

1. **Write minimum code** to make the test pass
2. **Don't over-engineer** - just enough to pass
3. **Run the test** - it MUST pass

```bash
# Run specific test
./gradlew test --tests "*TestName*"

# Expected: PASSED
```

## Rules

- Write the **simplest** code that passes
- Don't add extra features
- Don't refactor yet
- Just make it green

## Next Step

Once test passes:
```
/tdd-validate
```

Then refactor if needed (keeping tests green).
