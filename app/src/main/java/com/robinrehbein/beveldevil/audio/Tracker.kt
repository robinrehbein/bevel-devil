package com.robinrehbein.beveldevil.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** A tiny tracker: patterns of notes per channel, rendered once to mono 16-bit PCM. Pure Kotlin, deterministic. */
enum class Wave { PULSE, TRI, SAW, NOISE, KICK, SNARE, HAT }

/** Attack / decay (seconds), sustain level, release (seconds). */
class Adsr(val a: Float, val d: Float, val s: Float, val r: Float) {
    /** Level while the gate is open, [t] seconds after note-on. */
    fun held(t: Float): Float = when {
        t < a -> if (a <= 0f) 1f else t / a
        t < a + d -> 1f + (s - 1f) * ((t - a) / d)
        else -> s
    }
}

/**
 * One voice. [bars] holds one string per bar with exactly [Song.STEPS] whitespace separated tokens (16th notes):
 * `C#4` / `Bb2` = note on (chords: `A3+C4+E4`), `.` = hold, `-` = release, `x` / `X` = drum hit (soft / accented).
 */
class Channel(
    val wave: Wave,
    val bars: List<String>,
    val vol: Float,
    val adsr: Adsr = Adsr(0.003f, 0.08f, 0.6f, 0.05f),
    val duty: Float = 0.5f,
    /** Pulse width sweep amount (0..0.4) over a 4 second cycle. */
    val pwm: Float = 0f,
    /** Vibrato depth in semitones. */
    val vibrato: Float = 0f,
    val tremolo: Float = 0f,
    /** Pitch glide in octaves per second. */
    val glide: Float = 0f,
    /** One-pole low-pass coefficient (1 = off, smaller = darker). */
    val lp: Float = 1f,
)

class Song(val bpm: Int, val channels: List<Channel>) {
    val bars get() = channels.first().bars.size

    companion object { const val STEPS = 16 }
}

object Tracker {
    const val RATE = 22050
    private const val PEAK = 0.85f

    fun loopSamples(song: Song) = stepStart(song, song.bars * Song.STEPS)
    fun seconds(song: Song) = loopSamples(song).toFloat() / RATE

    private fun stepStart(song: Song, step: Int) = (step * 60.0 / song.bpm / 4.0 * RATE).roundToInt()

    fun midiFreq(m: Int) = 440.0 * 2.0.pow((m - 69) / 12.0)

    /** `C#4`, `Bb2` ... to a MIDI number. */
    fun midi(name: String): Int {
        val base = when (name[0]) { 'C' -> 0; 'D' -> 2; 'E' -> 4; 'F' -> 5; 'G' -> 7; 'A' -> 9; 'B' -> 11; else -> error("bad note $name") }
        var i = 1
        var acc = 0
        while (name[i] == '#' || name[i] == 'b') { acc += if (name[i] == '#') 1 else -1; i++ }
        return 12 * (name.substring(i).toInt() + 1) + base + acc
    }

    private class Ev(val start: Int, val end: Int, val token: String)

    private fun events(song: Song, ch: Channel): List<Ev> {
        require(ch.bars.size == song.bars) { "all channels need ${song.bars} bars" }
        val tokens = ch.bars.flatMap { b ->
            b.trim().split(Regex("\\s+")).also { require(it.size == Song.STEPS) { "bar needs ${Song.STEPS} steps: '$b'" } }
        }
        val out = ArrayList<Ev>()
        var cur = -1
        var tok = ""
        fun close(end: Int) { if (cur >= 0) out += Ev(cur, end, tok); cur = -1 }
        tokens.forEachIndexed { i, t ->
            when (t) {
                "." -> {}
                "-" -> close(i)
                else -> { close(i); cur = i; tok = t }
            }
        }
        close(tokens.size)
        return out
    }

    /** Renders one seamless loop; note tails that run past the end wrap around to the start. */
    fun render(song: Song): ShortArray {
        val n = loopSamples(song)
        val mix = FloatArray(n)
        song.channels.forEachIndexed { ci, ch ->
            val buf = FloatArray(n)
            val rnd = Rng(0x9E3779B9.toInt() xor (ci * 7919 + 1))
            for (e in events(song, ch)) {
                val s0 = stepStart(song, e.start)
                val gate = (stepStart(song, e.end) - s0) / RATE.toFloat()
                when (ch.wave) {
                    Wave.KICK, Wave.SNARE, Wave.HAT -> drum(buf, s0, ch, e.token == "X", rnd)
                    else -> for (note in e.token.split('+')) tone(buf, s0, gate, ch, midiFreq(midi(note)), rnd)
                }
            }
            if (ch.lp < 1f) lowpass(buf, ch.lp)
            for (i in 0 until n) mix[i] += buf[i] * ch.vol
        }
        var peak = 0f
        for (v in mix) peak = maxOf(peak, abs(v))
        val g = if (peak > 0f) PEAK / peak else 1f
        return ShortArray(n) { (mix[it] * g * 32767f).roundToInt().coerceIn(-32767, 32767).toShort() }
    }

