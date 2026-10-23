package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.data.GrammarItem
import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity

/** Etwas Luft am Ende der Listen */
private val FAB_SPACE = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(vm: MainViewModel, lessonId: String, onBack: () -> Unit, onPractice: () -> Unit) {
    val lesson by remember(lessonId) { vm.lesson(lessonId) }.collectAsStateWithLifecycle(null)
    val words by remember(lessonId) { vm.words(lessonId) }.collectAsStateWithLifecycle(emptyList())
    val sentences by remember(lessonId) { vm.sentences(lessonId) }.collectAsStateWithLifecycle(emptyList())
    val grammar by remember(lessonId) { vm.grammar(lessonId) }.collectAsStateWithLifecycle(emptyList())
    val slow by vm.slow.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Aufgeklappte Wortgruppen; hier gehalten, damit sie beim Wechsel der Reiter erhalten bleiben
    var expanded by rememberSaveable(lessonId) { mutableStateOf(arrayListOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
                actions = {
                    FilterChip(selected = slow, onClick = { vm.setSlow(!slow) }, label = { Text("Langsam") })
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(52.dp),
                ) { Text("🃏  Diese Lektion üben") }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val titles = listOf("Wörter (${words.size})", "Sätze (${sentences.size})", "Grammatik")
            PrimaryTabRow(selectedTabIndex = tab) {
                titles.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }
            when (tab) {
                0 -> WordList(
                    words,
                    expanded = expanded,
                    onToggle = { g -> expanded = ArrayList(if (g in expanded) expanded - g else expanded + g) },
                    onSetAll = { expanded = ArrayList(it) },
                    onSpeak = vm::speak,
                )
                1 -> SentenceList(sentences, vm::speak)
                else -> GrammarList(grammar, vm::speak)
            }
        }
    }
}

@Composable
private fun WordList(
    words: List<WordEntity>,
    expanded: List<String>,
    onToggle: (String) -> Unit,
    onSetAll: (List<String>) -> Unit,
    onSpeak: (String) -> Unit,
) {
    // Woerter nach Gruppe (Reihenfolge wie in der Lektion); ohne Gruppe -> "Weitere Wörter"
    val groups = words.groupBy { it.category ?: OTHER_GROUP }
    // Nur eine Gruppe (z. B. Lektion ohne Gruppen): einfach alles zeigen
    val flat = groups.size <= 1

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = FAB_SPACE)) {
        if (!flat) {
            item(key = "toggle-all") {
                val allOpen = groups.keys.all { it in expanded }
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onSetAll(if (allOpen) emptyList() else groups.keys.toList()) }) {
                        Text(if (allOpen) "Alle zuklappen" else "Alle aufklappen")
                    }
                }
            }
        }
        groups.forEach { (group, list) ->
            val open = flat || group in expanded
            if (!flat) {
                item(key = "group-$group") {
                    GroupHeader(group, list.size, open, onClick = { onToggle(group) })
                }
            }
            if (open) {
                items(list, key = { it.wordKey }) { w ->
                    SpeakRow(primary = displayText(w), secondary = w.de, note = w.note, onClick = { onSpeak(w.text) })
                    HorizontalDivider(Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

private const val OTHER_GROUP = "Weitere Wörter"

@Composable
private fun GroupHeader(name: String, count: Int, open: Boolean, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (open) "▾" else "▸", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(12.dp))
            Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                "$count",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SentenceList(sentences: List<SentenceEntity>, onSpeak: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = FAB_SPACE)) {
        items(sentences, key = { it.sentenceKey }) { s ->
            SpeakRow(primary = s.text, secondary = s.de, onClick = { onSpeak(s.text) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun GrammarList(rules: List<GrammarItem>, onSpeak: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = FAB_SPACE),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rules, key = { it.id }) { rule ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(rule.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        rule.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    )
                    rule.examples.forEach { ex ->
                        SpeakRow(primary = ex.text, secondary = ex.de, onClick = { onSpeak(ex.text) }, compact = true)
                    }
                }
            }
        }
    }
}

/** Wort mit Geschlecht, falls es nicht schon am Artikel erkennbar ist. */
fun displayText(w: WordEntity): String {
    val hasArticle = w.text.startsWith("o ") || w.text.startsWith("a ")
    return when {
        hasArticle -> w.text
        w.gender == "m" -> "${w.text}  (m)"
        w.gender == "f" -> "${w.text}  (f)"
        else -> w.text
    }
}

/** Eine Zeile mit portugiesischem Text; Antippen spielt ihn ab. */
@Composable
private fun SpeakRow(
    primary: String,
    secondary: String,
    onClick: () -> Unit,
    note: String? = null,
    compact: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = if (compact) 0.dp else 16.dp, vertical = if (compact) 6.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(primary, style = MaterialTheme.typography.titleMedium)
            Text(secondary, style = MaterialTheme.typography.bodyMedium)
            note?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SpeakButton(onClick = onClick)
    }
}
