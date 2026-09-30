package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [Trigger.Airborne] and [Trigger.Landed]: traps that fire on the jump, not on the walk. */
class TriggerTest {
    /** A flat room whose only trap says "x" when [t] fires. */
    private fun room(t: Trigger) = Level(T("room", "Raum"), T("", ""), traps = listOf(trap(t, Action.Say(T("x", "x"))))) {
        border(); floor()
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    private fun Bot.said() = world.events.any { it is Event.Say }

    @Test
    fun airborneIgnoresWalkingAndStandingStill() {
        val bot = Bot(room(Trigger.Airborne(10f, 14f))).rightTo(12f).wait(0.5f).rightTo(16f)
        assertFalse(bot.said())
    }

    @Test
    fun airborneFiresMidJumpInsideItsRange() {
        val bot = Bot(room(Trigger.Airborne(10f, 14f))).rightTo(9f).rightJump(0.35f)
        assertTrue(bot.said())
    }

    @Test
    fun airborneIgnoresJumpsOutsideItsRange() {
        val bot = Bot(room(Trigger.Airborne(20f, 24f))).rightTo(4f).rightJump(0.35f).landRight().wait(0.3f)
        assertFalse(bot.said())
    }

    @Test
    fun airborneDoesNotFireOnTheSpawnStep() {
        // the player is not "grounded" on the very first step; a range around the spawn must not fire by itself
        assertFalse(Bot(room(Trigger.Airborne(0f, 6f))).wait(0.5f).said())
    }

    @Test
    fun landedFiresWhenTheJumpEndsInsideItsRange() {
        // a full jump from x=8 lands at about 12.8
        val hit = Bot(room(Trigger.Landed(12f, 14f))).rightTo(8f).rightJump(0.35f).landRight().wait(0.05f)
        assertTrue(hit.said())
        val miss = Bot(room(Trigger.Landed(16f, 20f))).rightTo(8f).rightJump(0.35f).landRight().wait(0.05f)
        assertFalse(miss.said())
    }

    @Test
    fun landedIgnoresWalkingAndTheTimeInTheAir() {
        val walk = Bot(room(Trigger.Landed(0f, 31f))).rightTo(20f)
        assertFalse(walk.said())
        val flying = Bot(room(Trigger.Landed(0f, 31f))).rightTo(8f).rightJump(0.35f)
        assertFalse("still in the air", flying.world.player.grounded || flying.said())
    }

    @Test
    fun landedFiresOnceAndTrapsAreOneShot() {
        val bot = Bot(room(Trigger.Landed(0f, 31f))).rightTo(8f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().wait(0.1f)
        assertEquals(1, bot.world.events.count { it is Event.Say })
    }
}
