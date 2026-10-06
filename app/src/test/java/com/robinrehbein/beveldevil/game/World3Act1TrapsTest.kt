package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The extra chain links of World 3, act 1 (levels 10, 13 and 16; levels 1-8 are the rebuilt block A, guarded by [World3DesignTest]): each one kills the counter the trap
 * before it taught, can be beaten once known, and never leaves the player alive with the door out of reach.
 */
class World3Act1TrapsTest {
    private val TOTAL = 40
    private fun level(n: Int) = World3Part1.levels[n - 1]
    private fun b(n: Int) = Bot(level(n))

    /** Level [n] without its trap number [i] (0-based): proves that it is the new link that bites. */
    private fun without(n: Int, i: Int): Bot {
        val l = level(n)
        val copy = Level(l.name, l.intro, l.legend, l.traps.filterIndexed { k, _ -> k != i }, l.start, rooms = l.rooms) {
            for (y in 0 until l.rows) for (x in 0 until l.cols) put(x, y, l.map.grid[y][x])
        }
        return Bot(copy)
    }

    @Test
    fun actOneAveragesAboutThreeTrapsPerLevel() {
        val total = World3Part1.levels.sumOf { it.traps.size }
        assertTrue("only $total traps in act 1", total >= TOTAL)
        for (n in listOf(10, 13)) assertEquals("level $n", 3, level(n).traps.size)
        assertEquals(4, level(16).traps.size)
    }

    // 10: the stones run on clocks, so you wait on one for the next; the first one clocks out early once you are on it

    private fun onStone1(bot: Bot) = bot.rightTo(6f).rightJump(0.55f).landRight().rightTo(12.3f)

    @Test
    fun metronomeWaitingOnTheFirstStoneDrops() {
        val bot = onStone1(b(10)).wait(1.15f)
        assertFalse(bot.world.player.grounded)
        bot.wait(1f).expect(WorldState.DEAD)
        val old = onStone1(without(10, 0)).wait(1.15f)
        assertTrue(old.world.player.grounded)
    }

    @Test
    fun metronomeWatchingTheNextStoneFromTheFloorWorks() {
        // wait on the floor until stone two is up, then cross stone one without stopping
        b(10).rightTo(6f).waitPowered('b', false).waitPowered('b').wait(0.2f).rightJump(0.55f).landRight().rightTo(12.3f).rightJump(0.55f).landRight()
            .rightTo(19.3f).waitPowered('c').rightJump(0.55f).landRight().rightTo(26.3f).right(3f).expect(WorldState.WON)
    }

    // 13: the fuse is a clock, so you wait for the dark beat; the rail in front of the wall is no place to wait

    private fun downTheHill(bot: Bot) = bot.rightTo(5.3f).rightJump(0.3f).landRight().rightJump(0.3f).landRight()

    @Test
    fun fuseBoxWaitingOnTheRailInFrontOfTheWallCutsIt() {
        val bot = downTheHill(b(13)).rightTo(20.5f).wait(1.5f)
        bot.expect(WorldState.DEAD)
        assertFalse(bot.world.circuits['a']!!.powered)
        downTheHill(without(13, 1)).rightTo(20.5f).wait(1.5f).expect(WorldState.PLAYING)
    }

    @Test
    fun fuseBoxWaitingOnTheHillWorks() {
        downTheHill(b(13)).rightTo(16.5f).waitPowered('Z').waitPowered('Z', false)
            .rightTo(22.8f).rightJump(0.3f).landRight().right(2f).expect(WorldState.WON)
    }

    @Test
    fun fuseBoxACutRailLeavesTheWayOpen() {
        // step on the rail, retreat to the hill: the rail goes dark behind you, and the gap can still be jumped from the chip
        val bot = downTheHill(b(13)).rightTo(18.6f).wait(0.15f).hopL(18.4f, 0.2f).wait(1f)
        bot.expect(WorldState.PLAYING)
        assertFalse(bot.world.circuits['a']!!.powered)
        bot.waitPowered('Z').waitPowered('Z', false).leftTo(16.5f).rightTo(18.1f).rightJump(0.55f).landRight()
            .rightJump(0.3f).landRight().right(2f).expect(WorldState.WON)
    }

    // 16: the bit flip saves whoever jumps early; a moment after landing it flips back

    private fun onRailC(bot: Bot) = bot.rightTo(5f).rightTo(15.5f).waitPowered('Z', false).rightTo(21f).rightJump(0.55f).landRight()

    @Test
    fun motherboardStoppingOnTheSavingRailDrops() {
        val bot = onRailC(b(16)).wait(1f)
        bot.expect(WorldState.DEAD)
        assertEquals(Card.GRAND_FINALE, bot.world.lastCard)
        onRailC(without(16, 3)).wait(1f).expect(WorldState.PLAYING)
    }

    @Test
    fun motherboardRunningOffTheRailMakesIt() {
        onRailC(b(16)).right(3f).expect(WorldState.WON)
    }
}
