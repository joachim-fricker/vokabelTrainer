package de.jfricker.vokabeltrainer.domain

import kotlin.random.Random

/**
 * Grammatik-Uebung aus assets/exercises/<sprache>.json (erzeugt mit tools/grammar/exercises.py):
 * Luecke zwischen before und after, answer gehoert hinein. type = PracticeMode.code.
 * key = "<sprache>:<id>", z. B. "pt-BR:vf:pt-BR-03:s05" (Lernstand-Schluessel).
 */
data class GrammarExercise(
    val key: String,
    val type: String,
    val language: Language,
    val lessonId: String,
    val sortOrder: Int,
    val before: String,
    val after: String,
    val answer: String,
    /** alle Antwortmoeglichkeiten inkl. Loesung */
    val options: List<String>,
    /** nicht mischen (z. B. ser | estar, o | a) */
    val fixedOrder: Boolean,
    /** z. B. "(querer)" oder "(em + o)" */
    val hint: String,
    val de: String,
    /** wird nach der Antwort vorgelesen */
    val speak: String,
    /** kurzer Hinweis nach der Antwort, z. B. "querer: Präsens, eu" */
    val note: String,
    /** Woerter, die man kennen sollte (fuer "Heute lernen") */
    val words: List<String>,
    /** darf eingetippt werden (Verbform, Plural) */
    val typeable: Boolean,
)

/** Eine Grammatik-Aufgabe in der Runde; rule = allgemeine Regel zur Uebungsart. */
data class GapItem(
    val exercise: GrammarExercise,
    val options: List<String>,
    val rule: String?,
) : PracticeItem {
    override val key: String get() = exercise.key
    val correctIndex: Int get() = options.indexOf(exercise.answer)

    companion object {
        fun of(e: GrammarExercise, rule: String?, random: Random) =
            GapItem(e, if (e.fixedOrder) e.options else e.options.shuffled(random), rule)
    }
}
