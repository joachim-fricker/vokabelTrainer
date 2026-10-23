package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.ProgressEntity
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import de.jfricker.vokabeltrainer.data.local.ReviewLogEntity
import de.jfricker.vokabeltrainer.domain.CardItem
import de.jfricker.vokabeltrainer.domain.DailyPlan
import de.jfricker.vokabeltrainer.domain.Exercises
import de.jfricker.vokabeltrainer.domain.Leitner
import de.jfricker.vokabeltrainer.domain.PracticeItem
import de.jfricker.vokabeltrainer.domain.PracticeMode
import java.time.LocalDate
import kotlin.random.Random

data class PracticeCounts(val due: Int, val new: Int)

/** Ueberblick fuer die Karte "Heute lernen". */
data class TodaySummary(
    val due: Int,
    val newWordsTarget: Int,
    val newWordsDone: Int,
    val daysLeft: Long,
    val targetDate: LocalDate,
    val wordsTotal: Int,
    val wordsStarted: Int,
) {
    val newWordsOpen: Int get() = (newWordsTarget - newWordsDone).coerceAtLeast(0)
    val goalReached: Boolean get() = due == 0 && newWordsOpen == 0
}

class PracticeRepository(db: AppDatabase, private val prefs: LearningPrefs) {

    private val dao = db.practiceDao()
    private val random = Random.Default

    /** Saetze einer Lektion (oder aller) samt verknuepfter Woerter. */
    private class SentenceScope(
        val sentences: List<SentenceEntity>,
        val allWords: List<WordEntity>,
        private val links: Map<String, List<WordEntity>>,
    ) {
        fun linked(s: SentenceEntity): List<WordEntity> = links[s.sentenceKey].orEmpty()
    }

    private class Plan(val due: List<SentenceEntity>, val new: List<SentenceEntity>)

    suspend fun lessonTitle(lessonId: String): String? = dao.lessonTitle(lessonId)

    suspend fun counts(lessonId: String?, mode: PracticeMode): PracticeCounts {
        val now = System.currentTimeMillis()
        return if (!mode.sentenceBased) {
            PracticeCounts(
                due = dao.dueCount(lessonId, mode.code, now),
                new = dao.newCount(lessonId, mode.code),
            )
        } else {
            val plan = plan(loadScope(lessonId), mode, now)
            PracticeCounts(plan.due.size, plan.new.size)
        }
    }

    /** Erst das Faellige, dann Neues bis zum Limit; gemischt. */
    suspend fun session(lessonId: String?, mode: PracticeMode): List<PracticeItem> {
        val now = System.currentTimeMillis()
        if (!mode.sentenceBased) {
            val due = dao.dueWords(lessonId, mode.code, now, MAX_CARDS)
            val newLimit = minOf(MAX_NEW, MAX_CARDS - due.size)
            val new = if (newLimit > 0) dao.newWords(lessonId, mode.code, newLimit) else emptyList()
            return (due + new).shuffled(random).map { w -> CardItem(w, dao.exampleSentence(w.wordKey)) }
        }

        val scope = loadScope(lessonId)
        val plan = plan(scope, mode, now)
        val due = plan.due.take(MAX_CARDS)
        val new = plan.new.take(minOf(MAX_NEW, MAX_CARDS - due.size))
        val chosen = (due + new).shuffled(random)
        // Woerter aller Saetze als Ablenker fuers Hoer-Diktat
        val tokensByLanguage = if (mode == PracticeMode.DICTATION) {
            dao.allSentences().groupBy { it.language }
                .mapValues { (_, list) -> list.flatMap { Exercises.tokenize(it.text) } }
        } else {
            emptyMap()
        }

        return chosen.mapNotNull { s ->
            when (mode) {
                PracticeMode.CLOZE -> Exercises.cloze(s, scope.linked(s), scope.allWords, random)
                PracticeMode.DICTATION -> {
                    val target = Exercises.tokenize(s.text)
                    val candidates = tokensByLanguage[s.language].orEmpty()
                    Exercises.build(s, random, Exercises.similarDistractors(target, candidates, random), audioOnly = true)
                }
                PracticeMode.BUILD -> Exercises.build(s, random)
                else -> null
            }
        }
    }

