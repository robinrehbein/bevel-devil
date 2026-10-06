package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The extra chain links of act 3 (levels 33-48): each one punishes the counter the trap before it taught. Levels 33-40 are the rebuilt block E
 * ([World3RoomsE]), 41-48 the rebuilt block F ([World3RoomsF]); their rules are checked by [World3DesignTest].
 */
class World3Act3TrapsTest {
    private fun b(n: Int) = Bot(World3.levels[n - 1])

    @Test
    fun actThreeAveragesAlmostThreeTrapsPerLevel() {
        val average = World3Part3.levels.sumOf { it.traps.size } / World3Part3.levels.size.toFloat()
        assertTrue("only $average traps per level", average >= 2.9f)
        // the rebuilt levels keep a real trap every 3 s (docs/LEVEL_DESIGN_V2.md H3), which in a slog of gusts and blades takes more links than four
        assertTrue(World3Part3.levels.all { it.traps.size in 2..6 })
    }

    // 33: the plank you land on gives way under whoever stays on it, and the plank ahead drops out as you land
    @Test
    fun theSecondPlankGivesWayUnderWhoStays() {
        val onC = b(33).rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.rightTo(15.3f).rightJump(0.5f).landRight()
        onC.wait(1.5f).expect(WorldState.DEAD)
        // walking on instead of leaping falls into the gap the first plank left
        b(33).rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.right(1.5f).expect(WorldState.DEAD)
    }

    // 35: the button turns the wall of wind off and the headwind into a tailwind; a blade comes with it
    @Test
    fun theButtonSetsTheFanAndTheWindOver() {
        val won = DesignRules.play(World3.levels[34], 0, World3DesignTest.SOLUTIONS.getValue(35)[0])
        won.expect(WorldState.WON)
        assertTrue(won.world.pads.single().presses >= 1)
        assertTrue("tailwind after the button", won.world.fans[0].target < 0f)
        // in the rematch the button is moved before the wind and bites: stepping on it ends the run
        Bot(World3.levels[34], 1).rightTo(8.5f).wait(3f).expect(WorldState.DEAD)
    }

    // 36: bar B comes out of the right wall: hugging it, the usual way, is where it hits
    @Test
    fun theSecondBarComesFromTheWallYouHug() {
        b(36).rightTo(7.4f).right(8f).expect(WorldState.DEAD)
        // standing in the middle of the shaft the first bar hits instead
        b(36).rightTo(7.4f).right(0.5f).wait(4f).expect(WorldState.DEAD)
    }

    // 37: the wall walks behind you; who waits in the corridor is caught
    @Test
    fun theWallBehindYouCatchesWhoWaits() {
        b(37).rightTo(6f).wait(8f).expect(WorldState.DEAD)
    }

    // 38: the lift runs, the cable in its shaft takes turns: riding up while it is live ends at the cable
    @Test
    fun theCableInTheShaftIsLiveInItsTurn() {
        val bot = b(38).leftTo(2.4f).rightTo(12.2f).rightJump(0.45f).landRight().rightTo(18.6f).waitFor { it.circuits['Z']?.powered == true }.rightUntil { it.player.box.cx > 25f }
        bot.expect(WorldState.DEAD)
        // the floor cable goes live as soon as you head for the lift: walking over it kills, hopping it does not
        b(38).right(1.5f).expect(WorldState.DEAD)
        // and without touching the plate the lift stays dead
        b(38).rightTo(12.2f).rightJump(0.45f).landRight().rightTo(22f).wait(2f).expect(WorldState.PLAYING)
    }

    // 39: the bridge: the slab behind the lift exit comes down on whoever stands under it, the stud wants a hop, the floor closes on whoever waits
    @Test
    fun theCeilingFallsOnWhoStandsUnderIt() {
        b(39).rightUntil { it.player.box.b < 6.3f }.rightUntil { it.player.grounded && it.player.box.cx > 7.3f }.rightTo(8.3f).rightJump(0.1f).landRight().wait(2f).expect(WorldState.DEAD)
        b(39).rightUntil { it.player.box.b < 6.3f }.right(3f).expect(WorldState.DEAD)
    }

    // 40: the draft reverses while you float: the keep is the only safe place, and nobody stays on it for long
    @Test
    fun theDraftSinksWhoDoesNotStopOnTheKeep() {
        b(40).rightTo(8.6f).right(5f).expect(WorldState.DEAD)
        // standing on the keep for good works as long as you do not leave before the draft is back
        b(40).rightTo(8.6f).rightUntil { it.player.grounded && it.player.box.cx > 14.4f }.wait(1.4f).expect(WorldState.PLAYING)
    }

    // 41-48 (block F): the bot solutions are the registered ones; each level kills whoever runs it carelessly
    private fun sol(n: Int) = World3DesignTest.SOLUTIONS.getValue(n)[0]

    @Test
    fun theSolutionsOfBlockFWinInTheirRoom() {
        for (n in 41..48) DesignRules.play(World3.levels[n - 1], 0, sol(n)).expect(WorldState.WON)
    }

    @Test
    fun blockFHasItsCardsAndTheFinaleHasSixTraps() {
        for (n in 41..48) assertTrue("level $n has no card", World3.levels[n - 1].traps.any { t -> t.actions.any { it is Action.Play } } || n in listOf(45, 46))
        assertTrue(World3.levels[47].traps.size in 5..6)
    }
}
