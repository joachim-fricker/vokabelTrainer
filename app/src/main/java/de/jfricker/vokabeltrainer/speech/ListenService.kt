package de.jfricker.vokabeltrainer.speech

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import de.jfricker.vokabeltrainer.MainActivity
import de.jfricker.vokabeltrainer.R
import de.jfricker.vokabeltrainer.VokabelApp
import de.jfricker.vokabeltrainer.domain.Dialogues
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.ListenItem
import de.jfricker.vokabeltrainer.domain.ListenOrder
import de.jfricker.vokabeltrainer.domain.ListenPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/** Zustand von "Nur hören" fuer den Bildschirm. */
data class ListenState(
    val running: Boolean = false,
    val loading: Boolean = false,
    /** Kurz unterbrochen, z. B. durch einen Anruf */
    val paused: Boolean = false,
    val current: ListenItem? = null,
    /** Aktueller Eintrag in dieser Runde als "Nicht gewusst" markiert */
    val currentBoosted: Boolean = false,
    /** Dialog: Index der gerade gesprochenen Zeile, sonst -1 */
    val line: Int = -1,
    /** So viele Eintraege wurden in dieser Runde schon vorgelesen */
    val count: Int = 0,
    val total: Int = 0,
    val error: String? = null,
)

/**
 * "Nur hören": liest Woerter/Saetze und ihre Uebersetzung in einer Endlosschlaufe vor, bis man aufhoert.
 * Laeuft als Vordergrunddienst (Benachrichtigung mit Stopp), damit es auch bei ausgeschaltetem Bildschirm
 * weitergeht. Eigene TextToSpeech-Instanz, unabhaengig vom Speaker der App.
 */
class ListenService : Service(), TextToSpeech.OnInitListener {

    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var picker: ListenPicker? = null
    private var language = Language.PT_BR
    private var order = ListenOrder.TARGET_FIRST
    private var slow = false
    private var targetLocale: Locale? = null
    private var germanLocale: Locale? = null
    /** Nummer des gerade gesprochenen Eintrags; Rueckmeldungen aelterer Eintraege werden ignoriert. */
    @Volatile private var generation = 0
    private var wakeLock: PowerManager.WakeLock? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stopListening()
            ACTION_SKIP -> skip()
            ACTION_UNKNOWN -> markUnknown()
            // Neustart durch das System ohne Auftrag: nicht weiterlaufen
            else -> if (!_state.value.running) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        if (_state.value.running) return
        val app = application as VokabelApp
        language = app.prefs.language
        order = app.prefs.listenOrder
        slow = app.prefs.listenSlow
        val words = app.prefs.listenWords
        val sentences = app.prefs.listenSentences
        val dialogues = app.prefs.listenDialogues
        app.speaker.stop()

        _state.value = ListenState(running = true, loading = true)
        startForeground(NOTIFICATION_ID, buildNotification(null), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vokabeltrainer:listen")
            .apply { acquire(MAX_RUN_MS) }

        tts = TextToSpeech(applicationContext, this)
        scope.launch {
            val items = app.listenSource.load(language, words, sentences, dialogues)
            picker = ListenPicker(items)
            _state.value = _state.value.copy(total = items.size)
            if (items.isEmpty()) fail("Nichts zum Vorlesen gefunden.") else maybeBegin()
        }
    }

    override fun onInit(status: Int) {
        main.post {
            if (status != TextToSpeech.SUCCESS) {
                fail("Die Sprachausgabe konnte nicht gestartet werden.")
                return@post
            }
            targetLocale = localeFor(language.ttsLocales)
            germanLocale = localeFor(listOf("de-DE", "de-CH", "de-AT", "de"))
            when {
                targetLocale == null -> fail("Keine Stimme für ${language.variantName} installiert.")
                germanLocale == null -> fail("Keine deutsche Stimme installiert (Android-Einstellungen → Sprachausgabe).")
                else -> {
                    tts.setAudioAttributes(AUDIO_ATTRIBUTES)
                    tts.setOnUtteranceProgressListener(progressListener)
                    ttsReady = true
                    maybeBegin()
                }
            }
        }
    }

