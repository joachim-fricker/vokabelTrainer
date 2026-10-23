package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.LessonStat
import de.jfricker.vokabeltrainer.domain.DailyPlan
import de.jfricker.vokabeltrainer.domain.PracticeMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayCount(val date: LocalDate, val count: Int)

data class Stats(
    val targetDate: LocalDate,
    val daysLeft: Long,
    val wordsTotal: Int,
    val wordsStarted: Int,
    /** PT->DE mindestens in Fach 3 (also mehrmals an verschiedenen Tagen gewusst) */
    val wordsLearned: Int,
    val newWordsPerDay: Int,
    val newWordsToday: Int,
    /** Tage in Folge mit mindestens einer Antwort */
    val streak: Int,
    val reviewsToday: Int,
    val correctPercentToday: Int?,
    val dueUntilTomorrow: Int,
    val last14Days: List<DayCount>,
    val lessons: List<LessonStat>,
)

class StatsRepository(db: AppDatabase, private val prefs: LearningPrefs) {

    private val dao = db.practiceDao()

    suspend fun load(): Stats {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        fun startOf(d: LocalDate) = d.atStartOfDay(zone).toInstant().toEpochMilli()

        val rows = dao.reviewsSince(startOf(today.minusDays(400)))
        val byDay = rows.groupBy { Instant.ofEpochMilli(it.reviewedAt).atZone(zone).toLocalDate() }

        // Serie: heute (oder, falls heute noch nichts, ab gestern) rueckwaerts zaehlen
        var day = if (byDay.containsKey(today)) today else today.minusDays(1)
        var streak = 0
        while (byDay.containsKey(day)) {
            streak++
            day = day.minusDays(1)
        }

        val todayRows = byDay[today].orEmpty()
        val total = dao.wordCount()
        val started = dao.startedCount(PracticeMode.CARD_PT_DE.code)
        val newToday = prefs.countToday(LearningPrefs.NEW_WORDS)
        val target = prefs.targetDate

        return Stats(
            targetDate = target,
            daysLeft = DailyPlan.daysLeft(target, today),
            wordsTotal = total,
            wordsStarted = started,
            wordsLearned = dao.boxAtLeastCount(PracticeMode.CARD_PT_DE.code, LEARNED_BOX),
            newWordsPerDay = DailyPlan.newWordsPerDay(total, started, newToday, target, today),
            newWordsToday = newToday,
            streak = streak,
            reviewsToday = todayRows.size,
            correctPercentToday = if (todayRows.isEmpty()) null else todayRows.count { it.correct } * 100 / todayRows.size,
            dueUntilTomorrow = dao.dueBeforeCount(startOf(today.plusDays(2)), PracticeMode.entries.map { it.code }),
            last14Days = (13 downTo 0).map { today.minusDays(it.toLong()) }.map { DayCount(it, byDay[it]?.size ?: 0) },
            lessons = dao.lessonStats(LEARNED_BOX),
        )
    }

    companion object {
        const val LEARNED_BOX = 3
    }
}
