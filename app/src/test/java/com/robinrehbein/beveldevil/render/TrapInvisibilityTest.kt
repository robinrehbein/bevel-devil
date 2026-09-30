package com.robinrehbein.beveldevil.render

import android.graphics.Color
import com.robinrehbein.beveldevil.game.Action
import com.robinrehbein.beveldevil.game.Controls
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.GroupMode
import com.robinrehbein.beveldevil.game.Level
import com.robinrehbein.beveldevil.game.Levels
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
    private fun plain(level: Level) = Level(level.name, level.intro) {
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

    private fun checkTraps(range: IntRange): Int {
        var checked = 0
        for (i in range) {
            val level = Levels.all[i]
            val theme = Themes.of(Worlds.of(i).number)
            val trapWorld = World(level)
            if (trapWorld.groups.isEmpty()) continue
            // blinking and tilting groups are honest mechanics: they show what they do from the first frame
            if (level.start.any { it is Action.Blink || it is Action.Tilt }) continue
            val plainWorld = World(plain(level))
            assertTrue(plainWorld.groups.isEmpty())
            for (t in floatArrayOf(0.37f, 2.9f, 5.55f)) {
                diff(render(trapWorld, t, theme), render(plainWorld, t, theme))?.let { throw AssertionError("Level ${i + 1} (${level.name.en}) at t=$t: $it") }
            }
            checked++
        }
        return checked
    }

    @Test
    fun crackingFrameLooksLikeTheFrameUntilItFires() {
        val level = TwistDemos.crack
        for (t in floatArrayOf(0.37f, 2.9f)) diff(render(World(level), t), render(World(plain(level)), t))?.let { throw AssertionError("t=$t: $it") }
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
