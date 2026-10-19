#!/usr/bin/env bash
# Prepare only the dedicated development AVD, never a connected physical device.
set -euo pipefail
source /workspace/.tools/env.sh
device_serial="${1:-emulator-5554}"
case "$device_serial" in emulator-*) ;; *) echo "Use the dedicated emulator serial." >&2; exit 1 ;; esac
timeout 240 adb -s "$device_serial" wait-for-device
if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then
    adb -s "$device_serial" root
    timeout 120 adb -s "$device_serial" wait-for-device
    multiplier="$(adb -s "$device_serial" shell getprop ro.hw_timeout_multiplier | tr -d '\r')"
    if [[ -z "$multiplier" ]]; then
        adb -s "$device_serial" shell setprop ro.hw_timeout_multiplier 10
        # Reload framework classes so their watchdog timeout uses this property.
        adb -s "$device_serial" shell stop
        adb -s "$device_serial" shell start
    fi
fi
for ((attempt=0; attempt<180; attempt++)); do
    if [[ "$(adb -s "$device_serial" shell getprop sys.boot_completed | tr -d '\r')" == 1 ]]; then
        adb -s "$device_serial" shell svc wifi disable
        adb -s "$device_serial" shell svc data disable
        adb -s "$device_serial" shell settings put global window_animation_scale 0
        adb -s "$device_serial" shell settings put global transition_animation_scale 0
        adb -s "$device_serial" shell settings put global animator_duration_scale 0
        echo "Android emulator ready for offline MVP tests: $device_serial"
        exit 0
    fi
    echo "Waiting for Android boot ($attempt/180)..."
    sleep 5
done
echo "Android boot did not finish; inspect the emulator log." >&2
exit 1
