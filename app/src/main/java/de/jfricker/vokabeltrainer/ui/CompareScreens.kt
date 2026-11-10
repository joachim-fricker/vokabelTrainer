package de.jfricker.vokabeltrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.data.CompareData
import de.jfricker.vokabeltrainer.domain.CompareCard
import de.jfricker.vokabeltrainer.domain.FalseFriend
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.PairKind
import de.jfricker.vokabeltrainer.domain.WordPair

/** Reiter: die drei Gruppen, dann falsche Freunde */
private const val TAB_FALSE_FRIENDS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(vm: CompareViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val practice by vm.practice.collectAsStateWithLifecycle()

    val p = practice
    if (p != null) {
        BackHandler { vm.endPractice() }
        ComparePracticeScreen(vm, p)
        return
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var pattern by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔀 Sprachen vergleichen") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        val d = data
        if (d == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("…") }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                val kinds = PairKind.entries
                PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                    kinds.forEachIndexed { i, k ->
                        Tab(
                            selected = tab == i,
                            onClick = { tab = i },
                            text = { Text("${k.title} (${d.pairs.count { it.kind == k }})") },
                        )
                    }
                    Tab(
                        selected = tab == TAB_FALSE_FRIENDS,
                        onClick = { tab = TAB_FALSE_FRIENDS },
                        text = { Text("Falsche Freunde (${d.falseFriends.size})") },
                    )
                }
                if (tab == TAB_FALSE_FRIENDS) {
                    FalseFriendsTab(vm, d)
                } else {
                    PairsTab(vm, d, kinds[tab], pattern, onPattern = { pattern = it })
                }
            }
        }
    }
}

@Composable
private fun PairsTab(vm: CompareViewModel, d: CompareData, kind: PairKind, pattern: String?, onPattern: (String?) -> Unit) {
    val version by vm.version.collectAsStateWithLifecycle()
    val all = remember(d, kind) { d.pairs.filter { it.kind == kind } }
    // Muster-Filter nur bei "Ähnlich"
    val selected = pattern.takeIf { kind == PairKind.SIMILAR && d.patternById.containsKey(it) }
    val shown = if (selected == null) all else all.filter { selected in it.patterns }
    val cards = remember(shown) { shown.map { CompareCard.of(it) } }
    val counts = remember(cards, version) { vm.counts(cards) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OverviewCard(
                description = kind.description,
                counts = counts,
                onPractice = { vm.startPractice(cards) },
            )
        }
        if (kind == PairKind.SIMILAR && d.patterns.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = selected == null, onClick = { onPattern(null) }, label = { Text("Alle") }) }
                    items(d.patterns, key = { it.id }) { pt ->
                        FilterChip(
                            selected = selected == pt.id,
                            onClick = { onPattern(if (selected == pt.id) null else pt.id) },
                            label = { Text(pt.title) },
                        )
                    }
                }
            }
            selected?.let { d.patternById[it] }?.let { pt ->
                item {
                    Text("💡 ${pt.explanation}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(shown, key = { it.key }) { pair ->
            PairRow(
                pair,
                patternTitles = if (kind == PairKind.SIMILAR) pair.patterns.mapNotNull { d.patternById[it]?.title } else emptyList(),
                sits = remember(pair.key, version) { vm.sits(pair.key) },
                onSpeak = vm::speak,
            )
        }
    }
}

@Composable
private fun FalseFriendsTab(vm: CompareViewModel, d: CompareData) {
    val version by vm.version.collectAsStateWithLifecycle()
    val cards = remember(d) { d.falseFriends.map { CompareCard.of(it) } }
    val counts = remember(cards, version) { vm.counts(cards) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OverviewCard(
                description = "Gleiche oder fast gleiche Wörter mit verschiedener Bedeutung – Vorsicht beim Wechseln der Sprache!",
                counts = counts,
                onPractice = { vm.startPractice(cards) },
            )
        }
        items(d.falseFriends, key = { it.key }) { f -> FalseFriendRow(f, onSpeak = vm::speak) }
    }
}

