package de.jfricker.vokabeltrainer.domain

import de.jfricker.vokabeltrainer.data.local.ProgressEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Uebungsarten. code wird in progress.direction gespeichert - nicht aendern,
 * sonst geht der Lernstand verloren. "pt-de"/"de-pt" stehen historisch fuer
 * Lernsprache -> Deutsch bzw. Deutsch -> Lernsprache und gelten fuer alle Sprachen.
 * In label/description wird {L} durch den Sprachnamen, {a} durch das Adjektiv ersetzt.
 */
enum class PracticeMode(
    val code: String,
    private val labelTemplate: String,
    private val descriptionTemplate: String,
    val sentenceBased: Boolean,
) {
    CARD_PT_DE("pt-de", "{L} → Deutsch", "Karteikarte: Wort lesen und hören", false),
    CARD_DE_PT("de-pt", "Deutsch → {L}", "Karteikarte: das {a} Wort finden", false),
    CLOZE("cloze", "Lückentext", "Das fehlende Wort im Satz auswählen", true),
    DICTATION("dictation", "Hörverstehen", "Satz nur anhören und aus Bausteinen zusammensetzen", true),
    BUILD("build", "Satzbau", "Den Satz aus Bausteinen zusammensetzen", true);

    fun label(language: Language): String = fill(labelTemplate, language)
    fun description(language: Language): String = fill(descriptionTemplate, language)

    private fun fill(t: String, l: Language) = t.replace("{L}", l.displayName).replace("{a}", l.adjective)
}

/**
 * Leitner-Karteikasten:
 * - richtig: ein Fach weiter, Pause laut [INTERVAL_DAYS]
 * - falsch: zurueck in Fach 1, sofort wieder faellig
 * - Abkuerzung fuer Bekanntes: gleich beim ersten Mal richtig -> Fach [FIRST_TRY_BOX];
 *   noch nie falsch und zum zweiten Mal richtig -> [KNOWN_BOX], kommt nicht mehr dran
 */
object Leitner {
    const val MAX_BOX = 6

    /** "Kann ich": wird nicht mehr abgefragt (dueAt = Long.MAX_VALUE). */
    const val KNOWN_BOX = 7

    /** Fach nach dem ersten Mal, wenn gleich richtig (Pause 8 Tage). */
    const val FIRST_TRY_BOX = 4

    /** Pause in Tagen je Fach (Index = Fach). */
    private val INTERVAL_DAYS = intArrayOf(0, 1, 2, 4, 8, 16, 32)

    fun next(
        current: ProgressEntity?,
        itemKey: String,
        mode: PracticeMode,
        correct: Boolean,
        now: Long = System.currentTimeMillis(),
    ): ProgressEntity {
        val neverWrong = (current?.wrongCount ?: 0) == 0
        val correctBefore = current?.correctCount ?: 0
        val box = when {
            !correct -> 1
            current == null -> FIRST_TRY_BOX
            neverWrong && correctBefore >= 1 -> KNOWN_BOX
            else -> minOf((current?.box ?: 0) + 1, MAX_BOX)
        }
        val dueAt = when {
            !correct -> now
            box == KNOWN_BOX -> Long.MAX_VALUE
            else -> startOfDayInDays(INTERVAL_DAYS[box])
        }
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
