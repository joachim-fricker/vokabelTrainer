package de.jfricker.vokabeltrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.jfricker.vokabeltrainer.ui.LessonListScreen
import de.jfricker.vokabeltrainer.ui.LessonScreen
import de.jfricker.vokabeltrainer.ui.MainViewModel
import de.jfricker.vokabeltrainer.ui.PracticeScreen
import de.jfricker.vokabeltrainer.ui.theme.VokabelTrainerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as VokabelApp
                MainViewModel(app.repository, app.practiceRepository, app.speaker)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VokabelTrainerTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppContent(viewModel)
                }
            }
        }
    }
}

/** Platzhalter fuer "alle Lektionen" beim Ueben */
private const val ALL_LESSONS = "*"

@Composable
fun AppContent(vm: MainViewModel) {
    var selectedLesson by rememberSaveable { mutableStateOf<String?>(null) }
    var practiceTarget by rememberSaveable { mutableStateOf<String?>(null) }
    val lessonId = selectedLesson
    val target = practiceTarget

    when {
        target != null -> PracticeScreen(
            vm,
            lessonId = target.takeUnless { it == ALL_LESSONS },
            onBack = { practiceTarget = null },
        )

        lessonId != null -> {
            BackHandler { selectedLesson = null }
            LessonScreen(
                vm, lessonId,
                onBack = { selectedLesson = null },
                onPractice = { practiceTarget = lessonId },
            )
        }

        else -> LessonListScreen(
            vm,
            onOpen = { selectedLesson = it },
            onPracticeAll = { practiceTarget = ALL_LESSONS },
        )
    }
}
