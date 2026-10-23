package de.jfricker.vokabeltrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.jfricker.vokabeltrainer.data.GrammarItem
import de.jfricker.vokabeltrainer.data.LessonRepository
import de.jfricker.vokabeltrainer.data.PracticeCounts
import de.jfricker.vokabeltrainer.data.PracticeRepository
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
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: LessonRepository,
    private val practiceRepository: PracticeRepository,
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

    /** Startbildschirm der Abfrage mit Anzahl faelliger und neuer Aufgaben. lessonId = null: alle Lektionen. */
    fun preparePractice(lessonId: String?) {
        practiceLessonId = lessonId
        _practice.value = null
        viewModelScope.launch {
            val title = lessonId?.let { practiceRepository.lessonTitle(it) } ?: "Alle Lektionen"
            val counts = PracticeMode.entries.associateWith { practiceRepository.counts(lessonId, it) }
            _practice.value = PracticeState.Setup(title, counts)
        }
    }

    fun startPractice(mode: PracticeMode) {
        practiceMode = mode
        viewModelScope.launch {
            val items = practiceRepository.session(practiceLessonId, mode)
            queue.clear()
            items.forEach { queue.addLast(QueueEntry(it)) }
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
            viewModelScope.launch { practiceRepository.recordAnswer(entry.item.key, s.mode, correct) }
        }
        if (correct) finishedItems++ else queue.addLast(entry.copy(firstAttempt = false))
    }

    private fun showNext() {
        val next = queue.removeFirstOrNull()
        _practice.value = if (next == null) {
            PracticeState.Finished(correctFirst, wrongFirst, nothingToDo = total == 0)
        } else {
            step++
            PracticeState.Asking(practiceMode, next, done = finishedItems, total = total, step = step)
        }
    }
}

data class QueueEntry(val item: PracticeItem, val firstAttempt: Boolean = true)

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

    data class Finished(val correct: Int, val wrong: Int, val nothingToDo: Boolean) : PracticeState
}
