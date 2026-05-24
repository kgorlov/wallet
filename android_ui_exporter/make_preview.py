#!/usr/bin/env python3
"""
Create an exact visual verification preview from exported Android UI data.

The preview uses a real frame from animation.mp4 as the visual reference and
adds an optional UIAutomator overlay. This is meant for checking captured UI
structure, not for producing an operable app clone.
"""

from __future__ import annotations

import argparse
import html
import json
import shutil
import subprocess
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Tuple


def flatten(node: Dict[str, Any], depth: int = 0) -> Iterable[Tuple[Dict[str, Any], int]]:
    yield node, depth
    for child in node.get("children", []):
        yield from flatten(child, depth + 1)


def as_int(value: Any) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return 0


def infer_canvas(nodes: List[Tuple[Dict[str, Any], int]]) -> Tuple[int, int]:
    right = 0
    bottom = 0
    for node, _depth in nodes:
        bounds = node.get("bounds", {})
        right = max(right, as_int(bounds.get("right")))
        bottom = max(bottom, as_int(bounds.get("bottom")))
    return max(right, 390), max(bottom, 844)


def extract_reference_frame(out_dir: Path, preview_dir: Path) -> Optional[Path]:
    existing = preview_dir / "frame0.png"
    if existing.exists():
        return existing

    video = out_dir / "animation.mp4"
    if not video.exists() or not shutil.which("ffmpeg"):
        return None

    result = subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-i",
            str(video),
            "-frames:v",
            "1",
            "-update",
            "1",
            str(existing),
        ],
        capture_output=True,
        text=True,
    )
    return existing if result.returncode == 0 and existing.exists() else None


def node_text(node: Dict[str, Any]) -> str:
    return (node.get("text") or node.get("content_desc") or "").strip()


def short_class(node: Dict[str, Any]) -> str:
    class_name = node.get("class", "")
    return class_name.rsplit(".", 1)[-1] if class_name else "View"


def significant_nodes(
    nodes: List[Tuple[Dict[str, Any], int]], width: int, height: int
) -> List[Tuple[Dict[str, Any], int]]:
    picked: List[Tuple[Dict[str, Any], int]] = []
    seen = set()
    for node, depth in nodes:
        bounds = node.get("bounds", {})
        x = as_int(bounds.get("x"))
        y = as_int(bounds.get("y"))
        w = as_int(bounds.get("width"))
        h = as_int(bounds.get("height"))
        if w <= 0 or h <= 0:
            continue
        if x == 0 and y == 0 and w == width and h == height:
            continue
        if not (node_text(node) or node.get("clickable") or node.get("scrollable")):
            continue
        key = (x, y, w, h, node.get("resource_id", ""), node_text(node), node.get("class", ""))
        if key in seen:
            continue
        seen.add(key)
        picked.append((node, depth))
    return picked


def pct(value: Any, total: int) -> str:
    total = max(total, 1)
    return f"{as_int(value) / total * 100:.6f}%"


def overlay_div(node: Dict[str, Any], index: int, width: int, height: int) -> str:
    bounds = node.get("bounds", {})
    label = node_text(node)
    resource_id = node.get("resource_id", "")
    kind = "text" if label else "target"
    if node.get("scrollable"):
        kind = "scroll"
    title = html.escape(
        f"{label or resource_id or short_class(node)} | {short_class(node)} | "
        f"{bounds.get('x')},{bounds.get('y')} {bounds.get('width')}x{bounds.get('height')}"
    )
    style = (
        f"left:{pct(bounds.get('x'), width)};"
        f"top:{pct(bounds.get('y'), height)};"
        f"width:{pct(bounds.get('width'), width)};"
        f"height:{pct(bounds.get('height'), height)};"
        f"z-index:{index + 3};"
    )
    content = ""
    if label:
        content = f'<span>{html.escape(label[:80])}</span>'
    elif resource_id:
        content = f'<span>{html.escape(resource_id[:40])}</span>'
    return f'<div class="hit {kind}" style="{style}" title="{title}">{content}</div>'


