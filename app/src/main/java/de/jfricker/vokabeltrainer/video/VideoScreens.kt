package de.jfricker.vokabeltrainer.video

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.data.local.VideoEntity
import de.jfricker.vokabeltrainer.data.local.VideoStatus
import kotlinx.coroutines.launch

/** Meldungen des ViewModels als Snackbar zeigen. */
@Composable
private fun MessageSnackbar(vm: VideoViewModel, host: SnackbarHostState) {
    val message by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(message) {
        message?.let {
            host.showSnackbar(it)
            vm.clearMessage()
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
}

// ====================================================================== Playlists

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(vm: VideoViewModel, onBack: () -> Unit, onOpenPlaylist: (String) -> Unit) {
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    val hasKey by vm.hasApiKey.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showKeyDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    MessageSnackbar(vm, snackbar)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                navigationIcon = { BackButton(onBack) },
                actions = { TextButton(onClick = { showKeyDialog = true }) { Text("🔑 Schlüssel") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!hasKey) {
                item { ApiKeyHelpCard(onEnter = { showKeyDialog = true }) }
            }
            if (busy) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            items(playlists, key = { it.playlist.id }) { p ->
                Card(onClick = { onOpenPlaylist(p.playlist.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.playlist.title, style = MaterialTheme.typography.titleMedium)
                        p.playlist.channelTitle?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LinearProgressIndicator(
                            progress = { if (p.total == 0) 0f else p.done.toFloat() / p.total },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                        Text("${p.done} von ${p.total} gesehen", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (playlists.isEmpty() && hasKey) {
                item {
                    Text(
                        "Noch keine Playlist. Kopiere in YouTube den Link einer Playlist (Teilen → Link kopieren) " +
                            "und füge ihn hier ein.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                Button(
                    onClick = { showAddDialog = true },
                    enabled = hasKey && !busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("+ Playlist hinzufügen") }
            }
        }
    }

    if (showKeyDialog) {
        TextInputDialog(
            title = "YouTube-API-Schlüssel",
            label = "Schlüssel",
            initial = vm.apiKey(),
            confirm = "Speichern",
            onConfirm = { vm.setApiKey(it); showKeyDialog = false },
            onDismiss = { showKeyDialog = false },
        )
    }
    if (showAddDialog) {
        TextInputDialog(
            title = "Playlist hinzufügen",
            label = "Link zur Playlist",
            initial = "",
            confirm = "Hinzufügen",
            onConfirm = { vm.addPlaylist(it); showAddDialog = false },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun ApiKeyHelpCard(onEnter: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Einmalig: YouTube-API-Schlüssel", style = MaterialTheme.typography.titleMedium)
            Text(
                "1. Am PC console.cloud.google.com öffnen und mit deinem Google-Konto anmelden.\n" +
                    "2. Oben ein neues Projekt anlegen (z. B. „Vokabeltrainer“).\n" +
                    "3. „APIs & Dienste“ → „Bibliothek“ → „YouTube Data API v3“ suchen → Aktivieren.\n" +
                    "4. „APIs & Dienste“ → „Anmeldedaten“ → „Anmeldedaten erstellen“ → „API-Schlüssel“.\n" +
                    "5. Den Schlüssel kopieren (z. B. per Mail ans Handy schicken) und hier einfügen.\n\n" +
                    "Der Schlüssel ist kostenlos und bleibt nur auf deinem Handy gespeichert.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onEnter, modifier = Modifier.fillMaxWidth()) { Text("Schlüssel eingeben") }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    label: String,
    initial: String,
    confirm: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

// ====================================================================== Folgen einer Playlist

private enum class VideoFilter(val label: String) { OPEN("Offen"), DONE("Gesehen"), ALL("Alle"), HIDDEN("Ausgeblendet") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(vm: VideoViewModel, playlistId: String, onBack: () -> Unit, onPlay: (String) -> Unit) {
    val playlist by remember(playlistId) { vm.playlist(playlistId) }.collectAsStateWithLifecycle(null)
    val videos by remember(playlistId) { vm.videos(playlistId) }.collectAsStateWithLifecycle(emptyList())
    val busy by vm.busy.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(VideoFilter.OPEN) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    MessageSnackbar(vm, snackbar)

    val shown = videos.filter {
        when (filter) {
            VideoFilter.OPEN -> it.status == VideoStatus.NEW
            VideoFilter.DONE -> it.status == VideoStatus.DONE
            VideoFilter.ALL -> it.status != VideoStatus.HIDDEN
            VideoFilter.HIDDEN -> it.status == VideoStatus.HIDDEN
        }
    }

    fun hide(v: VideoEntity) {
        val previous = v.status
        vm.setStatus(v.videoId, VideoStatus.HIDDEN)
        scope.launch {
            val r = snackbar.showSnackbar("Video ausgeblendet", actionLabel = "Rückgängig")
            if (r == SnackbarResult.ActionPerformed) vm.setStatus(v.videoId, previous)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    TextButton(onClick = { vm.sync(playlistId) }, enabled = !busy) { Text("↻") }
                    TextButton(onClick = { confirmDelete = true }) { Text("Entfernen") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VideoFilter.entries.forEach { f ->
                    val count = videos.count {
                        when (f) {
                            VideoFilter.OPEN -> it.status == VideoStatus.NEW
                            VideoFilter.DONE -> it.status == VideoStatus.DONE
                            VideoFilter.ALL -> it.status != VideoStatus.HIDDEN
                            VideoFilter.HIDDEN -> it.status == VideoStatus.HIDDEN
                        }
                    }
                    if (f != VideoFilter.HIDDEN || count > 0) {
                        FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text("${f.label} $count") })
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                if (shown.isEmpty()) {
                    item {
                        Text(
                            if (filter == VideoFilter.OPEN && videos.isNotEmpty()) "Alles gesehen. 🎉" else "Keine Videos.",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                items(shown, key = { it.videoId }) { v ->
                    VideoRow(
                        v,
                        onPlay = { onPlay(v.videoId) },
                        onToggleDone = {
                            vm.setStatus(v.videoId, if (v.status == VideoStatus.DONE) VideoStatus.NEW else VideoStatus.DONE)
                        },
                        onHideOrRestore = {
                            if (v.status == VideoStatus.HIDDEN) vm.setStatus(v.videoId, VideoStatus.NEW) else hide(v)
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Playlist entfernen?") },
            text = { Text("Die Playlist und ihr Status (gesehen/ausgeblendet) werden aus der App entfernt. Auf YouTube ändert sich nichts.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deletePlaylist(playlistId)
                    onBack()
                }) { Text("Entfernen") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") } },
        )
    }
}

@Composable
private fun VideoRow(v: VideoEntity, onPlay: () -> Unit, onToggleDone: () -> Unit, onHideOrRestore: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(
            v.thumbnailUrl,
            Modifier
                .width(112.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(v.title, style = MaterialTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(
                "Folge ${v.position + 1}" + if (v.status == VideoStatus.DONE) " · gesehen ✓" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(4.dp))
        // Grosse Tippflaechen (48 dp)
        IconButton(onClick = onToggleDone, modifier = Modifier.size(48.dp)) {
            Text(
                if (v.status == VideoStatus.DONE) "↺" else "✓",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onHideOrRestore, modifier = Modifier.size(48.dp)) {
            Text(if (v.status == VideoStatus.HIDDEN) "↩" else "🗑", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ====================================================================== Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(vm: VideoViewModel, videoId: String, onBack: () -> Unit, onPlay: (String) -> Unit) {
    val video by remember(videoId) { vm.video(videoId) }.collectAsStateWithLifecycle(null)
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun next(status: String) {
        val v = video ?: return
        vm.setStatusAndNext(v, status) { nextId ->
            if (nextId != null) onPlay(nextId)
            else scope.launch { snackbar.showSnackbar("Das war die letzte offene Folge. 🎉") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { BackButton(onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            YouTubePlayer(
                videoId,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val v = video
                if (v != null) {
                    Text(v.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(v.channelTitle, "Folge ${v.position + 1}").joinToString(" · ") +
                            if (v.status == VideoStatus.DONE) " · gesehen ✓" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { next(VideoStatus.DONE) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text("✓  Gesehen & nächste Folge") }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(
                            onClick = {
                                vm.setStatus(v.videoId, if (v.status == VideoStatus.DONE) VideoStatus.NEW else VideoStatus.DONE)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) { Text(if (v.status == VideoStatus.DONE) "Doch nicht gesehen" else "Nur als gesehen") }
                        OutlinedButton(
                            onClick = { next(VideoStatus.HIDDEN) },
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) { Text("🗑  Gefällt nicht") }
                    }
                    TextButton(onClick = { openInYouTube(context, v.videoId) }) {
                        Text("Lässt sich nicht abspielen? In YouTube öffnen")
                    }
                    Text(
                        "Tipp: Über ⚙️ im Player kannst du die Geschwindigkeit (z. B. 0,75) und die Untertitel ändern.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
