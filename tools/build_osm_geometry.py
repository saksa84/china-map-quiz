"""Construit les contours tirés d'OpenStreetMap : parcs, et petits lacs absents de Natural Earth (version 2).

Usage (depuis la racine du projet, après tools/build_map_geometry.py) :
    python tools/build_osm_geometry.py --download   # interroge l'API Overpass, garde la réponse dans tools/raw/
    python tools/build_osm_geometry.py              # reconstruit à partir de tools/raw/osm_features.json

Entrées :
    tools/raw/osm_features.json                  (réponse Overpass : objets OSM listés dans OSM_IDS, avec leur géométrie)
    app/src/main/assets/geo/map.geometry.json (contours des provinces : vérification « le parc est dans sa province »)
    app/src/main/assets/geo/{parks,lakes}.json (identifiants et provinces attendus)
Sortie :
    app/src/main/assets/geo/osm.geometry.json

Tous les lieux d'OSM sont réunis dans un seul fichier, pour que les données sous ODbL restent séparées
des données Natural Earth (domaine public).

LICENCE : les contours viennent d'OpenStreetMap, © les contributeurs d'OpenStreetMap, sous licence
Open Database License (ODbL 1.0). osm.geometry.json est une base de données dérivée : elle reste sous
ODbL (attribution et partage dans les mêmes conditions), contrairement au reste des données du projet.

Les parcs sans contour exploitable dans OSM (vérifié le 2026-10-25) sont représentés par un point
(MANUAL_POINTS) : coordonnées approximatives du centre du parc.
"""

import json
import math
import sys
import urllib.parse
import urllib.request
from pathlib import Path

from build_map_geometry import GRID_KM, LAT0, LAT1, LAT2, LON0, douglas_peucker, key, label_point, point_in_rings, project

ROOT = Path(__file__).resolve().parent.parent
RAW_FILE = ROOT / "tools" / "raw" / "osm_features.json"
ASSETS = ROOT / "app" / "src" / "main" / "assets" / "geo"
OUT_FILE = ASSETS / "osm.geometry.json"
OVERPASS_URL = "https://overpass-api.de/api/interpreter"
SOURCE_ID = "osm"

# Objets OSM retenus : (type, identifiant). Choisis à la main le 2026-10-25 parmi les résultats
# de recherche par nom (voir docs/data-sources.md, section Parcs).
OSM_IDS = {
    "PA-SANJIANGYUAN": ("relation", 13373980),  # 三江源国家公园
    "PA-PANDA": ("relation", 13556512),         # 大熊猫国家公园
    "PA-WUYISHAN": ("relation", 21522962),      # 武夷山 (Mount Wuyi)
    "PA-JIUZHAIGOU": ("relation", 7516592),     # 九寨沟风景名胜区
    "PA-HUANGLONG": ("relation", 6213328),      # 黄龙风景名胜区
    "PA-WULINGYUAN": ("relation", 18589298),    # 武陵源风景名胜区
    "PA-LIJIANG": ("relation", 21263564),       # 桂林喀斯特世界自然遗产地-漓江地区核心区
    "PA-SHILIN": ("relation", 21669396),        # 石林风景区
    "PA-FANJINGSHAN": ("way", 626892766),       # 梵净山风景区 (réserve nationale)
    "PA-KANAS": ("way", 614666060),             # 喀纳斯自然保护区
    "PA-DANXIA": ("relation", 21669142),        # 丹霞山世界地质公园
    "PA-LUSHAN": ("relation", 6634714),         # 庐山国家公园
    "PA-SANQINGSHAN": ("way", 385707111),       # 三清山风景区
    "PA-ZHANGYE": ("way", 379678356),           # 张掖七彩丹霞旅游景区
    # Petits lacs absents de Natural Earth (natural=water).
    "LK-XIHU": ("relation", 2308774),           # 西湖 (Hangzhou)
    "LK-DIANCHI": ("relation", 254775),         # 滇池
    "LK-ERHAI": ("relation", 254433),           # 洱海
}

