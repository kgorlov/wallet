#!/usr/bin/env python3
"""
Generate a no-screenshot UI reconstruction from ui_hierarchy.json.

This renderer uses only parsed hierarchy data: bounds, labels, content
descriptions/resource ids, and a small set of layout heuristics. It does not use
animation.mp4, frame captures, or screenshot backgrounds.
"""

from __future__ import annotations

import argparse
import html
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Tuple


@dataclass
class UiNode:
    class_name: str
    resource_id: str
    text: str
    content_desc: str
    bounds: Dict[str, int]
    clickable: bool
    scrollable: bool
    depth: int

    @property
    def label(self) -> str:
        return (self.text or self.content_desc or "").strip()

    @property
    def x(self) -> int:
        return self.bounds.get("x", 0)

    @property
    def y(self) -> int:
        return self.bounds.get("y", 0)

    @property
    def w(self) -> int:
        return self.bounds.get("width", 0)

    @property
    def h(self) -> int:
        return self.bounds.get("height", 0)

    @property
    def right(self) -> int:
        return self.bounds.get("right", self.x + self.w)

    @property
    def bottom(self) -> int:
        return self.bounds.get("bottom", self.y + self.h)


def parse_bounds(bounds: Dict[str, Any]) -> Dict[str, int]:
    return {
        "x": int(bounds.get("x") or 0),
        "y": int(bounds.get("y") or 0),
        "width": int(bounds.get("width") or 0),
        "height": int(bounds.get("height") or 0),
        "right": int(bounds.get("right") or 0),
        "bottom": int(bounds.get("bottom") or 0),
    }


def walk(node: Dict[str, Any], depth: int = 0) -> Iterable[UiNode]:
    yield UiNode(
        class_name=node.get("class", ""),
        resource_id=node.get("resource_id", ""),
        text=node.get("text", ""),
        content_desc=node.get("content_desc", ""),
        bounds=parse_bounds(node.get("bounds", {})),
        clickable=bool(node.get("clickable")),
        scrollable=bool(node.get("scrollable")),
        depth=depth,
    )
    for child in node.get("children", []):
        yield from walk(child, depth + 1)


def esc(value: str) -> str:
    return html.escape(value, quote=True)


def pct(value: int, total: int) -> str:
    return f"{value / max(total, 1) * 100:.6f}%"


def style_from_bounds(node: UiNode, width: int, height: int) -> str:
    return (
        f"left:{pct(node.x, width)};"
        f"top:{pct(node.y, height)};"
        f"width:{pct(node.w, width)};"
        f"height:{pct(node.h, height)};"
    )


def box_style(x: int, y: int, w: int, h: int, width: int, height: int) -> str:
    return (
        f"left:{pct(x, width)};"
        f"top:{pct(y, height)};"
        f"width:{pct(w, width)};"
        f"height:{pct(h, height)};"
    )


def find_by_resource(nodes: List[UiNode], resource_id: str) -> Optional[UiNode]:
    return next((node for node in nodes if node.resource_id == resource_id and node.w > 0 and node.h > 0), None)


def find_text(nodes: List[UiNode], text: str) -> Optional[UiNode]:
    return next((node for node in nodes if node.label == text and node.w > 0 and node.h > 0), None)


def texts_between(nodes: List[UiNode], y1: int, y2: int) -> List[UiNode]:
    return sorted(
        [node for node in nodes if node.label and node.w > 0 and node.h > 0 and y1 <= node.y <= y2],
        key=lambda node: (node.y, node.x),
    )


def canvas_size(nodes: List[UiNode]) -> Tuple[int, int]:
    width = max((node.right for node in nodes), default=390)
    height = max((node.bottom for node in nodes), default=844)
    return max(width, 390), max(height, 844)


def div(cls: str, style: str = "", body: str = "", attrs: str = "") -> str:
    style_attr = f' style="{style}"' if style else ""
    attrs = f" {attrs}" if attrs else ""
    return f'<div class="{cls}"{style_attr}{attrs}>{body}</div>'


def text_block(node: UiNode, cls: str, width: int, height: int) -> str:
    return div(cls, style_from_bounds(node, width, height), esc(node.label))


def icon_for_action(label: str) -> str:
    mapping = {
        "Отправить": "↗",
        "Получить": "↓",
        "Обмен": "⇄",
        "Покупка": "+",
    }
    return mapping.get(label, "•")


