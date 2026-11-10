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

## Verben (Konjugationstrainer)

`tools/verbs/conj.py` erzeugt `app/src/main/assets/verbs/pt-BR.json` und `es-AR.json` aus allen Verben der Lektionen
(Präsens und Vergangenheit). Regelmässige Verben nach Regeln, unregelmässige in `PT_IRREGULAR` / `ES_IRREGULAR`,
Verben fast nur in der 3. Person (chover, doler, gustar …) in `PT_EXCLUDE` / `ES_EXCLUDE`.
Nach neuen Verben in einer Lektion einfach erneut ausführen:

    cd tools/verbs
    python3 conj.py

## Grammatik-Übungen

`tools/grammar/exercises.py` erzeugt `app/src/main/assets/exercises/pt-BR.json` und `es-AR.json` aus den Lektionen
und Verbtabellen: Verb im Satz, ser/estar, Artikel/Geschlecht, Präposition + Artikel, Plural. Nach Änderungen an
Lektionen oder Verben (erst `conj.py`) erneut ausführen:

    cd tools/grammar
    python3 exercises.py

## Dialoge

`tools/dialogues/gen.py` erzeugt `app/src/main/assets/dialogues/pt-BR.json` und `es-AR.json` aus `pt_br.py` / `es_ar.py`
(je 12 typische Situationen: Begrüssung, Café, Restaurant, Flughafen, Weg fragen, Hotel, Taxi, Markt, Apotheke, Bus,
Smalltalk, Problem im Hotelzimmer). Zeile: `("a"|"b", Text, Deutsch[, Hinweis])`, a = Gegenüber, b = Du.
Die Prüfung meldet doppelte IDs, unbekannte Lektionen und Rollen mit weniger als 3 verschiedenen Sätzen.

    cd tools/dialogues
    python3 gen.py

## Sprachvergleich (Portugiesisch ↔ Spanisch)

`tools/compare/gen.py` erzeugt `app/src/main/assets/compare/pairs.json`: Wortpaare über die gemeinsame deutsche
Übersetzung (Gross-/Kleinschreibung zählt: sie ≠ Sie), eingeteilt in *ganz verschieden*, *ähnlich* (mit typischen Mustern
wie -ção → -ción, nh → ñ, o → ue) und *gleich*. Von Hand korrigieren über `OVERRIDES`, `EXCLUDE`, `EXTRA_PAIRS` in `gen.py`.
Falsche Freunde stehen in `false_friends.py`. Nach Änderungen an den Lektionen erneut ausführen:

    cd tools/compare
    python3 gen.py            # Übersicht
    python3 gen.py --list     # alle Paare je Gruppe
