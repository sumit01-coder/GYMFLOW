import urllib.request
import json
import os
import re
import subprocess

remote = subprocess.check_output(['git', 'remote', 'get-url', 'origin']).decode().strip()
m = re.search(r'x-access-token:([^@]+)@', remote)
token = m.group(1) if m else os.environ.get('GITHUB_TOKEN')

headers = {
    'Authorization': f'Bearer {token}',
    'Accept': 'application/vnd.github+json',
    'User-Agent': 'GymFlow-Release-Bot'
}

req = urllib.request.Request(
    'https://api.github.com/repos/sumit01-coder/GYMFLOW/releases/tags/v1.0.21',
    headers=headers
)
with urllib.request.urlopen(req) as resp:
    rel = json.loads(resp.read().decode('utf-8'))
    release_id = rel['id']
    upload_url = rel['upload_url'].split('{')[0]
    assets = rel.get('assets', [])

for a in assets:
    del_req = urllib.request.Request(
        f'https://api.github.com/repos/sumit01-coder/GYMFLOW/releases/assets/{a["id"]}',
        headers=headers,
        method='DELETE'
    )
    with urllib.request.urlopen(del_req) as del_resp:
        print(f"Deleted old asset: {a['name']} ({a['id']})")

apk_path = 'GymFlow-v1.0.21.apk'
apk_size = os.path.getsize(apk_path)
print(f"Uploading signed {apk_path} ({apk_size} bytes)...")

upload_headers = {
    'Authorization': f'Bearer {token}',
    'Content-Type': 'application/vnd.android.package-archive',
    'Content-Length': str(apk_size),
    'User-Agent': 'GymFlow-Release-Bot'
}

with open(apk_path, 'rb') as f:
    apk_bytes = f.read()

upload_req = urllib.request.Request(
    f"{upload_url}?name=GymFlow-v1.0.21.apk",
    data=apk_bytes,
    headers=upload_headers,
    method='POST'
)

with urllib.request.urlopen(upload_req) as upload_resp:
    asset_data = json.loads(upload_resp.read().decode('utf-8'))
    print(f"Uploaded signed asset successfully: {asset_data['name']} URL: {asset_data['browser_download_url']}")
