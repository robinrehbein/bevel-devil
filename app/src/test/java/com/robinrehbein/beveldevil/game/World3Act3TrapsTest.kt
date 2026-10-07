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

    // 34: the doormat behind the gap is overclocked as you land: walking on over it burns, a low hop does not
    @Test
    fun theDoormatBurnsWhoWalksOverIt() {
        val landed = b(34).rightTo(18.6f).waitFor { it.fans[0].wind < -11.5f }.rightJump(0.5f).landRight()
        landed.right(2f).expect(WorldState.DEAD)
        DesignRules.play(World3.levels[33], 0, World3DesignTest.SOLUTIONS.getValue(34)[0]).expect(WorldState.WON)
    }

    // 35 (run right to left): the button turns the wall of wind off and the headwind into a tailwind; a blade comes with it
    @Test
    fun theButtonSetsTheFanAndTheWindOver() {
        val won = DesignRules.play(World3.levels[34], 0, World3DesignTest.SOLUTIONS.getValue(35)[0])
        won.expect(WorldState.WON)
        assertTrue(won.world.pads.single().presses >= 1)
        assertTrue("tailwind after the button", won.world.fans[0].target < 0f)
        // in the rematch the button is moved before the wind and bites: stepping on it ends the run
        Bot(World3.levels[34], 1).leftTo(23.5f).wait(3f).expect(WorldState.DEAD)
    }

    // 36: bar B comes out of the right wall: hugging it, the usual way, is where it hits
    @Test
    fun theSecondBarComesFromTheWallYouHug() {
        b(36).rightTo(7.4f).right(8f).expect(WorldState.DEAD)
        // standing in the middle of the shaft the first bar hits instead
        b(36).rightTo(7.4f).right(0.5f).wait(4f).expect(WorldState.DEAD)
    }

    // 36: the floor in front of the pin sinks as you come: walking on drops you, waiting at the edge and jumping pin and hole works
    @Test
    fun theFloorAheadSinksAsYouCome() {
        val bottom = b(36).rightTo(7.4f).rightUntil { it.player.box.b > 8.2f }.leftUntil { it.player.box.cx < 9.2f }
            .waitFor { it.player.box.b > 11.95f }.rightUntil { it.player.box.cx > 12.2f }.rightUntil { it.player.grounded }
        bottom.right(3f).expect(WorldState.DEAD)
        assertTrue(DesignRules.play(World3.levels[35], 0, World3DesignTest.SOLUTIONS.getValue(36)[0]).world.state == WorldState.WON)
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

    // 38: the cable on the shelf wakes up as you land there: walking on over it kills, the registered hop does not
    @Test
    fun theShelfCableWakesUpAsYouLand() {
        val top = b(38).leftTo(2.4f).rightTo(12.2f).rightJump(0.45f).landRight().rightTo(18.6f)
            .waitFor { w -> w.circuits['Z']?.let { !it.powered && w.time - it.flipTime < 0.4f } == true }.rightUntil { it.player.box.cx > 25f }
        top.right(2f).expect(WorldState.DEAD)
        DesignRules.play(World3.levels[37], 0, World3DesignTest.SOLUTIONS.getValue(38)[0]).expect(WorldState.WON)
    }

    // 39: the loose ceiling over the roof comes down in front of whoever heads for it: running on under it is the end, stopping short is not
    @Test
    fun theCeilingFallsOnWhoStandsUnderIt() {
        b(39).right(1.5f).expect(WorldState.DEAD)
        b(39).rightTo(8.0f).waitFor { it.group('h').oy > 2.5f }.wait(2f).expect(WorldState.PLAYING)
    }

    // 40: the draft reverses while you float: the keep is the only safe place, and nobody stays on it for long
    @Test
    fun theDraftSinksWhoDoesNotStopOnTheKeep() {
        b(40).leftTo(23.4f).left(5f).expect(WorldState.DEAD)
        // standing on the keep for good works as long as you do not leave before the draft is back
        b(40).leftTo(23.4f).leftUntil { it.player.grounded && it.player.box.cx < 17.6f }.wait(1.4f).expect(WorldState.PLAYING)
    }

    // 40: the welcome mat grows its spikes as you walk up to it: walking on dies, a long hop from well back lands in the door
    @Test
    fun theWelcomeMatGrowsSpikesAsYouWalkUp() {
        val ledge = b(40).leftTo(23.4f).leftUntil { it.player.grounded && it.player.box.cx < 17.6f }
            .waitFor { it.fans[0].wind > 4.5f }.leftUntil { it.player.box.cx < 8.8f }.leftUntil { it.player.grounded && it.player.box.cx < 8.6f }
        ledge.left(2f).expect(WorldState.DEAD)
        DesignRules.play(World3.levels[39], 0, World3DesignTest.SOLUTIONS.getValue(40)[0]).expect(WorldState.WON)
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