def render_support_layers(nodes: List[UiNode], width: int, height: int) -> str:
    parts: List[str] = []
    first_scroll = next(
        (
            node
            for node in nodes
            if node.scrollable and node.w >= width * 0.7 and node.h > 500 and node.y > 900
        ),
        None,
    )
    if first_scroll:
        parts.append(div("content-panel", box_style(0, first_scroll.y, width, height - first_scroll.y, width, height)))

    banner = find_by_resource(nodes, "bannerSlide")
    if banner:
        dots_y = min(banner.bottom + 58, height - 180)
        parts.append(
            div(
                "carousel-dots",
                box_style(width // 2 - 40, dots_y, 80, 18, width, height),
                '<i></i><i class="active"></i>',
            )
        )

    active_tab = find_by_resource(nodes, "Криптовалюта")
    if active_tab:
        parts.append(div("tab-divider", box_style(0, active_tab.bottom, width, 3, width, height)))

    parts.append(div("bottom-fade", box_style(0, max(height - 240, 0), width, 240, width, height)))
    return "\n".join(parts)


def render_top_bar(nodes: List[UiNode], width: int, height: int) -> str:
    settings = find_by_resource(nodes, "topBarSettingsIcon")
    qr = find_by_resource(nodes, "topBarQrIcon")
    search_text = find_text(nodes, "Поиск")
    search_box = next(
        (
            node
            for node in nodes
            if node.clickable and node.w > 500 and 70 <= node.y <= 160 and node.x > 100
        ),
        None,
    )
    parts: List[str] = []
    if settings:
        parts.append(div("top-icon settings", style_from_bounds(settings, width, height), "⚙"))
    if search_box:
        label = esc(search_text.label if search_text else "Поиск")
        parts.append(
            div(
                "search-pill",
                style_from_bounds(search_box, width, height),
                f'<span class="search-glyph">⌕</span><span>{label}</span>',
            )
        )
    if qr:
        parts.append(div("top-icon qr", style_from_bounds(qr, width, height), "▣"))
    return "\n".join(parts)


def render_wallet_selector(nodes: List[UiNode], width: int, height: int) -> str:
    label = find_text(nodes, "Основной кошелек 1")
    copy = find_by_resource(nodes, "walletSelectorCopyIcon")
    chip = next(
        (
            node
            for node in nodes
            if node.clickable and 240 <= node.y <= 310 and 250 <= node.x <= 400 and node.w > 250
        ),
        None,
    )
    parts: List[str] = []
    if chip:
        parts.append(
            div(
                "wallet-chip",
                style_from_bounds(chip, width, height),
                f"<span>{esc(label.label if label else 'Кошелек')}</span><b>›</b>",
            )
        )
    if copy:
        parts.append(div("copy-icon", style_from_bounds(copy, width, height), "□"))
    return "\n".join(parts)


def render_actions(nodes: List[UiNode], width: int, height: int) -> str:
    resource_ids = ["HomeSendButton", "HomeReceiveButton", "HomeSwapButton", "HomeBuyButton"]
    labels = ["Отправить", "Получить", "Обмен", "Покупка"]
    parts: List[str] = []
    for rid, label_text in zip(resource_ids, labels):
        node = find_by_resource(nodes, rid)
        label = find_text(nodes, label_text)
        if not node:
            continue
        icon = icon_for_action(label_text)
        body = (
            f'<div class="action-icon">{esc(icon)}</div>'
            f'<div class="action-label">{esc(label.label if label else label_text)}</div>'
        )
        parts.append(div("action", style_from_bounds(node, width, height), body))
    return "\n".join(parts)


def render_banner(nodes: List[UiNode], width: int, height: int) -> str:
    banner = find_by_resource(nodes, "bannerSlide")
    texts = texts_between(nodes, 760, 980)
    if not banner:
        return ""
    title = texts[0].label if texts else ""
    cta = texts[1].label if len(texts) > 1 else ""
    body = (
        '<div class="banner-art"><span>H</span></div>'
        '<div class="banner-copy">'
        f'<strong>{esc(title)}</strong>'
        f'<em>{esc(cta)}</em>'
        '</div>'
    )
    return div("banner", style_from_bounds(banner, width, height), body)


def render_tabs(nodes: List[UiNode], width: int, height: int) -> str:
    parts: List[str] = []
    crypto_tab = find_by_resource(nodes, "Криптовалюта")
    watch_tab = find_by_resource(nodes, "WatchlistTab")
    history = find_by_resource(nodes, "historyIcon")
    settings = find_by_resource(nodes, "assetsLayoutSettingsIcon")
    labels = {
        "Криптовалюта": find_text(nodes, "Криптовалюта"),
        "WatchlistTab": find_text(nodes, "Избранное"),
    }
    if crypto_tab:
        parts.append(
            div(
                "tab active",
                style_from_bounds(crypto_tab, width, height),
                esc(labels["Криптовалюта"].label if labels["Криптовалюта"] else "Криптовалюта"),
            )
        )
    if watch_tab:
        parts.append(
            div(
                "tab",
                style_from_bounds(watch_tab, width, height),
                esc(labels["WatchlistTab"].label if labels["WatchlistTab"] else "Избранное"),
            )
        )
    if history:
        parts.append(div("tab-icon", style_from_bounds(history, width, height), "↺"))
    if settings:
        parts.append(div("tab-icon sliders", style_from_bounds(settings, width, height), "☷"))
    return "\n".join(parts)


def render_empty_state(nodes: List[UiNode], width: int, height: int) -> str:
    title = find_text(nodes, "Пополните кошелек, чтобы начать торговать и зарабатывать")
    button_label = find_text(nodes, "Пополнить")
    button = next(
        (
            node
            for node in nodes
            if node.clickable and 1750 <= node.y <= 1900 and node.w > 800 and node.h > 100
        ),
        None,
    )
    secondary_label = find_text(nodes, "Получить криптовалюту")
    secondary = next(
        (
            node
            for node in nodes
            if node.clickable and 1980 <= node.y <= 2050 and node.w > 800
        ),
        None,
    )
    if not title:
        return ""
    card_x = 44
    card_y = 1280
    card_w = 992
    card_h = 820
    parts = [
        div(
            "empty-card",
            (
                f"left:{pct(card_x, width)};top:{pct(card_y, height)};"
                f"width:{pct(card_w, width)};height:{pct(card_h, height)};"
            ),
            '<div class="empty-art"><div></div></div>',
        ),
        text_block(title, "empty-title", width, height),
    ]
    if button:
        parts.append(div("primary-button", style_from_bounds(button, width, height), esc(button_label.label if button_label else "Пополнить")))
    if secondary:
        parts.append(div("secondary-button", style_from_bounds(secondary, width, height), esc(secondary_label.label if secondary_label else "")))
    return "\n".join(parts)


def render_bottom_nav(nodes: List[UiNode], width: int, height: int) -> str:
    nav_labels = [
        ("HomeNavigationButton", "⌂", "Главная", True),
        ("TrendingTokenNavigationButton", "↗", "Рынки", False),
        ("", "⇄", "Обмен", True),
        ("PerpsNavigationButton", "♜", "Бессрочные...", False),
        ("DiscoverNavigationButton", "◉", "Подробнее", False),
    ]
    bar_style = f"left:{pct(44, width)};top:{pct(2020, height)};width:{pct(992, width)};height:{pct(148, height)};"
    items: List[str] = []
    for rid, icon, label, special in nav_labels:
        if rid:
            node = find_by_resource(nodes, rid)
        else:
            node = next((node for node in nodes if node.label == "Обмен" and node.y >= 2000), None)
        if not node:
            continue
        cls = "nav-item"
        if label == "Главная":
            cls += " selected"
        if label == "Обмен":
            cls += " center"
        items.append(
            div(
                cls,
                style_from_bounds(node, width, height),
                f'<strong>{esc(icon)}</strong><span>{esc(label)}</span>',
            )
        )
    return div("bottom-bar", bar_style) + "\n" + "\n".join(items)


def render_generic_text(nodes: List[UiNode], used_labels: set[str], width: int, height: int) -> str:
    parts: List[str] = []
    for node in nodes:
        if not node.label or node.w <= 0 or node.h <= 0:
            continue
        if node.label in used_labels:
            continue
        if node.y >= 2050:
            continue
        parts.append(text_block(node, "generic-text", width, height))
    return "\n".join(parts)


def render(out_dir: Path) -> Path:
    hierarchy_path = out_dir / "ui_hierarchy.json"
    if not hierarchy_path.exists():
        raise SystemExit(f"Missing {hierarchy_path}")
    hierarchy = json.loads(hierarchy_path.read_text(encoding="utf-8"))
    nodes = list(walk(hierarchy["root"]))
    width, height = canvas_size(nodes)
    direct_dir = out_dir / "direct_ui"
    direct_dir.mkdir(parents=True, exist_ok=True)

    used_labels = {
        "Поиск",
        "0,00\u00a0$",
        "0,00 $",
        "Основной кошелек 1",
        "Отправить",
        "Получить",
        "Обмен",
        "Покупка",
        "Hyperliquid live with 200+ markets, 0% markup on fees",
        "Explore now",
        "Криптовалюта",
        "Избранное",
        "Пополните кошелек, чтобы начать торговать и зарабатывать",
        "Пополнить",
        "Получить криптовалюту",
    }

    body = "\n".join(
        part
        for part in [
            render_support_layers(nodes, width, height),
            render_top_bar(nodes, width, height),
            render_wallet_selector(nodes, width, height),
            render_actions(nodes, width, height),
            render_banner(nodes, width, height),
            render_tabs(nodes, width, height),
            render_empty_state(nodes, width, height),
            render_bottom_nav(nodes, width, height),
            render_generic_text(nodes, used_labels, width, height),
        ]
        if part
    )

    html_text = f"""<!doctype html>
<html lang="ru">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Direct Parsed UI</title>
  <style>
    * {{ box-sizing: border-box; }}
    html, body {{
      margin: 0;
      min-height: 100%;
      background: #101114;
      color: #f5f5f7;
      font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }}
    body {{
      display: grid;
      place-items: start center;
      padding: 18px;
    }}
    .phone {{
      position: relative;
      width: min(100%, 460px);
      aspect-ratio: {width} / {height};
      background: #111216;
      overflow: hidden;
      border-radius: 30px;
      box-shadow: 0 24px 80px rgba(0, 0, 0, .55);
      border: 1px solid #2a2c32;
    }}
    .node {{ position: absolute; }}
    .content-panel {{
      position: absolute;
      background: #111216;
    }}
    .tab-divider {{
      position: absolute;
      background: rgba(255, 255, 255, .08);
    }}
    .carousel-dots {{
      position: absolute;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12%;
    }}
    .carousel-dots i {{
      display: block;
      width: 16%;
      aspect-ratio: 1;
      border-radius: 999px;
      background: rgba(255, 255, 255, .24);
    }}
    .carousel-dots i.active {{
      width: 42%;
      aspect-ratio: 4 / 1;
      background: rgba(255, 255, 255, .88);
    }}
    .bottom-fade {{
      position: absolute;
      background: linear-gradient(transparent, rgba(17, 18, 22, .9));
      pointer-events: none;
    }}
    .top-icon, .copy-icon, .tab-icon {{
      position: absolute;
      display: grid;
      place-items: center;
      color: #e6e6ea;
      font-size: 24px;
      font-weight: 700;
    }}
    .settings::after {{
      content: "";
      position: absolute;
      right: 28%;
      top: 22%;
      width: 10%;
      aspect-ratio: 1;
      border-radius: 999px;
      background: #ff4d5a;
    }}
    .search-pill {{
      position: absolute;
      display: flex;
      align-items: center;
      gap: 4%;
      padding: 0 5%;
      border-radius: 999px;
      background: #2b2c31;
      color: #8f9097;
      font-size: 18px;
      font-weight: 700;
    }}
    .search-glyph {{
      color: #d7d7dc;
      font-size: 1.2em;
      transform: translateY(-2%);
    }}
    .wallet-chip {{
      position: absolute;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4%;
      border-radius: 999px;
      background: #27282d;
      color: #f4f4f7;
      font-size: 12px;
      font-weight: 800;
      padding: 0 4%;
    }}
    .wallet-chip span {{
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }}
    .wallet-chip b {{ font-size: 1.35em; line-height: 1; }}
    .wallet-chip::after {{
      content: "";
      position: absolute;
      right: 4%;
      top: -7%;
      width: 7%;
      aspect-ratio: 1;
      background: #ff4d5a;
      border-radius: 999px;
    }}
    .action {{
      position: absolute;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      gap: 10%;
      color: #f4f4f7;
      font-weight: 750;
    }}
    .action-icon {{
      width: 64%;
      aspect-ratio: 1;
      border-radius: 24%;
      background: #27282d;
      display: grid;
      place-items: center;
      font-size: 34px;
      line-height: 1;
    }}
    .action-label {{
      font-size: 14px;
      line-height: 1;
      white-space: nowrap;
    }}
    .banner {{
      position: absolute;
      display: grid;
      grid-template-columns: 23% 1fr;
      align-items: center;
      gap: 4%;
      border-radius: 22px;
      background: #1f2025;
      border: 2px solid #2d3139;
      padding: 0 5%;
    }}
    .banner-art {{
      width: 100%;
      aspect-ratio: 1;
      display: grid;
      place-items: center;
      border-radius: 24%;
      background: linear-gradient(135deg, #27f58a, #8757ff 54%, #ffdb3b);
      transform: rotate(-8deg);
    }}
    .banner-art span {{
      color: #101114;
      font-size: clamp(22px, 5vw, 40px);
      font-weight: 900;
    }}
    .banner-copy {{
      min-width: 0;
      display: grid;
      gap: 8%;
    }}
    .banner-copy strong {{
      display: block;
      color: #f5f5f7;
      font-size: 15px;
      line-height: 1.22;
    }}
    .banner-copy em {{
      display: block;
      color: #35f58a;
      font-style: normal;
      font-weight: 850;
      font-size: 13px;
    }}
    .tab {{
      position: absolute;
      display: flex;
      align-items: center;
      color: #8f9097;
      font-size: 17px;
      font-weight: 850;
      padding-left: 5%;
    }}
    .tab.active {{
      color: #f4f4f7;
    }}
    .tab.active::after {{
      content: "";
      position: absolute;
      left: 8%;
      right: 8%;
      bottom: 0;
      height: 6%;
      min-height: 3px;
      border-radius: 999px;
      background: #35f58a;
    }}
    .tab-icon {{
      color: #b8b8bf;
      font-size: 25px;
    }}
    .empty-card {{
      position: absolute;
      border-radius: 24px;
      background: #2b2c2f;
    }}
    .empty-art {{
      position: absolute;
      left: 38%;
      top: 7%;
      width: 24%;
      aspect-ratio: 1;
      border-radius: 20%;
      background: linear-gradient(135deg, #2cf58d, #e6ff4c 55%, #1024ff);
      transform: rotate(8deg);
      box-shadow: 0 0 0 10px rgba(53, 245, 138, .08);
    }}
    .empty-art div {{
      position: absolute;
      inset: 25%;
      background: #111216;
      border-radius: 18%;
    }}
    .empty-title {{
      position: absolute;
      display: grid;
      place-items: center;
      color: #f4f4f7;
      text-align: center;
      font-size: 18px;
      line-height: 1.12;
      font-weight: 900;
      padding: 0 4%;
      overflow: hidden;
    }}
    .primary-button {{
      position: absolute;
      display: grid;
      place-items: center;
      border-radius: 999px;
      background: #35f58a;
      color: #07140d;
      font-size: 22px;
      font-weight: 900;
    }}
    .secondary-button {{
      position: absolute;
      display: grid;
      place-items: center;
      border-radius: 999px;
      background: rgba(53, 245, 138, .16);
      color: #35f58a;
      font-size: 13px;
      font-weight: 800;
    }}
    .bottom-bar {{
      position: absolute;
      border-radius: 28px 28px 0 0;
      background: rgba(18, 18, 22, .96);
      box-shadow: 0 -12px 34px rgba(0, 0, 0, .35);
    }}
    .nav-item {{
      position: absolute;
      display: grid;
      grid-template-rows: 54% 1fr;
      place-items: center;
      color: #a8a8af;
      font-weight: 800;
      font-size: 11px;
      text-align: center;
    }}
    .nav-item strong {{
      font-size: 25px;
      line-height: 1;
      color: #bfc0c7;
    }}
    .nav-item.selected {{
      color: #35f58a;
      border-radius: 999px;
      background: rgba(53, 245, 138, .16);
    }}
    .nav-item.selected strong {{ color: #35f58a; }}
    .nav-item.center {{
      border-radius: 50%;
      background: #35f58a;
      color: #101114;
      transform: translateY(-28%);
      grid-template-rows: 70% 1fr;
      font-size: 10px;
    }}
    .nav-item.center strong {{
      color: #101114;
      font-size: 30px;
    }}
    .generic-text {{
      position: absolute;
      display: grid;
      place-items: center;
      color: #f4f4f7;
      font-size: 13px;
      font-weight: 750;
      text-align: center;
    }}
  </style>
</head>
<body>
  <main class="phone" aria-label="Direct parsed UI reconstruction">
{body}
  </main>
</body>
</html>
"""
    output = direct_dir / "index.html"
    output.write_text(html_text, encoding="utf-8")
    return output


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate no-screenshot UI reconstruction.")
    parser.add_argument("out_dir", nargs="?", default="app_ui_export")
    args = parser.parse_args()
    print(render(Path(args.out_dir)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