    private class Rng(var s: Int) {
        fun next(): Float {
            s = s xor (s shl 13); s = s xor (s ushr 17); s = s xor (s shl 5)
            return (s and 0xFFFF) / 32768f - 1f
        }
    }

    private fun lowpass(b: FloatArray, k: Float) {
        var y = 0f
        for (pass in 0..1) for (i in b.indices) { y += k * (b[i] - y); b[i] = y } // second pass starts warm: seamless wrap
    }

    private fun tone(buf: FloatArray, s0: Int, gate: Float, ch: Channel, f0: Double, rnd: Rng) {
        val ad = ch.adsr
        val len = ((gate + ad.r) * RATE).toInt()
        val endLevel = ad.held(gate)
        var phase = (f0 * s0 / RATE) % 1.0
        var hold = 0f
        for (i in 0 until len) {
            val t = i.toFloat() / RATE
            val env = if (t < gate) ad.held(t) else endLevel * (1f - (t - gate) / ad.r).coerceAtLeast(0f)
            var f = f0
            if (ch.glide != 0f) f *= 2.0.pow((ch.glide * t).toDouble())
            val abst = (s0 + i).toDouble() / RATE
            if (ch.vibrato != 0f) f *= 2.0.pow(ch.vibrato * sin(2 * PI * 5.5 * abst) / 12.0)
            phase += f / RATE
            val p = phase % 1.0
            val x = when (ch.wave) {
                Wave.PULSE -> {
                    val d = (ch.duty + ch.pwm * sin(2 * PI * abst / 4.0)).coerceIn(0.05, 0.95)
                    if (p < d) 1f else -1f
                }
                Wave.TRI -> (4 * abs(p - 0.5) - 1).toFloat()
                Wave.SAW -> (2 * p - 1).toFloat()
                else -> { if (i % 2 == 0) hold = rnd.next(); hold }
            }
            val trem = if (ch.tremolo != 0f) 1f - ch.tremolo * (0.5f + 0.5f * sin(2 * PI * 3.0 * abst).toFloat()) else 1f
            buf[(s0 + i) % buf.size] += x * env * trem
        }
    }

    private fun drum(buf: FloatArray, s0: Int, ch: Channel, accent: Boolean, rnd: Rng) {
        val amp = if (accent) 1f else 0.7f
        val secs = when (ch.wave) { Wave.KICK -> 0.25f; Wave.SNARE -> 0.2f; else -> if (accent) 0.12f else 0.06f }
        val len = (secs * RATE).toInt()
        var phase = 0.0
        var prev = 0f
        for (i in 0 until len) {
            val t = i.toFloat() / RATE
            val x = when (ch.wave) {
                Wave.KICK -> {
                    phase += (45.0 + 120.0 * exp(-t / 0.035)) / RATE
                    (sin(2 * PI * phase) * exp(-t / 0.08)).toFloat()
                }
                Wave.SNARE -> {
                    phase += 190.0 / RATE
                    rnd.next() * exp(-t / 0.05f) * 0.8f + (4 * abs(phase % 1.0 - 0.5) - 1).toFloat() * exp(-t / 0.03f) * 0.4f
                }
                else -> {
                    val r = rnd.next()
                    val h = r - prev
                    prev = r
                    h * 0.5f * exp(-t / (if (accent) 0.035f else 0.015f))
                }
            }
            buf[(s0 + i) % buf.size] += x * amp
        }
    }

    /** Minimal 16-bit mono PCM WAV. */
    fun wav(pcm: ShortArray): ByteArray {
        val data = pcm.size * 2
        val b = java.nio.ByteBuffer.allocate(44 + data).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        b.put("RIFF".toByteArray()).putInt(36 + data).put("WAVEfmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(RATE).putInt(RATE * 2).putShort(2).putShort(16).put("data".toByteArray()).putInt(data)
        pcm.forEach { b.putShort(it) }
        return b.array()
    }
}
