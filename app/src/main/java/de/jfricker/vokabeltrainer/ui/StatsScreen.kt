package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.ui.text.font.FontStyle
import de.jfricker.vokabeltrainer.data.DayCount
import de.jfricker.vokabeltrainer.data.DifficultWord
import de.jfricker.vokabeltrainer.data.Stats
import de.jfricker.vokabeltrainer.domain.DailyPlan
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_DAY = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: MainViewModel, onBack: () -> Unit, onPracticeDifficult: () -> Unit) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val difficult by vm.difficult.collectAsStateWithLifecycle()
    var showAllDifficult by rememberSaveable { mutableStateOf(false) }
    val language by vm.language.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik · ${language.displayName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        val s = stats
        if (s == null) {
            Text("Wird berechnet …", Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { GoalCard(s, onChange = vm::setNewWordsPerDay) }
            item { TodayStatsCard(s) }
            item { ActivityCard(s.last14Days) }
            item {
                DifficultHeader(
                    difficult,
                    onPractice = onPracticeDifficult,
                )
            }
            val shown = if (showAllDifficult) difficult else difficult.take(DIFFICULT_PREVIEW)
            items(shown, key = { "difficult-" + it.word.wordKey }) { d ->
                DifficultRow(d, onSpeak = { vm.speak(d.word.text) })
            }
            if (difficult.size > DIFFICULT_PREVIEW) {
                item {
                    TextButton(onClick = { showAllDifficult = !showAllDifficult }) {
                        Text(if (showAllDifficult) "Weniger zeigen" else "Alle ${difficult.size} zeigen")
                    }
                }
            }
            item {
                Text(
                    "Lektionen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(s.lessons, key = { it.lessonId }) { l ->
                StatCard {
                    Text("${l.sortOrder}. ${l.title}", style = MaterialTheme.typography.titleSmall)
                    LinearProgressIndicator(
                        progress = { if (l.total == 0) 0f else l.learned.toFloat() / l.total },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    )
                    Text(
                        "${l.learned} sicher · ${l.started} begonnen · ${l.total} Wörter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(
                    "„Sicher“ heisst: ${language.displayName} → Deutsch mindestens in Fach 3, " +
                        "also an mehreren Tagen hintereinander gewusst.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val DIFFICULT_PREVIEW = 15

@Composable
private fun DifficultHeader(words: List<DifficultWord>, onPractice: () -> Unit) {
    val open = words.count { !it.settled }
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Schwierige Wörter", style = MaterialTheme.typography.titleMedium)
        Text(
            if (words.isEmpty()) "Bisher hast du bei den Karteikarten noch kein Wort falsch gehabt. 👍"
            else "Wörter, die du bei den Karteikarten mindestens einmal nicht gewusst hast – " +
                "die häufigsten Fehler oben. Antippen spielt das Wort ab.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (words.isNotEmpty()) {
            Button(onClick = onPractice, modifier = Modifier.fillMaxWidth()) {
                Text(if (open > 0) "🃏  Diese Wörter üben ($open offen)" else "🃏  Nochmals alle üben")
            }
        }
    }
}

@Composable
private fun DifficultRow(d: DifficultWord, onSpeak: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onSpeak)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(displayText(d.word), style = MaterialTheme.typography.titleMedium)
                Text(d.word.de, style = MaterialTheme.typography.bodyMedium)
                d.word.note?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${d.wrong}× falsch",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (d.settled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
                if (d.settled) {
                    Text("sitzt inzwischen ✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun BigNumber(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GoalCard(s: Stats, onChange: (Int) -> Unit) {
    StatCard {
        Text("Tagesziel", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BigNumber("${s.newWordsGoal}", "neue Wörter pro Tag", Modifier.weight(1f))
            OutlinedButton(onClick = { onChange(s.newWordsGoal - 1) }, enabled = s.newWordsGoal > 0) { Text("−") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { onChange(s.newWordsGoal + 1) }, enabled = s.newWordsGoal < DailyPlan.MAX_NEW_PER_DAY) { Text("+") }
        }
        LinearProgressIndicator(
            progress = { if (s.wordsTotal == 0) 0f else s.wordsStarted.toFloat() / s.wordsTotal },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        )
        Text(
            "${s.wordsStarted} von ${s.wordsTotal} Wörtern begonnen · ${s.wordsLearned} sicher",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TodayStatsCard(s: Stats) {
    StatCard {
        Text("Heute", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            BigNumber("🔥 ${s.streak}", if (s.streak == 1) "Tag in Folge" else "Tage in Folge", Modifier.weight(1f))
            BigNumber("${s.reviewsToday}", "Aufgaben gelöst", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            BigNumber(s.correctPercentToday?.let { "$it %" } ?: "–", "richtig", Modifier.weight(1f))
            BigNumber("${s.newWordsToday} / ${s.newWordsPerDay}", "neue Wörter", Modifier.weight(1f))
        }
        Text(
            "Bis morgen fällig: ${s.dueUntilTomorrow} Aufgaben",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/** Gelöste Aufgaben der letzten 14 Tage; ein Balken pro Tag, antippen zeigt den Wert. */
@Composable
private fun ActivityCard(days: List<DayCount>) {
    var selected by remember(days) { mutableStateOf(days.lastIndex) }
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary

    StatCard {
        Text("Letzte 14 Tage", style = MaterialTheme.typography.titleMedium)
        days.getOrNull(selected)?.let { d ->
            Text(
                "${d.date.format(DATE_DAY)}: ${d.count} Aufgaben",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEachIndexed { i, d ->
                // Ganze Spaltenhoehe ist Tippflaeche, damit auch kleine Balken leicht zu treffen sind
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selected = i },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (d.count > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight((d.count.toFloat() / max).coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (i == selected) barColor else barColor.copy(alpha = 0.45f)),
                        )
                    }
                }
            }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            days.forEachIndexed { i, d ->
                Text(
                    // Nur jeden zweiten Tag beschriften, damit nichts ueberlappt
                    if (i % 2 == days.lastIndex % 2) "${d.date.dayOfMonth}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
