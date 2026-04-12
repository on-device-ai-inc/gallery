#!/usr/bin/env bash
# Restart the OnDevice AI web server on port 3002
# Usage: ./scripts/restart.sh [--build]
set -e

cd "$(dirname "$0")/.."

if [[ "$1" == "--build" ]]; then
  echo "Building..."
  pnpm build   # also copies static assets (see package.json)
fi

# Load env vars from .env.local
if [[ -f .env.local ]]; then
  set -a
  source .env.local
  set +a
else
  echo "Warning: .env.local not found — server may start with missing env vars"
fi

# Kill existing process on port 3002
fuser -k 3002/tcp 2>/dev/null || true
# Wait until port is free
for i in {1..10}; do
  sleep 1
  fuser 3002/tcp > /dev/null 2>&1 || break
done

# Start
echo "Starting on port 3002..."
PORT=3002 nohup node .next/standalone/server.js >> /tmp/gora-web-3002.log 2>&1 &
echo "PID: $!"

sleep 4
if fuser 3002/tcp > /dev/null 2>&1; then
  echo "Done — server is up on :3002"
else
  echo "ERROR — server did not start. Check /tmp/gora-web-3002.log"
  exit 1
fi
