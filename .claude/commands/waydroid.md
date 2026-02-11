Start Waydroid Android container for testing on DGX Spark.

**Same as /emulator - automatically starts everything.**

```bash
# Start Weston if not running
pgrep weston || weston &
sleep 2

# Start Waydroid session
WAYLAND_DISPLAY=wayland-1 waydroid session start &
sleep 5

# Show UI
WAYLAND_DISPLAY=wayland-1 waydroid show-full-ui &
sleep 3

# Connect ADB
adb connect 192.168.240.112:5555
sleep 2

# Show status
echo "=== Waydroid Status ==="
waydroid status
echo ""
echo "=== ADB Devices ==="
adb devices
echo ""

# Try to launch app if installed
if adb shell pm list packages 2>/dev/null | grep -q ai.ondevice.app; then
  echo "Launching OnDevice AI app..."
  adb shell am start -n ai.ondevice.app/.MainActivity
else
  echo "OnDevice AI app not installed. Use /install-apk to install."
fi
```

**First time:** Accept "Allow USB debugging?" dialog in Waydroid UI.

## Quick Commands

**Start Waydroid:**
```bash
WAYLAND_DISPLAY=wayland-1 waydroid session start
WAYLAND_DISPLAY=wayland-1 waydroid show-full-ui &
```

**Connect ADB:**
```bash
adb connect 192.168.240.112:5555
adb devices
```

**Stop Waydroid:**
```bash
waydroid session stop
```

**Check Status:**
```bash
waydroid status
```

**Install APK:**
```bash
adb install -r "/home/nashie/Downloads/app-debug(209)/app-debug.apk"
```

**Launch App:**
```bash
adb shell am start -n ai.ondevice.app/.MainActivity
```

## First Time Setup

**Enable WiFi (one-time, requires sudo):**
```bash
sudo waydroid prop set persist.waydroid.fake_wifi "wlan0"
```

**Accept ADB Authorization:**
- When you first connect ADB, a dialog appears in Waydroid UI
- Click "Always allow from this computer"

## Troubleshooting

**Session won't start:**
```bash
# Check Waydroid logs
journalctl -u waydroid-container -f

# Or restart everything
waydroid session stop
WAYLAND_DISPLAY=wayland-1 waydroid session start
```

**ADB unauthorized:**
- Check Waydroid UI for authorization dialog
- Click "Always allow"

**Can't find WAYLAND_DISPLAY:**
```bash
# Check what Weston is using
ls -la /run/user/1000/ | grep wayland
# Should show wayland-1
```

## Key Info
- **Waydroid IP:** 192.168.240.112:5555
- **Package name:** ai.ondevice.app
- **WAYLAND_DISPLAY:** wayland-1 (not wayland-0!)
- **WiFi:** Must set fake_wifi property for model downloads
