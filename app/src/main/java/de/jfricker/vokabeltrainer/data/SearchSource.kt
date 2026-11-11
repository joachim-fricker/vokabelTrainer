package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.PracticeDao
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.SearchEngine
import de.jfricker.vokabeltrainer.domain.SearchEntry

/** Baut den Suchindex ueber alle Woerter und Saetze aller Sprachen. */
class SearchSource(private val dao: PracticeDao, private val compare: CompareStore) {

    suspend fun load(): SearchEngine {
        val lessons = dao.allLessons().associate { it.id to "Lektion ${it.sortOrder} · ${it.title}" }
        // Wortpaare aus dem Sprachvergleich: wordKey -> Paar ("p:<pt-ID>|<es-ID>")
        val pairOf = HashMap<String, String>()
        compare.get().pairs.forEach { p ->
            val ids = p.key.removePrefix("p:").split('|')
            if (ids.size == 2) {
                pairOf["${Language.PT_BR.code}:${ids[0]}"] = p.key
                pairOf["${Language.ES_AR.code}:${ids[1]}"] = p.key
            }
        }
        val entries = ArrayList<SearchEntry>()
        dao.allWords().forEach { w ->
            entries += SearchEntry(
                key = w.wordKey, language = Language.fromCode(w.language), text = w.text, de = w.de,
                sentence = false, lessonTitle = lessons[w.lessonId],
                group = pairOf[w.wordKey] ?: SearchEngine.wordGroup(w.de),
            )
        }
        dao.allSentences().forEach { s ->
            entries += SearchEntry(
                key = s.sentenceKey, language = Language.fromCode(s.language), text = s.text, de = s.de,
                sentence = true, lessonTitle = lessons[s.lessonId], group = SearchEngine.sentenceGroup(s.de),
            )
        }
        return SearchEngine(entries)
    }
}
