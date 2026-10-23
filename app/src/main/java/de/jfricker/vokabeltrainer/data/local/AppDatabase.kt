package de.jfricker.vokabeltrainer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LessonEntity::class,
        WordEntity::class,
        SentenceEntity::class,
        SentenceWordEntity::class,
        GrammarEntity::class,
        ProgressEntity::class,
        ReviewLogEntity::class,
        PlaylistEntity::class,
        VideoEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun practiceDao(): PracticeDao
    abstract fun videoDao(): VideoDao
}

/** v3: Wortgruppen innerhalb einer Lektion. Lektionen neu importieren, damit die Gruppen gefuellt werden. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE word ADD COLUMN category TEXT")
        db.execSQL("UPDATE lesson SET contentHash = ''")
    }
}

/** v4: Protokoll aller Antworten fuer die Statistik. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `review_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemKey` TEXT NOT NULL, " +
                "`mode` TEXT NOT NULL, `correct` INTEGER NOT NULL, `reviewedAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_log_reviewedAt` ON `review_log` (`reviewedAt`)")
    }
}

/** v5: YouTube-Playlists und Videos mit Status (neu / gesehen / ausgeblendet). */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `video_playlist` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`channelTitle` TEXT, `addedAt` INTEGER NOT NULL, `lastSyncAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `video` (`videoId` TEXT NOT NULL, `playlistId` TEXT NOT NULL, " +
                "`title` TEXT NOT NULL, `channelTitle` TEXT, `thumbnailUrl` TEXT, `position` INTEGER NOT NULL, " +
                "`status` TEXT NOT NULL, `statusChangedAt` INTEGER, PRIMARY KEY(`videoId`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_video_playlistId` ON `video` (`playlistId`)")
    }
}
