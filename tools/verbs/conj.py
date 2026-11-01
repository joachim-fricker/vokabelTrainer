"""
Konjugationstabellen fuer den Konjugationstrainer.

    cd tools/verbs
    python3 conj.py          # schreibt app/src/main/assets/verbs/pt-BR.json und es-AR.json

Es werden alle Verben aus den Lektionen konjugiert (Praesens und Vergangenheit). Regelmaessige
Verben werden nach Regeln erzeugt, unregelmaessige stehen ausdruecklich in IRREGULAR.
Verben, die man praktisch nur in der 3. Person braucht (chover, doler ...), stehen in EXCLUDE.
Unregelmaessige Verben (eigene Formen oder Stammwechsel) bekommen "irregular": true und werden
in der App Form fuer Form geuebt; regelmaessige nur ueber ihre Endungen mit zufaelligen Verben.
Die Pruefung meldet Verben aus den Lektionen, die weder konjugiert noch ausgeschlossen sind.

Personen (Reihenfolge der Formen):
  pt-BR: eu | você/ele/ela/a gente | nós | vocês/eles/elas
  es-AR: yo | vos | él/ella/usted | nosotros | ellos/ellas/ustedes
"""
import glob
import json
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
TENSES = ["pres", "past"]

# ------------------------------------------------------------------ Portugiesisch (Brasilien)

PT_EXCLUDE = {
    "chover": "nur 3. Person (chove)",
    "doer": "meist 3. Person (dói, doem)",
    "custar": "meist 3. Person (custa, custam)",
    "funcionar": "meist 3. Person (funciona)",
}

PT_IRREGULAR = {
    "ser": (["sou", "é", "somos", "são"], ["fui", "foi", "fomos", "foram"]),
    "estar": (["estou", "está", "estamos", "estão"], ["estive", "esteve", "estivemos", "estiveram"]),
    "ter": (["tenho", "tem", "temos", "têm"], ["tive", "teve", "tivemos", "tiveram"]),
    "ir": (["vou", "vai", "vamos", "vão"], ["fui", "foi", "fomos", "foram"]),
    "vir": (["venho", "vem", "vimos", "vêm"], ["vim", "veio", "viemos", "vieram"]),
    "fazer": (["faço", "faz", "fazemos", "fazem"], ["fiz", "fez", "fizemos", "fizeram"]),
    "querer": (["quero", "quer", "queremos", "querem"], ["quis", "quis", "quisemos", "quiseram"]),
    "poder": (["posso", "pode", "podemos", "podem"], ["pude", "pôde", "pudemos", "puderam"]),
    "dizer": (["digo", "diz", "dizemos", "dizem"], ["disse", "disse", "dissemos", "disseram"]),
    "saber": (["sei", "sabe", "sabemos", "sabem"], ["soube", "soube", "soubemos", "souberam"]),
    "ver": (["vejo", "vê", "vemos", "veem"], ["vi", "viu", "vimos", "viram"]),
    "dar": (["dou", "dá", "damos", "dão"], ["dei", "deu", "demos", "deram"]),
    "trazer": (["trago", "traz", "trazemos", "trazem"], ["trouxe", "trouxe", "trouxemos", "trouxeram"]),
    "sair": (["saio", "sai", "saímos", "saem"], ["saí", "saiu", "saímos", "saíram"]),
    "ler": (["leio", "lê", "lemos", "leem"], ["li", "leu", "lemos", "leram"]),
    # nur das Praesens ist unregelmaessig
    "pedir": (["peço", "pede", "pedimos", "pedem"], None),
    "perder": (["perco", "perde", "perdemos", "perdem"], None),
    "dormir": (["durmo", "dorme", "dormimos", "dormem"], None),
    "sentir": (["sinto", "sente", "sentimos", "sentem"], None),
    "subir": (["subo", "sobe", "subimos", "sobem"], None),
}

