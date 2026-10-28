package de.jfricker.vokabeltrainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query(
        """
        SELECT p.*,
            (SELECT COUNT(*) FROM video v WHERE v.playlistId = p.id AND v.status != 'hidden') AS total,
            (SELECT COUNT(*) FROM video v WHERE v.playlistId = p.id AND v.status = 'done') AS done
        FROM video_playlist p
        WHERE p.language = :lang
        ORDER BY p.addedAt
        """
    )
    fun observePlaylists(lang: String): Flow<List<PlaylistSummary>>

    @Query("SELECT * FROM video_playlist WHERE id = :id")
    fun observePlaylist(id: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM video WHERE playlistId = :playlistId ORDER BY position")
    fun observeVideos(playlistId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM video WHERE videoId = :videoId")
    fun observeVideo(videoId: String): Flow<VideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM video_playlist WHERE id = :id")
    suspend fun playlist(id: String): PlaylistEntity?

    /** Vorhandene Videos (mit ihrem Status) bleiben unveraendert. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewVideos(videos: List<VideoEntity>)

    @Query("UPDATE video SET title = :title, channelTitle = :channel, thumbnailUrl = :thumb, position = :position WHERE videoId = :videoId")
    suspend fun updateVideoInfo(videoId: String, title: String, channel: String?, thumb: String?, position: Int)

    @Query("SELECT videoId FROM video WHERE playlistId = :playlistId")
    suspend fun videoIds(playlistId: String): List<String>

    @Query("UPDATE video SET status = :status, statusChangedAt = :at WHERE videoId = :videoId")
    suspend fun setStatus(videoId: String, status: String, at: Long)

    @Query("UPDATE video_playlist SET lastSyncAt = :at WHERE id = :id")
    suspend fun setSynced(id: String, at: Long)

    @Query("SELECT * FROM video WHERE videoId = :videoId")
    suspend fun video(videoId: String): VideoEntity?

    @Query("SELECT * FROM video WHERE playlistId = :playlistId AND status = 'new' AND position > :afterPosition ORDER BY position LIMIT 1")
    suspend fun nextOpen(playlistId: String, afterPosition: Int): VideoEntity?

    @Query("DELETE FROM video WHERE playlistId = :playlistId")
    suspend fun deleteVideos(playlistId: String)

    @Query("DELETE FROM video_playlist WHERE id = :id")
    suspend fun deletePlaylist(id: String)
}
