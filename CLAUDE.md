# CLAUDE.md — China Map Quiz

## 1. Rôle de Claude Code

Tu es mon assistant principal de développement pour créer une application Android personnelle de quiz géographique sur la Chine.

Tu dois agir comme un développeur Android expérimenté, un architecte logiciel pragmatique, un spécialiste des données géographiques et un mentor pédagogique.

Je possède des bases en programmation : j'ai utilisé Python, MATLAB et C++, mais je n'ai jamais développé d'application Android et je connais peu JavaScript. Je comprends les variables, les fonctions, les structures de données, les algorithmes, la programmation orientée objet et les tests.

Adapte tes explications à ce niveau : ne m'explique pas les bases de la programmation inutilement, mais explique les concepts propres à Android, Kotlin, Gradle, Jetpack Compose et à la gestion des cartes.

### Informations personnelles du projet

- Compte GitHub : `saksa84`
- Nom de paquet (`applicationId`) : `io.github.saksa84.chinamapquiz`
- Téléphone cible : **Google Pixel 8 Pro** (écran 6,7" 1344×2992, haute densité). C'est sur lui que l'APK sera installé et testé ; l'émulateur sert au développement quotidien.
- Plan initial et décisions prises : `docs/plan-initial.md`
- Plan en cours (version 1.1) : `docs/plan-v1.1.md`
- Sources et licences : `docs/data-sources.md` ; compilation et installation : `docs/build.md`

**Objectif principal : construire progressivement une application fonctionnelle, fiable, agréable à utiliser, installable sur mon téléphone Android sous forme d'APK et publiable sur Git.**

Ne cherche pas à tout construire immédiatement. Privilégie une application minimale fonctionnelle, puis enrichis-la étape par étape.

## 2. Vision du produit

Le projet est provisoirement nommé China Map Quiz.

Il s'inspire du concept général des applications de quiz géographique de Qbis Studio, notamment World Map Quiz, France Map Quiz et UK Map Quiz.

Il ne s'agit pas de copier leur code, leurs ressources graphiques, leurs textes, leur interface exacte ou leurs éléments protégés. Nous voulons créer une application indépendante reprenant les idées générales du quiz géographique et en développant une expérience adaptée à la Chine.

L'application doit permettre d'apprendre la géographie chinoise à travers une carte interactive et plusieurs modes de jeu.

### Principes fondamentaux

- Application Android native.
- Fonctionnement intégralement hors ligne après installation.
- Interface adaptée à un téléphone.
- Noms géographiques affichés en alphabet latin, principalement en pinyin.
- Données géographiques séparées du code de l'application.
- Architecture compréhensible et maintenable.
- Aucun compte utilisateur, serveur ou système de paiement nécessaire.
- Pas de publicité.
- Progression sauvegardée localement.
- APK installable manuellement sur mon téléphone.
- Projet versionné avec Git et publiable sur GitHub.

La langue initiale de l'interface peut être l'anglais ou le français. Propose une solution simple permettant éventuellement de changer de langue ultérieurement.

## 3. Technologies

Utilise en priorité :

- Kotlin comme langage principal.
- Android Studio comme environnement de développement.
- Jetpack Compose pour les interfaces.
- Gradle avec Kotlin DSL pour la configuration du projet.
- Les composants Android natifs pour la navigation, les interactions et la persistance.
- Une bibliothèque de stockage locale simple, par exemple Preferences DataStore pour les paramètres et la progression.
- Une solution de carte vectorielle interactive adaptée aux polygones géographiques.

Pour la carte, compare les options pertinentes avant de décider : rendu personnalisé avec Canvas, rendu SVG ou bibliothèque cartographique adaptée. Choisis la solution la plus simple qui permette de sélectionner précisément les régions, de zoomer et de déplacer la carte.

N'ajoute pas de dépendances complexes sans justification. Évite une architecture surdimensionnée pour une application personnelle.

Utilise des versions stables et compatibles des outils. Ne devine pas les versions des bibliothèques : vérifie les versions disponibles et leur compatibilité lorsque cela est possible.

L'application ne doit pas dépendre d'un service cartographique en ligne ou d'une connexion réseau pour jouer.

## 4. Périmètre géographique

Le contenu géographique doit être structuré par niveaux administratifs.

### Versions du produit (contenu)

L'application est livrée en trois versions de contenu successives. Chaque version ajoute des catégories de lieux et donc de nouveaux attendus. Ne pas confondre ces versions avec les étapes de développement de `docs/plan-initial.md` : chaque version se construit en plusieurs étapes.

**Version 1 — Bases de la géographie chinoise**
- Grandes régions : ce sont des régions historiques et géographiques, sans statut administratif actuel. On suit le découpage statistique en 6 régions, hérité des grandes régions administratives de 1949-1954, en subdivisant Zhongnan. Leur géométrie est l'union des provinces qui les composent, il n'y a pas de tracé séparé. Chaque province appartient à une seule grande région.
  - Dongbei (Nord-Est) : Liaoning, Jilin, Heilongjiang
  - Huabei (Nord de Chine) : Beijing, Tianjin, Hebei, Shanxi, Nei Mongol (en entier)
  - Huadong (Est de Chine) : Shanghai, Jiangsu, Zhejiang, Anhui, Fujian, Jiangxi, Shandong, Taiwan
  - Zhongnan (Centre-Sud), découpée en :
    - Huazhong (Centre de Chine) : Henan, Hubei, Hunan
    - Huanan (Sud de Chine) : Guangdong, Guangxi, Hainan, Hong Kong, Macao
  - Xibei (Nord-Ouest) : Shaanxi, Gansu, Qinghai, Ningxia, Xinjiang
  - Xinan (Sud-Ouest) : Chongqing, Sichuan, Guizhou, Yunnan, Xizang
- Les 34 unités provinciales : provinces (shěng), régions autonomes (zìzhìqū), les 4 municipalités (zhíxiáshì : Beijing, Shanghai, Tianjin, Chongqing), Hong Kong, Macao et Taïwan.
- Grandes villes : au moins 30, classées selon la population urbaine en millions d'habitants, avec une source citable. Le seuil exact est fixé à l'étape de préparation des données.
- Fleuves, en deux catégories sélectionnables séparément :
  - grands fleuves (Chang Jiang, Huang He, Zhu Jiang, Heilong Jiang…) ;
  - affluents importants, rattachés au fleuve dans lequel ils se jettent.
- Montagnes, en deux catégories sélectionnables séparément :
  - chaînes, représentées par une **zone touchable** (polygones Natural Earth, coupés à la frontière au dessin), comme dans les applications de référence (Himalaya, Tian Shan, Kunlun, Qinling…) ;
  - sommets, représentés par un point (Qomolangma/Everest, Tai Shan, Huang Shan…), rattachés à leur chaîne quand c'est pertinent.

**Version 2 — Approfondissement** (plan et décisions : `docs/plan-v2.md`)
- Parcs : les 5 parcs nationaux officiels (créés en 2021) et une quinzaine de sites naturels célèbres, avec leur statut exact affiché. Contours tirés d'**OpenStreetMap** (ODbL : attribution, fichier dérivé sous ODbL). Décision du 2026-10-25.
- Lacs : une vingtaine de grands lacs en surface (Natural Earth `ne_10m_lakes`, domaine public), plus Xi Hu, Dian Chi et Erhai dont les contours viennent d'OpenStreetMap (décision du 2026-10-25, d'abord en point). Bangong Co (ligne de contrôle Chine–Inde) écarté pour l'instant.
- Villes-préfectures (après les lacs et les parcs).

