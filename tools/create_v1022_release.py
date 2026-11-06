import urllib.request
import json
import os
import re
import subprocess

def main():
    remote = subprocess.check_output(['git', 'remote', 'get-url', 'origin']).decode().strip()
    m = re.search(r'x-access-token:([^@]+)@', remote)
    token = m.group(1) if m else os.environ.get('GITHUB_TOKEN')

    headers = {
        'Authorization': f'Bearer {token}',
        'Accept': 'application/vnd.github+json',
        'User-Agent': 'GymFlow-Release-Bot'
    }

    tag = 'v1.0.22'
    title = 'GymFlow v1.0.22 - Instant GIF Sync & Firebase Crash Telemetry'
    notes = (
        "## What's New in GymFlow v1.0.22\n\n"
        "- ⚡ **Instant Exercise GIF Loading**: Dual-layer crossfade engine with screen-level eager preloading for 0ms animated movement loops.\n"
        "- 🔄 **Multi-Threaded Background Preloader**: Core workout routine GIFs now synchronize concurrently in parallel batches.\n"
        "- 🔒 **Deterministic Release Signing**: Keystore signing unified across all debug and release builds, permanently eliminating package signature conflicts.\n"
        "- 🛡️ **Automated Firebase Firestore Bug Telemetry**: Continuous crash reporting, update diagnostics, and pre-install signature verification.\n"
        "- 🐞 **In-App Bug Reporter**: Direct feedback and diagnostics reporting built into Member Settings.\n"
        "- 📦 **Differential Delta Update**: Delta patch `GymFlow-1.0.21-to-1.0.22.patch` available for fast, low-bandwidth updates."
    )

    # 1. Check if release already exists
    release_url = f'https://api.github.com/repos/sumit01-coder/GYMFLOW/releases/tags/{tag}'
    req = urllib.request.Request(release_url, headers=headers)
    rel = None
    try:
        with urllib.request.urlopen(req) as resp:
            rel = json.loads(resp.read().decode('utf-8'))
            print(f"Release {tag} already exists (id: {rel['id']})")
    except urllib.error.HTTPError as e:
        if e.code == 404:
            print(f"Creating new GitHub Release {tag}...")
            create_payload = json.dumps({
                'tag_name': tag,
                'target_commitish': 'main',
                'name': title,
                'body': notes,
                'draft': False,
                'prerelease': False
            }).encode('utf-8')
            create_req = urllib.request.Request(
                'https://api.github.com/repos/sumit01-coder/GYMFLOW/releases',
                data=create_payload,
                headers={**headers, 'Content-Type': 'application/json'},
                method='POST'
            )
            with urllib.request.urlopen(create_req) as create_resp:
                rel = json.loads(create_resp.read().decode('utf-8'))
                print(f"Created release {tag} successfully (id: {rel['id']})")
        else:
            raise

    upload_url = rel['upload_url'].split('{')[0]
    existing_assets = {a['name']: a['id'] for a in rel.get('assets', [])}

    files_to_upload = [
        ('GymFlow-v1.0.22.apk', 'application/vnd.android.package-archive'),
        ('GymFlow-v1.0.22.apk.sha256', 'text/plain'),
        ('GymFlow-1.0.21-to-1.0.22.patch', 'application/octet-stream')
    ]

    for fname, ctype in files_to_upload:
        if not os.path.exists(fname):
            print(f"Warning: File {fname} does not exist, skipping.")
            continue

        if fname in existing_assets:
            del_id = existing_assets[fname]
            del_req = urllib.request.Request(
                f'https://api.github.com/repos/sumit01-coder/GYMFLOW/releases/assets/{del_id}',
                headers=headers,
                method='DELETE'
            )
            with urllib.request.urlopen(del_req) as del_resp:
                print(f"Deleted existing asset {fname} ({del_id})")

        fsize = os.path.getsize(fname)
        print(f"Uploading {fname} ({fsize} bytes)...")
        with open(fname, 'rb') as f:
            fdata = f.read()

        up_headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': ctype,
            'Content-Length': str(fsize),
            'User-Agent': 'GymFlow-Release-Bot'
        }
        up_req = urllib.request.Request(
            f"{upload_url}?name={fname}",
            data=fdata,
            headers=up_headers,
            method='POST'
        )
        with urllib.request.urlopen(up_req) as up_resp:
            res = json.loads(up_resp.read().decode('utf-8'))
            print(f"Uploaded {fname} successfully -> {res['browser_download_url']}")

    print("\nRelease v1.0.22 completed successfully!")

if __name__ == '__main__':
    main()
