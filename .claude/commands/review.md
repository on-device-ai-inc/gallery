Security and quality review checklist.

## Security Check

- [ ] No hardcoded secrets/API keys
- [ ] No sensitive data in logs
- [ ] Input validation present
- [ ] No SQL injection vectors
- [ ] Permissions are minimal

## Quality Check

- [ ] Code follows project patterns
- [ ] No duplicate functionality (check CODE_INDEX.md)
- [ ] Error handling present
- [ ] Edge cases considered
- [ ] Tests cover critical paths

## Android Specific

- [ ] No memory leaks (lifecycle aware)
- [ ] Background work uses WorkManager
- [ ] UI updates on main thread
- [ ] Coroutines properly scoped

## Commands

```bash
# Check for secrets
grep -r "api_key\|secret\|password" --include="*.kt" .

# Check lint
./gradlew lint

# Run tests
./gradlew test
```