**Version 3 — Culture**
- Spécialités culinaires par province.

Conséquences pour l'architecture, à prévoir dès la version 1 :
- Les entités ne sont pas toutes administratives. Le modèle doit gérer des catégories (région, province, ville, fleuve, montagne, lac, parc) et plusieurs formes de géométrie : surfaces (provinces, lacs, parcs), lignes (fleuves) et points (villes, sommets).
- Un contenu non géographique, comme les spécialités culinaires, se rattache à une entité existante par son identifiant. Il est stocké dans un fichier séparé.
- Chaque catégorie est un fichier de données indépendant, activable selon la version.

Les trois versions sont des enrichissements successifs d'une **seule et même application**, avec un seul moteur de jeu, une seule carte et une seule progression. Elles ne forment pas trois apps distinctes.

### Niveau 1 — Unités provinciales

Commencer par les unités de premier niveau, couramment regroupées en 34 divisions dans le périmètre de référence choisi :

- provinces ;
- régions autonomes ;
- municipalités relevant directement du niveau central ;
- régions administratives spéciales.

La couverture exacte, notamment le traitement de Taïwan, de Hong Kong, de Macao et des frontières représentées, doit être définie explicitement dans la configuration et la documentation. Ne prends pas de décision implicite sur ces questions.

### Niveau 2 — Villes et divisions préfectorales

Ajouter ensuite les divisions de niveau préfectoral, notamment les villes de niveau préfectoral, les préfectures et les préfectures autonomes.

Ne considère pas automatiquement chaque division administrative comme une ville au sens courant.

### Niveau 3 — Grandes villes et lieux habités

Ajouter les villes importantes et d'autres lieux habités à partir d'une source vérifiable.

Les villes administratives, les agglomérations et les sièges administratifs ne sont pas toujours équivalents. Leur définition doit être documentée.

