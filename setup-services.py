import subprocess

web = """[Unit]
Description=OnDevice AI Web
After=network.target docker.service

[Service]
User=nashie
WorkingDirectory=/home/nashie/Development/gora-ai-web
EnvironmentFile=/home/nashie/Development/gora-ai-web/.env.local
Environment=PATH=/home/nashie/.nvm/versions/node/v24.13.0/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin
Environment=PORT=3002
ExecStart=/home/nashie/.nvm/versions/node/v24.13.0/bin/node /home/nashie/.nvm/versions/node/v24.13.0/bin/pnpm start
Restart=always
RestartSec=3s

[Install]
WantedBy=multi-user.target
"""

tunnel = """[Unit]
Description=Cloudflare Tunnel OnDevice AI
After=network.target

[Service]
User=nashie
ExecStart=/usr/local/bin/cloudflared tunnel run ondevice
Restart=always
RestartSec=3s

[Install]
WantedBy=multi-user.target
"""

with open('/etc/systemd/system/ondevice-web.service', 'w') as f:
    f.write(web)
print("wrote ondevice-web.service")

with open('/etc/systemd/system/cloudflared-ondevice.service', 'w') as f:
    f.write(tunnel)
print("wrote cloudflared-ondevice.service")

subprocess.run(['systemctl', 'daemon-reload'], check=True)
subprocess.run(['systemctl', 'enable', 'ondevice-web', 'cloudflared-ondevice'], check=True)
print("Services enabled. Run: sudo systemctl start ondevice-web cloudflared-ondevice")
