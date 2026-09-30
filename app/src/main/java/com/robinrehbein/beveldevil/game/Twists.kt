package com.robinrehbein.beveldevil.game

/** What a [Action.FakeWin] pretends: the clear screen, or the game's closing credits. */
enum class FakeEnd { CLEAR, CREDITS }

/**
 * What the HUD pause button does. The back button and the app going to the background always pause for real,
 * and the pause screen itself never traps: a tap outside its buttons resumes.
 */
enum class PauseTrick {
    HONEST,
    /** The button hops away from the first [Twists.DODGES] taps, then gives up. */
    DODGE,
    /** The button is a spike: tapping it kills Bevel. */
    SPIKE,
    /** The pause screen's RESUME and LEVELS buttons trade places (labels move with them). */
    SWAP,
}

/** Player positions (box x, y) per simulation tick of one attempt, for the ghost of the next one. */
class Trail {
    private var xy = FloatArray(512)
    var size = 0
        private set

    fun add(x: Float, y: Float) {
        if (2 * size + 2 > xy.size) xy = xy.copyOf(xy.size * 2)
        xy[2 * size] = x
        xy[2 * size + 1] = y
        size++
    }

    fun x(i: Int) = xy[2 * i]
    fun y(i: Int) = xy[2 * i + 1]
}

/** A credits line in tiles: its center x and where it stops (null: it rolls off the top). */
class CreditLine(val text: T, val big: Boolean, val x: Float, val stop: Float?, val start: Float)

object Twists {
    /** Simulation steps per second (the game loop is fixed at 120 Hz). */
    const val HZ = 120
    const val DODGES = 3
    /** Seconds the dodging pause button stays away before it sneaks back. */
    const val DODGE_TIME = 0.9f
    /** Seconds from touching the door to the fake screen, as for a real win. */
    const val FAKE_DELAY = 0.8f
    const val FAKE_CLEAR = 2.6f
    /** After the fake: the door can't be entered for a moment, while Bevel is spat out. */
    const val DOOR_LOCK = 0.6f
    const val TURN = 0.35f
    const val CREDIT_SPEED = 6f
    const val CREDIT_GAP = 0.55f
    const val CREDIT_HOLD = 0.7f

    val nope = T("Nope.", "Nee.")
    val dodgeLines = listOf(T("Missed.", "Daneben."), T("Too slow.", "Zu langsam."), T("Fine. Pause, then.", "Na gut. Pausier halt."))
    val credits = listOf(
        T("THANKS FOR PLAYING", "DANKE FÜRS SPIELEN"),
        T("BEVEL DEVIL", "BEVEL DEVIL"),
        T("GAME DESIGN: MEPHI", "SPIELDESIGN: MEPHI"),
        T("TRAPS: MEPHI", "FALLEN: MEPHI"),
        T("QA: YOU (%d DEATHS)", "TESTER: DU (%d TODE)"),
        T("THE END", "ENDE"),
        T("(FOR REAL.)", "(WIRKLICH.)"),
    )

    /**
     * Lays out the credits roll: lines enter below the level one after another and roll up. The last lines stop
     * on the rows of [platforms] (top to bottom), centered over that row's pieces; the others roll off the top.
     */
    fun creditLines(pieces: List<Piece>, platforms: Char?, cols: Int): List<CreditLine> {
        val rows = pieces.filter { !it.spike && it.group != null && it.group.id == platforms }.groupBy { it.hy.toInt() }.toSortedMap()
        val stops = rows.entries.toList().takeLast(credits.size)
        val first = credits.size - stops.size
        return credits.mapIndexed { i, t ->
            val row = stops.getOrNull(i - first)
            val x = row?.value?.let { ps -> (ps.minOf { it.hx } + ps.maxOf { it.hx } + 1f) / 2f } ?: cols / 2f
            CreditLine(t, i == 0, x, row?.key?.plus(0.5f), i * CREDIT_GAP)
        }
    }

    /** Center y (tiles) of [line] at [age] seconds into the roll, starting [rows] + 1 below the top. */
    fun creditY(line: CreditLine, age: Float, rows: Int): Float {
        val y = rows + 1f - (age - line.start).coerceAtLeast(0f) * CREDIT_SPEED
        return if (line.stop != null) maxOf(y, line.stop) else y
    }

    fun creditsLength(lines: List<CreditLine>, rows: Int) = lines.maxOf { l ->
        l.start + (rows + 1f - (l.stop ?: -2f)) / CREDIT_SPEED
    } + CREDIT_HOLD
}
