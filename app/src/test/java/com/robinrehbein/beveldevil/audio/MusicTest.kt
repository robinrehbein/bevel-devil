package com.robinrehbein.beveldevil.audio

import com.robinrehbein.beveldevil.game.Tune
import java.io.File
import kotlin.math.abs
import org.junit.Test
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class MusicTest {
    private val pcm = Tune.entries.associateWith { Tracker.render(Songs.of(it)) }

    @Test fun lengthMatchesPatterns() {
        for ((t, p) in pcm) {
            val song = Songs.of(t)
            val expected = Math.round(song.bars * 16 * 60.0 / song.bpm / 4.0 * Tracker.RATE).toInt()
            assertEquals("$t", expected, p.size)
        }
    }

    @Test fun loopLengths() {
        for (t in Tune.entries) {
            val s = Tracker.seconds(Songs.of(t))
            if (t == Tune.TITLE) assertTrue("$t $s", s in 15f..30f) else assertTrue("$t $s", s in 30f..60f)
        }
    }

    @Test fun noClippingAndNotSilent() {
        for ((t, p) in pcm) {
            val peak = p.maxOf { abs(it.toInt()) }
            assertTrue("$t clips", peak < 32767)
            assertTrue("$t too quiet: $peak", peak > 16000)
            val rms = Math.sqrt(p.sumOf { it.toDouble() * it } / p.size)
            assertTrue("$t rms $rms", rms > 1500)
        }
    }

    @Test fun loopIsSeamless() {
        for ((t, p) in pcm) {
            val seam = abs(p[0] - p.last().toInt())
            assertTrue("$t seam jump $seam", seam < 6000)
        }
    }

    @Test fun deterministic() {
        for (t in Tune.entries) assertArrayEquals(pcm.getValue(t), Tracker.render(Songs.of(t)))
    }

    @Test fun parsesNotes() {
        assertEquals(69, Tracker.midi("A4"))
        assertEquals(61, Tracker.midi("C#4"))
        assertEquals(58, Tracker.midi("Bb3"))
    }

    /** Exports the loops for listening: build/music. */
    @Test fun exportWavs() {
        val dirs = listOf(File("build/music"))
        for (dir in dirs) {
            if (!dir.isDirectory && !dir.mkdirs()) continue
            for ((t, p) in pcm) File(dir, "${t.name.lowercase()}.wav").writeBytes(Tracker.wav(p))
        }
    }
}
