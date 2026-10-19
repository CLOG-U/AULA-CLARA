#!/usr/bin/env bash
# Reproducible cloud setup. Run from the existing checkout; no worktree is needed.
set -euo pipefail
cd "$(dirname "$0")/.."
python scripts/cloud_toolchain.py
python scripts/cloud_sdk.py
python - <<'PY'
import os
from pathlib import Path
from urllib.parse import urlsplit
root = Path('/workspace/.tools')
gradle = root / 'gradle-user-home'
gradle.mkdir(exist_ok=True)
proxy = urlsplit(os.environ.get('HTTPS_PROXY', ''))
properties = 'org.gradle.daemon=false\norg.gradle.workers.max=3\n'
trust = Path('/etc/ssl/certs/java/cacerts')
if trust.is_file():
    properties += f'systemProp.javax.net.ssl.trustStore={trust}\n'
if proxy.hostname:
    for protocol in ['http', 'https']:
        properties += f'systemProp.{protocol}.proxyHost={proxy.hostname}\nsystemProp.{protocol}.proxyPort={proxy.port or 80}\n'
(gradle / 'gradle.properties').write_text(properties)
(root / 'env.sh').write_text('''export JAVA_HOME=/workspace/.tools/jdk-17.0.16+8
export ANDROID_HOME=/workspace/.tools/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME=/workspace/.tools/android-user
export ANDROID_AVD_HOME=/workspace/.tools/android-user/avd
export GRADLE_USER_HOME=/workspace/.tools/gradle-user-home
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
''')
local = Path('local.properties')
if not local.exists():
    local.write_text('sdk.dir=/workspace/.tools/android-sdk\n')
PY
source /workspace/.tools/env.sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
