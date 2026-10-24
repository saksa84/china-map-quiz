# Plan initial — China Map Quiz

Rédigé le 2026-10-24 (Première mission, section 15 du CLAUDE.md). Document vivant : les décisions prises sont reportées dans la section « Décisions ».

## 1. État du projet

- Dossier `C:\Code\China map quiz` : uniquement `CLAUDE.md` et `liste_composant_pc.txt`. Pas encore de dépôt Git, pas de code.
- Machine : Ryzen 5 7500F, Radeon RX 7800 XT, Windows 11 Pro. Virtualisation active (l'émulateur fonctionnera avec accélération).

## 2. Prérequis logiciels

| Élément | État | Remarque |
|---|---|---|
| Android Studio 2026.2.1 | ✅ installé | Fournit son propre JDK (JBR 25) |
| SDK Platform API 37 | ✅ | `compileSdk = 37` |
| Build-tools 36.0.0 | ✅ | Minimum requis par AGP 9.4 |
| Image système API 37 x86_64 + AVD « Medium Phone » | ✅ | Émulateur prêt |
| Git | ✅ | Dépôt à initialiser à l'étape 1 |
| Python 3 | à vérifier | Uniquement pour les scripts de préparation des données (étape 3) |
| Compte GitHub | à confirmer | Uniquement au moment de publier |

Versions stables vérifiées le 2026-10-24 :

| Outil | Version |
|---|---|
| Android Gradle Plugin | 9.4.0 (Gradle 9.6.0, JDK 17+, API max 37) |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |

Recommandation : créer le squelette avec l'assistant d'Android Studio (« Empty Activity »), qui choisit automatiquement des versions compatibles entre elles. On ajuste ensuite.

## 3. Architecture proposée

Un seul module Gradle `app`, découpé en packages. Plusieurs modules Gradle seraient surdimensionnés pour une application personnelle.

```
app/src/main/
  assets/geo/                  ← données, séparées du code
    map.geometry.json          géométries simplifiées et déjà projetées (généré)
    provinces.meta.json        id, pinyin, alias, hanzi, type, capitale, source
  java/.../chinamapquiz/
    data/        modèles (GeoEntity, AdminLevel), chargement JSON, validation
    map/         rendu Canvas, zoom/déplacement, détection de la région touchée
    game/        moteur commun : question, réponse, session, normalisation du pinyin
    game/modes/  Learn, Find, Type, Eliminate (logique pure, sans Android)
    progress/    sauvegarde DataStore
    ui/          écrans Compose + thème
    navigation/  graphe de navigation
  res/values/strings.xml       textes de l'interface (traduisibles plus tard)
tools/                         scripts Python : GeoJSON source → assets
docs/                          plan, sources de données, licences, compilation
```

Règle clé : `game/` et `data/` (hors lecture des fichiers) ne dépendent pas d'Android. Ils se testent avec de simples tests JUnit en quelques secondes, sans émulateur.

### Choix du rendu de la carte

| Option | Avantages | Inconvénients |
|---|---|---|
| **Canvas Compose + polygones pré-projetés** | Aucune dépendance. Contrôle total des couleurs et de la détection du toucher. Zoom et déplacement natifs (`detectTransformGestures`). | Il faut écrire la projection et la détection du toucher (environ 200 lignes). |
| SVG (AndroidSVG ou VectorDrawable) | Fichier facile à visualiser | Identifier la région touchée est difficile. Colorer chaque région dynamiquement est peu pratique. |
| MapLibre (cartes vectorielles hors ligne) | Moteur cartographique complet | Lourd. Il faut générer des tuiles. Disproportionné pour 34 polygones. |

**Recommandation : Canvas.** Les coordonnées sont projetées une fois pour toutes par le script Python, l'application ne fait que dessiner. Pour détecter la région touchée, on applique la transformation inverse (zoom/déplacement) puis un test point-dans-polygone, en commençant par les boîtes englobantes. Pour les petites régions (Hong Kong, Macao, municipalités), on prévoit une tolérance de toucher en pixels et, si besoin, un marqueur cliquable.

### Préparation des données

Script Python dans `tools/` :
1. lire le GeoJSON source ;
2. simplifier les géométries de manière contrôlée en conservant la topologie, pour éviter les trous et chevauchements aux frontières ;
3. projeter les coordonnées (conique conforme ou d'Albers centrée sur la Chine, pour éviter l'aplatissement des régions du nord) ;
4. écrire un JSON compact dans `assets/geo/` ;
5. vérifier que les identifiants sont uniques, que tous les éléments de `meta` ont une géométrie, et l'inverse.

Le script est relancé à chaque changement de source. Toutes les transformations sont consignées dans `docs/data-sources.md`.

## 4. Sources cartographiques évaluées

| Source | Niveau 1 | Niveau 2 | Licence | Remarques |
|---|---|---|---|---|
| **geoBoundaries (gbOpen CHN)** | ADM1 : 34 unités, 2019 | ADM2 : 2 391 unités, 2017 | ADM1 : domaine public. ADM2 : ODC-PDDL 1.0 | Les 34 unités correspondent au périmètre « 34 divisions ». La source ADM1 citée (Wikimedia Commons) est peu précise. **L'ADM2 semble être au niveau des comtés, pas des préfectures (environ 333)** : à vérifier avant l'étape niveau 2. |
| **Natural Earth Admin 1 (10m)** | Oui | Non | Domaine public, attribution facultative | Politique « de facto » par défaut : Taïwan est un pays séparé. Champs `name`, `iso_3166_2`, noms en chinois. Résolution moyenne. |
| OpenStreetMap | Oui | Oui | ODbL : attribution obligatoire, partage à l'identique des bases dérivées | Données riches, mais l'extraction est complexe et la licence plus contraignante. |
| GADM | Oui | Oui | **Usage non commercial, redistribution interdite** | ❌ À exclure : incompatible avec un dépôt GitHub public. |
| DataV / Aliyun (GeoJSON chinois) | Oui | Oui | Conditions de réutilisation non claires | ❌ À exclure tant que la licence n'est pas établie. |

**Recommandation pour la V1 : geoBoundaries ADM1** (34 unités, domaine public). À recouper avec Natural Earth pour vérifier les tracés et les noms. Aucune donnée ne sera téléchargée dans le projet avant ta validation.

## 5. Décisions ouvertes

1. **Couverture géographique** : traitement de Taïwan, Hong Kong et Macao. Le choix sera inscrit dans la configuration et dans `docs/data-sources.md`.
2. **Source de la carte** : voir la recommandation ci-dessus.
3. **Convention des noms** : pinyin sans tons (« Nei Mongol » ou « Inner Mongolia » ? « Xizang » ou « Tibet » ?). Proposition : le pinyin est le nom principal ; les exonymes anglais ou français courants sont des alias acceptés dans le mode Type.
4. **Langue de l'interface** : français ou anglais. Tous les textes passent par `strings.xml`, donc on peut traduire plus tard.
5. **Identifiants stables** : proposition, les codes ISO 3166-2 (`CN-SC` pour Sichuan, `CN-BJ` pour Beijing, etc.), indépendants des noms.
6. **minSdk** : proposition 26 (Android 8.0), qui couvre la quasi-totalité des téléphones actuels.
7. **Nom de paquet (`applicationId`)** : à fixer avant le premier build, difficile à changer ensuite. Par exemple `io.github.<pseudo>.chinamapquiz`.
8. **Règles du mode Eliminate** : à définir avant l'étape 7.

## 6. Plan de développement

Chaque étape compile, est testée et se termine par un commit si tu le demandes.

| # | Étape | Résultat vérifiable |
|---|---|---|
| 1 | Squelette : projet Android Studio, Git, `.gitignore`, README | L'app « Hello » se lance sur l'émulateur |
| 2 | Modèle de données et chargement JSON (avec 3 régions factices) | Tests unitaires : chargement, unicité des identifiants, hiérarchie |
| 3 | Pipeline de données : script Python → `assets/geo/` pour les 34 unités, `docs/data-sources.md` | JSON généré et validé, licences consignées |
| 4 | **Prototype carte** : rendu Canvas, zoom/déplacement, toucher → nom en pinyin | **Objectif V1** atteint sur téléphone |
| 5 | Moteur de jeu + mode **Find** | Tests : génération de questions, fin de session |
| 6 | Mode **Type** + normalisation du pinyin et des alias | Tests de normalisation |
| 7 | Mode **Eliminate** (règles définies avant) | Tests de fin de partie |
| 8 | Mode **Learn** enrichi (capitale, type de division) | — |
| 9 | Progression DataStore + niveaux configurables | Tests de progression |
| 10 | Accueil, navigation, résultats, paramètres | Parcours complet |
| 11 | APK release signé (clé hors Git) + publication GitHub | APK installé sur le téléphone |
| 12+ | Niveau 2 (préfectures), niveau 3 (villes), modes futurs | — |

**Avancement au 2026-10-24** : les étapes 1 à 10 sont faites.
- Données des 34 unités et carte interactive.
- Mode Apprendre, avec les noms sur la carte et la vue grandes régions.
- Modes Trouver, Saisir et Éliminer, avec niveaux, résultat et révision des erreurs.
- Progression sauvegardée : records, temps, étoiles et lieux les plus manqués.
- Écrans Progression et Paramètres, icône de l'application.

Prochaines étapes :
- étape 11 : APK release signé et publication GitHub ;
- puis la suite de la version 1 : grandes villes, fleuves et montagnes, et le mode Contours.

## 7. Périmètre de la version minimale (V1)

Inclus : les 34 unités provinciales (selon la décision 1), le mode Learn simple (toucher → nom en pinyin), zoom et déplacement, fonctionnement 100 % hors ligne, APK debug installable.

Exclu : modes de jeu, progression, villes, préfectures, traduction.

## 8. Premiers tests de validation

- Chargement : le JSON des métadonnées se lit sans erreur ; nombre d'entités attendu.
- Unicité : aucun identifiant n'apparaît deux fois.
- Cohérence : chaque entité de `meta` a une géométrie, et chaque géométrie a des métadonnées.
- Hiérarchie : le parent de chaque entité de niveau 1 est `CN`.
- Noms : aucun nom principal vide ; tous en alphabet latin.
- Détection du toucher : un point connu à l'intérieur de Sichuan renvoie `CN-SC` ; un point en mer ne renvoie rien ; un toucher proche de Macao est accepté dans la tolérance.
- Normalisation (dès l'étape 6) : `"  sichuan "` → `sichuan` ; `"Xi'an"` et `"Xian"` selon les alias déclarés.

## 9. Fichiers créés à l'étape 1

- Projet généré par Android Studio : `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, `gradlew(.bat)` + `gradle/wrapper/`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `MainActivity.kt`, `ui/theme/*`
- Ajoutés : `.gitignore` (Android/Kotlin, exclusion de `local.properties`, `*.jks` et `*.keystore`), `README.md` (ouverture, build, installation), `docs/build.md`, `docs/data-sources.md` (registre des sources et licences, vide au départ)
- `git init` + premier commit (sur ta demande)

## Inspirations à exploiter (captures dans `Inspirations/`, hors Git)

- **Fleuves** (`fleuve.png`) : fond de carte uni et sobre, grands fleuves tracés en bleu soutenu et épais, cours d'eau secondaires en bleu pâle et fin, frontières discrètes. On touche le tracé du fleuve, ce qui demande une tolérance au toucher le long des lignes.
- **Montagnes** (`montagne.png`) : fond en relief stylisé, avec des teintes d'altitude du vert au orange puis au rouge, et des petits triangles pour les sommets. Les chaînes y sont des **zones** de relief qu'on touche, pas de simples lignes. À rediscuter avant l'étape montagnes : nous avions prévu des lignes de crête (CLAUDE.md section 4), et une zone serait plus facile à toucher.
- **Commun** : boutons + et − de zoom à droite de la carte, pratiques à une main, à envisager pour tous les modes.

## Décisions

| Date | Sujet | Décision |
|---|---|---|
| 2026-10-24 | Couverture niveau 1 | 34 unités : 31 continentales + Hong Kong + Macao + Taïwan (référence ISO 3166-2:CN). Choix délibéré, à inscrire dans la configuration et `docs/data-sources.md`. |
| 2026-10-24 | Langue de l'interface | Français, via `strings.xml` (l'anglais pourra être ajouté) |
| 2026-10-24 | Convention des noms | Pinyin sans tons comme nom principal (Xizang, Nei Mongol) ; les exonymes (Tibet, Inner Mongolia) sont des alias acceptés |
| 2026-10-24 | Création du squelette | Assistant Android Studio, « Empty Activity », Kotlin DSL |
| 2026-10-24 | Nom de paquet | `io.github.saksa84.chinamapquiz` (compte GitHub `saksa84`) |
| 2026-10-24 | Versions de contenu | Une seule app enrichie en 3 versions. V1 : grandes régions, 34 unités provinciales, grandes villes, fleuves, montagnes. V2 : parcs, lacs, villes-préfectures. V3 : spécialités culinaires par province. Détail dans CLAUDE.md section 4. |
| 2026-10-24 | Municipalités | Beijing, Shanghai, Tianjin et Chongqing sont incluses (34 unités au total) |
| 2026-10-24 | Grandes régions | Découpage statistique en 6 régions, avec Zhongnan subdivisée en Huazhong et Huanan. Hong Kong et Macao sont dans Huanan, Taïwan dans Huadong, Nei Mongol entièrement dans Huabei. Pas de tracé propre : chaque région est l'union de ses provinces. |
| 2026-10-24 | Grandes villes | Au moins 30, classées par population urbaine (millions d'habitants), avec une source citable. Seuil fixé à l'étape données. |
| 2026-10-24 | Fleuves | Deux catégories : grands fleuves, puis affluents importants (rattachés à leur fleuve) |
| 2026-10-24 | Montagnes | Deux catégories : chaînes (ligne de crête) et sommets (point) |
| 2026-10-24 | Source de la carte | Natural Earth 1:10m v5.1.2 (domaine public) au lieu de geoBoundaries, jugé trop grossier. Voir `docs/data-sources.md`. |
| 2026-10-24 | Noms de Hong Kong et Macao | Nom francophone en principal, pinyin (Xianggang, Aomen) en alias. Le champ `pinyin` devient `name`. |
| 2026-10-24 | Îles Paracel | Affichées comme territoire disputé, sans rattachement, « revendiquées par » CN, TW, VN (champ `claimedBy`) |
| 2026-10-24 | Mode Find : erreur | Une seule chance : en cas d'erreur, la bonne région est montrée et on passe à la question suivante |
| 2026-10-24 | Mode Find : temps | Chronomètre visible pendant la partie, temps affiché sur l'écran de résultat |
| 2026-10-24 | Niveaux | Pas de notoriété. Zones Nord (15) / Sud (16) selon la ligne Qinling–Huaihe, plus Toute la Chine ; variantes continent (31) / complet (35, avec Hong Kong, Macao, Taïwan et les Paracel). Détail dans CLAUDE.md section 7. |
| 2026-10-24 | Modes au menu | Trouver, Éliminer, Saisir, Contours (les autres modes des applications de référence sont écartés) |
| 2026-10-24 | Validation dans Trouver | Toucher pour sélectionner, puis bouton Confirmer ou second toucher sur la même région |
| 2026-10-24 | Résultats | Liste des erreurs en fin de partie et option « rejouer les erreurs » ; le moteur garde le détail pour une révision plus riche plus tard |
| 2026-10-24 | Progression | Sauvegarde locale (DataStore, JSON versionné) du meilleur résultat par mode et niveau : plus de bonnes réponses, puis moins d'erreurs, puis plus rapide. On enregistre aussi le nombre de parties où chaque lieu a été manqué. « Rejouer les erreurs » ne compte pas comme record. Pas de verrouillage de niveaux. |
| 2026-10-24 | Navigation | Pas de bibliothèque de navigation : une simple variable « écran courant » dans le ViewModel, avec le bouton retour géré à la main. Suffisant pour une dizaine d'écrans, à revoir si l'app grandit beaucoup. |
| 2026-10-24 | Inspirations | Captures d'applications commerciales dans `Inspirations/`, exclues de Git |
| 2026-10-24 | Appareil cible | Pixel 8 Pro ; minSdk 26 conservé (le Pixel 8 Pro est bien au-dessus) |
