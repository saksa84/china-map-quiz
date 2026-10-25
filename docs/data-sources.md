# Sources de données et licences

Registre de toutes les données intégrées à l'application : provenance, version, licence et transformations. À mettre à jour à chaque ajout ou changement de source.

## Registre

| `sourceId` | Contenu | Source et version | Licence | Attribution |
|---|---|---|---|---|
| `natural-earth-5.1.2` | Contours des 34 unités provinciales et des îles Paracel | [Natural Earth](https://www.naturalearthdata.com/) 1:10m, version **v5.1.2** ([dépôt GeoJSON](https://github.com/nvkelso/natural-earth-vector/tree/v5.1.2/geojson)) | Domaine public ([conditions](https://www.naturalearthdata.com/about/terms-of-use/)) | Facultative ; mention « Made with Natural Earth » dans l'écran À propos |
| `manual-iso-3166-2-cn` | Identifiants, types, noms pinyin, alias, hanzi, capitales des 34 unités | Saisie manuelle d'après [ISO 3166-2:CN](https://www.iso.org/obp/ui/#iso:code:3166:CN) et les noms officiels | Faits non protégeables, saisis pour le projet | — |
| `manual-regions` | Composition des 8 grandes régions | Découpage statistique du Bureau national des statistiques de Chine, Zhongnan subdivisée (voir [CLAUDE.md](../CLAUDE.md) section 4) | Faits non protégeables, saisis pour le projet | — |
| `census-2020-urban` | Population urbaine des 38 villes de plus de 3 millions | Recensement chinois 2020, via [List of cities in China by population](https://en.wikipedia.org/wiki/List_of_cities_in_China_by_population) ; coordonnées Natural Earth `populated_places` (Zhongshan saisie à la main : 113,39° E, 22,52° N) | Faits ; Natural Earth domaine public | — |
| `natural-earth-rivers` | Tracés de 12 grands fleuves et 15 affluents, coupés à la frontière avec une marge de 10 km | Natural Earth `rivers_lake_centerlines` v5.1.2 ; rattachements saisis pour le projet | Domaine public | — |
| `natural-earth-mountains` | 17 zones de chaînes et 17 sommets avec altitude | Natural Earth `geography_regions_polys` et `geography_regions_elevation_points` v5.1.2 | Domaine public | — |
| `manual-peaks` | Tai Shan, Huang Shan, Emei Shan (absents de Natural Earth) | Coordonnées et altitudes approximatives d'après les articles encyclopédiques | Faits | — |
| `natural-earth-lakes` | Contours de 19 lacs et 2 lacs de barrage (version 2) | Natural Earth `lakes` v5.1.2 ; superficies et altitudes : voir « Lacs » plus bas | Domaine public | — |
| `osm-parks` | Contours de 14 parcs et sites naturels (version 2), dans `osm.geometry.json` avec les 3 petits lacs | [OpenStreetMap](https://www.openstreetmap.org/), objets listés dans `tools/build_osm_geometry.py`, extraits le 2026-10-25 par l'API Overpass ; statuts d'après les listes de l'UNESCO et l'annonce des parcs nationaux de 2021 | **ODbL 1.0** : `osm.geometry.json` est une base dérivée, sous ODbL (voir « Parcs » plus bas) | **Obligatoire** : « © les contributeurs d'OpenStreetMap » dans l'écran À propos et le README |
| `manual-parks` | Parcs nationaux Tigre et léopard, Forêt tropicale de Hainan, représentés par un point | Centre approximatif du parc, saisi pour le projet (absents d'OSM le 2026-10-25) | Faits | — |
| `osm-lakes` | Xi Hu, Dian Chi, Er Hai (absents de Natural Earth) | Contours OpenStreetMap (relations 2308774, 254775, 254433) dans `osm.geometry.json` ; superficies et altitudes de [Wikidata](https://www.wikidata.org/) (Q502371, Q83640, Q83628), consulté le 2026-10-25 | Contours ODbL 1.0 ; chiffres Wikidata CC0 | « © les contributeurs d'OpenStreetMap » |
| `facts.json` (pas de `sourceId`) | Fiches « En savoir plus » : population, superficie, langues, histoire, économie, plat, anecdotes | Rédigées par Claude à partir de connaissances générales (2026-10-24), **à relire** ; population : recensement chinois 2020 (Hong Kong et Macao 2021, Taïwan estimation 2023) | Texte original du projet | — |
| `manual-disputes` | Statut des territoires disputés (pays revendicateurs) | Saisie manuelle d'après les sources encyclopédiques ([Paracel Islands](https://en.wikipedia.org/wiki/Paracel_Islands)) | Faits non protégeables, saisis pour le projet | — |

Fichiers produits (`app/src/main/assets/geo/`) :

| Fichier | Origine |
|---|---|
| `provinces.json` | Écrit à la main |
| `regions.json` | Écrit à la main |
| `islands.json` | Écrit à la main |
| `map.geometry.json` | **Généré** par `tools/build_map_geometry.py`. Ne pas modifier à la main. |
| `cities.json`, `rivers.json`, `mountains.json` | Écrits à la main (dont les noms connus `knownAs` et les pinyins avec tons `pinyinTones`, saisis d'après les hanzi) |
| `facts.json` | Écrit à la main ; chaque fiche porte `reviewed` (faux tant qu'elle n'est pas relue) |
| `features.geometry.json` | **Généré** par `tools/build_features_geometry.py` : sélection des tronçons, villes et sommets, découpe à la frontière, vérification ville/province. Ne pas modifier à la main. |
| `res/drawable/ic_launcher_foreground.xml` (icône) | **Généré** par `tools/build_icon.py` à partir de `map.geometry.json` |
| `res/raw/answer_correct.wav`, `answer_wrong.wav` (sons de réponse) | **Générés** par `tools/build_sounds.py` : notes synthétisées par le script, aucun enregistrement extérieur ; création du projet, sans licence tierce |

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

## Cours d'eau : tracés et longueurs

### Tracés (`tools/build_features_geometry.py`)

Chaque cours d'eau est l'assemblage de tronçons Natural Earth nommés (dictionnaire `RIVERS`). On ajoute le cours supérieur ou inférieur quand la longueur retenue l'inclut :

| Cours d'eau | Tronçons Natural Earth ajoutés (2026-10-24) |
|---|---|
| Huai He | « Hudi » : cours inférieur, du confluent du Ying au lac Hongze puis au Yangtsé à Sanjiangying |
| Liao He | « Xiliao » (Liao occidental) |
| Tarim He | « Yarkant », source principale dans la convention chinoise |
| Yongding He | « Sanggan », cours supérieur |

Le test `riverSectionsLeaveNoGap` vérifie qu'aucun tronçon n'est isolé de plus de 30 km (il aurait détecté le trou du Qian Jiang dans le Zhu Jiang). Écart maximal mesuré le 2026-10-24 : 0,8 km.

**Limite connue** : Natural Earth ne contient que le cours inférieur du **Min Jiang** (146 km, de Leshan à Yibin). Le cours supérieur (Dujiangyan, Wenchuan) manque ; le tracé reste partiel. La longueur des tracés est en général 10 à 20 % plus courte que la longueur réelle, à cause de la généralisation au 1:10m et de la simplification.

### Longueurs (`lengthKm`, `lengthInChinaKm` dans `rivers.json`)

Les longueurs de fleuves dépendent de la source retenue pour le cours principal. Règle : **valeur de l'infobox de la Wikipédia en chinois** (qui reprend en général les chiffres officiels chinois), révision consultée le 2026-10-24. Exceptions notées dans le tableau. `lengthInChinaKm` est renseigné pour les fleuves qui traversent la frontière ; pas pour l'Amour, qui la longe.

| Identifiant | Longueur (km) | En Chine (km) | Article zh.wikipedia (révision) | Remarque |
|---|---|---|---|---|
| RV-CHANGJIANG | 6 300 | | 长江 (94386981) | |
| RV-HUANGHE | 5 464 | | 黄河 (94788068) | |
| RV-HEILONGJIANG | 2 824 | | 黑龙江 (河流) (93272292) | Cours principal ; 4 344 à 5 498 km selon la source retenue |
| RV-ZHUJIANG | 2 214 | | 珠江 (94491231) | Cours principal du Xi Jiang |
| RV-LANCANGJIANG | 4 350 | 2 139 | 湄公河 (93356851), 澜沧江 (94114875) | Les sources varient de 4 180 à 4 909 km |
| RV-NUJIANG | 2 815 | 2 013 | 怒江 (94281866) | |
| RV-YARLUNGZANGBO | 2 900 | 2 057 | 布拉马普特拉河 (88154453), 雅鲁藏布江 (92828114) | Texte de l'article (infobox : 2 070) |
| RV-HUAIHE | 1 252 | | 淮河 (94005878) | Le même article donne aussi « environ 1 000 km » jusqu'au Yangtsé |
| RV-LIAOHE | 1 345 | | — | Pas de longueur sur zh ; infobox de en.wikipedia « Liao River » |
| RV-HAIHE | 73 | | 海河 (93198103) | **Cours principal** (Commission du Hai He), comme le tracé ; l'infobox (1 122 km) compte le bassin jusqu'à la source la plus lointaine |
| RV-TARIMHE | 2 327 | | 塔里木河 (87541207) | Avec le Yarkant (cours principal seul : 1 321 km) |
| RV-EERQISIHE | 4 248 | 593 | 额尔齐斯河 (93192547) | En Chine : texte de l'article |
| RV-HANJIANG | 1 532 | | — | Pas de longueur sur zh ; infobox de en.wikipedia « Han River (Hubei and Shaanxi) » |
| RV-JIALINGJIANG | 1 119 | | 嘉陵江 (94723143) | |
| RV-MINJIANG | 1 279 | | 岷江 (93776648) | Mesure de l'Académie des sciences (2013) ; 735 km dans la convention ancienne |
| RV-DADUHE | 1 062 | | 大渡河 (92660953) | |
| RV-YALONGJIANG | 1 633 | | 雅砻江 (94369861) | |
| RV-WUJIANG | 1 037 | | 乌江 (94659576) | |
| RV-XIANGJIANG | 948 | | 湘江 (94984896) | Avec la source orientale |
| RV-YUANJIANG | 1 033 | | 沅江 (89754771) | |
| RV-GANJIANG | 991 | | 赣江 (85547540) | Cours principal seul : 751 km |
| RV-WEIHE | 818 | | 渭河 (93668330) | |
| RV-SONGHUAJIANG | 1 725 | | 松花江 (92233287) | Avec le Second Songhua ; 2 309 km avec le Nen |
| RV-WUSULIJIANG | 890 | | 乌苏里江 (93905371) | |
| RV-DONGJIANG | 523 | | 东江 (85546270) | Texte de l'article |
| RV-BEIJIANG | 468 | | 北江 (85546379) | |
| RV-YONGDINGHE | 747 | | 永定河 (94719486) | Texte de l'article, avec le Sanggan |

## Lacs (version 2)

### Contours (`tools/build_features_geometry.py`, dictionnaire `LAKES`)

Chaque lac est le contour Natural Earth `ne_10m_lakes` du même nom, simplifié à 0,5 km. Le **Dongting Hu** est en trois morceaux dans Natural Earth, dont un seul est nommé : les deux autres sont rattachés par un cadre géographique (`LAKE_EXTRA_PARTS`). Le **lac Khanka** (Xingkai Hu) est partagé avec la Russie : comme les chaînes de montagnes, il est coupé à la frontière au dessin, et seule sa partie chinoise est touchable. Le script vérifie que chaque lac tombe dans sa province (le réservoir de Danjiangkou, à cheval sur le Hubei et le Henan, est rattaché au Hubei, où se trouve le barrage).

Les contours Natural Earth datent et ne suivent pas les saisons : le Poyang Hu y est à environ 2 100 km² (basses eaux), le Selin Cuo à 1 600 km² (il s'est agrandi depuis). Le **Weishan Hu** n'y est que le lac Weishan proprement dit, alors que sa superficie (1 266 km²) est celle des quatre lacs Nansi, souvent appelés ensemble « lac Weishan ».

Écarté : Bangong Co, à cheval sur la ligne de contrôle Chine–Inde (territoire disputé).

### Superficies et altitudes (`areaKm2`, `elevation` dans `lakes.json`)

Règle, comme pour les cours d'eau : **Wikipédia en chinois** (infobox ou texte de l'article), consultée le 2026-10-25 ; à défaut, **Wikidata**. La superficie d'un lac varie avec les saisons et les années : la valeur est indicative.

| Identifiant | Superficie (km²) | Altitude (m) | Source |
|---|---:|---:|---|
| LK-QINGHAI | 4 489 | 3 260 | zh 青海湖 (94928825), valeur 2007 |
| LK-POYANG | 3 283 | | zh 鄱阳湖 (92186273), à la cote de 21,71 m à Hukou |
| LK-DONGTING | 2 820 | | zh 洞庭湖 (88682529) |
| LK-TAIHU | 2 338 | | zh 太湖 (94420285) |
| LK-HONGZE | 1 960 | | Wikidata Q31845950 |
| LK-CHAOHU | 775 | | zh 巢湖 (92747516), texte |
| LK-GAOYOU | 760,67 | | zh 高邮湖 (79509755), texte |
| LK-WEISHAN | 1 266 | | zh 微山湖 (94296175), les quatre lacs Nansi |
| LK-HONGHU | 348,2 | | zh 洪湖 (84129158) |
| LK-HULUN | 2 399 | 545 | Wikidata Q537374 ; altitude zh 呼伦湖 (94706863) |
| LK-XINGKAI | 4 190 | 69 | Wikidata Q201843 (lac entier) ; altitude zh 兴凯湖 (92434621) |
| LK-NAMCO | 1 920 | 4 718 | zh 纳木错 (94330020) |
| LK-SILING | 2 437,33 | 4 544 | zh 色林错 (91956217), mesure 2021 |
| LK-YAMZHO | 638 | 4 441 | zh 羊卓雍错 (93373038), texte |
| LK-MAPAM | 395 | 4 588 | Wikidata Q233627 ; altitude zh 玛旁雍错 (95244181) |
| LK-GYARING | 526 | 4 294 | zh 扎陵湖 (91955855), texte |
| LK-NGORING | 610 | 4 272 | zh 鄂陵湖 (94425369), texte |
| LK-BOSTEN | 1 010 | 1 048 | Wikidata Q611206 ; altitude zh 博斯腾湖 (94311353) |
| LK-EBINUR | 500 | 189 | Wikidata Q31845317 ; altitude zh 艾比湖 (91620068) |
| LK-DANJIANGKOU | 1 022,75 | | zh 丹江口水库 (94413291), après la surélévation du barrage |
| LK-QIANDAO | 567,4 | | zh 千岛湖 (94705290) |
| LK-XIHU | 6,5 | 10 | Wikidata Q502371 ; altitude zh 西湖 (94452475) |
| LK-DIANCHI | 297,9 | 1 900 | Wikidata Q83640 |
| LK-ERHAI | 252,91 | 1 972 | Wikidata Q83628 |

## Parcs (version 2)

### Licence : OpenStreetMap, ODbL

Les contours des parcs viennent d'OpenStreetMap, sous **Open Database License 1.0**. Conséquences, appliquées dans le projet :

- **Attribution** : « © les contributeurs d'OpenStreetMap » avec un lien vers <https://www.openstreetmap.org/copyright>, dans l'écran Paramètres → À propos et dans le README.
- **Base dérivée sous la même licence** : `assets/geo/osm.geometry.json` reste sous ODbL. Il est séparé de `features.geometry.json` (Natural Earth, domaine public) pour que chaque fichier garde sa licence ; son en-tête le rappelle.
- **Reproductibilité** : `tools/build_osm_geometry.py --download` refait l'extraction ; la réponse brute (`tools/raw/osm_features.json`) n'est pas versionnée.

Le reste du projet (code, autres données) n'est pas concerné : l'ODbL ne s'étend pas à l'application qui affiche la base.

### Objets retenus

Recherche par nom chinois dans OSM le 2026-10-25 (objets `boundary=national_park` ou `protected_area`, ou `leisure=nature_reserve`). Contours simplifiés à 0,3 km (davantage pour les deux très grands parcs, 600 sommets au plus) ; les miettes de moins de 0,5 % de la plus grande partie sont retirées.

| Identifiant | Objet OSM | Remarque |
|---|---|---|
| PA-SANJIANGYUAN | relation 13373980 (三江源国家公园) | 152 800 km² dessinés (190 700 km² annoncés) |
| PA-PANDA | relation 13556512 (大熊猫国家公园) | Sur le Sichuan, le Shaanxi et le Gansu ; rattaché au Sichuan |
| PA-WUYISHAN | relation 21522962 (武夷山, Mount Wuyi) | Partie du Fujian ; le parc national s'étend aussi au Jiangxi |
| PA-JIUZHAIGOU | relation 7516592 | Entouré par le parc du Panda géant : la plus petite zone l'emporte au toucher |
| PA-HUANGLONG | relation 6213328 | |
| PA-WULINGYUAN | relation 18589298 | |
| PA-LIJIANG | relation 21263564 | Zone centrale du bien UNESCO « Karst de Chine du Sud » à Guilin |
| PA-SHILIN | relation 21669396 | Zone centrale seulement (1 km²) |
| PA-FANJINGSHAN | chemin 626892766 | |
| PA-KANAS | chemin 614666060 | |
| PA-DANXIA | relation 21669142 | |
| PA-LUSHAN | relation 6634714 | |
| PA-SANQINGSHAN | chemin 385707111 | |
| PA-ZHANGYE | chemin 379678356 | Zone touristique du Danxia coloré |
| PA-HUBAO | — | **Absent d'OSM** : point (130,5° E, 43,3° N), entre Hunchun et Wangqing |
| PA-HAINAN | — | **Absent d'OSM** : point (109,6° E, 18,95° N), massif central de l'île |

Écartés faute de contour dans OSM : Shennongjia, Tianchi du Tianshan, Trois rivières parallèles, Xishuangbanna. Ils pourront revenir sous forme de point.

Nouvelle recherche le 2026-10-25 pour les deux parcs nationaux en point : OSM n'a que des points (`东北虎豹国家公园`, nœud 11243074638) et une route touristique autour de la forêt de Hainan, pas de limite. La base mondiale WDPA (Protected Planet) a ces limites mais **interdit la redistribution** ; les cartes officielles chinoises ne sont pas sous licence libre. Ils restent donc en point.

### Statuts (`designations` dans `parks.json`)

On affiche le statut exact : seuls les 5 parcs créés en octobre 2021 sont des **parcs nationaux** ; les autres sont des sites du patrimoine mondial, des géoparcs mondiaux UNESCO, des réserves naturelles nationales ou des zones panoramiques nationales. Les années d'inscription au patrimoine mondial et de label géoparc viennent des listes de l'UNESCO ; elles ont été saisies de mémoire par Claude et **restent à vérifier**. Les zones panoramiques et les réserves n'ont pas d'année.

## Régénérer les données

```powershell
python tools/download_natural_earth.py        # télécharge dans tools/raw/ et vérifie les SHA-256
python tools/build_map_geometry.py            # écrit map.geometry.json et affiche un rapport
python tools/build_features_geometry.py       # écrit features.geometry.json (villes, cours d'eau, montagnes, lacs)
python tools/build_osm_geometry.py --download  # écrit osm.geometry.json (OpenStreetMap, ODbL)
```

Empreintes SHA-256 attendues (v5.1.2) :

| Fichier | SHA-256 |
|---|---|
| `ne_10m_admin_0_countries.geojson` | `239eec57ac17f100a11e2536cffc56752c318b50ae765b0918ff7aab4ce8f255` |
| `ne_10m_admin_1_states_provinces.geojson` | `22d0e3ad85eb3e27f17cabf8ba2d50e554fbc27a87796ff891d958185da62fb5` |
| `ne_10m_lakes.geojson` (version 2) | `2d036f53dedec578001c5c30c2959ee7d4eebc1306900fa4367c49929ec8f2d9` |

Les empreintes des autres fichiers (villes, cours d'eau, montagnes) sont dans `tools/download_natural_earth.py`.
