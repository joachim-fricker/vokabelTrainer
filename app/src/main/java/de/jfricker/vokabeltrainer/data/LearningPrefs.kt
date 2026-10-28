package de.jfricker.vokabeltrainer.data

import android.content.Context
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

    /** Bis wann alle Woerter der aktuellen Sprache einmal gelernt sein sollen (z. B. Reisebeginn). */
    var targetDate: LocalDate
        get() = LocalDate.ofEpochDay(prefs.getLong(key(KEY_TARGET), DEFAULT_TARGET.toEpochDay()))
        set(value) {
            prefs.edit().putLong(key(KEY_TARGET), value.toEpochDay()).apply()
        }

    /** Schluessel fuer die YouTube Data API (nur lokal auf dem Handy gespeichert). */
    var youtubeApiKey: String?
        get() = prefs.getString(KEY_YOUTUBE, null)
        set(value) {
            prefs.edit().putString(KEY_YOUTUBE, value).apply()
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
        val DEFAULT_TARGET: LocalDate = LocalDate.of(2026, 12, 1)
        const val NEW_WORDS = "newWords"
        const val NEW_SENTENCES = "newSentences"
        private const val KEY_TARGET = "targetDate"
        private const val KEY_YOUTUBE = "youtubeApiKey"
        private const val KEY_LANGUAGE = "language"
    }
}
