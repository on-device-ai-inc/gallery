Launch Android environment for testing.

**NOTE: DGX Spark is ARM-based. Uses Waydroid instead of Android Emulator.**

## Quick Start (Waydroid on DGX Spark)

```bash
# Step 1: Verify Weston is running
pgrep weston || weston &

# Step 2: Set WiFi property (one-time, needs sudo)
sudo waydroid prop set persist.waydroid.fake_wifi "wlan0"

# Step 3: Start Waydroid session with correct WAYLAND_DISPLAY
WAYLAND_DISPLAY=wayland-1 waydroid session start

# Step 4: Wait and verify it's running
sleep 5
waydroid status
# Should show: Session: RUNNING, Container: RUNNING, IP: 192.168.240.112

# Step 5: Show Waydroid UI
WAYLAND_DISPLAY=wayland-1 waydroid show-full-ui &

# Step 6: Connect ADB
sleep 3
adb connect 192.168.240.112:5555
# First time: click "Always allow" in Waydroid UI dialog

# Step 7: Verify ADB connection
adb devices
# Should show: 192.168.240.112:5555	device
```

## Install and Launch App

```bash
# Install APK
adb install -r "/path/to/app-debug.apk"

# Launch app (package name is ai.ondevice.app)
adb shell am start -n ai.ondevice.app/.MainActivity
```

## Restart Waydroid (if issues)

```bash
# Stop everything
waydroid session stop
sudo waydroid container stop

# Start fresh
sudo waydroid container start
pgrep weston || weston &
sleep 2
WAYLAND_DISPLAY=wayland-1 waydroid session start
sleep 5
WAYLAND_DISPLAY=wayland-1 waydroid show-full-ui &
sleep 3
adb connect 192.168.240.112:5555
```

## Key Points

- **WAYLAND_DISPLAY=wayland-1** - Weston uses wayland-1, not wayland-0
- **WiFi property** must be set before starting session
- **Accept ADB dialog** in Waydroid UI on first connect
- **Package name**: `ai.ondevice.app`
- **Waydroid IP**: 192.168.240.112
