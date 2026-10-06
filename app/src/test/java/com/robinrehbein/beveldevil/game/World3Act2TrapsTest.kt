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

    @Test
    fun coolingTowerFloorTurnsUpUnderWhoClimbsDown() {
        // stepping down from the tower and walking on burns before the exit
        b(25).rightTo(11.4f).rightJump(0.55f).landRight().waitCooled('h').rightTo(24.2f).expect(WorldState.DEAD)
        // leaping off the tower lands late enough to make it
        val leap = b(25).rightTo(11.4f).rightJump(0.55f).landRight().waitCooled('h').rightTo(15.5f).rightJump(0.55f).landRight().rightTo(24.2f)
        leap.expect(WorldState.PLAYING)
    }

    @Test
    fun breakTimeFanHasASecondBlade() {
        // waiting on the heatsink for the first blade to arrive: a faster one comes from behind before it
        val bot = b(28).rightTo(16.5f).waitCooled('h').wait(3f)
        bot.expect(WorldState.DEAD)
        assertEquals(2, bot.world.saws.size)
        assertTrue("hit from behind", bot.world.saws.any { it.vx > 0f && kotlin.math.abs(it.x - bot.world.player.box.cx) < 1.2f })
        assertEquals(2, World3.levels[27].traps.flatMap { it.actions }.count { it is Action.Saw })
        // a runner who leaves the sink once it has cooled is never caught by it
        b(28).rightTo(16.5f).waitCooled('h').rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    }

    @Test
    fun thermostatLandingRunsIntoAFloorThatIsGone() {
        val bot = b(29).rightTo(10.5f).waitFor { !it.group('w').visible }.rightTo(17.3f).rightJump(0.55f).landRight().right(3f)
        bot.expect(WorldState.DEAD)
        assertTrue(bot.world.player.box.cy > 15f)
    }

    @Test
    fun burnInFloorBurnsUnderWhoWaitsAtTheGate() {
        // waiting right at the gate for its gap burns in; waiting a step back works (see the level test)
        val bot = b(30).rightTo(9.2f).waitPowered('Z', false, max = 3f)
        bot.expect(WorldState.DEAD)
        assertTrue(bot.world.heaters['e']!!.heat >= 1f)
        assertFalse(World(World3.levels[29]).heaters.containsKey('e'))
    }

    /** None of the new links strands the player: the exit pits kill, the door that ran off is reachable on foot. */
    @Test
    fun theNewLinksLeaveNoSoftlock() {
        // 29: falling into the opened floor is death, not a floor below
        val fell = b(29).rightTo(10.5f).waitFor { !it.group('w').visible }.rightTo(17.3f).rightJump(0.55f).landRight().right(3f)
        assertEquals(WorldState.DEAD, fell.world.state)
        // 20: the door that ran home ends on the walking row
        val won = DesignRules.play(World3.levels[19], 0, World3DesignTest.SOLUTIONS.getValue(20)[0])
        won.expect(WorldState.WON)
        assertEquals(World(World3.levels[19]).door.box.y, won.world.door.box.y, 0.01f)
        // 30: waiting a step back from the gate, off the burnt-in floor, still gets through
        val gate = b(30).rightTo(6.5f).waitPowered('Z', false).rightTo(12f)
        gate.expect(WorldState.PLAYING)
    }
}
