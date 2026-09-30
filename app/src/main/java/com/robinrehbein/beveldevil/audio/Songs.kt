package com.robinrehbein.beveldevil.audio

import com.robinrehbein.beveldevil.game.Tune

/**
 * The songs, composed as note patterns for the [Tracker]. Every channel lists one string per bar, 16 tokens (16th notes) each.
 * To tweak: change `bpm`, a chord progression string ("Am F Dm E"), a pattern template or a channel's volume, then run
 * `MusicTest` (it exports the WAVs). Template digits 0..5 pick chord tones (0 root, 1 third, 2 fifth, 3/4/5 the same an octave up),
 * `c` plays the whole triad, `.` holds, `-` releases; drums use `x` / `X`.
 */
object Songs {
    private val cache = HashMap<Tune, Song>()

    fun of(t: Tune): Song = cache.getOrPut(t) {
        when (t) {
            Tune.TITLE -> title()
            Tune.WORLD1 -> world1()
            Tune.WORLD2 -> world2()
            Tune.WORLD3 -> world3()
        }
    }

    private const val Q = "- . . . . . . . . . . . . . . ."
    private val PC = mapOf("C" to 0, "C#" to 1, "D" to 2, "Eb" to 3, "E" to 4, "F" to 5, "F#" to 6, "G" to 7, "Ab" to 8, "A" to 9, "Bb" to 10, "B" to 11)
    private val NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    private fun nm(m: Int) = NAMES[m % 12] + (m / 12 - 1)
    private fun bars(n: Int, b: String) = List(n) { b }
    private fun words(s: String) = s.trim().split(Regex("\\s+"))
    private fun hold(note: String) = note + " .".repeat(15)

    /** Chord tones [root, third, fifth, root+12, third+12, fifth+12] of "Am", "F", "Bb" ... at octave [oct]. */
    private fun tones(chord: String, oct: Int): List<Int> {
        val minor = chord.endsWith("m")
        val r = 12 * (oct + 1) + PC.getValue(if (minor) chord.dropLast(1) else chord)
        val t = r + if (minor) 3 else 4
        return listOf(r, t, r + 7, r + 12, t + 12, r + 19)
    }

    /** One bar per chord, filled from a 16 token template. */
    private fun gen(chords: String, tpl: String, oct: Int): List<String> = words(chords).map { c ->
        val t = tones(c, oct)
        words(tpl).joinToString(" ") { tk ->
            when {
                tk == "c" -> t.take(3).joinToString("+") { nm(it) }
                tk[0].isDigit() -> nm(t[tk.toInt()])
                else -> tk
            }
        }
    }

    /** Template turned [k] steps to the right, for echo voices. */
    private fun rot(tpl: String, k: Int) = words(tpl).let { (it.takeLast(k) + it.dropLast(k)).joinToString(" ") }

    // --- the melody of World 1, shared with the title theme ---
    private val melodyA = listOf(
        "A4 . . C5 . . E5 . . D5 C5 . B4 . - .",
        "A4 . . - A4 B4 C5 . E5 . . . . . - .",
        "F5 . . E5 . . D5 . C5 . . A4 . . - .",
        "G#4 . B4 . D5 . E5 . G#5 . . . F5 E5 D5 B4",
        "E5 . E5 . E5 D5 C5 . B4 . A4 . B4 . C5 .",
        "A4 . . . - . A4 C5 E5 . D5 . C5 . B4 .",
        "D5 . F5 . A5 . . . G5 . F5 . D5 . - .",
        "E5 . . G#5 . . B5 . . . A5 . . G#5 . .",
    )
    private const val CHORDS_A = "Am Am F E Am Am Dm E"

    private val harpsi = Adsr(0.002f, 0.15f, 0.2f, 0.08f)

    /** World 1, Hell's Cellar: A minor, cheeky bass, harpsichord lead, organ stabs. 16 bars at 112 bpm. */
    private fun world1(): Song {
        val chordsB = "Dm Dm Am Am F G E E"
        val lead = melodyA + gen("Dm Dm Am Am F G", "0 . 1 . 2 . 1 . 0 . 1 . 2 . 4 .", 4) +
            listOf("B4 . . . E5 . . . G#5 . . . B5 . . .", "A5 . G#5 . F5 . E5 . D5 . B4 . G#4 . - .")
        val bass = gen(CHORDS_A, "0 . - 0 . - 2 . 3 . - 2 . - 1 .", 2) + gen(chordsB, "0 . 0 . 3 . 0 . 0 . 0 . 3 . 2 .", 2)
        val organ = gen("$CHORDS_A $chordsB", "- - c . - - c . - - c . - - c .", 3)
        val kick = "X . . . . . . . x . . x . . . ."
        val hat = "x . x . x . x . x . x . x . x X"
        return Song(
            112, listOf(
                Channel(Wave.PULSE, lead, 0.5f, harpsi, duty = 0.25f),
                Channel(Wave.PULSE, bass, 0.55f, Adsr(0.002f, 0.06f, 0.7f, 0.04f), duty = 0.25f),
                Channel(Wave.PULSE, organ, 0.2f, Adsr(0.01f, 0.05f, 0.8f, 0.08f), duty = 0.5f, vibrato = 0.15f),
                Channel(Wave.KICK, bars(2, Q) + bars(14, kick), 0.8f),
                Channel(Wave.SNARE, bars(2, Q) + bars(14, "- . . . x . . . - . . . x . . ."), 0.35f),
                Channel(Wave.HAT, bars(16, hat), 0.3f),
            ),
        )
    }

