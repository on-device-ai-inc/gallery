Check OpenSpec active changes status.

```bash
# List active changes
ls -la openspec/changes/

# Check specific change status
cat openspec/changes/<change-name>/status.md

# View tasks
cat openspec/changes/<change-name>/tasks.md
```

## Current Active Changes

| Change | Description | Status |
|--------|-------------|--------|
| `brave-search-integration` | Brave Search API | 0/85 tasks |
| `privacy-lock-web-search` | Privacy lock UI | 0/72 tasks |

## OpenSpec Workflow

```
/openspec:proposal <name>   # Create new change
/openspec:apply <name>      # Mark task complete
/openspec:archive <name>    # Archive completed change
```