# Parcs absents d'OSM : (longitude, latitude) approximatives du centre.
MANUAL_POINTS = {
    "PA-HUBAO": (130.5, 43.3),     # Tigre et léopard de Chine du Nord-Est : entre Hunchun et Wangqing (Jilin)
    "PA-HAINAN": (109.6, 18.95),   # Forêt tropicale de Hainan : massif central de l'île (Wuzhishan, Qiongzhong)
}

TOLERANCE_UNITS = 3          # simplification : 0,3 km
MAX_VERTICES = 600           # au-delà (Sanjiangyuan, Panda), on simplifie davantage
PROVINCE_SLACK = 150         # un parc à cheval sur deux provinces peut avoir son centre près de la limite


def download():
    lines = [f"{kind}({osm_id});" for kind, osm_id in OSM_IDS.values()]
    query = "[out:json][timeout:300];(" + "".join(lines) + ");out geom;"
    req = urllib.request.Request(OVERPASS_URL, data=urllib.parse.urlencode({"data": query}).encode("utf-8"),
                                 headers={"User-Agent": "ChinaMapQuiz/1.0 (projet personnel)"})
    with urllib.request.urlopen(req, timeout=600) as response:
        RAW_FILE.write_bytes(response.read())
    print(f"téléchargé : {RAW_FILE} ({RAW_FILE.stat().st_size // 1024} Ko)")


def to_units(lon, lat):
    x, y = project(key(lon, lat))
    return round(x / GRID_KM), -round(y / GRID_KM)


def stitch(segments):
    """Assemble des chemins (listes de points) en anneaux fermés, en raccordant leurs extrémités."""
    segments = [list(s) for s in segments if len(s) >= 2]
    rings = []
    while segments:
        ring = segments.pop(0)
        changed = True
        while ring[0] != ring[-1] and changed:
            changed = False
            for i, s in enumerate(segments):
                if s[0] == ring[-1]:
                    ring += s[1:]
                elif s[-1] == ring[-1]:
                    ring += s[::-1][1:]
                elif s[-1] == ring[0]:
                    ring = s[:-1] + ring
                elif s[0] == ring[0]:
                    ring = s[::-1][:-1] + ring
                else:
                    continue
                segments.pop(i)
                changed = True
                break
        if ring[0] == ring[-1] and len(ring) >= 4:
            rings.append(ring)
    return rings


def ring_area(ring):
    return abs(sum(a[0] * b[1] - b[0] * a[1] for a, b in zip(ring, ring[1:] + ring[:1]))) / 2


def polygons_of(element):
    """Élément Overpass (way fermé ou relation multipolygone) -> polygones [[extérieur, trous…]] en lon/lat."""
    if element["type"] == "way":
        pts = [(p["lon"], p["lat"]) for p in element["geometry"]]
        return [[pts]] if pts[0] == pts[-1] else []
    outers = stitch([[(p["lon"], p["lat"]) for p in m["geometry"]] for m in element["members"]
                     if m["type"] == "way" and m.get("role") in ("outer", "") and "geometry" in m])
    inners = stitch([[(p["lon"], p["lat"]) for p in m["geometry"]] for m in element["members"]
                     if m["type"] == "way" and m.get("role") == "inner" and "geometry" in m])
    polygons = [[outer] for outer in outers]
    for inner in inners:
        for poly in polygons:
            if point_in_rings([poly[0]], *inner[0]):
                poly.append(inner)
                break
    return polygons


def simplify(polygons, tolerance):
    out = []
    for poly in polygons:
        rings = []
        for ring in poly:
            pts = douglas_peucker([to_units(lon, lat) for lon, lat in ring], tolerance)
            if len(pts) >= 4:
                rings.append(pts[:-1] if pts[0] == pts[-1] else pts)
        if rings:
            out.append(rings)
    return out


def province_of(provinces, x, y):
    return next((pid for pid, rings in provinces.items() if point_in_rings(rings, x, y)), None)


