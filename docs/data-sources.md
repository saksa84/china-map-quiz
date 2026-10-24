# Sources de données et licences

Registre de toutes les données intégrées à l'application : provenance, version, licence et transformations. À mettre à jour à chaque ajout ou changement de source.

## Registre

| `sourceId` | Contenu | Source et version | Licence | Attribution |
|---|---|---|---|---|
| `natural-earth-5.1.2` | Contours des 34 unités provinciales et des îles Paracel | [Natural Earth](https://www.naturalearthdata.com/) 1:10m, version **v5.1.2** ([dépôt GeoJSON](https://github.com/nvkelso/natural-earth-vector/tree/v5.1.2/geojson)) | Domaine public ([conditions](https://www.naturalearthdata.com/about/terms-of-use/)) | Facultative ; mention « Made with Natural Earth » dans l'écran À propos |
| `manual-iso-3166-2-cn` | Identifiants, types, noms pinyin, alias, hanzi, capitales des 34 unités | Saisie manuelle d'après [ISO 3166-2:CN](https://www.iso.org/obp/ui/#iso:code:3166:CN) et les noms officiels | Faits non protégeables, saisis pour le projet | — |
| `manual-regions` | Composition des 8 grandes régions | Découpage statistique du Bureau national des statistiques de Chine, Zhongnan subdivisée (voir [CLAUDE.md](../CLAUDE.md) section 4) | Faits non protégeables, saisis pour le projet | — |
| `manual-disputes` | Statut des territoires disputés (pays revendicateurs) | Saisie manuelle d'après les sources encyclopédiques ([Paracel Islands](https://en.wikipedia.org/wiki/Paracel_Islands)) | Faits non protégeables, saisis pour le projet | — |

Fichiers produits (`app/src/main/assets/geo/`) :

| Fichier | Origine |
|---|---|
| `provinces.json` | Écrit à la main |
| `regions.json` | Écrit à la main |
| `islands.json` | Écrit à la main |
| `map.geometry.json` | **Généré** par `tools/build_map_geometry.py`. Ne pas modifier à la main. |
| `res/drawable/ic_launcher_foreground.xml` (icône) | **Généré** par `tools/build_icon.py` à partir de `map.geometry.json` |

## Sources évaluées et écartées

| Source | Raison |
|---|---|
| geoBoundaries gbOpen CHN ADM1 (2019, domaine public) | Tracés trop grossiers (Macao réduit à 5 points, Taïwan à 35), Guangdong nommé « Guangzhou Province », aucun code ISO renseigné |
| GADM | Licence non commerciale, redistribution interdite : incompatible avec un dépôt public |
| DataV / Aliyun | Conditions de réutilisation non établies |
| OpenStreetMap | Licence ODbL plus contraignante et extraction lourde ; à réévaluer pour le niveau préfectoral |

## Choix de couverture (unités provinciales)

- **34 unités** : 22 provinces + Taïwan, 5 régions autonomes, 4 municipalités, 2 régions administratives spéciales. Ce choix suit la liste ISO 3166-2:CN et a été validé le 2026-10-24.
- **Taïwan, Hong Kong, Macao** : Natural Earth les décrit comme des pays (admin 0). Leurs contours sont repris de `ne_10m_admin_0_countries` et rattachés à `CN-TW`, `CN-HK` et `CN-MO`. Les îles de Kinmen et Penghu sont incluses dans Taïwan.
- **Îles Paracel** (`CN-X01~` dans Natural Earth, `IS-PARACEL` chez nous) : affichées comme **territoire disputé**, sans rattachement à un pays, avec la mention « revendiquées par » la Chine, Taïwan et le Vietnam (décision du 2026-10-24, voir CLAUDE.md section 5). Elles ne comptent pas parmi les 34 unités provinciales. Les 6 îlots sont conservés et peu simplifiés (tolérance 50 m).
- **Autres îles de la mer de Chine méridionale** (Spratleys, etc.) : absentes de Natural Earth admin 1, donc non représentées.
- **Frontières extérieures** : celles de Natural Earth, qui suit la politique « de facto » (lignes de contrôle effectif, par exemple dans l'Aksai Chin et en Arunachal Pradesh).

## Transformations (`tools/build_map_geometry.py`)

1. Sélection des 31 unités `adm0_a3 = CHN` d'admin 1 et des îles Paracel, puis de TWN, HKG et MAC d'admin 0. Contrôle que chaque identifiant existe dans `provinces.json` ou `islands.json`, et inversement.
2. **Recollage de Macao sur le Guangdong** : les deux contours viennent de fichiers Natural Earth différents et ne partageaient aucun sommet, ce qui laissait un écart d'environ 100 m visible au zoom. Chaque sommet de Macao à moins de 300 m du Guangdong est accroché sur sa frontière. Entre deux sommets accrochés, Macao reprend exactement le tracé du Guangdong.
3. Projection conique équivalente d'Albers, sphère de rayon 6 371 km, méridien central 105°E, parallèles 25°N et 47°N. Résultat en kilomètres.
4. Simplification de Douglas-Peucker, tolérance **0,5 km**, appliquée **par arc** : chaque frontière commune à deux unités est simplifiée une seule fois et réutilisée des deux côtés, ce qui évite trous et chevauchements.
5. Suppression des îles de moins de **2 km²**, sauf la plus grande partie de chaque unité (Macao est donc conservé).
6. **Point d'étiquette** de chaque lieu, pour écrire son nom sur la carte : le point de la plus grande partie le plus éloigné des bords (recherche sur grille en trois passes), avec cette distance, qui sert à savoir si le nom tient à un zoom donné.
7. Quantification à **100 m** (coordonnées entières) et axe y inversé (y croît vers le bas, comme à l'écran).

Résultat le 2026-10-24 : 35 entités (34 unités et les îles Paracel), 54 214 sommets ramenés à 20 924, fichier de 260 Ko.

### Limites connues

- **Macao / Guangdong** : la frontière terrestre de la péninsule est recollée (étape 2). Le chenal entre Taipa-Coloane et l'île de Hengqin reste ouvert : il existe réellement, mais il paraît plus large qu'en réalité à cause de la résolution de Natural Earth.
- Les contours de Natural Earth 1:10m sont adaptés à une carte nationale, pas à une carte locale détaillée.

## Régénérer les données

```powershell
python tools/download_natural_earth.py        # télécharge dans tools/raw/ et vérifie les SHA-256
python tools/build_map_geometry.py            # écrit map.geometry.json et affiche un rapport
```

Empreintes SHA-256 attendues (v5.1.2) :

| Fichier | SHA-256 |
|---|---|
| `ne_10m_admin_0_countries.geojson` | `239eec57ac17f100a11e2536cffc56752c318b50ae765b0918ff7aab4ce8f255` |
| `ne_10m_admin_1_states_provinces.geojson` | `22d0e3ad85eb3e27f17cabf8ba2d50e554fbc27a87796ff891d958185da62fb5` |
