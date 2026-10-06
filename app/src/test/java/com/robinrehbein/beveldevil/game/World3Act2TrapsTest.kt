package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The extra links in the trap chains of World 3, act 2 ("Überhitzung", levels 17-32): each one punishes the counter the
 * player just learned, and each has an answer the bot plays (the full solutions are in [World3Test]).
 */
class World3Act2TrapsTest {
    private fun b(n: Int) = Bot(World3.levels[n - 1])

    @Test
    fun actTwoAveragesAlmostThreeTrapsPerLevel() {
        val traps = World3Part2.levels.sumOf { it.traps.size }
        assertTrue("only $traps traps in act two", traps >= 46)
        // a level may have more trap entries than moments: a door that flees in several hops is one moment of three entries (level 20)
        assertTrue(World3Part2.levels.all { it.traps.size in 2..7 })
    }

    // 18, 20, 21 and 22 are the rebuilt block C: what each one does to the player who plays it the obvious way

    @Test
    fun fullLoadTheWayBackOverTheBurningChipNeedsTheSink() {
        // the first blade is hopped, the switch is pressed, and running straight back over the chip burns
        b(18).rightUntil { w -> w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= 5.5f } }.rightJump(0.55f).landRight()
            .rightTo(27.6f).leftTo(3.0f).left(1.5f).expect(WorldState.DEAD)
    }

    @Test
    fun coldStartDoorRunsHomeAndTheWayBackIsHoppedTheOtherWayRound() {
        // hopping the plain floor to the far end sends the door home; walking back straight over the plates burns
        val bot = b(20).rightTo(6.7f).rightJump(0.5f).landRight().rightTo(13.2f).rightJump(0.5f).landRight().rightTo(20.7f).rightJump(0.5f).landRight()
            .rightTo(28.0f).wait(0.8f)
        assertTrue("door ${bot.world.door.box.cx} ${bot.world.state} x=${bot.world.player.box.cx}", bot.world.door.box.cx < 29f)
        bot.left(5f).expect(WorldState.DEAD)
    }

    @Test
    fun relayRaceStayingOnTheSinkLetsTheWallCatchUp() {
        b(21).rightTo(10.7f).wait(3f).expect(WorldState.DEAD)
    }

    @Test
    fun warmUpTheStripThatWaitedAtTheFarEndSlidesAtWhoLands() {
        // landing on the middle slab and standing there is the end of it
        b(22).rightTo(10.4f).rightJump(0.55f).landRight().rightTo(21.5f).landRight().wait(3f).expect(WorldState.DEAD)
    }

    // 25, 28, 29 and 30 are the rebuilt block D: what each one does to the player who plays it the obvious way

    @Test
    fun coolingTowerTheBeltTurnsAroundUnderWhoStandsStill() {
        // standing on the first ground belt when it turns round carries you back into the spikes in the corner
        b(25).leftTo(22.5f).landLeft().leftJump(0.55f).landLeft().leftJump(0.55f).landLeft()
            .leftUntil { it.player.grounded && it.player.box.b > 11.5f }.rightUntil { (it.group('c').belt ?: 0f) < 0f }.wait(3f).expect(WorldState.DEAD)
    }

    @Test
    fun breakTimeTheBenchIsNoPlaceToStay() {
        // sitting on the first heatsink for good: the second blade crawls out of the wall of the dip
        b(28).rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.b > 12.5f }.rightTo(12.4f).rightUntil { it.player.grounded && it.player.box.b > 15.5f }
            .rightTo(16.2f).rightJump(0.3f).landRight().wait(3f).expect(WorldState.DEAD)
    }

    @Test
    fun thermostatTheHatchIsOpenOnlyOnThePadAndTheSlabComesDownThroughIt() {
        // the pad holds the hatch open for as long as you stand on it, and the slab above it comes down through the gap
        val bot = b(29).rightTo(27.5f).wait(3f)
        assertFalse(bot.world.group('w').visible)
        assertTrue(bot.world.group('s').oy > 5f)
    }

    @Test
    fun burnInTheTestPatternSpikesAreOnlyRoundOnesLie() {
        // round one: hopping the spikes that are about to sink lands on the plate behind them, which flares for whoever is in the air
        val hop: Solution = {
            rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                .rightUntil { w -> w.group('Q').let { it.homeX + it.ox - w.player.box.cx < 3.2f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(22.4f).leftJump(0.55f).landLeft().left(2f)
        }
        DesignRules.play(World3.levels[29], 0, hop).expect(WorldState.DEAD)
        // round two: the spikes are real, so walking through them as in round one is the end of it
        DesignRules.play(World3.levels[29], 1, World3DesignTest.SOLUTIONS.getValue(30)[0]).expect(WorldState.DEAD)
    }

    /** None of the rebuilt levels of block D strands the player: every registered solution of every round wins, and none needs a second try. */
    @Test
    fun theRebuiltBlockLeavesNoSoftlock() {
        for (n in 25..32) World3DesignTest.SOLUTIONS.getValue(n).forEachIndexed { r, sol -> DesignRules.play(World3.levels[n - 1], r, sol).expect(WorldState.WON) }
        // 20: the door that ran home ends on the walking row
        val won = DesignRules.play(World3.levels[19], 0, World3DesignTest.SOLUTIONS.getValue(20)[0])
        won.expect(WorldState.WON)
        assertEquals(World(World3.levels[19]).door.box.y, won.world.door.box.y, 0.01f)
    }
}
