package de.jfricker.vokabeltrainer.domain

import java.text.Normalizer

/** Ein Wort oder Satz einer Sprache fuer die Suche. */
data class SearchEntry(
    /** wordKey bzw. sentenceKey */
    val key: String,
    val language: Language,
    val text: String,
    val de: String,
    val sentence: Boolean,
    val lessonTitle: String?,
    /** Gleiche Bedeutung in allen Sprachen = gleiche Gruppe */
    val group: String,
)

/** Treffer: eine Bedeutung mit den Eintraegen aller Sprachen. */
data class SearchHit(val group: String, val de: String, val sentence: Boolean, val entries: List<SearchEntry>) {
    fun entriesFor(language: Language): List<SearchEntry> = entries.filter { it.language == language }
}

/**
 * Suche ueber alle Sprachen: Eingabe auf Deutsch, Portugiesisch oder Spanisch, Gross-/Kleinschreibung
 * und Akzente egal. Gefunden wird die Bedeutung, angezeigt wird sie in allen Sprachen.
 * Woerter werden ueber den Sprachvergleich (Wortpaare) bzw. die gleiche deutsche Uebersetzung
 * zusammengefasst, Saetze ueber die gleiche deutsche Uebersetzung.
 */
class SearchEngine(entries: List<SearchEntry>) {

    private class Indexed(val entry: SearchEntry, val text: String, val textCore: String, val de: String, val deVariants: List<String>)

    private val indexed = entries.map { e ->
        Indexed(
            e,
            text = normalize(e.text),
            textCore = normalize(stripArticle(e.text)),
            de = normalize(e.de),
            deVariants = deVariants(e.de),
        )
    }
    private val groups: Map<String, List<SearchEntry>> = entries.groupBy { it.group }

    val size: Int get() = indexed.size

    fun search(query: String, limit: Int = 60): List<SearchHit> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()
        val best = HashMap<String, Int>()
        for (i in indexed) {
            val score = score(i, q) ?: continue
            val g = i.entry.group
            if (score < (best[g] ?: Int.MAX_VALUE)) best[g] = score
        }
        return best.entries
            .map { (g, score) -> score to hit(g) }
            // Bester Treffer zuerst, Woerter vor Saetzen, kurze vor langen
            .sortedWith(compareBy({ it.first }, { it.second.sentence }, { it.second.de.length }))
            .take(limit)
            .map { it.second }
    }

    private fun hit(group: String): SearchHit {
        val list = groups.getValue(group).sortedBy { it.language.ordinal }
        return SearchHit(group, list.first().de, list.first().sentence, list)
    }

    /** 0 = genau, 1 = Wortanfang, 2 = irgendwo; null = kein Treffer */
    private fun score(i: Indexed, q: String): Int? = when {
        i.text == q || i.textCore == q || q in i.deVariants -> 0
        startsWord(i.text, q) || startsWord(i.de, q) -> 1
        q in i.text || q in i.de -> 2
        else -> null
    }

    private fun startsWord(s: String, q: String) = s.startsWith(q) || s.contains(" $q")

    companion object {
        private val ARTICLES = listOf("o ", "a ", "os ", "as ", "el ", "la ", "los ", "las ", "der ", "die ", "das ")

        /** Klein, ohne Akzente und Satzzeichen, Leerraum zusammengefasst */
        fun normalize(s: String): String =
            Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
                .replace(Regex("[¿¡?!.,;:\"“”„()]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        fun stripArticle(s: String): String {
            val l = s.lowercase()
            return ARTICLES.firstOrNull { l.startsWith(it) }?.let { s.substring(it.length) } ?: s
        }

        /** "die Ehefrau; die Frau (umgangssprachlich)" -> ["ehefrau", "frau"] usw. */
        fun deVariants(de: String): List<String> =
            de.replace(Regex("\\([^)]*\\)"), "")
                .split(',', ';', '/')
                .map { normalize(stripArticle(it.trim())) }
                .filter { it.isNotEmpty() } + normalize(de)

        /** Gruppe fuer ein Wort ohne Wortpaar: gleiche deutsche Uebersetzung */
        fun wordGroup(de: String) = "w:" + normalize(de.replace(Regex("\\([^)]*\\)"), ""))

        fun sentenceGroup(de: String) = "s:" + normalize(de)
    }
}
