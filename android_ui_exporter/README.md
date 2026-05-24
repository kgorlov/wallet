# Android UI Exporter

Educational Android UI export scripts for apps you own or are explicitly
authorized to analyze. Do not use this to clone a third-party brand, wallet, or
production app interface without permission.

## Prerequisites

- Android platform tools: `adb`
- USB debugging enabled on the connected Android device
- Java Runtime Environment, required by apktool
- `apktool` available on `PATH`
- Python 3.10+
- Optional Python packages:

```bash
python -m pip install -r requirements.txt
```

The current Python implementation uses the standard library for extraction and
parsing. `lxml` and `Pillow` are listed for follow-up analysis, validation, image
inspection, and richer local processing.

## Quick Start

PowerShell or CMD:

```powershell
python .\export_ui.py --package com.example.app --out app_ui_export --generate flutter
```

If more than one device/emulator is connected, choose one explicitly:

```powershell
adb devices
python .\export_ui.py --device 35e35b0d --package com.example.app --out app_ui_export --generate flutter
```

Git Bash or WSL:

```bash
./export_ui.sh --package com.example.app --out app_ui_export --generate flutter
```

Find a package by substring:

```bash
adb shell pm list packages | grep -i example
python export_ui.py --package-filter example --out app_ui_export
```

PowerShell package search examples:

```powershell
adb -s 35e35b0d shell pm list packages | findstr /i trust
python .\export_ui.py --device 35e35b0d --package-filter trust --out app_ui_export
```

Package names do not contain spaces. Use a single package filter such as
`trust`, or quote multi-word arguments if you really need them.

## Live MIUI Capture and Control

For the connected MIUI phone, use the local helper from the workspace root:

```powershell
.\android_ui_exporter\miui.cmd devices
.\android_ui_exporter\miui.cmd launch com.wallet.crypto.trustapp
.\android_ui_exporter\miui.cmd capture trust_now
.\android_ui_exporter\miui.cmd dump trust_now
.\android_ui_exporter\miui.cmd tap 620 790
.\android_ui_exporter\miui.cmd scroll-down
```

`capture` writes `PNG`, `XML`, and `meta.json` files into `phone_capture`.
On MIUI, `tap` and `swipe` require Developer options -> USB debugging
(Security settings). If that switch is off, Android returns `INJECT_EVENTS`
security errors and only capture/dump/launch commands remain available.

Capture video and animation logs:

```bash
python export_ui.py --package com.example.app --out app_ui_export --record-seconds 10 --log-seconds 20
```

Try dynamic resource extraction with `run-as` for an owned/debuggable app:

```bash
python export_ui.py --package com.example.app --out app_ui_export --include-private-app-data
```

`run-as` normally fails for release builds that are not debuggable. The script
does not bypass that. Private app folders may contain sensitive user data; review
authorization and scope before using the flag.

## Output

The output directory is structured as:

- `images/`: PNG, WebP, JPEG, GIF assets
- `vectors/`: converted SVG files and unconverted vector XML fallbacks
- `animations/`: Lottie JSON, `.lottie`, and XML animation resources
- `fonts/`: TTF and OTF assets
- `dynamic_res/`: resource-like files copied through `run-as`, if enabled
- `extracted_res/`: complete decompiled `res/` tree when apktool is available
- `ui.xml`: raw UIAutomator dump
- `ui_hierarchy.json`: parsed UI tree with classes, labels, and bounds
- `animation.mp4`: optional screen recording
- `animation_logs.txt`: optional filtered animation logcat output
- `animation_params.json`: parsed duration/property/interpolator guesses
- `colors.xml`, `colors.json`, `dimens.xml`, `dimens.json`, `styles.xml`
- `generated/flutter_ui/` or `generated/compose_ui/`: approximate layout skeleton
- `report.json`: summary of performed steps

## Notes

- `screenrecord` supports output size, but not arbitrary crop regions. Crop later
  with a video tool such as ffmpeg if needed.
- Slow screenshot frame capture is available through `--frame-count`, but video
  is usually more reliable for animation analysis.
- Vector conversion is intentionally conservative. Complex Android vector
  features may remain as copied XML in `vectors/`.
