"""Génère les deux sons de réponse du quiz : bonne réponse et mauvaise réponse.

Usage (depuis la racine du projet) :
    python tools/build_sounds.py

Sorties : app/src/main/res/raw/answer_correct.wav et answer_wrong.wav (mono, 16 bits, 44,1 kHz).

Les sons sont synthétisés ici, note par note, avec la bibliothèque standard de Python : aucun
fichier audio extérieur, donc aucune question de licence. Le script est déterministe : le relancer
redonne exactement les mêmes fichiers.
"""

import math
import struct
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "app" / "src" / "main" / "res" / "raw"
RATE = 44_100


def note(freq, duration, harmonics, decay, attack=0.005):
    """Une note : somme d'harmoniques (rang, amplitude), attaque brève puis décroissance exponentielle."""
    samples = []
    for i in range(int(RATE * duration)):
        t = i / RATE
        envelope = min(1.0, t / attack) * math.exp(-decay * t)
        value = sum(amp * math.sin(2 * math.pi * freq * rank * t) for rank, amp in harmonics)
        samples.append(envelope * value)
    # Fin adoucie sur 10 ms pour éviter un clic.
    fade = int(RATE * 0.01)
    for i in range(1, fade + 1):
        samples[-i] *= i / fade
    return samples


def silence(duration):
    return [0.0] * int(RATE * duration)


def write(name, samples, peak=0.7):
    scale = peak / max(abs(s) for s in samples)
    frames = b"".join(struct.pack("<h", round(s * scale * 32767)) for s in samples)
    path = OUT / name
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(frames)
    print(f"{path.relative_to(ROOT)} : {len(samples) / RATE:.2f} s")


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    # Bonne réponse : « ding-ding » clair et montant (mi 5 puis la 5), timbre de clochette.
    bell = [(1, 1.0), (2, 0.35), (3, 0.12)]
    write("answer_correct.wav", note(659.25, 0.11, bell, 18) + note(880.0, 0.32, bell, 9))
    # Mauvaise réponse : deux notes graves descendantes, timbre plus rugueux (harmoniques impaires).
    buzz = [(1, 1.0), (3, 0.4), (5, 0.2), (7, 0.1)]
    write("answer_wrong.wav", note(233.08, 0.16, buzz, 6) + silence(0.03) + note(174.61, 0.34, buzz, 7))


if __name__ == "__main__":
    main()
