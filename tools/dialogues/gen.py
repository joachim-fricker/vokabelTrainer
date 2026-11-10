"""Erzeugt app/src/main/assets/dialogues/<sprache>.json aus pt_br.py / es_ar.py und prueft die Daten.

    cd tools/dialogues
    python3 gen.py

Ein Dialog: id, Titel (deutsch), Titel in der Lernsprache, Situation, passende Lektion,
Rollen [Gegenueber, Du] und Zeilen ("a"|"b", Text, Deutsch[, Hinweis]).
"a" = Gegenueber (Rolle 0), "b" = Du (Rolle 1). Hinweise in Klammern im Text werden nicht vorgelesen.
"""
import json
import os
import sys

import es_ar
import pt_br

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "dialogues")
LESSONS = os.path.join(ROOT, "app", "src", "main", "assets", "lessons")


def lesson_ids(lang):
    ids = set()
    for f in os.listdir(os.path.join(LESSONS, lang)):
        if f.endswith(".json"):
            with open(os.path.join(LESSONS, lang, f), encoding="utf-8") as fh:
                ids.add(json.load(fh)["id"])
    return ids


def build(lang, dialogues):
    errors = []
    lessons = lesson_ids(lang)
    seen = set()
    out = []
    for d in dialogues:
        did = d["id"]
        if did in seen:
            errors.append(f"{did}: doppelte ID")
        seen.add(did)
        if d["lesson"] not in lessons:
            errors.append(f"{did}: unbekannte Lektion {d['lesson']}")
        lines = []
        for i, line in enumerate(d["lines"]):
            role, text, de = line[0], line[1], line[2]
            note = line[3] if len(line) > 3 else ""
            if role not in ("a", "b"):
                errors.append(f"{did} Zeile {i + 1}: Rolle {role!r}")
            if not text.strip() or not de.strip():
                errors.append(f"{did} Zeile {i + 1}: Text oder Deutsch fehlt")
            item = {"role": 0 if role == "a" else 1, "text": text, "de": de}
            if note:
                item["note"] = note
            lines.append(item)
        for r in (0, 1):
            n = len({l["text"].lower() for l in lines if l["role"] == r})
            if n < 3:
                errors.append(f"{did}: Rolle {d['roles'][r]} hat nur {n} verschiedene Saetze (min. 3 fuer die Auswahl)")
        out.append({
            "id": did, "title": d["title"], "titleTarget": d["titleTarget"], "situation": d["situation"],
            "lesson": d["lesson"], "roles": d["roles"], "lines": lines,
        })
    return out, errors


def main():
    os.makedirs(OUT, exist_ok=True)
    ok = True
    for lang, mod in (("pt-BR", pt_br), ("es-AR", es_ar)):
        data, errors = build(lang, mod.DIALOGUES)
        for e in errors:
            print(f"[{lang}] FEHLER {e}")
        ok = ok and not errors
        path = os.path.join(OUT, f"{lang}.json")
        with open(path, "w", encoding="utf-8") as fh:
            json.dump({"language": lang, "dialogues": data}, fh, ensure_ascii=False, indent=1)
            fh.write("\n")
        n = sum(len(d["lines"]) for d in data)
        print(f"{lang}: {len(data)} Dialoge, {n} Zeilen -> {os.path.relpath(path, ROOT)}")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
