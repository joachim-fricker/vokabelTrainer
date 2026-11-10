package de.jfricker.vokabeltrainer.data

import android.content.Context
import de.jfricker.vokabeltrainer.domain.DailyPlan
import de.jfricker.vokabeltrainer.domain.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Kleine Einstellungen und Tageszaehler (SharedPreferences). */
class LearningPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("learning", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(Language.fromCode(prefs.getString(KEY_LANGUAGE, null)))

    /** Gerade gewaehlte Lernsprache; Lektionen, Tagesplan, Statistik und Videos gelten nur fuer sie. */
    val languageFlow: StateFlow<Language> = _language.asStateFlow()

    var language: Language
        get() = _language.value
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value.code).apply()
            _language.value = value
        }

    /** Schluessel je Sprache; Portugiesisch behaelt die alten Namen (bestehende Einstellungen). */
    private fun key(base: String): String =
        if (language == Language.PT_BR) base else "$base.${language.code}"

    /** Tagesziel: so viele neue Woerter pro Tag in der aktuellen Sprache. */
    var newWordsPerDay: Int
        get() = prefs.getInt(key(KEY_NEW_PER_DAY), DailyPlan.DEFAULT_NEW_PER_DAY)
        set(value) {
            prefs.edit().putInt(key(KEY_NEW_PER_DAY), value.coerceIn(0, DailyPlan.MAX_NEW_PER_DAY)).apply()
        }

    /** Schluessel fuer die YouTube Data API (nur lokal auf dem Handy gespeichert). */
    var youtubeApiKey: String?
        get() = prefs.getString(KEY_YOUTUBE, null)
        set(value) {
            prefs.edit().putString(KEY_YOUTUBE, value).apply()
        }

    /** Konjugation: Form eintippen (true) oder aus vier Moeglichkeiten auswaehlen. Gilt fuer alle Sprachen. */
    var conjTyping: Boolean
        get() = prefs.getBoolean(KEY_CONJ_TYPING, true)
        set(value) {
            prefs.edit().putBoolean(KEY_CONJ_TYPING, value).apply()
        }

    /** Wie viele neue Woerter/Saetze heute in der aktuellen Sprache schon dazugekommen sind. */
    fun countToday(kind: String): Int {
        val today = LocalDate.now().toEpochDay()
        val k = key(kind)
        return if (prefs.getLong("$k.day", -1L) == today) prefs.getInt("$k.count", 0) else 0
    }

    fun incrementToday(kind: String) {
        val today = LocalDate.now().toEpochDay()
        val count = countToday(kind) + 1
        val k = key(kind)
        prefs.edit().putLong("$k.day", today).putInt("$k.count", count).apply()
    }

    companion object {
        const val NEW_WORDS = "newWords"
        const val NEW_SENTENCES = "newSentences"
        const val NEW_CONJ = "newConj"
        const val DICTATIONS = "dictations"
        const val NUMBERS = "numbers"
        const val NEW_GRAMMAR = "newGrammar"
        private const val KEY_CONJ_TYPING = "conjTyping"
        private const val KEY_NEW_PER_DAY = "newWordsPerDay"
        private const val KEY_YOUTUBE = "youtubeApiKey"
        private const val KEY_LANGUAGE = "language"
    }
}
