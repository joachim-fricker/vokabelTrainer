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
import de.jfricker.vokabeltrainer.ui.StatsScreen
import de.jfricker.vokabeltrainer.ui.theme.VokabelTrainerTheme
import de.jfricker.vokabeltrainer.video.PlaylistScreen
import de.jfricker.vokabeltrainer.video.VideoPlayerScreen
import de.jfricker.vokabeltrainer.video.VideoViewModel
import de.jfricker.vokabeltrainer.video.VideosScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as VokabelApp
                MainViewModel(app.repository, app.practiceRepository, app.statsRepository, app.speaker)
            }
        }
    }

    private val videoViewModel: VideoViewModel by viewModels {
        viewModelFactory {
            initializer { VideoViewModel((application as VokabelApp).videoRepository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VokabelTrainerTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppContent(viewModel, videoViewModel)
                }
            }
        }
    }
}

/** Platzhalter beim Ueben: alle Lektionen bzw. gemischte Tagesrunde */
private const val ALL_LESSONS = "*"
private const val TODAY = "today"

@Composable
fun AppContent(vm: MainViewModel, videoVm: VideoViewModel) {
    var selectedLesson by rememberSaveable { mutableStateOf<String?>(null) }
    var practiceTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    var showVideos by rememberSaveable { mutableStateOf(false) }
    var openPlaylist by rememberSaveable { mutableStateOf<String?>(null) }
    var playingVideo by rememberSaveable { mutableStateOf<String?>(null) }
    val playlistId = openPlaylist
    val videoId = playingVideo
    val lessonId = selectedLesson
    val target = practiceTarget

    when {
        target != null -> PracticeScreen(
            vm,
            lessonId = target.takeUnless { it == ALL_LESSONS || it == TODAY },
            today = target == TODAY,
            onBack = { practiceTarget = null },
        )

        videoId != null -> {
            BackHandler { playingVideo = null }
            VideoPlayerScreen(videoVm, videoId, onBack = { playingVideo = null }, onPlay = { playingVideo = it })
        }

        playlistId != null -> {
            BackHandler { openPlaylist = null }
            PlaylistScreen(videoVm, playlistId, onBack = { openPlaylist = null }, onPlay = { playingVideo = it })
        }

        showVideos -> {
            BackHandler { showVideos = false }
            VideosScreen(videoVm, onBack = { showVideos = false }, onOpenPlaylist = { openPlaylist = it })
        }

        showStats -> {
            BackHandler { showStats = false }
            StatsScreen(vm, onBack = { showStats = false })
        }

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
            onToday = { practiceTarget = TODAY },
            onStats = { showStats = true },
            onVideos = { showVideos = true },
        )
    }
}
