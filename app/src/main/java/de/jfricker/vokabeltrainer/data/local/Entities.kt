package de.jfricker.vokabeltrainer.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lesson")
data class LessonEntity(
    @PrimaryKey val id: String,
    val language: String,
    val sortOrder: Int,
    val title: String,
    val titleTarget: String?,
    /** SHA-1 der JSON-Datei; aendert sich die Datei, wird die Lektion neu importiert. */
    val contentHash: String,
)

/** wordKey = "<sprache>:<id>", z. B. "pt-BR:voce" */
@Entity(tableName = "word", indices = [Index("lessonId")])
data class WordEntity(
    @PrimaryKey val wordKey: String,
    val id: String,
    val language: String,
    val lessonId: String,
    val sortOrder: Int,
    val text: String,
    val de: String,
    val pos: String?,
    val gender: String?,
    val note: String?,
    /** Komma-getrennte Grammatik-IDs */
    val grammarIds: String,
    /** Gruppe innerhalb der Lektion, z. B. "Getränke" (seit DB-Version 3) */
    val category: String? = null,
)

/** sentenceKey = "<lektion>:<id>", z. B. "pt-BR-01:s05" */
@Entity(tableName = "sentence", indices = [Index("lessonId")])
data class SentenceEntity(
    @PrimaryKey val sentenceKey: String,
    val id: String,
    val language: String,
    val lessonId: String,
    val sortOrder: Int,
    val text: String,
    val de: String,
    val grammarIds: String,
)

/** Welche Woerter kommen in welchem Satz vor (fuer Lueckentext & Co.). */
@Entity(
    tableName = "sentence_word",
    primaryKeys = ["sentenceKey", "wordKey"],
    indices = [Index("wordKey"), Index("lessonId")],
)
data class SentenceWordEntity(
    val sentenceKey: String,
    val wordKey: String,
    val lessonId: String,
)

/** grammarKey = "<sprache>:<id>" */
@Entity(tableName = "grammar", indices = [Index("lessonId")])
data class GrammarEntity(
    @PrimaryKey val grammarKey: String,
    val id: String,
    val language: String,
    val lessonId: String,
    val sortOrder: Int,
    val title: String,
    val explanation: String,
    /** Beispiele als JSON-Liste von ExampleDto */
    val examplesJson: String,
)

/** Lektion mit Anzahl der Inhalte fuer die Uebersicht. */
data class LessonSummary(
    @Embedded val lesson: LessonEntity,
    val wordCount: Int,
    val sentenceCount: Int,
    val grammarCount: Int,
    /** Woerter dieser Lektion, die schon mindestens einmal abgefragt wurden (egal welche Richtung). */
    val practicedCount: Int,
)

/** Lernstand einer Aufgabe (Leitner-Karteikasten): Wort je Richtung oder Satz je Uebungsart. */
@Entity(tableName = "progress", primaryKeys = ["wordKey", "direction"])
data class ProgressEntity(
    /** wordKey bei Karteikarten, sentenceKey bei Satz-Uebungen */
    val wordKey: String,
    /** PracticeMode.code, z. B. "pt-de" oder "cloze" */
    val direction: String,
    /** Fach 1..MAX_BOX; je hoeher, desto laenger die Pause bis zur naechsten Abfrage */
    val box: Int,
    /** Ab wann das Wort wieder faellig ist (Epoch-Millisekunden) */
    val dueAt: Long,
    val lastReviewedAt: Long,
    val correctCount: Int,
    val wrongCount: Int,
)
