package de.jfricker.vokabeltrainer.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.domain.Dialogue
import de.jfricker.vokabeltrainer.domain.DialogueLine

// ---------------------------------------------------------------- Liste

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueListScreen(vm: DialogueViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val language by vm.language.collectAsStateWithLifecycle()
    val list by vm.list.collectAsStateWithLifecycle()

    // Bei jedem Anzeigen neu laden (Stand nach "Rolle spielen")
    LaunchedEffect(language) { vm.loadList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 Typische Dialoge") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "${language.flag}  ${language.displayName}: anhören oder selbst eine Rolle spielen",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            val entries = list
            when {
                entries == null -> item { Text("…") }
                entries.isEmpty() -> item { Text("Noch keine Dialoge für ${language.displayName}.") }
                else -> items(entries, key = { it.dialogue.key }) { s ->
                    DialogueCard(s, onClick = { onOpen(s.dialogue.key) })
                }
            }
        }
    }
}

@Composable
private fun DialogueCard(s: DialogueSummary, onClick: () -> Unit) {
    val d = s.dialogue
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(d.title, style = MaterialTheme.typography.titleMedium)
            Text(d.titleTarget, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            Text(d.situation, style = MaterialTheme.typography.bodySmall)
            val p = s.progress
            Text(
                if (p == null) "Noch nicht gespielt"
                else "${p.count}× gespielt · zuletzt ${p.lastCorrect} von ${p.lastTotal} richtig",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------- ein Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(vm: DialogueViewModel, key: String, onBack: () -> Unit) {
    LaunchedEffect(key) { vm.open(key) }
    DisposableEffect(Unit) { onDispose { vm.close() } }

    val dialogue by vm.dialogue.collectAsStateWithLifecycle()
    val slow by vm.slow.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dialogue?.title ?: "Dialog") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
                actions = {
                    FilterChip(selected = slow, onClick = { vm.setSlow(!slow) }, label = { Text("Langsam") })
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
    ) { padding ->
        val d = dialogue
        if (d == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("…") }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                PrimaryTabRow(selectedTabIndex = tab) {
                    listOf("🎧 Anhören", "🎭 Rolle spielen").forEachIndexed { i, title ->
                        Tab(selected = tab == i, onClick = { vm.stop(); tab = i }, text = { Text(title) })
                    }
                }
                if (tab == 0) ListenTab(vm, d) else RolePlayTab(vm, d)
            }
        }
    }
}

@Composable
private fun ListenTab(vm: DialogueViewModel, d: Dialogue) {
    val playing by vm.playing.collectAsStateWithLifecycle()
    val showDe by vm.showTranslation.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Bildschirm nachfuehren: gesprochene Zeile (Item i + 1 wegen Kopfteil) sichtbar halten
    LaunchedEffect(playing) {
        val item = (playing ?: return@LaunchedEffect) + 1
        val info = listState.layoutInfo
        val visible = info.visibleItemsInfo.firstOrNull { it.index == item }
        val fullyVisible = visible != null && visible.offset >= info.viewportStartOffset &&
            visible.offset + visible.size <= info.viewportEndOffset
        // Vorherige Zeile oben stehen lassen, damit man den Zusammenhang sieht
        if (!fullyVisible) listState.animateScrollToItem(maxOf(0, item - 1))
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(d.titleTarget, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
                Text(d.situation, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (playing == null) {
                        Button(onClick = { vm.playAll() }) { Text("▶  Abspielen") }
                    } else {
                        OutlinedButton(onClick = { vm.stop() }) { Text("⏹  Stopp") }
                    }
                    FilterChip(selected = showDe, onClick = { vm.setShowTranslation(!showDe) }, label = { Text("Deutsch") })
                }
                Text("Satz antippen, um ihn einzeln anzuhören.", style = MaterialTheme.typography.bodySmall)
            }
        }
        itemsIndexed(d.lines) { i, line ->
            LineBubble(
                d, line,
                right = line.role == 1,
                highlighted = playing == i,
                showDe = showDe,
                onClick = { vm.speakLine(i) },
            )
        }
    }
}

@Composable
private fun RolePlayTab(vm: DialogueViewModel, d: Dialogue) {
    val state by vm.rolePlay.collectAsStateWithLifecycle()
    val speakMode by vm.speakMode.collectAsStateWithLifecycle()
    val playing by vm.playing.collectAsStateWithLifecycle()
    val showDe by vm.showTranslation.collectAsStateWithLifecycle()
    val s = state

    if (s == null) {
        RolePlayIntro(d, speakMode, onSpeakMode = vm::setSpeakMode, onStart = vm::startRolePlay)
        return
    }

    val listState = rememberLazyListState()
    // Immer zum Ende scrollen: Kopfzeile + sichtbare Saetze + Aufgabe/Ergebnis
    LaunchedEffect(s.shown, s.current, s.chosen, s.revealed) {
        listState.animateScrollToItem(s.shown + 1)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Du spielst: ${d.roleName(s.learnerRole)}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(selected = speakMode, onClick = { vm.setSpeakMode(!speakMode) }, label = { Text("Laut sagen") })
            }
        }
        items(s.shown) { i ->
            val line = d.lines[i]
            val mine = line.role == s.learnerRole
            LineBubble(
                d, line,
                right = mine,
                highlighted = playing == i,
                // Eigene Saetze immer mit Deutsch; beim Gegenueber nach Schalter "Deutsch" (Reiter Anhören)
                showDe = mine || showDe,
                mark = s.results[i],
                onClick = { vm.speakLine(i) },
            )
        }
        item {
            val cur = s.current
            if (cur == null) {
                RolePlayFinished(
                    s,
                    onAgain = { vm.startRolePlay(s.learnerRole) },
                    onSwap = { vm.startRolePlay(1 - s.learnerRole) },
                    swapName = d.roleName(1 - s.learnerRole),
                )
            } else {
                RolePlayPrompt(vm, d.lines[cur], s, speakMode)
            }
        }
    }
}

