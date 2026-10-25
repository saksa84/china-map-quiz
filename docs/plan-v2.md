# Plan d'action — version 2 (lacs et parcs)

Rédigé le 2026-10-25, après la publication de la version 1.2 (commit `a93dded`).
Périmètre demandé : **lacs et parcs**. Les villes-préfectures, aussi prévues en version 2, viendront ensuite.

Comme d'habitude : petites étapes, chacune terminée par la compilation, les tests, un essai sur l'émulateur, puis un commit **sur demande**.

## Ce qui existe déjà

Le modèle de données a prévu ces catégories dès la version 1 : `Category.LAKE` et `Category.PARK`, types `lake` et `park` (surfaces) dans `GeoEntity.kt`. Le moteur de jeu, la carte (surfaces, lignes, points), les fiches et la progression par catégorie servent tels quels. Il faut surtout des **données**, deux catégories de menu (`GeoCategory.LAKES`, `PARKS`) et leurs niveaux dans `levels.json`.

## Sources examinées

### Lacs : Natural Earth `ne_10m_lakes` (v5.1.2)

- Même source et même licence que la carte actuelle : **domaine public**. Rien à ajouter au registre des licences, sauf la ligne du fichier.
- Couverture vérifiée le 2026-10-25 : tous les grands lacs chinois y sont, avec leur nom en pinyin et en caractères (`name_zh`) : Qinghai Hu, Poyang Hu, Dongting Hu, Tai Hu, Hongze Hu, Chao Hu, Hulun Hu, Xingkai Hu (lac Khanka, partagé avec la Russie), Nam Co, Siling Co, Yamzho Yumco, Mapam Yumco, Bosten Hu, Ebinur Hu, Gyaring Hu, Ngoring Hu, Gaoyou Hu, Weishan Hu, ainsi que les réservoirs de Danjiangkou et du Xin'anjiang (Qiandao Hu).
- Absents (trop petits à cette échelle) : Xi Hu (Hangzhou), Dian Chi (Kunming), Erhai (Dali), Lugu Hu.
- Écarté : Bangong Co, à cheval sur la ligne de contrôle Chine–Inde (territoire disputé) ; on pourra l'ajouter plus tard avec `claimedBy`.

### Parcs : pas de contours libres

- Les **parcs nationaux officiels** sont 5, tous créés en octobre 2021 : Sanjiangyuan, Panda géant, Tigre et léopard de Chine du Nord-Est, Forêt tropicale de Hainan, Wuyishan. Aucun nouveau n'a été trouvé en 2025-2026.
- Contours : Natural Earth n'en a pas pour la Chine. La base mondiale WDPA (Protected Planet) **interdit la redistribution** : exclue. OpenStreetMap en a certains, sous licence ODbL (attribution et partage à l'identique des données dérivées) : possible, mais plus lourd.
- Beaucoup de lieux appelés « parcs » par les guides (Jiuzhaigou, Zhangjiajie, Guilin…) sont des **zones panoramiques nationales** ou des **sites naturels de l'UNESCO**, pas des parcs nationaux au sens officiel.

## Décisions prises le 2026-10-25

1. **Parcs** : les 5 parcs nationaux officiels **plus une quinzaine de sites naturels célèbres** (Jiuzhaigou, Zhangjiajie, Guilin–Lijiang, Huanglong, Forêt de pierre…). La fiche affiche le statut exact de chacun (parc national, site naturel UNESCO, zone panoramique nationale) : on ne présente pas comme « parc national » ce qui n'en est pas un. Pas de doublon avec les sommets déjà présents (Huang Shan, Tai Shan).
2. **Dessin des parcs** : par une **zone tirée d'OpenStreetMap** (licence ODbL). Conséquences : attribution « © les contributeurs d'OpenStreetMap » dans À propos et `data-sources.md` ; le fichier de contours dérivé (`parks.geometry.json`) est lui-même sous ODbL, ce qui est indiqué dans le registre ; le script d'extraction est versionné pour pouvoir régénérer le fichier. Un parc sans contour exploitable dans OSM est signalé et décidé au cas par cas (point de repli).
3. **Petits lacs célèbres** : Xi Hu, Dian Chi et Erhai ajoutés **par un point** ; les ~20 grands lacs en surface depuis Natural Earth.

## Étapes prévues (après les décisions)

- **A. Lacs.** *Faite le 2026-10-25* : 24 lacs, catégorie « Lacs » (niveaux Ouest et plateaux / Est et Sud / Tous), 147 tests. Restent les fiches (étape C). Détail : télécharger `ne_10m_lakes` (empreinte ajoutée à `tools/download_natural_earth.py`), choisir les lacs, les découper à la frontière (lac Khanka) dans `build_features_geometry.py`, écrire `lakes.json` (noms, alias, hanzi, pinyin avec tons, province, superficie, nom connu), catégorie « Lacs » dans le menu, niveaux, tests sur les vraies données.
- **B. Parcs.** Script d'extraction des contours OpenStreetMap (requête Overpass, simplification, découpe à la frontière), `parks.json` (noms, statut, date de création, province, source), `parks.geometry.json` sous ODbL, catégorie « Parcs », niveaux, tests.
- **C. Fiches.** Fiches « En savoir plus » des nouveaux lieux dans `facts.json`, marquées « à vérifier ».
- **D. Finitions.** Écran d'accueil à 6 catégories, documentation (`data-sources.md`, README, tests), version 2.0.

Sources : [List of national parks of China (Wikipedia)](https://en.wikipedia.org/wiki/List_of_national_parks_of_China), [China completes property rights registration for 5 national parks (gov.cn, 2025)](https://english.www.gov.cn/news/202508/11/content_WS6899a99ec6d0868f4e8f4c16.html), [Five years, five parks (China.org.cn, 2026)](http://www.china.org.cn/2026-06/15/content_118549126.shtml).
