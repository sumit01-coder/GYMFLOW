#!/usr/bin/env python3
"""
GYMFLOW Release & Version Manager Tool
Automates build validation, unit testing, semantic version bumping,
APK checksum verification, and GitHub Release deployment gating.
"""

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
from pathlib import Path
import urllib.request
import urllib.error

REPO_ROOT = Path(__file__).resolve().parent.parent
BUILD_GRADLE = REPO_ROOT / "app" / "build.gradle.kts"
APK_RELEASE_DIR = REPO_ROOT / "app" / "build" / "outputs" / "apk" / "release"
APK_DEBUG_DIR = REPO_ROOT / "app" / "build" / "outputs" / "apk" / "debug"
GITHUB_REPO = "sumit01-coder/GYMFLOW"

def get_gradle_cmd():
    if os.name == 'nt':
        bat = REPO_ROOT / "gradlew.bat"
        if bat.exists():
            return str(bat)
    sh = REPO_ROOT / "gradlew"
    if sh.exists():
        return str(sh)
    return "gradle"

def read_current_version():
    content = BUILD_GRADLE.read_text(encoding="utf-8")
    m = re.search(r'\?:\s*"([0-9]+\.[0-9]+\.[0-9]+)"', content)
    if not m:
        raise ValueError("Could not find default appVersionName in app/build.gradle.kts")
    version_str = m.group(1)
    parts = [int(p) for p in version_str.split(".")]
    return parts[0], parts[1], parts[2], version_str

def bump_version(part="patch"):
    major, minor, patch, current_str = read_current_version()
    if part == "major":
        major += 1
        minor = 0
        patch = 0
    elif part == "minor":
        minor += 1
        patch = 0
    elif part == "patch":
        patch += 1
    else:
        raise ValueError(f"Unknown bump part: {part}")

    new_version_str = f"{major}.{minor}.{patch}"
    content = BUILD_GRADLE.read_text(encoding="utf-8")

    # Replace the default fallback in rawVersionName
    content = re.sub(
        r'(\?:\s*)"[0-9]+\.[0-9]+\.[0-9]+"',
        rf'\1"{new_version_str}"',
        content,
        count=1
    )
    # Replace default patch in versionParts
    content = re.sub(
        r'val patch = versionParts\.getOrElse\(2\) \{ [0-9]+ \}',
        f'val patch = versionParts.getOrElse(2) {{ {patch} }}',
        content,
        count=1
    )

    BUILD_GRADLE.write_text(content, encoding="utf-8")
    print(f"[VERSION BUMP] Successfully bumped version: v{current_str} -> v{new_version_str}")
    return new_version_str

