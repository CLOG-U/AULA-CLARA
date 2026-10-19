#!/usr/bin/env bash
set -euo pipefail
source /workspace/.tools/env.sh
avd_name=aula_clara_api29
if [[ ! -f "$ANDROID_AVD_HOME/$avd_name.ini" ]]; then
    avdmanager create avd -n "$avd_name" -k 'system-images;android-29;default;x86_64' --device pixel_3a --force < /dev/null
    python - <<'PY'
import os, re
from pathlib import Path
p = Path(os.environ['ANDROID_AVD_HOME']) / 'aula_clara_api29.avd/config.ini'
s = p.read_text()
for key, value in [('hw.lcd.width', '720'), ('hw.lcd.height', '1280'), ('hw.lcd.density', '320'), ('hw.ramSize', '4096'), ('vm.heapSize', '512')]:
    pattern = '^' + re.escape(key) + '=.*$'
    s = re.sub(pattern, key + '=' + value, s, flags=re.M) if re.search(pattern, s, flags=re.M) else s + '\n' + key + '=' + value + '\n'
p.write_text(s)
PY
fi
# A restored task can retain lock files after its emulator process was terminated.
python - <<'PY'
import os, subprocess
from pathlib import Path
processes = subprocess.check_output(['ps', '-eo', 'stat=,comm='], text=True).splitlines()
live = any('qemu-system' in line and not line.strip().startswith('Z') for line in processes)
if not live:
    root = Path(os.environ['ANDROID_AVD_HOME']) / 'aula_clara_api29.avd'
    for name in ['hardware-qemu.ini.lock', 'multiinstance.lock']:
        (root / name).unlink(missing_ok=True)
PY
acceleration=auto
if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then acceleration=off; fi
exec emulator -avd "$avd_name" -no-window -no-audio -no-boot-anim -no-snapshot \
    -gpu swiftshader_indirect -accel "$acceleration" -cores 2 -memory 4096 \
    -camera-back none -camera-front none -no-metrics