PT_NOTES = {
    "ser": "Vergangenheit gleich wie bei ir: fui, foi …",
    "ir": "Vergangenheit gleich wie bei ser. Zukunft: vou + Infinitiv (vou viajar)",
    "estar": "Vorübergehender Zustand / Ort (estou cansado, está no hotel)",
    "ter": "têm (Plural) mit Zirkumflex, gesprochen wie tem",
    "vir": "Achtung: vimos = wir kommen, viemos = wir kamen",
    "precisar": "immer mit de: preciso de um táxi",
    "gostar": "immer mit de: gosto de café",
    "pedir": "eu peço",
    "perder": "eu perco",
    "dormir": "eu durmo",
    "sentir": "eu sinto",
    "subir": "ele sobe, eles sobem",
    "conhecer": "eu conheço (ç vor o)",
    "descer": "eu desço (ç vor o)",
}


def pt_regular(inf):
    stem, end = inf[:-2], inf[-2:]
    if end == "ar":
        pres = [stem + "o", stem + "a", stem + "amos", stem + "am"]
        eu = stem + "ei"
        if inf.endswith("car"):
            eu = stem[:-1] + "quei"
        elif inf.endswith("gar"):
            eu = stem + "uei"
        elif inf.endswith("çar"):
            eu = stem[:-1] + "cei"
        past = [eu, stem + "ou", stem + "amos", stem + "aram"]
    elif end == "er":
        eu = stem[:-1] + "ço" if inf.endswith("cer") else stem + "o"
        pres = [eu, stem + "e", stem + "emos", stem + "em"]
        past = [stem + "i", stem + "eu", stem + "emos", stem + "eram"]
    elif end == "ir":
        pres = [stem + "o", stem + "e", stem + "imos", stem + "em"]
        past = [stem + "i", stem + "iu", stem + "imos", stem + "iram"]
    else:
        raise ValueError(inf)
    return pres, past


def pt_conjugate(inf):
    pres, past = pt_regular(inf)
    if inf in PT_IRREGULAR:
        p, q = PT_IRREGULAR[inf]
        pres = p or pres
        past = q or past
    return {"pres": pres, "past": past}


# ------------------------------------------------------------------ Spanisch (Argentinien, vos)

ES_EXCLUDE = {
    "llover": "nur 3. Person (llueve)",
    "doler": "wie gustar: me duele",
    "gustar": "me gusta / me gustan (eigene Grammatik)",
    "encantar": "wie gustar: me encanta",
    "costar": "meist 3. Person (cuesta, cuestan)",
    "funcionar": "meist 3. Person (funciona)",
    "hay": "unpersönlich (es gibt)",
}

ES_IRREGULAR = {
    "ser": (["soy", "sos", "es", "somos", "son"], ["fui", "fuiste", "fue", "fuimos", "fueron"]),
    "estar": (["estoy", "estás", "está", "estamos", "están"], ["estuve", "estuviste", "estuvo", "estuvimos", "estuvieron"]),
    "tener": (["tengo", "tenés", "tiene", "tenemos", "tienen"], ["tuve", "tuviste", "tuvo", "tuvimos", "tuvieron"]),
    "ir": (["voy", "vas", "va", "vamos", "van"], ["fui", "fuiste", "fue", "fuimos", "fueron"]),
    "venir": (["vengo", "venís", "viene", "venimos", "vienen"], ["vine", "viniste", "vino", "vinimos", "vinieron"]),
    "hacer": (["hago", "hacés", "hace", "hacemos", "hacen"], ["hice", "hiciste", "hizo", "hicimos", "hicieron"]),
    "querer": (["quiero", "querés", "quiere", "queremos", "quieren"], ["quise", "quisiste", "quiso", "quisimos", "quisieron"]),
    "poder": (["puedo", "podés", "puede", "podemos", "pueden"], ["pude", "pudiste", "pudo", "pudimos", "pudieron"]),
    "decir": (["digo", "decís", "dice", "decimos", "dicen"], ["dije", "dijiste", "dijo", "dijimos", "dijeron"]),
    "saber": (["sé", "sabés", "sabe", "sabemos", "saben"], ["supe", "supiste", "supo", "supimos", "supieron"]),
    "ver": (["veo", "ves", "ve", "vemos", "ven"], ["vi", "viste", "vio", "vimos", "vieron"]),
    "traer": (["traigo", "traés", "trae", "traemos", "traen"], ["traje", "trajiste", "trajo", "trajimos", "trajeron"]),
    "salir": (["salgo", "salís", "sale", "salimos", "salen"], None),
    "conocer": (["conozco", "conocés", "conoce", "conocemos", "conocen"], None),
    "pedir": (["pido", "pedís", "pide", "pedimos", "piden"], ["pedí", "pediste", "pidió", "pedimos", "pidieron"]),
    "seguir": (["sigo", "seguís", "sigue", "seguimos", "siguen"], ["seguí", "seguiste", "siguió", "seguimos", "siguieron"]),
    "dormir": (["duermo", "dormís", "duerme", "dormimos", "duermen"], ["dormí", "dormiste", "durmió", "dormimos", "durmieron"]),
    "sentir": (["siento", "sentís", "siente", "sentimos", "sienten"], ["sentí", "sentiste", "sintió", "sentimos", "sintieron"]),
    "leer": (None, ["leí", "leíste", "leyó", "leímos", "leyeron"]),
}

