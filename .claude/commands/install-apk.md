Install APK to connected device.

```bash
# Check device connected
adb devices

# Install APK (replace existing)
adb install -r ./app-debug/app-debug.apk

# Or specify path
adb install -r /path/to/app.apk

# Launch app
adb shell am start -n ai.ondevice.app/.MainActivity
```

## Troubleshooting

**Device not found:**
```bash
adb kill-server && adb start-server
adb devices
```

**Unauthorized:**
- Check phone for USB debugging dialog
- Accept and check "Always allow"

**Install failed - already exists:**
```bash
adb uninstall ai.ondevice.app
adb install ./app-debug/app-debug.apk
```
