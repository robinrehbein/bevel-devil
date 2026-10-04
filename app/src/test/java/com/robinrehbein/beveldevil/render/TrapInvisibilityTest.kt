package com.robinrehbein.beveldevil.render

import android.graphics.Color
import com.robinrehbein.beveldevil.game.Action
import com.robinrehbein.beveldevil.game.Controls
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.GroupMode
import com.robinrehbein.beveldevil.game.HardwareDemos
import com.robinrehbein.beveldevil.game.Level
import com.robinrehbein.beveldevil.game.Levels
import com.robinrehbein.beveldevil.game.ROOM_COLS
import com.robinrehbein.beveldevil.game.ROOM_ROWS
import com.robinrehbein.beveldevil.game.RoomDemos
import com.robinrehbein.beveldevil.game.Trigger
import com.robinrehbein.beveldevil.game.TwistDemos
import com.robinrehbein.beveldevil.game.World
import com.robinrehbein.beveldevil.game.Worlds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Traps must not give themselves away: until a trap fires, a level renders pixel-identically to the same
 * level with every trap group turned into plain geometry (blocks → '#', spikes → '^v<>', hidden → nothing).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class TrapInvisibilityTest {
    private val layout = Layout().apply { update(2400, 1080, 2.75f) }

    /** The level as a player sees it before anything happens: no groups, no legend, no traps. */
    private fun plain(level: Level) = Level(level.name, level.intro, rooms = level.rooms) {
        for (y in 0 until level.rows) for (x in 0 until level.cols) {
            val c = level.map.grid[y][x]
            val g = if (c == '#' || c in "^v<>PD.") null else level.glyph(c)
            put(x, y, when {
                g == null -> c
                g.hidden -> '.'
                !g.spike -> '#'
                else -> when (g.dir) { Dir.UP -> '^'; Dir.DOWN -> 'v'; Dir.LEFT -> '<'; Dir.RIGHT -> '>' }
            })
        }
    }

    private fun render(w: World, t: Float, theme: Theme = Themes.HELL): IntArray {
        val px = Pixels(RuntimeEnvironment.getApplication())
        px.resize(layout.lw, layout.lh)
        px.lo.eraseColor(Color.BLACK)
        WorldPainter(px).draw(w, t, 0f, emptyList(), layout, theme)
        val out = IntArray(layout.lw * layout.lh)
        px.lo.getPixels(out, 0, layout.lw, 0, 0, layout.lw, layout.lh)
        return out
    }

    private fun diff(a: IntArray, b: IntArray): String? {
        val i = a.indices.firstOrNull { a[it] != b[it] } ?: return null
        val x = i % layout.lw - layout.fx
        val y = i / layout.lw - layout.fy
        return "first difference at playfield pixel ($x, $y), tile (${Math.floorDiv(x, TS)}, ${Math.floorDiv(y, TS)})"
    }

    @Test
    fun trapsLookLikePlainLevelUntilTheyFire() {
        val checked = checkTraps(0 until Worlds.get(2).firstLevel)
        // collapse, hidden spikes, headbutt, bonk block, sinking, decoy, crumble, finale
        assertTrue("checked $checked levels", checked >= 8)
    }

    @Test
    fun trapsLookLikePlainLevelInTheDataCenterToo() {
        val w2 = Worlds.get(2)
        val checked = checkTraps(w2.firstLevel until w2.firstLevel + w2.size)
        assertTrue("checked $checked levels", checked >= 8)
    }

    @Test
    fun trapsLookLikePlainLevelOnBothCircuitBoards() {
        // World 3 has no levels of its own yet: the earlier worlds' trap levels stand in, dressed as a green and a blue board
        for (theme in listOf(Themes.PCB_GREEN, Themes.PCB_BLUE)) {
            val checked = checkTraps(0 until Worlds.get(2).firstLevel + 10, theme)
            assertTrue("checked $checked levels", checked >= 8)
        }
    }

    @Test
    fun actsPickTheirBoard() {
        assertTrue(Themes.of(3, 1) === Themes.PCB_GREEN)
        assertTrue(Themes.of(3, 32) === Themes.PCB_GREEN)
        assertTrue(Themes.of(3, 33) === Themes.PCB_BLUE)
        assertTrue(Themes.of(3, 48) === Themes.PCB_BLUE)
        assertTrue(Themes.of(2, 40) === Themes.DATA_CENTER)
        assertTrue(Themes.forLevel(0) === Themes.HELL)
    }

    /** Hardware in plain sight: copper circuits, pads, hot plates, heatsinks and fans (see [hardwareTrapsLookHarmlessUntilTheyFire]). */
    private fun hardware(a: Action) = a is Action.Circuit || a is Action.Clock || a is Action.Pad || a is Action.Heat || a is Action.Heatsink || a is Action.Fan

    private fun checkTraps(range: IntRange, fixed: Theme? = null): Int {
        var checked = 0
        for (i in range) for ((k, level) in Levels.all[i].rounds.withIndex()) {
            val theme = fixed ?: Themes.forLevel(i)
            val trapWorld = World(level)
            if (trapWorld.groups.isEmpty()) continue
            // blinking, tilting and network gear (belts, lasers, portals) are honest mechanics: they show what they do from the first frame
            if (level.start.any { it is Action.Blink || it is Action.Tilt || it is Action.Belt || it is Action.Laser || it is Action.Portal || hardware(it) }) continue
            val plainWorld = World(plain(level))
            assertTrue(plainWorld.groups.isEmpty())
            for (t in floatArrayOf(0.37f, 2.9f, 5.55f)) for (room in 0 until trapWorld.rooms) {
                trapWorld.showRoom(room); plainWorld.showRoom(room)
                diff(render(trapWorld, t, theme), render(plainWorld, t, theme))?.let { throw AssertionError("Level ${i + 1} round ${k + 1} (${level.name.en}) room ${room + 1} at t=$t: $it") }
            }
            checked++
        }
        return checked
    }

    /** The wall Mephi breaks open ([Action.Extend]) is plain wall in every room until he does, and the rooms render like one-room levels. */
    @Test
    fun theBreachLooksLikeTheWallUntilItFires() {
        for (level in RoomDemos.all) for (theme in listOf(Themes.HELL, Themes.DATA_CENTER, Themes.PCB_GREEN)) {
            val a = World(level)
            // the same level without its traps (saws and belts run as before)
            val b = World(Level(level.name, level.intro, level.legend, emptyList(), level.start, rooms = level.rooms) {
                for (y in 0 until level.rows) for (x in 0 until level.cols) put(x, y, level.map.grid[y][x])
            })
            assertTrue(a.groups.isNotEmpty())
            for (room in 0 until a.rooms) {
                a.showRoom(room); b.showRoom(room)
                for (t in floatArrayOf(0.37f, 2.9f)) diff(render(a, t, theme), render(b, t, theme))?.let { throw AssertionError("${level.name.en} room ${room + 1} t=$t: $it") }
            }
        }
        // room 1 of a two-room level looks exactly like the same room built as a level of its own
        val wide = plain(RoomDemos.annex)
        val alone = Level(wide.name, wide.intro) { for (y in 0 until ROOM_ROWS) for (x in 0 until ROOM_COLS) put(x, y, wide.map.grid[y][x]) }
        diff(render(World(wide), 0.37f), render(World(alone), 0.37f))?.let { throw AssertionError("room 1 alone: $it") }
    }

    @Test
    fun theBreachShowsOnceItFires() {
        val w = World(RoomDemos.annex)
        val input = Controls().apply { right = true }
        // to the door, hopping the block: the wall cracks, then is gone
        while (w.cracks.isEmpty() && w.time < 10f) {
            input.jump = w.player.box.cx in 12.3f..14.5f
            input.jumpPressed = input.jump && w.player.grounded
            w.step(1f / 120f, input)
        }
        assertTrue(w.cracks.isNotEmpty())
        val before = render(World(plain(RoomDemos.annex)), w.time)
        while (w.cracks[0].group.visible && w.time < 12f) w.step(1f / 120f, Controls())
        val after = render(w, w.time)
        var changed = 0
        for (y in 12 * TS until 15 * TS) for (x in 30 * TS until 32 * TS) {
            val i = (layout.fy + y) * layout.lw + layout.fx + x
            if (after[i] != before[i]) changed++
        }
        assertTrue("open wall must be visible ($changed px)", changed > 150)
    }

    @Test
    fun crackingFrameLooksLikeTheFrameUntilItFires() {
        val level = TwistDemos.crack
        for (t in floatArrayOf(0.37f, 2.9f)) diff(render(World(level), t), render(World(plain(level)), t))?.let { throw AssertionError("t=$t: $it") }
    }

    /**
     * Hardware traps (a rail about to lose power, a fan about to reverse, floor about to be overclocked) look exactly
     * like the same level without its traps, with circuits, clocks, fans and heat running, until they fire.
     */
    @Test
    fun hardwareTrapsLookHarmlessUntilTheyFire() {
        var checked = 0
        for (level in HardwareDemos.all + Worlds.get(3).levels) {
            if (level.traps.isEmpty()) continue
            // traps that fire on their own while nobody moves are not surprises of this kind
            if (level.traps.any { it.trigger is Trigger.After || it.trigger is Trigger.Idle || it.trigger is Trigger.Heated }) continue
            val harmless = Level(level.name, level.intro, level.legend, emptyList(), level.start) {
                for (y in 0 until level.rows) for (x in 0 until level.cols) put(x, y, level.map.grid[y][x])
            }
            val a = World(level)
            val b = World(harmless)
            for (t in floatArrayOf(0.37f, 1.6f, 2.9f)) {
                while (a.time < t) { a.step(1f / 120f, Controls()); b.step(1f / 120f, Controls()) }
                for (theme in listOf(Themes.PCB_GREEN, Themes.PCB_BLUE)) {
                    diff(render(a, t, theme), render(b, t, theme))?.let { throw AssertionError("${level.name.en} at t=$t: $it") }
                }
            }
            checked++
        }
        assertTrue("checked $checked levels", checked >= 3)
        // an overclocked floor is plain floor until then
        val oc = HardwareDemos.overclock
        for (t in floatArrayOf(0.37f, 2.9f)) for (theme in listOf(Themes.PCB_GREEN, Themes.PCB_BLUE)) {
            diff(render(World(oc), t, theme), render(World(plain(oc)), t, theme))?.let { throw AssertionError("t=$t: $it") }
        }
    }

    @Test
    fun aFiredTrapDoesShow() {
        val level = Levels.all[0]
        val w = World(level)
        val input = Controls().apply { right = true }
        while (w.group('a').mode != GroupMode.FALL && w.time < 10f) w.step(1f / 120f, input)
        repeat(12) { w.step(1f / 120f, input) }
        assertEquals(GroupMode.FALL, w.group('a').mode)
        val a = render(w, 1f)
        val b = render(World(plain(level)), 1f)
        // the collapsing floor (tiles 19..21 × 15..17) and its seams now differ from solid ground
        var changed = 0
        for (y in 15 * TS until 18 * TS) for (x in 18 * TS until 23 * TS) {
            val i = (layout.fy + y) * layout.lw + layout.fx + x
            if (a[i] != b[i]) changed++
        }
        assertTrue("falling floor must be visible ($changed px)", changed > 100)
    }
}
