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
  - chaînes, représentées par une ligne de crête (Himalaya, Tian Shan, Kunlun, Qinling…) ;
  - sommets, représentés par un point (Qomolangma/Everest, Tai Shan, Huang Shan…), rattachés à leur chaîne quand c'est pertinent.

**Version 2 — Approfondissement**
- Parcs.
- Lacs.
- Villes-préfectures.

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

Exceptions : quand un nom francophone usuel est nettement plus connu que le pinyin, il devient le nom principal et le pinyin passe en alias. C'est le cas de Hong Kong (Xianggang), de Macao (Aomen) et des îles Paracel (Xisha Qundao). Toute nouvelle exception doit être ajoutée à cette liste.

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

### Mode Type

- Demander le nom d'une région ou d'une ville.
- Accepter une réponse textuelle en pinyin.
- Valider les réponses en tenant compte des alias autorisés.
- Afficher un retour clair en cas de bonne ou mauvaise réponse.
- Permettre de poursuivre le quiz sans bloquer l'utilisateur.

### Mode Eliminate

- Afficher une liste d'entités restant à identifier.
- Permettre de sélectionner ou d'éliminer les bonnes entités selon la règle choisie.
- Retirer progressivement les éléments résolus.
- Détecter la fin de partie.
- Afficher le résultat final.

Définis précisément les règles de ce mode avant de le développer. Il doit être possible de faire évoluer la logique sans modifier le moteur cartographique.

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

## 15. Première mission

Pour commencer, ne développe pas encore toute l'application.

Effectue uniquement les tâches suivantes :

1. Vérifie l'état du dossier du projet.
2. Propose l'architecture initiale et explique les choix techniques.
3. Identifie les décisions encore ouvertes, notamment la source de la carte, les licences, la couverture géographique et la convention des noms.
4. Propose un plan de développement découpé en étapes courtes.
5. Définis le périmètre d'une première version minimale fonctionnelle.
6. Identifie les prérequis logiciels nécessaires sur mon ordinateur.
7. Propose les premiers tests de validation.
8. Présente les fichiers qui seront créés lors de la première étape d'implémentation.

Ne commence pas à générer l'ensemble des fichiers tant que le plan initial n'a pas été présenté.

Le premier objectif concret sera une application Android qui se lance, affiche une carte provinciale interactive, permet de sélectionner une région et affiche son nom en pinyin.

Une fois ce prototype validé, nous ajouterons progressivement les modes de jeu.

**Règle générale : privilégie toujours une petite fonctionnalité terminée, testée et comprise à une grande quantité de code générée sans validation.**