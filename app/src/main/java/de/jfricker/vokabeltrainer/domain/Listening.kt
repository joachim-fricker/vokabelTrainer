package de.jfricker.vokabeltrainer.domain

import kotlin.random.Random

/**
 * "Nur hören": Woerter und Saetze mit Uebersetzung in einer Endlosschlaufe vorlesen.
 * Nicht gewusste kommen oefter dran (Gewicht aus dem Lernstand), ohne Karteikasten.
 */
data class ListenItem(
    /** wordKey bzw. sentenceKey */
    val key: String,
    /** Text in der Lernsprache */
    val text: String,
    val de: String,
    val sentence: Boolean,
    /** Wie oft im Verhaeltnis zu den anderen (1 = normal) */
    val weight: Double,
    /** Nur bei Dialogen: alle Zeilen, nacheinander vorgelesen (text/de sind dann die Titel) */
    val lines: List<ListenLine> = emptyList(),
) {
    val dialogue: Boolean get() = lines.isNotEmpty()
}

/** Zeile eines Dialogs fuer "Nur hören"; pitch unterscheidet die beiden Rollen. */
data class ListenLine(val speaker: String, val text: String, val de: String, val pitch: Float)

/** Reihenfolge beim Vorlesen */
enum class ListenOrder(val code: String, val label: String) {
    TARGET_FIRST("target", "Lernsprache → Deutsch"),
    DE_FIRST("de", "Deutsch → Lernsprache");

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: TARGET_FIRST
    }
}

/** Zusammengefasster Lernstand eines Eintrags ueber alle Uebungsarten/Richtungen. */
data class ListenStat(val wrong: Int, val minBox: Int, val maxBox: Int)

object ListenWeights {
    /** Noch nie geuebt */
    const val NEW = 1.5
    /** Sitzt (nie falsch, bekannt) */
    const val KNOWN = 0.4
    /** In dieser Runde "Nicht gewusst" gedrueckt: so viel oefter */
    const val SESSION_BOOST = 3.0

    /**
     * Dialog: noch nie "Rolle spielen" gemacht -> NEW; zuletzt weniger als 80 % richtig -> 2 .. 5 je nach Ergebnis;
     * sonst 1. correct/total vom letzten Rollenspiel.
     */
    fun dialogueWeight(correct: Int?, total: Int?): Double {
        if (correct == null || total == null || total == 0) return NEW
        val rate = correct.toDouble() / total
        return if (rate < 0.8) 2.0 + 3.0 * (0.8 - rate) / 0.8 else 1.0
    }

    /**
     * Gewicht aus dem Lernstand:
     * - schon falsch gewesen: 3 + 2 je Fehler (max. 5 Fehler) -> 5 .. 13; noch nicht sicher (Fach <= 2) zusaetzlich x1.5
     * - nie falsch und bekannt / Fach >= 4: KNOWN
     * - noch nie geuebt: NEW
     * - sonst 1
     */
    fun weight(stat: ListenStat?): Double = when {
        stat == null -> NEW
        stat.wrong > 0 -> (3.0 + 2.0 * minOf(stat.wrong, 5)) * (if (stat.minBox <= 2) 1.5 else 1.0)
        stat.minBox >= 4 -> KNOWN
        else -> 1.0
    }
}

/**
 * Zieht gewichtet zufaellig den naechsten Eintrag. Die zuletzt gehoerten kommen nicht gleich wieder
 * (bei wenigen Eintraegen entsprechend weniger gesperrt).
 */
class ListenPicker(private val items: List<ListenItem>, private val random: Random = Random.Default) {

    private val recent = ArrayDeque<String>()
    private val boost = HashMap<String, Double>()
    private val blockCount = minOf(8, items.size / 2)

    val size: Int get() = items.size

    /** Eintrag in dieser Runde oefter abspielen ("Nicht gewusst"); mehrfach druecken verstaerkt bis x9. */
    fun markUnknown(key: String) {
        boost[key] = minOf((boost[key] ?: 1.0) * ListenWeights.SESSION_BOOST, 9.0)
    }

    fun isBoosted(key: String) = (boost[key] ?: 1.0) > 1.0

    fun weightOf(item: ListenItem) = item.weight * (boost[item.key] ?: 1.0)

    fun next(): ListenItem? {
        if (items.isEmpty()) return null
        val candidates = items.filter { it.key !in recent }.ifEmpty { items }
        val total = candidates.sumOf { weightOf(it) }
        var r = random.nextDouble() * total
        var chosen = candidates.last()
        for (c in candidates) {
            r -= weightOf(c)
            if (r <= 0) { chosen = c; break }
        }
        recent.addLast(chosen.key)
        while (recent.size > blockCount) recent.removeFirst()
        return chosen
    }
}