# Stammwechsel im Praesens (nur yo, él, ellos - bei vos und nosotros nicht!)
ES_STEM_CHANGE = {
    "entender": ("e", "ie"),
    "recomendar": ("e", "ie"),
    "perder": ("e", "ie"),
    "empezar": ("e", "ie"),
    "probar": ("o", "ue"),
    "volver": ("o", "ue"),
    "acostar": ("o", "ue"),
    "jugar": ("u", "ue"),
}

ES_REFLEXIVE = ["me", "te", "se", "nos", "se"]

ES_NOTES = {
    "ser": "Vergangenheit gleich wie bei ir: fui, fue …",
    "ir": "Vergangenheit gleich wie bei ser. Zukunft: voy a + Infinitiv (voy a viajar)",
    "querer": "Stammwechsel e → ie, aber nicht bei vos: querés",
    "poder": "Stammwechsel o → ue, aber nicht bei vos: podés",
    "tener": "vos tenés (nicht tienes)",
    "entender": "Stammwechsel e → ie, aber nicht bei vos und nosotros",
    "recomendar": "Stammwechsel e → ie, aber nicht bei vos und nosotros",
    "perder": "Stammwechsel e → ie, aber nicht bei vos und nosotros",
    "perderse": "Stammwechsel e → ie, aber nicht bei vos und nosotros",
    "empezar": "Stammwechsel e → ie; yo empecé (z → c vor e)",
    "probar": "Stammwechsel o → ue, aber nicht bei vos und nosotros",
    "volver": "Stammwechsel o → ue, aber nicht bei vos und nosotros",
    "acostarse": "Stammwechsel o → ue, aber nicht bei vos und nosotros",
    "jugar": "Stammwechsel u → ue; yo jugué",
    "pedir": "Stammwechsel e → i, auch in der Vergangenheit (pidió)",
    "seguir": "Stammwechsel e → i, auch in der Vergangenheit (siguió)",
    "dormir": "Stammwechsel o → ue, Vergangenheit durmió",
    "sentirse": "Stammwechsel e → ie, Vergangenheit se sintió",
    "buscar": "yo busqué (c → qu vor e)",
    "pagar": "yo pagué (g → gu vor e)",
    "llegar": "yo llegué (g → gu vor e)",
    "cruzar": "yo crucé (z → c vor e)",
    "conocer": "yo conozco",
    "salir": "yo salgo",
    "leer": "Vergangenheit leyó, leyeron",
}


