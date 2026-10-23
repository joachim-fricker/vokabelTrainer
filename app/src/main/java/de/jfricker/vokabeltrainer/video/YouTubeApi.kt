package de.jfricker.vokabeltrainer.video

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeException(message: String) : Exception(message)

data class PlaylistInfo(val id: String, val title: String, val channelTitle: String?)

data class PlaylistItem(
    val videoId: String,
    val title: String,
    val channelTitle: String?,
    val thumbnailUrl: String?,
    val position: Int,
)

/** Minimaler Client fuer die YouTube Data API v3 (nur lesend, mit API-Schluessel). */
object YouTubeApi {

    private const val BASE = "https://www.googleapis.com/youtube/v3"
    private val json = Json { ignoreUnknownKeys = true }

    /** Playlist-ID aus einem Link ("...&list=PL...") oder direkt die ID. */
    fun extractPlaylistId(input: String): String? {
        val text = input.trim()
        if (text.isEmpty()) return null
        if (text.contains("list=")) {
            return runCatching { Uri.parse(text).getQueryParameter("list") }.getOrNull()
                ?: Regex("list=([A-Za-z0-9_-]+)").find(text)?.groupValues?.get(1)
        }
        return text.takeIf { Regex("^[A-Za-z0-9_-]{10,}$").matches(it) }
    }

    suspend fun playlistInfo(playlistId: String, key: String): PlaylistInfo {
        val root = get("playlists", mapOf("part" to "snippet", "id" to playlistId, "key" to key))
        val item = root["items"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw YouTubeException("Playlist nicht gefunden. Ist sie öffentlich?")
        val snippet = item["snippet"]?.jsonObject
        return PlaylistInfo(
            id = playlistId,
            title = snippet?.str("title") ?: playlistId,
            channelTitle = snippet?.str("channelTitle"),
        )
    }

    /** Alle Videos der Playlist (seitenweise je 50). Private und geloeschte Videos werden uebersprungen. */
    suspend fun playlistItems(playlistId: String, key: String): List<PlaylistItem> {
        val result = mutableListOf<PlaylistItem>()
        var pageToken: String? = null
        var pages = 0
        do {
            val params = mutableMapOf(
                "part" to "snippet,contentDetails,status",
                "playlistId" to playlistId,
                "maxResults" to "50",
                "key" to key,
            )
            pageToken?.let { params["pageToken"] = it }
            val root = get("playlistItems", params)
            root["items"]?.jsonArray?.forEach { el ->
                val item = el.jsonObject
                val snippet = item["snippet"]?.jsonObject ?: return@forEach
                val privacy = item["status"]?.jsonObject?.str("privacyStatus")
                val videoId = item["contentDetails"]?.jsonObject?.str("videoId")
                    ?: snippet["resourceId"]?.jsonObject?.str("videoId")
                    ?: return@forEach
                if (privacy == "private" || privacy == "privacyStatusUnspecified") return@forEach
                val thumbs = snippet["thumbnails"]?.jsonObject
                val thumb = listOf("medium", "high", "default")
                    .firstNotNullOfOrNull { thumbs?.get(it)?.jsonObject?.str("url") }
                result += PlaylistItem(
                    videoId = videoId,
                    title = snippet.str("title") ?: videoId,
                    channelTitle = snippet.str("videoOwnerChannelTitle") ?: snippet.str("channelTitle"),
                    thumbnailUrl = thumb,
                    position = snippet["position"]?.jsonPrimitive?.intOrNull ?: result.size,
                )
            }
            pageToken = root["nextPageToken"]?.jsonPrimitive?.contentOrNull
            pages++
        } while (pageToken != null && pages < 40)
        return result
    }

    private fun JsonObject.str(name: String): String? = this[name]?.jsonPrimitive?.contentOrNull

    private suspend fun get(endpoint: String, params: Map<String, String>): JsonObject = withContext(Dispatchers.IO) {
        val query = params.entries.joinToString("&") { (k, v) -> "$k=" + URLEncoder.encode(v, "UTF-8") }
        val conn = URL("$BASE/$endpoint?$query").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()
            if (code !in 200..299) {
                val apiMessage = root?.get("error")?.jsonObject?.str("message")
                throw YouTubeException(
                    when (code) {
                        400, 403 -> "YouTube lehnt die Anfrage ab ($code). Ist der API-Schlüssel richtig und die " +
                            "„YouTube Data API v3“ aktiviert?" + (apiMessage?.let { "\n$it" } ?: "")
                        404 -> "Playlist nicht gefunden."
                        else -> "Fehler $code von YouTube" + (apiMessage?.let { ": $it" } ?: "")
                    }
                )
            }
            root ?: throw YouTubeException("Unerwartete Antwort von YouTube.")
        } finally {
            conn.disconnect()
        }
    }
}
