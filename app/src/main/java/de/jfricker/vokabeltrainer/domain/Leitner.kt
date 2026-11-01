package de.jfricker.vokabeltrainer.domain

import de.jfricker.vokabeltrainer.data.local.ProgressEntity
import java.time.LocalDate
import java.time.ZoneId

/** Worauf sich eine Uebungsart bezieht. */
enum class ItemKind { WORD, SENTENCE, VERB, EXERCISE, NUMBER }

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
    val kind: ItemKind,
) {
    CARD_PT_DE("pt-de", "{L} → Deutsch", "Karteikarte: Wort lesen und hören", ItemKind.WORD),
    CARD_DE_PT("de-pt", "Deutsch → {L}", "Karteikarte: das {a} Wort finden", ItemKind.WORD),
    CLOZE("cloze", "Lückentext", "Das fehlende Wort im Satz auswählen", ItemKind.SENTENCE),
    DICTATION("dictation", "Hörverstehen", "Längere Sätze anhören und zusammensetzen – ohne Karteikasten", ItemKind.SENTENCE),
    BUILD("build", "Satzbau", "Den Satz aus Bausteinen zusammensetzen", ItemKind.SENTENCE),
    CONJUGATION("conj", "Konjugation", "Unregelmässige Verben Form für Form, regelmässige per Zufall", ItemKind.VERB),
    VERBFORM("verbform", "Verb im Satz", "Die passende Verbform in den Satz einsetzen", ItemKind.EXERCISE),
    SER_ESTAR("serestar", "ser oder estar", "Welches der beiden Verben passt in den Satz?", ItemKind.EXERCISE),
    ARTICLE("article", "Artikel & Geschlecht", "Den richtigen Artikel vor das Nomen setzen", ItemKind.EXERCISE),
    CONTRACTION("contraction", "Präposition + Artikel", "Zusammengezogene Formen: {c}", ItemKind.EXERCISE),
    PLURAL("plural", "Plural bilden", "Die Mehrzahl eines Nomens bilden", ItemKind.EXERCISE),
    NUMBERS("numbers", "Zahlen hören", "Preise, Uhrzeiten und Zahlen hören und eintippen – ohne Karteikasten", ItemKind.NUMBER);

    val sentenceBased: Boolean get() = kind == ItemKind.SENTENCE

    /** Mit Karteikasten (Lernstand, Faelligkeit). Nicht bei Hoerverstehen und Zahlen hoeren: nur ab und zu zum Ueben. */
    val leitner: Boolean get() = this != DICTATION && this != NUMBERS

    fun label(language: Language): String = fill(labelTemplate, language)
    fun description(language: Language): String = fill(descriptionTemplate, language)

    private fun fill(t: String, l: Language) =
        t.replace("{L}", l.displayName).replace("{a}", l.adjective).replace("{c}", l.contractionExample)
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
