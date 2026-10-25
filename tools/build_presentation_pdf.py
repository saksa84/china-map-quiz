"""Produit docs/presentation.pdf : une présentation illustrée du projet et de sa création.

Usage (depuis la racine du projet) :
    python tools/build_presentation_pdf.py

Le texte est écrit ici, les chiffres (lieux, fiches, tests, commits) sont relus dans le dépôt pour
rester exacts, les illustrations sont les captures de docs/images/. Microsoft Edge, installé avec
Windows, imprime la page HTML en PDF. Aucune bibliothèque à installer.
"""

import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GEO = ROOT / "app" / "src" / "main" / "assets" / "geo"
IMAGES = ROOT / "docs" / "images"
OUTPUT = ROOT / "docs" / "presentation.pdf"
EDGE_PATHS = [
    Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"),
    Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"),
]


def entities(name):
    return json.loads((GEO / f"{name}.json").read_text(encoding="utf-8"))["entities"]


def count_tests():
    pattern = re.compile(r"@Test\b")
    return sum(len(pattern.findall(p.read_text(encoding="utf-8"))) for p in (ROOT / "app" / "src" / "test").rglob("*.kt"))


def git(*args):
    return subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True, encoding="utf-8").stdout.strip()


def version():
    text = (ROOT / "app" / "build.gradle.kts").read_text(encoding="utf-8")
    return re.search(r'versionName = "([^"]+)"', text).group(1)


def figure(name, caption):
    return f'<figure><img src="{(IMAGES / name).as_uri()}"><figcaption>{caption}</figcaption></figure>'


CSS = """
@page { size: A4; margin: 14mm 14mm; }
body { font-family: "Segoe UI", Arial, sans-serif; font-size: 10pt; color: #1f2328; line-height: 1.45; }
.cover { background: linear-gradient(#0e4f57, #138c93); color: white; padding: 12mm 14mm 10mm; margin: -14mm -14mm 6mm; }
.cover h1 { font-size: 30pt; margin: 0; }
.cover p { font-size: 12pt; margin: 6pt 0 0; color: #e6f2f3; }
.cover .tag { display: inline-block; background: #ffc94d; color: #0a3b41; font-weight: 600; padding: 2pt 8pt; border-radius: 3pt; margin-top: 10pt; }
h2 { font-size: 15pt; color: #0b5563; border-bottom: 2px solid #ffc94d; padding-bottom: 2pt; margin: 16pt 0 6pt; break-after: avoid; }
h3 { font-size: 11pt; color: #0b5563; margin: 10pt 0 3pt; break-after: avoid; }
p { margin: 4pt 0; }
ul { margin: 3pt 0 3pt 15pt; padding: 0; }
li { margin: 1.5pt 0; }
.stats { display: flex; gap: 6pt; margin: 8pt 0; }
.stat { flex: 1; background: #eef3f4; border-radius: 4pt; padding: 6pt; text-align: center; }
.stat b { display: block; font-size: 17pt; color: #0b5563; }
.stat span { font-size: 8.5pt; color: #4a5a5e; }
.gallery { display: flex; gap: 8pt; justify-content: space-between; margin: 6pt 0; break-inside: avoid; }
figure { margin: 0; flex: 1; text-align: center; }
figure img { max-width: 100%; max-height: 88mm; border-radius: 5pt; border: 1px solid #c9d6da; }
figcaption { font-size: 8.5pt; color: #4a5a5e; margin-top: 2pt; }
table { border-collapse: collapse; width: 100%; margin: 4pt 0 8pt; font-size: 9pt; }
th, td { border: 1px solid #c9d6da; padding: 3pt 5pt; vertical-align: top; text-align: left; }
th { background: #0b5563; color: white; }
tr { break-inside: avoid; }
.note { background: #fff6dc; border-left: 3pt solid #ffc94d; padding: 5pt 8pt; margin: 6pt 0; }
.page { break-before: page; }
.small { font-size: 8.5pt; color: #4a5a5e; }
"""


