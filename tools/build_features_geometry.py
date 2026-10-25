"""Construit la géométrie des villes, cours d'eau, montagnes et lacs pour l'application.

Usage (depuis la racine du projet, après tools/download_natural_earth.py et tools/build_map_geometry.py) :
    python tools/build_features_geometry.py

Entrées :
    tools/raw/ne_10m_populated_places.geojson            (coordonnées des villes)
    tools/raw/ne_10m_rivers_lake_centerlines.geojson     (tracés des cours d'eau)
    tools/raw/ne_10m_geography_regions_polys.geojson     (zones des chaînes de montagnes)
    tools/raw/ne_10m_geography_regions_elevation_points.geojson (sommets)
    tools/raw/ne_10m_lakes.geojson                       (contours des lacs)
    app/src/main/assets/geo/map.geometry.json            (contours des provinces : « est-ce en Chine ? »)
    app/src/main/assets/geo/{cities,rivers,mountains,lakes}.json (identifiants et rattachements attendus)
Sortie :
    app/src/main/assets/geo/features.geometry.json

Le choix des lieux (quelle ville, quel tronçon de rivière…) est fait ici, dans SELECTIONS : les
noms Natural Earth ne sont pas toujours uniques (deux « Min », deux « Han »), d'où les cadres
géographiques qui lèvent l'ambiguïté. Les cours d'eau sont coupés à la frontière chinoise
(avec une marge pour garder les fleuves frontaliers comme l'Amour).
"""

import json
import math
import sys
from collections import defaultdict
from pathlib import Path

from build_osm_geometry import OSM_IDS
from build_map_geometry import (
    GRID_KM, LAT0, LAT1, LAT2, LON0, SOURCE_ID, douglas_peucker, key, label_point, point_in_rings, project,
)

ROOT = Path(__file__).resolve().parent.parent
RAW = ROOT / "tools" / "raw"
ASSETS = ROOT / "app" / "src" / "main" / "assets" / "geo"
OUT_FILE = ASSETS / "features.geometry.json"

# Marge autour de la Chine (unités de carte de 100 m) : 10 km.
BORDER_BUFFER = 100
RIVER_TOLERANCE = 8      # simplification des rivières : 0,8 km
RANGE_TOLERANCE = 15     # simplification des zones de montagne : 1,5 km
LAKE_TOLERANCE = 5       # simplification des lacs : 0,5 km (petits contours, à garder reconnaissables)
LAKE_PROVINCE_SLACK = 150  # un petit lac ou le centre d'un lac allongé peut tomber près d'une limite provinciale
CITY_PROVINCE_SLACK = 50 # une ville côtière peut tomber 5 km en mer après simplification du littoral

# Lieux absents de Natural Earth : (longitude, latitude), d'après les articles encyclopédiques.
MANUAL_POINTS = {
    "CT-ZHONGSHAN": (113.39, 22.52),
    "PK-TAISHAN": (117.10, 36.25),
    "PK-HUANGSHAN": (118.16, 30.13),
    "PK-EMEI": (103.33, 29.52),
}

# Villes : nom chinois dans Natural Earth (champ NAME_ZH).
CITY_ZH = {
    "CT-SHANGHAI": "上海", "CT-BEIJING": "北京", "CT-SHENZHEN": "深圳", "CT-GUANGZHOU": "广州",
    "CT-CHENGDU": "成都", "CT-TIANJIN": "天津", "CT-WUHAN": "武汉", "CT-DONGGUAN": "东莞",
    "CT-CHONGQING": "重庆", "CT-XIAN": "西安", "CT-HANGZHOU": "杭州", "CT-FOSHAN": "佛山",
    "CT-NANJING": "南京", "CT-SHENYANG": "沈阳", "CT-ZHENGZHOU": "郑州", "CT-QINGDAO": "青岛",
    "CT-SUZHOU": "苏州", "CT-JINAN": "济南", "CT-CHANGSHA": "长沙", "CT-KUNMING": "昆明",
    "CT-HARBIN": "哈尔滨", "CT-SHIJIAZHUANG": "石家庄", "CT-HEFEI": "合肥", "CT-DALIAN": "大连",
    "CT-XIAMEN": "厦门", "CT-NANNING": "南宁", "CT-CHANGCHUN": "长春", "CT-TAIYUAN": "太原",
    "CT-GUIYANG": "贵阳", "CT-WUXI": "无锡", "CT-URUMQI": "乌鲁木齐", "CT-SHANTOU": "汕头",
    "CT-NINGBO": "宁波", "CT-FUZHOU": "福州", "CT-NANCHANG": "南昌", "CT-CHANGZHOU": "常州",
    "CT-LANZHOU": "兰州", "CT-HONGKONG": "香港", "CT-TAIPEI": "台北",
}

