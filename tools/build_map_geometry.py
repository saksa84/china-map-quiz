"""Construit la géométrie du fond de carte : 34 unités provinciales et îles Paracel.

Usage (depuis la racine du projet, après tools/download_natural_earth.py) :
    python tools/build_map_geometry.py

Entrées :
    tools/raw/ne_10m_admin_1_states_provinces.geojson  (31 unités de Chine continentale, îles Paracel)
    tools/raw/ne_10m_admin_0_countries.geojson         (Taïwan, Hong Kong, Macao)
    app/src/main/assets/geo/provinces.json             (identifiants attendus)
    app/src/main/assets/geo/islands.json               (identifiants attendus)
Sortie :
    app/src/main/assets/geo/map.geometry.json

Étapes :
    1. sélection des unités et correspondance avec nos identifiants ISO 3166-2 ;
    2. projection conique équivalente d'Albers centrée sur la Chine (en km) ;
    3. simplification de Douglas-Peucker *par frontière partagée* : une frontière commune à
       deux provinces est simplifiée une seule fois, ce qui évite trous et chevauchements ;
    4. suppression des îlots minuscules, quantification à 100 m, axe y orienté vers le bas ;
    5. vérifications (identifiants, polygones vides, voisinages) et écriture.

Uniquement la bibliothèque standard de Python : rien à installer.
"""

import json
import math
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RAW_DIR = ROOT / "tools" / "raw"
ASSETS_DIR = ROOT / "app" / "src" / "main" / "assets" / "geo"
META_FILES = [ASSETS_DIR / "provinces.json", ASSETS_DIR / "islands.json"]
OUT_FILE = ASSETS_DIR / "map.geometry.json"

SOURCE_ID = "natural-earth-5.1.2"

# Projection d'Albers (paramètres usuels pour la Chine) : surfaces respectées,
# formes peu déformées entre les parallèles 25°N et 47°N.
LON0, LAT0, LAT1, LAT2 = 105.0, 0.0, 25.0, 47.0
EARTH_RADIUS_KM = 6371.0088

# Écart maximal toléré entre le tracé simplifié et l'original. Carte entière sur un
# Pixel 8 Pro : environ 4 km par pixel ; 0,5 km reste net avec un zoom ×8.
TOLERANCE_KM = 0.5
# Îles plus petites que ce seuil supprimées (sauf la plus grande partie de chaque unité).
MIN_PART_AREA_KM2 = 2.0
# Pas de quantification : les coordonnées sont stockées en entiers de 100 m.
GRID_KM = 0.1

# Unités décrites comme des pays dans Natural Earth (admin 0) -> nos identifiants.
ADMIN0_UNITS = {"TWN": "CN-TW", "HKG": "CN-HK", "MAC": "CN-MO"}
# Codes Natural Earth non ISO -> nos identifiants (territoires disputés, voir docs/data-sources.md).
ADMIN1_RENAMED = {"CN-X01~": "IS-PARACEL"}
# Contours issus de fichiers différents qui ne partagent aucun sommet : on accroche la petite
# unité sur la grande (unité, voisine, distance maximale en km). Voir stitch().
STITCHES = [("CN-MO", "CN-GD", 0.3)]
# Archipels d'îlots : tracé presque intact et aucun îlot supprimé, sinon ils disparaîtraient.
DETAILED_UNITS = {"IS-PARACEL"}
DETAILED_TOLERANCE_KM = 0.05


# --- Lecture ---------------------------------------------------------------------------