def html():
    prov = [e for e in entities("provinces") if e["type"] != "country"]
    regions = entities("regions")
    cities = entities("cities")
    rivers = entities("rivers")
    mountains = entities("mountains")
    lakes = entities("lakes")
    parks = entities("parks")
    facts = json.loads((GEO / "facts.json").read_text(encoding="utf-8"))["facts"]
    places = len(prov) + len(regions) + len(entities("islands")) + len(cities) + len(rivers) + len(mountains) + len(lakes) + len(parks)
    reviewed = sum(1 for f in facts if f.get("reviewed"))
    tests = count_tests()
    commits = git("rev-list", "--count", "HEAD")
    first, last = git("log", "--reverse", "--format=%ad", "--date=short").splitlines()[0], git("log", "-1", "--format=%ad", "--date=short")
    kotlin_lines = sum(len(p.read_text(encoding="utf-8").splitlines()) for p in (ROOT / "app" / "src" / "main" / "java").rglob("*.kt"))

    return f"""<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>China Map Quiz — présentation</title><style>{CSS}</style></head><body>
<div class="cover">
  <h1>China Map Quiz</h1>
  <p>Une application Android pour apprendre la géographie de la Chine sur une carte : provinces, villes, fleuves, montagnes, lacs et parcs.</p>
  <p>Hors ligne, sans compte, sans publicité. Réalisée entièrement avec une IA générative, Claude Code.</p>
  <span class="tag">Version {version()}</span>
</div>

<div class="stats">
  <div class="stat"><b>{places}</b><span>lieux sur la carte</span></div>
  <div class="stat"><b>{len(facts)}</b><span>fiches « En savoir plus », {reviewed} vérifiées avec sources</span></div>
  <div class="stat"><b>{tests}</b><span>tests automatisés</span></div>
  <div class="stat"><b>{commits}</b><span>commits, du {first} au {last}</span></div>
</div>

<h2>Le projet en bref</h2>
<p>China Map Quiz s'inspire des applications de quiz géographique de Qbis Studio (<i>World Map Quiz</i>, <i>France Map Quiz</i>) : on touche une région sur une carte, on enchaîne les questions contre la montre, on revoit ses erreurs. L'auteur voulait la même chose pour la Chine, en apprenant les noms en pinyin et leur prononciation. C'est un projet personnel et indépendant : aucun code, texte ni dessin de ces applications n'a été repris.</p>

<div class="gallery">
  {figure("01-accueil.jpg", "L'accueil : six catégories et leur progression")}
  {figure("03-apprendre-regions.jpg", "Apprendre : la carte des grandes régions")}
  {figure("07-trouver-fiche.jpg", "Trouver : la fiche après une réponse, chrono en pause")}
</div>

<h2>Ce que l'on peut faire</h2>
<table>
<tr><th>Catégorie</th><th>Contenu</th></tr>
<tr><td>Régions</td><td>{len(prov)} unités provinciales, {len(regions)} grandes régions, îles Paracel (présentées comme territoire disputé)</td></tr>
<tr><td>Villes</td><td>{len(cities)} villes de plus de 3 millions d'habitants (recensement 2020), plus Hong Kong et Taipei</td></tr>
<tr><td>Fleuves</td><td>{len(rivers)} cours d'eau, grands fleuves et affluents, avec leur longueur</td></tr>
<tr><td>Montagnes</td><td>{len(mountains)} chaînes et sommets, avec leur altitude</td></tr>
<tr><td>Lacs</td><td>{len(lakes)} lacs et lacs de barrage, avec superficie et altitude</td></tr>
<tr><td>Parcs</td><td>{len(parks)} parcs nationaux et sites naturels, avec leur statut exact (parc national, patrimoine mondial, géoparc)</td></tr>
</table>
<ul>
  <li><b>Apprendre</b> : explorer la carte, toucher un lieu pour lire sa fiche (histoire, langues, plat, légendes, faits insolites).</li>
  <li><b>Trouver</b> : toucher le lieu demandé, une seule chance ; la fiche se déplie ensuite, chrono en pause.</li>
  <li><b>Éliminer</b> : les lieux trouvés s'effacent, les lieux manqués restent ; à la fin, la carte montre ses lacunes.</li>
  <li><b>Saisir</b> : écrire le nom du lieu coloré ; tons, accents et majuscules sont ignorés, Shanxi et Shaanxi restent distincts.</li>
  <li>Prononciation en mandarin (bouton 🔊), sons de réponse, records, révision des lieux les plus souvent manqués.</li>
</ul>

<div class="gallery">
  {figure("05-trouver.jpg", "Trouver : un toucher sélectionne, Confirmer valide")}
  {figure("10-eliminer.jpg", "Éliminer : l'erreur en rouge, le bon fleuve en vert")}
  {figure("08-parcs-fiche.jpg", "Parcs : statut UNESCO et légende de Jiuzhaigou")}
</div>

<h2>Comment l'application a été construite</h2>
<p>L'auteur a des bases en programmation (Python, MATLAB, C++) mais n'avait jamais fait d'application Android. Tout le code ({kotlin_lines} lignes de Kotlin), les données, les tests et la documentation ont été produits par <b>Claude Code</b>, l'assistant de programmation d'Anthropic, au fil d'une conversation. L'auteur a fixé le besoin et les règles dans un fichier d'instructions (<code>CLAUDE.md</code>), pris les décisions et testé sur son téléphone, un Google Pixel 8 Pro.</p>
<h3>Une méthode par petites étapes</h3>
<ul>
  <li>Chaque étape commence par un plan court et se termine par la compilation, les tests, un essai sur l'émulateur, puis un commit sur demande.</li>
  <li>Les décisions qui reviennent à l'auteur lui sont posées : traitement de Taïwan et des Paracel, source des contours de parcs, noms à afficher.</li>
  <li>Les règles de jeu sont écrites dans un moteur indépendant de l'écran, pour pouvoir être testées sans téléphone.</li>
</ul>

<h3>Les versions</h3>
<table>
<tr><th>Version</th><th>Apport</th></tr>
<tr><td>1.0</td><td>Carte interactive, quatre catégories, modes Apprendre, Trouver, Éliminer et Saisir, progression et révision des erreurs</td></tr>
<tr><td>1.1</td><td>Noms connus en Europe (« Pékin / Beijing »), pinyin avec tons, prononciation, fiches « En savoir plus », longueurs des fleuves</td></tr>
<tr><td>1.2</td><td>Après un premier essai sur le téléphone : réécoute de la consigne, sons de réponse, Éliminer à une seule chance, fiche dépliable en jeu</td></tr>
<tr><td>2.0</td><td>Lacs et parcs, légendes et faits insolites, puis vérification de toutes les fiches avec des sources citées</td></tr>
</table>

<h2>Des données sourcées</h2>
<ul>
  <li><b>Cartes</b> : Natural Earth 1:10m (domaine public), simplifiées par des scripts Python versionnés dans <code>tools/</code>.</li>
  <li><b>Parcs et petits lacs</b> : contours OpenStreetMap (© les contributeurs d'OpenStreetMap, licence ODbL), dans un fichier séparé.</li>
  <li><b>Chiffres</b> : recensement chinois de 2020, longueurs et superficies tirées de Wikipédia (révision citée), statuts tirés de la liste officielle de l'UNESCO.</li>
</ul>
<div class="note"><b>Vérifier ce que l'IA écrit.</b> Les fiches ont d'abord été rédigées par Claude de mémoire. À la demande de l'auteur, chacune a ensuite été confrontée phrase par phrase aux articles Wikipédia anglais et chinois, à l'UNESCO ou à des sites officiels. Ce qui n'était confirmé par aucune source a été retiré. Plus de la moitié des fiches ont été corrigées ; exemples : Shenzhen comptait 30 000 habitants en 1980 et non 300 000 ; le canal de Lingqu n'est plus en service depuis 1956 ; Er Hai n'est pas « en forme d'oreille ». Registre complet : <code>docs/verification-fiches.md</code>.</div>

<div class="gallery">
  {figure("04-fiche-chongqing.jpg", "Fiche d'une province")}
  {figure("09-lacs.jpg", "Lacs : superficie et altitude")}
  {figure("12-parametres.jpg", "Paramètres et crédits")}
</div>

<h2>Qualité</h2>
<p>{tests} tests automatisés tournent sur l'ordinateur en quelques secondes : validation des données, règles de jeu, détection du toucher sur la carte, et contrôles sur les vraies données livrées (par exemple : chaque ville tombe dans sa province, aucun fleuve n'a de trou, seuls les cinq parcs de 2021 sont présentés comme parcs nationaux). Ils sont décrits un par un dans <code>docs/tests-unitaires.pdf</code>.</p>

<h2>Installer</h2>
<p>Sur la page <b>Releases</b> du dépôt <code>github.com/saksa84/china-map-quiz</code>, télécharger le fichier APK de la dernière version depuis le téléphone, puis l'ouvrir et autoriser l'installation. La prononciation utilise la voix « chinois (Chine) » de la synthèse vocale du téléphone.</p>
<p class="small">China Map Quiz est un projet indépendant, ni affilié à Qbis Studio, ni approuvé par lui. Document généré par <code>tools/build_presentation_pdf.py</code>.</p>
</body></html>"""


def main() -> int:
    edge = next((p for p in EDGE_PATHS if p.exists()), None)
    if edge is None:
        print("Microsoft Edge introuvable : impossible d'imprimer le PDF.")
        return 1
    with tempfile.TemporaryDirectory() as tmp:
        page = Path(tmp) / "presentation.html"
        page.write_text(html(), encoding="utf-8")
        subprocess.run(
            [str(edge), "--headless", "--disable-gpu", "--no-pdf-header-footer", "--allow-file-access-from-files",
             f"--user-data-dir={Path(tmp) / 'edge'}", f"--print-to-pdf={OUTPUT}", page.as_uri()],
            check=True, capture_output=True, timeout=120,
        )
    print(f"PDF écrit : {OUTPUT} ({OUTPUT.stat().st_size // 1024} Ko)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
