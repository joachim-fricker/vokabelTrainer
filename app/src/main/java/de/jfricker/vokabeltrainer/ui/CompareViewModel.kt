package de.jfricker.vokabeltrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.jfricker.vokabeltrainer.data.CompareData
import de.jfricker.vokabeltrainer.data.CompareStore
import de.jfricker.vokabeltrainer.domain.CompareCard
import de.jfricker.vokabeltrainer.domain.CompareSchedule
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.speech.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Ueberblick einer Gruppe: sitzt / faellig / neu */
data class CompareCounts(val total: Int, val sits: Int, val due: Int, val new: Int) {
    /** So viele Karten hat die naechste Runde */
    val round: Int get() = minOf(CompareSchedule.MAX_CARDS, due + minOf(new, CompareSchedule.MAX_NEW))
}

/** Uebung "Beide Woerter": deutsches Wort (bzw. falscher Freund) -> beide Sprachen, selbst bewerten. */
sealed interface ComparePractice {
    data class Asking(val card: CompareCard, val revealed: Boolean, val done: Int, val total: Int) : ComparePractice
    data class Finished(val correct: Int, val total: Int) : ComparePractice
}

class CompareViewModel(private val store: CompareStore, private val speaker: Speaker) : ViewModel() {

    private val _data = MutableStateFlow<CompareData?>(null)
    val data: StateFlow<CompareData?> = _data.asStateFlow()

    /** Zaehlt hoch, wenn sich der Lernstand aendert (Ueberblick neu berechnen) */
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    init {
        viewModelScope.launch { _data.value = store.get() }
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    fun counts(cards: List<CompareCard>): CompareCounts {
        val today = today()
        val progress = cards.map { store.progress(it.key) }
        return CompareCounts(
            total = cards.size,
            sits = progress.count { CompareSchedule.sits(it) },
            due = progress.count { CompareSchedule.isDue(it, today) },
            new = progress.count { CompareSchedule.isNew(it) },
        )
    }

    fun sits(key: String): Boolean = CompareSchedule.sits(store.progress(key))

    /** Ein Wort in seiner Sprache vorlesen */
    fun speak(text: String, language: Language) = speaker.speakIn(listOf(text to language))

    fun speakAll(card: CompareCard) = speaker.speakIn(card.answers.map { it.speak to it.language })

    // ---------------------------------------------------------------- Uebung

    private val _practice = MutableStateFlow<ComparePractice?>(null)
    val practice: StateFlow<ComparePractice?> = _practice.asStateFlow()

    private val queue = ArrayDeque<Pair<CompareCard, Boolean>>()
    private var lastPool: List<CompareCard> = emptyList()
    private var total = 0
    private var done = 0
    private var correctFirst = 0

    fun startPractice(pool: List<CompareCard>) {
        lastPool = pool
        val round = CompareSchedule.pick(pool, store::progress, today())
        queue.clear()
        round.shuffled().forEach { queue.addLast(it to true) }
        total = round.size
        done = 0
        correctFirst = 0
        showNext()
    }

    /** Nach einer Runde gleich weiter mit derselben Gruppe */
    fun practiceAgain() = startPractice(lastPool)

    fun reveal() {
        val s = _practice.value as? ComparePractice.Asking ?: return
        if (s.revealed) return
        _practice.value = s.copy(revealed = true)
        speakAll(s.card)
    }

    /** Selbst bewerten: beide gewusst? Nur der erste Versuch zaehlt, Falsches kommt nochmals. */
    fun grade(correct: Boolean) {
        val s = _practice.value as? ComparePractice.Asking ?: return
        if (!s.revealed) return
        val (card, first) = queue.removeFirstOrNull() ?: return
        if (first) {
            store.save(card.key, CompareSchedule.answer(store.progress(card.key), correct, today()))
            if (correct) correctFirst++
            _version.value++
        }
        if (correct) done++ else queue.addLast(card to false)
        showNext()
    }

    fun endPractice() {
        speaker.stop()
        queue.clear()
        _practice.value = null
        _version.value++
    }

    private fun showNext() {
        val next = queue.firstOrNull()
        _practice.value = if (next == null) {
            ComparePractice.Finished(correctFirst, total)
        } else {
            ComparePractice.Asking(next.first, revealed = false, done = done, total = total)
        }
    }
}
