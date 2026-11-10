package de.jfricker.vokabeltrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.jfricker.vokabeltrainer.data.DialogueProgress
import de.jfricker.vokabeltrainer.data.DialogueStore
import de.jfricker.vokabeltrainer.data.LearningPrefs
import de.jfricker.vokabeltrainer.domain.Dialogue
import de.jfricker.vokabeltrainer.domain.Dialogues
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.speech.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DialogueSummary(val dialogue: Dialogue, val progress: DialogueProgress?)

/** "Rolle spielen": du sagst die Saetze einer Rolle, die App spricht die andere. */
data class RolePlayState(
    /** 1 = Du (normal), 0 = Rollen getauscht */
    val learnerRole: Int,
    /** Zeilen 0 until shown sind als Sprechblasen sichtbar */
    val shown: Int,
    /** Zeile, die du gerade sagen sollst; null = Dialog fertig */
    val current: Int?,
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1,
    /** Ausgewaehlte Antwort (Auswaehlen) */
    val chosen: Int? = null,
    /** Laut sagen: Loesung aufgedeckt */
    val revealed: Boolean = false,
    /** Zeile -> richtig */
    val results: Map<Int, Boolean> = emptyMap(),
) {
    val finished: Boolean get() = current == null
    val answered: Boolean get() = current != null && results.containsKey(current)
    val correctCount: Int get() = results.values.count { it }
}

class DialogueViewModel(
    private val store: DialogueStore,
    private val speaker: Speaker,
    private val prefs: LearningPrefs,
) : ViewModel() {

    val language: StateFlow<Language> = prefs.languageFlow

    // ---------------------------------------------------------------- Liste

    private val _list = MutableStateFlow<List<DialogueSummary>?>(null)
    val list: StateFlow<List<DialogueSummary>?> = _list.asStateFlow()

    fun loadList() {
        val lang = prefs.language
        viewModelScope.launch {
            _list.value = store.get(lang).map { DialogueSummary(it, store.progress(it.key)) }
        }
    }

    // ---------------------------------------------------------------- ein Dialog

    private val _dialogue = MutableStateFlow<Dialogue?>(null)
    val dialogue: StateFlow<Dialogue?> = _dialogue.asStateFlow()

    /** Alle Dialoge der Sprache (fuer Antworten aus anderen Dialogen) */
    private var all: List<Dialogue> = emptyList()

    fun open(key: String) {
        if (_dialogue.value?.key == key) return
        stop()
        _rolePlay.value = null
        viewModelScope.launch {
            all = store.get(Language.fromCode(key.substringBefore(':')))
            _dialogue.value = all.firstOrNull { it.key == key }
        }
    }

    fun close() {
        stop()
        _rolePlay.value = null
        _dialogue.value = null
    }

    private val _slow = MutableStateFlow(false)
    val slow: StateFlow<Boolean> = _slow.asStateFlow()
    fun setSlow(value: Boolean) {
        _slow.value = value
    }

    /** Deutsche Uebersetzung unter den Saetzen zeigen */
    private val _showTranslation = MutableStateFlow(true)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()
    fun setShowTranslation(value: Boolean) {
        _showTranslation.value = value
    }

    /** Rolle spielen: Satz laut sagen und selbst bewerten statt aus drei Saetzen auswaehlen */
    private val _speakMode = MutableStateFlow(false)
    val speakMode: StateFlow<Boolean> = _speakMode.asStateFlow()
    fun setSpeakMode(value: Boolean) {
        _speakMode.value = value
    }

    // ---------------------------------------------------------------- Anhoeren

    /** Zeile, die gerade vorgelesen wird */
    private val _playing = MutableStateFlow<Int?>(null)
    val playing: StateFlow<Int?> = _playing.asStateFlow()

    fun playAll() = playFrom(0, _dialogue.value?.lines?.size ?: 0)

    /** Zeilen from until end nacheinander vorlesen, jede Rolle mit eigener Stimmhoehe. */
    private fun playFrom(from: Int, end: Int) {
        val d = _dialogue.value ?: return
        if (from >= end) return
        val lines = d.lines.subList(from, end)
        speaker.speakLines(
            lines.map { it.text to Dialogues.pitch(it.role) },
            _slow.value,
            pauseMs = Dialogues.pauseMs(_slow.value),
            onLine = { _playing.value = from + it },
            onDone = { _playing.value = null },
        )
    }

    fun speakLine(index: Int) {
        val line = _dialogue.value?.lines?.getOrNull(index) ?: return
        speaker.speak(line.text, _slow.value, Dialogues.pitch(line.role))
        _playing.value = null
    }

    fun stop() {
        speaker.stop()
        _playing.value = null
    }

    // ---------------------------------------------------------------- Rolle spielen

    private val _rolePlay = MutableStateFlow<RolePlayState?>(null)
    val rolePlay: StateFlow<RolePlayState?> = _rolePlay.asStateFlow()

    fun startRolePlay(learnerRole: Int) {
        val d = _dialogue.value ?: return
        stop()
        advance(d, RolePlayState(learnerRole = learnerRole, shown = 0, current = null), 0)
    }

    /** Antwort auswaehlen */
    fun choose(index: Int) {
        val s = _rolePlay.value ?: return
        val cur = s.current ?: return
        if (s.answered) return
        _rolePlay.value = s.copy(chosen = index, results = s.results + (cur to (index == s.correctIndex)))
        speakLine(cur)
    }

    /** Laut sagen: Loesung zeigen und vorlesen */
    fun reveal() {
        val s = _rolePlay.value ?: return
        val cur = s.current ?: return
        if (s.answered || s.revealed) return
        _rolePlay.value = s.copy(revealed = true)
        speakLine(cur)
    }

    /** Laut sagen: selbst bewerten, dann gleich weiter */
    fun selfGrade(correct: Boolean) {
        val s = _rolePlay.value ?: return
        val cur = s.current ?: return
        if (s.answered || !s.revealed) return
        val d = _dialogue.value ?: return
        advance(d, s.copy(results = s.results + (cur to correct)), cur + 1)
    }

    /** Nach dem Auswaehlen zum naechsten eigenen Satz */
    fun next() {
        val s = _rolePlay.value ?: return
        val cur = s.current ?: return
        if (!s.answered) return
        val d = _dialogue.value ?: return
        advance(d, s, cur + 1)
    }

    /**
     * Saetze des Gegenuebers ab from zeigen und vorlesen, bis du wieder dran bist.
     * Kommt kein eigener Satz mehr, ist der Dialog fertig und das Ergebnis wird gespeichert.
     */
    private fun advance(d: Dialogue, base: RolePlayState, from: Int) {
        val next = (from until d.lines.size).firstOrNull { d.lines[it].role == base.learnerRole }
        if (next == null) {
            _rolePlay.value = base.copy(shown = d.lines.size, current = null, options = emptyList(), chosen = null, revealed = false)
            if (base.results.isNotEmpty()) {
                store.recordRolePlay(d.key, base.correctCount, base.results.size)
            }
        } else {
            val (options, correctIndex) = Dialogues.options(d, next, all)
            _rolePlay.value = base.copy(
                shown = next, current = next, options = options, correctIndex = correctIndex,
                chosen = null, revealed = false,
            )
        }
        playFrom(from, next ?: d.lines.size)
    }

    override fun onCleared() {
        speaker.stop()
    }
}
