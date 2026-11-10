package de.jfricker.vokabeltrainer.domain

/** Tagesziel neue Woerter: fest eingestellte Anzahl pro Tag (je Sprache, in der Statistik aenderbar). */
object DailyPlan {
    const val DEFAULT_NEW_PER_DAY = 10
    const val MAX_NEW_PER_DAY = 40

    /** Gewaehltes Tagesziel, aber nicht mehr, als zu Beginn des Tages noch offen war. */
    fun newWordsPerDay(totalWords: Int, startedWords: Int, introducedToday: Int, perDay: Int): Int {
        val remaining = (totalWords - startedWords + introducedToday).coerceAtLeast(0)
        return perDay.coerceAtMost(remaining)
    }
}
