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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import de.jfricker.vokabeltrainer.data.DayCount
import de.jfricker.vokabeltrainer.data.Stats
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_LONG = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMAN)
private val DATE_DAY = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()
    var pickDate by rememberSaveable { mutableStateOf(false) }

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
            item { GoalCard(s, onChangeDate = { pickDate = true }) }
            item { TodayStatsCard(s) }
            item { ActivityCard(s.last14Days) }
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

    if (pickDate) {
        val initial = stats?.targetDate ?: LocalDate.now()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        // DatePicker liefert UTC-Mitternacht
                        vm.setTargetDate(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    pickDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text("Abbrechen") } },
        ) {
            DatePicker(state = state)
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
private fun GoalCard(s: Stats, onChangeDate: () -> Unit) {
    StatCard {
        Text("Ziel", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Alle Wörter bis ${s.targetDate.format(DATE_LONG)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onChangeDate) { Text("Ändern") }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            BigNumber(if (s.daysLeft > 0) "${s.daysLeft}" else "–", "Tage übrig", Modifier.weight(1f))
            BigNumber("${s.newWordsPerDay}", "neue Wörter pro Tag", Modifier.weight(1f))
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
