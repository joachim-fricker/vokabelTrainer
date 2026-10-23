package de.jfricker.vokabeltrainer

import android.app.Application
import androidx.room.Room
import de.jfricker.vokabeltrainer.data.LessonRepository
import de.jfricker.vokabeltrainer.data.PracticeRepository
import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.MIGRATION_2_3
import de.jfricker.vokabeltrainer.speech.Speaker

class VokabelApp : Application() {
    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "vokabeln.db")
            // Ab Version 2 steckt Lernfortschritt in der DB: jede Schemaaenderung braucht eine Migration
            .addMigrations(MIGRATION_2_3)
            .build()
    }
    val repository by lazy { LessonRepository(this, database) }
    val practiceRepository by lazy { PracticeRepository(database) }
    val speaker by lazy { Speaker(this) }
}
