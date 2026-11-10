package de.jfricker.vokabeltrainer.domain

import kotlin.random.Random

/** Gruppen im Sprachvergleich Portugiesisch <-> Spanisch (assets/compare/pairs.json). */
enum class PairKind(val code: String, val title: String, val description: String) {
    DIFFERENT(
        "different", "Ganz verschieden",
        "Wörter, die in den beiden Sprachen ganz anders heissen. Hier passieren die meisten Verwechslungen – " +
            "am besten immer beide zusammen lernen.",
    ),
    SIMILAR(
        "similar", "Ähnlich",
        "Ähnliche Wörter, oft nach einem festen Muster. Kennst du das Muster, kannst du das andere Wort ableiten.",
    ),
    SAME(
        "same", "Gleich",
        "Gleich geschrieben (höchstens andere Akzente oder Artikel) – einmal merken, doppelt gewonnen.",
    );

    companion object {
        fun fromCode(code: String): PairKind = entries.firstOrNull { it.code == code } ?: DIFFERENT
    }
}

/** Wort mit gleicher Bedeutung in beiden Sprachen; key "p:<pt-Wort-ID>|<es-Wort-ID>" */
data class WordPair(
    val key: String,
    val ptText: String,
    val esText: String,
    val de: String,
    val kind: PairKind,
    /** IDs aus SoundPattern */
    val patterns: List<String>,
)

/** Typisches Lautmuster, z. B. -ção -> -ción */
data class SoundPattern(val id: String, val title: String, val explanation: String)

/** Gleiches (oder fast gleiches) Wort mit verschiedener Bedeutung; key "f:<pt>" */
data class FalseFriend(val key: String, val pt: String, val ptDe: String, val es: String, val esDe: String, val note: String)

// ---------------------------------------------------------------- Konjugation regelmaessiger Verben

/** Zeile der Konjugationstabelle nach deutscher Person; Index = Position der Form in den Verbtabellen */
data class VerbPerson(val de: String, val pt: String, val ptIndex: Int, val es: String, val esIndex: Int)

data class VerbTense(val id: String, val title: String, val pt: String, val es: String)

/** Regelmaessiges Verb in beiden Sprachen (gleiche Klasse), Formen je Zeit ("pres", "past") */
data class VerbPair(
    val de: String,
    val pt: String,
    val es: String,
    val ptForms: Map<String, List<String>>,
    val esForms: Map<String, List<String>>,
) {
    fun ptForm(tense: String, p: VerbPerson): String? = ptForms[tense]?.getOrNull(p.ptIndex)?.let { "${p.pt} $it" }
    fun esForm(tense: String, p: VerbPerson): String? = esForms[tense]?.getOrNull(p.esIndex)?.let { "${p.es} $it" }
}

/** -ar / -er / -ir; das erste Verb ist das Beispiel */
data class VerbClass(val id: String, val title: String, val verbs: List<VerbPair>)

data class VerbSection(
    val persons: List<VerbPerson>,
    val tenses: List<VerbTense>,
    val notes: List<String>,
    val classes: List<VerbClass>,
) {
    /**
     * Karten "beide konjugieren": eine pro Klasse x Zeit x Person (Lernstand pro Endung wie im
     * Konjugationstrainer), jedes Mal mit einem zufaelligen Verb der Klasse.
     */
    fun cards(random: Random = Random): List<CompareCard> = classes.flatMap { cls ->
        tenses.flatMap { tense ->
            persons.mapIndexedNotNull { i, person ->
                val verb = cls.verbs.randomOrNull(random) ?: return@mapIndexedNotNull null
                val pt = verb.ptForm(tense.id, person) ?: return@mapIndexedNotNull null
                val es = verb.esForm(tense.id, person) ?: return@mapIndexedNotNull null
                CompareCard(
                    key = "v:${cls.id}:${tense.id}:$i",
                    question = "Konjugiere auf Portugiesisch und auf Spanisch – ${tense.title}:",
                    prompt = "${person.de} · ${verb.de}",
                    answers = listOf(CardAnswer(Language.PT_BR, pt, pt), CardAnswer(Language.ES_AR, es, es)),
                    note = "${verb.pt} / ${verb.es} · regelmässig ${cls.title}",
                )
            }
        }
    }
}