def load_json(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def key(lon: float, lat: float):
    """Identité d'un sommet : deux unités voisines partagent exactement les mêmes clés."""
    return round(lon * 1e7), round(lat * 1e7)


def to_polygons(geometry):
    """GeoJSON -> liste de polygones ; polygone = liste d'anneaux ; anneau = liste de clés (non fermé)."""
    if geometry["type"] == "Polygon":
        raw = [geometry["coordinates"]]
    elif geometry["type"] == "MultiPolygon":
        raw = geometry["coordinates"]
    else:
        raise ValueError(f"géométrie inattendue : {geometry['type']}")

    polygons = []
    for polygon in raw:
        rings = []
        for ring in polygon:
            keys = []
            for lon, lat in ring:
                k = key(lon, lat)
                if not keys or keys[-1] != k:
                    keys.append(k)
            if len(keys) > 1 and keys[0] == keys[-1]:
                keys.pop()
            if len(keys) >= 3:
                rings.append(keys)
        if rings:
            polygons.append(rings)
    return polygons


def select_units(expected_ids):
    admin1 = load_json(RAW_DIR / "ne_10m_admin_1_states_provinces.geojson")
    admin0 = load_json(RAW_DIR / "ne_10m_admin_0_countries.geojson")

    units = {}
    for feature in admin1["features"]:
        props = feature["properties"]
        if props["adm0_a3"] != "CHN":
            continue
        iso = ADMIN1_RENAMED.get(props["iso_3166_2"], props["iso_3166_2"])
        if iso not in expected_ids:
            raise SystemExit(f"ERREUR : unité Natural Earth absente des métadonnées : {iso} ({props['name']})")
        units[iso] = to_polygons(feature["geometry"])

    for feature in admin0["features"]:
        our_id = ADMIN0_UNITS.get(feature["properties"]["ADM0_A3"])
        if our_id:
            units[our_id] = to_polygons(feature["geometry"])
    return units


# --- Projection ------------------------------------------------------------------------

_n = (math.sin(math.radians(LAT1)) + math.sin(math.radians(LAT2))) / 2
_c = math.cos(math.radians(LAT1)) ** 2 + 2 * _n * math.sin(math.radians(LAT1))
_rho0 = EARTH_RADIUS_KM * math.sqrt(_c - 2 * _n * math.sin(math.radians(LAT0))) / _n


def project(k):
    """Clé (lon, lat) -> (x, y) en km, y vers le nord."""
    lon, lat = k[0] / 1e7, k[1] / 1e7
    rho = EARTH_RADIUS_KM * math.sqrt(_c - 2 * _n * math.sin(math.radians(lat))) / _n
    theta = _n * math.radians(lon - LON0)
    return rho * math.sin(theta), _rho0 - rho * math.cos(theta)


# --- Recollage de frontières -------------------------------------------------------------

def stitch(units, small_id, big_id, threshold_km):
    """Fait coïncider la frontière de `small_id` avec celle de `big_id`.

    1. Chaque sommet de la petite unité à moins de `threshold_km` d'une arête de la grande est
       déplacé sur cette arête ; le point obtenu est aussi inséré dans le contour de la grande.
    2. Entre deux sommets ainsi accrochés, la petite unité reprend exactement le tracé de la
       grande. La frontière devient un arc commun, simplifié une seule fois comme les autres.
    Renvoie le nombre de sommets accrochés.
    """
    big_rings = [ring for polygon in units[big_id] for ring in polygon]
    big_projected = [[project(k) for k in ring] for ring in big_rings]
    insertions = defaultdict(list)  # (anneau, segment) -> [(t, clé)]
    snapped = 0

    for polygon in units[small_id]:
        for ring in polygon:
            for i, k in enumerate(ring):
                px, py = project(k)
                best = None
                for ri, pring in enumerate(big_projected):
                    n = len(pring)
                    for si in range(n):
                        (ax, ay), (bx, by) = pring[si], pring[(si + 1) % n]
                        dx, dy = bx - ax, by - ay
                        length_sq = dx * dx + dy * dy
                        t = 0.0 if length_sq == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length_sq))
                        d = math.hypot(px - (ax + t * dx), py - (ay + t * dy))
                        if d <= threshold_km and (best is None or d < best[0]):
                            best = (d, ri, si, t)
                if best is None:
                    continue
                _, ri, si, t = best
                a, b = big_rings[ri][si], big_rings[ri][(si + 1) % len(big_rings[ri])]
                if t < 0.01:
                    q = a
                elif t > 0.99:
                    q = b
                else:
                    q = (round(a[0] + (b[0] - a[0]) * t), round(a[1] + (b[1] - a[1]) * t))
                    insertions[(ri, si)].append((t, q))
                ring[i] = q
                snapped += 1

    # Insertion des points accrochés dans les arêtes de la grande unité, dans l'ordre.
    for ri, ring in enumerate(big_rings):
        rebuilt = []
        for si, k in enumerate(ring):
            rebuilt.append(k)
            for _, q in sorted(insertions.get((ri, si), [])):
                if rebuilt[-1] != q:
                    rebuilt.append(q)
        ring[:] = rebuilt

    # Entre deux sommets accrochés consécutifs, reprendre le tracé exact de la grande unité.
    position = {k: (ri, idx) for ri, ring in enumerate(big_rings) for idx, k in enumerate(ring)}
    for polygon in units[small_id]:
        for ring in polygon:
            rebuilt = []
            n = len(ring)
            for i, k in enumerate(ring):
                if not rebuilt or rebuilt[-1] != k:
                    rebuilt.append(k)
                nxt = ring[(i + 1) % n]
                if k in position and nxt in position and position[k][0] == position[nxt][0]:
                    ri, ia = position[k]
                    ib = position[nxt][1]
                    size = len(big_rings[ri])
                    forward, backward = (ib - ia) % size, (ia - ib) % size
                    step, count = (1, forward) if forward <= backward else (-1, backward)
                    if 1 < count <= 200:  # au-delà, ce n'est plus une frontière commune
                        rebuilt.extend(big_rings[ri][(ia + step * j) % size] for j in range(1, count))
            while len(rebuilt) > 1 and rebuilt[0] == rebuilt[-1]:
                rebuilt.pop()
            ring[:] = rebuilt
    return snapped


