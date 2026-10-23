package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.data.TodaySummary
import de.jfricker.vokabeltrainer.speech.SpeakerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    vm: MainViewModel,
    onOpen: (String) -> Unit,
    onPracticeAll: () -> Unit,
    onToday: () -> Unit,
    onStats: () -> Unit,
    onVideos: () -> Unit,
) {
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val speakerState by vm.speakerState.collectAsStateWithLifecycle()
    val importMessage by vm.importMessage.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()

    // Bei jedem Anzeigen neu berechnen (z. B. nach einer Uebungsrunde); lessons.size: nach dem Import
    LaunchedEffect(lessons.size) { vm.refreshToday() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vokabeltrainer") },
                actions = {
                    TextButton(onClick = onVideos) { Text("🎬 Videos") }
                    TextButton(onClick = onStats) { Text("📊 Statistik") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            (speakerState as? SpeakerState.Unavailable)?.let { s ->
                item { InfoCard(s.message) }
            }
            importMessage?.let { msg ->
                item { InfoCard(msg) }
            }
            if (lessons.isEmpty()) {
                item { Text("Lektionen werden geladen …") }
            } else {
                item { TodayCard(today, onToday) }
                item {
                    OutlinedButton(onClick = onPracticeAll, modifier = Modifier.fillMaxWidth()) {
                        Text("🃏  Alle Lektionen gezielt üben")
                    }
                }
            }
            items(lessons, key = { it.lesson.id }) { summary ->
                val lesson = summary.lesson
                Card(onClick = { onOpen(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Lektion ${lesson.sortOrder}", style = MaterialTheme.typography.labelMedium)
                        Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                        lesson.titleTarget?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        }
                        Text(
                            "${summary.wordCount} Wörter · ${summary.sentenceCount} Sätze · " +
                                "${summary.grammarCount} Grammatik · ${summary.practicedCount} geübt",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun TodayCard(summary: TodaySummary?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("☀️  Heute lernen", style = MaterialTheme.typography.titleLarge)
            if (summary == null) {
                Text("…", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    "${summary.due} fällig · neue Wörter ${summary.newWordsDone} von ${summary.newWordsTarget}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (summary.goalReached) {
                    Text("Tagesziel erreicht ✅", style = MaterialTheme.typography.bodyMedium)
                }
                val left = summary.daysLeft
                Text(
                    when {
                        left > 1 -> "Noch $left Tage bis zum Ziel · ${summary.wordsStarted} von ${summary.wordsTotal} Wörtern begonnen"
                        left == 1L -> "Morgen ist es so weit! · ${summary.wordsStarted} von ${summary.wordsTotal} Wörtern begonnen"
                        else -> "${summary.wordsStarted} von ${summary.wordsTotal} Wörtern begonnen"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