### Évolutivité

Les niveaux supplémentaires, tels que les comtés et les districts, sont hors du périmètre initial. L'architecture doit néanmoins permettre de les ajouter plus tard sans réécriture majeure.

## 5. Noms et données

L'alphabet latin est obligatoire pour les noms présentés dans les modes de jeu.

Le pinyin sans tons sera la convention d'affichage par défaut.

Exemples : Beijing, Shanghai, Guangdong, Sichuan, Chongqing, Urumqi.

Exceptions : quand un nom francophone usuel est nettement plus connu que le pinyin, il devient le nom principal et le pinyin passe en alias. C'est le cas de Hong Kong (Xianggang), de Macao (Aomen), de Taipei (Taibei) et des îles Paracel (Xisha Qundao). Toute nouvelle exception doit être ajoutée à cette liste.

**Noms connus en Europe (décision du 2026-10-24, mise en œuvre en v1.1)** : quand un lieu a un nom courant en français ou à l'international, on l'affiche avec le pinyin, sous la forme « Nom connu / pinyin ». Exemples : Everest / Zhumulangma Feng, K2 / Qiaogeli Feng, Yangtsé / Chang Jiang, Pékin / Beijing, Canton / Guangzhou. Le pinyin reste le nom de référence (`name`) ; le nom connu est un champ à part (`knownAs`), dans lequel les accents sont autorisés. Les deux sont acceptés dans le mode Saisir.

### Territoires disputés

Rester neutre : ne pas rattacher un territoire disputé à un pays par la hiérarchie (pas de `parentId`). On renseigne `claimedBy` avec les codes ISO 3166-1 des pays qui le revendiquent, et l'application affiche « revendiqué par … » sans trancher, comme le font les applications de quiz pour les Malouines.

- Îles Paracel : revendiquées par la Chine (CN), Taïwan (TW) et le Vietnam (VN).

Les noms chinois en caractères peuvent être conservés comme métadonnées facultatives afin de vérifier les données et d'enrichir ultérieurement l'application.

Chaque unité géographique doit avoir un identifiant stable, indépendant de son nom affiché.

Un modèle de données doit prévoir, selon le type d'entité :

- identifiant unique ;
- nom principal en pinyin ;
- autres graphies ou alias acceptés ;
- nom chinois facultatif ;
- type de division administrative ;
- identifiant du parent ;
- capitale ou siège administratif, si applicable ;
- géométrie géographique ou référence à celle-ci ;
- source des données ;
- informations de licence ;
- métadonnées utiles au quiz.

Ne confonds pas une ville et sa province, ni une capitale provinciale et une capitale nationale.

Les réponses saisies doivent être normalisées avant comparaison. Prévois notamment la gestion des espaces superflus, de la casse et des variantes explicitement autorisées.

Ne considère pas toutes les fautes d'orthographe comme correctes. Les variantes acceptées doivent être documentées dans les données.

## 6. Données cartographiques et licences

La qualité de la carte est une priorité.

Recherche des données vectorielles administratives fiables, notamment au format GeoJSON, et examine leur compatibilité avec Android.

Sources à évaluer, sans présumer de leur licence :

- jeux de données administratives issus de sources officielles ;
- jeux de données géographiques documentés ;
- sources ouvertes proposant les frontières provinciales et préfectorales ;
- données issues d'OpenStreetMap lorsque leur contenu est pertinent.

Avant d'intégrer une source, vérifie :

1. sa couverture géographique ;
2. sa date de mise à jour ;
3. son système de coordonnées ;
4. sa précision ;
5. sa structure ;
6. les identifiants disponibles ;
7. les conditions de réutilisation et d'attribution ;
8. les éventuelles restrictions de redistribution.

Ne télécharge pas, n'intègre pas et ne redistribue pas automatiquement des données dont les droits sont incertains.

Conserve un registre des sources, licences, versions et transformations dans la documentation du projet.

Si les données sont trop lourdes, étudie une simplification géométrique contrôlée. La simplification ne doit pas rendre les frontières incohérentes ni supprimer des régions importantes.

La carte doit permettre d'identifier chaque région par un identifiant unique, et non par son nom ou par sa position dans une liste.

## 7. Modes de jeu

Conçois le moteur de jeu indépendamment des écrans. Les différents modes doivent réutiliser les mêmes données et services de validation.

### Modes retenus et parcours

