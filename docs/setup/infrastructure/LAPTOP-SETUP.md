# Laptop Setup — OnDevice AI Inc.

**DGX Spark Tailscale IP:** `100.102.217.122`
**DGX Spark Hostname:** `ondeviceai-spark`

**Users available for RDP login:**

| Username | Purpose |
|----------|---------|
| nashie.c | Primary remote user |
| admin | Administrative remote user |

---

## Linux Laptop Setup

### Step 1: Update system and fix any broken packages

```bash
sudo apt update
sudo apt --fix-broken install -y
sudo apt upgrade -y
```

### Step 2: Install Tailscale (secure VPN to DGX Spark)

```bash
curl -fsSL https://tailscale.com/install.sh | sh
```

### Step 3: Authenticate Tailscale

```bash
sudo tailscale up
```

- A URL will appear in the terminal
- Open it in your browser
- **Sign in with the same account used on the DGX Spark** (this is critical — both devices must be on the same Tailscale network)

### Step 4: Verify connection to DGX Spark

```bash
ping -c 3 100.102.217.122
```

You should see replies. If not, check that Tailscale is running on both the laptop and the DGX Spark:

```bash
tailscale status
```

Look for `ondeviceai-spark` in the list.

### Step 5: Install RDP clients

```bash
sudo apt install -y remmina remmina-plugin-rdp freerdp2-x11
```

This installs two RDP clients:
- **Remmina** — GUI application with saved connections
- **FreeRDP** — command-line alternative

### Step 6: Connect via Remmina (GUI method)

1. Open **Remmina** from the applications menu
2. Click the **+** button to create a new connection
3. Fill in these fields:
   - **Name:** `DGX Spark`
   - **Group:** (leave blank)
   - **Protocol:** `RDP - Remote Desktop Protocol`
   - **Server:** `100.102.217.122`
   - **Username:** `nashie.c`
   - **Password:** (leave blank — it will prompt you)
   - **Resolution:** `Use client resolution`
   - **Color depth:** `True color (32 bpp)`
4. Click **Save and Connect**
5. Enter your password when prompted
6. Accept the certificate if asked

### Step 7: (Alternative) Connect via command line

```bash
xfreerdp /v:100.102.217.122 /u:nashie.c /dynamic-resolution /f /sound /clipboard +auto-reconnect /cert:ignore
```

- Enter your password when prompted
- **Ctrl+Alt+Enter** — toggle fullscreen on/off
- The connection will auto-reconnect if it drops

To connect as admin instead:

```bash
xfreerdp /v:100.102.217.122 /u:admin /dynamic-resolution /f /sound /clipboard +auto-reconnect /cert:ignore
```

---

## Windows Laptop Setup

### Step 1: Install Tailscale

1. Go to https://tailscale.com/download/windows
2. Download and run the installer
3. Tailscale will appear in the system tray (bottom-right of taskbar)
4. Click the Tailscale icon and select **Log in**
5. **Sign in with the same account used on the DGX Spark**

### Step 2: Verify connection

1. Open **Command Prompt** (press `Win+R`, type `cmd`, press Enter)
2. Run:

```
ping 100.102.217.122
```

You should see replies. If not:
- Check the Tailscale icon in the system tray — make sure it says "Connected"
- Make sure you signed in with the same account as the DGX Spark

### Step 3: Connect via Remote Desktop

1. Press **Win+R**, type `mstsc`, press Enter
2. In the **Computer** field, enter: `100.102.217.122`
3. Click **Show Options** to expand the window
4. In the **User name** field, enter: `nashie.c` (or `admin`)
5. Click **Connect**
6. Enter the password when prompted
7. Click **Yes** if asked about the certificate

### Step 4: Switch users

If you're logged in as one user and want to switch:
1. When the credentials prompt appears, click **More choices**
2. Click **Use a different account**
3. Enter the other username (`admin` or `nashie.c`) and its password

### Step 5: Save the connection for quick access

1. In the `mstsc` window, fill in Computer and User name
2. Click **Save As** and save to the Desktop
3. Next time, just double-click the `.rdp` file on your Desktop

---

## What You Get When Connected

Both users get the same enterprise desktop with:

- **GNOME desktop** — identical to sitting at the DGX Spark
- **Desktop shortcuts:**
  - AI Chat (Open WebUI) — http://localhost:8080
  - JupyterLab — http://localhost:8888
  - ComfyUI (Image Generation) — http://localhost:8188
  - GPU Monitor
- **Terminal** with:
  - Claude Code (`claude` command)
  - Node.js + npm
  - Python 3 + venv at `/opt/venv`
  - CUDA toolkit
  - Ollama CLI
  - Docker with GPU support
- **Home folders:** Desktop, Documents, Downloads, Projects

---

## Troubleshooting

### "Remote computer not available" (Windows)

- Tailscale not connected. Check the system tray icon.
- Run `ping 100.102.217.122` — if it fails, re-authenticate: open Tailscale and click Log in.

### "Connection refused" or "Unable to connect" (Linux)

- Check Tailscale: `tailscale status`
- Check xrdp is running on DGX: `ssh nashie@100.102.217.122` then `sudo systemctl status xrdp`
- Restart xrdp on DGX: `sudo systemctl restart xrdp xrdp-sesman`

### Desktop flashes and disconnects

- SSH into the DGX and restart xrdp:

```bash
ssh nashie@100.102.217.122
sudo pkill -u nashie.c
sudo systemctl restart xrdp xrdp-sesman
```

Then reconnect.

### Black screen after login

- The GNOME session may have crashed. SSH into DGX and run:

```bash
ssh nashie@100.102.217.122
sudo pkill -u nashie.c
rm -rf /home/nashie.c/.cache/sessions
sudo systemctl restart xrdp xrdp-sesman
```

### Apps show X icons on desktop

- Right-click each shortcut → Properties → Permissions → check "Allow executing file as program"
- Or SSH into DGX and run:

```bash
sudo chmod +x /home/nashie.c/Desktop/*.desktop
sudo -u nashie.c gio set /home/nashie.c/Desktop/ai-chat.desktop metadata::trusted true
sudo -u nashie.c gio set /home/nashie.c/Desktop/jupyterlab.desktop metadata::trusted true
sudo -u nashie.c gio set /home/nashie.c/Desktop/comfyui.desktop metadata::trusted true
sudo -u nashie.c gio set /home/nashie.c/Desktop/gpu-monitor.desktop metadata::trusted true
```

### Tailscale disconnects after reboot

```bash
# Linux
sudo tailscale up

# Windows — click Tailscale icon in system tray → Log in
```

### Can't type password (RDP login prompt unresponsive)

- Close the connection completely and reconnect
- If using Remmina, try the command-line method instead:

```bash
xfreerdp /v:100.102.217.122 /u:nashie.c /dynamic-resolution /f /cert:ignore
```

---

## Quick Reference

| Action | Linux Command | Windows |
|--------|---------------|---------|
| Connect as nashie.c | `xfreerdp /v:100.102.217.122 /u:nashie.c /f /cert:ignore` | `mstsc` → 100.102.217.122 |
| Connect as admin | `xfreerdp /v:100.102.217.122 /u:admin /f /cert:ignore` | `mstsc` → 100.102.217.122 |
| Toggle fullscreen | **Ctrl+Alt+Enter** | **Ctrl+Alt+Break** |
| Check Tailscale | `tailscale status` | Tailscale system tray icon |
| SSH to DGX | `ssh nashie.c@100.102.217.122` | Use PuTTY or Windows Terminal |
| Restart xrdp (on DGX) | `sudo systemctl restart xrdp xrdp-sesman` | — |
