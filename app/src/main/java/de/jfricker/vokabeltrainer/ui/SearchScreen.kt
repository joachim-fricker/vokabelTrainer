package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.jfricker.vokabeltrainer.VokabelApp
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.SearchEngine
import de.jfricker.vokabeltrainer.domain.SearchEntry
import de.jfricker.vokabeltrainer.domain.SearchHit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Suche ueber alle Sprachen (Wunsch des Nutzers, 11.11.2026): Wort oder Satz auf Deutsch, Portugiesisch
 * oder Spanisch eingeben; jeder Treffer zeigt die Bedeutung in allen Sprachen. Antippen = vorlesen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as VokabelApp
    var engine by remember { mutableStateOf<SearchEngine?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        engine = withContext(Dispatchers.Default) { app.searchSource.load() }
        focus.requestFocus()
    }
    // Kurz warten, bis fertig getippt ist
    LaunchedEffect(query, engine) {
        val e = engine ?: return@LaunchedEffect
        delay(200)
        hits = withContext(Dispatchers.Default) { e.search(query) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Suchen") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Wort oder Satz") },
                placeholder = { Text("Deutsch, Portugiesisch oder Spanisch") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("✕") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focus),
            )
            when {
                engine == null -> Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
                SearchEngine.normalize(query).length < 2 -> Text(
                    "Mindestens zwei Buchstaben eingeben. Akzente und Gross-/Kleinschreibung sind egal. " +
                        "Antippen liest das Wort in der jeweiligen Sprache vor.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                hits.isEmpty() -> Text("Nichts gefunden.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(hits, key = { it.group }) { hit ->
                        HitCard(hit, onSpeak = { e -> app.speaker.speakIn(listOf(e.text to e.language)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HitCard(hit: SearchHit, onSpeak: (SearchEntry) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                (if (hit.sentence) "💬 " else "") + hit.de,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Language.entries.forEachIndexed { i, lang ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp))
                val list = hit.entriesFor(lang)
                if (list.isEmpty()) {
                    Text(
                        "${lang.flag}  – kein Eintrag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                list.forEach { e -> EntryRow(e, showDe = e.de != hit.de, onSpeak = { onSpeak(e) }) }
            }
        }
    }
}

@Composable
private fun EntryRow(e: SearchEntry, showDe: Boolean, onSpeak: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(e.language.flag, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(e.text, style = MaterialTheme.typography.titleMedium)
            if (showDe) Text(e.de, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            e.lessonTitle?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        SpeakButton(onClick = onSpeak)
    }
}
