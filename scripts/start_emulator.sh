#!/usr/bin/env bash
set -euo pipefail
source /workspace/.tools/env.sh
avd_name=aula_clara_api29
if [[ ! -f "$ANDROID_AVD_HOME/$avd_name.ini" ]]; then
    avdmanager create avd -n "$avd_name" -k 'system-images;android-29;default;x86_64' --device pixel_3a --force < /dev/null
fi
# A restored task can retain lock files after its emulator process was terminated.
python - <<'PY'
import os, re, subprocess
from pathlib import Path
processes = subprocess.check_output(['ps', '-eo', 'stat=,comm='], text=True).splitlines()
live = any('qemu-system' in line and not line.strip().startswith('Z') for line in processes)
if not live:
    root = Path(os.environ['ANDROID_AVD_HOME']) / 'aula_clara_api29.avd'
    # Apply to restored AVDs too: Pixel profiles disable host keyboard input.
    config = root / 'config.ini'
    text = config.read_text()
    for key, value in [('hw.lcd.width', '720'), ('hw.lcd.height', '1280'), ('hw.lcd.density', '320'),
                       ('hw.ramSize', '4096'), ('vm.heapSize', '512'),
                       ('hw.keyboard', 'yes'), ('hw.keyboard.lid', 'no')]:
        pattern = '^' + re.escape(key) + '=.*$'
        text = re.sub(pattern, key + '=' + value, text, flags=re.M) if re.search(pattern, text, flags=re.M) else text + '\n' + key + '=' + value + '\n'
    config.write_text(text)
    for name in ['hardware-qemu.ini.lock', 'multiinstance.lock']:
        (root / name).unlink(missing_ok=True)
PY
acceleration=auto
if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then acceleration=off; fi
exec emulator -avd "$avd_name" -no-window -no-audio -no-boot-anim -no-snapshot \
    -gpu swiftshader_indirect -accel "$acceleration" -cores 2 -memory 4096 \
    -camera-back none -camera-front none -no-metrics
