# China Map Quiz

Application Android personnelle pour apprendre la géographie de la Chine sur une carte interactive : provinces, grandes régions, villes, fleuves et montagnes. Elle fonctionne entièrement hors ligne, sans compte, sans publicité et sans collecte de données. Les noms sont affichés en pinyin, avec le nom connu en français quand il existe (« Mékong / Lancang Jiang »).

**Version actuelle : 1.1.**

> **Transparence.** Ce projet a été entièrement réalisé avec une IA générative, **[Claude Code](https://claude.com/claude-code)** (Anthropic). Voir [Réalisation avec l'IA générative](#réalisation-avec-lia-générative).
>
> **Inspiration.** L'idée générale vient des applications de quiz géographique de **Qbis Studio**, en particulier *World Map Quiz*. Voir [Inspiration et crédits](#inspiration-et-crédits).

## Fonctionnalités

**Contenu**
- Les 34 unités provinciales (provinces, régions autonomes, municipalités, Hong Kong, Macao, Taïwan), les 6 grandes régions (Zhongnan divisée en deux) et les îles Paracel, présentées comme territoire disputé.
- 40 grandes villes (plus de 3 millions d'habitants au recensement de 2020, plus Hong Kong et Taipei).
- 27 cours d'eau (12 grands fleuves, 15 affluents), avec leur longueur.
- 17 chaînes de montagnes et 20 sommets, avec leur altitude.

**Modes de jeu**, pour chaque catégorie :
- **Apprendre** : explorer la carte, toucher un lieu pour afficher sa fiche, avec une section « En savoir plus ».
- **Trouver** : toucher le lieu demandé, une seule chance par question.
- **Éliminer** : trouver tous les lieux ; ceux qui sont trouvés disparaissent de la carte.
- **Saisir** : taper le nom du lieu coloré ; tons, accents, casse et espaces sont ignorés.
- *Contours* : prévu, pas encore disponible.

**Pour mieux retenir** (version 1.1)
- Noms connus en Europe affichés avec le pinyin : Everest / Zhumulangma Feng, Pékin / Beijing, Canton / Guangzhou.
- Pinyin avec tons (Sìchuān) et bouton 🔊 qui prononce le nom en mandarin grâce à la synthèse vocale du téléphone. Le nom demandé est aussi lu automatiquement dans Trouver et Éliminer (désactivable).
- 138 fiches « mini-Wikipédia » : population, langues, histoire, économie, plat emblématique, anecdotes.
- Résultats avec la liste des erreurs, « rejouer les erreurs », records et lieux les plus souvent manqués.

## Réalisation avec l'IA générative

Tout le contenu de ce dépôt a été produit par **Claude Code**, l'assistant de programmation d'Anthropic, au fil d'une conversation avec l'auteur du projet (compte GitHub `saksa84`) :

- **Produit par Claude Code** : le code Kotlin, les scripts Python de préparation des données, les fichiers de données (noms, alias, rattachements, longueurs, pinyins avec tons), les 138 fiches de lecture, les tests unitaires et toute la documentation, y compris ce README.
- **Fait par l'auteur** : la définition du besoin et des règles du jeu (voir [CLAUDE.md](CLAUDE.md), les instructions données à l'IA), les décisions (périmètre, noms, traitement des territoires disputés), la relecture et les essais sur un vrai téléphone (Google Pixel).

Conséquences à connaître :
- Les commits portent la mention `Co-Authored-By: Claude`.
- Les **fiches « En savoir plus »** ont été rédigées par Claude à partir de ses connaissances générales, sans source consultée pour chaque phrase. Elles ont été relues, mais peuvent contenir des erreurs. L'application les signale par la mention « à vérifier ».
- Les **données vérifiables** (frontières, coordonnées, populations, longueurs des fleuves) viennent de sources citées dans [docs/data-sources.md](docs/data-sources.md), et non de la mémoire de l'IA.
- Les tests automatisés (138, décrits plus bas) et les essais sur téléphone servent de garde-fous, mais ne garantissent pas l'absence d'erreur.

## Inspiration et crédits

**Inspiration.** Le concept (une carte à toucher, les modes « trouver », « éliminer » et « saisir », un chronomètre, la liste des erreurs en fin de partie) s'inspire des applications de quiz géographique de **Qbis Studio** : *World Map Quiz*, ainsi que *France Map Quiz* et *UK Map Quiz*. Merci à eux pour ces applications, qui ont donné envie de faire la même chose pour la Chine.

China Map Quiz est un projet **indépendant et personnel** :
- aucun code, aucune image, aucun texte ni aucun élément graphique de ces applications n'a été copié ; seules les idées générales de jeu ont été reprises, et l'interface a été dessinée pour ce projet ;
- le projet n'est ni affilié à Qbis Studio, ni approuvé ou soutenu par Qbis Studio ;
- les noms *World Map Quiz*, *France Map Quiz*, *UK Map Quiz* et *Qbis Studio* appartiennent à leurs propriétaires.

**Données.**
- Cartes : *Made with [Natural Earth](https://www.naturalearthdata.com/)*, version 5.1.2 (domaine public).
- Codes des provinces : ISO 3166-2:CN.
- Populations : recensement chinois de 2020.
- Longueurs des fleuves : Wikipédia (révisions citées).
- Prononciation : synthèse vocale d'Android installée sur le téléphone ; aucun fichier audio n'est fourni.

Le registre complet des sources, licences et transformations est dans [docs/data-sources.md](docs/data-sources.md).

**Licence.** Aucune licence n'a encore été choisie pour le code : sans licence, les droits restent réservés par défaut.

## Installer l'application

| Méthode | Pour qui |
|---|---|
| Télécharger `app-debug.apk` dans les **Releases** du dépôt GitHub, puis l'ouvrir sur le téléphone | Le plus simple, sans câble |
| `.\gradlew.bat installDebug` avec le téléphone branché en USB (débogage USB activé) | Développement |
| Android Studio → ▶ **Run** | Développement, avec l'émulateur |

Il s'agit d'un APK **debug**, signé avec une clé de développement. Il est suffisant pour un usage personnel, mais n'est pas destiné à être distribué largement. Guide pas à pas : [docs/build.md](docs/build.md).

La prononciation utilise la voix « chinois (Chine) » de la synthèse vocale Google. Si elle manque, l'application propose de la télécharger (une seule fois ; elle fonctionne ensuite hors ligne).

## Compiler

Prérequis : Android Studio 2026.2 ou plus récent (il fournit le JDK utilisé par Gradle), SDK Android API 37, appareil Android 8.0 (API 26) ou plus récent.

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest   # 138 tests unitaires, sur le PC, sans émulateur
.\gradlew.bat assembleDebug       # APK debug
```

APK généré : `app/build/outputs/apk/debug/app-debug.apk`.

## Tests unitaires

Les **138 tests** s'exécutent sur le PC en quelques secondes, sans émulateur. Les règles du jeu, la géométrie de la carte et la validation des données sont écrites en Kotlin pur, sans dépendre d'Android, justement pour pouvoir être testées ainsi. Une partie des tests vérifie les **vraies données livrées** dans l'application, et pas seulement des exemples fabriqués.

| Fichier | Tests | Ce qui est vérifié |
|---|---:|---|
| `data/GeoCatalogLoaderTest` | 42 | Lecture et validation des fichiers de données : identifiants uniques, hiérarchie (une province sous la Chine, un affluent sous un fleuve…), grandes régions, noms en alphabet latin, territoires disputés, longueurs, noms connus, pinyin avec tons. |
| `data/RealAssetsTest` | 9 | Vraies données provinciales : 34 unités et leurs types, codes ISO, capitales, hanzi, découpage des grandes régions, îles Paracel neutres. |
| `data/RealFeaturesTest` | 12 | Vraies villes, fleuves et montagnes : géométrie du bon type, villes dans leur province, fleuves sans trou, pinyin cohérent avec le nom, niveaux de jeu. |
| `data/PlaceFactsTest` | 5 | Fiches « En savoir plus » : validation, et une fiche complète pour chacun des 138 lieux. |
| `game/AnswerCheckerTest` | 10 | Mode Saisir : normalisation (tons, accents, casse), alias acceptés, fautes refusées, Shanxi et Shaanxi jamais confondus. |
| `game/QuizSessionTest` | 13 | Moteur des modes Trouver et Saisir : score, une seule réponse par question, fin de partie, chronomètre, erreurs à rejouer. |
| `game/EliminateSessionTest` | 6 | Moteur du mode Éliminer : erreurs illimitées, lieux éliminés, fin de partie, bilan. |
| `game/QuizLevelsTest` | 6 | Niveaux Nord, Sud et Toute la Chine : couverture, tailles, équilibre, aucune question sans réponse. |
| `map/HitTesterTest` | 13 | Détection du toucher : îles, trous, tolérance pour les petites régions, lignes, points prioritaires. |
| `map/MapViewportTest` | 5 | Zoom et déplacement : centrage, zoom sous les doigts, limites. |
| `map/RealMapTest` | 7 | Vraie carte et vraies coordonnées GPS : Pékin dans Beijing, Macao et les Paracel touchables, la mer ne répond pas. |
| `progress/ProgressTest` | 9 | Records, erreurs fréquentes, taux de réussite, sauvegarde. |
| `ExampleUnitTest` | 1 | Test d'exemple créé par le modèle de projet Android Studio. |

**Description détaillée de chaque test** : [docs/tests-unitaires.md](docs/tests-unitaires.md), également en PDF : [docs/tests-unitaires.pdf](docs/tests-unitaires.pdf).

## Organisation du code

| Dossier | Contenu |
|---|---|
| `app/src/main/java/.../data` | Modèle des lieux, lecture et validation des données, fiches « En savoir plus » |
| `app/src/main/java/.../map` | Géométrie, projection, zoom et détection du toucher (sans Android) |
| `app/src/main/java/.../game` | Moteurs de jeu : parties, niveaux, validation des réponses tapées |
| `app/src/main/java/.../progress` | Progression (records, erreurs fréquentes), réglages et sauvegarde locale |
| `app/src/main/java/.../speech` | Prononciation par la synthèse vocale d'Android |
| `app/src/main/java/.../ui` | Écrans Compose : accueil, Apprendre, jeux, résultat, progression, paramètres |
| `app/src/main/assets/geo` | Données géographiques et fiches (voir `docs/data-sources.md`) |
| `app/src/main/assets/game` | Configuration des niveaux |
| `app/src/test` | Tests unitaires |
| `tools/` | Scripts Python : préparation des données, icône, PDF des tests |
| `docs/` | Plans, sources de données, compilation, tests |
