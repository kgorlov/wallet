#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path
from xml.etree import ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
CAPTURE = ROOT / "phone_capture"
PWA = ROOT / "trust-wallet-pwa"

PAGES = {
    "markets": "live_ref2_markets.xml",
    "swap": "live_ref2_swap.xml",
    "discover": "live_ref2_discover_fixed.xml",
    "history": "live_ref2_assets_history.xml",
    "manage": "live_ref2_assets_manage.xml",
    "nft": "live_ref2_nft_retry.xml",
    "perps": "trustvisual_final2_perps.xml",
}


def parse_bounds(value: str) -> dict[str, int]:
    match = re.match(r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]", value or "")
    if not match:
        return {"x": 0, "y": 0, "w": 0, "h": 0, "r": 0, "b": 0}
    x1, y1, x2, y2 = map(int, match.groups())
    return {"x": x1, "y": y1, "w": x2 - x1, "h": y2 - y1, "r": x2, "b": y2}


def short_resource(value: str) -> str:
    return value.rsplit("/", 1)[-1] if value else ""


def parse_page(path: Path) -> list[dict[str, object]]:
    root = ET.parse(path).getroot()
    items: list[dict[str, object]] = []
    seen: set[tuple[object, ...]] = set()

    for node in root.iter("node"):
        b = parse_bounds(node.attrib.get("bounds", ""))
        if b["w"] <= 0 or b["h"] <= 0:
            continue
        if b["x"] == 0 and b["y"] == 0 and b["w"] >= 1080 and b["h"] >= 2168:
            continue

        text = node.attrib.get("text", "").strip()
        desc = node.attrib.get("content-desc", "").strip()
        rid = short_resource(node.attrib.get("resource-id", "").strip())
        cls = node.attrib.get("class", "").rsplit(".", 1)[-1]
        clickable = node.attrib.get("clickable") == "true"
        scrollable = node.attrib.get("scrollable") == "true"
        selected = node.attrib.get("selected") == "true"
        label = text or desc

        if not (label or rid or clickable or scrollable):
            continue

        key = (b["x"], b["y"], b["w"], b["h"], label, rid, clickable, scrollable)
        if key in seen:
            continue
        seen.add(key)

        role = "text"
        if clickable:
            role = "button"
        elif scrollable:
            role = "scroll"
        elif "TextView" not in cls and not label:
            role = "shape"

        items.append(
            {
                "x": b["x"],
                "y": b["y"],
                "w": b["w"],
                "h": b["h"],
                "text": text,
                "desc": desc,
                "rid": rid,
                "cls": cls,
                "role": role,
                "clickable": clickable,
                "scrollable": scrollable,
                "selected": selected,
            }
        )
    return items


def main() -> int:
    data = {}
    for page, filename in PAGES.items():
        path = CAPTURE / filename
        if not path.exists():
            raise SystemExit(f"missing {path}")
        data[page] = parse_page(path)

    output = PWA / "parsed-pages.js"
    output.write_text(
        "window.PARSED_PAGES = "
        + json.dumps(data, ensure_ascii=False, separators=(",", ":"))
        + ";\n",
        encoding="utf-8",
    )
    print(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
