# Compiler et installer l'application

## Sur l'émulateur (développement quotidien)

1. Android Studio → **File → Open…** → dossier `C:\Code\China map quiz`.
2. En haut de la fenêtre : module **app**, appareil **Medium Phone API 37**.
3. ▶ **Run**. L'émulateur démarre, puis l'app s'installe et s'ouvre.

Dans l'émulateur, pour simuler un pincement à deux doigts : maintenir **Ctrl** et glisser avec la souris.

## Sur le Pixel 8 Pro, par câble USB (recommandé)

### 1. Activer le mode développeur (une seule fois)

1. Sur le téléphone : **Paramètres → À propos du téléphone**.
2. Tout en bas, touchez **Numéro de build** 7 fois de suite. Le téléphone demande votre code de déverrouillage, puis affiche « Vous êtes maintenant développeur ».
3. Revenez dans **Paramètres → Système → Options pour les développeurs**.
4. Activez **Débogage USB** et confirmez.

### 2. Brancher le téléphone

1. Branchez le Pixel au PC avec un câble USB-C capable de transférer des données. Certains câbles ne font que charger : si rien ne se passe, essayez-en un autre.
2. Déverrouillez le téléphone. La fenêtre **« Autoriser le débogage USB ? »** apparaît, avec l'empreinte de la clé du PC.
3. Cochez **« Toujours autoriser à partir de cet ordinateur »** et touchez **Autoriser**.

Si la fenêtre n'apparaît pas : déroulez les notifications, touchez « Recharge de cet appareil via USB » et choisissez **Transfert de fichiers**.

### 3. Lancer l'app depuis Android Studio

1. Dans la liste des appareils en haut, le téléphone apparaît sous le nom **Google Pixel 8 Pro**.
2. Sélectionnez-le, puis ▶ **Run**.
3. L'app s'installe et s'ouvre sur le téléphone. Elle reste installée après débranchement, avec l'icône « China Map Quiz ».

### Vérifier en ligne de commande (facultatif)

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Le téléphone doit apparaître avec l'état `device`. Les autres états :
- `unauthorized` : la fenêtre d'autorisation de l'étape 2 n'a pas été acceptée.
- Liste vide : le câble ou le mode USB est en cause.

## Sans câble : télécharger l'APK depuis GitHub

Chaque version publiée a une page **Release** sur GitHub, avec le fichier `app-debug.apk` en pièce jointe.

1. Sur le téléphone, ouvrez dans Chrome la page **Releases** du dépôt, puis la dernière version.
2. Touchez `app-debug.apk` dans la section « Assets ».
3. Ouvrez le fichier téléchargé. La première fois, Android demande d'autoriser Chrome à installer des applications inconnues : acceptez pour cette source uniquement.
4. Installez. Une nouvelle version remplace l'ancienne et conserve la progression, à condition d'être signée avec la même clé : c'est le cas des APK debug compilés sur le même PC.

Publier une version : sur la page du dépôt, **Releases → Draft a new release**, créez le tag (`v1.1`), puis faites glisser `app\build\outputs\apk\debug\app-debug.apk` dans « Attach binaries ».

## Sans câble : copier l'APK soi-même

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

1. Copiez `app\build\outputs\apk\debug\app-debug.apk` sur le téléphone : Google Drive, e-mail à vous-même, ou câble en mode transfert de fichiers.
2. Sur le téléphone, ouvrez le fichier. Android demande d'autoriser l'installation d'applications inconnues depuis l'application utilisée (Drive, Fichiers…) : acceptez pour cette source uniquement.
3. Installez.

Cet APK est une version **debug** : signée avec une clé de développement, pas destinée à être distribuée. Une version **release** signée demandera une clé de signature personnelle, à ne jamais versionner dans Git (voir `.gitignore`).

## Tests et données

```powershell
.\gradlew.bat testDebugUnitTest            # 138 tests unitaires (sur le PC, sans émulateur), décrits dans docs/tests-unitaires.md
python tools/build_tests_pdf.py            # régénérer docs/tests-unitaires.pdf après modification du .md
python tools/download_natural_earth.py     # sources cartographiques (une fois)
python tools/build_map_geometry.py         # régénérer la carte après modification du script
```
