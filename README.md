# China Map Quiz

Application Android personnelle de quiz géographique sur la Chine : provinces, puis préfectures et villes. Fonctionne entièrement hors ligne. Les noms sont affichés en pinyin.

Projet en cours de développement : voir [docs/plan-initial.md](docs/plan-initial.md) pour le plan et les décisions.

## Prérequis

- Android Studio 2026.2 ou plus récent (il fournit le JDK utilisé par Gradle)
- SDK Android API 37
- Un émulateur ou un téléphone Android 8.0+ (API 26) avec le débogage USB activé

## Ouvrir et lancer

1. Android Studio → **File → Open…** → choisir ce dossier, celui qui contient `settings.gradle.kts`.
2. Attendre la fin de la synchronisation Gradle.
3. Choisir l'appareil, puis ▶ **Run**.

## Ligne de commande (PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest   # tests unitaires
.\gradlew.bat assembleDebug       # APK debug
```

APK debug généré : `app/build/outputs/apk/debug/app-debug.apk`

Installation sur un téléphone branché en USB :

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

## Organisation

| Dossier | Contenu |
|---|---|
| `app/src/main/java/.../data` | Modèle des entités géographiques, lecture et validation des données |
| `app/src/main/assets/geo` | Données géographiques (à venir) |
| `app/src/test` | Tests unitaires (exécutés sur le PC, sans émulateur) |
| `docs/` | Plan, sources de données, licences |
