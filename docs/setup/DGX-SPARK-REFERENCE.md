# DGX Spark Setup Reference

**Machine:** NVIDIA DGX Spark
**Chip:** GB10 Grace Blackwell Superchip
**Memory:** 120GB unified
**Storage:** 3.7TB SSD
**OS:** Ubuntu 24.04 (aarch64)
**CUDA:** 13.0
**Hostname:** ondeviceai-spark
**Tailscale IP:** 100.102.217.122

---

## Users

| User | Groups | Purpose |
|------|--------|---------|
| nashie | sudo, docker, ollama, users | Primary admin (local + SSH) |
| nashie.c | sudo, docker, ollama, users | Remote desktop user |
| admin | sudo, docker, ollama, users | Remote desktop user |

All remote users have:
- NVM + Node.js v24
- Claude Code (`claude` command)
- Python 3 + venv at `/opt/venv`
- CUDA toolkit in PATH
- Desktop shortcuts for all web services

---

## Services — Always Running

| Service | URL | Port | Manage |
|---------|-----|------|--------|
| Ollama API | http://localhost:11434 | 11434 | `sudo systemctl restart ollama` |
| Open WebUI | http://localhost:8080 | 8080 | `sudo docker restart open-webui` |
| JupyterLab | http://localhost:8888 | 8888 | `sudo systemctl restart jupyterlab` |
| ComfyUI | http://localhost:8188 | 8188 | `sudo systemctl restart comfyui` |
| xrdp (Remote Desktop) | — | 3389 | `sudo systemctl restart xrdp xrdp-sesman` |
| SSH | — | 22 | `sudo systemctl restart ssh` |
| Tailscale | — | — | `sudo tailscale up` |

## Services — Start Manually

| Service | Command | Port |
|---------|---------|------|
| vLLM (OpenAI API) | `sudo systemctl start vllm` | 8000 |
| NIM (Llama 3.1) | `sudo systemctl start nim-llama` | 8001 |
| RAPIDS Notebook | `docker run --gpus all --rm -it -p 8889:8888 nvcr.io/nvidia/rapidsai/notebooks:24.12-cuda12.5-py3.12` | 8889 |
| NeMo | `docker run --gpus all --rm -it nvcr.io/nvidia/nemo:24.07` | — |

> **Note:** vLLM and Ollama compete for GPU memory. Stop one before starting the other:
> `sudo systemctl stop ollama` before `sudo systemctl start vllm`

---

## Ollama Models Installed

| Model | Size | Command |
|-------|------|---------|
| llama3.1:8b | 4.7GB | `ollama run llama3.1:8b` |
| mistral:7b | 4.1GB | `ollama run mistral:7b` |
| codellama:13b | 7.4GB | `ollama run codellama:13b` |
| nomic-embed-text | 274MB | `ollama run nomic-embed-text` |

Add more: `ollama pull <model-name>`

---

## Remote Access

### Tailscale VPN

All devices must be on the same Tailscale account. The DGX Spark IP is `100.102.217.122`.

```bash
# Check status
tailscale status

# Re-authenticate if needed
sudo tailscale up
```

### xrdp Configuration

xrdp is configured to launch GNOME desktop sessions. Key files:

| File | Purpose |
|------|---------|
| `/etc/xrdp/xrdp.ini` | Main xrdp config. Xorg section must have `port=-1` |
| `/etc/xrdp/sesman.ini` | Session manager config |
| `/etc/xrdp/startwm.sh` | Session startup — launches `gnome-session` |

The startwm.sh content should be:

```bash
#!/bin/sh
if [ -r /etc/default/locale ]; then
    . /etc/default/locale
    export LANG LANGUAGE
fi
if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
    eval $(dbus-launch --sh-syntax)
    export DBUS_SESSION_BUS_ADDRESS
fi
export XDG_SESSION_TYPE=x11
export XDG_SESSION_DESKTOP=ubuntu
export XDG_CURRENT_DESKTOP=ubuntu:GNOME
export GNOME_SHELL_SESSION_MODE=ubuntu
export DESKTOP_SESSION=ubuntu
export LIBGL_ALWAYS_SOFTWARE=1
exec gnome-session --session=ubuntu
```

### Polkit rule for RDP users

File: `/etc/polkit-1/rules.d/02-allow-colord.rules` — prevents authentication popups during RDP sessions.

---

## Python Environment

All Python packages are in a virtual environment (Ubuntu 24.04 blocks system pip):

```bash
source /opt/venv/bin/activate
```

