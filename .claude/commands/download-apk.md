Download APK artifact from GitHub Actions.

```bash
# List recent runs
gh run list --limit 5

# Download artifact from latest successful run
gh run download <run-id> -n app-debug

# Or download from latest
gh run download $(gh run list --status success --limit 1 --json databaseId -q '.[0].databaseId') -n app-debug
```

The APK will be in `./app-debug/app-debug.apk`

Then install with:
```
/install-apk
```
