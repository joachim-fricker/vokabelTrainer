package de.jfricker.vokabeltrainer.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import de.jfricker.vokabeltrainer.domain.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface SpeakerState {
    data object Initializing : SpeakerState
    data object Ready : SpeakerState
    data class Unavailable(val message: String) : SpeakerState
}

/** Sprachausgabe ueber die Android-TextToSpeech-Engine (offline, kostenlos). */
class Speaker(context: Context, language: Language) : TextToSpeech.OnInitListener {

    private var language = language
    private var initialized = false
    private val _state = MutableStateFlow<SpeakerState>(SpeakerState.Initializing)
    val state: StateFlow<SpeakerState> = _state.asStateFlow()

    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            _state.value = SpeakerState.Unavailable("Die Sprachausgabe konnte nicht gestartet werden.")
            return
        }
        initialized = true
        tts.setOnUtteranceProgressListener(progressListener)
        applyLanguage()
    }

    /** Beim Wechsel der Lernsprache aufrufen. */
    fun setLanguage(value: Language) {
        if (value == language && _state.value != SpeakerState.Initializing) return
        language = value
        if (initialized) applyLanguage()
    }

    /**
     * Erste Stimme aus Language.ttsLocales, die es fuer genau dieses Land gibt;
     * sonst irgendeine Stimme der Sprache.
     */
    private fun localeFor(lang: Language): Locale? {
        val locales = lang.ttsLocales.map { Locale.forLanguageTag(it) }
        return locales.firstOrNull { tts.isLanguageAvailable(it) >= TextToSpeech.LANG_COUNTRY_AVAILABLE }
            ?: locales.firstOrNull { tts.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
    }

    private fun applyLanguage() {
        val chosen = localeFor(language)
        val result = chosen?.let { tts.setLanguage(it) } ?: TextToSpeech.LANG_NOT_SUPPORTED
        _state.value = if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            SpeakerState.Unavailable(
                "Keine Stimme für ${language.variantName} installiert. " +
                    "Bitte in den Android-Einstellungen unter Sprachausgabe nachladen."
            )
        } else {
            SpeakerState.Ready
        }
    }

    fun speak(text: String, slow: Boolean = false, pitch: Float = 1.0f) {
        finishSequence()
        if (_state.value != SpeakerState.Ready) return
        tts.setSpeechRate(if (slow) 0.6f else 1.0f)
        tts.setPitch(pitch)
        tts.speak(cleanForSpeech(text), TextToSpeech.QUEUE_FLUSH, null, "utt-${text.hashCode()}")
    }

    /**
     * Texte in bestimmten Sprachen nacheinander vorlesen, unabhaengig von der eingestellten Lernsprache
     * (Sprachvergleich: erst Portugiesisch, dann Spanisch). Danach gilt wieder die Lernsprache.
     */
    fun speakIn(items: List<Pair<String, Language>>, slow: Boolean = false) {
        finishSequence()
        if (!initialized || items.isEmpty()) return
        tts.setSpeechRate(if (slow) 0.6f else 1.0f)
        tts.setPitch(1.0f)
        var first = true
        items.forEachIndexed { i, (text, lang) ->
            // Sprache wird bei jedem speak() fuer diesen Text uebernommen
            val locale = localeFor(lang) ?: return@forEachIndexed
            tts.setLanguage(locale)
            if (!first) tts.playSilentUtterance(400, TextToSpeech.QUEUE_ADD, "in-pause-$i")
            tts.speak(cleanForSpeech(text), if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "in-$i-${text.hashCode()}")
            first = false
        }
        applyLanguage()
    }

    // ---------------------------------------------------------------- mehrere Saetze hintereinander (Dialoge)

    /** Nummer der laufenden Folge; Rueckmeldungen aelterer Folgen werden ignoriert. */
    @Volatile private var sequence = 0
    @Volatile private var onLine: ((Int) -> Unit)? = null
    @Volatile private var onSequenceDone: (() -> Unit)? = null

    /**
     * Saetze nacheinander vorlesen, jeder mit eigener Stimmhoehe (Text, Pitch), dazwischen pauseMs Stille.
     * onLine(i) beim Start von Satz i, onDone am Ende oder beim Abbrechen (auch durch speak/stop).
     * Die Rueckmeldungen kommen nicht im Main-Thread.
     */
    fun speakLines(
        lines: List<Pair<String, Float>>,
        slow: Boolean,
        pauseMs: Long = 0,
        onLine: (Int) -> Unit,
        onDone: () -> Unit,
    ) {
        finishSequence()
        if (_state.value != SpeakerState.Ready || lines.isEmpty()) {
            onDone()
            return
        }
        val seq = ++sequence
        this.onLine = onLine
        this.onSequenceDone = onDone
        tts.setSpeechRate(if (slow) 0.6f else 1.0f)
        lines.forEachIndexed { i, (text, pitch) ->
            if (i > 0 && pauseMs > 0) {
                tts.playSilentUtterance(pauseMs, TextToSpeech.QUEUE_ADD, "pause:$seq:$i")
            }
            // Rate und Pitch werden bei jedem speak() fuer diesen Satz uebernommen
            tts.setPitch(pitch)
            val last = if (i == lines.lastIndex) 1 else 0
            tts.speak(
                cleanForSpeech(text),
                if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                "seq:$seq:$i:$last",
            )
        }
        tts.setPitch(1.0f)
    }

    /** Vorlesen abbrechen. */
    fun stop() {
        if (initialized) tts.stop()
        finishSequence()
    }

    private fun finishSequence() {
        val done = onSequenceDone
        onSequenceDone = null
        onLine = null
        done?.invoke()
    }

    /** "seq:<folge>:<satz>:<letzter>" -> (folge, satz, letzter) */
    private fun parse(id: String?): Triple<Int, Int, Boolean>? {
        val parts = id?.split(':') ?: return null
        if (parts.size != 4 || parts[0] != "seq") return null
        val seq = parts[1].toIntOrNull() ?: return null
        val index = parts[2].toIntOrNull() ?: return null
        return Triple(seq, index, parts[3] == "1")
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            val (seq, index, _) = parse(utteranceId) ?: return
            if (seq == sequence) onLine?.invoke(index)
        }

        override fun onDone(utteranceId: String?) {
            val (seq, _, last) = parse(utteranceId) ?: return
            if (seq == sequence && last) finishSequence()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            val (seq, _, _) = parse(utteranceId) ?: return
            if (seq == sequence) finishSequence()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            val (seq, _, _) = parse(utteranceId) ?: return
            if (seq == sequence) finishSequence()
        }
    }

    /** Hinweise in Klammern, z. B. "(sagt Maria)", nicht mitsprechen. */
    private fun cleanForSpeech(text: String): String =
        text.replace(Regex("\\([^)]*\\)"), "").trim()
}