Menu des modes : **Trouver** (Find), **Éliminer** (Eliminate), **Saisir** (Type) et **Contours** (reconnaître une région d'après sa silhouette). Aucun autre mode n'est prévu pour l'instant. Les modes pas encore développés apparaissent comme « bientôt disponibles ».

Parcours : catégorie (Régions, Villes, Fleuves, Montagnes) → mode → niveau → partie → résultat. La progression est enregistrée séparément pour chaque catégorie. Le style s'inspire librement des applications de quiz géographique, sans copie : tuiles sombres, chrono en haut, consigne et bouton en bas.

### Niveaux (configuration `assets/game/levels.json`)

- Pas de classement par notoriété.
- **Zones équilibrées** : Nord (15 unités) et Sud (16 unités), séparées selon la ligne Qinling–Huaihe, limite traditionnelle entre Chine du Nord et du Sud. Qinghai est rattaché au Nord et Xizang au Sud pour équilibrer. S'y ajoute « Toute la Chine ».
- **Variantes** : « continent » (les 31 unités de Chine continentale) et « complet » (avec Hong Kong, Macao, Taïwan et les îles Paracel, soit 35).

### Résultats et erreurs

L'écran de résultat affiche le score, le pourcentage de bonnes réponses, le temps et **la liste des erreurs** (lieu cherché, lieu touché), avec la possibilité de rejouer uniquement les erreurs. Le moteur conserve le détail de chaque réponse pour permettre plus tard une révision plus riche (voir chaque erreur sur la carte, statistiques par région).

### Mode Learn

- Afficher la carte.
- Permettre de toucher une région pour afficher son nom.
- Afficher des informations géographiques utiles.
- Permettre d'explorer les régions sans chronomètre.
- Préparer un fonctionnement comparable pour les villes.

### Mode Find

- Afficher une consigne telle que « Find Sichuan ».
- Permettre de sélectionner la bonne région directement sur la carte.
- Fournir un retour visuel immédiat.
- Passer à la question suivante.
- Afficher la progression de la session.
- **Une seule chance par question** : un toucher sélectionne la région ; on valide avec le bouton Confirmer ou en touchant une seconde fois la région sélectionnée. En cas d'erreur, la bonne région est montrée et l'on passe à la suite.
- Chronomètre visible pendant la partie, points et numéro de la question affichés.
- Seules les régions du niveau choisi sont sélectionnables ; les autres sont grisées.

### Mode Type

- Demander le nom d'une région ou d'une ville.
- Accepter une réponse textuelle en pinyin.
- Valider les réponses en tenant compte des alias autorisés.
- Afficher un retour clair en cas de bonne ou mauvaise réponse.
- Permettre de poursuivre le quiz sans bloquer l'utilisateur.
- Décisions du 2026-10-24 :
  - La région demandée est colorée sur la carte ; on tape son nom au clavier du téléphone, sans suggestion.
  - **Une seule chance**, avec un bouton « Passer ».
  - **Normalisation stricte** : on ignore la casse, les espaces, les tons et accents (Sìchuān = Sichuan), les apostrophes et les tirets. On accepte le nom principal et les alias listés dans les données. Toute autre faute est une erreur : Shanxi et Shaanxi restent distincts.

### Mode Eliminate

- Afficher une liste d'entités restant à identifier.
- Permettre de sélectionner ou d'éliminer les bonnes entités selon la règle choisie.
- Retirer progressivement les éléments résolus.
- Détecter la fin de partie.
- Afficher le résultat final.

Définis précisément les règles de ce mode avant de le développer. Il doit être possible de faire évoluer la logique sans modifier le moteur cartographique.

Règles retenues le 2026-10-24 (remplacées en version 1.2, voir plus bas) :
- Une consigne « Trouver : X » à la fois, dans un ordre aléatoire.
- **Erreurs illimitées** : un toucher vaut une réponse, sans confirmation. Une erreur fait clignoter la région touchée en rouge et la consigne reste la même.
- Une région trouvée est **éliminée** : elle reste colorée et n'est plus touchable, si bien que la carte se vide peu à peu.
- La partie se termine quand tout est trouvé. Le résultat compte les régions trouvées du premier coup, les erreurs et le temps ; la liste des erreurs et « rejouer les erreurs » fonctionnent comme dans Trouver.

**Règles révisées le 2026-10-25 (version 1.2), qui remplacent les précédentes** :
- **Une seule chance par lieu**, avec **confirmation comme dans Trouver** (décision du 2026-10-25, après essai) : un toucher sélectionne (en jaune), on valide avec le bouton Confirmer ou en touchant une seconde fois la même région. Une région éliminée ne peut pas être sélectionnée.
- Bonne réponse : le lieu clignote en vert puis **s'efface** (presque transparent) et n'est plus touchable.
- Erreur : le lieu touché clignote en rouge, le lieu demandé est montré en vert (1,5 s), puis on **passe à la consigne suivante**. Le lieu manqué **reste sur la carte** et n'est plus redemandé dans la partie.
- La partie se termine quand chaque lieu a été demandé une fois : il ne reste sur la carte que les lieux manqués. Résultat, liste des erreurs et « rejouer les erreurs » comme dans Trouver.
- Pendant le clignotement, la consigne reste affichée et la carte ne répond pas.

### Son et fiches pendant les quiz (version 1.2, demandé le 2026-10-25)

- **Réécouter la consigne** : un bouton 🔊 à côté de « Trouver : X » (modes Trouver et Éliminer) relit le nom en mandarin autant de fois qu'on veut. Pas dans Saisir pendant la question, où il donnerait la réponse (la fiche de réponse a déjà son 🔊).
- **Sons de réponse** : un son court pour une bonne réponse, un autre pour une erreur, toujours les mêmes, dans Trouver, Éliminer et Saisir (rien pour « Passer »). Sons synthétisés par `tools/build_sounds.py` (aucun fichier extérieur, donc aucune licence), lus avec `speech/AnswerSounds` (SoundPool). Réglage « Sons de réponse », activé par défaut.
- **Fiche dépliable en jeu** : après une réponse dans Trouver et Saisir, la fiche compacte a un bouton « En savoir plus » qui déplie la fiche complète (comme dans Apprendre). **Le chrono est en pause** tant qu'elle est ouverte (« ⏸ en pause » sous le temps) ; il repart en la refermant ou en passant à la question suivante. Le moteur utilise `game/Stopwatch`. Pas de fiche dans Éliminer, où l'on enchaîne sans bouton « Suivant ».

### Modes futurs

Prévois la possibilité d'ajouter ultérieurement :

- capitales et sièges administratifs ;
- quiz à choix multiples ;
- entraînement sur les erreurs ;
- sessions chronométrées ;
- statistiques par région ;
- niveaux de difficulté supplémentaires ;
- modes de révision.

Ces fonctionnalités ne sont pas prioritaires pour la première version.

## 8. Progression et niveaux

Le système de niveaux doit être simple, progressif et configurable.

Exemple de progression pour les unités provinciales :

1. Exploration et apprentissage.
2. Identification d'un petit groupe de régions.
3. Identification de toutes les régions.
4. Saisie de tous les noms.
5. Mode élimination.
6. Révision des erreurs.
7. Test final sans aide.

Les nombres de régions, les critères de réussite et les seuils de progression doivent provenir de la configuration ou des données, et non être dispersés dans le code.

Ne déverrouille pas arbitrairement un niveau si les conditions de réussite ne sont pas définies.

Sauvegarde localement les niveaux terminés, les meilleurs résultats et les erreurs fréquentes.

## 9. Architecture du code

Sépare les responsabilités en modules ou packages simples.

Architecture indicative :

- `data` : données géographiques, chargement et validation ;
- `map` : rendu cartographique, zoom, déplacement et sélection ;
- `game` : règles communes du quiz ;
- `game.modes` : logique spécifique de chaque mode ;
- `progress` : progression et sauvegarde ;
- `ui` : écrans et composants visuels ;
- `navigation` : navigation entre les écrans.

Adapte cette structure à la taille réelle du projet. Ne crée pas de modules artificiels uniquement pour suivre un modèle théorique.

Les écrans ne doivent pas contenir les données géographiques en dur ni l'ensemble de la logique métier.

Les règles du quiz doivent être testables sans démarrer Android ni afficher la carte.

## 10. Interface et ergonomie

L'interface doit être sobre, lisible et adaptée au tactile.

Écrans prévus :

- écran d'accueil ;
- sélection du niveau géographique ;
- sélection du mode ;
- écran de jeu ;
- écran de résultat ;
- écran de progression ;
- écran de paramètres.

Pour l'écran de jeu, prévois :

- une carte occupant une place importante ;
- une consigne clairement visible ;
- une progression de session ;
- un retour visuel immédiat ;
- un accès simple à l'abandon ou à la reprise ;
- une gestion correcte du clavier dans le mode Type.

La carte doit prendre en charge le zoom et le déplacement tactile si cela est pertinent pour le mode utilisé.

Les petites régions doivent rester sélectionnables. Prévois des tests spécifiques pour les zones denses et les régions de petite superficie.

Évite de surcharger la carte avec des noms qui se chevauchent. Affiche les noms de manière contextuelle lorsque cela améliore la lisibilité.

## 11. Tests et qualité

Chaque fonctionnalité importante doit être testée.

Prévois notamment :

- des tests unitaires de validation des réponses ;
- des tests de normalisation du pinyin ;
- des tests de génération des questions ;
- des tests de progression ;
- des tests de fin de partie ;
- des tests de chargement des données ;
- des vérifications de l'unicité des identifiants ;
- des vérifications de la hiérarchie administrative ;
- des tests de sélection sur la carte ;
- des tests d'intégration pour les parcours principaux.

Aucune question ne doit cibler une région absente des données.

Les questions doivent toujours avoir une réponse valide.

Un même identifiant ne doit pas désigner deux régions différentes.

Le moteur de jeu doit gérer proprement les données manquantes, les listes vides et les sessions terminées.

L'application doit compiler avant qu'une étape soit considérée comme terminée.

Lance les tests pertinents après chaque modification importante et indique clairement ceux qui n'ont pas pu être exécutés.

## 12. Construction et installation

Le résultat doit être une application Android installable sur mon téléphone.

Prévois une procédure documentée permettant de :

- ouvrir le projet dans Android Studio ;
- synchroniser Gradle ;
- lancer les tests ;
- démarrer l'application sur un émulateur ou un téléphone ;
- générer un APK debug ;
- installer l'APK ;
- générer ultérieurement un APK release signé.

Ne confonds pas un APK debug et une version release.

Ne stocke jamais les clés privées de signature ni les mots de passe dans Git.

Le chemin du fichier APK généré doit être indiqué après chaque build réussi.

## 13. Git et documentation

Le projet doit être versionnable dès le début.

Prévois :

- un `.gitignore` adapté à Android et Kotlin ;
- un `README.md` ;
- un `CLAUDE.md` ;
- un dossier de documentation des données géographiques ;
- une documentation de compilation ;
- un registre des licences ;
- des tests automatisés.

Ne versionne pas les fichiers de compilation temporaires, les clés privées ou les secrets.

Fais des commits logiques lorsque je le demande, avec des messages descriptifs.

Ne pousse jamais de modifications sur un dépôt distant sans mon autorisation explicite.

## 14. Méthode de travail avec Claude Code

Travaille par petites étapes indépendantes et vérifiables.

Au début de chaque étape :

1. Examine les fichiers et l'état du projet.
2. Résume brièvement l'objectif.
3. Identifie les décisions techniques nécessaires.
4. Propose un plan court.
5. Signale les risques ou dépendances importantes.

Ensuite, implémente uniquement le périmètre convenu.

Après chaque étape :

1. Compile le projet si possible.
2. Exécute les tests pertinents.
3. Examine les erreurs et corrige-les.
4. Résume les fichiers créés ou modifiés.
5. Donne les commandes utiles.
6. Explique les concepts Android nouveaux que je dois comprendre.
7. Indique les vérifications manuelles à faire sur le téléphone.

Ne prétends jamais qu'une compilation ou un test a réussi sans avoir obtenu le résultat correspondant.

Si un outil, un environnement ou une dépendance manque, explique le blocage et propose une solution concrète.

Ne réécris pas des parties fonctionnelles sans nécessité.

Ne supprime pas des fichiers, des données ou du travail existant sans m'expliquer la conséquence et obtenir mon accord lorsque l'opération est risquée.

Privilégie les solutions simples, lisibles et robustes.

## 15. État du projet et mission en cours

### Version 2 en cours (2026-10-25) : lacs et parcs

Version 1.2 poussée sur GitHub (`a93dded`). Plan, sources examinées et décisions dans `docs/plan-v2.md`.
- **Étape A (lacs) faite, pas encore commitée** : `assets/geo/lakes.json` (24 lacs, types `lake` et `reservoir`, champ `areaKm2`, parent = province), contours dans `features.geometry.json` (dictionnaire `LAKES` de `build_features_geometry.py`), catégorie `GeoCategory.LAKES`, niveaux `lakes-west` / `lakes-east` / `lakes-all`, 147 tests. Sources des superficies dans `docs/data-sources.md`. Xi Hu, Dian Chi et Er Hai : contours OSM dans `osm.geometry.json` (le type `small_lake` en point a été supprimé). Pas encore de fiches « En savoir plus » pour les lacs (étape C).
- **Étape A commitée** (`2851072`).
- **Étape B (parcs) faite, pas encore commitée** : `assets/geo/parks.json` (16 parcs, champ `designations` : national_park, world_heritage, global_geopark, national_nature_reserve, national_scenic_area, avec l'année), contours OSM dans `osm.geometry.json` (**ODbL**, fichier séparé, script `tools/build_osm_geometry.py`), attribution OSM dans À propos et le README. Parcs Tigre et léopard et Hainan en points (absents d'OSM ; WDPA interdit la redistribution), de même taille que les petits parcs vus de loin (`ShapeStyle.largePoint = false`). Tous les lieux OSM (parcs et 3 petits lacs) sont dans `osm.geometry.json`, construit par `tools/build_osm_geometry.py`. Au toucher, la plus petite de deux zones superposées l'emporte (`HitTester`) ; au dessin, les grandes zones passent dessous. 151 tests. Années UNESCO saisies de mémoire : à vérifier.
- **Étape B commitée** (`d4854e0`), avec les contours OSM de Xi Hu, Dian Chi et Er Hai.
- **Étape C (fiches) faite** : 40 fiches lacs et parcs dans `facts.json` (178 au total). Demande de l'utilisateur : privilégier les **légendes** et les **faits insolites**, plus mémorables que l'altitude ; nouveau champ `legend`, affiché « Légende : … » juste après la présentation. Rien inventé pour combler : une fiche sans légende connue reste plus courte. 152 tests.
- **Vérification avec sources** (demandée le 2026-10-25, « scientifique et historique ») : méthode et registre dans `docs/verification-fiches.md`. Règle : chaque phrase a une source citable (révision Wikipédia notée, UNESCO, site officiel) ; ce qui est introuvable est **retiré**, pas remplacé par du non vérifié ; une fiche vérifiée passe à `reviewed: true`. **Fait** : les 40 fiches lacs et parcs et les statuts des parcs (plus de la moitié corrigées ; Er Hai n'est pas « en forme d'oreille », Zhangye géoparc en 2020). **Reste** : provinces (34), villes (40), cours d'eau (27), montagnes (37), par lots. Outil : script `wp.py` (texte brut d'un article Wikipédia + recherche de mots-clés, avec pauses : Wikipédia renvoie des erreurs 429 si l'on enchaîne trop vite).
- Ensuite : D (finitions, version 2.0).

### Version 1.2 (2026-10-25) — retours après essai sur le Pixel

Codée, 143 tests unitaires, essayée sur l'émulateur puis dans Android Studio par l'utilisateur, **commitée et poussée** (`versionName = "1.2"`, `versionCode = 3`) ; release GitHub pas encore créée. Détail des règles dans la section 7 (« Mode Eliminate » et « Son et fiches pendant les quiz ») :
1. Bouton 🔊 pour réécouter la consigne (Trouver, Éliminer).
2. Sons de bonne et de mauvaise réponse (`res/raw`, `tools/build_sounds.py`, `speech/AnswerSounds`, réglage `answerSounds`).
3. Éliminer : une seule chance par lieu, avec sélection puis confirmation comme dans Trouver ; trouvé = effacé, manqué = reste ; à la fin il ne reste que les manqués.
4. Fiche « En savoir plus » dépliable après chaque réponse dans Trouver et Saisir, chrono en pause (`game/Stopwatch`).

À vérifier sur le Pixel : volume et timbre des sons, lisibilité de la fiche dépliée, rythme d'Éliminer (0,6 s après une bonne réponse, 1,5 s après une erreur).

### Où on en est (au 2026-10-25)

**Version 1.1 terminée, testée sur l'émulateur, commitée et publiée sur GitHub** : https://github.com/saksa84/china-map-quiz (branche `main`). 138 tests unitaires, `versionName = "1.1"`, `versionCode = 2`. La V1 (commit `37a4fe8`) avait été validée sur le Pixel 8 Pro le 2026-10-24 ; la V1.1 n'a pas encore été installée ni écoutée sur le Pixel.

Contenu : 34 unités provinciales, 8 grandes régions (6 de premier rang, Zhongnan subdivisée) et les îles Paracel ; 40 villes, 27 cours d'eau (12 grands fleuves, 15 affluents), 17 chaînes et 20 sommets. Modes Apprendre, Trouver, Éliminer et Saisir pour chaque catégorie ; progression, records, révision des erreurs, paramètres. Le mode Contours est annoncé « bientôt ».

**Ce que la V1.1 a ajouté** (détail et décisions dans `docs/plan-v1.1.md`) :
- A. Longueur des cours d'eau (`lengthKm`, `lengthInChinaKm`, sources dans `docs/data-sources.md`) ; tracés du Huai, du Liao, du Tarim et du Yongding complétés ; test `riverSectionsLeaveNoGap`.
- B. Noms connus en Europe (`knownAs`, 23 lieux) : « Mékong / Lancang Jiang » partout (fiches, consignes, résultats), nom court sur la carte ; accepté dans Saisir, avec ou sans accents.
- C. Pinyin avec tons (`pinyinTones`, 148 lieux) ; bouton 🔊 (`speech/Pronouncer`, synthèse vocale d'Android en mandarin, lit les hanzi, voix hors ligne de préférence, canal « médias » pour le volume) ; lecture automatique du nom demandé dans Trouver et Éliminer (réglage « Lire les consignes », activé par défaut, pas dans Saisir où elle donnerait la réponse).
- D. Fiches « En savoir plus » : `assets/geo/facts.json` (138 fiches, modèle `PlaceFacts`, validation dans `PlaceFactsLoader`), section dépliable dans Apprendre (le panneau passe à 60 % de la hauteur).
- E. Fiche compacte de 4 lignes avec 🔊 dans le retour de réponse des quiz.

### Repères dans le code

- `data/` : `GeoEntity` (dont `knownAs`, `pinyinTones`, `lengthKm`), chargement et validation, `PlaceFacts`, `AssetGeoRepository` ;
- `map/` : géométrie (surfaces, lignes, points), projection, détection du toucher, zoom ;
- `game/` : moteurs `QuizSession` et `EliminateSession`, chronomètre `Stopwatch`, niveaux, `AnswerChecker`, `GeoCategory` ;
- `progress/` : progression, réglages (`showHanzi`, `autoSpeak`, `answerSounds`) et DataStore ;
- `speech/` : `Pronouncer` (synthèse vocale, sans dépendance du moteur de jeu) et `AnswerSounds` (sons de réponse) ;
- `ui/` : `AppViewModel`, écrans, `ChinaMap`, `common/EntityInfo` (fiche complète et fiche compacte), `common/SpeakButton` ;
- `tools/` : scripts Python (`download_natural_earth.py`, `build_map_geometry.py`, `build_features_geometry.py`, `build_icon.py`, `build_sounds.py`, `build_tests_pdf.py`).

### Commandes

Voir `docs/build.md`. Avec PowerShell : `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`, puis `.\gradlew.bat testDebugUnitTest assembleDebug`. Essai sur l'émulateur : `installDebug`, captures avec `adb exec-out screencap -p`. L'APK est dans `app/build/outputs/apk/debug/app-debug.apk`. Dans l'émulateur, la voix mandarin est installée : le journal `adb logcat -s Pronouncer:*` montre « Lecture commencée / terminée ».

### À faire ensuite

1. **Créer la Release GitHub `v1.1`** avec l'APK debug en pièce jointe (procédure dans `docs/build.md`), puis installer sur le Pixel et **écouter** la prononciation et le volume (jamais entendue, seulement vue dans les journaux). `gh` n'est pas installé : la release se fait sur le site (ou `winget install GitHub.cli`).
2. **Relire les 138 fiches** de `facts.json` : Claude les a relues le 2026-10-24 (9 corrections) mais de mémoire, sans source. Elles restent marquées « à vérifier » tant que `reviewed` n'est pas à `true`.
3. **Choisir une licence** pour le code (aucune pour l'instant ; MIT proposée).
4. **Version 2** : parcs, lacs, villes-préfectures ; puis le **mode Contours** ; version 3 : spécialités culinaires.
5. Limite connue : le Min Jiang n'a que son cours inférieur (Natural Earth n'a pas le reste).

### Règles de publication (mises en place le 2026-10-24)

- Le dépôt est public et **ne contient aucune capture d'application commerciale** : `Inspirations/` et `screenshot/` restent dans `.gitignore`, comme `tools/raw/`, les APK, les clés de signature et `.idea/deploymentTargetSelector.xml` (il contient le numéro de série du téléphone).
- **Transparence** : le README et l'écran Paramètres → À propos disent que tout est fait avec une IA générative (Claude Code) et que le projet s'inspire des applications de Qbis Studio (World Map Quiz) sans rien copier ni être affilié. À conserver à chaque mise à jour.
- Les tests (143 en v1.2) sont décrits dans `docs/tests-unitaires.md` et en PDF (`docs/tests-unitaires.pdf`). **Quand on ajoute ou modifie un test** : mettre à jour le `.md` (et le tableau du README), puis `python tools/build_tests_pdf.py`.
- Les commits se terminent par `Co-Authored-By: Claude` (la ligne exacte vient de la consigne d'attribution de la session).
- **Ne jamais pousser sans autorisation explicite** (déjà donnée pour la V1.1 uniquement). Au premier envoi, Git Credential Manager demande une connexion GitHub dans le navigateur : c'est à l'utilisateur de la faire. Le README a été modifié sur GitHub par l'utilisateur après le premier envoi : toujours faire `git fetch` avant de travailler.

### Pièges rencontrés

- PowerShell : l'encodage par défaut est cp1252 ; pour afficher du français ou du chinois depuis Python, utiliser `sys.stdout.reconfigure(encoding="utf-8")`. Python 3.10 : pas de barre oblique inverse dans une expression d'f-string.
- Expressions régulières Kotlin dans une chaîne normale : il faut doubler la barre inverse (`"\\p{IsLatin}"`, `"\\-"`).
- Ne pas modifier un fichier de test sans relancer tout `testDebugUnitTest` : plusieurs tests lisent les vrais fichiers de `assets`.

**Règle générale : privilégie toujours une petite fonctionnalité terminée, testée et comprise à une grande quantité de code générée sans validation.**
