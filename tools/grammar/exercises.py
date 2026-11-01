"""
Grammatik-Uebungen aus den Lektionen erzeugen (fuer beide Sprachen).

    cd tools/grammar
    python3 exercises.py     # schreibt app/src/main/assets/exercises/pt-BR.json und es-AR.json

Arten (type):
  verbform     Verb im Satz: "Eu ___ (querer) um café."   (Saetze + Verbtabellen aus assets/verbs)
  serestar     ser oder estar: Luecke im Satz, zwei Knoepfe
  article      Artikel/Geschlecht: "___ mesa" -> a
  contraction  Praeposition + Artikel: "Estou ___ (em + o) hotel." -> no
  plural       Plural bilden: "a cidade -> as ___" -> cidades

options = alle Antwortmoeglichkeiten inkl. Loesung (fixedOrder: nicht mischen, z. B. ser | estar).
Jede Uebung gehoert zur Lektion ihres Satzes bzw. Wortes. words = Woerter, die man kennen sollte
(fuer "Heute lernen"). Vorher conj.py laufen lassen, falls sich Verben geaendert haben.
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
L = r"[^\W\d_]"  # Buchstabe

PERSONS = {
    "pt-BR": ["eu", "você/ele/ela", "nós", "vocês/eles/elas"],
    "es-AR": ["yo", "vos", "él/ella/usted", "nosotros", "ellos/ustedes"],
}
TENSES = {"pres": "Präsens", "past": "Vergangenheit"}

RULES = {
    "pt-BR": {
        "verbform": "Setze die passende Form des Verbs in Klammern ein. Die Übersetzung verrät Person und Zeit.",
        "serestar": "ser: was etwas oder jemand ist – Identität, Herkunft, Beruf, Eigenschaften, Uhrzeit "
                    "(sou alemão, é caro, são duas horas). estar: wie oder wo etwas gerade ist – Zustand, "
                    "Befinden, Ort (estou cansado, está no hotel, estou com fome).",
        "article": "Männlich meist auf -o, weiblich meist auf -a. Ausnahmen merken: o dia, o mapa, o problema, "
                   "a mão, a viagem, a cidade. Unbestimmter Artikel: um / uma.",
        "contraction": "em, de, a und por verschmelzen mit dem Artikel: em + o = no, em + a = na, de + o = do, "
                       "de + a = da, a + o = ao, a + a = à, por + o = pelo. Getrennt („em o“) ist falsch.",
        "plural": "Vokal: + s · -ão: meist -ões, manchmal -ães (pães) oder -ãos (mãos) · -l: -is "
                  "(real → reais, hotel → hotéis) · -m: -ns (trem → trens) · -r, -z und betontes -s: + es · "
                  "unbetontes -s bleibt (o ônibus → os ônibus).",
    },
    "es-AR": {
        "verbform": "Setze die passende Form des Verbs in Klammern ein (mit vos). Die Übersetzung verrät Person und Zeit.",
        "serestar": "ser: was etwas oder jemand ist – Identität, Herkunft, Beruf, Eigenschaften, Uhrzeit "
                    "(soy alemán, es caro, son las dos). estar: wie oder wo etwas gerade ist – Zustand, "
                    "Befinden, Ort (estoy cansado, está en el hotel).",
        "article": "Männlich meist auf -o, weiblich meist auf -a. Ausnahmen merken: el día, el mapa, el problema, "
                   "la mano, la ciudad, la habitación. Vor betontem a- steht el, obwohl weiblich (el agua). "
                   "Unbestimmter Artikel: un / una.",
        "contraction": "a + el = al, de + el = del – immer zusammengezogen. Mit la, los, las nicht (a la, de los).",
        "plural": "Vokal: + s · Konsonant: + es · -z: -ces · Akzent auf -ión, -án, -én fällt weg "
                  "(habitación → habitaciones) · unbetontes -s bleibt (el lunes → los lunes).",
    },
}


def load(lang):
    lessons = []
    for f in sorted(glob.glob(os.path.join(ASSETS, "lessons", lang, "*.json"))):
        with open(f, encoding="utf-8") as fh:
            lessons.append(json.load(fh))
    with open(os.path.join(ASSETS, "verbs", f"{lang}.json"), encoding="utf-8") as fh:
        verbs = {v["word"]: v for v in json.load(fh)["verbs"]}
    words = {w["id"]: (w, l["id"]) for l in lessons for w in l["words"]}
    return lessons, verbs, words


def find(text, form, start=0):
    m = re.compile(rf"(?<!{L}){re.escape(form)}(?!{L})", re.IGNORECASE).search(text, start)
    return m


def cap(s, like):
    return s[:1].upper() + s[1:] if like[:1].isupper() else s


def gap(text, m):
    return text[: m.start()], text[m.start():m.end()], text[m.end():]


# ------------------------------------------------------------------ 1) Verb im Satz

# Formen, die auch etwas anderes heissen koennen
VERB_SKIP = {"como", "nada", "limpo", "troco", "pago", "olha", "fecha", "volta", "ajuda", "espera",
             "cambio", "llama", "camino", "trabajo", "baile", "vino", "mira", "baja", "sube", "sale"}


def verbform(lang, lessons, verbs, words):
    out = []
    for l in lessons:
        for s in l["sentences"]:
            text = s["text"]
            hits = []
            for wid in s.get("words", []):
                v = verbs.get(wid)
                if not v:
                    continue
                for tense, forms in v["forms"].items():
                    for slot, form in enumerate(forms):
                        if form.lower() in VERB_SKIP:
                            continue
                        m = find(text, form)
                        # 1. Person nur, wenn die Uebersetzung sie bestaetigt (sonst z. B. Imperativ "Seguí derecho")
                        if m and slot == 0 and not re.search(r"\b(ich|mir|mich)\b", s["de"], re.IGNORECASE):
                            continue
                        if m:
                            # bei gleicher Form (llegamos, falamos) das Praesens bevorzugen
                            hits.append((m.start(), -len(form), 0 if tense == "pres" else 1, wid, tense, slot, m))
            if not hits:
                continue
            hits.sort()
            _, _, _, wid, tense, slot, m = hits[0]
            before, answer, after = gap(text, m)
            v = verbs[wid]
            inf = words[wid][0]["text"]
            others = [f for f in v["forms"][tense] if f.lower() != answer.lower()]
            other_tense = [f for t, fs in v["forms"].items() if t != tense for f in fs if f.lower() != answer.lower()]
            options = []
            for f in others + other_tense:
                if f.lower() not in [o.lower() for o in options]:
                    options.append(cap(f, answer))
            out.append({
                "id": f"vf:{l['id']}:{s['id']}", "type": "verbform", "lesson": l["id"],
                "before": before, "after": after, "answer": answer, "options": options[:3] + [answer],
                "hint": f"({inf})", "de": s["de"], "speak": text,
                "note": f"{inf}: {TENSES[tense]}, " + " / ".join(
                    PERSONS[lang][i] for i, f in enumerate(v["forms"][tense]) if f.lower() == answer.lower()),
                "words": s.get("words", []), "typeable": True,
            })
    return out


# ------------------------------------------------------------------ 2) ser oder estar

def serestar(lang, lessons, verbs, words):
    ser, estar = verbs["ser"]["forms"], verbs["estar"]["forms"]
    pairs = []  # (form, ser-form, estar-form)
    for tense in ("pres", "past"):
        for slot in range(len(PERSONS[lang])):
            s_form, e_form = ser[tense][slot], estar[tense][slot]
            if tense == "past":
                # Vergangenheit von ser = ir (fui, foi ...): im Satz nicht eindeutig
                pairs.append((e_form, s_form, e_form))
            else:
                pairs.append((s_form, s_form, e_form))
                pairs.append((e_form, s_form, e_form))
    out = []
    for l in lessons:
        for s in l["sentences"]:
            text = s["text"]
            best = None
            for form, sf, ef in pairs:
                m = find(text, form)
                if not m:
                    continue
                # "São Paulo", "São João" ...
                if form == "são" and re.match(r"São [A-ZÁÉÍÓÚ]", text[m.start():]):
                    continue
                if best is None or m.start() < best[0].start():
                    best = (m, sf, ef)
            if not best:
                continue
            m, sf, ef = best
            before, answer, after = gap(text, m)
            out.append({
                "id": f"se:{l['id']}:{s['id']}", "type": "serestar", "lesson": l["id"],
                "before": before, "after": after, "answer": answer,
                "options": [cap(sf, answer), cap(ef, answer)], "fixedOrder": True,
                "hint": "", "de": s["de"], "speak": text,
                "note": f"{'ser' if answer.lower() == sf.lower() else 'estar'}",
                "words": s.get("words", []),
            })
    return out


# ------------------------------------------------------------------ 3) Artikel und Geschlecht

ART = {
    "pt-BR": {"o": ("m", "um", "a"), "a": ("f", "uma", "o"), "os": ("m", "uns", "as"), "as": ("f", "umas", "os")},
    "es-AR": {"el": (None, None, "la"), "la": ("f", "una", "el"), "los": ("m", "unos", "las"), "las": ("f", "unas", "los")},
}


def article(lang, lessons, verbs, words):
    out = []
    for l in lessons:
        for w in l["words"]:
            if w.get("pos") != "noun":
                continue
            parts = w["text"].split(" ", 1)
            if len(parts) < 2 or parts[0] not in ART[lang]:
                continue
            art, rest = parts
            gender = w.get("gender") or ART[lang][art][0] or "m"
            other = ART[lang][art][2]
            if lang == "es-AR" and art == "el":
                indef = "un"
            else:
                indef = ART[lang][art][1]
            g = "weiblich" if gender == "f" else "männlich"
            note = f"{g} · {indef} {rest}"
            if w.get("note") and lang == "es-AR" and art == "el" and gender == "f":
                note += " · " + w["note"]
            elif lang == "es-AR" and art == "el" and gender == "f":
                note += " · weiblich, aber el wegen des betonten a-"
            out.append({
                "id": f"art:{w['id']}", "type": "article", "lesson": l["id"],
                "before": "", "after": " " + rest, "answer": art,
                "options": [art, other] if art in ("o", "el", "os", "los") else [other, art], "fixedOrder": True,
                "hint": "", "de": w["de"], "speak": w["text"], "note": note,
                "words": [w["id"]],
            })
    return out


# ------------------------------------------------------------------ 4) Praeposition + Artikel

PT_CONTRACTIONS = {
    ("em", "o"): "no", ("em", "a"): "na", ("em", "as"): "nas",  # "nos" ist auch Pronomen -> nicht abgefragt
    ("de", "o"): "do", ("de", "a"): "da", ("de", "os"): "dos", ("de", "as"): "das",
    ("a", "o"): "ao", ("a", "a"): "à", ("a", "os"): "aos", ("a", "as"): "às",
    ("por", "o"): "pelo", ("por", "a"): "pela", ("por", "os"): "pelos", ("por", "as"): "pelas",
    ("em", "um"): "num", ("em", "uma"): "numa",
    ("de", "ele"): "dele", ("de", "ela"): "dela", ("de", "eles"): "deles", ("de", "elas"): "delas",
    ("em", "este"): "neste", ("em", "esta"): "nesta", ("em", "esse"): "nesse", ("em", "essa"): "nessa",
    ("em", "isso"): "nisso", ("de", "este"): "deste", ("de", "esta"): "desta", ("de", "esse"): "desse",
    ("de", "essa"): "dessa", ("de", "isso"): "disso", ("em", "aquele"): "naquele", ("em", "aquela"): "naquela",
    ("de", "aquele"): "daquele", ("de", "aquela"): "daquela",
}
PT_SWAP = {"o": "a", "a": "o", "os": "as", "as": "os", "um": "uma", "uma": "um", "ele": "ela", "ela": "ele",
           "eles": "elas", "elas": "eles", "este": "esta", "esta": "este", "esse": "essa", "essa": "esse",
           "isso": "esse", "aquele": "aquela", "aquela": "aquele"}
ES_CONTRACTIONS = {("a", "el"): "al", ("de", "el"): "del"}


def contraction(lang, lessons, verbs, words):
    table = PT_CONTRACTIONS if lang == "pt-BR" else ES_CONTRACTIONS
    by_form = {v: k for k, v in table.items()}
    out = []
    for l in lessons:
        for s in l["sentences"]:
            text = s["text"]
            best = None
            for form, (p, a) in by_form.items():
                m = find(text, form)
                if m and (best is None or m.start() < best[0].start()):
                    best = (m, p, a)
            if not best:
                continue
            m, p, a = best
            before, answer, after = gap(text, m)
            options = [f"{p} {a}"]
            if lang == "pt-BR":
                swapped = table.get((p, PT_SWAP.get(a, a)))
                if swapped:
                    options.append(swapped)
                for q in ("em", "de", "a", "por"):
                    if q != p and (q, a) in table:
                        options.append(table[(q, a)])
                        break
            else:
                options.append("del" if answer.lower() == "al" else "al")
            options = [cap(o, answer) for o in options if o.lower() != answer.lower()]
            out.append({
                "id": f"con:{l['id']}:{s['id']}", "type": "contraction", "lesson": l["id"],
                "before": before, "after": after, "answer": answer, "options": options[:3] + [answer],
                "hint": f"({p} + {a})", "de": s["de"], "speak": text, "note": f"{p} + {a} = {answer.lower()}",
                "words": s.get("words", []),
            })
    return out


# ------------------------------------------------------------------ 5) Plural

PT_PLURAL_SKIP = {"pôr", "calor", "sal", "açúcar", "arroz", "Brasil", "futebol", "carnaval", "macarrão",
                  "check-in", "check-out", "shopping", "show", "bagagem", "hospedagem"}
PT_AO = {"pão": "pães", "mão": "mãos", "irmão": "irmãos", "cão": "cães", "alemão": "alemães",
         "cidadão": "cidadãos", "órgão": "órgãos"}
ES_PLURAL_SKIP = {"calor", "sal", "azúcar", "arroz", "fútbol", "sed", "humedad", "calefacción", "check-in",
                  "check-out", "shopping", "hostel", "tos", "SUBE"}
ES_ACCENT_ADD = {"examen": "exámenes", "imagen": "imágenes", "joven": "jóvenes", "origen": "orígenes"}
UNACCENT = str.maketrans("áéíóúâêô", "aeiouaeo")


def pt_plural(n):
    """Plural eines portugiesischen Nomens oder None, wenn einfach + s."""
    if n in PT_AO:
        return PT_AO[n]
    if n.endswith("ão"):
        return n[:-2] + "ões"
    if n.endswith("al") or n.endswith("ul"):
        return n[:-1] + "is"
    if n.endswith("el"):
        return n[:-2] + ("éis" if not re.search("[áéíóúâêô]", n) else "eis")
    if n.endswith("ol"):
        return n[:-2] + "óis"
    if n.endswith("il"):
        return n[:-1] + "s" if not re.search("[áéíóúâêô]", n) else n[:-2] + "eis"
    if n.endswith("m"):
        return n[:-1] + "ns"
    if n.endswith(("r", "z")):
        return n + "es"
    if n.endswith("s"):
        # betont: mês -> meses, país -> países, gás -> gases; unbetont: ônibus -> ônibus
        if re.search("[éê]s$", n):
            return n[:-2] + "eses"
        if n.endswith("ás"):
            return n[:-2] + "ases"
        if n.endswith("ís"):
            return n + "es"
        if len(re.findall("[aeiouáéíóúâêôãõ]+", n)) == 1:
            return n + "es"
        return n
    return None


def es_plural(n):
    if n in ES_ACCENT_ADD:
        return ES_ACCENT_ADD[n]
    if n.endswith("z"):
        return n[:-1] + "ces"
    if n.endswith("s"):
        if re.search("[aeiou]ís$", n):
            return n + "es"                      # país -> países (Akzent bleibt)
        if re.search("[áéíóú]s$", n):
            return n[:-2] + n[-2].translate(UNACCENT) + "ses"   # inglés -> ingleses, remís -> remises
        if len(re.findall("[aeiouáéíóú]+", n)) == 1:
            return n + "es"                      # mes -> meses
        return n                                 # lunes -> lunes
    if re.search("[^aeiouáéíóú]$", n):
        if re.search("[áéíóú][nlrd]$", n) and len(re.findall("[aeiouáéíóú]+", n)) > 1:
            return n[:-2] + n[-2].translate(UNACCENT) + n[-1] + "es"   # habitación -> habitaciones
        return n + "es"                          # hotel -> hoteles, dólar -> dólares
    return None


def plural_distractors(lang, sing, plural):
    c = []
    if sing.endswith("s"):
        sing_s = sing + "es"
    else:
        sing_s = sing + "s"
    if lang == "pt-BR":
        if sing.endswith("ão"):
            c += [sing[:-2] + x for x in ("ões", "ães", "ãos")]
        c += [sing_s, sing + "es", sing]
        if sing.endswith("l"):
            c += [sing[:-1] + "is", sing[:-2] + "eis"]
        if sing.endswith("m"):
            c += [sing[:-1] + "ns"]
    else:
        c += [sing_s, sing + "es", sing]
        if re.search("[áéíóú]", sing):
            c += [sing + "es"]
        if sing.endswith("z"):
            c += [sing + "es"]
        if plural.translate(UNACCENT) != plural:
            c += [plural.translate(UNACCENT)]
    out = []
    for x in c:
        if x != plural and x not in out:
            out.append(x)
    return out[:3]


PLURAL_ART = {"pt-BR": {"m": "os", "f": "as"}, "es-AR": {"m": "los", "f": "las"}}


def plural(lang, lessons, verbs, words):
    skip = PT_PLURAL_SKIP if lang == "pt-BR" else ES_PLURAL_SKIP
    rule = pt_plural if lang == "pt-BR" else es_plural
    out = []
    for l in lessons:
        for w in l["words"]:
            if w.get("pos") != "noun":
                continue
            parts = w["text"].split(" ")
            if parts[0] not in ART[lang] or parts[0] in ("os", "as", "los", "las"):
                continue
            head, rest = parts[1], parts[2:]
            # Mehrwortige nur als "X de Y"
            if rest and rest[0] not in ("de", "do", "da"):
                continue
            if head in skip:
                continue
            p = rule(head)
            if not p:
                continue
            gender = w.get("gender") or ("f" if parts[0] == "a" or parts[0] == "la" else "m")
            part = PLURAL_ART[lang][gender]
            tail = (" " + " ".join(rest)) if rest else ""
            out.append({
                "id": f"pl:{w['id']}", "type": "plural", "lesson": l["id"],
                "before": f"{w['text']} → {part} ", "after": tail, "answer": p,
                "options": plural_distractors(lang, head, p) + [p], "hint": "", "de": w["de"],
                "speak": f"{part} {p}{tail}", "note": "", "words": [w["id"]], "typeable": True,
            })
    return out


# ------------------------------------------------------------------ Ausgabe

def build(lang):
    lessons, verbs, words = load(lang)
    exercises = []
    for gen in (verbform, serestar, article, contraction, plural):
        exercises += gen(lang, lessons, verbs, words)
    keys = set()
    for e in exercises:
        assert e["id"] not in keys, e["id"]
        keys.add(e["id"])
        low = [o.lower() for o in e["options"]]
        assert e["answer"] in e["options"] and len(set(low)) == len(low) and len(low) >= 2, e
    out = {"language": lang, "rules": RULES[lang], "exercises": exercises}
    path = os.path.join(ASSETS, "exercises", f"{lang}.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(out, fh, ensure_ascii=False, indent=1)
        fh.write("\n")
    counts = {}
    for e in exercises:
        counts[e["type"]] = counts.get(e["type"], 0) + 1
    print(lang, counts, "->", os.path.relpath(path, ROOT))
    return exercises


if __name__ == "__main__":
    for lang in ("pt-BR", "es-AR"):
        build(lang)
