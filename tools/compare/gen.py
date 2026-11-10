"""Erzeugt app/src/main/assets/compare/pairs.json: Vergleich Portugiesisch (pt-BR) <-> Spanisch (es-AR).

    cd tools/compare
    python3 gen.py            # schreibt die Datei und zeigt eine Uebersicht
    python3 gen.py --list     # zusaetzlich alle Paare je Gruppe ausgeben

Paare entstehen ueber die deutsche Uebersetzung der Lektionswoerter (gemeinsame Variante, getrennt durch , ; /).
Einteilung: same (gleich bis auf Akzente/Artikel), similar (aehnlich oder typisches Muster), different.
Korrekturen von Hand: OVERRIDES (Gruppe erzwingen), EXCLUDE (Paar weglassen), EXTRA_PAIRS (zusaetzliche Paare).
Falsche Freunde: false_friends.py (von Hand gepflegt).
"""
import difflib
import glob
import json
import os
import re
import sys
import unicodedata

from false_friends import FALSE_FRIENDS

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
LESSONS = os.path.join(ROOT, "app", "src", "main", "assets", "lessons")
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "compare", "pairs.json")

# (pt-Wort-ID, es-Wort-ID) -> Gruppe
OVERRIDES = {
    ("num_2", "dos"): "similar",
    ("muito", "muy"): "similar",
    ("cartao", "tarjeta"): "different",
    ("cardapio", "carta"): "different",
    ("mala", "valija"): "different",
    ("cofre", "caja_fuerte"): "different",
    ("batata_frita", "papas_fritas"): "different",
}
# (pt-Wort-ID, es-Wort-ID), die nicht zusammenpassen
EXCLUDE = {("senhora", "ustedes")}
# zusaetzliche Paare (pt-Wort-ID, es-Wort-ID), wenn die Uebersetzungen nicht automatisch passen
EXTRA_PAIRS = []

PATTERNS = [
    ("cao", "-ção → -ción", "Portugiesisch -ção (Mehrzahl -ções) wird im Spanischen zu -ción (-ciones)."),
    ("dade", "-dade → -dad", "Portugiesisch -dade wird im Spanischen zu -dad."),
    ("ao", "-ão → -ón, -án, -ano", "Portugiesisch -ão (ohne -ção) endet im Spanischen meist auf -ón, -án oder -ano."),
    ("vel", "-vel → -ble", "Portugiesisch -vel wird im Spanischen zu -ble."),
    ("eiro", "-ei- → -e-", "Portugiesisch -ei- (oft -eiro/-eira) ist im Spanischen meist nur -e- (-ero/-era)."),
    ("ou", "ou → o", "Portugiesisch ou ist im Spanischen meist nur o."),
    ("nh", "nh → ñ", "Portugiesisch nh schreibt man im Spanischen ñ – gleicher Laut."),
    ("lh", "lh → j / ll", "Portugiesisch lh wird im Spanischen meist zu j oder ll."),
    ("ch", "ch- → ll-", "Am Wortanfang wird portugiesisch ch- im Spanischen oft zu ll-."),
    ("fh", "f- → h-", "Am Wortanfang wird portugiesisch f- im Spanischen oft zu h- (stumm)."),
    ("ue", "o → ue", "Betontes o im Portugiesischen wird im Spanischen oft zu ue."),
    ("ie", "e → ie", "Betontes e im Portugiesischen wird im Spanischen oft zu ie."),
    ("mn", "-m → -n", "Portugiesisch endet oft auf -m (Nasal), Spanisch auf -n."),
    ("nl", "n/l fällt weg", "Zwischen Vokalen fällt im Portugiesischen oft ein n oder l weg, das das Spanische behalten hat."),
]

KIND_ORDER = {"different": 0, "similar": 1, "same": 2}


def load(lang):
    words = []
    for f in sorted(glob.glob(os.path.join(LESSONS, lang, "*.json"))):
        with open(f, encoding="utf-8") as fh:
            d = json.load(fh)
        for i, w in enumerate(d["words"]):
            w["lesson"] = d["id"]
            w["order"] = (d["order"], i)
            words.append(w)
    return words


def variants(de):
    """deutsche Uebersetzung -> {normalisiert: Original}"""
    out = {}
    for v in re.split(r"[,;/]", re.sub(r"\([^)]*\)", "", de)):
        orig = v.strip(" .!?")
        # Gross-/Kleinschreibung zaehlt (sie / Sie)
        norm = re.sub(r"^(der|die|das|ein|eine|einen) ", "", orig).strip()
        if norm:
            out.setdefault(norm, orig)
    return out


def bare(text, articles):
    t = re.sub(r"\([^)]*\)", "", text).strip().lower()
    for a in articles:
        if t.startswith(a + " "):
            t = t[len(a) + 1:]
            break
    return t.strip(" ¿?¡!.")


def plain(s):
    """ohne Akzente; ñ bleibt (ano und año sind verschiedene Woerter)"""
    s = s.replace("ñ", "\0")
    s = "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn")
    return s.replace("\0", "ñ")


def ratio(a, b):
    return difflib.SequenceMatcher(None, a, b).ratio()