# --- Simplification respectant les frontières partagées ---------------------------------

def find_junctions(all_rings):
    """Sommets où plus de deux segments distincts se rencontrent : début ou fin d'une frontière commune."""
    neighbours = defaultdict(set)
    for ring in all_rings:
        n = len(ring)
        for i, p in enumerate(ring):
            neighbours[p].add(ring[i - 1])
            neighbours[p].add(ring[(i + 1) % n])
    return {p for p, s in neighbours.items() if len(s) > 2}


def split_into_arcs(ring, junctions):
    """Découpe un anneau en arcs allant d'une jonction à la suivante (extrémités incluses)."""
    cuts = [i for i, p in enumerate(ring) if p in junctions]
    if not cuts:
        # Anneau isolé (île) : départ canonique pour que deux anneaux identiques donnent le même arc.
        start = ring.index(min(ring))
        rotated = ring[start:] + ring[:start]
        return [rotated + [rotated[0]]]
    rotated = ring[cuts[0]:] + ring[:cuts[0]]
    arcs, current = [], [rotated[0]]
    for p in rotated[1:]:
        current.append(p)
        if p in junctions:
            arcs.append(current)
            current = [p]
    current.append(rotated[0])
    arcs.append(current)
    return arcs


def douglas_peucker(points, tolerance):
    """Garde les extrémités et les sommets qui s'écartent de plus de `tolerance` de la corde."""
    if len(points) < 3:
        return points
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        first, last = stack.pop()
        (x1, y1), (x2, y2) = points[first], points[last]
        dx, dy = x2 - x1, y2 - y1
        length = math.hypot(dx, dy)
        best, best_i = -1.0, -1
        for i in range(first + 1, last):
            px, py = points[i]
            if length == 0:
                d = math.hypot(px - x1, py - y1)
            else:
                d = abs(dy * px - dx * py + x2 * y1 - y2 * x1) / length
            if d > best:
                best, best_i = d, i
        if best > tolerance:
            keep[best_i] = True
            stack.append((first, best_i))
            stack.append((best_i, last))
    return [p for p, k in zip(points, keep) if k]


def ring_area(points):
    return abs(sum(x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(points, points[1:] + points[:1]))) / 2


def simplify_units(units):
    all_rings = [ring for polygons in units.values() for polygon in polygons for ring in polygon]
    junctions = find_junctions(all_rings)
    cache = {}
    arc_owners = defaultdict(set)  # arc canonique -> unités qui l'utilisent (pour les voisinages)

    def simplified_arc(arc, unit_id):
        t = tuple(arc)
        canonical = min(t, t[::-1])
        arc_owners[canonical].add(unit_id)
        if canonical not in cache:
            projected = [project(p) for p in canonical]
            tolerance = DETAILED_TOLERANCE_KM if unit_id in DETAILED_UNITS else TOLERANCE_KM
            cache[canonical] = douglas_peucker(projected, tolerance)
        result = cache[canonical]
        return result if t == canonical else result[::-1]

    output = {}
    for unit_id, polygons in units.items():
        kept = []
        for polygon in polygons:
            rings = []
            for ring_index, ring in enumerate(polygon):
                points = []
                for arc in split_into_arcs(ring, junctions):
                    points.extend(simplified_arc(arc, unit_id)[:-1])
                quantized = []
                for x, y in points:
                    q = (round(x / GRID_KM), -round(y / GRID_KM))  # y vers le bas, comme à l'écran
                    if not quantized or quantized[-1] != q:
                        quantized.append(q)
                if len(quantized) > 1 and quantized[0] == quantized[-1]:
                    quantized.pop()
                if len(quantized) >= 3:
                    rings.append(quantized)
                elif ring_index == 0:
                    break  # contour extérieur effondré : on abandonne ce polygone
            if rings and len(rings[0]) >= 3:
                kept.append(rings)

        areas = [ring_area(p[0]) * GRID_KM ** 2 for p in kept]
        largest = max(range(len(kept)), key=lambda i: areas[i]) if kept else -1
        output[unit_id] = [
            p for i, p in enumerate(kept)
            if unit_id in DETAILED_UNITS or i == largest or areas[i] >= MIN_PART_AREA_KM2
        ]

    neighbours = defaultdict(set)
    for owners in arc_owners.values():
        for a in owners:
            neighbours[a] |= owners - {a}
    return output, neighbours


