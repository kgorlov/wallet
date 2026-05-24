#!/usr/bin/env python3
"""
Android UI resource exporter for authorized analysis.

The tool extracts public APK resources, UIAutomator hierarchy dumps, optional
screen recordings, animation-related logcat lines, and resource metadata. It is
intended for apps you own or are explicitly allowed to analyze.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import time
import zipfile
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple
from xml.etree import ElementTree as ET


ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
ADB_SERIAL: Optional[str] = None
RESOURCE_EXTS = {
    ".json",
    ".lottie",
    ".png",
    ".jpg",
    ".jpeg",
    ".webp",
    ".gif",
    ".ttf",
    ".otf",
    ".xml",
}
IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".webp", ".gif"}
FONT_EXTS = {".ttf", ".otf"}
LOTTIE_EXTS = {".json", ".lottie"}
ANIMATION_LOG_RE = re.compile(
    r"Animator|Choreographer|ViewPropertyAnimator|translationX|translationY|"
    r"alpha|scaleX|scaleY|rotation|duration|interpolator",
    re.IGNORECASE,
)
PROPERTY_RE = re.compile(
    r"\b(alpha|translationX|translationY|translationZ|scaleX|scaleY|rotation|"
    r"rotationX|rotationY|x|y|z|elevation|backgroundColor)\b"
)
DURATION_RE = re.compile(r"(?:duration|setDuration|dur)\D{0,12}(\d{2,6})", re.IGNORECASE)
INTERPOLATOR_RE = re.compile(r"([A-Za-z0-9_$.]*(?:Interpolator|Easing)[A-Za-z0-9_$.]*)")


class ExportError(RuntimeError):
    """Raised for expected, user-actionable failures."""


def info(message: str) -> None:
    print(f"[info] {message}")


def warn(message: str) -> None:
    print(f"[warn] {message}", file=sys.stderr)


def ensure_dir(path: Path) -> Path:
    path.mkdir(parents=True, exist_ok=True)
    return path


def run(
    args: Sequence[str],
    *,
    check: bool = True,
    capture: bool = True,
    text: bool = True,
    timeout: Optional[int] = None,
) -> subprocess.CompletedProcess:
    try:
        return subprocess.run(
            list(args),
            check=check,
            capture_output=capture,
            text=text,
            timeout=timeout,
        )
    except FileNotFoundError as exc:
        raise ExportError(f"Command not found: {args[0]}") from exc
    except subprocess.CalledProcessError as exc:
        stdout = exc.stdout or ""
        stderr = exc.stderr or ""
        detail = (stderr or stdout).strip()
        if detail:
            raise ExportError(f"Command failed: {' '.join(args)}\n{detail}") from exc
        raise ExportError(f"Command failed: {' '.join(args)}") from exc


def set_adb_serial(serial: Optional[str]) -> None:
    global ADB_SERIAL
    ADB_SERIAL = serial


def adb_command(args: Sequence[str]) -> List[str]:
    command = ["adb"]
    if ADB_SERIAL:
        command.extend(["-s", ADB_SERIAL])
    command.extend(args)
    return command


def adb(args: Sequence[str], **kwargs: Any) -> subprocess.CompletedProcess:
    return run(adb_command(args), **kwargs)


def adb_shell(command: str, **kwargs: Any) -> subprocess.CompletedProcess:
    return adb(["shell", command], **kwargs)


def check_tool(command: str, required: bool = True) -> bool:
    found = shutil.which(command) is not None
    if not found and required:
        raise ExportError(f"Required tool is missing: {command}")
    if not found:
        warn(f"Optional tool is missing: {command}")
    return found


def check_device(requested_serial: Optional[str]) -> str:
    check_tool("adb", required=True)
    result = run(["adb", "devices"], capture=True)
    devices: List[Tuple[str, str]] = []
    for line in result.stdout.splitlines()[1:]:
        line = line.strip()
        if not line:
            continue
        parts = line.split()
        if len(parts) >= 2:
            devices.append((parts[0], parts[1]))
    online = [serial for serial, state in devices if state == "device"]
    if not online:
        states = ", ".join(f"{serial}:{state}" for serial, state in devices) or "none"
        raise ExportError(f"No authorized Android device found. adb devices: {states}")
    if requested_serial:
        matching = [state for serial, state in devices if serial == requested_serial]
        if not matching:
            raise ExportError(
                f"Requested device '{requested_serial}' was not found. "
                f"Connected devices: {', '.join(serial for serial, _ in devices)}"
            )
        if matching[0] != "device":
            raise ExportError(f"Requested device '{requested_serial}' is not authorized: {matching[0]}")
        info(f"Using device: {requested_serial}")
        return requested_serial
    if len(online) > 1:
        warn(
            f"Multiple devices found; using {online[0]}. "
            f"Pass --device SERIAL to choose one explicitly. Online: {', '.join(online)}"
        )
    else:
        info(f"Using device: {online[0]}")
    return online[0]


def list_packages() -> List[str]:
    result = adb_shell("pm list packages", capture=True)
    packages = []
    for line in result.stdout.splitlines():
        line = line.strip()
        if line.startswith("package:"):
            packages.append(line.split(":", 1)[1])
    return packages


def resolve_package(package: Optional[str], package_filter: Optional[str]) -> str:
    packages = list_packages()
    if package:
        if package not in packages:
            matches = [p for p in packages if package.lower() in p.lower()]
            if matches:
                raise ExportError(
                    f"Package '{package}' is not installed. Similar matches: {', '.join(matches[:10])}"
                )
            raise ExportError(f"Package '{package}' is not installed on the device.")
        info(f"Using package: {package}")
        return package

    if not package_filter:
        raise ExportError("Pass --package com.example.app or --package-filter text.")

    matches = [p for p in packages if package_filter.lower() in p.lower()]
    if not matches:
        raise ExportError(f"No packages matched filter: {package_filter}")
    if len(matches) > 1:
        raise ExportError(
            "Package filter matched multiple packages. Re-run with --package:\n"
            + "\n".join(f"  {p}" for p in matches)
        )
    info(f"Package filter matched: {matches[0]}")
    return matches[0]


def get_base_apk_path(package: str) -> str:
    result = adb_shell(f"pm path {shell_quote(package)}", capture=True)
    paths = []
    for line in result.stdout.splitlines():
        line = line.strip()
        if line.startswith("package:"):
            paths.append(line.split(":", 1)[1])
    base = next((p for p in paths if p.endswith("/base.apk")), paths[0] if paths else None)
    if not base:
        raise ExportError(f"Could not find APK path for package: {package}")
    return base


def shell_quote(value: str) -> str:
    return "'" + value.replace("'", "'\\''") + "'"


def pull_apk(package: str, work_dir: Path) -> Path:
    apk_dir = ensure_dir(work_dir / "apk")
    apk_path = get_base_apk_path(package)
    local_apk = apk_dir / "app.apk"
    info(f"Pulling APK: {apk_path}")
    adb(["pull", apk_path, str(local_apk)], capture=True)
    return local_apk


def decompile_apk(apk_path: Path, work_dir: Path) -> Optional[Path]:
    if not check_tool("apktool", required=False):
        warn("apktool is not installed. Decompiled XML resources will be skipped.")
        return fallback_extract_res(apk_path, work_dir / "apk_unpacked_res")
    out_dir = work_dir / "apktool_src"
    if out_dir.exists():
        shutil.rmtree(out_dir)
    info("Decompiling APK resources with apktool")
    run(["apktool", "d", "-f", str(apk_path), "-o", str(out_dir)], capture=True)
    return out_dir


def fallback_extract_res(apk_path: Path, out_dir: Path) -> Path:
    if out_dir.exists():
        shutil.rmtree(out_dir)
    ensure_dir(out_dir)
    try:
        with zipfile.ZipFile(apk_path, "r") as apk_zip:
            for member in apk_zip.namelist():
                if member.startswith("res/") or member == "resources.arsc":
                    apk_zip.extract(member, out_dir)
        warn(f"Raw APK res/ files extracted to {out_dir}. XML may still be binary encoded.")
        return out_dir
    except zipfile.BadZipFile as exc:
        raise ExportError(f"APK is not a valid zip: {apk_path}") from exc


def copytree_clean(src: Path, dst: Path) -> None:
    if not src.exists():
        return
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(src, dst)


def unique_dest(root: Path, source: Path) -> Path:
    safe_name = "__".join(source.parts[-3:])
    candidate = root / safe_name
    if not candidate.exists():
        return candidate
    stem = candidate.stem
    suffix = candidate.suffix
    for index in range(1, 10000):
        next_candidate = root / f"{stem}_{index}{suffix}"
        if not next_candidate.exists():
            return next_candidate
    raise ExportError(f"Could not choose unique filename for: {source}")


def iter_files(root: Path) -> Iterable[Path]:
    if not root.exists():
        return
    for path in root.rglob("*"):
        if path.is_file():
            yield path


def organize_decompiled_resources(src_dir: Optional[Path], out_dir: Path) -> Dict[str, Any]:
    report: Dict[str, Any] = {
        "apktool_src": str(src_dir) if src_dir else None,
        "copied": {},
        "warnings": [],
    }
    if not src_dir:
        return report
    res_dir = src_dir / "res"
    if not res_dir.exists():
        report["warnings"].append("apktool output has no res directory")
        return report
    if src_dir.name == "apk_unpacked_res":
        report["warnings"].append(
            "apktool is unavailable; XML files copied from raw APK may be binary encoded."
        )

    extracted_res = out_dir / "extracted_res"
    copytree_clean(res_dir, extracted_res)
    report["copied"]["extracted_res"] = str(extracted_res)

    images_dir = ensure_dir(out_dir / "images")
    vectors_dir = ensure_dir(out_dir / "vectors")
    animations_dir = ensure_dir(out_dir / "animations")
    fonts_dir = ensure_dir(out_dir / "fonts")

    raw_dir = res_dir / "raw"
    anim_dirs = [p for p in res_dir.iterdir() if p.is_dir() and p.name.startswith("anim")]
    drawable_dirs = [p for p in res_dir.iterdir() if p.is_dir() and p.name.startswith("drawable")]
    font_dir = res_dir / "font"
    values_dir = res_dir / "values"

    for path in iter_files(raw_dir):
        ext = path.suffix.lower()
        if ext in LOTTIE_EXTS:
            shutil.copy2(path, unique_dest(animations_dir, path))
    for anim_dir in anim_dirs:
        for path in iter_files(anim_dir):
            if path.suffix.lower() == ".xml":
                shutil.copy2(path, unique_dest(animations_dir, path))
    for drawable_dir in drawable_dirs:
        for path in iter_files(drawable_dir):
            ext = path.suffix.lower()
            if ext in IMAGE_EXTS:
                shutil.copy2(path, unique_dest(images_dir, path))
            elif ext == ".xml":
                svg_text = vector_xml_to_svg(path)
                if svg_text:
                    dest = unique_dest(vectors_dir, path.with_suffix(".svg"))
                    dest.write_text(svg_text, encoding="utf-8")
                else:
                    shutil.copy2(path, unique_dest(vectors_dir, path))
    for path in iter_files(font_dir):
        if path.suffix.lower() in FONT_EXTS:
            shutil.copy2(path, unique_dest(fonts_dir, path))

    if values_dir.exists():
        for name in ("styles.xml", "colors.xml", "dimens.xml"):
            source = values_dir / name
            if source.exists():
                shutil.copy2(source, out_dir / name)
        colors = parse_values_file(values_dir / "colors.xml")
        dimens = parse_values_file(values_dir / "dimens.xml")
        (out_dir / "colors.json").write_text(json.dumps(colors, indent=2), encoding="utf-8")
        (out_dir / "dimens.json").write_text(json.dumps(dimens, indent=2), encoding="utf-8")

    report["copied"].update(
        {
            "images": str(images_dir),
            "vectors": str(vectors_dir),
            "animations": str(animations_dir),
            "fonts": str(fonts_dir),
        }
    )
    return report


def android_attr(element: ET.Element, name: str) -> Optional[str]:
    return element.attrib.get(ANDROID_NS + name) or element.attrib.get(name)


def parse_dimension(value: Optional[str]) -> Optional[str]:
    if not value:
        return None
    match = re.match(r"([0-9.]+)(dp|sp|px)?", value)
    return match.group(1) if match else value


def vector_xml_to_svg(path: Path) -> Optional[str]:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        return None
    if root.tag.split("}")[-1] != "vector":
        return None

    width = parse_dimension(android_attr(root, "width")) or "24"
    height = parse_dimension(android_attr(root, "height")) or "24"
    viewport_width = android_attr(root, "viewportWidth") or width
    viewport_height = android_attr(root, "viewportHeight") or height
    body_parts = []
    for child in root:
        converted = vector_child_to_svg(child, 1)
        if converted:
            body_parts.append(converted)
    body = "\n".join(body_parts)
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" '
        f'viewBox="0 0 {viewport_width} {viewport_height}">\n{body}\n</svg>\n'
    )


def svg_color(value: Optional[str], default: str = "none") -> str:
    if not value:
        return default
    if value.startswith("@"):
        return "currentColor"
    return value


def vector_child_to_svg(element: ET.Element, level: int) -> str:
    indent = "  " * level
    local = element.tag.split("}")[-1]
    if local == "path":
        path_data = android_attr(element, "pathData")
        if not path_data:
            return ""
        fill = svg_color(android_attr(element, "fillColor"), "none")
        stroke = svg_color(android_attr(element, "strokeColor"), "none")
        stroke_width = android_attr(element, "strokeWidth") or "1"
        attrs = [f'd="{path_data}"', f'fill="{fill}"']
        if stroke != "none":
            attrs.extend([f'stroke="{stroke}"', f'stroke-width="{stroke_width}"'])
        fill_alpha = android_attr(element, "fillAlpha")
        stroke_alpha = android_attr(element, "strokeAlpha")
        if fill_alpha:
            attrs.append(f'fill-opacity="{fill_alpha}"')
        if stroke_alpha:
            attrs.append(f'stroke-opacity="{stroke_alpha}"')
        return f"{indent}<path {' '.join(attrs)} />"
    if local == "group":
        transforms = []
        rotation = android_attr(element, "rotation")
        pivot_x = android_attr(element, "pivotX") or "0"
        pivot_y = android_attr(element, "pivotY") or "0"
        scale_x = android_attr(element, "scaleX")
        scale_y = android_attr(element, "scaleY")
        translate_x = android_attr(element, "translateX")
        translate_y = android_attr(element, "translateY")
        if translate_x or translate_y:
            transforms.append(f"translate({translate_x or '0'} {translate_y or '0'})")
        if rotation:
            transforms.append(f"rotate({rotation} {pivot_x} {pivot_y})")
        if scale_x or scale_y:
            transforms.append(f"scale({scale_x or '1'} {scale_y or scale_x or '1'})")
        transform_attr = f' transform="{" ".join(transforms)}"' if transforms else ""
        children = "\n".join(vector_child_to_svg(child, level + 1) for child in element)
        return f"{indent}<g{transform_attr}>\n{children}\n{indent}</g>"
    return ""


def parse_values_file(path: Path) -> Dict[str, str]:
    if not path.exists():
        return {}
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError as exc:
        warn(f"Could not parse {path}: {exc}")
        return {}
    values: Dict[str, str] = {}
    for child in root:
        name = child.attrib.get("name")
        if name:
            values[name] = "".join(child.itertext()).strip()
    return values


def run_as_works(package: str) -> bool:
    result = adb(["shell", "run-as", package, "sh", "-c", "pwd"], capture=True, check=False)
    return result.returncode == 0 and bool(result.stdout.strip())


def pull_dynamic_resources(package: str, out_dir: Path, include_private: bool) -> Dict[str, Any]:
    report: Dict[str, Any] = {"attempted": include_private, "copied": 0, "error": None}
    if not include_private:
        report["error"] = "Skipped. Use --include-private-app-data only for owned/authorized apps."
        return report
    if not run_as_works(package):
        report["error"] = (
            "run-as failed. The app is probably not debuggable. With explicit authorization, "
            "a rooted device or app-provided export is required."
        )
        warn(report["error"])
        return report

    dynamic_dir = ensure_dir(out_dir / "dynamic_res")
    extracted = ensure_dir(dynamic_dir / "files")
    find_expr = " -o ".join(f"-name '*{ext}'" for ext in sorted(RESOURCE_EXTS))
    command = (
        f"cd /data/data/{shell_quote(package)} 2>/dev/null && "
        f"find files cache -type f \\( {find_expr} \\) -print 2>/dev/null"
    )
    info("Trying to list resource-like private app files with run-as")
    listed = subprocess.run(
        adb_command(["exec-out", "run-as", package, "sh", "-c", command]),
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if listed.returncode != 0:
        report["error"] = (listed.stderr or "run-as find failed").strip()
        warn(report["error"])
        return report
    rel_paths = [line.strip() for line in listed.stdout.splitlines() if line.strip()]
    if not rel_paths:
        report["error"] = "run-as worked, but no resource-like files were found in files/ or cache/."
        return report

    streamed = 0
    for rel_path in rel_paths:
        if rel_path.startswith("/") or ".." in Path(rel_path).parts:
            warn(f"Skipping unsafe dynamic path: {rel_path}")
            continue
        full_path = f"/data/data/{package}/{rel_path}"
        cat_command = f"cat {shell_quote(full_path)}"
        file_proc = subprocess.run(
            adb_command(["exec-out", "run-as", package, "sh", "-c", cat_command]),
            capture_output=True,
        )
        if file_proc.returncode != 0:
            warn(f"Could not read dynamic file: {rel_path}")
            continue
        local_path = extracted / rel_path
        ensure_dir(local_path.parent)
        local_path.write_bytes(file_proc.stdout)
        streamed += 1
    copied = copy_dynamic_resource_files(extracted, out_dir)
    report["streamed"] = streamed
    report["copied"] = copied
    return report


def copy_dynamic_resource_files(source_dir: Path, out_dir: Path) -> int:
    count = 0
    images_dir = ensure_dir(out_dir / "images")
    animations_dir = ensure_dir(out_dir / "animations")
    fonts_dir = ensure_dir(out_dir / "fonts")
    dynamic_copy_dir = ensure_dir(out_dir / "dynamic_res" / "resource_files")
    for path in iter_files(source_dir):
        ext = path.suffix.lower()
        if ext not in RESOURCE_EXTS:
            continue
        shutil.copy2(path, unique_dest(dynamic_copy_dir, path))
        if ext in IMAGE_EXTS:
            shutil.copy2(path, unique_dest(images_dir, path))
        elif ext in LOTTIE_EXTS:
            shutil.copy2(path, unique_dest(animations_dir, path))
        elif ext in FONT_EXTS:
            shutil.copy2(path, unique_dest(fonts_dir, path))
        count += 1
    return count


def dump_ui_hierarchy(out_dir: Path) -> Optional[Dict[str, Any]]:
    remote = "/sdcard/ui.xml"
    local_xml = out_dir / "ui.xml"
    info("Dumping UI hierarchy with uiautomator")
    result = adb_shell(f"uiautomator dump {remote}", capture=True, check=False)
    if result.returncode != 0:
        warn((result.stderr or result.stdout or "uiautomator dump failed").strip())
        return None
    adb(["pull", remote, str(local_xml)], capture=True)
    adb_shell(f"rm {remote}", capture=True, check=False)
    hierarchy = ui_xml_to_json(local_xml)
    (out_dir / "ui_hierarchy.json").write_text(json.dumps(hierarchy, indent=2), encoding="utf-8")
    return hierarchy


def parse_bounds(value: str) -> Dict[str, int]:
    match = re.match(r"\[(\-?\d+),(\-?\d+)\]\[(\-?\d+),(\-?\d+)\]", value or "")
    if not match:
        return {"x": 0, "y": 0, "width": 0, "height": 0, "right": 0, "bottom": 0}
    x1, y1, x2, y2 = map(int, match.groups())
    return {"x": x1, "y": y1, "width": x2 - x1, "height": y2 - y1, "right": x2, "bottom": y2}


def ui_xml_to_json(path: Path) -> Dict[str, Any]:
    root = ET.parse(path).getroot()
    return {"source": str(path), "root": ui_element_to_json(root)}


def ui_element_to_json(element: ET.Element) -> Dict[str, Any]:
    attrs = dict(element.attrib)
    node: Dict[str, Any] = {
        "tag": element.tag,
        "class": attrs.get("class", ""),
        "resource_id": attrs.get("resource-id", ""),
        "text": attrs.get("text", ""),
        "content_desc": attrs.get("content-desc", ""),
        "package": attrs.get("package", ""),
        "bounds": parse_bounds(attrs.get("bounds", "")),
        "clickable": attrs.get("clickable") == "true",
        "enabled": attrs.get("enabled") != "false",
        "selected": attrs.get("selected") == "true",
        "focused": attrs.get("focused") == "true",
        "scrollable": attrs.get("scrollable") == "true",
        "children": [ui_element_to_json(child) for child in element],
    }
    return node


def record_screen(out_dir: Path, seconds: int, size: Optional[str]) -> Optional[Path]:
    if seconds <= 0:
        return None
    remote = "/sdcard/animation.mp4"
    local = out_dir / "animation.mp4"
    args = ["shell", "screenrecord", "--time-limit", str(seconds)]
    if size:
        args.extend(["--size", size])
    args.append(remote)
    info(f"Recording screen for {seconds} seconds")
    result = adb(args, capture=True, check=False, timeout=seconds + 15)
    if result.returncode != 0:
        warn((result.stderr or result.stdout or "screenrecord failed").strip())
        return None
    adb(["pull", remote, str(local)], capture=True)
    adb_shell(f"rm {remote}", capture=True, check=False)
    return local


def dump_filtered_logcat(log_path: Path) -> Path:
    result = adb(["logcat", "-d", "-v", "time"], capture=True, check=False)
    with log_path.open("w", encoding="utf-8", errors="replace") as log_file:
        for line in (result.stdout or "").splitlines():
            if ANIMATION_LOG_RE.search(line):
                log_file.write(line + "\n")
    return log_path


def record_screen_with_logs(
    out_dir: Path,
    record_seconds: int,
    size: Optional[str],
    log_seconds: int,
) -> Tuple[Optional[Path], Path]:
    remote = "/sdcard/animation.mp4"
    local = out_dir / "animation.mp4"
    log_path = out_dir / "animation_logs.txt"
    capture_seconds = max(record_seconds, log_seconds)

    info("Enabling debug.renderanimator and clearing logcat")
    adb_shell("setprop debug.renderanimator 1", capture=True, check=False)
    adb(["logcat", "-c"], capture=True, check=False)

    args = ["shell", "screenrecord", "--time-limit", str(record_seconds)]
    if size:
        args.extend(["--size", size])
    args.append(remote)

    info(
        f"Recording screen for {record_seconds} seconds while capturing logs for "
        f"{capture_seconds} seconds"
    )
    proc = subprocess.Popen(
        adb_command(args),
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    time.sleep(capture_seconds)
    try:
        proc.wait(timeout=10)
    except subprocess.TimeoutExpired:
        proc.terminate()
        try:
            proc.wait(timeout=3)
        except subprocess.TimeoutExpired:
            proc.kill()

    if proc.returncode not in (0, None):
        output = ""
        if proc.stdout:
            output = proc.stdout.read().strip()
        warn(output or "screenrecord failed")
        recording_path = None
    else:
        adb(["pull", remote, str(local)], capture=True, check=False)
        adb_shell(f"rm {remote}", capture=True, check=False)
        recording_path = local

    dump_filtered_logcat(log_path)
    return recording_path, log_path


def capture_frames(out_dir: Path, frame_count: int, interval_ms: int) -> List[str]:
    frames_dir = ensure_dir(out_dir / "frames")
    paths: List[str] = []
    if frame_count <= 0:
        return paths
    info(f"Capturing {frame_count} screenshots every {interval_ms} ms")
    for index in range(frame_count):
        remote = f"/sdcard/frame_{index:05d}.png"
        local = frames_dir / f"frame_{index:05d}.png"
        adb(["shell", "screencap", "-p", remote], capture=True, check=False)
        adb(["pull", remote, str(local)], capture=True, check=False)
        adb_shell(f"rm {remote}", capture=True, check=False)
        paths.append(str(local))
        if index < frame_count - 1:
            time.sleep(max(interval_ms, 1) / 1000.0)
    return paths


def capture_animation_logs(out_dir: Path, seconds: int) -> Path:
    log_path = out_dir / "animation_logs.txt"
    info("Enabling debug.renderanimator and clearing logcat")
    adb_shell("setprop debug.renderanimator 1", capture=True, check=False)
    adb(["logcat", "-c"], capture=True, check=False)
    info(f"Capturing animation-related logcat lines for {seconds} seconds")
    time.sleep(seconds)
    return dump_filtered_logcat(log_path)


def parse_animation_logs(log_path: Path, out_dir: Path) -> List[Dict[str, Any]]:
    entries: List[Dict[str, Any]] = []
    if not log_path.exists():
        (out_dir / "animation_params.json").write_text("[]", encoding="utf-8")
        return entries
    for line_number, line in enumerate(log_path.read_text(encoding="utf-8", errors="replace").splitlines(), 1):
        if not ANIMATION_LOG_RE.search(line):
            continue
        duration_match = DURATION_RE.search(line)
        interpolators = INTERPOLATOR_RE.findall(line)
        properties = sorted(set(PROPERTY_RE.findall(line)))
        entries.append(
            {
                "source": str(log_path),
                "line": line_number,
                "duration_ms": int(duration_match.group(1)) if duration_match else None,
                "properties": properties,
                "interpolator_guess": interpolators[0] if interpolators else None,
                "target_guess": guess_log_target(line),
                "raw": line,
            }
        )
    (out_dir / "animation_params.json").write_text(json.dumps(entries, indent=2), encoding="utf-8")
    return entries


def guess_log_target(line: str) -> Optional[str]:
    for pattern in (r"target=([^,\s]+)", r"view=([^,\s]+)", r"mView=([^,\s]+)"):
        match = re.search(pattern, line)
        if match:
            return match.group(1)
    return None


def generate_code(out_dir: Path, hierarchy: Optional[Dict[str, Any]], target: str) -> Optional[Path]:
    if target == "none":
        return None
    if not hierarchy:
        warn("Skipping code generation because UI hierarchy is unavailable.")
        return None
    if target == "flutter":
        return generate_flutter(out_dir, hierarchy)
    if target == "compose":
        return generate_compose(out_dir, hierarchy)
    raise ExportError(f"Unknown code generation target: {target}")


def flatten_ui_nodes(node: Dict[str, Any]) -> List[Dict[str, Any]]:
    result = [node]
    for child in node.get("children", []):
        result.extend(flatten_ui_nodes(child))
    return result


def infer_canvas_size(nodes: List[Dict[str, Any]]) -> Tuple[int, int]:
    max_right = 0
    max_bottom = 0
    for node in nodes:
        bounds = node.get("bounds", {})
        max_right = max(max_right, int(bounds.get("right") or 0))
        max_bottom = max(max_bottom, int(bounds.get("bottom") or 0))
    return max(max_right, 390), max(max_bottom, 844)


def escape_dart(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "\\'")


def generate_flutter(out_dir: Path, hierarchy: Dict[str, Any]) -> Path:
    project_dir = ensure_dir(out_dir / "generated" / "flutter_ui" / "lib")
    nodes = flatten_ui_nodes(hierarchy["root"])
    width, height = infer_canvas_size(nodes)
    widgets: List[str] = []
    for node in nodes:
        bounds = node.get("bounds", {})
        w = max(int(bounds.get("width", 0)), 0)
        h = max(int(bounds.get("height", 0)), 0)
        x = max(int(bounds.get("x", 0)), 0)
        y = max(int(bounds.get("y", 0)), 0)
        if w <= 0 or h <= 0 or (x == 0 and y == 0 and w == width and h == height):
            continue
        label = node.get("text") or node.get("content_desc") or node.get("class", "").split(".")[-1]
        label = escape_dart(label[:80])
        if node.get("text"):
            child = (
                f"Text('{label}', overflow: TextOverflow.ellipsis, "
                "style: const TextStyle(fontSize: 12, color: Color(0xff111111)))"
            )
        else:
            child = (
                "Container(decoration: BoxDecoration("
                "border: Border.all(color: const Color(0x33999999), width: 0.5)))"
            )
        widgets.append(
            "          Positioned("
            f"left: {x}.0, top: {y}.0, width: {w}.0, height: {h}.0, "
            f"child: {child}),"
        )
    main_dart = f"""import 'package:flutter/material.dart';

