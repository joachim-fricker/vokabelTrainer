package de.jfricker.vokabeltrainer.video

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/** Sehr einfacher Bild-Lader fuer Vorschaubilder, mit Speicher-Cache. */
private object ThumbnailCache {
    private val cache = LruCache<String, ImageBitmap>(150)

    suspend fun load(url: String): ImageBitmap? {
        cache.get(url)?.let { return it }
        val image = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            }.getOrNull()
        }
        image?.let { cache.put(url, it) }
        return image
    }
}

@Composable
fun Thumbnail(url: String?, modifier: Modifier = Modifier) {
    val image by produceState<ImageBitmap?>(initialValue = null, url) {
        value = url?.let { ThumbnailCache.load(it) }
    }
    val img = image
    if (img != null) {
        Image(bitmap = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