# --- Points d'étiquette -----------------------------------------------------------------

def point_in_rings(rings, x, y):
    inside = False
    for ring in rings:
        j = len(ring) - 1
        for i in range(len(ring)):
            (xi, yi), (xj, yj) = ring[i], ring[j]
            if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi) + xi:
                inside = not inside
            j = i
    return inside


def distance_to_rings(rings, x, y):
    best = float("inf")
    for ring in rings:
        for (ax, ay), (bx, by) in zip(ring, ring[1:] + ring[:1]):
            dx, dy = bx - ax, by - ay
            length_sq = dx * dx + dy * dy
            t = 0.0 if length_sq == 0 else max(0.0, min(1.0, ((x - ax) * dx + (y - ay) * dy) / length_sq))
            best = min(best, math.hypot(x - ax - t * dx, y - ay - t * dy))
    return best


def label_point(polygons, grid=24):
    """Point où écrire le nom [x, y, distance au bord] : dans la plus grande partie, le plus loin possible des bords.

    Le centre de gravité tombe parfois hors de la forme (Gansu, Nei Mongol) : on cherche donc
    sur une grille le point intérieur le plus éloigné du contour, puis on affine autour.
    """
    largest = max(polygons, key=lambda p: ring_area(p[0]))
    xs = [x for x, _ in largest[0]]
    ys = [y for _, y in largest[0]]
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
    best = ((x0 + x1) / 2, (y0 + y1) / 2, -1.0)
    for _ in range(3):  # trois passes de plus en plus fines autour du meilleur point
        step_x, step_y = (x1 - x0) / grid, (y1 - y0) / grid
        for i in range(grid + 1):
            for j in range(grid + 1):
                x, y = x0 + i * step_x, y0 + j * step_y
                if point_in_rings(largest, x, y):
                    d = distance_to_rings(largest, x, y)
                    if d > best[2]:
                        best = (x, y, d)
        cx, cy = best[0], best[1]
        x0, x1, y0, y1 = cx - 2 * step_x, cx + 2 * step_x, cy - 2 * step_y, cy + 2 * step_y
    # Troisième valeur : distance au bord le plus proche, pour savoir si le nom tient à un zoom donné.
    return [round(best[0]), round(best[1]), round(best[2])]


# --- Écriture et vérifications ----------------------------------------------------------

def main() -> int:
    expected = {
        e["id"] for path in META_FILES for e in load_json(path)["entities"] if e["type"] != "country"
    }

    units = select_units(expected)
    missing = expected - units.keys()
    if missing:
        print(f"ERREUR : unités sans géométrie : {sorted(missing)}")
        return 1

    for small_id, big_id, threshold in STITCHES:
        count = stitch(units, small_id, big_id, threshold)
        print(f"recollage {small_id} sur {big_id} : {count} sommets accrochés (seuil {threshold} km)")

    simplified, neighbours = simplify_units(units)
    empty = [u for u, polygons in simplified.items() if not polygons]
    if empty:
        print(f"ERREUR : unités vides après simplification : {empty}")
        return 1

    xs = [x for ps in simplified.values() for p in ps for r in p for x, _ in r]
    ys = [y for ps in simplified.values() for p in ps for r in p for _, y in r]

    header = {
        "schemaVersion": 1,
        "sourceId": SOURCE_ID,
        "projection": {"name": "albers", "lon0": LON0, "lat0": LAT0, "lat1": LAT1, "lat2": LAT2},
        "unitKm": GRID_KM,
        "yAxis": "down",
        "bounds": [min(xs), min(ys), max(xs), max(ys)],
    }
    lines = []
    for unit_id in sorted(simplified):
        polygons = [[[c for point in ring for c in point] for ring in polygon] for polygon in simplified[unit_id]]
        label = label_point(simplified[unit_id])
        lines.append(json.dumps({"id": unit_id, "label": label, "polygons": polygons}, separators=(",", ":")))
    text = json.dumps(header, ensure_ascii=False)[:-1] + ',\n"features":[\n' + ",\n".join(lines) + "\n]}\n"
    OUT_FILE.write_text(text, encoding="utf-8", newline="\n")

    before = sum(len(r) for ps in units.values() for p in ps for r in p)
    after = sum(len(r) for ps in simplified.values() for p in ps for r in p)
    print(f"{len(simplified)} unités, sommets {before} -> {after}, fichier {OUT_FILE.stat().st_size // 1024} Ko")
    for unit_id in sorted(simplified):
        n = sum(len(r) for p in simplified[unit_id] for r in p)
        print(f"  {unit_id}: {len(simplified[unit_id]):2} polygone(s), {n:5} sommets, voisins {sorted(neighbours[unit_id])}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
