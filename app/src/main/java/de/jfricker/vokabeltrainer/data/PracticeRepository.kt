package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import de.jfricker.vokabeltrainer.domain.CardItem
import de.jfricker.vokabeltrainer.domain.Exercises
import de.jfricker.vokabeltrainer.domain.Leitner
import de.jfricker.vokabeltrainer.domain.PracticeItem
import de.jfricker.vokabeltrainer.domain.PracticeMode
import kotlin.random.Random

data class PracticeCounts(val due: Int, val new: Int)

class PracticeRepository(db: AppDatabase) {

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
        dao.saveProgress(Leitner.next(current, itemKey, mode, correct))
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
    }
}