    suspend fun recordAnswer(itemKey: String, mode: PracticeMode, correct: Boolean) {
        val current = dao.progress(itemKey, mode.code)
        if (current == null) {
            // Zaehlt fuer das Tagesziel
            if (mode == PracticeMode.CARD_PT_DE) prefs.incrementToday(LearningPrefs.NEW_WORDS)
            else if (mode.sentenceBased) prefs.incrementToday(LearningPrefs.NEW_SENTENCES)
        }
        dao.saveProgress(Leitner.next(current, itemKey, mode, correct))
        dao.insertReview(ReviewLogEntity(itemKey = itemKey, mode = mode.code, correct = correct, reviewedAt = System.currentTimeMillis()))
    }

    // ------------------------------------------------------------ Heute lernen

    private val allCodes = PracticeMode.entries.map { it.code }

    suspend fun todaySummary(): TodaySummary {
        val total = dao.wordCount()
        val started = dao.startedCount(PracticeMode.CARD_PT_DE.code)
        val introduced = prefs.countToday(LearningPrefs.NEW_WORDS)
        val target = prefs.targetDate
        return TodaySummary(
            due = dao.dueAllCount(System.currentTimeMillis(), allCodes),
            newWordsTarget = DailyPlan.newWordsPerDay(total, started, introduced, target),
            newWordsDone = introduced,
            daysLeft = DailyPlan.daysLeft(target),
            targetDate = target,
            wordsTotal = total,
            wordsStarted = started,
        )
    }

    fun setTargetDate(date: LocalDate) {
        prefs.targetDate = date
    }

    /**
     * Gemischte Tagesrunde ueber alle Lektionen:
     * 1. alles Faellige (Woerter und Saetze, alle Uebungsarten)
     * 2. neue Woerter in Lektionsreihenfolge bis zum Tagesziel
     * 3. Woerter, die PT->DE sitzen, erstmals DE->PT
     * 4. neue Satz-Uebungen, aber nur mit Saetzen, deren Woerter man schon kennt;
     *    pro Satz erst Lueckentext, dann Hoerverstehen, dann Satzbau
     * extraNew = Tagesziel erreicht, trotzdem weitere neue Aufgaben.
     */
    suspend fun todaySession(extraNew: Boolean): List<Pair<PracticeItem, PracticeMode>> {
        val now = System.currentTimeMillis()
        val scope = loadScope(null)
        val sentencesByKey = scope.sentences.associateBy { it.sentenceKey }
        val tokensByLanguage = scope.sentences.groupBy { it.language }
            .mapValues { (_, list) -> list.flatMap { Exercises.tokenize(it.text) } }

        fun sentenceItem(sentence: SentenceEntity, mode: PracticeMode): PracticeItem? = when (mode) {
            PracticeMode.CLOZE -> Exercises.cloze(sentence, scope.linked(sentence), scope.allWords, random)
            PracticeMode.DICTATION -> {
                val target = Exercises.tokenize(sentence.text)
                val candidates = tokensByLanguage[sentence.language].orEmpty()
                Exercises.build(sentence, random, Exercises.similarDistractors(target, candidates, random), audioOnly = true)
            }
            PracticeMode.BUILD -> Exercises.build(sentence, random)
            else -> null
        }

        val result = mutableListOf<Pair<PracticeItem, PracticeMode>>()

        // 1. Faelliges
        for (p in dao.dueAll(now, allCodes, TODAY_MAX)) {
            val mode = PracticeMode.entries.firstOrNull { it.code == p.direction } ?: continue
            val item: PracticeItem? = if (!mode.sentenceBased) {
                dao.word(p.wordKey)?.let { CardItem(it, dao.exampleSentence(it.wordKey)) }
            } else {
                sentencesByKey[p.wordKey]?.let { sentenceItem(it, mode) }
            }
            if (item != null) result += item to mode
        }
        var slots = TODAY_MAX - result.size

        // 2. Neue Woerter
        if (slots > 0) {
            val summary = todaySummary()
            val quota = if (extraNew) EXTRA_NEW_WORDS else summary.newWordsOpen
            val words = dao.newWords(null, PracticeMode.CARD_PT_DE.code, minOf(quota, slots))
            words.forEach { w -> result += CardItem(w, dao.exampleSentence(w.wordKey)) to PracticeMode.CARD_PT_DE }
            slots -= words.size
        }

        // 3. Umgekehrte Richtung fuer Woerter, die schon sitzen
        if (slots > 0) {
            val words = dao.reverseCandidates(minBox = 2, limit = minOf(slots, MAX_REVERSE_NEW))
            words.forEach { w -> result += CardItem(w, dao.exampleSentence(w.wordKey)) to PracticeMode.CARD_DE_PT }
            slots -= words.size
        }

        // 4. Neue Satz-Uebungen
        if (slots > 0) {
            val quota = minOf(
                slots,
                if (extraNew) EXTRA_NEW_SENTENCES
                else (SENTENCES_PER_DAY - prefs.countToday(LearningPrefs.NEW_SENTENCES)).coerceAtLeast(0),
            )
            val known = dao.startedWordKeys().toSet()
            val progress = SENTENCE_CHAIN.associateWith { m -> dao.progressForMode(m.code).associateBy { it.wordKey } }
            var picked = 0
            for (s in scope.sentences) {
                if (picked >= quota) break
                val linked = scope.linked(s)
                if (linked.isEmpty() || linked.any { it.wordKey !in known }) continue
                val mode = nextSentenceMode(s, linked, progress) ?: continue
                sentenceItem(s, mode)?.let {
                    result += it to mode
                    picked++
                }
            }
        }

        return result.shuffled(random)
    }

