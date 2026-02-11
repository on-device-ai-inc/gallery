Mirror device screen with scrcpy.

```bash
# Basic mirror
scrcpy

# With specific device
scrcpy -s R3CT10HETMM

# Record while mirroring
scrcpy --record screen.mp4

# Lower resolution (faster)
scrcpy --max-size 1024

# Stay awake while connected
scrcpy --stay-awake
```

## Install scrcpy (if needed)

```bash
# Ubuntu/Debian
sudo apt install scrcpy

# Or via snap
sudo snap install scrcpy
```
