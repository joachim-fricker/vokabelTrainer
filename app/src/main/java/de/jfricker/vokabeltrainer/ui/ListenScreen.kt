package de.jfricker.vokabeltrainer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.VokabelApp
import de.jfricker.vokabeltrainer.domain.ListenOrder
import de.jfricker.vokabeltrainer.speech.ListenService
import de.jfricker.vokabeltrainer.speech.ListenState

/**
 * "Nur hören": Woerter/Saetze mit Uebersetzung in einer Endlosschlaufe, bis man Stopp drueckt.
 * Das Vorlesen laeuft im ListenService weiter, auch wenn man den Bildschirm verlaesst.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as VokabelApp
    val prefs = app.prefs
    val state by ListenService.state.collectAsStateWithLifecycle()
    val language by prefs.languageFlow.collectAsStateWithLifecycle()

    var words by remember { mutableStateOf(prefs.listenWords) }
    var sentences by remember { mutableStateOf(prefs.listenSentences) }
    var dialogues by remember { mutableStateOf(prefs.listenDialogues) }
    // Mindestens eines muss gewaehlt bleiben
    fun selectedCount() = listOf(words, sentences, dialogues).count { it }
    var order by remember { mutableStateOf(prefs.listenOrder) }
    var slow by remember { mutableStateOf(prefs.listenSlow) }

    fun start() = ListenService.send(context, ListenService.ACTION_START)

    // Ohne Erlaubnis laeuft es trotzdem, nur ohne sichtbare Benachrichtigung (Stopp dann nur hier)
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { start() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎧 Nur hören · ${language.flag}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.running) {
                Playing(state)
            } else {
                Text(
                    "Liest ${language.displayName} und Deutsch abwechselnd vor – ohne Ende, bis du Stopp drückst. " +
                        "Läuft auch bei ausgeschaltetem Bildschirm weiter. Was du bei den Übungen nicht gewusst hast, " +
                        "kommt öfter dran.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text("Was?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = words,
                        onClick = { if (!words || selectedCount() > 1) { words = !words; prefs.listenWords = words } },
                        label = { Text("Wörter") },
                    )
                    FilterChip(
                        selected = sentences,
                        onClick = { if (!sentences || selectedCount() > 1) { sentences = !sentences; prefs.listenSentences = sentences } },
                        label = { Text("Sätze") },
                    )
                    FilterChip(
                        selected = dialogues,
                        onClick = { if (!dialogues || selectedCount() > 1) { dialogues = !dialogues; prefs.listenDialogues = dialogues } },
                        label = { Text("Dialoge") },
                    )
                }
                if (dialogues) {
                    Text(
                        "Dialoge werden ganz und nur auf ${language.displayName} abgespielt, ohne Übersetzung " +
                            "(die steht hier auf dem Bildschirm). Dialoge, die du bei " +
                            "„Rolle spielen“ schlecht oder noch nie gemacht hast, kommen öfter.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Text("Reihenfolge", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ListenOrder.entries.forEach { o ->
                        FilterChip(
                            selected = order == o,
                            onClick = { order = o; prefs.listenOrder = o },
                            label = {
                                Text(o.label.replace("Lernsprache", language.displayName))
                            },
                        )
                    }
                }
                Text(
                    if (order == ListenOrder.DE_FIRST) "Nach dem Deutschen kommt eine Denkpause – sag das Wort selbst, bevor die Lösung kommt."
                    else "Erst das ${language.adjective} Wort, kurze Pause, dann die Übersetzung.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
                FilterChip(
                    selected = slow,
                    onClick = { slow = !slow; prefs.listenSlow = slow },
                    label = { Text("Langsam (auch längere Pausen)") },
                )
                state.error?.let {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                    }
                }
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) start() else permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("▶  Starten", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

@Composable
private fun Playing(state: ListenState) {
    val context = LocalContext.current
    val item = state.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                state.loading || item == null -> CircularProgressIndicator()
                item.dialogue -> {
                    Text("💬 ${item.text}", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(item.de, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    item.lines.getOrNull(state.line)?.let { line ->
                        Text(line.speaker, style = MaterialTheme.typography.labelLarge)
                        Text(line.text, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Text(line.de, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    }
                    Text(
                        if (state.line >= 0) "Satz ${state.line + 1} von ${item.lines.size}" else "${item.lines.size} Sätze",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                else -> {
                    Text(
                        item.text,
                        style = if (item.sentence) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(item.de, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                }
            }
            Text(
                when {
                    state.paused -> "⏸ Unterbrochen – geht nach dem Anruf weiter"
                    state.loading -> "Wird vorbereitet …"
                    else -> "${state.count}. Eintrag · ${state.total} insgesamt"
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    OutlinedButton(
        onClick = { ListenService.send(context, ListenService.ACTION_UNKNOWN) },
        enabled = item != null && !state.currentBoosted,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text(if (state.currentBoosted) "✓ Kommt jetzt öfter" else "🤔  Nicht gewusst – öfter abspielen") }
    OutlinedButton(
        onClick = { ListenService.send(context, ListenService.ACTION_SKIP) },
        enabled = item != null,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text("⏭  Weiter") }
    Button(
        onClick = { ListenService.send(context, ListenService.ACTION_STOP) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) { Text("⏹  Stopp", style = MaterialTheme.typography.titleMedium) }
    Text(
        "Du kannst die App verlassen oder den Bildschirm ausschalten. Stopp, Weiter und Nicht gewusst " +
            "gibt es auch in der Benachrichtigung.",
        style = MaterialTheme.typography.bodySmall,
    )
}