@Composable
private fun RolePlayIntro(d: Dialogue, speakMode: Boolean, onSpeakMode: (Boolean) -> Unit, onStart: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(d.situation, style = MaterialTheme.typography.bodyLarge)
        Text(
            "Die App spricht ${d.roleName(0)}, du antwortest als ${d.roleName(1)}. " +
                "Was du sagen sollst, steht auf Deutsch da.",
            style = MaterialTheme.typography.bodyMedium,
        )
        FilterChip(
            selected = speakMode,
            onClick = { onSpeakMode(!speakMode) },
            label = { Text("Laut sagen und selbst bewerten (statt auswählen)") },
        )
        Button(onClick = { onStart(1) }, modifier = Modifier.fillMaxWidth()) { Text("▶  Starten") }
        OutlinedButton(onClick = { onStart(0) }, modifier = Modifier.fillMaxWidth()) {
            Text("🔄  Rolle tauschen: du bist ${d.roleName(0)}")
        }
    }
}

@Composable
private fun RolePlayPrompt(vm: DialogueViewModel, line: DialogueLine, s: RolePlayState, speakMode: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Du sagst:", style = MaterialTheme.typography.labelMedium)
            Text(line.de, style = MaterialTheme.typography.titleMedium)

            if (speakMode && s.chosen == null) {
                if (!s.revealed) {
                    Text("Sag den Satz laut, dann aufdecken.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { vm.reveal() }, modifier = Modifier.fillMaxWidth()) { Text("Lösung zeigen") }
                } else {
                    Text(line.text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    if (line.note.isNotEmpty()) ExplanationText("💡 ${line.note}", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.selfGrade(false) }, modifier = Modifier.weight(1f)) { Text("✗ Nicht gewusst") }
                        Button(onClick = { vm.selfGrade(true) }, modifier = Modifier.weight(1f)) { Text("✓ Gewusst") }
                    }
                }
            } else {
                val answered = s.chosen != null
                s.options.forEachIndexed { i, option ->
                    val isCorrect = i == s.correctIndex
                    val color = when {
                        answered && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        answered && i == s.chosen -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val prefix = when {
                        answered && isCorrect -> "✓  "
                        answered && i == s.chosen -> "✗  "
                        else -> ""
                    }
                    Card(
                        onClick = { if (!answered) vm.choose(i) },
                        colors = CardDefaults.cardColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(prefix + option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(14.dp))
                    }
                }
                if (answered) {
                    if (line.note.isNotEmpty()) ExplanationText("💡 ${line.note}", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { vm.next() }, modifier = Modifier.fillMaxWidth()) { Text("Weiter") }
                }
            }
        }
    }
}

@Composable
private fun RolePlayFinished(s: RolePlayState, onAgain: () -> Unit, onSwap: () -> Unit, swapName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dialog geschafft 🎉", style = MaterialTheme.typography.titleLarge)
            Text("${s.correctCount} von ${s.results.size} Sätzen richtig", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onAgain, modifier = Modifier.fillMaxWidth()) { Text("Nochmals") }
            OutlinedButton(onClick = onSwap, modifier = Modifier.fillMaxWidth()) { Text("🔄  Rolle tauschen: du bist $swapName") }
        }
    }
}

/** Sprechblase: Gegenueber links, du rechts. mark = Ergebnis beim Rollenspiel (✓/✗). */
@Composable
private fun LineBubble(
    d: Dialogue,
    line: DialogueLine,
    right: Boolean,
    highlighted: Boolean,
    showDe: Boolean,
    mark: Boolean? = null,
    onClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (right) Arrangement.End else Arrangement.Start) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = when {
                highlighted -> MaterialTheme.colorScheme.tertiaryContainer
                right -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val markText = when (mark) {
                    true -> "  ✓"
                    false -> "  ✗"
                    null -> ""
                }
                Text(d.roleName(line.role) + markText, style = MaterialTheme.typography.labelMedium)
                Text(line.text, style = MaterialTheme.typography.bodyLarge)
                if (showDe) {
                    Text(line.de, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    if (line.note.isNotEmpty()) ExplanationText("💡 ${line.note}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
