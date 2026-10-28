import json, glob, re, os
ASSETS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'app', 'src', 'main', 'assets', 'lessons')

# Artikel je Sprache: (maennlich, weiblich); wie Language.articles in der App
ARTICLES = {
    'pt-BR': (['o ', 'os '], ['a ', 'as ']),
    'es-AR': (['el ', 'los '], ['la ', 'las ']),
}

def lesson_dir(lang):
    return os.path.join(ASSETS, lang)

DIR = lesson_dir('pt-BR')
POS = dict(n='noun', v='verb', adj='adjective', adv='adverb', ph='phrase', num='numeral',
           prep='preposition', pron='pronoun', int='interjection', conj='conjunction')

def word(line, category, lang='pt-BR'):
    parts = [p.strip() for p in line.split('|')]
    id_, text, de, pos = parts[:4]
    note = parts[4] if len(parts) > 4 and parts[4] else None
    # "n:f" = Geschlecht ausdruecklich (z. B. el agua ist weiblich)
    pos, _, gender = pos.partition(':')
    w = dict(id=id_, text=text, de=de, pos=POS.get(pos, pos))
    if gender:
        w['gender'] = gender
    elif w['pos'] == 'noun':
        m, f = ARTICLES[lang]
        if any(text.startswith(a) for a in m): w['gender'] = 'm'
        elif any(text.startswith(a) for a in f): w['gender'] = 'f'
    if note: w['note'] = note
    w['category'] = category
    return w

def make(fname, id_, order, title, title_target, groups, sentences, grammar, word_grammar=None, lang='pt-BR'):
    words = []
    for cat, block in groups:
        for line in block.strip().splitlines():
            if line.strip():
                words.append(word(line, cat, lang))
    for w in words:
        if word_grammar and w['id'] in word_grammar:
            w['grammar'] = word_grammar[w['id']]
    sents = []
    for i, s in enumerate(sentences, 1):
        text, de, ws = s[0], s[1], s[2].split()
        d = dict(id=f's{i:02d}', text=text, de=de, words=ws)
        if len(s) > 3 and s[3]: d['grammar'] = s[3].split()
        sents.append(d)
    lesson = dict(id=id_, language=lang, order=order, title=title, titleTarget=title_target,
                  words=words, sentences=sents, grammar=grammar)
    os.makedirs(lesson_dir(lang), exist_ok=True)
    with open(os.path.join(lesson_dir(lang), fname), 'w', encoding='utf-8') as f:
        json.dump(lesson, f, ensure_ascii=False, indent=2)
    print(f'{fname}: {len(words)} Wörter, {len(sents)} Sätze, {len(grammar)} Regeln, {len(groups)} Gruppen')
    check(lang)

def forms(w, lang='pt-BR'):
    m, f = ARTICLES[lang]
    arts = sorted(m + f, key=len, reverse=True)
    out = []
    for p in w['text'].split(','):
        p = p.strip().strip('?!¿¡').strip()
        for a in arts:
            if p.startswith(a): p = p[len(a):]; break
        if p: out.append(p)
    return out

def check(lang='pt-BR'):
    W = {}; G = {}; L = []
    for f in sorted(glob.glob(os.path.join(lesson_dir(lang), '*.json'))):
        d = json.load(open(f, encoding='utf-8')); L.append((os.path.basename(f), d))
        for w in d['words']:
            assert w['id'] not in W, f"doppelte Wort-ID {w['id']} in {f} und {W[w['id']]}"
            W[w['id']] = os.path.basename(f)
        for g in d['grammar']:
            assert g['id'] not in G, f"doppelte Grammatik-ID {g['id']}"
            G[g['id']] = f
    Wd = {}
    for _, d in L:
        for w in d['words']: Wd[w['id']] = w
    used = set(); problems = []
    for f, d in L:
        ids = [s['id'] for s in d['sentences']]
        assert len(ids) == len(set(ids)), f
        for s in d['sentences']:
            for i in s['words']:
                if i not in Wd: problems.append(f'{f}: unbekanntes Wort {i} in "{s["text"]}"')
                used.add(i)
            for i in s.get('grammar', []):
                if i not in G: problems.append(f'{f}: unbekannte Regel {i}')
            ok = any(re.search(r'(?<![^\W\d_])' + re.escape(fm) + r'(?![^\W\d_])', s['text'], re.I)
                     for i in s['words'] if i in Wd for fm in forms(Wd[i], lang))
            if not ok: problems.append(f'{f}: kein Lückentext möglich: "{s["text"]}"')
        for w in d['words']:
            for i in w.get('grammar', []):
                if i not in G: problems.append(f'{f}: unbekannte Regel {i} bei Wort {w["id"]}')
    unused = [i for i in W if i not in used]
    if unused: problems.append(f'Wörter ohne Satz: {unused}')
    print('PROBLEME:' if problems else 'alles ok', *problems, sep='\n  ')
