# Lektions-Generator

Die Lektionen 4–10 werden aus diesen Python-Skripten erzeugt (Lektionen 1–3 wurden direkt als JSON gepflegt).

    cd tools/lessons
    python3 l05.py        # schreibt app/src/main/assets/lessons/pt-BR/05_unterwegs.json und prüft alles

Format einer Wortzeile: `id|text|deutsch|wortart|hinweis` (Hinweis optional).
Wortarten: n, v, adj, adv, ph, num, prep, pron, int, conj. Bei Nomen wird das Geschlecht aus dem Artikel (o/a bzw. el/la) abgeleitet;
mit `n:f` / `n:m` lässt es sich ausdrücklich angeben (z. B. „el agua“ ist weiblich).

Satz: `("portugiesisch", "deutsch", "wort_id wort_id", "grammatik_id")`.

Die Prüfung (`check()`) meldet doppelte IDs, unbekannte Verweise, Sätze ohne möglichen Lückentext
(mindestens ein verknüpftes Wort muss unverändert im Satz stehen) und Wörter, die in keinem Satz vorkommen.

## Spanisch (Argentinien)

Die spanischen Lektionen liegen in `es-AR/` (`python3 e03.py` usw.) und schreiben nach
`app/src/main/assets/lessons/es-AR/`. Gleiche Themen wie Portugiesisch, argentinisches Spanisch mit vos.
