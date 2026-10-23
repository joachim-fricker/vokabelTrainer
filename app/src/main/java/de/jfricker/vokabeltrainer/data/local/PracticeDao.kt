package de.jfricker.vokabeltrainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** lessonId = null bedeutet: alle Lektionen. */
@Dao
interface PracticeDao {

    @Query("SELECT title FROM lesson WHERE id = :id")
    suspend fun lessonTitle(id: String): String?

    @Query(
        """
        SELECT w.* FROM word w
        JOIN progress p ON p.wordKey = w.wordKey AND p.direction = :direction
        WHERE (:lessonId IS NULL OR w.lessonId = :lessonId) AND p.dueAt <= :now
        ORDER BY p.dueAt
        LIMIT :limit
        """
    )
    suspend fun dueWords(lessonId: String?, direction: String, now: Long, limit: Int): List<WordEntity>

    @Query(
        """
        SELECT COUNT(*) FROM word w
        JOIN progress p ON p.wordKey = w.wordKey AND p.direction = :direction
        WHERE (:lessonId IS NULL OR w.lessonId = :lessonId) AND p.dueAt <= :now
        """
    )
    suspend fun dueCount(lessonId: String?, direction: String, now: Long): Int

    @Query(
        """
        SELECT w.* FROM word w
        JOIN lesson l ON l.id = w.lessonId
        WHERE (:lessonId IS NULL OR w.lessonId = :lessonId)
          AND NOT EXISTS (SELECT 1 FROM progress p WHERE p.wordKey = w.wordKey AND p.direction = :direction)
        ORDER BY l.sortOrder, w.sortOrder
        LIMIT :limit
        """
    )
    suspend fun newWords(lessonId: String?, direction: String, limit: Int): List<WordEntity>

    @Query(
        """
        SELECT COUNT(*) FROM word w
        WHERE (:lessonId IS NULL OR w.lessonId = :lessonId)
          AND NOT EXISTS (SELECT 1 FROM progress p WHERE p.wordKey = w.wordKey AND p.direction = :direction)
        """
    )
    suspend fun newCount(lessonId: String?, direction: String): Int

    @Query("SELECT * FROM progress WHERE wordKey = :wordKey AND direction = :direction")
    suspend fun progress(wordKey: String, direction: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ProgressEntity)

    /** Ein zufaelliger Beispielsatz, in dem das Wort vorkommt. */
    @Query(
        """
        SELECT s.* FROM sentence s
        JOIN sentence_word sw ON sw.sentenceKey = s.sentenceKey
        WHERE sw.wordKey = :wordKey
        ORDER BY RANDOM()
        LIMIT 1
        """
    )
    suspend fun exampleSentence(wordKey: String): SentenceEntity?

    // ------------------------------------------------ fuer Satz-Uebungen

    @Query(
        """
        SELECT s.* FROM sentence s
        JOIN lesson l ON l.id = s.lessonId
        WHERE (:lessonId IS NULL OR s.lessonId = :lessonId)
        ORDER BY l.sortOrder, s.sortOrder
        """
    )
    suspend fun sentences(lessonId: String?): List<SentenceEntity>

    @Query("SELECT * FROM sentence")
    suspend fun allSentences(): List<SentenceEntity>

    @Query("SELECT * FROM word")
    suspend fun allWords(): List<WordEntity>

    @Query("SELECT * FROM sentence_word WHERE (:lessonId IS NULL OR lessonId = :lessonId)")
    suspend fun sentenceWords(lessonId: String?): List<SentenceWordEntity>

    /** Lernstand aller Eintraege einer Uebungsart (bei Satz-Uebungen ist wordKey der sentenceKey). */
    @Query("SELECT * FROM progress WHERE direction = :mode")
    suspend fun progressForMode(mode: String): List<ProgressEntity>
}