def main() -> int:
    if "--download" in sys.argv:
        download()
    raw = json.loads(RAW_FILE.read_text(encoding="utf-8"))
    elements = {(e["type"], e["id"]): e for e in raw["elements"]}
    geometry = json.loads((ASSETS / "map.geometry.json").read_text(encoding="utf-8"))
    provinces = {f["id"]: [list(zip(r[0::2], r[1::2])) for p in f["polygons"] for r in p] for f in geometry["features"]}
    meta = {e["id"]: e for name in ("parks", "lakes") for e in json.loads((ASSETS / f"{name}.json").read_text(encoding="utf-8"))["entities"]
            if e["id"] in OSM_IDS or e["id"] in MANUAL_POINTS}

    errors, out = [], []
    for pid, ref in OSM_IDS.items():
        element = elements.get(ref)
        if element is None:
            errors.append(f"{pid} : {ref} absent de la réponse Overpass")
            continue
        lonlat = polygons_of(element)
        tolerance = TOLERANCE_UNITS
        polygons = simplify(lonlat, tolerance)
        while sum(len(r) for p in polygons for r in p) > MAX_VERTICES:
            tolerance *= 1.5
            polygons = simplify(lonlat, tolerance)
        # Les miettes (moins de 0,5 % de la plus grande partie) sont retirées.
        if polygons:
            biggest = max(ring_area(p[0]) for p in polygons)
            polygons = [p for p in polygons if ring_area(p[0]) >= 0.005 * biggest]
        if not polygons:
            errors.append(f"{pid} : aucun contour fermé")
            continue
        out.append((pid, "polygon", polygons))
        x, y = label_point(polygons)[:2]
        found = province_of(provinces, x, y)
        if found != meta[pid]["parentId"]:
            errors.append(f"{pid} : centre dans {found}, attendu {meta[pid]['parentId']}")
        area = sum(ring_area(p[0]) for p in polygons) * GRID_KM * GRID_KM
        print(f"  {pid:18} {sum(len(r) for p in polygons for r in p):4} sommets, {len(polygons)} partie(s), {area:9.0f} km²")
    for pid, (lon, lat) in MANUAL_POINTS.items():
        point = to_units(lon, lat)
        out.append((pid, "point", point))
        if province_of(provinces, *point) != meta[pid]["parentId"]:
            errors.append(f"{pid} : point hors de {meta[pid]['parentId']}")

    ids = sorted(i for i, _, _ in out)
    if ids != sorted(meta):
        errors.append(f"identifiants : manquants {sorted(set(meta) - set(ids))}, en trop {sorted(set(ids) - set(meta))}")
    if errors:
        print("ERREURS :\n  " + "\n  ".join(errors))
        return 1

    lines = []
    for i, kind, g in sorted(out):
        if kind == "point":
            feature = {"id": i, "kind": "point", "point": list(g)}
        else:
            feature = {"id": i, "kind": "polygon", "label": label_point(g),
                       "polygons": [[[c for p in ring for c in p] for ring in poly] for poly in g]}
        lines.append(json.dumps(feature, separators=(",", ":")))
    header = {
        "schemaVersion": 1, "sourceId": SOURCE_ID,
        "license": "ODbL 1.0 — © les contributeurs d'OpenStreetMap (https://www.openstreetmap.org/copyright)",
        "projection": {"name": "albers", "lon0": LON0, "lat0": LAT0, "lat1": LAT1, "lat2": LAT2},
        "unitKm": GRID_KM, "yAxis": "down", "bounds": geometry["bounds"],
    }
    text = json.dumps(header, ensure_ascii=False)[:-1] + ',\n"features":[\n' + ",\n".join(lines) + "\n]}\n"
    OUT_FILE.write_text(text, encoding="utf-8", newline="\n")
    print(f"{len(out)} lieux, fichier {OUT_FILE.stat().st_size // 1024} Ko")
    return 0


if __name__ == "__main__":
    sys.exit(main())
