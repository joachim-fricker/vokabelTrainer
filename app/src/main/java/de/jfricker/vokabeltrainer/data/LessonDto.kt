package de.jfricker.vokabeltrainer.data

import kotlinx.serialization.Serializable

/** Aufbau einer Lektions-Datei in assets/lessons/<sprache>/NN_name.json */
@Serializable
data class LessonDto(
    val id: String,
    val language: String,
    val order: Int,
    val title: String,
    val titleTarget: String? = null,
    val words: List<WordDto> = emptyList(),
    val sentences: List<SentenceDto> = emptyList(),
    val grammar: List<GrammarDto> = emptyList(),
)

/** Wort-IDs gelten pro Sprache global: ein Wort wird nur in einer Lektion definiert,
 *  Saetze spaeterer Lektionen duerfen aber darauf verweisen. */
@Serializable
data class WordDto(
    val id: String,
    val text: String,
    val de: String,
    val pos: String? = null,
    val gender: String? = null,
    val note: String? = null,
    val grammar: List<String> = emptyList(),
    /** Gruppe innerhalb der Lektion, z. B. "Getränke" */
    val category: String? = null,
)

@Serializable
data class SentenceDto(
    val id: String,
    val text: String,
    val de: String,
    val words: List<String> = emptyList(),
    val grammar: List<String> = emptyList(),
)

@Serializable
data class GrammarDto(
    val id: String,
    val title: String,
    val explanation: String,
    val examples: List<ExampleDto> = emptyList(),
)

@Serializable
data class ExampleDto(
    val text: String,
    val de: String,
)