# Cours d'eau : noms des tronçons Natural Earth (champ name) et cadre [lon min, lon max, lat min, lat max]
# pour lever les ambiguïtés de nom.
RIVERS = {
    "RV-CHANGJIANG": (["Tuotuo", "Tongtian", "Jinsha", "Chang Jiang", "Yangtze"], None),
    "RV-HUANGHE": (["Huang"], None),
    "RV-HEILONGJIANG": (["Heilong Jiang", "Amur"], None),
    # Chaîne du Xi Jiang d'amont en aval : Nanpan, Hongshui, Quan (= Qian Jiang), Xun, Xi.
    "RV-ZHUJIANG": (["Xi", "Xun", "Quan", "Hongshui", "Nanpan"], (100, 116, 20, 27)),
    "RV-LANCANGJIANG": (["Lancang", "Mekong", "Za"], None),
    "RV-NUJIANG": (["Nu", "Salween"], None),
    "RV-YARLUNGZANGBO": (["Yarlung", "Maquan", "Dihang", "Brahmaputra"], None),
    # « Hudi » : cours inférieur du Huai, du confluent du Ying au lac Hongze puis au Yangtsé (Sanjiangying).
    "RV-HUAIHE": (["Huai", "Hudi"], (110, 122, 30, 35)),
    # Le Xiliao (Liao occidental) fait partie du Liao dans la longueur conventionnelle de 1 345 km.
    "RV-LIAOHE": (["Liao", "Xiliao"], (118, 126, 38, 46)),
    "RV-HAIHE": (["Hai"], (115, 119, 38, 41)),
    # Le Yarkant est la source principale du Tarim dans la longueur retenue (2 327 km).
    "RV-TARIMHE": (["Tarim", "Yarkant"], None),
    "RV-EERQISIHE": (["Ertix"], None),
    "RV-HANJIANG": (["Han"], (100, 120, 28, 36)),
    "RV-JIALINGJIANG": (["Jialing"], None),
    "RV-MINJIANG": (["Min"], (100, 110, 26, 34)),
    "RV-DADUHE": (["Dadu"], None),
    "RV-YALONGJIANG": (["Yalong"], None),
    "RV-WUJIANG": (["Wu"], (103, 110, 25, 31)),
    "RV-XIANGJIANG": (["Xiang"], None),
    "RV-YUANJIANG": (["Yuan"], (106, 114, 25, 30)),
    "RV-GANJIANG": (["Gan"], (113, 117, 24, 30)),
    "RV-WEIHE": (["Wei"], (103, 111, 33, 36)),
    "RV-SONGHUAJIANG": (["Songhua", "Di’er Songhua"], None),
    "RV-WUSULIJIANG": (["Ussuri"], None),
    "RV-DONGJIANG": (["Dong"], (112, 117, 22, 26)),
    "RV-BEIJIANG": (["Bei"], (111, 115, 22, 26)),
    # Le Sanggan est le cours supérieur du Yongding (longueur retenue : 747 km).
    "RV-YONGDINGHE": (["Yongding", "Sanggan"], None),
}

# Chaînes : nom Natural Earth (champ NAME) des zones « Range/mtn ».
RANGES = {
    "MT-HIMALAYA": "HIMALAYAS", "MT-KARAKORAM": "KARAKORAM RA.", "MT-KUNLUN": "KUNLUN MOUNTAINS",
    "MT-TIANSHAN": "TIAN SHAN", "MT-ALTAI": "ALTAY MOUNTAINS", "MT-ALTUN": "ALTUN MTS.",
    "MT-QILIAN": "QUILIAN MOUNTAINS", "MT-QINLING": "Qinling Mountains", "MT-DAXINGANLING": "GREATER KHINGAN RANGE",
    "MT-XIAOXINGANLING": "Lesser Khingan Range", "MT-TAIHANG": "Taihang Mts.", "MT-LULIANG": "Lüliang Mts.",
    "MT-YINSHAN": "Yin Mts.", "MT-DABIE": "Dabie Mts.", "MT-DALOU": "Dalou Mts.", "MT-NANLING": "Nan Ling Mts.",
    "MT-WUYI": "Wuyi Mts.",
}

