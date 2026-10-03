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
        assertTrue(World3Part2.levels.all { it.traps.size in 2..4 })
    }

    @Test
    fun fullLoadChipRunsHotterOnceYouAreDownSoTheLastTilesAreForJumping() {
        // walking off the heatsink and across the chip burns on its last tiles
        val walk = b(18).hopR(3f).waitCooled('c').rightTo(24f)
        walk.expect(WorldState.DEAD)
        assertTrue(walk.world.player.box.cx < 23.5f)
        // the same run with a leap off the chip's end gets over it, and the overclocked landing needs a second hop
        b(18).hopR(3f).waitCooled('c').rightTo(19f).rightJump(0.55f).landRight().right(1.2f).expect(WorldState.DEAD)
    }

    @Test
    fun coldStartDoorRunsHomeToTheCoolPlateOverAnOverclockedFloor() {
        val bot = b(20).hopR(13f).rightTo(21.6f).rightJump(0.55f).landRight().wait(0.5f)
        bot.expect(WorldState.PLAYING)
        assertTrue(bot.world.door.box.cx < 20f)
        assertTrue(bot.world.heaters['f']!!.heat > 0.6f)
        // running straight back burns on the overclocked floor
        bot.left(4f).expect(WorldState.DEAD)
        // waiting for it to cool, the door stands on the glowing plate, which stays cool
        val patient = b(20).hopR(13f).rightTo(21.6f).rightJump(0.55f).landRight().wait(0.1f).waitWhile(3f) { it.door.moving }.waitCooled('f').waitCooled('g')
        assertEquals(7f, patient.world.door.box.x + 0.1f, 0.01f)
        patient.left(4f).expect(WorldState.WON)
    }

    @Test
    fun relayRaceLastLegTurnsUpSoLeapFromTheSecondSink() {
        // cooling on both sinks and walking the last plate is no longer enough
        b(21).rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').rightTo(24.6f).expect(WorldState.DEAD)
        val leap = b(21).rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').rightJump(0.55f).landRight()
        leap.expect(WorldState.PLAYING)
        assertTrue(leap.world.heaters['h']!!.spec.rise < 0.6f)
    }

    @Test
    fun warmUpSendsAFanBladeAcrossTheChip() {
        // the sprint that beat the chip runs into a blade
        val bot = b(22).rightTo(23.4f)
        bot.expect(WorldState.DEAD)
        assertTrue(bot.world.saws.isNotEmpty())
        // stopping for it is no answer either: the chip goes to full load
        b(22).rightUntilSaw(6f).waitFor(3f) { false }.expect(WorldState.DEAD)
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
        // 20: the door on the cool plate stands on the walking row
        val home = b(20).hopR(13f).rightTo(21.6f).rightJump(0.55f).landRight().wait(0.1f).waitWhile(3f) { it.door.moving }
        assertEquals(World(World3.levels[19]).door.box.y, home.world.door.box.y, 0.01f)
        // 20: a short leap that lands on the last plate leaves the door hovering where a jump reaches it
        val short = b(20).hopR(13f).rightTo(20.4f).rightJump(0.55f).landRight()
        assertTrue(short.world.door.box.y < World(World3.levels[19]).door.box.y - 1.5f && short.world.door.box.x > 28f)
        short.rightTo(28.3f).rightJump(0.3f).expect(WorldState.WON)
        // 30: waiting a step back from the gate, off the burnt-in floor, still gets through
        val gate = b(30).rightTo(6.5f).waitPowered('Z', false).rightTo(12f)
        gate.expect(WorldState.PLAYING)
    }
}
