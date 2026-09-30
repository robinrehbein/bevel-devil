package com.robinrehbein.beveldevil.render

import android.graphics.Color
import com.robinrehbein.beveldevil.game.Controls
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.GroupMode
import com.robinrehbein.beveldevil.game.Level
import com.robinrehbein.beveldevil.game.Levels
import com.robinrehbein.beveldevil.game.TwistDemos
import com.robinrehbein.beveldevil.game.World
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

    private fun render(w: World, t: Float): IntArray {
        val px = Pixels(RuntimeEnvironment.getApplication())
        px.resize(layout.lw, layout.lh)
        px.lo.eraseColor(Color.BLACK)
        WorldPainter(px).draw(w, t, 0f, emptyList(), layout)
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
        var checked = 0
        for ((i, level) in Levels.all.withIndex()) {
            val trapWorld = World(level)
            if (trapWorld.groups.isEmpty()) continue
            val plainWorld = World(plain(level))
            assertTrue(plainWorld.groups.isEmpty())
            for (t in floatArrayOf(0.37f, 2.9f, 5.55f)) {
                diff(render(trapWorld, t), render(plainWorld, t))?.let { throw AssertionError("Level ${i + 1} (${level.name.en}) at t=$t: $it") }
            }
            checked++
        }
        // collapse, hidden spikes, headbutt, bonk block, sinking, decoy, crumble, finale
        assertTrue("checked $checked levels", checked >= 8)
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
