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

    // ------------------------------------------------ "Heute lernen" und Statistik
    // 'pt-de' / 'de-pt' entsprechen PracticeMode.CARD_PT_DE.code / CARD_DE_PT.code

    @Query("SELECT * FROM progress WHERE dueAt <= :now AND direction IN (:modes) ORDER BY dueAt LIMIT :limit")
    suspend fun dueAll(now: Long, modes: List<String>, limit: Int): List<ProgressEntity>

    @Query("SELECT COUNT(*) FROM progress WHERE dueAt <= :now AND direction IN (:modes)")
    suspend fun dueAllCount(now: Long, modes: List<String>): Int

    @Query("SELECT COUNT(*) FROM progress WHERE dueAt < :before AND direction IN (:modes)")
    suspend fun dueBeforeCount(before: Long, modes: List<String>): Int

    @Query("SELECT * FROM word WHERE wordKey = :key")
    suspend fun word(key: String): WordEntity?

    @Query("SELECT COUNT(*) FROM word")
    suspend fun wordCount(): Int

    @Query("SELECT COUNT(*) FROM progress WHERE direction = :mode")
    suspend fun startedCount(mode: String): Int

    @Query("SELECT COUNT(*) FROM progress WHERE direction = :mode AND box >= :minBox")
    suspend fun boxAtLeastCount(mode: String, minBox: Int): Int

    @Query("SELECT wordKey FROM progress WHERE direction = 'pt-de'")
    suspend fun startedWordKeys(): List<String>

    /** Woerter, die in Richtung PT->DE schon sitzen, aber noch nie DE->PT abgefragt wurden. */
    @Query(
        """
        SELECT w.* FROM word w
        JOIN lesson l ON l.id = w.lessonId
        JOIN progress p ON p.wordKey = w.wordKey AND p.direction = 'pt-de'
        WHERE p.box >= :minBox
          AND NOT EXISTS (SELECT 1 FROM progress q WHERE q.wordKey = w.wordKey AND q.direction = 'de-pt')
        ORDER BY l.sortOrder, w.sortOrder
        LIMIT :limit
        """
    )
    suspend fun reverseCandidates(minBox: Int, limit: Int): List<WordEntity>

    @Insert
    suspend fun insertReview(review: ReviewLogEntity)

    @Query("SELECT reviewedAt, correct FROM review_log WHERE reviewedAt >= :since")
    suspend fun reviewsSince(since: Long): List<ReviewRow>

    @Query(
        """
        SELECT l.id AS lessonId, l.title AS title, l.sortOrder AS sortOrder,
            COUNT(w.wordKey) AS total,
            COUNT(p.wordKey) AS started,
            SUM(CASE WHEN p.box >= :minBox THEN 1 ELSE 0 END) AS learned
        FROM lesson l
        JOIN word w ON w.lessonId = l.id
        LEFT JOIN progress p ON p.wordKey = w.wordKey AND p.direction = 'pt-de'
        GROUP BY l.id
        ORDER BY l.sortOrder
        """
    )
    suspend fun lessonStats(minBox: Int): List<LessonStat>
}
