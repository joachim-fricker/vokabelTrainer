package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.ProgressEntity
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import de.jfricker.vokabeltrainer.data.local.ReviewLogEntity
import de.jfricker.vokabeltrainer.domain.CardItem
import de.jfricker.vokabeltrainer.domain.Conjugation
import de.jfricker.vokabeltrainer.domain.ItemKind
import de.jfricker.vokabeltrainer.domain.Tense
import de.jfricker.vokabeltrainer.domain.Verb
import de.jfricker.vokabeltrainer.domain.DailyPlan
import de.jfricker.vokabeltrainer.domain.Exercises
import de.jfricker.vokabeltrainer.domain.GapItem
import de.jfricker.vokabeltrainer.domain.GrammarExercise
import de.jfricker.vokabeltrainer.domain.NumberDrill
import de.jfricker.vokabeltrainer.domain.Leitner
import de.jfricker.vokabeltrainer.domain.PracticeItem
import de.jfricker.vokabeltrainer.domain.PracticeMode
import kotlin.random.Random

/**
 * extra = zusaetzlich frei uebbar (Konjugation: regelmaessige Verben per Zufall; Hoeren; Zahlen).
 * exists = es gibt in dieser Lektion ueberhaupt Aufgaben dieser Art (sonst wird sie nicht angeboten).
 */
data class PracticeCounts(val due: Int, val new: Int, val extra: Int = 0, val exists: Boolean = true)

/**
 * Wort, das bei den Karteikarten schon mal nicht gewusst wurde.
 * wrong = Fehler in beiden Richtungen zusammen; settled = sitzt inzwischen (alle Richtungen ab Fach 3);
 * mode = Richtung mit den meisten Fehlern (darin wird gezielt geuebt).
 */
data class DifficultWord(val word: WordEntity, val wrong: Int, val settled: Boolean, val mode: PracticeMode)

/** Ueberblick fuer die Karte "Heute lernen". */
data class TodaySummary(
    val due: Int,
    val newWordsTarget: Int,
    val newWordsDone: Int,
    val wordsTotal: Int,
    val wordsStarted: Int,
) {
    val newWordsOpen: Int get() = (newWordsTarget - newWordsDone).coerceAtLeast(0)
    val goalReached: Boolean get() = due == 0 && newWordsOpen == 0
}

