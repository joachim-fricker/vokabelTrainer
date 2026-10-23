package de.jfricker.vokabeltrainer.video

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Eingebetteter YouTube-Player (WebView mit dem offiziellen Embed-Player).
 * Portugiesische Untertitel werden automatisch eingeschaltet (cc_load_policy, cc_lang_pref).
 * Vollbild ueber den Knopf im Player: das Video wird dann ueber die ganze App gelegt.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayer(videoId: String, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity()
    // Vollbild-Ansicht, die der Player liefert, und der Rueckruf zum Beenden
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    fun exitFullscreen() {
        fullscreenCallback?.onCustomViewHidden()
    }

    BackHandler(enabled = fullscreenView != null) { exitFullscreen() }

    // Vollbild-Ansicht ueber das ganze Fenster legen
    DisposableEffect(fullscreenView) {
        val view = fullscreenView
        val decor = activity?.window?.decorView as? FrameLayout
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        if (view != null && decor != null) {
            view.setBackgroundColor(Color.BLACK)
            decor.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            controller?.hide(WindowInsetsCompat.Type.systemBars())
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            if (view != null && decor != null) {
                decor.removeView(view)
                controller?.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                // Ohne feste Groesse und Hardware-Ebene zeigen manche Geraete nur Ton, aber kein Bild
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(Color.BLACK)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                        fullscreenView = view
                        fullscreenCallback = callback
                    }

                    override fun onHideCustomView() {
                        fullscreenView = null
                        fullscreenCallback = null
                    }

                    // Verhindert ein graues Standard-Vorschaubild vor dem Start
                    override fun getDefaultVideoPoster(): Bitmap =
                        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }
                webViewClient = object : WebViewClient() {
                    // Links, die den Player verlassen (z. B. YouTube-Logo), extern oeffnen
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (!request.isForMainFrame) return false
                        openExternally(ctx, request.url)
                        return true
                    }
                }
            }
        },
        update = { webView ->
            if (webView.tag != videoId) {
                webView.tag = videoId
                // Eine https-Basis-URL sorgt fuer einen gueltigen Referer; ohne ihn verweigert YouTube
                // in WebViews die Wiedergabe (Fehler 152/153).
                webView.loadDataWithBaseURL(BASE_URL, playerHtml(videoId), "text/html", "utf-8", null)
            }
        },
        onRelease = {
            fullscreenCallback?.onCustomViewHidden()
            it.destroy()
        },
    )
}

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

fun openInYouTube(context: Context, videoId: String) =
    openExternally(context, Uri.parse("https://www.youtube.com/watch?v=$videoId"))

private fun openExternally(context: Context, uri: Uri) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        // keine passende App - ignorieren
    }
}

private const val BASE_URL = "https://de.jfricker.vokabeltrainer/"

private fun playerHtml(videoId: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="referrer" content="strict-origin-when-cross-origin">
<style>
  html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
  iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
</style>
</head>
<body>
<iframe
  src="https://www.youtube.com/embed/$videoId?playsinline=1&autoplay=1&rel=0&fs=1&cc_load_policy=1&cc_lang_pref=pt&hl=pt"
  referrerpolicy="strict-origin-when-cross-origin"
  allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
  allowfullscreen></iframe>
</body>
</html>
""".trimIndent()
