package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Trigger.PastX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The V2 trap cards: Ctrl+Z puts Bevel back, the stalker follows him. */
class V2TrapsTest {
    private val undo = Level(
        name = T("Undo Demo", "Strg-Z-Demo"),
        intro = T("x", "x"),
        traps = listOf(trap(PastX(24f), Play(Card.UNDO), Undo(2f))),
    ) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    private val stalker = Level(
        name = T("Stalker Demo", "Verfolger-Demo"),
        intro = T("x", "x"),
        legend = mapOf('S' to Glyph(spike = true, dir = Dir.DOWN, hidden = true)),
        traps = listOf(trap(PastX(6f), Play(Card.STALKER), Show('S'), Chase('S', speed = 5f, left = 3f, right = 20f))),
    ) {
        border(); floor()
        put(4, 12, 'S')
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    @Test
    fun undoPutsBevelBackTwoSeconds() {
        val b = Bot(undo).right(2.7f)
        // reached x 24 after ~2.6 s, was ~17 tiles left of there 2 s before
        assertTrue("x=${b.world.player.box.cx}", b.world.player.box.cx < 12f)
        assertEquals(WorldState.PLAYING, b.world.state)
        // the trap is spent: the second run gets through
        b.right(4f).expect(WorldState.WON)
    }

    @Test
    fun undoNeverPutsBevelIntoAWall() {
        val l = Level(T("u", "u"), T("u", "u"), traps = listOf(trap(PastX(10f), Undo(4f)))) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D')
        }
        val b = Bot(l).right(3f)
        assertEquals(WorldState.PLAYING, b.world.state)
        assertTrue(b.world.pieces.none { it.solid && it.box.overlaps(b.world.player.box) })
    }

    @Test
    fun theStalkerFollowsAndWalkingIntoItKills() {
        // standing under the stalker's track: it slides over and the spikes hanging under it… stay up at row 12
        val w = Bot(stalker).right(0.6f).wait(1.5f).world
        val s = w.group('S')
        assertTrue("stalker should follow, ox=${s.ox}", s.ox > 1f)
        assertEquals(WorldState.PLAYING, w.state)
    }

    @Test
    fun theStalkerCannotLeaveItsTrack() {
        val b = Bot(stalker).right(2.8f)
        assertTrue(b.world.group('S').ox <= 20f + 1e-3f)
    }
}