# Sommets : nom Natural Earth (champ name) des points d'altitude.
PEAKS = {
    "PK-QOMOLANGMA": "Mount Everest", "PK-K2": "K2", "PK-NAMCHABARWA": "Namcha Barwa", "PK-KONGUR": "Kongkoerh",
    "PK-GONGGA": "Gongga Shan", "PK-TOMUR": "Pik Pobeda", "PK-NYAINQENTANGLHA": "Nyainqentanglha Feng",
    "PK-KAILASH": "Kailash", "PK-GELADANDONG": "Geladandong", "PK-ANYEMAQEN": "Magen Gangri",
    "PK-BOGDA": "Bogda Feng", "PK-YUSHAN": "Yu Shan", "PK-TAIBAI": "Taibai Shan", "PK-WUTAI": "Wutai Shan",
    "PK-CHANGBAI": "Paektu-san", "PK-HUASHAN": "Hua Shan", "PK-WUZHI": "Wuzhi Shan",
}


# Lacs : nom Natural Earth (champ name) des contours de ne_10m_lakes.
LAKES = {
    "LK-QINGHAI": "Qinghai Hu", "LK-POYANG": "Poyang Hu", "LK-DONGTING": "Dongting Hu", "LK-TAIHU": "Tai Hu",
    "LK-HONGZE": "Hongze Hu", "LK-CHAOHU": "Chao Hu", "LK-GAOYOU": "Gaoyou Hu", "LK-WEISHAN": "Weishan Hu",
    "LK-HONGHU": "Hong Hu", "LK-HULUN": "Hulun Nuur", "LK-XINGKAI": "Lake Khanka", "LK-NAMCO": "Nam Co",
    "LK-SILING": "Siling Co", "LK-YAMZHO": "Yamzho Yumco", "LK-MAPAM": "Mapam Yumco", "LK-GYARING": "Gyaring Hu",
    "LK-NGORING": "Ngoring Hu", "LK-BOSTEN": "Bosten Hu", "LK-EBINUR": "Ebinur Hu",
    "LK-DANJIANGKOU": "Danjiangkou Shuiku", "LK-QIANDAO": "Xinanjiang Shuiku",
}

# Parties sans nom dans Natural Earth à rattacher à un lac : cadre [lon min, lon max, lat min, lat max].
# Le Dongting Hu y est en trois morceaux : seul le Dongting oriental porte un nom.
LAKE_EXTRA_PARTS = {
    "LK-DONGTING": (112.0, 113.2, 28.7, 29.6),
}


def polygons_of(geometry, tolerance):
    """Polygones GeoJSON -> anneaux simplifiés en unités de carte (anneaux trop petits supprimés)."""
    polys = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
    polygons = []
    for poly in polys:
        rings = []
        for ring in poly:
            pts = douglas_peucker([to_units(lon, lat) for lon, lat in ring], tolerance)
            if len(pts) >= 4:
                rings.append(pts[:-1] if pts[0] == pts[-1] else pts)
        if rings:
            polygons.append(rings)
    return polygons