    private fun localeFor(tags: List<String>): Locale? {
        val locales = tags.map { Locale.forLanguageTag(it) }
        return locales.firstOrNull { tts.isLanguageAvailable(it) >= TextToSpeech.LANG_COUNTRY_AVAILABLE }
            ?: locales.firstOrNull { tts.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
    }

    /** Beginnen, sobald Sprachausgabe und Eintraege bereit sind. */
    private fun maybeBegin() {
        if (!ttsReady || picker == null || !_state.value.loading) return
        if (!requestFocus()) {
            fail("Die Audioausgabe ist gerade belegt (z. B. durch einen Anruf).")
            return
        }
        _state.value = _state.value.copy(loading = false)
        next()
    }

    /** Naechsten Eintrag vorlesen: beide Texte, dazwischen Denkpause, danach Pause bis zum naechsten. */
    private fun next() {
        val s = _state.value
        if (!s.running || s.paused) return
        val p = picker ?: return
        val item = p.next() ?: return
        val gen = ++generation
        _state.value = s.copy(current = item, currentBoosted = p.isBoosted(item.key), count = s.count + 1, line = -1)
        updateNotification(item)

        val factor = if (slow) 1.5 else 1.0
        // Satz braucht mehr Zeit zum Mitdenken als ein einzelnes Wort
        val think = ((if (order == ListenOrder.DE_FIRST) 1500 else 500) * factor * (if (item.sentence) 1.2 else 1.0)).toLong()
        val after = (2000 * factor).toLong()
        tts.setSpeechRate(if (slow) 0.6f else 0.9f)
        var first = true

        fun say(text: String, locale: Locale, pitch: Float, id: String) {
            tts.setLanguage(locale)
            tts.setPitch(pitch)
            tts.speak(clean(text), if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
            first = false
        }

        fun pair(text: String, de: String, pitch: Float, tag: String) {
            if (order == ListenOrder.DE_FIRST) {
                say(de, germanLocale!!, 1.0f, "L:$gen:${tag}1")
                tts.playSilentUtterance(think, TextToSpeech.QUEUE_ADD, "L:$gen:${tag}p")
                say(text, targetLocale!!, pitch, "L:$gen:${tag}2")
            } else {
                say(text, targetLocale!!, pitch, "L:$gen:${tag}1")
                tts.playSilentUtterance(think, TextToSpeech.QUEUE_ADD, "L:$gen:${tag}p")
                say(de, germanLocale!!, 1.0f, "L:$gen:${tag}2")
            }
        }

        if (item.dialogue) {
            // Dialoge immer nur in der Lernsprache, ohne Uebersetzung (Wunsch des Nutzers, 11.11.2026):
            // Titel in der Lernsprache, dann Zeile fuer Zeile mit Rollen-Stimmhoehe; Kennung "d<zeile>-1"
            say(item.text, targetLocale!!, 1.0f, "L:$gen:t")
            tts.playSilentUtterance((1000 * factor).toLong(), TextToSpeech.QUEUE_ADD, "L:$gen:tp")
            item.lines.forEachIndexed { i, line ->
                if (i > 0) tts.playSilentUtterance(Dialogues.pauseMs(slow), TextToSpeech.QUEUE_ADD, "L:$gen:d$i-g")
                say(line.text, targetLocale!!, line.pitch, "L:$gen:d$i-1")
            }
        } else {
            pair(item.text, item.de, 1.0f, "")
        }
        tts.setPitch(1.0f)
        tts.playSilentUtterance(after, TextToSpeech.QUEUE_ADD, "L:$gen:end")
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            // Dialog: gerade gesprochene Zeile anzeigen ("L:<gen>:d<zeile>-1")
            val parts = utteranceId?.split(':') ?: return
            if (parts.size != 3 || parts[0] != "L" || parts[1].toIntOrNull() != generation) return
            val line = Regex("^d(\\d+)-[12]$").find(parts[2])?.groupValues?.get(1)?.toIntOrNull() ?: return
            main.post { if (_state.value.line != line) _state.value = _state.value.copy(line = line) }
        }

        override fun onDone(utteranceId: String?) {
            val parts = utteranceId?.split(':') ?: return
            if (parts.size == 3 && parts[0] == "L" && parts[2] == "end" && parts[1].toIntOrNull() == generation) {
                main.post { next() }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            // Einzelner Fehler (z. B. leerer Text): einfach weiter
            onDone(utteranceId?.substringBeforeLast(':')?.plus(":end"))
        }
    }

    private fun skip() {
        if (!_state.value.running || _state.value.loading) return
        generation++
        if (ttsReady) tts.stop()
        if (_state.value.paused) _state.value = _state.value.copy(paused = false)
        next()
    }

    /** Aktueller Eintrag: in dieser Runde oefter vorlesen. */
    private fun markUnknown() {
        val item = _state.value.current ?: return
        picker?.markUnknown(item.key)
        _state.value = _state.value.copy(currentBoosted = true)
    }

    private fun fail(message: String) {
        _state.value = ListenState(error = message)
        shutdown()
    }

    private fun stopListening() {
        _state.value = ListenState()
        shutdown()
    }

    private fun shutdown() {
        generation++
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        ttsReady = false
        abandonFocus()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        if (_state.value.running) {
            _state.value = ListenState()
            shutdown()
        }
        super.onDestroy()
    }

    // ------------------------------------------------------------ Audiofokus (Anrufe, andere Apps)

    private fun requestFocus(): Boolean {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setOnAudioFocusChangeListener({ change -> onFocusChange(change) }, main)
            .build()
        focusRequest = req
        return am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        val req = focusRequest ?: return
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).abandonAudioFocusRequest(req)
        focusRequest = null
    }

    private fun onFocusChange(change: Int) {
        val s = _state.value
        if (!s.running) return
        when (change) {
            // Andere App spielt dauerhaft ab: aufhoeren
            AudioManager.AUDIOFOCUS_LOSS -> stopListening()
            // Anruf, Navigationsansage ...: anhalten und danach weiter
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                generation++
                if (ttsReady) tts.stop()
                _state.value = s.copy(paused = true)
            }
            AudioManager.AUDIOFOCUS_GAIN -> if (s.paused) {
                _state.value = s.copy(paused = false)
                next()
            }
        }
    }

