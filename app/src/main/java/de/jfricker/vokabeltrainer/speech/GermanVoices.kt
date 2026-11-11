package de.jfricker.vokabeltrainer.speech

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice

/**
 * Deutsche Stimme fuer "Nur hören" (Wunsch des Nutzers, 11.11.2026: maennlich statt weiblich).
 * Android verraet das Geschlecht einer Stimme nicht offiziell; geraten wird ueber den Namen
 * (Samsung "SMTm00", Google "de-de-x-deb/deg", Namen mit "male"). In "Nur hören" laesst sich die
 * Stimme deshalb auch von Hand waehlen.
 */
object GermanVoices {

    /** Installierte deutsche Stimmen, die ohne Internet gehen, nach Name sortiert. */
    fun list(tts: TextToSpeech): List<Voice> =
        (tts.voices ?: emptySet())
            .filter { it.locale.language == "de" }
            .filter { !it.isNetworkConnectionRequired }
            .filter { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features }
            .sortedWith(compareBy({ it.locale.country != "DE" }, { it.name }))

    /** true = vermutlich maennlich, false = vermutlich weiblich, null = unbekannt */
    fun isMale(v: Voice): Boolean? {
        val n = v.name.lowercase()
        return when {
            "female" in n || Regex("smtf\\d").containsMatchIn(n) -> false
            "male" in n || Regex("smtm\\d").containsMatchIn(n) -> true
            Regex("x-de[bg]").containsMatchIn(n) -> true
            Regex("x-(dea|nfh|def)").containsMatchIn(n) -> false
            else -> null
        }
    }

    /** Gewaehlte Stimme, sonst die erste vermutlich maennliche, sonst null (= Standardstimme). */
    fun pick(tts: TextToSpeech, preferredName: String?): Voice? {
        val voices = list(tts)
        return voices.firstOrNull { it.name == preferredName }
            ?: voices.firstOrNull { isMale(it) == true }
    }

    fun label(v: Voice, index: Int): String {
        val gender = when (isMale(v)) {
            true -> "männlich"
            false -> "weiblich"
            null -> "?"
        }
        return "Stimme ${index + 1} ($gender) · ${v.locale.country}"
    }
}