void main() => runApp(const ExportedUiApp());

class ExportedUiApp extends StatelessWidget {{
  const ExportedUiApp({{super.key}});

  @override
  Widget build(BuildContext context) {{
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        body: Center(
          child: FittedBox(
            fit: BoxFit.contain,
            child: SizedBox(
              width: {width}.0,
              height: {height}.0,
              child: Stack(
                children: <Widget>[
{os.linesep.join(widgets)}
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }}
}}
"""
    path = project_dir / "main.dart"
    path.write_text(main_dart, encoding="utf-8")
    return path


def escape_kotlin(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"')


def generate_compose(out_dir: Path, hierarchy: Dict[str, Any]) -> Path:
    project_dir = ensure_dir(out_dir / "generated" / "compose_ui")
    nodes = flatten_ui_nodes(hierarchy["root"])
    width, height = infer_canvas_size(nodes)
    lines: List[str] = []
    for node in nodes:
        bounds = node.get("bounds", {})
        w = max(int(bounds.get("width", 0)), 0)
        h = max(int(bounds.get("height", 0)), 0)
        x = max(int(bounds.get("x", 0)), 0)
        y = max(int(bounds.get("y", 0)), 0)
        if w <= 0 or h <= 0 or (x == 0 and y == 0 and w == width and h == height):
            continue
        label = escape_kotlin((node.get("text") or node.get("content_desc") or "").strip()[:80])
        if label:
            lines.append(
                f'        Text("{label}", modifier = Modifier.absoluteOffset({x}.dp, {y}.dp)'
                f".size({w}.dp, {h}.dp), fontSize = 12.sp)"
            )
        else:
            lines.append(
                f"        Box(modifier = Modifier.absoluteOffset({x}.dp, {y}.dp)"
                f".size({w}.dp, {h}.dp).border(0.5.dp, Color(0x33999999)))"
            )
    kotlin = f"""package com.example.exportedui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExportedUiScreen() {{
    Box(modifier = Modifier.size({width}.dp, {height}.dp)) {{
{os.linesep.join(lines)}
    }}
}}
"""
    path = project_dir / "ExportedUiScreen.kt"
    path.write_text(kotlin, encoding="utf-8")
    return path


def build_report(out_dir: Path, data: Dict[str, Any]) -> Path:
    report_path = out_dir / "report.json"
    report_path.write_text(json.dumps(data, indent=2), encoding="utf-8")
    return report_path


def parse_args(argv: Optional[Sequence[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export Android UI resources for owned or authorized apps."
    )
    parser.add_argument("--package", help="Exact package name, e.g. com.example.app")
    parser.add_argument("--package-filter", help="Substring used to find one installed package")
    parser.add_argument("--device", help="ADB device serial from `adb devices`, e.g. emulator-5554")
    parser.add_argument("--out", default="ui_export", help="Output directory")
    parser.add_argument("--skip-apk", action="store_true", help="Skip APK pull/decompile")
    parser.add_argument("--skip-ui", action="store_true", help="Skip uiautomator hierarchy dump")
    parser.add_argument("--include-private-app-data", action="store_true", help="Try run-as for files/cache")
    parser.add_argument("--record-seconds", type=int, default=0, help="Screenrecord duration; 0 disables")
    parser.add_argument("--record-size", help="Optional screenrecord size, e.g. 1080x1920")
    parser.add_argument("--frame-count", type=int, default=0, help="Optional slow screencap frame count")
    parser.add_argument("--frame-interval-ms", type=int, default=100, help="Delay between screencaps")
    parser.add_argument("--log-seconds", type=int, default=0, help="Capture filtered animation logs")
    parser.add_argument("--generate", choices=("none", "flutter", "compose"), default="none")
    return parser.parse_args(argv)


def main(argv: Optional[Sequence[str]] = None) -> int:
    args = parse_args(argv)
    try:
        serial = check_device(args.device)
        set_adb_serial(serial)
        package = resolve_package(args.package, args.package_filter)
        out_dir = ensure_dir(Path(args.out).resolve())
        work_dir = ensure_dir(out_dir / "_work")
        report: Dict[str, Any] = {"package": package, "out_dir": str(out_dir)}

        src_dir: Optional[Path] = None
        if not args.skip_apk:
            apk_path = pull_apk(package, work_dir)
            report["apk"] = str(apk_path)
            src_dir = decompile_apk(apk_path, work_dir)
            report["resources"] = organize_decompiled_resources(src_dir, out_dir)

        report["dynamic_resources"] = pull_dynamic_resources(
            package, out_dir, args.include_private_app_data
        )

        hierarchy: Optional[Dict[str, Any]] = None
        if not args.skip_ui:
            hierarchy = dump_ui_hierarchy(out_dir)
            report["ui_hierarchy"] = str(out_dir / "ui_hierarchy.json") if hierarchy else None

        if args.record_seconds and args.log_seconds:
            recording_path, log_path = record_screen_with_logs(
                out_dir, args.record_seconds, args.record_size, args.log_seconds
            )
            report["screenrecord"] = str(recording_path) if recording_path else None
            report["animation_logs"] = str(log_path)
            report["animation_params_count"] = len(parse_animation_logs(log_path, out_dir))
        elif args.record_seconds:
            report["screenrecord"] = str(record_screen(out_dir, args.record_seconds, args.record_size))
        if args.frame_count:
            report["frames"] = capture_frames(out_dir, args.frame_count, args.frame_interval_ms)
        if args.log_seconds and not args.record_seconds:
            log_path = capture_animation_logs(out_dir, args.log_seconds)
            report["animation_logs"] = str(log_path)
            report["animation_params_count"] = len(parse_animation_logs(log_path, out_dir))
        if not args.log_seconds:
            parse_animation_logs(out_dir / "animation_logs.txt", out_dir)

        generated = generate_code(out_dir, hierarchy, args.generate)
        report["generated_code"] = str(generated) if generated else None
        report_path = build_report(out_dir, report)
        info(f"Done. Report: {report_path}")
        return 0
    except ExportError as exc:
        print(f"[error] {exc}", file=sys.stderr)
        return 2
    except KeyboardInterrupt:
        print("\n[error] Interrupted", file=sys.stderr)
        return 130


if __name__ == "__main__":
    raise SystemExit(main())
