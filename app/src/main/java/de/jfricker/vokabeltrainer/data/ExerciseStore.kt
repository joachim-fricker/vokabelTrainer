package de.jfricker.vokabeltrainer.data

import android.content.Context
import android.util.Log
import de.jfricker.vokabeltrainer.domain.GrammarExercise
import de.jfricker.vokabeltrainer.domain.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Aufbau von assets/exercises/<sprache>.json */
@Serializable
data class ExerciseFileDto(
    val language: String,
    /** Regel je Uebungsart (type -> Text) */
    val rules: Map<String, String> = emptyMap(),
    val exercises: List<ExerciseDto> = emptyList(),
)

@Serializable
data class ExerciseDto(
    val id: String,
    val type: String,
    val lesson: String,
    val before: String,
    val after: String,
    val answer: String,
    val options: List<String>,
    val fixedOrder: Boolean = false,
    val hint: String = "",
    val de: String = "",
    val speak: String = "",
    val note: String = "",
    val words: List<String> = emptyList(),
    val typeable: Boolean = false,
)

class ExerciseSet(val exercises: List<GrammarExercise>, val rules: Map<String, String>) {
    val byKey: Map<String, GrammarExercise> = exercises.associateBy { it.key }
}

/**
 * Grammatik-Uebungen werden nicht in die Datenbank kopiert, sondern bei Bedarf aus den Assets gelesen
 * und im Speicher gehalten (nur der Lernstand liegt in progress).
 */
class ExerciseStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<Language, ExerciseSet>()
    private val mutex = Mutex()

    suspend fun get(language: Language): ExerciseSet = mutex.withLock {
        cache.getOrPut(language) { load(language) }
    }

    private suspend fun load(language: Language): ExerciseSet = withContext(Dispatchers.IO) {
        try {
            val content = context.assets.open("exercises/${language.code}.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val file = json.decodeFromString(ExerciseFileDto.serializer(), content)
            val list = file.exercises.mapIndexed { i, e ->
                GrammarExercise(
                    key = "${language.code}:${e.id}", type = e.type, language = language, lessonId = e.lesson,
                    sortOrder = i, before = e.before, after = e.after, answer = e.answer, options = e.options,
                    fixedOrder = e.fixedOrder, hint = e.hint, de = e.de, speak = e.speak.ifEmpty { e.before + e.answer + e.after },
                    note = e.note, words = e.words.map { "${language.code}:$it" }, typeable = e.typeable,
                )
            }
            ExerciseSet(list, file.rules)
        } catch (e: Exception) {
            Log.e("ExerciseStore", "Grammatik-Uebungen ${language.code} nicht lesbar", e)
            ExerciseSet(emptyList(), emptyMap())
        }
    }
}
