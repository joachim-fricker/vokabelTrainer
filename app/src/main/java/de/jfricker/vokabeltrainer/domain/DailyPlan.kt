package de.jfricker.vokabeltrainer.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

/** Wie viele neue Woerter pro Tag noetig sind, um bis zum Zieldatum durch zu sein. */
object DailyPlan {
    /** Die letzten Tage vor dem Ziel nur wiederholen (falls genug Zeit bleibt). */
    private const val BUFFER_DAYS = 3
    private const val DEFAULT_NEW_PER_DAY = 10

    fun daysLeft(target: LocalDate, today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, target)

    fun newWordsPerDay(
        totalWords: Int,
        startedWords: Int,
        introducedToday: Int,
        target: LocalDate,
        today: LocalDate = LocalDate.now(),
    ): Int {
        // Was zu Beginn des heutigen Tages noch offen war
        val remaining = totalWords - startedWords + introducedToday
        if (remaining <= 0) return 0
        val days = daysLeft(target, today)
        if (days <= 0) return DEFAULT_NEW_PER_DAY
        val learningDays = if (days > BUFFER_DAYS * 2) days - BUFFER_DAYS else days
        return ceil(remaining / learningDays.toDouble()).toInt()
    }
}
