package com.robinrehbein.beveldevil.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Sound
import kotlin.math.exp
import kotlin.random.Random

/** Chiptune sound effects synthesized at startup, so the app ships no audio files. */
class Sfx(private val enabled: () -> Boolean) : Audio {
    private val rate = 22050
    private val tracks = HashMap<Sound, AudioTrack>()

    init {
        runCatching {
            for (s in Sound.entries) tracks[s] = track(synth(s))
        }
    }

    override fun play(sound: Sound) {
        if (!enabled()) return
        val t = tracks[sound] ?: return
        runCatching {
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.stop()
            t.reloadStaticData()
            t.play()
        }
    }

    fun release() = tracks.values.forEach { runCatching { it.release() } }

    private fun track(pcm: ShortArray): AudioTrack {
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        t.write(pcm, 0, pcm.size)
        return t
    }

    private class Voice(val from: Float, val to: Float, val start: Float, val len: Float, val wave: Int, val vol: Float)

    private fun synth(s: Sound): ShortArray {
        val v = when (s) {
            Sound.JUMP -> listOf(Voice(320f, 720f, 0f, 0.11f, SQUARE, 0.35f))
            Sound.LAND -> listOf(Voice(160f, 70f, 0f, 0.05f, NOISE, 0.25f))
            Sound.DIE -> listOf(Voice(600f, 80f, 0f, 0.35f, SQUARE, 0.4f), Voice(0f, 0f, 0f, 0.25f, NOISE, 0.35f))
            Sound.WIN -> listOf(523f, 659f, 784f, 1047f).mapIndexed { i, f -> Voice(f, f, i * 0.08f, 0.14f, SQUARE, 0.3f) }
            Sound.CARD -> listOf(Voice(900f, 250f, 0f, 0.18f, NOISE, 0.25f), Voice(440f, 880f, 0.06f, 0.12f, TRI, 0.3f))
            Sound.LAUGH -> (0..3).map { i -> Voice(330f - i * 30, 260f - i * 30, 0.06f + i * 0.11f, 0.08f, SQUARE, 0.28f) }
            Sound.CLICK -> listOf(Voice(1200f, 900f, 0f, 0.03f, SQUARE, 0.25f))
            Sound.CRASH -> listOf(Voice(120f, 40f, 0f, 0.3f, NOISE, 0.45f))
            Sound.BONK -> listOf(Voice(220f, 180f, 0f, 0.09f, SQUARE, 0.35f))
            Sound.FLIP -> listOf(Voice(200f, 900f, 0f, 0.22f, TRI, 0.35f))
        }
        val total = v.maxOf { it.start + it.len }
        val out = FloatArray((total * rate).toInt() + 1)
        val rnd = Random(s.ordinal)
        for (voice in v) {
            var phase = 0.0
            val n = (voice.len * rate).toInt()
            val off = (voice.start * rate).toInt()
            var noise = 0f
            for (i in 0 until n) {
                val k = i.toFloat() / n
                val f = voice.from + (voice.to - voice.from) * k
                phase += f / rate
                val x = when (voice.wave) {
                    SQUARE -> if (phase % 1.0 < 0.5) 1f else -1f
                    TRI -> (4 * kotlin.math.abs(phase % 1.0 - 0.5) - 1).toFloat()
                    else -> { if (i % 3 == 0) noise = rnd.nextFloat() * 2 - 1; noise }
                }
                val env = (1f - k) * (1f - exp(-i / 60f))
                out[off + i] += x * env * voice.vol
            }
        }
        return ShortArray(out.size) { (out[it].coerceIn(-1f, 1f) * 32000).toInt().toShort() }
    }

    private companion object {
        const val SQUARE = 0
        const val TRI = 1
        const val NOISE = 2
    }
}