    /** Naechste noch nie geuebte Satz-Uebung; die vorige muss mindestens in Fach 2 sein. */
    private fun nextSentenceMode(
        s: SentenceEntity,
        linked: List<WordEntity>,
        progress: Map<PracticeMode, Map<String, ProgressEntity>>,
    ): PracticeMode? {
        var previousOk = true
        for (mode in SENTENCE_CHAIN) {
            val eligible = when (mode) {
                PracticeMode.CLOZE -> Exercises.clozeCandidates(s, linked).isNotEmpty()
                PracticeMode.DICTATION -> Exercises.canBuild(s, minTokens = 2)
                else -> Exercises.canBuild(s)
            }
            if (!eligible) continue
            val p = progress[mode]?.get(s.sentenceKey) ?: return if (previousOk) mode else null
            previousOk = p.box >= 2
        }
        return null
    }

    private suspend fun loadScope(lessonId: String?): SentenceScope {
        val words = dao.allWords()
        val byKey = words.associateBy { it.wordKey }
        val links = dao.sentenceWords(lessonId)
            .groupBy({ it.sentenceKey }, { byKey[it.wordKey] })
            .mapValues { (_, list) -> list.filterNotNull() }
        return SentenceScope(dao.sentences(lessonId), words, links)
    }

    private suspend fun plan(scope: SentenceScope, mode: PracticeMode, now: Long): Plan {
        val eligible = when (mode) {
            PracticeMode.CLOZE -> scope.sentences.filter { Exercises.clozeCandidates(it, scope.linked(it)).isNotEmpty() }
            PracticeMode.BUILD -> scope.sentences.filter { Exercises.canBuild(it) }
            PracticeMode.DICTATION -> scope.sentences.filter { Exercises.canBuild(it, minTokens = 2) }
            else -> scope.sentences
        }
        val progress = dao.progressForMode(mode.code).associateBy { it.wordKey }
        val due = eligible
            .filter { s -> progress[s.sentenceKey]?.let { it.dueAt <= now } == true }
            .sortedBy { s -> progress[s.sentenceKey]?.dueAt ?: 0L }
        val new = eligible.filter { it.sentenceKey !in progress }
        return Plan(due, new)
    }

    companion object {
        const val MAX_CARDS = 20
        const val MAX_NEW = 10

        /** Aufgaben pro Runde bei "Heute lernen" */
        const val TODAY_MAX = 30
        const val MAX_REVERSE_NEW = 8
        const val SENTENCES_PER_DAY = 15
        const val EXTRA_NEW_WORDS = 10
        const val EXTRA_NEW_SENTENCES = 8
        private val SENTENCE_CHAIN = listOf(PracticeMode.CLOZE, PracticeMode.DICTATION, PracticeMode.BUILD)
    }
}