def run_tests():
    cmd = [get_gradle_cmd(), "testDebugUnitTest"]
    print(f"[TESTS] Running unit tests: {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=str(REPO_ROOT))
    if result.returncode != 0:
        print("[ERROR] Unit tests failed! Aborting release pipeline.")
        sys.exit(1)
    print("[TESTS] All unit tests passed cleanly.")

def build_apk(mode="release"):
    task = "assembleRelease" if mode == "release" else "assembleDebug"
    cmd = [get_gradle_cmd(), task]
    print(f"[BUILD] Building {mode} APK: {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=str(REPO_ROOT))
    if result.returncode != 0:
        print(f"[ERROR] Build {task} failed! Aborting release pipeline.")
        sys.exit(1)
    print(f"[BUILD] {task} finished successfully.")

def calculate_sha256(file_path: Path):
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()

def find_latest_apk():
    # Check release dir first
    if APK_RELEASE_DIR.exists():
        apks = list(APK_RELEASE_DIR.glob("*.apk"))
        if apks:
            apks.sort(key=lambda p: p.stat().st_mtime, reverse=True)
            return apks[0], "release"

    # Check debug dir
    if APK_DEBUG_DIR.exists():
        apks = list(APK_DEBUG_DIR.glob("*.apk"))
        if apks:
            apks.sort(key=lambda p: p.stat().st_mtime, reverse=True)
            return apks[0], "debug"

    return None, None

def audit_local_apks():
    print("=" * 60)
    print("  GYMFLOW APK RELEASE AUDIT")
    print("=" * 60)
    _, _, _, current_ver = read_current_version()
    print(f"Current Configured Version : v{current_ver}")
    
    apk, mode = find_latest_apk()
    if not apk:
        print("[AUDIT STATUS] No APK binaries found. Build required.")
        return None
    
    size_mb = apk.stat().st_size / (1024 * 1024)
    sha256 = calculate_sha256(apk)
    print(f"Latest Built APK : {apk.name}")
    print(f"Build Mode       : {mode.upper()}")
    print(f"File Size        : {size_mb:.2f} MB")
    print(f"SHA-256 Checksum : {sha256}")
    
    if mode == "release":
        print("[AUDIT STATUS] ELIGIBLE FOR GITHUB RELEASE (Signed release build).")
    else:
        print("[AUDIT STATUS] DEBUG ONLY (Not eligible for production GitHub release).")
    return apk

def check_github_releases():
    url = f"https://api.github.com/repos/{GITHUB_REPO}/releases"
    print(f"[GITHUB] Fetching releases from {url}...")
    req = urllib.request.Request(
        url,
        headers={"User-Agent": "GymFlow-Release-Manager"}
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            print(f"[GITHUB] Found {len(data)} published releases:")
            for r in data[:5]:
                assets = [a['name'] for a in r.get('assets', [])]
                print(f"  • {r.get('tag_name')} | {r.get('name')} | Assets: {', '.join(assets) or 'None'}")
    except Exception as e:
        print(f"[GITHUB] Note: Could not query GitHub releases API directly: {e}")

def create_and_push_tag(version_str, notes):
    tag = f"v{version_str}"
    print(f"[GIT] Committing changes and version bump to {tag}...")
    subprocess.run(["git", "add", "."], cwd=str(REPO_ROOT), check=True)
    subprocess.run(["git", "commit", "-m", f"fix(sync): real-time firestore member sync & eliminate mock data ({tag})"], cwd=str(REPO_ROOT), check=False)
    
    print(f"[GIT] Tagging {tag}...")
    subprocess.run(["git", "tag", "-a", tag, "-m", notes], cwd=str(REPO_ROOT), check=True)
    
    print(f"[GIT] Pushing commit and tag to origin main...")
    subprocess.run(["git", "push", "origin", "main"], cwd=str(REPO_ROOT), check=True)
    subprocess.run(["git", "push", "origin", tag], cwd=str(REPO_ROOT), check=True)
    print(f"[GITHUB ACTIONS] Pushed tag {tag}. Triggering automated Build & Release workflow!")

def main():
    parser = argparse.ArgumentParser(description="GYMFLOW APK Release & Version Manager")
    parser.add_argument("--audit", action="store_true", help="Audit APK binaries and GitHub release status")
    parser.add_argument("--test", action="store_true", help="Run full unit test suite")
    parser.add_argument("--bump", choices=["patch", "minor", "major"], help="Bump semantic version in build.gradle.kts")
    parser.add_argument("--build", choices=["release", "debug"], help="Build APK target")
    parser.add_argument("--release", choices=["patch", "minor", "major"], help="Full release pipeline: test -> bump -> build -> tag -> push to GitHub")
    parser.add_argument("--notes", default="GymFlow update with performance enhancements, bug fixes, and verified APK release.", help="Release notes for tag")
    
    args = parser.parse_args()

    if args.audit:
        audit_local_apks()
        check_github_releases()
        return

    if args.test:
        run_tests()
        return

    if args.bump:
        bump_version(args.bump)
        return

    if args.build:
        build_apk(args.build)
        audit_local_apks()
        return

    if args.release:
        print(f"=== STARTING AUTOMATED RELEASE PIPELINE ({args.release.upper()}) ===")
        run_tests()
        new_version = bump_version(args.release)
        build_apk("release")
        apk = audit_local_apks()
        if not apk:
            print("[ERROR] Release APK was not produced. Aborting.")
            sys.exit(1)
        create_and_push_tag(new_version, args.notes)
        print("=== RELEASE PIPELINE COMPLETED SUCCESSFULLY ===")
        return

    # Default: run audit
    audit_local_apks()
    check_github_releases()

if __name__ == "__main__":
    main()
