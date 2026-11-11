package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.data.TodaySummary
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.speech.SpeakerState
import de.jfricker.vokabeltrainer.speech.ListenService
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    vm: MainViewModel,
    onOpen: (String) -> Unit,
    onPracticeAll: () -> Unit,
    onToday: () -> Unit,
    onStats: () -> Unit,
    onVideos: () -> Unit,
    onDialogues: () -> Unit,
    onCompare: () -> Unit,
    onListen: () -> Unit,
) {
    val listen by ListenService.state.collectAsStateWithLifecycle()
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val speakerState by vm.speakerState.collectAsStateWithLifecycle()
    val importMessage by vm.importMessage.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()

    // Bei jedem Anzeigen neu berechnen (z. B. nach einer Uebungsrunde); lessons.size: nach dem Import
    LaunchedEffect(lessons.size, language) { vm.refreshToday() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vokabeltrainer") },
                actions = {
                    // Alle Menuepunkte oben in einem Menue (Wunsch des Nutzers, 11.11.2026);
                    // auf der Seite selbst nur noch "Heute lernen" und die Lektionen
                    MainMenu(
                        enabled = lessons.isNotEmpty(),
                        onPracticeAll = onPracticeAll,
                        onDialogues = onDialogues,
                        onCompare = onCompare,
                        onListen = onListen,
                        onVideos = onVideos,
                        onStats = onStats,
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LanguagePicker(language, vm::setLanguage) }
            (speakerState as? SpeakerState.Unavailable)?.let { s ->
                item { InfoCard(s.message) }
            }
            importMessage?.let { msg ->
                item { InfoCard(msg) }
            }
            if (lessons.isEmpty()) {
                item { Text("Noch keine Lektionen für ${language.displayName} …") }
            } else {
                if (listen.running) {
                    item { ListenRunningCard(onOpen = onListen) }
                }
                item { TodayCard(today, onToday) }
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

/** Menue oben rechts mit allen Bereichen ausser "Heute lernen" und den Lektionen */
@Composable
private fun MainMenu(
    enabled: Boolean,
    onPracticeAll: () -> Unit,
    onDialogues: () -> Unit,
    onCompare: () -> Unit,
    onListen: () -> Unit,
    onVideos: () -> Unit,
    onStats: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text("☰  Menü", style = MaterialTheme.typography.titleMedium) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            // Text, braucht Lektionen, Aktion
            val entries = listOf(
                Triple("🃏  Alle Lektionen gezielt üben", true, onPracticeAll),
                Triple("💬  Typische Dialoge", true, onDialogues),
                Triple("🔀  Portugiesisch ↔ Spanisch vergleichen", true, onCompare),
                Triple("🎧  Nur hören", true, onListen),
                Triple("🎬  Videos", false, onVideos),
                Triple("📊  Statistik", false, onStats),
            )
            entries.forEachIndexed { i, (label, needsLessons, action) ->
                if (i == 4) HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(label, style = MaterialTheme.typography.titleMedium) },
                    enabled = enabled || !needsLessons,
                    onClick = { open = false; action() },
                )
            }
        }
    }
}

/** Hinweis auf der Startseite, solange "Nur hören" im Hintergrund laeuft */
@Composable
private fun ListenRunningCard(onOpen: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🎧  Nur hören läuft", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { ListenService.send(context, ListenService.ACTION_STOP) }) { Text("⏹ Stopp") }
        }
    }
}

/** Umschalter zwischen den Lernsprachen */
@Composable
private fun LanguagePicker(selected: Language, onSelect: (Language) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Language.entries.forEach { l ->
            FilterChip(
                selected = l == selected,
                onClick = { onSelect(l) },
                label = { Text("${l.flag}  ${l.displayName}", style = MaterialTheme.typography.titleSmall) },
            )
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
                Text(
                    "${summary.wordsStarted} von ${summary.wordsTotal} Wörtern begonnen",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
