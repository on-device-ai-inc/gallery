# Deploy

**TL;DR: Deploy runs on the DGX Spark, not in CI. Manual.**

## How the live site actually runs

- Host: DGX Spark (this machine)
- Process: host `node .next/standalone/server.js` on `PORT=3002`
- Reverse proxy: Caddy → `localhost:3002` (see `Caddyfile`)
- Database: `postgres:16-alpine` docker container (`gora-ai-web-db-1`), persisted volume `pgdata`, user `gora` (NOT `ondevice` — see caveat below)

## Deploy after pushing to main

```bash
cd /home/nashie/Development/gora-ai-web
git pull origin main
./scripts/restart.sh --build
```

That's it. `restart.sh --build` runs `pnpm build`, kills the old process on :3002, starts the new one, tails logs to `/tmp/gora-web-3002.log`.

## Verify after deploy

```bash
curl -sL https://on-device.org | grep -oE "waitlist|Buy Now" | sort -u
# Expect: "Buy Now" only, no "waitlist"
```

Also hit `/install`, `/buy`, `/download?token=...` to smoke-check.

## CI deploy is disabled

`.github/workflows/deploy.yml.disabled` — the GHA-based deploy never worked. Two main issues:

1. ARM64 qemu builds in GHA take ~5.5 min per build, hit env-var-at-module-load traps with Stripe/Resend SDKs (now fixed in code via lazy init)
2. `appleboy/scp-action` runs in its own Docker container and can't see `/tmp` on the runner host — "tar: empty archive"

If you want to resurrect it, the fixes are:
- Change `outputs: type=docker,dest=/tmp/ondevice-web.tar` → `dest=./ondevice-web.tar` (workspace-relative)
- Change scp `source: /tmp/ondevice-web.tar` → `source: ondevice-web.tar`
- Confirm DGX Spark SSH is reachable from GitHub Actions IPs (residential connection, likely not)

Until then: `git pull && scripts/restart.sh --build` on the Spark.

## Known caveats

### Docker-compose database user mismatch

`docker-compose.yml` declares `POSTGRES_USER=ondevice` but the initialized volume has user `gora`. The database was first created when the env was `gora`, and Postgres init scripts only run on empty data dirs, so `POSTGRES_USER=ondevice` is a no-op.

`DATABASE_URL` in `.env.local` correctly uses `postgresql://gora:...`. Don't "fix" the docker-compose by recreating the volume — you'll lose all orders. Either:
- Leave as-is (current state, works)
- Change `POSTGRES_USER` in docker-compose.yml to `gora` to match reality (cosmetic fix, safe)
- Migrate to a real `ondevice` role via `CREATE ROLE ondevice ...` + `GRANT ...` (invasive, don't bother)

### Port 3000 is taken by something else

`deer-flow-frontend` container uses :3000. Our site is on :3002. Don't try to bind to :3000 or you'll collide.
