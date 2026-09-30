package com.robinrehbein.beveldevil.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import com.robinrehbein.beveldevil.game.Tune

/**
 * Plays the synthesized loops: rendered lazily on a background thread, played as looping static [AudioTrack]s,
 * crossfaded when the [Tune] changes. All work happens on one worker thread; the public calls just post to it.
 */
class Music(context: Context, private val enabled: () -> Boolean) {
    private val thread = HandlerThread("music").apply { start() }
    private val h = Handler(thread.looper)
    private val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private class Slot(val tune: Tune, val track: AudioTrack) { var gain = 0f; var target = 1f }

    // worker thread state
    private var want: Tune? = null
    private var duck = false
    private var active = true
    private var focusLost = false
    private var cur: Slot? = null
    private var old: Slot? = null
    private val pcm = LinkedHashMap<Tune, ShortArray>()
    private var focus: AudioFocusRequest? = null
    private var ticking = false

    /** Any thread, cheap: what should play now (null = silence) and whether to play it softly (pause menu). */
    fun set(tune: Tune?, duck: Boolean) {
        val on = enabled()
        if (tune == lastTune && duck == lastDuck && on == lastOn) return
        lastTune = tune; lastDuck = duck; lastOn = on
        h.post { want = tune; this.duck = duck; apply() }
    }
    @Volatile private var lastTune: Tune? = null
    @Volatile private var lastDuck = false
    @Volatile private var lastOn = false

    /** Activity onPause / onResume. */
    fun pause() = h.post { active = false; apply() }
    fun resume() = h.post { active = true; focusLost = false; apply() }

    fun release() {
        h.post { active = false; want = null; drop(cur); drop(old); cur = null; old = null; abandon(); thread.quitSafely() }
    }

    private fun apply() {
        val shouldPlay = active && !focusLost && enabled()
        val tune = if (shouldPlay) want else null
        if (tune == null) {
            if (!active || !enabled()) stopAll() else fadeOut()
            return
        }
        if (cur?.tune != tune) {
            val pcmData = pcm[tune] ?: render(tune) ?: return
            requestFocus()
            old?.let { drop(it) }
            old = cur?.also { it.target = 0f }
            cur = start(tune, pcmData)
        }
        cur?.target = if (duck) DUCK else 1f
        tick()
    }

    private fun render(tune: Tune): ShortArray? = runCatching {
        Tracker.render(Songs.of(tune)).also {
            pcm[tune] = it
            while (pcm.size > 2) pcm.remove(pcm.keys.first { k -> k != tune && k != cur?.tune })
        }
    }.getOrNull()

    private fun start(tune: Tune, data: ShortArray): Slot? = runCatching {
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(Tracker.RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(data.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        t.write(data, 0, data.size)
        t.setLoopPoints(0, data.size, -1)
        t.setVolume(0f)
        t.play()
        Slot(tune, t)
    }.getOrNull()

    private fun drop(s: Slot?) { s?.let { runCatching { it.track.stop(); it.track.release() } } }

    private fun stopAll() {
        drop(cur); drop(old); cur = null; old = null
        abandon()
    }

    private fun fadeOut() { cur?.target = 0f; old?.target = 0f; tick() }

    private fun tick() {
        if (ticking) return
        ticking = true
        h.post(step)
    }

    private val step = object : Runnable {
        override fun run() {
            val dt = STEP_MS / 1000f / FADE
            var busy = false
            cur?.let { busy = busy or move(it, dt) }
            old?.let { o ->
                busy = busy or move(o, dt)
                if (o.gain <= 0f) { drop(o); old = null }
            }
            cur?.let { if (it.gain <= 0f && it.target <= 0f) { drop(it); cur = null } }
            if (busy) h.postDelayed(this, STEP_MS) else ticking = false
        }
    }

    private fun move(s: Slot, dt: Float): Boolean {
        val d = s.target - s.gain
        s.gain = if (d > 0) minOf(s.target, s.gain + dt) else maxOf(s.target, s.gain - dt)
        runCatching { s.track.setVolume(s.gain * VOLUME) }
        return s.gain != s.target
    }

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        h.post {
            if (change == AudioManager.AUDIOFOCUS_GAIN) { focusLost = false; apply() }
            else if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) { focusLost = true; drop(cur); drop(old); cur = null; old = null }
        }
    }

    private fun requestFocus() {
        val m = am ?: return
        if (focus != null) return
        val r = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener(listener, h).build()
        // a refused request must not silence the game for good: play on either way
        if (m.requestAudioFocus(r) != AudioManager.AUDIOFOCUS_REQUEST_FAILED) focus = r
    }

    private fun abandon() {
        val m = am ?: return
        focus?.let { runCatching { m.abandonAudioFocusRequest(it) } }
        focus = null
    }

    companion object {
        /** Music level; the synthesized SFX are louder so they stay readable. */
        const val VOLUME = 0.3f
        const val DUCK = 0.4f
        private const val FADE = 0.6f
        private const val STEP_MS = 30L
    }
}
