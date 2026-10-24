"""Génère le premier plan de l'icône de l'application : la silhouette de la Chine, en blanc.

Usage (depuis la racine du projet, après tools/build_map_geometry.py) :
    python tools/build_icon.py

Entrée  : app/src/main/assets/geo/map.geometry.json (nos propres contours, domaine public)
Sortie  : app/src/main/res/drawable/ic_launcher_foreground.xml (vecteur Android 108 × 108 dp)

L'icône adaptative d'Android est rognée par le lanceur (cercle, carré arrondi…) : la carte est
placée dans la « zone sûre » centrale de 66 dp pour ne jamais être coupée.
"""

import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GEOMETRY = ROOT / "app" / "src" / "main" / "assets" / "geo" / "map.geometry.json"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable" / "ic_launcher_foreground.xml"

VIEWPORT = 108.0
SAFE_ZONE = 62.0  # un peu moins que les 66 dp garantis, pour laisser respirer
TOLERANCE = 0.35  # en dp : à cette taille, un détail plus fin est invisible
MIN_RING_AREA = 0.6  # en dp² : les îlots plus petits disparaissent de l'icône


def douglas_peucker(points, tolerance):
    if len(points) < 3:
        return points
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        first, last = stack.pop()
        (x1, y1), (x2, y2) = points[first], points[last]
        length = math.hypot(x2 - x1, y2 - y1)
        best, best_i = -1.0, -1
        for i in range(first + 1, last):
            px, py = points[i]
            d = math.hypot(px - x1, py - y1) if length == 0 else abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) / length
            if d > best:
                best, best_i = d, i
        if best > tolerance:
            keep[best_i] = True
            stack += [(first, best_i), (best_i, last)]
    return [p for p, k in zip(points, keep) if k]


def area(points):
    return abs(sum(x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(points, points[1:] + points[:1]))) / 2


def main():
    geo = json.loads(GEOMETRY.read_text(encoding="utf-8"))
    minx, miny, maxx, maxy = geo["bounds"]
    scale = SAFE_ZONE / max(maxx - minx, maxy - miny)
    ox = (VIEWPORT - (maxx - minx) * scale) / 2 - minx * scale
    oy = (VIEWPORT - (maxy - miny) * scale) / 2 - miny * scale

    paths = []
    for feature in geo["features"]:
        for polygon in feature["polygons"]:
            for ring in polygon:
                pts = [(x * scale + ox, y * scale + oy) for x, y in zip(ring[0::2], ring[1::2])]
                pts = douglas_peucker(pts + pts[:1], TOLERANCE)[:-1]
                if len(pts) >= 3 and area(pts) >= MIN_RING_AREA:
                    paths.append("M" + "L".join(f"{x:.1f},{y:.1f}" for x, y in pts) + "Z")

    xml = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Généré par tools/build_icon.py : ne pas modifier à la main. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        f'    android:viewportWidth="{VIEWPORT:.0f}"\n'
        f'    android:viewportHeight="{VIEWPORT:.0f}">\n'
        '    <path\n'
        '        android:fillColor="#FFFFFFFF"\n'
        '        android:fillType="evenOdd"\n'
        f'        android:pathData="{"".join(paths)}" />\n'
        "</vector>\n"
    )
    OUT.write_text(xml, encoding="utf-8", newline="\n")
    print(f"{len(paths)} contours, {OUT.stat().st_size // 1024} Ko -> {OUT}")


if __name__ == "__main__":
    main()
