package de.jfricker.vokabeltrainer.speech

import android.content.Context
import android.speech.tts.TextToSpeech
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
class Speaker(context: Context, locale: Locale = Locale.forLanguageTag("pt-BR")) : TextToSpeech.OnInitListener {

    private val targetLocale = locale
    private val _state = MutableStateFlow<SpeakerState>(SpeakerState.Initializing)
    val state: StateFlow<SpeakerState> = _state.asStateFlow()

    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            _state.value = SpeakerState.Unavailable("Die Sprachausgabe konnte nicht gestartet werden.")
            return
        }
        val result = tts.setLanguage(targetLocale)
        _state.value = if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            SpeakerState.Unavailable(
                "Keine Stimme für Portugiesisch (Brasilien) installiert. " +
                    "Bitte in den Android-Einstellungen unter Sprachausgabe nachladen."
            )
        } else {
            SpeakerState.Ready
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (_state.value != SpeakerState.Ready) return
        tts.setSpeechRate(if (slow) 0.6f else 1.0f)
        tts.speak(cleanForSpeech(text), TextToSpeech.QUEUE_FLUSH, null, "utt-${text.hashCode()}")
    }

    /** Hinweise in Klammern, z. B. "(sagt Maria)", nicht mitsprechen. */
    private fun cleanForSpeech(text: String): String =
        text.replace(Regex("\\([^)]*\\)"), "").trim()
}
