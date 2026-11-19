# GYMFLOW Automated Release & APK Version Manager

This document describes the automated **Release Manager Subagent** and deployment pipeline for the GYMFLOW application.

## Overview
The Release Manager acts as a strict release gatekeeper:
- **Build & Test Gatekeeper**: Validates that all unit tests (`./gradlew testDebugUnitTest`) pass before any version update or release.
- **Version Bumping**: Automates semantic version increments (`versionName` & `calculatedVersionCode` in `app/build.gradle.kts`).
- **Release Verification & Audit**: Inspects built APK files, generates SHA-256 integrity checksums, and audits eligibility (signed production APK vs. debug builds).
- **GitHub Release Dispatcher**: Manages git tags (e.g. `v1.0.44`) and pushes to `origin`, triggering the GitHub Actions CI/CD workflow (`.github/workflows/release-apk.yml`) to publish releases with assets.

---

## Dedicated Subagent

The `release-manager` subagent is defined and active in Antigravity:
- **Name**: `release-manager`
- **Role**: `APK Release & Version Manager`
- **Trigger**: Can be invoked directly using `invoke_subagent` or via CLI.

---

## CLI Automation: `scripts/release_manager.py`

You can run the release automation directly from the terminal:

### 1. Audit APK & GitHub Release Status
```powershell
python scripts/release_manager.py --audit
```
Checks:
- Current configured app version
- Latest locally compiled APK (release or debug)
- APK file size and SHA-256 checksum
- Eligibility for GitHub release (rejects debug-only or untested builds)
- Recent GitHub releases and attached assets

### 2. Run Tests Only
```powershell
python scripts/release_manager.py --test
```

### 3. Bump Version Only
```powershell
python scripts/release_manager.py --bump patch   # e.g., 1.0.43 -> 1.0.44
python scripts/release_manager.py --bump minor   # e.g., 1.0.44 -> 1.1.0
python scripts/release_manager.py --bump major   # e.g., 1.1.0  -> 2.0.0
```

### 4. Build Release APK Only
```powershell
python scripts/release_manager.py --build release
```

### 5. Full End-to-End Release Pipeline
```powershell
python scripts/release_manager.py --release patch --notes "Performance enhancements, dynamic QR scan athlete sync, and bug fixes."
```
This performs the entire sequence in order:
1. Runs full unit test suite.
2. Bumps `versionName` and `versionCode` in `app/build.gradle.kts`.
3. Compiles signed production release APK (`:app:assembleRelease`).
4. Generates and displays SHA-256 checksum.
5. Commits version change and creates annotated git tag `v<version>`.
6. Pushes commit and tag to GitHub, triggering `.github/workflows/release-apk.yml`.
