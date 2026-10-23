package de.jfricker.vokabeltrainer.video

import de.jfricker.vokabeltrainer.data.LearningPrefs
import de.jfricker.vokabeltrainer.data.local.AppDatabase
import de.jfricker.vokabeltrainer.data.local.PlaylistEntity
import de.jfricker.vokabeltrainer.data.local.PlaylistSummary
import de.jfricker.vokabeltrainer.data.local.VideoEntity
import de.jfricker.vokabeltrainer.data.local.VideoStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class VideoRepository(db: AppDatabase, private val prefs: LearningPrefs) {

    private val dao = db.videoDao()

    var apiKey: String?
        get() = prefs.youtubeApiKey?.takeIf { it.isNotBlank() }
        set(value) {
            prefs.youtubeApiKey = value?.trim()?.takeIf { it.isNotEmpty() }
        }

    fun observePlaylists(): Flow<List<PlaylistSummary>> = dao.observePlaylists()
    fun observePlaylist(id: String): Flow<PlaylistEntity?> = dao.observePlaylist(id)
    fun observeVideos(playlistId: String): Flow<List<VideoEntity>> = dao.observeVideos(playlistId)
    fun observeVideo(videoId: String): Flow<VideoEntity?> = dao.observeVideo(videoId)

    /** Playlist hinzufuegen und alle Videos laden. Ergebnis: Anzahl neuer Videos. */
    suspend fun addPlaylist(input: String): Result<Int> = attempt {
        val id = YouTubeApi.extractPlaylistId(input)
            ?: throw YouTubeException("Im Link wurde keine Playlist gefunden (…&list=…).")
        val key = requireKey()
        val info = YouTubeApi.playlistInfo(id, key)
        val now = System.currentTimeMillis()
        val existing = dao.playlist(id)
        dao.savePlaylist(PlaylistEntity(id, info.title, info.channelTitle, existing?.addedAt ?: now, now))
        syncInternal(id, key)
    }

    /** Neue Folgen nachladen; Status bestehender Videos bleibt erhalten. */
    suspend fun sync(playlistId: String): Result<Int> = attempt { syncInternal(playlistId, requireKey()) }

    private suspend fun syncInternal(playlistId: String, key: String): Int {
        val items = YouTubeApi.playlistItems(playlistId, key)
        val known = dao.videoIds(playlistId).toSet()
        val new = items.filter { it.videoId !in known }
        dao.insertNewVideos(new.map {
            VideoEntity(it.videoId, playlistId, it.title, it.channelTitle, it.thumbnailUrl, it.position, VideoStatus.NEW, null)
        })
        items.filter { it.videoId in known }.forEach {
            dao.updateVideoInfo(it.videoId, it.title, it.channelTitle, it.thumbnailUrl, it.position)
        }
        dao.setSynced(playlistId, System.currentTimeMillis())
        return new.size
    }

    suspend fun setStatus(videoId: String, status: String) =
        dao.setStatus(videoId, status, System.currentTimeMillis())

    suspend fun nextOpen(current: VideoEntity): VideoEntity? = dao.nextOpen(current.playlistId, current.position)

    suspend fun deletePlaylist(id: String) {
        dao.deleteVideos(id)
        dao.deletePlaylist(id)
    }

    private fun requireKey(): String = apiKey ?: throw YouTubeException("Bitte zuerst den YouTube-API-Schlüssel eingeben.")

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: YouTubeException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(YouTubeException("Keine Verbindung zu YouTube. Bist du online?"))
    }
}