Installed: jupyterlab, numpy, pandas, matplotlib, scipy, scikit-learn, vllm, comfyui deps, nemo_toolkit

---

## NGC Container Registry

Logged in as `$oauthtoken`. API key stored encrypted at `~/.claude/ngc-key.gpg`.

```bash
# Re-login if needed
docker login nvcr.io -u '$oauthtoken' -p <NGC_API_KEY>
```

---

## Docker

Default runtime set to `nvidia` in `/etc/docker/daemon.json`.
All `docker run` commands automatically get GPU access.

---

## GPU Quick Reference

```bash
# Status
nvidia-smi

# Persistence mode (already enabled)
sudo nvidia-smi -pm 1

# Check what's using GPU
nvidia-smi --query-compute-apps=pid,name,used_memory --format=csv
```

---

## Firewall Ports Open

| Port | Service |
|------|---------|
| 22 | SSH |
| 3389 | xrdp (Remote Desktop) |
| 8000 | vLLM |
| 8001 | NIM |
| 8080 | Open WebUI |
| 8188 | ComfyUI |
| 8888 | JupyterLab |
| 8889 | RAPIDS |
| 11434 | Ollama |

---

## Adding a New User

To add a new employee/user with full access:

```bash
# Create user with groups
sudo useradd -m -s /bin/bash -G sudo,docker,ollama,users <username>
sudo passwd <username>

# Install NVM + Claude Code
sudo -u <username> bash -c 'curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash'
sudo -u <username> bash -c 'export NVM_DIR="$HOME/.nvm"; [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"; nvm install 24; npm install -g @anthropic-ai/claude-code'

# Copy environment profile
sudo cp /etc/skel/.profile_ondevice /home/<username>/
sudo cp /etc/skel/.xsessionrc /home/<username>/
sudo cp -r /etc/skel/Desktop /home/<username>/
sudo mkdir -p /home/<username>/Documents /home/<username>/Downloads /home/<username>/Projects
echo '[ -f ~/.profile_ondevice ] && source ~/.profile_ondevice' | sudo tee -a /home/<username>/.bashrc
sudo chown -R <username>:<username> /home/<username>
sudo chmod +x /home/<username>/Desktop/*.desktop
```

The user can then RDP in from any laptop with Tailscale installed.

---

## Troubleshooting

### xrdp — desktop flashes and disconnects

```bash
sudo pkill -u <username>
sudo systemctl restart xrdp xrdp-sesman
```

### xrdp — black screen after login

```bash
sudo pkill -u <username>
sudo rm -rf /home/<username>/.cache/sessions
sudo systemctl restart xrdp xrdp-sesman
```

### xrdp — sesman not receiving login attempts

Check that `/etc/xrdp/xrdp.ini` has `port=-1` in the `[Xorg]` section (NOT `port=3389`):

```bash
sudo grep -A5 '^\[Xorg\]' /etc/xrdp/xrdp.ini
```

If it shows `port=3389`, fix it:

```bash
sudo sed -i '/^\[Xorg\]/,/^\[/{s/^port=3389/port=-1/}' /etc/xrdp/xrdp.ini
sudo systemctl restart xrdp xrdp-sesman
```

### Desktop shortcuts show X icons

```bash
sudo chmod +x /home/<username>/Desktop/*.desktop
sudo -u <username> gio set /home/<username>/Desktop/*.desktop metadata::trusted true
```

### Service not starting

```bash
sudo systemctl status <service-name>
journalctl -u <service-name> -n 50
```

### Docker container issues

```bash
docker ps -a              # list all containers
docker logs <container>   # view logs
docker restart <container>
```

### GPU out of memory

```bash
nvidia-smi                        # check what's using GPU
sudo systemctl stop ollama        # free GPU for vLLM/NIM
```

### Disk space

```bash
df -h /
docker system prune -f    # clean unused containers/images
```

---

## Setup Scripts Archive

These scripts were used to build this environment (stored in nashie's home on DGX):

| Script | Purpose |
|--------|---------|
| `setup-dgx-spark.sh` | Initial setup: users, SSH, xrdp, Tailscale, Open WebUI, Ollama models |
| `setup-dgx-spark-continue.sh` | Fixed polkit rules for Ubuntu 24.04 |
| `setup-dgx-advanced-v2.sh` | JupyterLab, vLLM, ComfyUI, RAPIDS, NIM, NeMo (uses venv) |
| `setup-golden-image.sh` | Enterprise desktop: GNOME, Claude Code, desktop shortcuts |
| `setup-enterprise-desktop.sh` | Earlier XFCE-based desktop (superseded by golden image) |
