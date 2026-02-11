Check GitHub Actions CI status.

```bash
# Check workflow runs
gh run list --limit 5

# Watch current run
gh run watch

# View specific run details
gh run view <run-id>

# View failed job logs
gh run view <run-id> --log-failed
```

## CI Jobs

| Job | Purpose |
|-----|---------|
| lint | ktlintCheck, Android lint |
| test | Unit tests |
| build-debug | assembleDebug → APK artifact |
| build-release | assembleRelease (main branch only) |
