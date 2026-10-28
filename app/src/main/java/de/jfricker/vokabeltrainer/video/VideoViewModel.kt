package de.jfricker.vokabeltrainer.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.jfricker.vokabeltrainer.data.local.PlaylistEntity
import de.jfricker.vokabeltrainer.data.local.PlaylistSummary
import de.jfricker.vokabeltrainer.data.local.VideoEntity
import de.jfricker.vokabeltrainer.data.LearningPrefs
import de.jfricker.vokabeltrainer.domain.Language
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VideoViewModel(private val repository: VideoRepository, prefs: LearningPrefs) : ViewModel() {

    /** Gerade gewaehlte Lernsprache (Playlists und Untertitel) */
    val language: StateFlow<Language> = prefs.languageFlow

    val playlists: StateFlow<List<PlaylistSummary>> = prefs.languageFlow
        .flatMapLatest { repository.observePlaylists(it.code) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _hasApiKey = MutableStateFlow(repository.apiKey != null)
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Kurze Meldung fuer eine Snackbar */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun apiKey(): String = repository.apiKey.orEmpty()

    fun setApiKey(key: String) {
        repository.apiKey = key
        _hasApiKey.value = repository.apiKey != null
    }

    fun playlist(id: String): Flow<PlaylistEntity?> = repository.observePlaylist(id)
    fun videos(playlistId: String): Flow<List<VideoEntity>> = repository.observeVideos(playlistId)
    fun video(videoId: String): Flow<VideoEntity?> = repository.observeVideo(videoId)

    fun addPlaylist(input: String) = runBusy {
        repository.addPlaylist(input)
            .onSuccess { _message.value = "Playlist hinzugefügt: $it Videos" }
            .onFailure { _message.value = it.message }
    }

    fun sync(playlistId: String) = runBusy {
        repository.sync(playlistId)
            .onSuccess { _message.value = if (it == 0) "Keine neuen Videos" else "$it neue Videos" }
            .onFailure { _message.value = it.message }
    }

    fun setStatus(videoId: String, status: String) {
        viewModelScope.launch { repository.setStatus(videoId, status) }
    }

    /** Status setzen und danach die naechste offene Folge der Playlist melden (null = keine mehr). */
    fun setStatusAndNext(video: VideoEntity, status: String, onNext: (String?) -> Unit) {
        viewModelScope.launch {
            repository.setStatus(video.videoId, status)
            onNext(repository.nextOpen(video)?.videoId)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch { repository.deletePlaylist(id) }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun runBusy(block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                block()
            } finally {
                _busy.value = false
            }
        }
    }
}
