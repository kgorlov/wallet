#!/usr/bin/env bash
# Fast wrapper around export_ui.py.
# Use only for apps you own or are explicitly authorized to analyze.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_BIN="${PYTHON_BIN:-python3}"

PACKAGE=""
PACKAGE_FILTER=""
DEVICE=""
OUT_DIR="ui_export"
GENERATE="none"
RECORD_SECONDS=""
LOG_SECONDS=""
FRAME_COUNT=0
FRAME_INTERVAL_MS=100
INCLUDE_PRIVATE=0
SKIP_APK=0
SKIP_UI=0

usage() {
  cat <<'EOF'
Usage:
  ./export_ui.sh --package com.example.app [options]
  ./export_ui.sh --package-filter example [options]

Options:
  --package NAME              Exact Android package name.
  --package-filter TEXT       Find one installed package containing TEXT.
  --device SERIAL             ADB serial from 'adb devices'.
  --out DIR                   Output directory. Default: ui_export.
  --generate flutter|compose  Generate approximate UI skeleton. Default: none.
  --record                    Ask for screen recording duration.
  --record-seconds N          Record the screen for N seconds.
  --logs                      Ask for animation log capture duration.
  --log-seconds N             Capture filtered animation logs for N seconds.
  --frames N                  Capture N slow screenshots.
  --frame-interval-ms N       Delay between screenshots. Default: 100.
  --include-private-app-data  Try adb run-as for files/cache. Authorized apps only.
  --skip-apk                  Skip APK pull/decompile.
  --skip-ui                   Skip uiautomator dump.
  -h, --help                  Show this help.

Examples:
  ./export_ui.sh --package com.example.app --out app_ui_export --generate flutter
  ./export_ui.sh --package-filter example --record --logs
EOF
}

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[error] Missing required command: $1" >&2
    exit 2
  fi
}

warn_optional_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[warn] Optional command not found: $1" >&2
  fi
}

adb_cmd() {
  if [[ -n "$DEVICE" ]]; then
    adb -s "$DEVICE" "$@"
  else
    adb "$@"
  fi
}

check_device() {
  need_cmd adb
  local devices count state
  mapfile -t devices < <(adb devices | awk 'NR > 1 && $2 == "device" {print $1}')
  if [[ "${#devices[@]}" -eq 0 ]]; then
    echo "[error] No authorized Android device found. Check USB debugging and adb devices." >&2
    exit 2
  fi
  if [[ -n "$DEVICE" ]]; then
    state="$(adb devices | awk -v serial="$DEVICE" '$1 == serial {print $2}')"
    if [[ "$state" != "device" ]]; then
      echo "[error] Requested device is not online/authorized: $DEVICE" >&2
      exit 2
    fi
    echo "[info] Using device: $DEVICE"
    return
  fi
  count="${#devices[@]}"
  DEVICE="${devices[0]}"
  if [[ "$count" -gt 1 ]]; then
    echo "[warn] Multiple devices found; using $DEVICE. Pass --device SERIAL to choose explicitly." >&2
    printf '[warn] Online devices: %s\n' "${devices[*]}" >&2
  else
    echo "[info] Using device: $DEVICE"
  fi
}

find_package() {
  if [[ -n "$PACKAGE" ]]; then
    return
  fi
  if [[ -z "$PACKAGE_FILTER" ]]; then
    read -r -p "Package filter: " PACKAGE_FILTER
  fi
  mapfile -t matches < <(adb_cmd shell pm list packages | sed 's/^package://' | grep -i -- "$PACKAGE_FILTER" || true)
  if [[ "${#matches[@]}" -eq 0 ]]; then
    echo "[error] No packages matched filter: $PACKAGE_FILTER" >&2
    exit 2
  fi
  if [[ "${#matches[@]}" -gt 1 ]]; then
    echo "[error] Multiple packages matched. Re-run with --package:" >&2
    printf '  %s\n' "${matches[@]}" >&2
    exit 2
  fi
  PACKAGE="${matches[0]}"
  echo "[info] Package filter matched: $PACKAGE"
}

ask_record_duration() {
  if [[ "$RECORD_SECONDS" == "ask" ]]; then
    read -r -p "Screen recording duration in seconds [10]: " value
    RECORD_SECONDS="${value:-10}"
  fi
}

ask_log_duration() {
  if [[ "$LOG_SECONDS" == "ask" ]]; then
    read -r -p "Animation log capture duration in seconds [20]: " value
    LOG_SECONDS="${value:-20}"
    echo "[info] Start interacting with the app when log capture begins."
  fi
}

run_exporter() {
  local args=("$SCRIPT_DIR/export_ui.py" --package "$PACKAGE" --out "$OUT_DIR" --generate "$GENERATE")
  if [[ -n "$DEVICE" ]]; then
    args+=(--device "$DEVICE")
  fi
  if [[ "$INCLUDE_PRIVATE" -eq 1 ]]; then
    args+=(--include-private-app-data)
  fi
  if [[ "$SKIP_APK" -eq 1 ]]; then
    args+=(--skip-apk)
  fi
  if [[ "$SKIP_UI" -eq 1 ]]; then
    args+=(--skip-ui)
  fi
  if [[ -n "${RECORD_SECONDS:-}" && "$RECORD_SECONDS" != "0" ]]; then
    args+=(--record-seconds "$RECORD_SECONDS")
  fi
  if [[ -n "${LOG_SECONDS:-}" && "$LOG_SECONDS" != "0" ]]; then
    args+=(--log-seconds "$LOG_SECONDS")
  fi
  if [[ "$FRAME_COUNT" -gt 0 ]]; then
    args+=(--frame-count "$FRAME_COUNT" --frame-interval-ms "$FRAME_INTERVAL_MS")
  fi
  "$PYTHON_BIN" "${args[@]}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --package)
      PACKAGE="${2:?missing package}"
      shift 2
      ;;
    --package-filter)
      PACKAGE_FILTER="${2:?missing filter}"
      shift 2
      ;;
    --device)
      DEVICE="${2:?missing device serial}"
      shift 2
      ;;
    --out)
      OUT_DIR="${2:?missing output dir}"
      shift 2
      ;;
    --generate)
      GENERATE="${2:?missing target}"
      shift 2
      ;;
    --record)
      RECORD_SECONDS="ask"
      shift
      ;;
    --record-seconds)
      RECORD_SECONDS="${2:?missing seconds}"
      shift 2
      ;;
    --logs)
      LOG_SECONDS="ask"
      shift
      ;;
    --log-seconds)
      LOG_SECONDS="${2:?missing seconds}"
      shift 2
      ;;
    --frames)
      FRAME_COUNT="${2:?missing frame count}"
      shift 2
      ;;
    --frame-interval-ms)
      FRAME_INTERVAL_MS="${2:?missing interval}"
      shift 2
      ;;
    --include-private-app-data)
      INCLUDE_PRIVATE=1
      shift
      ;;
    --skip-apk)
      SKIP_APK=1
      shift
      ;;
    --skip-ui)
      SKIP_UI=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[error] Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

need_cmd "$PYTHON_BIN"
check_device
warn_optional_cmd apktool
find_package
ask_record_duration
ask_log_duration
run_exporter
