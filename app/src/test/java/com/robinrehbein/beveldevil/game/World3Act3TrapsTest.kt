package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The extra chain links of act 3 (levels 33-48): each one punishes the counter the trap before it taught. */
class World3Act3TrapsTest {
    private fun b(n: Int) = Bot(World3.levels[n - 1])

    @Test
    fun actThreeAveragesAlmostThreeTrapsPerLevel() {
        val average = World3Part3.levels.sumOf { it.traps.size } / World3Part3.levels.size.toFloat()
        assertTrue("only $average traps per level", average >= 2.9f)
        assertTrue(World3Part3.levels.all { it.traps.size in 2..4 })
    }

    // 35: the first lull ends early under the jump; only a leap from the very edge clears the spikes
    @Test
    fun theLullEndsEarlyUnderTheFirstJump() {
        val bot = b(35).rightTo(22f).waitFor { it.fans[0].wind == 0f }.rightTo(23.4f).rightJump(0.3f)
        assertTrue("the gust is back mid-air", bot.world.fans[0].wind > 0f)
        bot.rightJump(0.25f).landRight().right(3f).expect(WorldState.DEAD)
        b(35).rightTo(22f).waitFor { it.fans[0].wind == 0f }.rightTo(24.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    }

    // 39: waiting for the lull right at the pit edge burns; waiting further back and sprinting works
    @Test
    fun theWaitingSpotAtThePitEdgeWarmsUp() {
        val bot = b(39).rightTo(14.2f).waitFor { it.fans[0].wind == 0f }
        bot.expect(WorldState.DEAD)
        assertFalse("looked like plain floor", bot.world.heaters['w']!!.declared)
        b(39).rightTo(12f).waitFor { it.fans[0].wind == 0f }.rightTo(14.3f).rightJump(0.55f).landRight().rightTo(21.3f).rightJump(0.55f).landRight()
            .rightUntilSaw(3.2f).rightJump(0.55f).landRight().right(1f).left(3f).expect(WorldState.WON)
    }

    // 41: the live ceiling wiring is a scarecrow: it switches off under the jump, and walking under it drops you through the ledge
    @Test
    fun theLiveCeilingWiringSwitchesOffWhenYouJump() {
        val up = b(41).rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight()
        assertTrue(up.world.circuits['Z']!!.powered)
        up.rightTo(21.2f).rightJump(0.3f)
        assertFalse(up.world.circuits['Z']!!.powered)
        up.rightJump(0.25f).landRight().right(4f).expect(WorldState.WON)
        // walking under it is safe, it only scares you into walking onto the ledge that breaks
        val walk = b(41).rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight()
            .rightTo(24.5f)
        walk.expect(WorldState.PLAYING)
        assertTrue(walk.world.player.box.b > 4.5f)
    }

    // 43: landing on the bridge wakes a dead cable on it; running on dies, hopping it wins
    @Test
    fun aCableOnTheBridgeGoesLiveAsYouLand() {
        val bot = b(43).rightTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(14.6f).rightJump(0.55f).landRight().right(0.1f)
        assertTrue(bot.world.circuits['Z']!!.powered)
        bot.right(6f).expect(WorldState.DEAD)
        assertFalse(b(43).wait(0.1f).world.circuits['Z']!!.powered)
    }

    // 45: dropping off the cool ledge onto the floor overclocks it; walking on burns, a long leap or hopping gets across
    @Test
    fun theFloorIsOverclockedWhereYouDropOntoIt() {
        fun top() = b(45).rightTo(8.8f).waitFor { it.player.box.cy < 6.8f }.rightTo(13f).landRight().waitCooled('h')
        top().rightTo(24.6f).rightJump(0.55f).landRight().right(3f).expect(WorldState.DEAD)
        top().right(0.4f).landRight().rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightTo(24.6f).rightJump(0.55f).landRight().right(3f)
            .expect(WorldState.WON)
    }
}