/** Eine Antwort auf der Karte: Text (fuer die Anzeige) und was in welcher Sprache vorgelesen wird. */
data class CardAnswer(val language: Language, val text: String, val speak: String)

/** Karte der Uebung "Beide Woerter": vorne prompt, hinten je eine Antwort pro Sprache. */
data class CompareCard(
    val key: String,
    val question: String,
    val prompt: String,
    val answers: List<CardAnswer>,
    val note: String = "",
) {
    companion object {
        fun of(p: WordPair) = CompareCard(
            key = p.key,
            question = "Wie heisst das auf Portugiesisch und auf Spanisch?",
            prompt = p.de,
            answers = listOf(
                CardAnswer(Language.PT_BR, p.ptText, p.ptText),
                CardAnswer(Language.ES_AR, p.esText, p.esText),
            ),
        )

        fun of(f: FalseFriend) = CompareCard(
            key = f.key,
            question = "Was bedeutet es auf Portugiesisch und was auf Spanisch?",
            prompt = if (f.pt == f.es) f.pt else "${Language.PT_BR.flag} ${f.pt}   ${Language.ES_AR.flag} ${f.es}",
            answers = listOf(
                CardAnswer(Language.PT_BR, f.ptDe, f.pt),
                CardAnswer(Language.ES_AR, f.esDe, f.es),
            ),
            note = f.note,
        )
    }
}

/** Lernstand einer Vergleichskarte (kleiner Karteikasten, Faelligkeit in Tagen seit 1970). */
data class CardProgress(val box: Int = 0, val due: Long = 0, val wrong: Int = 0)

/**
 * Wie beim grossen Karteikasten: Bekanntes nur 1–2 Mal (beim ersten Mal gleich gewusst -> Fach 4,
 * danach nochmals gewusst und nie falsch -> bekannt). Falsch -> Fach 1, sofort wieder faellig.
 */
object CompareSchedule {
    const val KNOWN = 7
    const val MAX_CARDS = 15
    const val MAX_NEW = 10
    private val pauseDays = intArrayOf(0, 1, 2, 4, 8, 16, 32)

    fun answer(p: CardProgress, correct: Boolean, today: Long): CardProgress = when {
        !correct -> CardProgress(box = 1, due = today, wrong = p.wrong + 1)
        p.box == 0 -> CardProgress(box = 4, due = today + pauseDays[4], wrong = 0)
        p.wrong == 0 && p.box >= 4 -> CardProgress(box = KNOWN, due = Long.MAX_VALUE, wrong = 0)
        else -> {
            val box = (p.box + 1).coerceAtMost(6)
            CardProgress(box = box, due = today + pauseDays[box], wrong = p.wrong)
        }
    }

    fun isNew(p: CardProgress) = p.box == 0
    fun isDue(p: CardProgress, today: Long) = p.box in 1..6 && p.due <= today
    /** "sitzt": mindestens Fach 3 */
    fun sits(p: CardProgress) = p.box >= 3

    /** Runde: zuerst Faelliges (am laengsten faellig zuerst), dann Neues in Listenreihenfolge. */
    fun pick(
        cards: List<CompareCard>,
        progress: (String) -> CardProgress,
        today: Long,
        max: Int = MAX_CARDS,
        maxNew: Int = MAX_NEW,
    ): List<CompareCard> {
        val due = cards.filter { isDue(progress(it.key), today) }.sortedBy { progress(it.key).due }
        val new = cards.filter { isNew(progress(it.key)) }
        val round = due.take(max)
        return round + new.take(minOf(maxNew, max - round.size))
    }
}