def load(path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def to_units(lon, lat):
    """Longitude/latitude -> repère de la carte (unités de 100 m, y vers le bas), comme map.geometry.json."""
    x, y = project(key(lon, lat))
    return round(x / GRID_KM), -round(y / GRID_KM)


class China:
    """Test « dans la Chine, ou à moins de [BORDER_BUFFER] » sur les contours des provinces déjà générés."""

    CELL = 500

    def __init__(self, geometry):
        self.provinces = {}
        self.grid = defaultdict(list)
        for f in geometry["features"]:
            rings = [list(zip(r[0::2], r[1::2])) for p in f["polygons"] for r in p]
            self.provinces[f["id"]] = rings
            for ring in rings:
                for a, b in zip(ring, ring[1:] + ring[:1]):
                    for cell in self._cells(a, b):
                        self.grid[cell].append((a, b))

    def _cells(self, a, b):
        x0, x1 = sorted((a[0], b[0]))
        y0, y1 = sorted((a[1], b[1]))
        for cx in range(int(x0 // self.CELL), int(x1 // self.CELL) + 1):
            for cy in range(int(y0 // self.CELL), int(y1 // self.CELL) + 1):
                yield cx, cy

    def province_of(self, x, y):
        for pid, rings in self.provinces.items():
            if point_in_rings(rings, x, y):
                return pid
        return None

    def distance_to_border(self, x, y):
        best = float("inf")
        cx, cy = int(x // self.CELL), int(y // self.CELL)
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                for (ax, ay), (bx, by) in self.grid.get((cx + dx, cy + dy), ()):
                    vx, vy = bx - ax, by - ay
                    l2 = vx * vx + vy * vy
                    t = 0.0 if l2 == 0 else max(0.0, min(1.0, ((x - ax) * vx + (y - ay) * vy) / l2))
                    best = min(best, math.hypot(x - ax - t * vx, y - ay - t * vy))
        return best

    def near(self, x, y, buffer=BORDER_BUFFER):
        return self.province_of(x, y) is not None or self.distance_to_border(x, y) <= buffer


def lines_of(geometry):
    if geometry["type"] == "LineString":
        return [geometry["coordinates"]]
    return geometry["coordinates"]


def centroid_in(coords, box):
    if box is None:
        return True
    lon = sum(c[0] for c in coords) / len(coords)
    lat = sum(c[1] for c in coords) / len(coords)
    return box[0] <= lon <= box[1] and box[2] <= lat <= box[3]


def build_river(china, features, names, box):
    """Tronçons d'un cours d'eau, coupés à la frontière, simplifiés, en unités de carte."""
    runs = []
    for f in features:
        if f["properties"].get("name") not in names:
            continue
        for line in lines_of(f["geometry"]):
            if not centroid_in(line, box):
                continue
            current = []
            for lon, lat in line:
                p = to_units(lon, lat)
                if china.near(*p):
                    current.append(p)
                else:
                    if len(current) >= 2:
                        runs.append(current)
                    current = []
            if len(current) >= 2:
                runs.append(current)
    return [douglas_peucker(run, RIVER_TOLERANCE) for run in runs]


def polyline_length(points):
    return sum(math.hypot(b[0] - a[0], b[1] - a[1]) for a, b in zip(points, points[1:]))


def line_label(runs):
    longest = max(runs, key=polyline_length)
    mid = longest[len(longest) // 2]
    return [round(mid[0]), round(mid[1]), round(polyline_length(longest) / 9)]


def main() -> int:
    geometry = load(ASSETS / "map.geometry.json")
    china = China(geometry)
    # Les lieux tirés d'OpenStreetMap (petits lacs) sont construits par build_osm_geometry.py.
    meta = {e["id"]: e for name in ("cities", "rivers", "mountains", "lakes") for e in load(ASSETS / f"{name}.json")["entities"]
            if e["id"] not in OSM_IDS}
    errors = []
    out = []

    # --- Villes -------------------------------------------------------------------------
    places = load(RAW / "ne_10m_populated_places.geojson")["features"]
    for cid, zh in CITY_ZH.items():
        matches = [p for p in places if p["properties"].get("NAME_ZH") == zh and p["properties"].get("ADM0_A3") in ("CHN", "HKG", "TWN")]
        if len(matches) != 1:
            errors.append(f"{cid} : {len(matches)} correspondance(s) pour {zh}")
            continue
        lon, lat = matches[0]["geometry"]["coordinates"][:2]
        out.append((cid, "point", to_units(lon, lat)))
    for pid, (lon, lat) in MANUAL_POINTS.items():
        out.append((pid, "point", to_units(lon, lat)))

    for cid in [i for i in meta if i.startswith("CT-")]:
        point = next((g for i, k, g in out if i == cid), None)
        if point is None:
            errors.append(f"{cid} : pas de coordonnées")
            continue
        found = china.province_of(*point)
        expected = meta[cid]["parentId"]
        if found != expected and china.distance_to_border(*point) > CITY_PROVINCE_SLACK:
            errors.append(f"{cid} : dans {found}, attendu {expected}")

    # --- Cours d'eau --------------------------------------------------------------------
    river_features = load(RAW / "ne_10m_rivers_lake_centerlines.geojson")["features"]
    for rid, (names, box) in RIVERS.items():
        runs = build_river(china, river_features, set(names), box)
        if not runs:
            errors.append(f"{rid} : aucun tronçon en Chine")
            continue
        out.append((rid, "line", runs))

    # --- Montagnes ----------------------------------------------------------------------
    regions = load(RAW / "ne_10m_geography_regions_polys.geojson")["features"]
    for mid, ne_name in RANGES.items():
        f = next((f for f in regions if f["properties"].get("NAME") == ne_name), None)
        if f is None:
            errors.append(f"{mid} : zone {ne_name} introuvable")
            continue
        out.append((mid, "polygon", polygons_of(f["geometry"], RANGE_TOLERANCE)))

    elevation = load(RAW / "ne_10m_geography_regions_elevation_points.geojson")["features"]
    for pid, ne_name in PEAKS.items():
        f = next((f for f in elevation if f["properties"].get("name") == ne_name), None)
        if f is None:
            errors.append(f"{pid} : sommet {ne_name} introuvable")
            continue
        lon, lat = f["geometry"]["coordinates"][:2]
        out.append((pid, "point", to_units(lon, lat)))

    # --- Lacs -------------------------------------------------------------------------------
    lakes = load(RAW / "ne_10m_lakes.geojson")["features"]
    for lid, ne_name in LAKES.items():
        matches = [f for f in lakes if f["properties"].get("name") == ne_name]
        if len(matches) != 1:
            errors.append(f"{lid} : {len(matches)} contour(s) {ne_name}")
            continue
        polygons = polygons_of(matches[0]["geometry"], LAKE_TOLERANCE)
        box = LAKE_EXTRA_PARTS.get(lid)
        if box:
            for f in lakes:
                ring = (f["geometry"]["coordinates"][0] if f["geometry"]["type"] == "Polygon"
                        else f["geometry"]["coordinates"][0][0])
                if not f["properties"].get("name") and centroid_in(ring, box):
                    polygons += polygons_of(f["geometry"], LAKE_TOLERANCE)
        out.append((lid, "polygon", polygons))
        # Le lac est dans sa province : on teste son point d'étiquette (lac Khanka : partie chinoise).
        x, y = label_point(polygons)[:2]
        found = china.province_of(x, y)
        if found != meta[lid]["parentId"] and china.distance_to_border(x, y) > LAKE_PROVINCE_SLACK:
            errors.append(f"{lid} : centre dans {found}, attendu {meta[lid]['parentId']}")

    # --- Contrôles ----------------------------------------------------------------------
    ids = [i for i, _, _ in out]
    if sorted(ids) != sorted(meta):
        errors.append(f"identifiants : manquants {sorted(set(meta) - set(ids))}, en trop {sorted(set(ids) - set(meta))}")
    for i, kind, g in out:
        if kind == "point" and not china.near(*g, buffer=2 * BORDER_BUFFER):
            errors.append(f"{i} : hors de Chine")
    if errors:
        print("ERREURS :\n  " + "\n  ".join(errors))
        return 1

    # Sommets situés dans une zone de chaîne : aide à renseigner parentId dans mountains.json.
    ranges = {i: g for i, k, g in out if k == "polygon"}
    for i, kind, g in out:
        if i.startswith("PK-"):
            inside = [r for r, polys in ranges.items() if any(point_in_rings(p, *g) for p in polys)]
            declared = meta[i].get("parentId")
            if inside or declared:
                print(f"  {i} dans {inside or '-'} (parentId déclaré : {declared or '-'})")

    # --- Écriture -------------------------------------------------------------------------
    lines = []
    for i, kind, g in sorted(out):
        if kind == "point":
            feature = {"id": i, "kind": "point", "point": list(g)}
        elif kind == "line":
            feature = {"id": i, "kind": "line", "label": line_label(g),
                       "lines": [[c for p in run for c in p] for run in g]}
        else:
            feature = {"id": i, "kind": "polygon", "label": label_point(g),
                       "polygons": [[[c for p in ring for c in p] for ring in poly] for poly in g]}
        lines.append(json.dumps(feature, separators=(",", ":")))
    header = {
        "schemaVersion": 1, "sourceId": SOURCE_ID,
        "projection": {"name": "albers", "lon0": LON0, "lat0": LAT0, "lat1": LAT1, "lat2": LAT2},
        "unitKm": GRID_KM, "yAxis": "down", "bounds": geometry["bounds"],
    }
    text = json.dumps(header)[:-1] + ',\n"features":[\n' + ",\n".join(lines) + "\n]}\n"
    OUT_FILE.write_text(text, encoding="utf-8", newline="\n")

    counts = defaultdict(int)
    for _, kind, _ in out:
        counts[kind] += 1
    vertices = sum(len(run) for _, k, g in out if k == "line" for run in g)
    print(f"{len(out)} lieux ({dict(counts)}), {vertices} sommets de rivières, fichier {OUT_FILE.stat().st_size // 1024} Ko")
    return 0


if __name__ == "__main__":
    sys.exit(main())
