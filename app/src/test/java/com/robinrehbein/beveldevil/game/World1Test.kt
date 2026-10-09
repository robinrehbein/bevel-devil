package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.robinrehbein.beveldevil.game.World1RoomsD.pieceLanded
import com.robinrehbein.beveldevil.game.World1RoomsD.ropeUp
import com.robinrehbein.beveldevil.game.World1RoomsE.ropeUp as ropeUpE

/** One scripted solution per level of World 1 (48 levels, three acts of 16), played with the real physics. */
class World1Test {
    private fun b(n: Int) = Bot(World1.levels[n - 1])

    /** Seconds the blinking group [id] stays solid from now, 0 while it is gone. */
    private fun World.solidLeft(id: Char): Float = group(id).let { g ->
        val k = g.blink!!
        if (g.visible) k.on - k.cycle(time - g.blinkT0) else 0f
    }

    /** Seconds until the blinking group [id] is solid again, 0 while it is solid. */
    private fun World.gapLeft(id: Char): Float = group(id).let { g ->
        val k = g.blink!!
        if (g.visible) 0f else k.period - k.cycle(time - g.blinkT0)
    }

    private fun actions(l: Level) = l.start + l.traps.flatMap { it.actions }

    // ---------- structure ----------

    @Test
    fun worldHas48LevelsInThreeActsThatParse() {
        assertEquals(48, World1.levels.size)
        assertEquals(16, World1Part1.levels.size)
        assertEquals(16, World1Part2.levels.size)
        assertEquals(16, World1Part3.levels.size)
        World1.levels.forEach { World(it) }
    }

