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
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun practiceDao(): PracticeDao
}

/** v3: Wortgruppen innerhalb einer Lektion. Lektionen neu importieren, damit die Gruppen gefuellt werden. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE word ADD COLUMN category TEXT")
        db.execSQL("UPDATE lesson SET contentHash = ''")
    }
}
