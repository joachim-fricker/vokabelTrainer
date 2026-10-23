package de.jfricker.vokabeltrainer.data

import android.content.Context
import java.time.LocalDate

/** Kleine Einstellungen und Tageszaehler (SharedPreferences). */
class LearningPrefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("learning", Context.MODE_PRIVATE)

    /** Bis wann alle Woerter einmal gelernt sein sollen (z. B. Reisebeginn). */
    var targetDate: LocalDate
        get() = LocalDate.ofEpochDay(prefs.getLong(KEY_TARGET, DEFAULT_TARGET.toEpochDay()))
        set(value) {
            prefs.edit().putLong(KEY_TARGET, value.toEpochDay()).apply()
        }

    /** Schluessel fuer die YouTube Data API (nur lokal auf dem Handy gespeichert). */
    var youtubeApiKey: String?
        get() = prefs.getString(KEY_YOUTUBE, null)
        set(value) {
            prefs.edit().putString(KEY_YOUTUBE, value).apply()
        }

    /** Wie viele neue Woerter/Saetze heute schon dazugekommen sind. */
    fun countToday(kind: String): Int {
        val today = LocalDate.now().toEpochDay()
        return if (prefs.getLong("$kind.day", -1L) == today) prefs.getInt("$kind.count", 0) else 0
    }

    fun incrementToday(kind: String) {
        val today = LocalDate.now().toEpochDay()
        val count = countToday(kind) + 1
        prefs.edit().putLong("$kind.day", today).putInt("$kind.count", count).apply()
    }

    companion object {
        val DEFAULT_TARGET: LocalDate = LocalDate.of(2026, 12, 1)
        const val NEW_WORDS = "newWords"
        const val NEW_SENTENCES = "newSentences"
        private const val KEY_TARGET = "targetDate"
        private const val KEY_YOUTUBE = "youtubeApiKey"
    }
}