def list_item(node: Dict[str, Any], depth: int) -> str:
    bounds = node.get("bounds", {})
    label = node_text(node) or node.get("resource_id", "") or short_class(node)
    meta = (
        f"{short_class(node)} | depth {depth} | "
        f"{bounds.get('x')},{bounds.get('y')} {bounds.get('width')}x{bounds.get('height')}"
    )
    return f"<li><b>{html.escape(label)}</b><span>{html.escape(meta)}</span></li>"


def render(out_dir: Path) -> Path:
    hierarchy_path = out_dir / "ui_hierarchy.json"
    if not hierarchy_path.exists():
        raise SystemExit(f"Missing {hierarchy_path}")

    preview_dir = out_dir / "preview"
    preview_dir.mkdir(parents=True, exist_ok=True)
    frame = extract_reference_frame(out_dir, preview_dir)

    hierarchy = json.loads(hierarchy_path.read_text(encoding="utf-8"))
    nodes = list(flatten(hierarchy["root"]))
    width, height = infer_canvas(nodes)
    hits = significant_nodes(nodes, width, height)
    text_hits = [(node, depth) for node, depth in hits if node_text(node)]

    overlay_html = "\n".join(
        overlay_div(node, index, width, height) for index, (node, _depth) in enumerate(hits)
    )
    list_html = "\n".join(list_item(node, depth) for node, depth in hits)

    if frame:
        visual = f'<img class="shot" src="{html.escape(frame.name)}" alt="Captured screen">'
        reference_note = "real captured frame"
    else:
        visual = '<div class="fallback">No frame found. Re-run export with --record-seconds.</div>'
        reference_note = "hierarchy only"

    if frame:
        (preview_dir / "phone.html").write_text(
            f"""<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Captured Phone UI</title>
  <style>
    html, body {{
      margin: 0;
      min-height: 100%;
      background: #101114;
    }}
    body {{
      display: grid;
      place-items: start center;
      padding: 18px;
    }}
    img {{
      display: block;
      width: min(100%, 460px);
      height: auto;
      border-radius: 30px;
      box-shadow: 0 24px 80px rgba(0, 0, 0, .55);
    }}
  </style>
</head>
<body>
  <img src="{html.escape(frame.name)}" alt="Captured phone UI">
</body>
</html>
""",
            encoding="utf-8",
        )

    output = preview_dir / "index.html"
    output.write_text(
        f"""<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Captured UI Preview</title>
  <style>
    * {{ box-sizing: border-box; }}
    body {{
      margin: 0;
      min-height: 100vh;
      background: #101114;
      color: #f4f4f5;
      font: 14px/1.35 Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }}
    .layout {{
      min-height: 100vh;
      display: grid;
      grid-template-columns: minmax(360px, 1fr) 390px;
      gap: 18px;
      padding: 18px;
    }}
    .stage {{
      min-width: 0;
      display: flex;
      justify-content: center;
      align-items: flex-start;
      overflow: auto;
    }}
    .phone {{
      position: relative;
      width: min(100%, 460px);
      background: #18191d;
      border: 1px solid #2b2d33;
      border-radius: 30px;
      box-shadow: 0 24px 80px rgba(0, 0, 0, .55);
      overflow: hidden;
    }}
    .shot {{
      display: block;
      width: 100%;
      height: auto;
    }}
    .overlay {{
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      aspect-ratio: {width} / {height};
      pointer-events: none;
      opacity: 0;
      transition: opacity .12s ease;
    }}
    body.show-overlay .overlay {{ opacity: 1; }}
    .hit {{
      position: absolute;
      border: 2px solid rgba(73, 255, 142, .9);
      background: rgba(73, 255, 142, .12);
      color: #111827;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 2px;
      pointer-events: auto;
    }}
    .hit.text {{
      border-color: rgba(59, 130, 246, .95);
      background: rgba(59, 130, 246, .15);
      color: #eff6ff;
    }}
    .hit.scroll {{
      border-style: dashed;
      border-color: rgba(250, 204, 21, .95);
      background: rgba(250, 204, 21, .08);
    }}
    .hit span {{
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 10px;
      font-weight: 700;
      text-shadow: 0 1px 2px rgba(0, 0, 0, .8);
    }}
    .fallback {{
      width: 100%;
      aspect-ratio: {width} / {height};
      display: grid;
      place-items: center;
      color: #a1a1aa;
      padding: 24px;
      text-align: center;
    }}
    .panel {{
      min-width: 0;
      height: calc(100vh - 36px);
      overflow: auto;
      background: #18191d;
      border: 1px solid #2b2d33;
      border-radius: 10px;
      padding: 14px;
    }}
    h1 {{
      margin: 0 0 12px;
      font-size: 17px;
    }}
    .controls {{
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 14px;
      color: #d4d4d8;
    }}
    .controls input {{ width: 18px; height: 18px; }}
    .metrics {{
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      margin-bottom: 14px;
    }}
    .metric {{
      background: #202126;
      border: 1px solid #30323a;
      border-radius: 8px;
      padding: 9px;
    }}
    .metric b {{
      display: block;
      font-size: 18px;
      line-height: 1.1;
    }}
    .metric span {{
      display: block;
      color: #a1a1aa;
      font-size: 12px;
      margin-top: 4px;
    }}
    .note {{
      color: #a1a1aa;
      border-top: 1px solid #30323a;
      border-bottom: 1px solid #30323a;
      padding: 10px 0;
      margin-bottom: 14px;
    }}
    ol {{
      list-style: none;
      margin: 0;
      padding: 0;
      display: grid;
      gap: 7px;
    }}
    li {{
      padding: 0 0 7px;
      border-bottom: 1px solid #2b2d33;
    }}
    li b {{
      display: block;
      color: #f4f4f5;
      font-size: 13px;
      overflow-wrap: anywhere;
    }}
    li span {{
      display: block;
      color: #a1a1aa;
      font-size: 11px;
      margin-top: 2px;
    }}
    @media (max-width: 900px) {{
      .layout {{ grid-template-columns: 1fr; padding: 10px; }}
      .panel {{ height: auto; }}
      .phone {{ width: min(100%, 460px); }}
    }}
  </style>
</head>
<body>
  <main class="layout">
    <section class="stage">
      <div class="phone">
        {visual}
        <div class="overlay">
{overlay_html}
        </div>
      </div>
    </section>
    <aside class="panel">
      <h1>Captured UI preview</h1>
      <label class="controls">
        <input id="overlayToggle" type="checkbox">
        <span>Show UIAutomator overlay</span>
      </label>
      <div class="metrics">
        <div class="metric"><b>{width}x{height}</b><span>hierarchy canvas</span></div>
        <div class="metric"><b>{reference_note}</b><span>visual source</span></div>
        <div class="metric"><b>{len(hits)}</b><span>interactive/text nodes</span></div>
        <div class="metric"><b>{len(text_hits)}</b><span>text nodes</span></div>
      </div>
      <div class="note">The visible phone is a captured frame. The optional overlay shows parsed hierarchy targets from ui_hierarchy.json.</div>
      <ol>
{list_html}
      </ol>
    </aside>
  </main>
  <script>
    const toggle = document.getElementById('overlayToggle');
    toggle.addEventListener('change', () => {{
      document.body.classList.toggle('show-overlay', toggle.checked);
    }});
  </script>
</body>
</html>
""",
        encoding="utf-8",
    )
    return output


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate captured UI preview.")
    parser.add_argument("out_dir", nargs="?", default="app_ui_export")
    args = parser.parse_args()
    print(render(Path(args.out_dir)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
