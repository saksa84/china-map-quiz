"""Produit docs/tests-unitaires.pdf à partir de docs/tests-unitaires.md.

Usage (depuis la racine du projet) :
    python tools/build_tests_pdf.py

Le Markdown est converti en HTML par un petit convertisseur maison, limité à ce que le document
utilise (titres, paragraphes, listes, tableaux, blocs de code, `code`, **gras**, *italique*), puis
Microsoft Edge, installé avec Windows, imprime la page en PDF. Aucune bibliothèque à installer.
"""

import html
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "docs" / "tests-unitaires.md"
OUTPUT = ROOT / "docs" / "tests-unitaires.pdf"
EDGE_PATHS = [
    Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"),
    Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"),
]

CSS = """
@page { size: A4; margin: 16mm 14mm; }
body { font-family: "Segoe UI", Arial, sans-serif; font-size: 10pt; color: #1f2328; line-height: 1.45; }
h1 { font-size: 20pt; color: #0b5563; margin: 0 0 4pt; }
h2 { font-size: 13pt; color: #0b5563; border-bottom: 1px solid #c9d6da; padding-bottom: 2pt; margin: 18pt 0 6pt; }
h2 { break-after: avoid; }
p { margin: 4pt 0; }
ul { margin: 4pt 0 4pt 16pt; padding: 0; }
code { font-family: Consolas, monospace; font-size: 8.8pt; background: #eef3f4; padding: 0 2pt; border-radius: 2pt; }
pre { background: #eef3f4; padding: 6pt 8pt; border-radius: 3pt; font-size: 8.8pt; white-space: pre-wrap; }
pre code { background: none; padding: 0; }
table { border-collapse: collapse; width: 100%; margin: 6pt 0 10pt; }
th, td { border: 1px solid #c9d6da; padding: 3pt 5pt; vertical-align: top; text-align: left; }
th { background: #0b5563; color: white; }
tr { break-inside: avoid; }
td:first-child code { white-space: nowrap; }
td.num, th.num { text-align: right; }
"""


def inline(text):
    """`code`, **gras** et *italique*, le reste étant échappé pour le HTML."""
    parts = re.split(r"(`[^`]+`)", text)
    out = []
    for part in parts:
        if part.startswith("`") and part.endswith("`") and len(part) > 1:
            out.append(f"<code>{html.escape(part[1:-1])}</code>")
            continue
        part = html.escape(part, quote=False)
        part = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", part)
        part = re.sub(r"(?<![\w*])\*([^*]+?)\*(?![\w*])", r"<em>\1</em>", part)
        part = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1", part)
        out.append(part)
    return "".join(out)


def table(lines):
    rows = [[c.strip() for c in line.strip().strip("|").split("|")] for line in lines]
    header, align, body = rows[0], rows[1], rows[2:]
    right = [cell.endswith(":") and not cell.startswith(":") for cell in align]

    def cells(row, tag):
        num = ' class="num"'
        return "".join(f"<{tag}{num if right[i] else ''}>{inline(c)}</{tag}>" for i, c in enumerate(row))

    rows_html = "".join(f"<tr>{cells(r, 'td')}</tr>" for r in body)
    return f"<table><thead><tr>{cells(header, 'th')}</tr></thead><tbody>{rows_html}</tbody></table>"


def to_html(markdown):
    lines = markdown.splitlines()
    out, i = [], 0
    while i < len(lines):
        line = lines[i]
        if line.startswith("```"):
            j = i + 1
            while j < len(lines) and not lines[j].startswith("```"):
                j += 1
            out.append("<pre><code>" + html.escape("\n".join(lines[i + 1:j])) + "</code></pre>")
            i = j + 1
        elif line.startswith("## "):
            out.append(f"<h2>{inline(line[3:])}</h2>")
            i += 1
        elif line.startswith("# "):
            out.append(f"<h1>{inline(line[2:])}</h1>")
            i += 1
        elif line.startswith("|"):
            j = i
            while j < len(lines) and lines[j].startswith("|"):
                j += 1
            out.append(table(lines[i:j]))
            i = j
        elif line.startswith("- "):
            items = []
            while i < len(lines) and lines[i].startswith("- "):
                items.append(f"<li>{inline(lines[i][2:])}</li>")
                i += 1
            out.append("<ul>" + "".join(items) + "</ul>")
        elif line.strip():
            j = i
            while j < len(lines) and lines[j].strip() and not lines[j].startswith(("#", "|", "- ", "```")):
                j += 1
            out.append("<p>" + inline(" ".join(lines[i:j])) + "</p>")
            i = j
        else:
            i += 1
    body = "\n".join(out)
    return f'<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>Tests unitaires — China Map Quiz</title><style>{CSS}</style></head><body>{body}</body></html>'


def main() -> int:
    edge = next((p for p in EDGE_PATHS if p.exists()), None)
    if edge is None:
        print("Microsoft Edge introuvable : impossible d'imprimer le PDF.")
        return 1
    with tempfile.TemporaryDirectory() as tmp:
        page = Path(tmp) / "tests-unitaires.html"
        page.write_text(to_html(SOURCE.read_text(encoding="utf-8")), encoding="utf-8")
        subprocess.run(
            [str(edge), "--headless", "--disable-gpu", "--no-pdf-header-footer",
             f"--user-data-dir={Path(tmp) / 'edge'}", f"--print-to-pdf={OUTPUT}", page.as_uri()],
            check=True, capture_output=True, timeout=120,
        )
    print(f"PDF écrit : {OUTPUT} ({OUTPUT.stat().st_size // 1024} Ko)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