@Composable
private fun OverviewCard(description: String, counts: CompareCounts, onPractice: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(description, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { if (counts.total == 0) 0f else counts.sits.toFloat() / counts.total },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
            Text(
                "${counts.sits} von ${counts.total} sitzen · ${counts.due} fällig · ${counts.new} neu",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onPractice, enabled = counts.round > 0, modifier = Modifier.fillMaxWidth()) {
                Text(if (counts.round > 0) "🃏  Beide Wörter üben (${counts.round})" else "Heute nichts fällig ✓")
            }
        }
    }
}

/** Wortpaar: Deutsch oben, darunter Portugiesisch | Spanisch (antippen = vorlesen) */
@Composable
private fun PairRow(pair: WordPair, patternTitles: List<String>, sits: Boolean, onSpeak: (String, Language) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pair.de,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (sits) Text("✓", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                WordCell(Language.PT_BR, pair.ptText, Modifier.weight(1f)) { onSpeak(pair.ptText, Language.PT_BR) }
                Spacer(Modifier.width(8.dp))
                WordCell(Language.ES_AR, pair.esText, Modifier.weight(1f)) { onSpeak(pair.esText, Language.ES_AR) }
            }
            if (patternTitles.isNotEmpty()) {
                Text(
                    "Muster: " + patternTitles.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun WordCell(language: Language, text: String, modifier: Modifier, onClick: () -> Unit) {
    Text(
        "${language.flag} $text",
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.clickable(onClickLabel = "Vorlesen", onClick = onClick).padding(vertical = 6.dp),
    )
}

@Composable
private fun FalseFriendRow(f: FalseFriend, onSpeak: (String, Language) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f).clickable(onClickLabel = "Vorlesen") { onSpeak(f.pt, Language.PT_BR) }) {
                    Text("${Language.PT_BR.flag} ${f.pt}", style = MaterialTheme.typography.titleMedium)
                    Text(f.ptDe, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f).clickable(onClickLabel = "Vorlesen") { onSpeak(f.es, Language.ES_AR) }) {
                    Text("${Language.ES_AR.flag} ${f.es}", style = MaterialTheme.typography.titleMedium)
                    Text(f.esDe, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (f.note.isNotEmpty()) {
                Text("💡 ${f.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------- Uebung "Beide Woerter"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComparePracticeScreen(vm: CompareViewModel, state: ComparePractice) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state) {
                            is ComparePractice.Asking -> "Beide Wörter · ${state.done} / ${state.total}"
                            is ComparePractice.Finished -> "Beide Wörter"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { vm.endPractice() }) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state) {
                is ComparePractice.Asking -> {
                    LinearProgressIndicator(
                        progress = { if (state.total == 0) 0f else state.done.toFloat() / state.total },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AskingCard(vm, state)
                }
                is ComparePractice.Finished -> FinishedCard(vm, state)
            }
        }
    }
}

@Composable
private fun AskingCard(vm: CompareViewModel, s: ComparePractice.Asking) {
    val card = s.card
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(card.question, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(card.prompt, style = MaterialTheme.typography.headlineSmall)
            if (!s.revealed) {
                Text("Sag beide laut, dann aufdecken.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { vm.reveal() }, modifier = Modifier.fillMaxWidth()) { Text("Aufdecken") }
            } else {
                card.answers.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${a.language.flag}  ${a.text}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        SpeakButton(onClick = { vm.speak(a.speak, a.language) })
                    }
                }
                if (card.note.isNotEmpty()) Text("💡 ${card.note}", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.grade(false) }, modifier = Modifier.weight(1f)) {
                        Text("✗ Nicht beide", textAlign = TextAlign.Center)
                    }
                    Button(onClick = { vm.grade(true) }, modifier = Modifier.weight(1f)) {
                        Text("✓ Beide gewusst", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishedCard(vm: CompareViewModel, s: ComparePractice.Finished) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (s.total == 0) {
                Text("Heute ist hier nichts fällig ✓", style = MaterialTheme.typography.titleLarge)
            } else {
                Text("Runde geschafft 🎉", style = MaterialTheme.typography.titleLarge)
                Text("${s.correct} von ${s.total} auf Anhieb beide gewusst", style = MaterialTheme.typography.bodyLarge)
            }
            Button(onClick = { vm.practiceAgain() }, modifier = Modifier.fillMaxWidth()) { Text("Weiter üben") }
            OutlinedButton(onClick = { vm.endPractice() }, modifier = Modifier.fillMaxWidth()) { Text("Zurück zur Übersicht") }
        }
    }
}
