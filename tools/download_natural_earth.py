"""Télécharge les fichiers Natural Earth utilisés par le projet et vérifie leur empreinte SHA-256.

Usage (depuis la racine du projet) :
    python tools/download_natural_earth.py

Les fichiers vont dans tools/raw/ (ignoré par Git : plusieurs dizaines de Mo, régénérables).
Une empreinte différente signifie que le fichier a changé : on s'arrête plutôt que de
construire la carte sur des données non vérifiées.
"""

import hashlib
import sys
import urllib.request
from pathlib import Path

VERSION = "v5.1.2"
BASE_URL = f"https://raw.githubusercontent.com/nvkelso/natural-earth-vector/{VERSION}/geojson"

FILES = {
    "ne_10m_admin_0_countries.geojson": "239eec57ac17f100a11e2536cffc56752c318b50ae765b0918ff7aab4ce8f255",
    "ne_10m_admin_1_states_provinces.geojson": "22d0e3ad85eb3e27f17cabf8ba2d50e554fbc27a87796ff891d958185da62fb5",
    "ne_10m_populated_places.geojson": "9b8e3de09048ef00dfc70357dbb9fa324493f214b5e0ae4daf1aa79a8d10116b",
    "ne_10m_rivers_lake_centerlines.geojson": "bb854a900ecbd3b408df46d5e16e3e0f974ba55993f9d8b5c26e855273c0905a",
    "ne_10m_geography_regions_polys.geojson": "b7b26e50ea917d3696aec87f932def2bf5f890f5770e441d59c162c6f4c92a77",
    "ne_10m_geography_regions_elevation_points.geojson": "f98a16867867146ec4146d6d4b18c823eeedb2825947de666116cf9a4e3f43cb",
}

RAW_DIR = Path(__file__).resolve().parent / "raw"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def main() -> int:
    RAW_DIR.mkdir(exist_ok=True)
    for name, expected in FILES.items():
        target = RAW_DIR / name
        if target.exists() and sha256(target) == expected:
            print(f"déjà présent : {name}")
            continue
        print(f"téléchargement : {name}")
        urllib.request.urlretrieve(f"{BASE_URL}/{name}", target)
        actual = sha256(target)
        if actual != expected:
            print(f"ERREUR : empreinte inattendue pour {name}\n  attendue {expected}\n  obtenue  {actual}")
            return 1
    print(f"OK : fichiers Natural Earth {VERSION} vérifiés dans {RAW_DIR}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
