package de.jfricker.vokabeltrainer.data

import de.jfricker.vokabeltrainer.data.local.VerbEntity
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.Tense
import de.jfricker.vokabeltrainer.domain.Verb
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Aufbau von assets/verbs/<sprache>.json (erzeugt mit tools/verbs/conj.py).
 * word = ID des Verbs in den Lektionen; forms: Zeit -> Formen in der Reihenfolge von Language.persons.
 */
@Serializable
data class VerbFileDto(
    val language: String,
    val verbs: List<VerbDto> = emptyList(),
)

@Serializable
data class VerbDto(
    val word: String,
    val forms: Map<String, List<String>>,
    val note: String? = null,
    /** eigene Formen oder Stammwechsel: alle Formen ueben */
    val irregular: Boolean = false,
)

object VerbJson {
    private val json = Json { ignoreUnknownKeys = true }
    private val formsSerializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))

    fun encodeForms(forms: Map<String, List<String>>): String = json.encodeToString(formsSerializer, forms)

    fun toVerb(e: VerbEntity): Verb {
        val raw = json.decodeFromString(formsSerializer, e.formsJson)
        val forms = raw.mapNotNull { (code, list) -> Tense.fromCode(code)?.let { it to list } }.toMap()
        return Verb(
            key = e.verbKey,
            id = e.id,
            language = Language.fromCode(e.language),
            lessonId = e.lessonId,
            infinitive = e.infinitive,
            de = e.de,
            note = e.note,
            forms = forms,
            irregular = e.irregular,
        )
    }
}