def patterns(p, e):
    """p, e: ohne Artikel, kleingeschrieben (mit Akzenten)"""
    found = []
    pp, ee = plain(p), plain(e)
    if "ção" in p or "ções" in p:
        if "cion" in ee:
            found.append("cao")
    if "dade" in p and "dad" in e:
        found.append("dade")
    if re.search(r"(?<!ç)ão(s)?\b", p) and re.search(r"(on|ones|an|anes|ano|anos)\b", ee):
        found.append("ao")
    if re.search(r"vel\b|veis\b", p) and re.search(r"ble(s)?\b", e):
        found.append("vel")
    if "ei" in pp and "ei" not in ee and re.search(r"e(r|ro|ra)", ee) and ratio(pp.replace("ei", "e"), ee) > ratio(pp, ee):
        found.append("eiro")
    if "ou" in pp and "ou" not in ee and ratio(pp.replace("ou", "o"), ee) > ratio(pp, ee):
        found.append("ou")
    if "nh" in p and "ñ" in e:
        found.append("nh")
    if "lh" in p and ("j" in e or "ll" in e):
        found.append("lh")
    if p.startswith("ch") and e.startswith("ll"):
        found.append("ch")
    if p.startswith("f") and e.startswith("h"):
        found.append("fh")
    if "ue" in ee and "ue" not in pp and "o" in pp and ratio(pp, ee.replace("ue", "o")) > ratio(pp, ee):
        found.append("ue")
    if "ie" in ee and "ie" not in pp and "e" in pp and ratio(pp, ee.replace("ie", "e")) > ratio(pp, ee):
        found.append("ie")
    if re.search(r"m\b", pp) and re.search(r"n\b", ee) and ratio(re.sub(r"m\b", "n", pp), ee) > ratio(pp, ee):
        found.append("mn")
    base = ratio(pp, ee)
    for i, c in enumerate(ee):
        if c in "nl" and 0 < i < len(ee) - 1:
            shorter = ee[:i] + ee[i + 1:]
            if ratio(pp, shorter) >= max(0.75, base + 0.1):
                found.append("nl")
                break
    return found


def classify(p, e, pats):
    if plain(p) == plain(e):
        return "same"
    r = ratio(plain(p), plain(e))
    if r >= 0.6 or (pats and r >= 0.4):
        return "similar"
    return "different"


def main():
    pt, es = load("pt-BR"), load("es-AR")
    P = {w["id"]: w for w in pt}
    E = {w["id"]: w for w in es}
    pv = {w["id"]: variants(w["de"]) for w in pt}
    ev = {w["id"]: variants(w["de"]) for w in es}

    cands = []
    for p in pt:
        for e in es:
            if (p["id"], e["id"]) in EXCLUDE:
                continue
            shared = pv[p["id"]].keys() & ev[e["id"]].keys()
            if shared:
                full = re.sub(r"\([^)]*\)", "", p["de"]).strip() == re.sub(r"\([^)]*\)", "", e["de"]).strip()
                cands.append((full, len(shared), p["id"], e["id"]))
    cands.sort(key=lambda c: (not c[0], -c[1], P[c[2]]["order"], E[c[3]]["order"]))
    used_p, used_e, matched = set(), set(), []
    for pid, eid in EXTRA_PAIRS:
        used_p.add(pid)
        used_e.add(eid)
        matched.append((pid, eid))
    for _, _, pid, eid in cands:
        if pid in used_p or eid in used_e:
            continue
        used_p.add(pid)
        used_e.add(eid)
        matched.append((pid, eid))

    pairs = []
    for pid, eid in matched:
        p, e = P[pid], E[eid]
        pb, eb = bare(p["text"], ["o", "a", "os", "as"]), bare(e["text"], ["el", "la", "los", "las"])
        pats = patterns(pb, eb)
        kind = OVERRIDES.get((pid, eid)) or classify(pb, eb, pats)
        if p["de"] == e["de"]:
            de = p["de"]
        else:
            shared = [k for k in pv[pid] if k in ev[eid]]
            de = pv[pid][shared[0]] if shared else p["de"]
        pairs.append({
            "pt": pid, "ptText": p["text"], "ptLesson": p["lesson"],
            "es": eid, "esText": e["text"], "esLesson": e["lesson"],
            "de": de, "kind": kind, "patterns": pats if kind == "similar" else [],
            "_order": p["order"],
        })
    pairs.sort(key=lambda x: x["_order"])
    for x in pairs:
        del x["_order"]

    ff = []
    for item in FALSE_FRIENDS:
        pt_w, pt_de, es_w, es_de = item[:4]
        ff.append({"pt": pt_w, "ptDe": pt_de, "es": es_w, "esDe": es_de, "note": item[4] if len(item) > 4 else ""})

    used = {p for x in pairs for p in x["patterns"]}
    data = {
        "pairs": pairs,
        "patterns": [{"id": i, "title": t, "explanation": x} for i, t, x in PATTERNS if i in used],
        "falseFriends": ff,
    }
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as fh:
        json.dump(data, fh, ensure_ascii=False, indent=1)
        fh.write("\n")

    count = {k: sum(1 for x in pairs if x["kind"] == k) for k in KIND_ORDER}
    print(f"{len(pairs)} Paare: {count}; Muster: " +
          ", ".join(f"{i} {sum(1 for x in pairs if i in x['patterns'])}" for i, _, _ in PATTERNS))
    print(f"{len(ff)} falsche Freunde; ohne Partner: pt {len(pt) - len(used_p)}, es {len(es) - len(used_e)}")
    if "--list" in sys.argv:
        for k in KIND_ORDER:
            print(f"\n== {k}")
            for x in pairs:
                if x["kind"] == k:
                    print(f"  {x['ptText']:28} {x['esText']:28} {x['de']:30} {' '.join(x['patterns'])}")


if __name__ == "__main__":
    main()
