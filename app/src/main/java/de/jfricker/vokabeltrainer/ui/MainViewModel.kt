package de.jfricker.vokabeltrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.jfricker.vokabeltrainer.data.GrammarItem
import de.jfricker.vokabeltrainer.data.LessonRepository
import de.jfricker.vokabeltrainer.data.PracticeCounts
import de.jfricker.vokabeltrainer.data.PracticeRepository
import de.jfricker.vokabeltrainer.data.Stats
import de.jfricker.vokabeltrainer.data.StatsRepository
import de.jfricker.vokabeltrainer.data.TodaySummary
import java.time.LocalDate
import de.jfricker.vokabeltrainer.data.local.LessonEntity
import de.jfricker.vokabeltrainer.data.local.LessonSummary
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import de.jfricker.vokabeltrainer.domain.BuildItem
import de.jfricker.vokabeltrainer.domain.CardItem
import de.jfricker.vokabeltrainer.domain.ClozeItem
import de.jfricker.vokabeltrainer.domain.Exercises
import de.jfricker.vokabeltrainer.domain.PracticeItem
import de.jfricker.vokabeltrainer.domain.PracticeMode
import de.jfricker.vokabeltrainer.speech.Speaker
import de.jfricker.vokabeltrainer.speech.SpeakerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: LessonRepository,
    private val practiceRepository: PracticeRepository,
    private val statsRepository: StatsRepository,
    private val speaker: Speaker,
) : ViewModel() {

    val lessons: StateFlow<List<LessonSummary>> = repository.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val speakerState: StateFlow<SpeakerState> = speaker.state

    private val _importMessage = MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage.asStateFlow()

    private val _slow = MutableStateFlow(false)
    val slow: StateFlow<Boolean> = _slow.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repository.importFromAssets()
            if (result.errors.isNotEmpty()) {
                _importMessage.value = "Fehler beim Laden der Lektionen:\n" + result.errors.joinToString("\n")
            }
        }
    }

    fun lesson(id: String): Flow<LessonEntity?> = repository.observeLesson(id)
    fun words(lessonId: String): Flow<List<WordEntity>> = repository.observeWords(lessonId)
    fun sentences(lessonId: String): Flow<List<SentenceEntity>> = repository.observeSentences(lessonId)
    fun grammar(lessonId: String): Flow<List<GrammarItem>> = repository.observeGrammar(lessonId)

    fun setSlow(value: Boolean) {
        _slow.value = value
    }

    fun speak(text: String) = speaker.speak(text, _slow.value)

    // ---------------------------------------------------------------- Abfrage

    private val _practice = MutableStateFlow<PracticeState?>(null)
    val practice: StateFlow<PracticeState?> = _practice.asStateFlow()

    private var practiceLessonId: String? = null
    private var practiceMode = PracticeMode.CARD_PT_DE
    private val queue = ArrayDeque<QueueEntry>()
    private var total = 0
    private var finishedItems = 0
    private var correctFirst = 0
    private var wrongFirst = 0
    private var step = 0
    private var todayRun = false
    /** Noch laufende Speichervorgaenge von Antworten */
    private val pendingWrites = mutableListOf<Job>()

    // ---------------------------------------------------------------- Heute lernen

    private val _today = MutableStateFlow<TodaySummary?>(null)
    val today: StateFlow<TodaySummary?> = _today.asStateFlow()

    fun refreshToday() {
        viewModelScope.launch { _today.value = practiceRepository.todaySummary() }
    }

    /** Gemischte Tagesrunde. extraNew = trotz erreichtem Tagesziel weitere neue Aufgaben. */
    fun startToday(extraNew: Boolean = false) {
        todayRun = true
        practiceLessonId = null
        _practice.value = null
        viewModelScope.launch {
            val items = practiceRepository.todaySession(extraNew)
            queue.clear()
            items.forEach { (item, mode) -> queue.addLast(QueueEntry(item, mode)) }
            total = items.size
            finishedItems = 0
            correctFirst = 0
            wrongFirst = 0
            showNext()
        }
    }

    // ---------------------------------------------------------------- Statistik

    private val _stats = MutableStateFlow<Stats?>(null)
    val stats: StateFlow<Stats?> = _stats.asStateFlow()

    fun loadStats() {
        viewModelScope.launch { _stats.value = statsRepository.load() }
    }

    fun setTargetDate(date: LocalDate) {
        practiceRepository.setTargetDate(date)
        loadStats()
        refreshToday()
    }

    // ---------------------------------------------------------------- Ablauf einer Runde

    /** Startbildschirm der Abfrage mit Anzahl faelliger und neuer Aufgaben. lessonId = null: alle Lektionen. */
    fun preparePractice(lessonId: String?) {
        practiceLessonId = lessonId
        todayRun = false
        _practice.value = null
        viewModelScope.launch {
            val title = lessonId?.let { practiceRepository.lessonTitle(it) } ?: "Alle Lektionen"
            val counts = PracticeMode.entries.associateWith { practiceRepository.counts(lessonId, it) }
            _practice.value = PracticeState.Setup(title, counts)
        }
    }

    fun startPractice(mode: PracticeMode) {
        practiceMode = mode
        todayRun = false
        viewModelScope.launch {
            val items = practiceRepository.session(practiceLessonId, mode)
            queue.clear()
            items.forEach { queue.addLast(QueueEntry(it, mode)) }
            total = items.size
            finishedItems = 0
            correctFirst = 0
            wrongFirst = 0
            showNext()
        }
    }

    /** Karteikarte umdrehen. */
    fun reveal() {
        val s = asking() ?: return
        val item = s.item as? CardItem ?: return
        if (s.revealed) return
        _practice.value = s.copy(revealed = true)
        // Bei Deutsch -> Portugiesisch die Loesung vorlesen
        if (s.mode == PracticeMode.CARD_DE_PT) speak(item.word.text)
    }

    /** Karteikarte selbst bewerten. */
    fun answerCard(correct: Boolean) {
        val s = asking() ?: return
        if (s.item !is CardItem || !s.revealed) return
        grade(s, correct)
        showNext()
    }

    /** Antwort beim Lueckentext. */
    fun choose(index: Int) {
        val s = asking() ?: return
        if (s.result != null) return
        val (correct, sentence) = when (val item = s.item) {
            is ClozeItem -> (index == item.correctIndex) to item.sentence.text
            else -> return
        }
        grade(s, correct)
        _practice.value = s.copy(chosen = index, result = correct, revealed = true)
        speak(sentence)
    }

    /** Antwort bei Satzbau und Hoer-Diktat: die Bausteine in der gewaehlten Reihenfolge. */
    fun submitBuild(answer: List<String>) {
        val s = asking() ?: return
        val item = s.item as? BuildItem ?: return
        if (s.result != null) return
        val correct = Exercises.isBuildCorrect(item, answer)
        grade(s, correct)
        _practice.value = s.copy(result = correct, revealed = true)
        speak(item.sentence.text)
    }

    /** Nach Lueckentext, Hoerverstehen oder Satzbau zur naechsten Aufgabe. */
    fun continuePractice() {
        val s = asking() ?: return
        if (s.result == null) return
        showNext()
    }

    fun endPractice() {
        queue.clear()
        _practice.value = null
    }

    private fun asking(): PracticeState.Asking? = _practice.value as? PracticeState.Asking

    /** Nur die erste Antwort pro Runde zaehlt fuer den Karteikasten; Falsches kommt nochmals. */
    private fun grade(s: PracticeState.Asking, correct: Boolean) {
        val entry = s.entry
        if (entry.firstAttempt) {
            if (correct) correctFirst++ else wrongFirst++
            pendingWrites.removeAll { it.isCompleted }
            pendingWrites += viewModelScope.launch { practiceRepository.recordAnswer(entry.item.key, s.mode, correct) }
        }
        if (correct) finishedItems++ else queue.addLast(entry.copy(firstAttempt = false))
    }

    private fun showNext() {
        val next = queue.removeFirstOrNull()
        if (next != null) {
            step++
            _practice.value = PracticeState.Asking(next.mode, next, done = finishedItems, total = total, step = step)
            return
        }
        val correct = correctFirst
        val wrong = wrongFirst
        val nothing = total == 0
        if (!todayRun) {
            _practice.value = PracticeState.Finished(correct, wrong, nothingToDo = nothing)
            return
        }
        // Bei "Heute lernen" pruefen, ob noch etwas offen ist (Antworten werden asynchron gespeichert)
        viewModelScope.launch {
            pendingWrites.toList().joinAll()
            pendingWrites.clear()
            val summary = practiceRepository.todaySummary()
            _today.value = summary
            _practice.value = PracticeState.Finished(
                correct, wrong, nothingToDo = nothing, today = true, moreToDo = !summary.goalReached,
            )
        }
    }
}

data class QueueEntry(val item: PracticeItem, val mode: PracticeMode, val firstAttempt: Boolean = true)

sealed interface PracticeState {
    data class Setup(val title: String, val counts: Map<PracticeMode, PracticeCounts>) : PracticeState

    data class Asking(
        val mode: PracticeMode,
        val entry: QueueEntry,
        val done: Int,
        val total: Int,
        /** Zaehlt jede gezeigte Aufgabe hoch, auch wenn dieselbe nochmals kommt */
        val step: Int,
        /** Karteikarte umgedreht bzw. Loesung sichtbar */
        val revealed: Boolean = false,
        /** Gewaehlte Antwort (Lueckentext) */
        val chosen: Int? = null,
        /** null = noch nicht beantwortet */
        val result: Boolean? = null,
    ) : PracticeState {
        val item: PracticeItem get() = entry.item
    }

    data class Finished(
        val correct: Int,
        val wrong: Int,
        val nothingToDo: Boolean,
        /** Runde von "Heute lernen" */
        val today: Boolean = false,
        /** Heute noch Faelliges oder neue Woerter offen */
        val moreToDo: Boolean = false,
    ) : PracticeState
}
