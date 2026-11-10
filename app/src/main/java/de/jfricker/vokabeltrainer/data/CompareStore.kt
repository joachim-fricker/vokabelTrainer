package de.jfricker.vokabeltrainer.data

import android.content.Context
import android.util.Log
import de.jfricker.vokabeltrainer.domain.CardProgress
import de.jfricker.vokabeltrainer.domain.FalseFriend
import de.jfricker.vokabeltrainer.domain.PairKind
import de.jfricker.vokabeltrainer.domain.SoundPattern
import de.jfricker.vokabeltrainer.domain.WordPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Aufbau von assets/compare/pairs.json (erzeugt mit tools/compare/gen.py) */
@Serializable
data class CompareFileDto(
    val pairs: List<PairDto> = emptyList(),
    val patterns: List<PatternDto> = emptyList(),
    val falseFriends: List<FalseFriendDto> = emptyList(),
)

@Serializable
data class PairDto(
    val pt: String,
    val ptText: String,
    val es: String,
    val esText: String,
    val de: String,
    val kind: String,
    val patterns: List<String> = emptyList(),
)

@Serializable
data class PatternDto(val id: String, val title: String, val explanation: String = "")

@Serializable
data class FalseFriendDto(val pt: String, val ptDe: String, val es: String, val esDe: String, val note: String = "")

class CompareData(
    val pairs: List<WordPair>,
    val patterns: List<SoundPattern>,
    val falseFriends: List<FalseFriend>,
) {
    val patternById: Map<String, SoundPattern> = patterns.associateBy { it.id }
}

/**
 * Sprachvergleich: Daten aus den Assets (nicht in der DB), Lernstand der Karten
 * in SharedPreferences "compare" (<key>.box / .due / .wrong).
 */
class CompareStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("compare", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private var cache: CompareData? = null
    private val mutex = Mutex()

    suspend fun get(): CompareData = mutex.withLock {
        cache ?: load().also { cache = it }
    }

    private suspend fun load(): CompareData = withContext(Dispatchers.IO) {
        try {
            val content = appContext.assets.open("compare/pairs.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val file = json.decodeFromString(CompareFileDto.serializer(), content)
            CompareData(
                pairs = file.pairs.map {
                    WordPair("p:${it.pt}|${it.es}", it.ptText, it.esText, it.de, PairKind.fromCode(it.kind), it.patterns)
                },
                patterns = file.patterns.map { SoundPattern(it.id, it.title, it.explanation) },
                falseFriends = file.falseFriends.map { FalseFriend("f:${it.pt}", it.pt, it.ptDe, it.es, it.esDe, it.note) },
            )
        } catch (e: Exception) {
            Log.e("CompareStore", "Sprachvergleich nicht lesbar", e)
            CompareData(emptyList(), emptyList(), emptyList())
        }
    }

    fun progress(key: String): CardProgress = CardProgress(
        box = prefs.getInt("$key.box", 0),
        due = prefs.getLong("$key.due", 0L),
        wrong = prefs.getInt("$key.wrong", 0),
    )

    fun save(key: String, p: CardProgress) {
        prefs.edit()
            .putInt("$key.box", p.box)
            .putLong("$key.due", p.due)
            .putInt("$key.wrong", p.wrong)
            .apply()
    }
}