class PracticeRepository(
    db: AppDatabase,
    private val prefs: LearningPrefs,
    private val exerciseStore: ExerciseStore,
) {

    private val dao = db.practiceDao()
    private val random = Random.Default

    /** Gerade gewaehlte Lernsprache */
    private val lang: String get() = prefs.language.code
    private val prefix: String get() = prefs.language.keyPrefix

    /** Saetze einer Lektion (oder aller) samt verknuepfter Woerter. */
    private class SentenceScope(
        val sentences: List<SentenceEntity>,
        val allWords: List<WordEntity>,
        private val links: Map<String, List<WordEntity>>,
    ) {
        fun linked(s: SentenceEntity): List<WordEntity> = links[s.sentenceKey].orEmpty()
    }

    private class Plan(val due: List<SentenceEntity>, val new: List<SentenceEntity>)

    /**
     * Eine Konjugationsaufgabe: Verb, Zeit, Person. Bei regelmaessigen Verben steht patternKey fuer
     * die Endung (z. B. -ar, Praesens, nós); das Verb ist dann ein zufaellig gewaehltes regelmaessiges.
     */
    private data class ConjTask(val verb: Verb, val tense: Tense, val slot: Int, val patternKey: String? = null) {
        val key: String get() = patternKey ?: Conjugation.itemKey(verb.key, tense, slot)
        fun toItem(random: Random) = Conjugation.item(verb, tense, slot, random, patternKey)
    }

    /** free = schon begonnene Endungen, die gerade nicht faellig sind (freies Ueben) */
    private class ConjPlan(val due: List<ConjTask>, val new: List<ConjTask>, val free: List<ConjTask>)

    suspend fun lessonTitle(lessonId: String): String? = dao.lessonTitle(lessonId)

    suspend fun counts(lessonId: String?, mode: PracticeMode): PracticeCounts {
        val now = System.currentTimeMillis()
        return when (mode.kind) {
            ItemKind.WORD -> PracticeCounts(
                due = dao.dueCount(lang, lessonId, mode.code, now),
                new = dao.newCount(lang, lessonId, mode.code),
            )
            ItemKind.SENTENCE -> if (mode == PracticeMode.DICTATION) {
                PracticeCounts(0, 0, extra = dictationPool(loadScope(lessonId)).size)
            } else {
                val plan = plan(loadScope(lessonId), mode, now)
                PracticeCounts(plan.due.size, plan.new.size)
            }
            ItemKind.VERB -> {
                val plan = conjPlan(lessonId, now)
                PracticeCounts(
                    plan.due.size, plan.new.size, extra = plan.free.size,
                    exists = dao.verbs(lang, lessonId).isNotEmpty(),
                )
            }
            ItemKind.EXERCISE -> {
                val plan = exercisePlan(lessonId, mode, now)
                PracticeCounts(plan.due.size, plan.new.size, exists = plan.total > 0)
            }
            ItemKind.NUMBER -> {
                val available = numbersAvailable(lessonId)
                PracticeCounts(0, 0, extra = if (available) NUMBERS_PER_ROUND else 0, exists = available)
            }
        }
    }

    /** Erst das Faellige, dann Neues bis zum Limit; gemischt. */
    suspend fun session(lessonId: String?, mode: PracticeMode): List<PracticeItem> {
        val now = System.currentTimeMillis()
        if (mode.kind == ItemKind.EXERCISE) {
            val plan = exercisePlan(lessonId, mode, now)
            val due = plan.due.take(MAX_CARDS)
            val new = plan.new.take(minOf(MAX_NEW, MAX_CARDS - due.size))
            val rule = exerciseStore.get(prefs.language).rules[mode.code]
            return (due + new).shuffled(random).map { GapItem.of(it, rule, random) }
        }
        if (mode.kind == ItemKind.NUMBER) {
            return if (numbersAvailable(lessonId)) List(NUMBERS_PER_ROUND) { NumberDrill.item(prefs.language, random) } else emptyList()
        }
        if (mode.kind == ItemKind.VERB) {
            val plan = conjPlan(lessonId, now)
            val due = plan.due.take(MAX_CARDS)
            val new = plan.new.take(minOf(MAX_NEW, MAX_CARDS - due.size))
            // Rest der Runde mit regelmaessigen Verben per Zufall auffuellen
            val free = plan.free.shuffled(random).take(minOf(MAX_FREE_CONJ, MAX_CARDS - due.size - new.size))
            return (due + new + free).shuffled(random).map { it.toItem(random) }
        }
        if (mode.kind == ItemKind.WORD) {
            val due = dao.dueWords(lang, lessonId, mode.code, now, MAX_CARDS)
            val newLimit = minOf(MAX_NEW, MAX_CARDS - due.size)
            val new = if (newLimit > 0) dao.newWords(lang, lessonId, mode.code, newLimit) else emptyList()
            return (due + new).shuffled(random).map { w -> CardItem(w, dao.exampleSentence(w.wordKey)) }
        }

        val scope = loadScope(lessonId)
        if (mode == PracticeMode.DICTATION) {
            // Ohne Karteikasten: einfach ein paar zufaellige laengere Saetze
            val tokens = dictationTokens(scope)
            return dictationPool(scope).shuffled(random).take(DICTATION_PER_ROUND).map { dictationItem(it, tokens) }
        }
        val plan = plan(scope, mode, now)
        val due = plan.due.take(MAX_CARDS)
        val new = plan.new.take(minOf(MAX_NEW, MAX_CARDS - due.size))
        val chosen = (due + new).shuffled(random)

        return chosen.mapNotNull { s ->
            when (mode) {
                PracticeMode.CLOZE -> Exercises.cloze(s, scope.linked(s), scope.allWords, random)
                PracticeMode.BUILD -> Exercises.build(s, random)
                else -> null
            }
        }
    }

    // ------------------------------------------------------------ Hoerverstehen (ohne Karteikasten)

    /** Laengere Saetze zum Anhoeren; knownWords: nur Saetze, deren Woerter man schon begonnen hat. */
    private fun dictationPool(scope: SentenceScope, knownWords: Set<String>? = null): List<SentenceEntity> {
        val candidates = scope.sentences.filter { s ->
            knownWords == null || scope.linked(s).let { l -> l.isNotEmpty() && l.all { it.wordKey in knownWords } }
        }
        val long = candidates.filter { Exercises.tokenize(it.text).size >= LONG_SENTENCE }
        return long.ifEmpty { candidates.filter { Exercises.canBuild(it, minTokens = 4) } }
    }

    /** Woerter aller Saetze je Sprache als Ablenker */
    private fun dictationTokens(scope: SentenceScope): Map<String, List<String>> =
        scope.sentences.groupBy { it.language }.mapValues { (_, list) -> list.flatMap { Exercises.tokenize(it.text) } }

    private fun dictationItem(s: SentenceEntity, tokens: Map<String, List<String>>): PracticeItem {
        val target = Exercises.tokenize(s.text)
        val candidates = tokens[s.language].orEmpty()
        return Exercises.build(s, random, Exercises.similarDistractors(target, candidates, random), audioOnly = true)
    }

    suspend fun recordAnswer(itemKey: String, mode: PracticeMode, correct: Boolean) {
        val review = ReviewLogEntity(itemKey = itemKey, mode = mode.code, correct = correct, reviewedAt = System.currentTimeMillis())
        if (!mode.leitner) {
            // Hoerverstehen / Zahlen hoeren: nur fuer die Statistik zaehlen, kein Lernstand
            prefs.incrementToday(if (mode == PracticeMode.NUMBERS) LearningPrefs.NUMBERS else LearningPrefs.DICTATIONS)
            dao.insertReview(review)
            return
        }
        val current = dao.progress(itemKey, mode.code)
        if (current == null) {
            // Zaehlt fuer das Tagesziel
            when {
                mode == PracticeMode.CARD_PT_DE -> prefs.incrementToday(LearningPrefs.NEW_WORDS)
                mode.kind == ItemKind.SENTENCE -> prefs.incrementToday(LearningPrefs.NEW_SENTENCES)
                mode.kind == ItemKind.VERB -> prefs.incrementToday(LearningPrefs.NEW_CONJ)
                mode.kind == ItemKind.EXERCISE -> prefs.incrementToday(LearningPrefs.NEW_GRAMMAR)
                else -> Unit
            }
        }
        dao.saveProgress(Leitner.next(current, itemKey, mode, correct))
        dao.insertReview(review)
    }

    // ------------------------------------------------------------ Schwierige Woerter

    /** Schwierige Woerter nach Anzahl Fehler; bei gleich vielen zuerst die, die noch nicht sitzen. */
    suspend fun difficultWords(): List<DifficultWord> {
        val byWord = dao.troubleProgress(lang).groupBy { it.wordKey }
        if (byWord.isEmpty()) return emptyList()
        val words = byWord.keys.chunked(500).flatMap { dao.wordsByKeys(it) }.associateBy { it.wordKey }
        return byWord.mapNotNull { (key, rows) ->
            val word = words[key] ?: return@mapNotNull null
            val worst = rows.maxBy { it.wrongCount }
            DifficultWord(
                word = word,
                wrong = rows.sumOf { it.wrongCount },
                settled = rows.all { it.box >= SETTLED_BOX },
                mode = PracticeMode.entries.firstOrNull { it.code == worst.direction } ?: PracticeMode.CARD_PT_DE,
            )
        }.sortedWith(compareByDescending<DifficultWord> { it.wrong }.thenBy { it.settled })
    }

    /** Runde mit den schwierigsten Woertern, die noch nicht sitzen (sonst mit allen schwierigen). */
    suspend fun difficultSession(): List<Pair<PracticeItem, PracticeMode>> {
        val all = difficultWords()
        val open = all.filter { !it.settled }.ifEmpty { all }
        return open.take(MAX_CARDS)
            .map { d -> CardItem(d.word, dao.exampleSentence(d.word.wordKey)) to d.mode }
            .shuffled(random)
    }

    // ------------------------------------------------------------ Heute lernen

    /** Uebungsarten mit Karteikasten (Hoerverstehen hat keinen Lernstand) */
    private val allCodes = PracticeMode.entries.filter { it.leitner }.map { it.code }

    suspend fun todaySummary(): TodaySummary {
        val total = dao.wordCount(lang)
        val started = dao.startedCount(prefix, PracticeMode.CARD_PT_DE.code)
        val introduced = prefs.countToday(LearningPrefs.NEW_WORDS)
        return TodaySummary(
            due = dao.dueAllCount(prefix, System.currentTimeMillis(), allCodes),
            newWordsTarget = DailyPlan.newWordsPerDay(total, started, introduced, prefs.newWordsPerDay),
            newWordsDone = introduced,
            wordsTotal = total,
            wordsStarted = started,
        )
    }

    fun setNewWordsPerDay(value: Int) {
        prefs.newWordsPerDay = value
    }

    /**
     * Gemischte Tagesrunde ueber alle Lektionen:
     * 1. alles Faellige (Woerter und Saetze, alle Uebungsarten)
     * 2. neue Woerter in Lektionsreihenfolge bis zum Tagesziel
     * 3. Woerter, die PT->DE sitzen, erstmals DE->PT
     * 4. neue Konjugationsaufgaben fuer Verben, die man als Wort schon kennt
     * 5. neue Grammatik-Uebungen (max. 6 pro Tag, Arten abwechselnd, nur mit bekannten Woertern)
     * 6. neue Satz-Uebungen, aber nur mit Saetzen, deren Woerter man schon kennt;
     *    pro Satz erst Lueckentext, dann Satzbau
     * 7. ab und zu Zahlen hoeren (ohne Karteikasten, max. 2 pro Tag)
     * 8. ab und zu ein laengerer Satz zum Hoeren (Hoerverstehen ohne Karteikasten, max. 3 pro Tag)
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

        val allVerbs = dao.verbs(lang, null).map(VerbJson::toVerb)
        val verbsByKey = allVerbs.associateBy { it.key }
        val regularByClass = allVerbs.filter { !it.irregular }.groupBy { it.verbClass }
        val grammar = exerciseStore.get(prefs.language)
        val result = mutableListOf<Pair<PracticeItem, PracticeMode>>()

        // 1. Faelliges
        for (p in dao.dueAll(prefix, now, allCodes, TODAY_MAX)) {
            val mode = PracticeMode.entries.firstOrNull { it.code == p.direction } ?: continue
            val item: PracticeItem? = when (mode.kind) {
                ItemKind.WORD -> dao.word(p.wordKey)?.let { CardItem(it, dao.exampleSentence(it.wordKey)) }
                ItemKind.SENTENCE -> sentencesByKey[p.wordKey]?.let { sentenceItem(it, mode) }
                ItemKind.EXERCISE -> grammar.byKey[p.wordKey]?.let { GapItem.of(it, grammar.rules[it.type], random) }
                ItemKind.NUMBER -> null
                ItemKind.VERB -> Conjugation.parseKey(p.wordKey)?.let { (verbKey, tense, slot) ->
                    val cls = Conjugation.patternClass(verbKey)
                    if (cls != null) {
                        regularByClass[cls]?.randomOrNull(random)?.let { Conjugation.item(it, tense, slot, random, p.wordKey) }
                    } else {
                        verbsByKey[verbKey]?.takeIf { it.irregular }?.let { Conjugation.item(it, tense, slot, random) }
                    }
                }
            }
            if (item != null) result += item to mode
        }
        var slots = TODAY_MAX - result.size

        // 2. Neue Woerter
        if (slots > 0) {
            val summary = todaySummary()
            val quota = if (extraNew) EXTRA_NEW_WORDS else summary.newWordsOpen
            val words = dao.newWords(lang, null, PracticeMode.CARD_PT_DE.code, minOf(quota, slots))
            words.forEach { w -> result += CardItem(w, dao.exampleSentence(w.wordKey)) to PracticeMode.CARD_PT_DE }
            slots -= words.size
        }

        // 3. Umgekehrte Richtung fuer Woerter, die schon sitzen
        if (slots > 0) {
            val words = dao.reverseCandidates(lang, minBox = 2, limit = minOf(slots, MAX_REVERSE_NEW))
            words.forEach { w -> result += CardItem(w, dao.exampleSentence(w.wordKey)) to PracticeMode.CARD_DE_PT }
            slots -= words.size
        }

        // 4. Neue Konjugationsaufgaben (nur Verben, die man als Wort schon begonnen hat)
        val known = dao.startedWordKeys().toSet()
        if (slots > 0) {
            val quota = minOf(
                slots,
                if (extraNew) EXTRA_NEW_CONJ
                else (CONJ_PER_DAY - prefs.countToday(LearningPrefs.NEW_CONJ)).coerceAtLeast(0),
            )
            val tasks = conjPlan(null, now, knownWords = known).new.take(quota)
            tasks.forEach { t -> result += t.toItem(random) to PracticeMode.CONJUGATION }
            slots -= tasks.size
        }

        // 5. Neue Grammatik-Uebungen (Verb im Satz, ser/estar, Artikel, Praeposition, Plural), abwechselnd
        if (slots > 0) {
            val quota = minOf(
                slots,
                if (extraNew) EXTRA_NEW_GRAMMAR
                else (GRAMMAR_PER_DAY - prefs.countToday(LearningPrefs.NEW_GRAMMAR)).coerceAtLeast(0),
            )
            val picked = newGrammar(grammar.exercises, known, quota)
            picked.forEach { e ->
                val mode = PracticeMode.entries.first { it.code == e.type }
                result += GapItem.of(e, grammar.rules[e.type], random) to mode
            }
            slots -= picked.size
        }

        // 6. Neue Satz-Uebungen
        if (slots > 0) {
            val quota = minOf(
                slots,
                if (extraNew) EXTRA_NEW_SENTENCES
                else (SENTENCES_PER_DAY - prefs.countToday(LearningPrefs.NEW_SENTENCES)).coerceAtLeast(0),
            )
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

        // 7. Ab und zu Zahlen hoeren (Preise, Uhrzeiten), hoechstens NUMBERS_PER_DAY pro Tag
        if (result.size >= MIN_ROUND_FOR_DICTATION && prefs.countToday(LearningPrefs.NUMBERS) < NUMBERS_PER_DAY) {
            result += NumberDrill.item(prefs.language, random) to PracticeMode.NUMBERS
        }

        // 8. Ab und zu ein laengerer Satz zum Hoeren (ohne Karteikasten), nicht in sonst leeren Runden
        if (result.size >= MIN_ROUND_FOR_DICTATION && prefs.countToday(LearningPrefs.DICTATIONS) < DICTATIONS_PER_DAY) {
            dictationPool(scope, knownWords = known).randomOrNull(random)?.let {
                result += dictationItem(it, tokensByLanguage) to PracticeMode.DICTATION
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

    // ------------------------------------------------------------ Grammatik-Uebungen

    private class ExercisePlan(val due: List<GrammarExercise>, val new: List<GrammarExercise>, val total: Int)

    private suspend fun exercisePlan(lessonId: String?, mode: PracticeMode, now: Long): ExercisePlan {
        val all = exerciseStore.get(prefs.language).exercises
            .filter { it.type == mode.code && (lessonId == null || it.lessonId == lessonId) }
        val progress = dao.progressForMode(mode.code).associateBy { it.wordKey }
        val due = all.filter { e -> progress[e.key]?.let { it.dueAt <= now } == true }
            .sortedBy { progress[it.key]?.dueAt ?: 0L }
        val new = all.filter { it.key !in progress }
        return ExercisePlan(due, new, all.size)
    }

    /**
     * Neue Grammatik-Uebungen fuer "Heute lernen": nur mit bekannten Woertern, in Lektionsreihenfolge,
     * die Uebungsarten abwechselnd (sonst kaemen fast nur Artikel).
     */
    private suspend fun newGrammar(all: List<GrammarExercise>, known: Set<String>, quota: Int): List<GrammarExercise> {
        if (quota <= 0) return emptyList()
        val started = PracticeMode.entries.filter { it.kind == ItemKind.EXERCISE }
            .flatMap { m -> dao.progressForMode(m.code).map { it.wordKey } }.toSet()
        val queues = all
            .filter { it.key !in started && it.words.isNotEmpty() && it.words.all { w -> w in known } }
            .groupBy { it.type }
            .values.map { ArrayDeque(it) }
            .shuffled(random)
        val result = mutableListOf<GrammarExercise>()
        while (result.size < quota && queues.any { it.isNotEmpty() }) {
            for (q in queues) {
                if (result.size >= quota) break
                q.removeFirstOrNull()?.let { result += it }
            }
        }
        return result
    }

    /** Zahlen hoeren gibt es bei "alle Lektionen" und in der Lektion mit den Zahlen. */
    private fun numbersAvailable(lessonId: String?): Boolean =
        lessonId == null || lessonId == prefs.language.numbersLessonId

    /**
     * Konjugationsaufgaben in Lektionsreihenfolge.
     * - Unregelmaessige Verben: jede Form einzeln (Verb x Zeit x Person).
     * - Regelmaessige Verben: nur die Endungen (-ar/-er/-ir x Zeit x Person), jedes Mal mit einem
     *   zufaelligen regelmaessigen Verb dieser Klasse. Eine Klasse kommt dran, sobald ihr erstes
     *   regelmaessiges Verb in der Lektionsreihenfolge erreicht ist.
     * Die Vergangenheit kommt jeweils erst, wenn alle Praesensformen zuletzt richtig waren (Fach >= 2).
     * knownWords: nur Verben, deren Wort schon geuebt wurde (fuer "Heute lernen").
     */
    private suspend fun conjPlan(lessonId: String?, now: Long, knownWords: Set<String>? = null): ConjPlan {
        val verbs = dao.verbs(lang, lessonId).map(VerbJson::toVerb)
            .filter { knownWords == null || it.key in knownWords }
        val progress = dao.progressForMode(PracticeMode.CONJUGATION.code).associateBy { it.wordKey }
        val regularByClass = verbs.filter { !it.irregular }.groupBy { it.verbClass }
        val due = mutableListOf<Pair<ConjTask, Long>>()
        val new = mutableListOf<ConjTask>()
        val free = mutableListOf<ConjTask>()
        val classesDone = mutableSetOf<String>()

        /** tasksFor(tense) liefert die Aufgaben einer Zeit; regular = Endungen (frei uebbar) */
        fun collect(tasksFor: (Tense) -> List<ConjTask>?, regular: Boolean) {
            var previousLearned = true
            for (tense in Tense.entries) {
                val tasks = tasksFor(tense) ?: continue
                for (t in tasks) {
                    val p = progress[t.key]
                    when {
                        p == null -> if (previousLearned) new += t
                        p.dueAt <= now -> due += t to p.dueAt
                        regular -> free += t
                    }
                }
                previousLearned = tasks.all { (progress[it.key]?.box ?: 0) >= 2 }
            }
        }

        for (verb in verbs) {
            val slots = verb.language.persons.indices
            if (verb.irregular) {
                collect({ tense -> if (verb.forms[tense] == null) null else slots.map { ConjTask(verb, tense, it) } }, regular = false)
            } else if (classesDone.add(verb.verbClass)) {
                val pool = regularByClass[verb.verbClass].orEmpty()
                collect({ tense ->
                    slots.map { slot ->
                        val v = pool.random(random)
                        ConjTask(v, tense, slot, Conjugation.patternKey(v.language, v.verbClass, tense, slot))
                    }
                }, regular = true)
            }
        }
        return ConjPlan(due.sortedBy { it.second }.map { it.first }, new, free)
    }

    private suspend fun loadScope(lessonId: String?): SentenceScope {
        val words = dao.allWords()
        val byKey = words.associateBy { it.wordKey }
        val links = dao.sentenceWords(lessonId)
            .groupBy({ it.sentenceKey }, { byKey[it.wordKey] })
            .mapValues { (_, list) -> list.filterNotNull() }
        return SentenceScope(dao.sentences(lang, lessonId), words, links)
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

        /** Ab diesem Fach gilt ein schwieriges Wort als "sitzt inzwischen" (wie "sicher" in der Statistik) */
        const val SETTLED_BOX = 3

        /** Aufgaben pro Runde bei "Heute lernen" */
        const val TODAY_MAX = 30
        const val MAX_REVERSE_NEW = 8
        const val SENTENCES_PER_DAY = 15
        const val EXTRA_NEW_WORDS = 10
        const val EXTRA_NEW_SENTENCES = 8

        /** Neue Konjugationsaufgaben pro Tag bei "Heute lernen" (= zwei Verben im Praesens auf Portugiesisch) */
        const val CONJ_PER_DAY = 8
        const val EXTRA_NEW_CONJ = 8

        /** Gezieltes Ueben: so viele Zufallsaufgaben mit regelmaessigen Verben hoechstens zusaetzlich */
        const val MAX_FREE_CONJ = 10
        private val SENTENCE_CHAIN = listOf(PracticeMode.CLOZE, PracticeMode.BUILD)

        /** Hoerverstehen: ab so vielen Bausteinen gilt ein Satz als "laenger" */
        const val LONG_SENTENCE = 6
        const val DICTATION_PER_ROUND = 5
        /** Heute lernen: hoechstens ein Hoersatz pro Runde und so viele pro Tag */
        const val DICTATIONS_PER_DAY = 3
        const val MIN_ROUND_FOR_DICTATION = 5

        /** Neue Grammatik-Uebungen pro Tag bei "Heute lernen" */
        const val GRAMMAR_PER_DAY = 6
        const val EXTRA_NEW_GRAMMAR = 6

        /** Zahlen hoeren: so viele pro Runde beim gezielten Ueben, hoechstens so viele pro Tag bei "Heute lernen" */
        const val NUMBERS_PER_ROUND = 10
        const val NUMBERS_PER_DAY = 2
    }
}
