package de.jfricker.vokabeltrainer

import android.app.Application
import androidx.room.Room
import de.jfricker.vokabeltrainer.data.LessonRepository
import de.jfricker.vokabeltrainer.data.PracticeRepository
import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.MIGRATION_2_3
import de.jfricker.vokabeltrainer.data.local.MIGRATION_3_4
import de.jfricker.vokabeltrainer.data.local.MIGRATION_4_5
import de.jfricker.vokabeltrainer.data.local.MIGRATION_5_6
import de.jfricker.vokabeltrainer.video.VideoRepository
import de.jfricker.vokabeltrainer.data.LearningPrefs
import de.jfricker.vokabeltrainer.data.StatsRepository
import de.jfricker.vokabeltrainer.speech.Speaker

class VokabelApp : Application() {
    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "vokabeln.db")
            // Ab Version 2 steckt Lernfortschritt in der DB: jede Schemaaenderung braucht eine Migration
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
    }
    val repository by lazy { LessonRepository(this, database) }
    val prefs by lazy { LearningPrefs(this) }
    val practiceRepository by lazy { PracticeRepository(database, prefs) }
    val statsRepository by lazy { StatsRepository(database, prefs) }
    val videoRepository by lazy { VideoRepository(database, prefs) }
    val speaker by lazy { Speaker(this, prefs.language) }
}