def es_regular(inf):
    stem, end = inf[:-2], inf[-2:]
    if end == "ar":
        pres = [stem + "o", stem + "ás", stem + "a", stem + "amos", stem + "an"]
        yo = stem + "é"
        if inf.endswith("car"):
            yo = stem[:-1] + "qué"
        elif inf.endswith("gar"):
            yo = stem + "ué"
        elif inf.endswith("zar"):
            yo = stem[:-1] + "cé"
        past = [yo, stem + "aste", stem + "ó", stem + "amos", stem + "aron"]
    elif end in ("er", "ir"):
        v = "é" if end == "er" else "í"
        pres = [stem + "o", stem + v + "s", stem + "e", stem + end[0] + "mos", stem + "en"]
        past = [stem + "í", stem + "iste", stem + "ió", stem + "imos", stem + "ieron"]
    else:
        raise ValueError(inf)
    if inf in ES_STEM_CHANGE:
        old, new = ES_STEM_CHANGE[inf]
        i = stem.rfind(old)
        changed = stem[:i] + new + stem[i + len(old):]
        for k in (0, 2, 4):
            pres[k] = changed + pres[k][len(stem):]
    return pres, past


def es_conjugate(inf):
    reflexive = inf.endswith("se")
    base = inf[:-2] if reflexive else inf
    pres, past = es_regular(base)
    if base in ES_IRREGULAR:
        p, q = ES_IRREGULAR[base]
        pres = p or pres
        past = q or past
    if reflexive:
        pres = [f"{r} {f}" for r, f in zip(ES_REFLEXIVE, pres)]
        past = [f"{r} {f}" for r, f in zip(ES_REFLEXIVE, past)]
    return {"pres": list(pres), "past": list(past)}


# ------------------------------------------------------------------ Ausgabe

LANGS = {
    "pt-BR": (pt_conjugate, PT_EXCLUDE, PT_NOTES, 4),
    "es-AR": (es_conjugate, ES_EXCLUDE, ES_NOTES, 5),
}


def is_irregular(lang, inf):
    """Eigene Formen oder Stammwechsel. Reine Schreibregeln (paguei, busqué, conheço) zaehlen als regelmaessig."""
    if lang == "pt-BR":
        return inf in PT_IRREGULAR
    base = inf[:-2] if inf.endswith("se") else inf
    return base in ES_IRREGULAR or base in ES_STEM_CHANGE


def lesson_verbs(lang):
    """Alle Verben der Lektionen in Lektionsreihenfolge: (wort_id, infinitiv)."""
    result = []
    for f in sorted(glob.glob(os.path.join(ASSETS, "lessons", lang, "*.json"))):
        with open(f, encoding="utf-8") as fh:
            lesson = json.load(fh)
        for w in lesson["words"]:
            if w.get("pos") == "verb":
                # "precisar de" -> "precisar"
                result.append((w["id"], w["text"].split()[0]))
    return result


def build(lang):
    conjugate, exclude, notes, persons = LANGS[lang]
    verbs = []
    errors = []
    for word_id, inf in lesson_verbs(lang):
        if inf in exclude or word_id in exclude:
            continue
        forms = conjugate(inf)
        for t in TENSES:
            if len(forms[t]) != persons or any(not f for f in forms[t]):
                errors.append(f"{lang} {inf} {t}: {forms[t]}")
        entry = {"word": word_id, "forms": forms}
        if is_irregular(lang, inf):
            entry["irregular"] = True
        if inf in notes:
            entry["note"] = notes[inf]
        verbs.append(entry)
    out = {"language": lang, "verbs": verbs}
    path = os.path.join(ASSETS, "verbs", f"{lang}.json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(out, fh, ensure_ascii=False, indent=2)
        fh.write("\n")
    irregular = sum(1 for v in verbs if v.get("irregular"))
    print(f"{lang}: {len(verbs)} Verben ({irregular} unregelmaessig) -> {os.path.relpath(path, ROOT)}")
    return errors


if __name__ == "__main__":
    errs = []
    for lang in LANGS:
        errs += build(lang)
    if errs:
        print("FEHLER:\n" + "\n".join(errs))
        sys.exit(1)
