package de.jfricker.vokabeltrainer.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.GrammarEntity
import de.jfricker.vokabeltrainer.data.local.LessonEntity
import de.jfricker.vokabeltrainer.data.local.LessonSummary
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.SentenceWordEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/** Grammatikregel fuer die Anzeige, Beispiele bereits ausgepackt. */
data class GrammarItem(
    val id: String,
    val title: String,
    val explanation: String,
    val examples: List<ExampleDto>,
)

data class ImportResult(val imported: Int, val unchanged: Int, val errors: List<String>)

class LessonRepository(private val context: Context, private val db: AppDatabase) {

    private val dao = db.lessonDao()
    private val json = Json { ignoreUnknownKeys = true }
    private val examplesSerializer = ListSerializer(ExampleDto.serializer())

    fun observeLessons(lang: String): Flow<List<LessonSummary>> = dao.observeLessons(lang)
    fun observeLesson(id: String): Flow<LessonEntity?> = dao.observeLesson(id)
    fun observeWords(lessonId: String): Flow<List<WordEntity>> = dao.observeWords(lessonId)
    fun observeSentences(lessonId: String): Flow<List<SentenceEntity>> = dao.observeSentences(lessonId)

    fun observeGrammar(lessonId: String): Flow<List<GrammarItem>> =
        dao.observeGrammar(lessonId).map { list ->
            list.map { g ->
                GrammarItem(g.id, g.title, g.explanation, json.decodeFromString(examplesSerializer, g.examplesJson))
            }
        }

    /**
     * Liest alle Lektionen aus assets/lessons/<sprache>/ *.json.
     * Nur neue oder geaenderte Dateien werden in die Datenbank geschrieben.
     */
    suspend fun importFromAssets(): ImportResult = withContext(Dispatchers.IO) {
        var imported = 0
        var unchanged = 0
        val errors = mutableListOf<String>()
        val assets = context.assets

        for (language in assets.list(LESSON_DIR).orEmpty().sorted()) {
            val files = assets.list("$LESSON_DIR/$language").orEmpty().filter { it.endsWith(".json") }.sorted()
            for (file in files) {
                try {
                    val content = assets.open("$LESSON_DIR/$language/$file")
                        .bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val hash = sha1(content)
                    val lesson = json.decodeFromString(LessonDto.serializer(), content)
                    if (dao.contentHash(lesson.id) == hash) {
                        unchanged++
                    } else {
                        db.withTransaction { store(lesson, hash) }
                        imported++
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Import von $file fehlgeschlagen", e)
                    errors += "$language/$file: ${e.message}"
                }
            }
        }
        Log.i(TAG, "Import: $imported neu/geaendert, $unchanged unveraendert, ${errors.size} Fehler")
        ImportResult(imported, unchanged, errors)
    }

    private suspend fun store(l: LessonDto, hash: String) {
        val lang = l.language
        dao.deleteSentenceWords(l.id)
        dao.deleteSentences(l.id)
        dao.deleteWords(l.id)
        dao.deleteGrammar(l.id)

        dao.insertLesson(LessonEntity(l.id, lang, l.order, l.title, l.titleTarget, hash))

        dao.insertWords(l.words.mapIndexed { i, w ->
            WordEntity(
                wordKey = wordKey(lang, w.id), id = w.id, language = lang, lessonId = l.id, sortOrder = i,
                text = w.text, de = w.de, pos = w.pos, gender = w.gender, note = w.note,
                grammarIds = w.grammar.joinToString(","),
                category = w.category,
            )
        })

        dao.insertSentences(l.sentences.mapIndexed { i, s ->
            SentenceEntity(
                sentenceKey = "${l.id}:${s.id}", id = s.id, language = lang, lessonId = l.id, sortOrder = i,
                text = s.text, de = s.de, grammarIds = s.grammar.joinToString(","),
            )
        })

        dao.insertSentenceWords(l.sentences.flatMap { s ->
            s.words.distinct().map { w -> SentenceWordEntity("${l.id}:${s.id}", wordKey(lang, w), l.id) }
        })

        dao.insertGrammar(l.grammar.mapIndexed { i, g ->
            GrammarEntity(
                grammarKey = "$lang:${g.id}", id = g.id, language = lang, lessonId = l.id, sortOrder = i,
                title = g.title, explanation = g.explanation,
                examplesJson = json.encodeToString(examplesSerializer, g.examples),
            )
        })
    }

    private fun wordKey(language: String, id: String) = "$language:$id"

    private fun sha1(text: String): String =
        MessageDigest.getInstance("SHA-1").digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        private const val TAG = "LessonRepository"
        private const val LESSON_DIR = "lessons"
    }
}