    /** Title / menu theme: the World 1 tune, slower and softer. 8 bars at 92 bpm. */
    private fun title(): Song = Song(
        92, listOf(
            Channel(Wave.PULSE, melodyA, 0.45f, harpsi, duty = 0.25f),
            Channel(Wave.TRI, gen(CHORDS_A, "0 . . . . . . . 2 . . . . . 3 .", 3), 0.5f, Adsr(0.005f, 0.1f, 0.8f, 0.1f)),
            Channel(Wave.PULSE, gen(CHORDS_A, "c . . . . . . . . . . . . . . .", 3), 0.14f, Adsr(0.25f, 0f, 1f, 0.25f), vibrato = 0.2f, lp = 0.4f),
            Channel(Wave.HAT, bars(8, "x . . . x . . . x . . . x . . ."), 0.2f),
        ),
    )

    /** World 2, Server Farm: D minor techno, 16th arpeggios, server hum, a modem handshake at the section ends. 24 bars at 132 bpm. */
    private fun world2(): Song {
        val p = "Dm Dm Bb C Dm Dm Bb A"
        val modem = "D6 C#7 D6 D6 C#7 C#7 D6 C#7 A6 D6 C#7 C#7 D6 A6 C#7 D6"
        val humBar = hold("D2")
        val arp8 = gen(p, "0 . 1 . 2 . 4 . 2 . 1 . 2 . 4 .", 4)
        val arp16 = gen(p, "0 1 2 4 2 1 0 1 2 4 5 4 2 1 2 4", 4)
        val bass = gen(p, "- . 0 . - . 0 . - . 0 . - . 0 .", 2)
        val lead = gen(p, "2 . . . . . 4 . . . 5 . 4 . 2 .", 4)
        val floor = "X . . . X . . . X . . . X . . ."
        return Song(
            132, listOf(
                Channel(Wave.TRI, bars(24, humBar), 0.35f, Adsr(0.3f, 0f, 1f, 0.3f), tremolo = 0.3f),
                Channel(Wave.NOISE, bars(24, humBar), 3f, Adsr(0.3f, 0f, 1f, 0.3f), tremolo = 0.2f, lp = 0.03f),
                Channel(Wave.KICK, bars(2, Q) + bars(22, floor), 0.85f),
                Channel(Wave.HAT, bars(24, "- . x . - . x . - . x . - . x ."), 0.3f),
                Channel(Wave.SNARE, bars(8, Q) + bars(16, "- . . . x . . . - . . . x . . ."), 0.3f),
                Channel(Wave.PULSE, arp8 + arp16 + arp16, 0.28f, Adsr(0.002f, 0.05f, 0.5f, 0.03f), duty = 0.25f, pwm = 0.15f),
                Channel(Wave.SAW, bars(8, Q) + bass + bass, 0.5f, Adsr(0.002f, 0.05f, 0.6f, 0.03f), lp = 0.35f),
                Channel(Wave.PULSE, bars(16, Q) + lead, 0.26f, Adsr(0.01f, 0.1f, 0.7f, 0.1f), vibrato = 0.2f),
                Channel(Wave.PULSE, bars(7, Q) + modem + bars(15, Q) + modem, 0.1f, Adsr(0.001f, 0f, 1f, 0.002f)),
            ),
        )
    }

    /** World 3, Circuit Board: E minor, cool bleeps with echo, fan noise, BIOS POST beeps, a hot finale. 16 bars at 120 bpm. */
    private fun world3(): Song {
        val p = "Em C G D Em C G B Am Am C D Em C B B"
        val arpT = "0 . 2 . 4 . 2 . 1 . 2 . 5 . 4 ."
        val post = "B5 . . . . . . . - . B5 . - . B5 ."
        val beeps = List(16) { i -> if (i == 15) "B5+B6" + " .".repeat(14) + " -" else if (i % 4 == 0 || i == 14) post else Q }
        val lead = gen(p, "4 . . . . . . . 5 . . . 4 . . .", 4)
        val quick = Adsr(0.001f, 0.04f, 0.3f, 0.02f)
        return Song(
            120, listOf(
                Channel(Wave.NOISE, bars(16, hold("E2")), 3f, Adsr(0.3f, 0f, 1f, 0.3f), tremolo = 0.25f, lp = 0.05f),
                Channel(Wave.TRI, gen(p, "0 . 0 . 0 . 3 . 0 . 0 . 3 . 2 .", 3), 0.6f, Adsr(0.003f, 0.05f, 0.7f, 0.03f)),
                Channel(Wave.PULSE, gen(p, arpT, 5), 0.27f, quick, duty = 0.125f),
                Channel(Wave.PULSE, gen(p, rot(arpT, 3), 5), 0.12f, quick, duty = 0.125f),
                Channel(Wave.PULSE, beeps, 0.22f, Adsr(0.001f, 0f, 1f, 0.005f)),
                Channel(Wave.KICK, bars(4, Q) + bars(4, "X . . . . . . . X . . . . . . .") + bars(8, "X . . . X . . . X . . . X . . ."), 0.8f),
                Channel(Wave.HAT, bars(4, "x . . . x . . . x . . . x . . .") + bars(12, "x . x . x . x . x . x . x . x x"), 0.25f),
                Channel(Wave.SNARE, bars(8, Q) + bars(8, "- . . . x . . . - . . . x . . ."), 0.3f),
                Channel(Wave.PULSE, bars(8, Q) + lead.drop(8), 0.22f, Adsr(0.02f, 0.1f, 0.8f, 0.15f), duty = 0.25f, vibrato = 0.3f),
            ),
        )
    }
}
