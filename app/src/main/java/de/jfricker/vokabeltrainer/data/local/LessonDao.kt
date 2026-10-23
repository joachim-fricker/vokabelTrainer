package de.jfricker.vokabeltrainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT contentHash FROM lesson WHERE id = :id")
    suspend fun contentHash(id: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentences(sentences: List<SentenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentenceWords(refs: List<SentenceWordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammar(grammar: List<GrammarEntity>)

    @Query("DELETE FROM word WHERE lessonId = :lessonId")
    suspend fun deleteWords(lessonId: String)

    @Query("DELETE FROM sentence WHERE lessonId = :lessonId")
    suspend fun deleteSentences(lessonId: String)

    @Query("DELETE FROM sentence_word WHERE lessonId = :lessonId")
    suspend fun deleteSentenceWords(lessonId: String)

    @Query("DELETE FROM grammar WHERE lessonId = :lessonId")
    suspend fun deleteGrammar(lessonId: String)

    @Query(
        """
        SELECT lesson.*,
            (SELECT COUNT(*) FROM word WHERE word.lessonId = lesson.id) AS wordCount,
            (SELECT COUNT(*) FROM sentence WHERE sentence.lessonId = lesson.id) AS sentenceCount,
            (SELECT COUNT(*) FROM grammar WHERE grammar.lessonId = lesson.id) AS grammarCount,
            (SELECT COUNT(DISTINCT p.wordKey) FROM progress p JOIN word w ON w.wordKey = p.wordKey
                WHERE w.lessonId = lesson.id) AS practicedCount
        FROM lesson
        ORDER BY language, sortOrder
        """
    )
    fun observeLessons(): Flow<List<LessonSummary>>

    @Query("SELECT * FROM lesson WHERE id = :id")
    fun observeLesson(id: String): Flow<LessonEntity?>

    @Query("SELECT * FROM word WHERE lessonId = :lessonId ORDER BY sortOrder")
    fun observeWords(lessonId: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM sentence WHERE lessonId = :lessonId ORDER BY sortOrder")
    fun observeSentences(lessonId: String): Flow<List<SentenceEntity>>

    @Query("SELECT * FROM grammar WHERE lessonId = :lessonId ORDER BY sortOrder")
    fun observeGrammar(lessonId: String): Flow<List<GrammarEntity>>
}
