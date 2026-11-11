package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.PracticeDao
import de.jfricker.vokabeltrainer.domain.Dialogues
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.ListenLine
import de.jfricker.vokabeltrainer.domain.ListenItem
import de.jfricker.vokabeltrainer.domain.ListenStat
import de.jfricker.vokabeltrainer.domain.ListenWeights

/** Woerter und Saetze einer Sprache fuer "Nur hören", gewichtet nach Lernstand. */
class ListenSource(private val dao: PracticeDao, private val dialogues: DialogueStore) {

    suspend fun load(language: Language, words: Boolean, sentences: Boolean, withDialogues: Boolean): List<ListenItem> {
        // Lernstand ueber alle Richtungen/Uebungsarten je Schluessel (Wort: pt-de, de-pt; Satz: cloze, build)
        val stats = dao.progressWithPrefix(language.keyPrefix)
            .groupBy { it.wordKey }
            .mapValues { (_, rows) ->
                ListenStat(
                    wrong = rows.sumOf { it.wrongCount },
                    minBox = rows.minOf { it.box },
                    maxBox = rows.maxOf { it.box },
                )
            }
        val result = ArrayList<ListenItem>()
        if (words) {
            dao.wordsOfLanguage(language.code).forEach { w ->
                result += ListenItem(w.wordKey, w.text, w.de, sentence = false, weight = ListenWeights.weight(stats[w.wordKey]))
            }
        }
        if (sentences) {
            dao.sentences(language.code, null).forEach { s ->
                result += ListenItem(s.sentenceKey, s.text, s.de, sentence = true, weight = ListenWeights.weight(stats[s.sentenceKey]))
            }
        }
        if (withDialogues) {
            // Ganzer Dialog = ein Eintrag; schlecht gespielte Dialoge (Rolle spielen) kommen oefter
            dialogues.get(language).forEach { d ->
                val p = dialogues.progress(d.key)
                result += ListenItem(
                    key = d.key,
                    text = d.titleTarget.ifEmpty { d.title },
                    de = d.title,
                    sentence = true,
                    weight = ListenWeights.dialogueWeight(p?.lastCorrect, p?.lastTotal),
                    lines = d.lines.map { ListenLine(d.roleName(it.role), it.text, it.de, Dialogues.pitch(it.role)) },
                )
            }
        }
        return result
    }
}