    @Test
    fun standingStillIsSafeForTwoSeconds() {
        // levels that punish (or reward) standing still on purpose are the exception
        World1.levels.filter { l -> l.traps.none { it.trigger is Trigger.Idle } }.forEach { l -> Bot(l).wait(2.2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun namesAreUniqueAndShortEnoughForTheHud() {
        val names = World1.levels.map { it.name.en }
        assertEquals(names.size, names.toSet().size)
        World1.levels.forEachIndexed { i, l ->
            assertTrue("level ${i + 1} name too long", l.name.en.length <= 26 && l.name.de.length <= 26)
        }
    }

    @Test
    fun namesAndIntrosAreFilledInBothLanguages() {
        World1.levels.forEach { l ->
            assertTrue(l.name.en.isNotBlank() && l.name.de.isNotBlank() && l.intro.en.isNotBlank() && l.intro.de.isNotBlank())
        }
    }

    @Test
    fun everyTrapLevelPlaysExactlyOneCard() {
        // in every round, rematches included (a bluff counts)
        World1.levels.forEachIndexed { i, l ->
            l.rounds.forEachIndexed { k, r ->
                if (r.traps.isNotEmpty()) {
                    val plays = r.traps.sumOf { t -> t.actions.count { it is Action.Play || it is Action.Bluff } }
                    assertEquals("level ${i + 1} round ${k + 1} should play exactly one card", 1, plays)
                }
            }
        }
    }

    /**
     * Act one deals the classic cards. Level 16 plays ANNEX on the breach instead of GRAND_FINALE (one card per round), so the
     * last of the twelve is dealt by the finales of the later acts; the stalker joins the first eleven.
     */
    @Test
    fun actOneShowsTheClassicCardsAndTheAnnex() {
        val cards = World1Part1.levels.flatMap { l -> l.rounds.flatMap { r -> actions(r).filterIsInstance<Action.Play>().map { it.card } } }.toSet()
        // the classic eleven, the stalker and the annex of the finale (the return trip of 12 plays the collapse on the sliding
        // spike: the twisted card of 9 may only come back 8 levels later, docs/LEVEL_DESIGN_V2.md §7, and the bit flip is World 3's)
        assertEquals(Card.entries.take(11).toSet() + Card.STALKER + Card.ANNEX, cards)
    }

    /** The obvious thing to do, running right and never letting go, must not win any level. */
    @Test
    fun holdingRightAloneWinsNothing() {
        val winners = World1.levels.withIndex().filter { (_, l) -> Bot(l).right(14f).world.state == WorldState.WON }.map { it.index + 1 }
        assertTrue("holding right wins levels $winners", winners.isEmpty())
    }

    @Test
    fun actTwoIntroducesNewMechanics() {
        // the levels built around a new mechanic; the other act-2 levels are reworked classics
        val tagged = listOf(20, 23, 25, 26, 28, 31, 32)
        assertTrue(tagged.size >= 7)
        for (n in tagged) {
            val l = World1.levels[n - 1]
            val uses = actions(l).any { it is Action.Blink || it is Action.PathSaw } || l.traps.any { it.trigger is Trigger.Idle }
            assertTrue("level $n should use Blink, PathSaw or Idle", uses)
        }
        val classics = (17..32) - tagged.toSet()
        for (n in classics) {
            val l = World1.levels[n - 1]
            assertFalse("level $n is a classic", actions(l).any { it is Action.Blink || it is Action.PathSaw } || l.traps.any { it.trigger is Trigger.Idle })
        }
        // each mechanic is taught by itself before it is mixed in: blinking first, then saws, then idling
        assertTrue(actions(World1.levels[19]).any { it is Action.Blink } && actions(World1.levels[19]).none { it is Action.PathSaw })
        assertTrue(actions(World1.levels[22]).any { it is Action.PathSaw } && actions(World1.levels[22]).none { it is Action.Blink })
        assertTrue(World1.levels[25].traps.any { it.trigger is Trigger.Idle })
    }

    @Test
    fun actOneAndTwoDoNotUseMetaTwists() {
        val meta = { a: Action ->
            a is Action.FakeWin || a is Action.PauseTrap || a is Action.FrameCrack || a is Action.Flip || a is Action.Roll || a is Action.Ghost
        }
        assertTrue((World1Part1.levels + World1Part2.levels).none { l -> actions(l).any(meta) })
        assertEquals(setOf("PauseTrap", "FrameCrack", "Flip"),
            World1Part3.levels.flatMap { l -> actions(l).filter(meta).map { it::class.simpleName!! } }.toSet())
    }

    @Test
    fun exactlyTwoLevelsUsePhoneMotion() {
        val motion = World1.levels.withIndex().filter { (_, l) -> l.usesMotion }.map { it.index + 1 }
        assertEquals(listOf(39, 44), motion)
        assertTrue(World1.levels[38].usesTilt)
        assertTrue(World1.levels[43].usesShake)
    }

    // ---------- the levels that react to input in unusual ways ----------

    // ---------- Act 1: Die Karten ----------
    // the tutorial's clean runs live in World1DesignTest.TUTORIAL_SOLUTIONS (shared with the round rules and the probe report)
    private fun tutorial(n: Int) = b(n).also(World1DesignTest.TUTORIAL_SOLUTIONS.getValue(n)[0]).expect(WorldState.WON)
    @Test fun level01() = tutorial(1)
    /** The hole grows back toward whoever stops at its edge. */
    @Test fun level01StoppingAtTheEdgeFallsIn() = b(1).rightTo(17.6f).wait(1.5f).expect(WorldState.DEAD)
    @Test fun level02() = tutorial(2)
    @Test fun level03() = tutorial(3)
    /** At the top the broken lift comes down the shaft onto whoever stays. */
    @Test fun level03TheLiftComesDownOnWhoeverStaysUpThere() = b(3).hopR(21.2f).rightTo(26.5f)
        .leftTo(24.3f).leftJump(0.35f).landLeft().rightTo(21.3f).rightJump(0.35f).landRight()
        .rightTo(25.6f).rightJump(0.35f).landRight().wait(1.5f).expect(WorldState.DEAD)
    /** Falling with the crumbling step must not strand the player: the step floats back and the stairs work again. */
    @Test fun level03CrumbledStepComesBack() {
        val fell = b(3).hopR(21.2f).rightTo(26.5f).leftTo(24.3f).leftJump(0.35f).landLeft().wait(2.2f)
        fell.expect(WorldState.PLAYING)
        assertTrue("fell to the floor", fell.world.player.box.y > 13f)
        fell.wait(3f)
        assertEquals(0f, fell.world.group('b').oy, 0.05f)
        // it carries whoever fell with it back up, and the climb goes on
        assertEquals(11f, fell.world.player.box.b, 0.05f)
        fell.rightTo(21.3f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.35f).landRight()
            .leftTo(23f).right(4f).expect(WorldState.WON)
    }
    @Test fun level04() = tutorial(4)
    /** Waiting where the first slab fell: the piece above you follows. */
    @Test fun level04WaitingUnderTheSecondPieceIsFatal() = b(4).rightTo(13.12f).wait(2f).expect(WorldState.DEAD)
    @Test fun level05() = tutorial(5)
    @Test fun level06() = tutorial(6)
    // levels 7-16 are rebuilt (docs/LEVEL_DESIGN_V2.md): their clean runs live in World1RoomsA and are shared with the design tests
    @Test fun level07() = World1DesignTest.play(7)
    @Test fun level08() = World1DesignTest.play(8)
    @Test fun level09() = World1DesignTest.play(9)
    @Test fun level10() = World1DesignTest.play(10)
    @Test fun level11() = World1DesignTest.play(11)
    @Test fun level12() = World1DesignTest.play(12)
    @Test fun level13() = World1DesignTest.play(13)
    @Test fun level14() = World1DesignTest.play(14)
    @Test fun level15() = World1DesignTest.play(15)
    @Test fun level16() = World1DesignTest.play(16)

    // ---------- Act 1, levels 7-16: nobody is stranded, and the obvious reflexes meet their trap ----------

    /** 7: the prefab piece comes down in front of whoever runs at it; waiting for it is safe, and then it is hopped. */
    private fun past07() = b(7).rightTo(9.6f).waitFor { it.group('c').oy >= 4.9f }.hopR(10.6f)
    @Test fun level07RunningAtThePieceIsFatal() = b(7).right(2f).expect(WorldState.DEAD)
    @Test fun level07WaitingForThePieceIsSafe() = b(7).rightTo(9.6f).wait(2f).expect(WorldState.PLAYING)
    /** 7: hopping back off the first panel strands nobody on the start floor: it gives way. */
    @Test fun level07RetreatingFromTheSunkPanelDies() {
        val back = past07().hopR(16.0f).leftTo(18.3f).leftJump(0.35f).landLeft().left(0.3f).wait(0.3f)
        back.expect(WorldState.PLAYING)
        assertTrue("back on the start floor at x=${back.world.player.box.cx}", back.world.player.box.cx < 17f && back.world.player.grounded)
        back.wait(3f).expect(WorldState.DEAD)
    }
    @Test fun level07StandingOnAPanelSinksWithIt() = past07().hopR(16.0f).wait(2f).expect(WorldState.DEAD)

    /** 8: running on over the stalactites that just popped up ends in them; the keys have not changed. */
    private fun flipped08() = b(8).rightTo(19.5f).rightJump(0.25f).leftUntil { it.player.grounded }
    @Test fun level08TheStalactitesAreReal() = flipped08().leftTo(10f).expect(WorldState.DEAD)
    @Test fun level08TheDoorIsOnTheCeiling() = flipped08().wait(1f).also {
        assertTrue("the door hangs a few steps right of the start", it.world.door.box.y < 3f && it.world.door.box.x in 5f..8f)
        assertTrue("upside down", it.world.gravity < 0f)
    }.expect(WorldState.PLAYING)
    /** The room turns over in mid-hop: whoever keeps flying right lands in the spikes of the ceiling. */
    @Test fun level08FlyingOnLandsInTheCeilingSpikes() = b(8).hopR(19.5f, 0.5f).right(1f).expect(WorldState.DEAD)

    /** 9: the pothole opens in the deck ahead of you; and after the landing behind it the keys are swapped. */
    @Test fun level09RunningStraightIntoThePotholeDies() = b(9).right(3f).expect(WorldState.DEAD)
    @Test fun level09TheKeysSwapOnTheLandingAndPushYouBack() {
        val pushed = b(9).hopR(11.2f, 0.5f).right(1f)
        pushed.expect(WorldState.PLAYING)
        assertTrue(pushed.world.swapped && pushed.world.player.box.cx < 16f)
    }
    private fun World.said() = events.filterIsInstance<Event.Say>().map { it.text.en }
    /** Whoever drops through the pothole to the switch first still gets the swap behind the pit, and Mephi owns up to it. */
    @Test fun level09TheSwapAfterTheShortcutIsARepairThatExpired() {
        val bot = b(9).rightTo(11.5f).rightUntil { !it.player.grounded }.leftUntil { it.player.grounded }
            .leftUntil { it.pads[0].presses >= 1 }
        assertFalse("keys fixed before the landing", bot.world.swapped)
        bot.hopR(10.4f, 0.5f).wait(0.2f).expect(WorldState.PLAYING)
        assertTrue("the swap still comes", bot.world.swapped)
        val said = bot.world.said()
        assertTrue(said.toString(), "Repair expired. Warranty void." in said && "Left is the new right." !in said)
        // the door stays unlocked: the way there is the pothole on the ground with swapped hands
        bot.hopS(17.2f, 0.5f).leftKeyRightTo(30f).expect(WorldState.WON)
    }
    /** The say lint (H19, locked kit) reads [Action.Say.text] only: the line for the late swap is checked against act 1 here. */
    @Test fun level09TheExpiredRepairIsSaidNowhereElseInTheAct() {
        val late = World1.levels[8].traps.flatMap { it.actions }.filterIsInstance<Action.Say>().mapNotNull { it.otherwise }.single()
        val elsewhere = (World1.levels.take(16) - World1.levels[8]).flatMap { DesignRules.lines(it) }.map { it.text }
        assertTrue(late.en, elsewhere.none { DesignRules.sayKey(it.en) == DesignRules.sayKey(late.en) })
        assertTrue(late.de, elsewhere.none { DesignRules.sayKey(it.de) == DesignRules.sayKey(late.de) })
    }
    /** The intended route lands behind the pothole before the switch: the plain swap, no excuses. */
    @Test fun level09TheIntendedRouteHearsNoExpiredRepair() {
        val said = b(9).hopR(11.2f, 0.5f).right(1f).world.said()
        assertTrue(said.toString(), "Left is the new right." in said && "Repair expired. Warranty void." !in said)
    }

    /** 10: the wall is slower than you, but it catches whoever stops at the stairs; the spike bed is where the hop lands. */
    @Test fun level10StoppingAtTheStairsIsBeingCaught() = b(10).leftTo(6.6f).wait(6f).expect(WorldState.DEAD)
    @Test fun level10TheSpikeBedGrowsWhereTheHopLands() = b(10).leftTo(6.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.5f)
        .rightJump(0.5f).landRight().rightTo(8.2f).rightJump(0.5f).landRight().right(3f).expect(WorldState.DEAD)

    /** 11: the pothole in the deck, the fish in the creek, the stones. */
    @Test fun level11RunningStraightIntoThePotholeDies() = b(11).left(4f).expect(WorldState.DEAD)
    private fun bank11() = b(11).leftTo(12.6f).leftJump(0.5f).landLeft().left(1.0f).landLeft()
    @Test fun level11TheFishBitesWhoeverHopsWithoutLooking() = bank11().rightTo(2.3f).wait(0.25f).rightJump(0.3f).landRight().expect(WorldState.DEAD)
    @Test fun level11TheBankIsSafeToWaitOn() = bank11().rightTo(2.3f).wait(3f).expect(WorldState.PLAYING)
    /** Hopping back to the left bank off the first stone strands nobody: the bank gives way behind it. */
    @Test fun level11RetreatingFromTheCrumblingStoneDies() {
        var last = 0f
        bank11().rightTo(2.3f).waitFor { w -> val y = w.saws.first().y; val down = y > last; last = y; down && y in 15.6f..16.4f }
            .rightJump(0.3f).landRight().rightTo(8.4f).leftTo(3.0f).wait(3f).expect(WorldState.DEAD)
    }

    /** 12: after the first landing the keys are swapped, and going on with the old reflex runs back over the spike. */
    @Test fun level12KeepingTheOldKeysRunsIntoTheFirstSpike() = b(12).leftTo(26.4f).leftJump(0.5f).landLeft().left(2f).expect(WorldState.DEAD)

    /** 12: walking home with swapped keys but without hopping meets the pit. */
    @Test fun level12WalkingIntoThePitDies() = b(12).leftTo(26.4f).leftJump(0.5f).landLeft().rightKeyLeftTo(11.5f).wait(1f).expect(WorldState.DEAD)

    /** 12: the keys come back on the landing behind the last spike, so the old swapped habit now walks back into the spike. */
    @Test fun level12TheKeysComeBackOnTheLandingBehindTheLastSpike() {
        val home = b(12).leftTo(26.4f).leftJump(0.5f).landLeft().hopSL(14.4f, 0.5f).hopSL(8.6f, 0.5f).wait(0.1f)
        assertFalse("the keys are straight again", home.world.swapped)
        home.right(1.2f).expect(WorldState.DEAD)   // the old key walks back into the spike
    }

    /** 13: up on the floor the ceiling drops on whoever stops under it; down on the ground the spikes in the ceiling are a bluff, the plain ceiling is not. */
    @Test fun level13StandingUnderTheCeilingOnTheUpperFloorIsFatal() = b(13).leftTo(22.8f).wait(1f).expect(WorldState.DEAD)
    /** Down on the ground floor, the long way: past the first piece (waited for and hopped) and under the second. */
    private fun down13() = b(13).leftTo(25.5f).wait(0.4f).leftJump(0.5f).landLeft().leftTo(3.8f)
    @Test fun level13RunningAtTheFirstPieceIsFatal() = b(13).left(2f).expect(WorldState.DEAD)
    @Test fun level13TheSpikesInTheCeilingNeverFall() = down13().rightTo(7.5f).wait(2f).expect(WorldState.PLAYING)
    @Test fun level13StandingUnderThePlainCeilingIsFatal() = down13().rightTo(12.8f).wait(1f).expect(WorldState.DEAD)
    @Test fun level13RunningUnderThePlainCeilingIsFatal() = down13().right(2f).expect(WorldState.DEAD)

    /** 14: both lifts go too far, and the floor the first one leaves behind goes with it; the ground floor on the way to the door gives way. */
    @Test fun level14RunningHomeAlongTheGroundFloorFallsIn() = b(14).right(0.3f).waitFor { it.group('a').oy <= -5.6f }.rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(25.6f).waitFor { it.group('b').oy >= 2.6f }.left(3f).expect(WorldState.DEAD)
    @Test fun level14TheFirstLiftCarriesYouIntoTheSpikes() = b(14).right(0.3f).wait(4f).expect(WorldState.DEAD)
    @Test fun level14SteppingOffTheRisingLiftIsFatal() = b(14).right(0.3f).wait(0.4f).left(1.5f).wait(3f).expect(WorldState.DEAD)
    @Test fun level14TheSecondLiftCrushesYouIntoTheGround() = b(14).right(0.3f).waitFor { it.group('a').oy <= -5.6f }.rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(25.6f).wait(4f).expect(WorldState.DEAD)
    /** Bumping into the side of the lift must not set it off without the player on board. */
    @Test fun level14LiftWaitsForAPassenger() {
        val bump = b(14).wait(2f)
        bump.expect(WorldState.PLAYING)
        assertEquals(0f, bump.world.group('a').oy, 0.05f)
    }

    /** 15: the door is not where it stood, and the spiked blocks come down on whoever stops under them. */
    @Test fun level15TheDoorTakesTheLongWay() {
        val run = b(15).rightTo(16f).wait(3.5f)
        run.expect(WorldState.PLAYING)
        assertTrue("door on the upper floor, x=${run.world.door.box.x}", run.world.door.box.x in 18f..21f && run.world.door.box.y < 9f)
    }
    @Test fun level15RunningUnderTheLowerBlockIsFatal() = b(15).right(3f).expect(WorldState.DEAD)
    @Test fun level15StoppingUnderTheUpperBlockIsFatal() = b(15).rightTo(19.1f).wait(0.75f).leftTo(16.5f).hopR(19.25f, 0.5f)
        .rightTo(24.6f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(0.4f).leftJump(0.5f).landLeft().leftTo(24.6f)
        .wait(1.5f).expect(WorldState.DEAD)
    @Test fun level16HoldingRightMeetsTheLockedWall() {
        val locked = b(16).right(4f)
        locked.expect(WorldState.PLAYING)
        assertTrue(locked.world.group('w').visible && locked.world.player.box.cx < 24f)
    }
    /** 16 up to the switch: the ledge behind the stairs gives way under whoever stays on it. */
    private fun climbed16() = b(16).leftTo(15.0f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft()
    @Test fun level16TheLedgeGivesWayUnderWhoeverStays() {
        val stayed = climbed16().wait(1.5f)
        stayed.expect(WorldState.PLAYING)
        // down on the floor again with the block, the switch not pressed: the wall is still there
        assertTrue(stayed.world.player.box.b > 13f && stayed.world.group('w').visible)
    }
    /** 16: the switch opens the copper wall and the wall behind the door: the door slips into the second room before you get there. */
    @Test fun level16TheSwitchSendsTheDoorThroughTheBreach() {
        val pressed = climbed16().left(0.7f).landLeft().wait(1f)
        pressed.expect(WorldState.PLAYING)
        assertTrue("the copper wall is open", !pressed.world.group('w').visible)
        assertTrue("the door is in the second room, x=${pressed.world.door.box.x}", pressed.world.door.box.x > 32f)
    }
    /** 16 on the way to the breach: the ceiling drops a slab on whoever runs on. */
    @Test fun level16RunningOnUnderTheSlabIsFatal() = climbed16().left(0.7f).landLeft().rightJump(0.5f).landRight().rightTo(7.4f).rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(3f).expect(WorldState.DEAD)
    @Test fun level16TheWallBehindTheDoorBreaksOpen() {
        val b = Bot(World1.levels[15]).apply(World1DesignTest.SOLUTIONS.getValue(16)[0])
        b.expect(WorldState.WON)
        assertEquals(Card.ANNEX, b.world.lastCard)
        assertEquals(1, b.world.room)
        assertTrue(b.world.player.box.cx > 32f)
    }

    // ---------- Act 1: the obvious run dies at the second or third trap ----------

    @Test
    fun actOneChainsPunishTheCounterJustLearned() {
        // 2: one hop over the spike and a steady run ends in the spikes behind the landing; three hops end in the pit
        b(2).rightTo(8f).rightJump(0.35f).landRight().right(2f).expect(WorldState.DEAD)
        b(2).rightTo(8f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(2f).expect(WorldState.DEAD)
        // 3: the door comes back down when you reach the ledge, so nobody walks in on the ledge
        val ledge = b(3).hopR(21.2f).rightTo(26.5f).leftTo(24.3f).leftJump(0.35f).landLeft()
            .rightTo(21.3f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.35f).landRight().wait(0.45f)
        ledge.expect(WorldState.PLAYING)
        assertEquals(13.4f, ledge.world.door.box.y, 0.05f)
        // 4: the second slab falls where the wall-top hop lands: sprinting on is fatal
        b(4).rightTo(13.12f).wait(0.5f).leftTo(10.2f).wait(0.5f).rightTo(11.0f).rightJump(0.35f).landRight()
            .rightTo(17.3f).waitFor { it.player.grounded }.right(2f).expect(WorldState.DEAD)
        // 5: the wall top is a bad place to keep walking
        b(5).rightTo(18.5f).jump(0.3f).wait(0.5f).leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f)
            .right(2f).expect(WorldState.DEAD)
    }

    @Test
    fun actOneLevelsFromThreeOnChainTwoToFourTraps() {
        // a chain is two to four moments: traps with the same trigger (a slab and the one after it) are one
        for (n in 3..16) {
            val moments = World1Part1.levels[n - 1].traps.map { it.trigger }.distinct().size
            // the finale chains the act: two rooms, up to four moments each
            assertTrue("level $n has $moments trap moments", moments in (if (n == 16) 4..8 else 2..4))
        }
        assertEquals(2, World1Part1.levels[0].traps.map { it.trigger }.distinct().size)
    }

    @Test
    fun actOneTriggersFireCloseToTheSpot() {
        // no level of act 1 fires a trap on a plain PastX/BeforeX more than two tiles before the thing it springs on you
        // (the level notes say where); the jump triggers exist so that most of them need no such guess
        val jumpy = World1Part1.levels.count { l -> l.traps.any { it.trigger is Trigger.Airborne || it.trigger is Trigger.Landed } }
        assertTrue("$jumpy levels use jump triggers", jumpy >= 8)
    }

    // ---------- Act 2: Neue Regeln ----------

    /** Walks back and forth near the start (never idle, never far) until the clock reaches [t]. */
    private fun Bot.pace(t: Float): Bot {
        while (world.time < t && world.state == WorldState.PLAYING) right(0.2f).left(0.2f)
        return this
    }

    @Test fun level17() = World1DesignTest.play(17)
    @Test fun level18() = World1DesignTest.play(18)
    @Test fun level19() = World1DesignTest.play(19)
    @Test fun level20() = World1DesignTest.play(20)
    @Test fun level21() = World1DesignTest.play(21)
    @Test fun level22() = World1DesignTest.play(22)
    @Test fun level23() = World1DesignTest.play(23)
    @Test fun level24() = World1DesignTest.play(24)
    @Test fun level17Rematch() = World1DesignTest.play(17, 2)
    @Test fun level18Rematch() = World1DesignTest.play(18, 2)
    @Test fun level21Rematch() = World1DesignTest.play(21, 2)
    @Test fun level24Rematch() = World1DesignTest.play(24, 2)
    // levels 25-32 are rebuilt: their clean run is the registered solution (World1RoomsC), the rematch of 28 is in World1DeckTest
    @Test fun level25() = World1DesignTest.play(25)
    @Test fun level26() = World1DesignTest.play(26)
    @Test fun level27() = World1DesignTest.play(27)
    @Test fun level28() = World1DesignTest.play(28)
    @Test fun level29() = World1DesignTest.play(29)
    @Test fun level30() = World1DesignTest.play(30)
    @Test fun level31() = World1DesignTest.play(31)
    @Test fun level32() = World1DesignTest.play(32)

    // ---------- Act 3: Mephi schummelt ----------
    // levels 33-40 are rebuilt: their clean run is the registered solution (World1RoomsD); the rematches of 33 and 37 are in World1DeckTest
    @Test fun level33() = World1DesignTest.play(33)
    @Test fun level34() = World1DesignTest.play(34)
    @Test fun level35() = World1DesignTest.play(35)
    @Test fun level36() = World1DesignTest.play(36)
    @Test fun level37() = World1DesignTest.play(37)
    @Test fun level38() = World1DesignTest.play(38)
    @Test fun level39() = World1DesignTest.play(39)
    @Test fun level40() = World1DesignTest.play(40)

    // ---------- Act 3, levels 33-40: the obvious reflexes meet their trap ----------

    /** 33: running straight on, the strip of the road is pulled away under you; and reaching the door, the road goes on instead of ending. */
    @Test fun level33RunningStraightOnFallsIntoTheGap() = b(33).right(3f).expect(WorldState.DEAD)
    @Test fun level33TheEndOfTheRoadIsALie() {
        val run = b(33).also(World1DesignTest.SOLUTIONS.getValue(33)[0]).also { it.expect(WorldState.WON) }
        assertEquals(1, run.world.room)
        assertTrue("the door slipped into the second room", run.world.door.box.cx > 32f)
        val pressed = b(48).hopR(2.5f, 0.5f).hopR(4.7f, 0.5f).rightTo(8.4f).waitFor { it.group('S').oy >= 7.5f }.rightTo(11.8f)
            .waitFor { it.ropeUpE(14f, 0f, from = 0.643f, to = 0.843f) }.rightUntil { it.pads[0].down }.right(0.6f).waitFor { it.group('V').oy >= 11.5f }.rightTo(28.6f)
        assertEquals(0, pressed.world.room)
        assertTrue("the door left before it was reached", pressed.world.door.box.cx > 32f)
    }

    /** 34: the wall is closed until you pause for real; the button on screen dodges; the floor behind the wall is gone. */
    @Test fun level34TheWallStaysClosedUntilYouPause() = b(34).right(2f).also { assertTrue(it.world.group('w').visible) }.expect(WorldState.PLAYING)
    @Test fun level34TheButtonOnScreenDodges() = b(34).rightTo(7f).tapPause().also { assertEquals(1, it.world.dodges) }.also { assertTrue(it.world.group('w').visible) }.expect(WorldState.PLAYING)
    @Test fun level34RunningThroughTheOpenedWallFallsIntoThePit() = b(34).rightTo(7f).tapPause().pauseResume().right(2f).expect(WorldState.DEAD)

    /** 35: the door runs from you, to the ledge and back down, so running along the ground never wins: the low road under the step is unplugged. */
    @Test fun level35TheDoorFlees() = b(35).rightTo(10f).wait(1.5f).also { assertTrue("the door left the ground at the far end", it.world.door.box.y < 12f) }.expect(WorldState.PLAYING)
    @Test fun level35TheLowRoadIsUnplugged() = b(35).right(4f).expect(WorldState.DEAD)
    @Test fun level35TheDoorComesBackDown() = b(35).also(World1DesignTest.SOLUTIONS.getValue(35)[0]).also { assertTrue(it.world.door.box.y > 12f) }.expect(WorldState.WON)
    /** 35: the platform you climbed by uploads itself into the ceiling a moment after the door comes down: whoever comes down late goes up with it. */
    private fun ledge35() = b(35).rightTo(15.6f).rightJump(0.5f).landRight().rightTo(19.9f).rightJump(0.5f).landRight().rightTo(23.2f).rightJump(0.5f).landRight()
    @Test fun level35WaitingUpThereMeetsTheRouter() = ledge35().wait(1.5f).expect(WorldState.DEAD)
    @Test fun level35ComingDownLateIsUploaded() = ledge35().leftTo(25.6f).left(0.4f).landLeft().wait(1.5f).expect(WorldState.DEAD)

    /** 36: the rope stops the runner, and so does the first piece of the frame when you run on under it instead of waiting for it. */
    private fun climbed36() = b(36).rightTo(4.2f).waitFor { it.ropeUpE(7f, 0f, from = 0.643f, to = 0.843f) }.rightTo(11.5f).waitFor { it.pieceLanded(0, 14f) }
        .hopR(12.2f, 0.4f).rightTo(15.4f).rightJump(0.5f).landRight()
    @Test fun level36RunningStraightOnMeetsTheRope() = b(36).right(3f).expect(WorldState.DEAD)
    @Test fun level36RunningOnUnderTheFirstPieceIsFatal() = b(36).rightTo(4.2f).waitFor { it.ropeUpE(7f, 0f, from = 0.643f, to = 0.843f) }.right(3f).expect(WorldState.DEAD)
    @Test fun level36TheFirstPieceIsTheStepUpToTheLedge() {
        val up = climbed36()
        up.expect(WorldState.PLAYING)
        assertTrue("up on the ledge, y=${up.world.player.box.b}", up.world.player.box.b < 12.5f)
    }
    @Test fun level36TheSecondPieceCrushesWhoRunsOnTheLedge() = climbed36().right(2f).expect(WorldState.DEAD)

    /** 37: standing still on the deleted floor is the end of you, and the step you leave is gone. */
    @Test fun level37StandingStillOnTheDeletedFloorDies() = b(37).rightTo(14f).wait(4f).expect(WorldState.DEAD)
    @Test fun level37TheFirstStepIsGoneOnceYouLeaveIt() {
        val run = b(37).rightTo(19.3f).rightJump(0.5f).landRight().wait(1.5f)
        assertFalse(run.world.group('u').visible)
    }

    /** 37: you start in the middle of the ground floor, and the door is up at the far left wall. */
    @Test fun level37TheDoorIsUpAtTheFarWall() {
        val w = b(37).world
        assertTrue(w.door.box.cx < 4f && w.door.box.y < 9f && w.player.box.cx > 11f)
    }

    /** 38: the first saw is for the runner, the mirrored keys for whoever does not read the screen, and the usher for whoever stays on the step. */
    @Test fun level38StayingOnTheStepMeetsTheUsher() {
        val won = b(38).also(World1DesignTest.SOLUTIONS.getValue(38)[0])
        won.expect(WorldState.WON)
        val usher = won.world.sprung.first { s -> s.trap.actions.any { a -> a is Action.Saw && a.y < 8f } }
        DesignRules.patientProbe(World1.levels[37], 0, World1DesignTest.SOLUTIONS.getValue(38)[0], usher.triggered).expect(WorldState.DEAD)
    }
    @Test fun level38RunningStraightOnMeetsTheSaw() = b(38).left(3f).expect(WorldState.DEAD)
    @Test fun level38TheOldKeyDoesNotGetYouUpTheStairs() {
        val run = b(38).leftUntil { w -> w.saws.any { it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.5f } }.leftJump(0.5f).landLeft().left(3f)
        assertTrue(run.world.player.box.cx > 8f)
    }

    /** 39: the shelf only moves with the tilt of the phone; the floor sample at the platform's edge is gone for a moment; the far bank gives way. */
    private fun upTheStairs39() = b(39).rightTo(3.5f).waitFor { it.group('s').oy >= 12.9f }.rightTo(4.0f).rightJump(0.5f).landRight().rightJump(0.45f).landRight()
        .rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightJump(0.45f).landRight()
    private fun onTheShelf39() = upTheStairs39().rightUntil { it.player.box.cx > 17.0f }.waitFor { it.group('h').visible }.rightUntil { it.player.box.cx > 23.0f }
    @Test fun level39WithoutTiltTheShelfStaysPut() = onTheShelf39().wait(3f).also { assertEquals(0f, it.world.group('a').ox, 0.01f) }.expect(WorldState.PLAYING)
    @Test fun level39RunningUnderTheTileIsFatal() = b(39).right(3f).expect(WorldState.DEAD)
    @Test fun level39RunningOnOverTheFloorSampleIsFatal() = upTheStairs39().rightUntil { it.player.box.cx > 17.0f }.right(3f).expect(WorldState.DEAD)
    @Test fun level39TheFarBankGivesWayAsYouRideIn() = onTheShelf39().tilt(1f).waitFor { it.group('a').ox >= 2.9f }.rightUntil { it.player.box.cx > 25.5f }.right(2f).expect(WorldState.DEAD)

    /** 40: after the panic the keys are swapped, so holding right runs back to the start. */
    @Test fun level40HoldingRightRunsBack() = b(40).right(5f).also { assertTrue("x=${it.world.player.box.cx}", it.world.player.box.cx < 5f) }.expect(WorldState.PLAYING)
    @Test fun level40TheMemoryTestBeamStopsTheRunner() = b(40).rightUntil { it.swapped }.leftKeyRightTo(25f).expect(WorldState.DEAD)
    /** 40: the keys come back at the end of the shelf and swap again on the ground floor: the old reflex runs into the pit. */
    @Test fun level40TheKeysSwapAgainOnTheGroundFloor() {
        val run = b(40).rightUntil { it.swapped }.leftKeyRightTo(12.0f).waitFor { w -> !w.beams.any { it.laser.id == 'A' && it.lit } }.leftKeyRightTo(21.0f)
            .leftUntil(3f) { !it.swapped }.rightUntil(3f) { it.player.box.b > 11f }.landRight().leftUntil { it.swapped }
        assertTrue(run.world.swapped)
        run.leftTo(8f).expect(WorldState.PLAYING)
        assertTrue("the old key now runs away from the door, x=${run.world.player.box.cx}", run.world.player.box.cx > 18f)
    }
    @Test fun level40ThePitOnTheGroundIsReal() = b(40).rightUntil { it.swapped }.leftKeyRightTo(12.0f).waitFor { w -> !w.beams.any { it.laser.id == 'A' && it.lit } }
        .leftKeyRightTo(21.0f).leftUntil(3f) { !it.swapped }.rightUntil(3f) { it.player.box.b > 11f }.landRight().leftUntil { it.swapped }
        .rightKeyLeftTo(3.2f).expect(WorldState.DEAD)
    // ---------- Act 3, levels 41-48 (block E): rebuilt, their clean run is the registered solution (World1RoomsE); the rematches of 42, 46, 47 and 48 are in World1DeckTest ----------
    @Test fun level41() = World1DesignTest.play(41)
    @Test fun level42() = World1DesignTest.play(42)
    @Test fun level43() = World1DesignTest.play(43)

    /** 43: the wall behind the pad, then the lift: waiting at its foot is being run over, staying on it is riding into the spikes. */
    private fun toTheLift43() = b(43).rightTo(4.4f).waitFor { it.ropeUp(7f, 0.6f) }.rightTo(9.5f).rightUntil { it.pads[0].down }.rightTo(13.5f)
        .waitFor { it.ropeUpE(18f, 0.42f, 0.3f, from = 0.643f, to = 0.843f) }
    @Test fun level43WaitingAtTheFootOfTheLiftIsBeingRunOver() = toTheLift43().rightTo(23.5f).wait(3f).expect(WorldState.DEAD)
    @Test fun level43StayingOnTheLiftRidesIntoTheSpikes() = toTheLift43().rightTo(25.5f).wait(4f).expect(WorldState.DEAD)
    @Test fun level44() = World1DesignTest.play(44)
    @Test fun level45() = World1DesignTest.play(45)

    /** 45: the top floor is deleted ahead of you, and the shelf under it is all spikes; on the ground the next pieces go the same way. */
    @Test fun level45RunningStraightOnFallsOntoTheSpikeShelf() = b(45).left(2.5f).expect(WorldState.DEAD)
    @Test fun level45TheGroundIsDeletedAheadToo() = b(45).hopL(26.4f, 0.5f).hopL(18.4f, 0.5f).hopL(11.4f, 0.5f).leftTo(5.0f).landLeft().right(3f).expect(WorldState.DEAD)
    @Test fun level45StartingByRunningRightGoesNowhere() = b(45).right(4f).also { assertTrue(it.world.player.box.cx > 29f) }.expect(WorldState.PLAYING)
    @Test fun level46() = World1DesignTest.play(46)
    @Test fun level47() = World1DesignTest.play(47)
    @Test fun level48() = World1DesignTest.play(48)

    /** 41: the wall in front of the aerial is too high: running on ends at its foot, under the last stalactites. */
    @Test fun level41TheWallStopsTheRunner() = b(41).right(6f).also { assertTrue("x=${it.world.player.box.cx}", it.world.player.box.cx < 20.5f) }.expect(WorldState.DEAD)
    /** 48: the switch is not only the key: it breaks the wall open, and the door slips into the second room before it is reached. */
    @Test fun level48TheDoorWasNeverTheEnd() {
        val run = b(48).also(World1DesignTest.SOLUTIONS.getValue(48)[0]).also { it.expect(WorldState.WON) }
        assertEquals(1, run.world.room)
        assertTrue("the door slipped into the second room", run.world.door.box.cx > 32f)
        val pressed = b(48).hopR(2.5f, 0.5f).hopR(4.7f, 0.5f).rightTo(8.4f).waitFor { it.group('S').oy >= 7.5f }.rightTo(11.8f)
            .waitFor { it.ropeUpE(14f, 0f, from = 0.643f, to = 0.843f) }.rightUntil { it.pads[0].down }.right(0.6f).waitFor { it.group('V').oy >= 11.5f }.rightTo(28.6f)
        assertEquals(0, pressed.world.room)
        assertTrue("the door left before it was reached", pressed.world.door.box.cx > 32f)
    }
    // ---------- Acts 2 and 3: chains around the mechanics ----------

    @Test
    fun actTwoAndThreeChainsPunishTheCounterJustLearned() {
        // 17: standing on the floor that is cut away, and running on over the pit
        b(17).hopR(5.8f).wait(3f).expect(WorldState.DEAD)
        b(17).hopR(5.8f).rightTo(15.0f).right(3f).expect(WorldState.DEAD)
        // 18: the first saw comes at the runner, and nobody waits in the lane
        b(18).right(3f).expect(WorldState.DEAD)
        b(18).rightUntilSaw(4.5f).rightJump(0.35f).landRight().wait(3f).expect(WorldState.DEAD)
        // 19: running on over the second slab, and straight along the lower floor
        b(19).hopR(7.9f).hopR(13.8f).rightTo(17.0f).rightJump(0.35f).landRight().rightTo(20.4f).rightJump(0.35f).landRight().right(3f).expect(WorldState.DEAD)
        b(19).right(4f).expect(WorldState.DEAD)
        // 20: the first stone blinks out under whoever stays, and the way back along the top does not wait either
        b(20).hopR(5.8f, 0.3f).wait(8f).expect(WorldState.DEAD)
        b(20).hopR(5.8f, 0.3f).hopR(10.8f, 0.3f).hopR(15.8f, 0.3f).hopR(20.8f, 0.3f).rightJump(0.35f).landRight().rightJump(0.35f).landRight()
            .leftTo(28.6f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().left(4f).expect(WorldState.DEAD)
        // 21: the second hop lands on the strip that is not there, and whoever stays on the stairs goes down with them
        b(21).hopR(7.4f).hopR(13.6f).right(3f).expect(WorldState.DEAD)
        b(21).hopR(7.4f).hopR(13.6f).rightTo(20.2f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.3f).landRight().wait(4f).expect(WorldState.DEAD)
        // 22: the first beam stops whoever walks in on it, the rewind to the scanner whoever runs on at once, and the second beam too
        b(22).rightTo(10.6f).right(3f).expect(WorldState.DEAD)
        b(22).rightTo(10.6f).waitFor { it.gateOpen('A') }.rightTo(21.0f).right(2f).expect(WorldState.DEAD)
        b(22).rightTo(10.6f).waitFor { it.gateOpen('A') }.rightTo(20.0f).rightUntil { it.player.box.cx < 19.5f }.waitFor { it.gateOpen('S') }
            .rightTo(24.6f).leftUntil { it.player.grounded }.left(1f).expect(WorldState.DEAD)
        // 23: the first saw on the top plank, and the knot above the log: whoever hops it and stops is sawn
        b(23).rightTo(5.0f).right(3f).expect(WorldState.DEAD)
        b(23).rightTo(5.0f).waitFor { w -> w.saws.any { it.path != null && it.x < 9f && it.y < 2.8f } }.rightTo(9.0f).rightJump(0.3f).landRight().wait(2f).expect(WorldState.DEAD)
        // 24: HEAD catches whoever stands still, and the second branch whoever walks into it
        b(24).hopR(6.8f, 0.3f).wait(4f).expect(WorldState.DEAD)
        b(24).hopR(6.8f, 0.3f).hopR(17.4f, 0.3f).rightTo(24.4f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().left(5f).expect(WorldState.DEAD)
        // 41-48 are rebuilt: the naive runs of every rebuilt level (hold right, hop right) are checked by H2 in World1DesignTest
    }

    @Test
    fun actTwoAndThreeChainTwoOrMoreTrapsAndAverageTwoAndAHalf() {
        val all = World1Part2.levels + World1Part3.levels
        all.forEachIndexed { i, l -> assertTrue("level ${i + 17} has ${l.traps.size} traps", l.traps.size >= 2) }
        val counts = all.map { it.traps.size }
        assertTrue("average ${counts.average()}", counts.average() >= 2.5)
        assertTrue("act 2 average ${World1Part2.levels.map { it.traps.size }.average()}", World1Part2.levels.map { it.traps.size }.average() >= 2.5)
    }

    @Test
    fun actTwoAndThreeTriggersFireOnTheSpot() {
        val jumpy = (World1Part2.levels + World1Part3.levels).count { l -> l.traps.any { it.trigger is Trigger.Airborne || it.trigger is Trigger.Landed } }
        assertTrue("$jumpy levels use jump triggers", jumpy >= 15)
    }

    /** A dropped floor falls out of the world: nothing solid of another group lies below it to stand on afterwards. */
    @Test
    fun noTrapDropsThePlayerOntoAFloorBelow() {
        (World1Part2.levels + World1Part3.levels).withIndex().flatMap { (i, lv) -> lv.rounds.map { i to it } }.forEach { (i, l) ->
            if (i + 17 == 30) return@forEach   // the tetrominoes are meant to land
            val falling = l.traps.flatMap { it.actions }.filterIsInstance<Action.Fall>().map { it.group }.toSet()
            for (g in falling) for (y in 0 until l.rows) for (x in 0 until l.cols) {
                if (l.map.grid[y][x] != g || l.glyph(g)?.spike == true) continue
                for (yy in y + 1 until l.rows) {
                    val c = l.map.grid[yy][x]
                    val solid = c != '.' && c != g && c != 'P' && c != 'D' && l.glyph(c)?.spike == false && l.glyph(c)?.hidden != true
                    assertFalse("level ${i + 17}: group '$g' at ($x,$y) falls onto '$c' at ($x,$yy)", solid)
                }
            }
        }
    }

    /** Whoever stays on a piece that drops falls out of the world: a death, never a ledge to wait on. */
    @Test
    fun droppedFloorsLeaveNothingToWaitOn() {
        b(17).hopR(5.8f).wait(4f).also { assertTrue(it.world.player.box.y > it.world.rows - 1f) }.expect(WorldState.DEAD)
    }
}