    // ------------------------------------------------------------ Benachrichtigung

    private fun buildNotification(item: ListenItem?): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Nur hören", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Läuft, solange „Nur hören“ aktiv ist"
                    setShowBadge(false)
                }
            )
        }
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_listen)
            .setContentTitle("🎧 Nur hören · ${language.flag} ${language.displayName}")
            .setContentText(
                item?.let { if (it.dialogue) "💬 ${it.text}" else "${it.text} – ${it.de}" } ?: "Wird vorbereitet …"
            )
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(action("Nicht gewusst", ACTION_UNKNOWN, 1))
            .addAction(action("Weiter", ACTION_SKIP, 2))
            .addAction(action("Stopp", ACTION_STOP, 3))
            .build()
    }

    private fun action(title: String, action: String, requestCode: Int): Notification.Action {
        val pi = PendingIntent.getService(
            this, requestCode,
            Intent(this, ListenService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Action.Builder(null as Icon?, title, pi).build()
    }

    private fun updateNotification(item: ListenItem) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(item))
    }

    companion object {
        const val ACTION_START = "de.jfricker.vokabeltrainer.listen.START"
        const val ACTION_STOP = "de.jfricker.vokabeltrainer.listen.STOP"
        const val ACTION_SKIP = "de.jfricker.vokabeltrainer.listen.SKIP"
        const val ACTION_UNKNOWN = "de.jfricker.vokabeltrainer.listen.UNKNOWN"
        private const val CHANNEL_ID = "listen"
        private const val NOTIFICATION_ID = 42
        /** Sicherheitsgrenze fuer den Wake-Lock; danach laeuft es nur bei eingeschaltetem Bildschirm sicher weiter */
        private const val MAX_RUN_MS = 4 * 60 * 60 * 1000L

        private val AUDIO_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        private val _state = MutableStateFlow(ListenState())
        val state: StateFlow<ListenState> = _state.asStateFlow()

        fun send(context: Context, action: String) {
            val intent = Intent(context, ListenService::class.java).setAction(action)
            if (action == ACTION_START) context.startForegroundService(intent) else context.startService(intent)
        }

        /** Hinweise in Klammern nicht mitsprechen; "/" als kurze Pause */
        private fun clean(text: String) = text.replace(Regex("\\([^)]*\\)"), "").replace("/", ",").trim()
    }
}
