# Plan d'action — version 1.1 (« mieux retenir »)

Rédigé le 2026-10-24, après validation de la version 1 sur le Pixel 8 Pro (commit `37a4fe8`).
Objectif : aider à **retenir et prononcer** les lieux, sans alourdir l'application.

Chaque étape est petite. Elle se termine par la compilation, les tests, un essai sur l'émulateur, puis un commit **sur demande**.

## Déjà fait

- Trou du Zhu Jiang corrigé : le tronçon Natural Earth « Quan » (Qian Jiang) manquait entre le Hongshui et le Xun (commit `37a4fe8`).

## Étape A — Vérifications et longueurs des cours d'eau

1. Vérifier visuellement les 27 cours d'eau (aperçu SVG ou mode Apprendre) à la recherche d'autres tronçons manquants. Ajouter un test qui signale un écart de plus de 30 km entre deux tronçons d'un même cours d'eau.
2. Ajouter un champ `lengthKm` (longueur totale du cours d'eau) dans `rivers.json`, avec la source. Si utile, préciser aussi la longueur en Chine pour les fleuves frontaliers ou transfrontaliers : Mékong, Brahmapoutre, Amour, Irtych.
3. Afficher « Longueur : 6 300 km » dans les fiches.

## Étape B — Noms connus en Europe affichés avec le pinyin

Règle : quand un lieu a un nom courant en français ou à l'international, on l'affiche **avec** le pinyin, sous la forme « Nom connu / pinyin ». Exemples :
- Everest / Zhumulangma Feng
- K2 / Qiaogeli Feng
- Himalaya / Ximalaya Shan
- Yangtsé / Chang Jiang
- Fleuve Jaune / Huang He
- Mékong / Lancang Jiang
- Pékin / Beijing
- Canton / Guangzhou
- Nankin / Nanjing

Le Guangzhou que tu as visité s'appelle « Canton » en français.

1. Ajouter un champ facultatif `knownAs` (nom français ou international, accents autorisés) au modèle et aux données. Le pinyin reste `name`.
2. Affichage : fiches, consignes (« Trouver : Everest / Zhumulangma Feng ») et noms sur la carte (nom court).
3. Le mode Saisir accepte les deux, comme aujourd'hui avec les alias.
4. Tests : validation (accents autorisés dans `knownAs`, pas dans `name`) et affichage.

## Étape C — Prononciation (bouton son)

**Choix recommandé : la synthèse vocale d'Android** (`TextToSpeech`), en mandarin (`zh-CN`), qui lit les **caractères chinois** (`hanzi`) : c'est ainsi que la prononciation est la plus juste.
- Avantages : aucun fichier audio à fournir (rien à télécharger ni à licencier), et tous les lieux sont couverts, y compris les futurs.
- Point d'attention : la voix chinoise doit être présente sur le téléphone. Sur le Pixel, Google TTS la propose ; un téléchargement unique peut être nécessaire (Paramètres → Système → Synthèse vocale), ensuite cela fonctionne hors ligne. L'app détecte l'absence de voix et explique comment l'installer.
- Alternative écartée pour l'instant : des enregistrements audio (Wikimedia Commons, CC-BY-SA). La couverture est incomplète et il y aurait des attributions à gérer.

1. Ajouter un champ `pinyinTones` (« Sìchuān », « Zhūmùlǎngmǎ Fēng ») affiché sous le nom. Il aide à lire la prononciation, et la saisie reste tolérante aux tons.
2. Bouton 🔊 dans les fiches (Apprendre, retour de réponse en jeu) : lit le hanzi.
3. Composant `Pronouncer` isolé (initialisation, langue, arrêt du moteur), sans dépendance du moteur de jeu.

## Étape D — Fiches « mini-Wikipédia » (mode Apprendre)

Contenu dans un **fichier séparé** `assets/geo/facts.json`, indexé par identifiant (même principe que les spécialités culinaires de la V3). Il est rédigé par Claude à partir de connaissances générales, marqué « à vérifier », et relu par toi.

Rubriques pour une **province** : 5 ou 6 lignes courtes, pas plus.
1. Population et superficie.
2. Langues ou dialectes parlés (mandarin, cantonais, wu, min, hakka, tibétain, ouïghour…).
3. Histoire en 2 ou 3 phrases (périodes clés, grands événements).
4. Économie : ce qu'elle produit ou exporte.
5. Plat emblématique.
6. Une ou deux anecdotes insolites.

Rubriques pour une **ville** : population (déjà présente), langue ou dialecte, rôle (capitale, port, industrie…), une ou deux anecdotes, un monument ou lieu célèbre.

Rubriques pour un **cours d'eau** ou une **montagne** : 2 ou 3 faits (longueur ou altitude, rôle historique, anecdote).

Ton : factuel et neutre, en particulier pour Xizang, Xinjiang, Taïwan, Hong Kong et les territoires disputés. On présente les faits sans prendre parti. On ne fait pas de classement de sécurité ou de pauvreté sans source chiffrée ; on peut mentionner le PIB par habitant avec sa source.

1. Modèle `PlaceFacts` + chargement + validation (identifiants existants, longueur maximale des textes).
2. Contenu, par lots relus : provinces du Nord, du Sud, unités spéciales, puis villes, fleuves et montagnes.
3. Interface : section « En savoir plus » dépliable dans le panneau du mode Apprendre. Le panneau défile, ou s'agrandit en feuille glissante.

## Étape E — Fiche compacte pendant les quiz

Dans le retour de réponse (Trouver, Éliminer, Saisir) : **4 à 5 lignes au maximum**. Par exemple : nom connu / pinyin, hanzi, population, langue, capitale ou province, et le bouton 🔊.

## Ordre et estimation

A, puis B, puis C, puis D (provinces), puis E, puis D (villes, cours d'eau, montagnes). A et B sont rapides. C est indépendante et peut se faire à tout moment. D est la plus longue, à cause du volume de texte à rédiger et à relire.
