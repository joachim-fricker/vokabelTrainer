package de.jfricker.vokabeltrainer.data

import android.content.Context
import android.util.Log
import de.jfricker.vokabeltrainer.domain.Dialogue
import de.jfricker.vokabeltrainer.domain.DialogueLine
import de.jfricker.vokabeltrainer.domain.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

/** Aufbau von assets/dialogues/<sprache>.json (erzeugt mit tools/dialogues/gen.py) */
@Serializable
data class DialogueFileDto(val language: String, val dialogues: List<DialogueDto> = emptyList())

@Serializable
data class DialogueDto(
    val id: String,
    val title: String,
    val titleTarget: String = "",
    val situation: String = "",
    val lesson: String? = null,
    val roles: List<String> = listOf("", "Du"),
    val lines: List<DialogueLineDto> = emptyList(),
)

@Serializable
data class DialogueLineDto(val role: Int, val text: String, val de: String, val note: String = "")

/** Wie oft "Rolle spielen" gemacht wurde und mit welchem Ergebnis zuletzt. */
data class DialogueProgress(val count: Int, val lastCorrect: Int, val lastTotal: Int, val lastDay: LocalDate)

/**
 * Dialoge werden wie die Grammatik-Uebungen nicht in die Datenbank kopiert, sondern aus den Assets gelesen.
 * Dialoge laufen ohne Karteikasten; gespeichert wird nur ein kleiner Stand in SharedPreferences "dialogues".
 */
class DialogueStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("dialogues", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<Language, List<Dialogue>>()
    private val mutex = Mutex()

    suspend fun get(language: Language): List<Dialogue> = mutex.withLock {
        cache.getOrPut(language) { load(language) }
    }

    private suspend fun load(language: Language): List<Dialogue> = withContext(Dispatchers.IO) {
        try {
            val content = appContext.assets.open("dialogues/${language.code}.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            json.decodeFromString(DialogueFileDto.serializer(), content).dialogues.map { d ->
                Dialogue(
                    key = "${language.code}:${d.id}", id = d.id, language = language, title = d.title,
                    titleTarget = d.titleTarget, situation = d.situation, lessonId = d.lesson, roles = d.roles,
                    lines = d.lines.map { DialogueLine(it.role, it.text, it.de, it.note) },
                )
            }
        } catch (e: Exception) {
            Log.e("DialogueStore", "Dialoge ${language.code} nicht lesbar", e)
            emptyList()
        }
    }

    fun progress(key: String): DialogueProgress? {
        val count = prefs.getInt("$key.count", 0)
        if (count == 0) return null
        return DialogueProgress(
            count = count,
            lastCorrect = prefs.getInt("$key.correct", 0),
            lastTotal = prefs.getInt("$key.total", 0),
            lastDay = LocalDate.ofEpochDay(prefs.getLong("$key.day", LocalDate.now().toEpochDay())),
        )
    }

    fun recordRolePlay(key: String, correct: Int, total: Int) {
        prefs.edit()
            .putInt("$key.count", prefs.getInt("$key.count", 0) + 1)
            .putInt("$key.correct", correct)
            .putInt("$key.total", total)
            .putLong("$key.day", LocalDate.now().toEpochDay())
            .apply()
    }
}
