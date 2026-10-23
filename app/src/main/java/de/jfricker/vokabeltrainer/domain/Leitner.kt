package de.jfricker.vokabeltrainer.domain

import de.jfricker.vokabeltrainer.data.local.ProgressEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Uebungsarten. code wird in progress.direction gespeichert - nicht aendern,
 * sonst geht der Lernstand verloren.
 */
enum class PracticeMode(
    val code: String,
    val label: String,
    val description: String,
    val sentenceBased: Boolean,
) {
    CARD_PT_DE("pt-de", "Portugiesisch → Deutsch", "Karteikarte: Wort lesen und hören", false),
    CARD_DE_PT("de-pt", "Deutsch → Portugiesisch", "Karteikarte: das portugiesische Wort finden", false),
    CLOZE("cloze", "Lückentext", "Das fehlende Wort im Satz auswählen", true),
    DICTATION("dictation", "Hörverstehen", "Satz nur anhören und aus Bausteinen zusammensetzen", true),
    BUILD("build", "Satzbau", "Den Satz aus Bausteinen zusammensetzen", true),
}

/**
 * Leitner-Karteikasten:
 * - richtig: ein Fach weiter, Pause laut [INTERVAL_DAYS]
 * - falsch: zurueck in Fach 1, sofort wieder faellig
 */
object Leitner {
    const val MAX_BOX = 6

    /** Pause in Tagen je Fach (Index = Fach). */
    private val INTERVAL_DAYS = intArrayOf(0, 1, 2, 4, 8, 16, 32)

    fun next(
        current: ProgressEntity?,
        itemKey: String,
        mode: PracticeMode,
        correct: Boolean,
        now: Long = System.currentTimeMillis(),
    ): ProgressEntity {
        val box = if (correct) minOf((current?.box ?: 0) + 1, MAX_BOX) else 1
        val dueAt = if (correct) startOfDayInDays(INTERVAL_DAYS[box]) else now
        return ProgressEntity(
            wordKey = itemKey,
            direction = mode.code,
            box = box,
            dueAt = dueAt,
            lastReviewedAt = now,
            correctCount = (current?.correctCount ?: 0) + if (correct) 1 else 0,
            wrongCount = (current?.wrongCount ?: 0) + if (correct) 0 else 1,
        )
    }

    /** Faellig ab Tagesbeginn, damit ein Wort nicht erst zur selben Uhrzeit wiederkommt. */
    private fun startOfDayInDays(days: Int): Long =
        LocalDate.now().plusDays(days.toLong())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
