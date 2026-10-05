package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue(World1.levels[24].traps.any { it.trigger is Trigger.Idle })
    }

    @Test
    fun actOneAndTwoDoNotUseMetaTwists() {
        val meta = { a: Action ->
            a is Action.FakeWin || a is Action.PauseTrap || a is Action.FrameCrack || a is Action.Flip || a is Action.Roll || a is Action.Ghost
        }
        assertTrue((World1Part1.levels + World1Part2.levels).none { l -> actions(l).any(meta) })
        assertEquals(setOf("FakeWin", "PauseTrap", "FrameCrack", "Flip", "Roll", "Ghost"),
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

    @Test
    fun heisenbugPunishesStandingStillAndPatienceRewardsIt() {
        b(25).wait(1.9f).expect(WorldState.PLAYING)
        b(25).rightTo(3.2f).wait(1.5f).right(0.05f).wait(1.5f).right(3f).expect(WorldState.DEAD)
    }

    @Test
    fun panicButtonIsASpike() {
        val bot = b(42).rightTo(7.5f)
        assertFalse(bot.world.pausePressed())
        bot.wait(0.3f).expect(WorldState.DEAD)
    }

    // ---------- Act 1: Die Karten ----------
    @Test fun level01() = b(1).rightTo(17.6f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level02() = b(2).rightTo(8f).rightJump(0.35f).landRight().rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level03() = b(3).hopR(21.2f).rightTo(26.5f)
        .leftTo(24.3f).leftJump(0.35f).landLeft()
        .rightTo(21.3f).rightJump(0.35f).landRight()
        .rightTo(25.6f).rightJump(0.35f).landRight()
        .leftTo(23f).right(4f)
        .expect(WorldState.WON)
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
    @Test fun level04() = b(4).rightTo(13.12f).wait(0.7f).leftTo(10.4f).wait(0.4f).rightTo(11.6f).rightJump(0.35f).landRight()
        .rightTo(17.3f).waitFor { it.player.grounded }.wait(0.7f)
        .leftTo(17.6f).rightTo(19f).rightJump(0.35f).landRight().wait(0.5f)
        .rightJump(0.35f).landRight().right(1f).left(2f)
        .expect(WorldState.WON)
    @Test fun level05() = b(5).rightTo(18.5f).jump(0.3f).wait(0.5f)
        .leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f)
        .rightTo(21.4f).wait(0.6f)
        .rightTo(22.4f).rightJump(0.35f).landRight().rightJump(0.35f).right(2f)
        .expect(WorldState.WON)
    @Test fun level06() = b(6).rightTo(12.8f).rightJump(0.3f).rightTo(18.6f).rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
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

    /** 7: hopping back off the first panel strands nobody on the start floor: it gives way. */
    @Test fun level07RetreatingFromTheSunkPanelDies() {
        val back = b(7).hopR(16.0f).leftTo(18.3f).leftJump(0.35f).landLeft().left(0.3f).wait(0.3f)
        back.expect(WorldState.PLAYING)
        assertTrue("back on the start floor at x=${back.world.player.box.cx}", back.world.player.box.cx < 17f && back.world.player.grounded)
        back.wait(3f).expect(WorldState.DEAD)
    }
    @Test fun level07StandingOnAPanelSinksWithIt() = b(7).hopR(16.0f).wait(2f).expect(WorldState.DEAD)

    /** 8: running on over the stalactites that just popped up ends in them; the keys have not changed. */
    @Test fun level08TheStalactitesAreReal() = b(8).hopR(19.5f, 0.5f).rightTo(25.5f).landRight().leftTo(10f).expect(WorldState.DEAD)
    @Test fun level08TheDoorIsOnTheCeiling() = b(8).hopR(19.5f, 0.5f).wait(1f).also {
        assertTrue("the door hangs over the start", it.world.door.box.y < 3f && it.world.door.box.x < 3f)
    }.expect(WorldState.PLAYING)

    /** 9: the pothole opens in the deck ahead of you; and after the landing behind it the keys are swapped. */
    @Test fun level09RunningStraightIntoThePotholeDies() = b(9).right(3f).expect(WorldState.DEAD)
    @Test fun level09TheKeysSwapOnTheLandingAndPushYouBack() {
        val pushed = b(9).hopR(11.2f, 0.5f).right(1f)
        pushed.expect(WorldState.PLAYING)
        assertTrue(pushed.world.swapped && pushed.world.player.box.cx < 16f)
    }

    /** 10: the wall is slower than you, but it catches whoever stops at the stairs; the spike bed is where the hop lands. */
    @Test fun level10StoppingAtTheStairsIsBeingCaught() = b(10).leftTo(6.6f).wait(6f).expect(WorldState.DEAD)
    @Test fun level10TheSpikeBedGrowsWhereTheHopLands() = b(10).leftTo(6.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.5f)
        .rightJump(0.5f).landRight().rightTo(8.2f).rightJump(0.5f).landRight().right(3f).expect(WorldState.DEAD)

    /** 11: the pothole in the deck, the delivery on the bank, the stones. */
    @Test fun level11RunningStraightIntoThePotholeDies() = b(11).left(4f).expect(WorldState.DEAD)
    @Test fun level11TheBankDropsADeliveryOnWhoeverStays() = b(11).leftTo(12.6f).leftJump(0.5f).landLeft().left(1.0f).landLeft().wait(1f).expect(WorldState.DEAD)
    /** Hopping back to the left bank off the first stone strands nobody: the bank gives way behind it. */
    @Test fun level11RetreatingFromTheCrumblingStoneDies() = b(11).leftTo(12.6f).leftJump(0.5f).landLeft().left(1.0f).landLeft()
        .rightJump(0.12f).landRight().rightTo(8.8f).leftTo(3.0f).wait(3f).expect(WorldState.DEAD)

    /** 12: after the first landing the keys are swapped, and going on with the old reflex runs back over the spike. */
    @Test fun level12KeepingTheOldKeysRunsIntoTheFirstSpike() = b(12).leftTo(26.4f).leftJump(0.5f).landLeft().left(2f).expect(WorldState.DEAD)

    /** 12: walking home with swapped keys but without hopping meets the pit. */
    @Test fun level12WalkingIntoThePitDies() = b(12).leftTo(26.4f).leftJump(0.5f).landLeft().rightKeyLeftTo(11.5f).wait(1f).expect(WorldState.DEAD)

    /** 13: the spikes in the ceiling are a bluff, the plain ceiling is not. */
    @Test fun level13TheSpikesInTheCeilingNeverFall() = b(13).rightTo(7.5f).wait(2f).expect(WorldState.PLAYING)
    @Test fun level13StandingUnderThePlainCeilingIsFatal() = b(13).rightTo(13.5f).wait(1f).expect(WorldState.DEAD)
    @Test fun level13TheLoweringCeilingCatchesWhoeverStops() = b(13).rightTo(21.5f).wait(2f).expect(WorldState.DEAD)

    /** 14: both lifts go too far, and the floor the first one leaves behind goes with it. */
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
        assertTrue("door at the top left", run.world.door.box.x < 3f && run.world.door.box.y < 8f)
    }
    @Test fun level15StoppingUnderTheLowerBlockIsFatal() = b(15).rightTo(22.5f).wait(1.5f).expect(WorldState.DEAD)
    @Test fun level15StoppingUnderTheUpperBlockIsFatal() = b(15).rightTo(24.6f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(0.4f)
        .leftJump(0.5f).landLeft().leftTo(22.5f).wait(1.5f).expect(WorldState.DEAD)

    /** 16: the door is locked until the switch upstairs is pressed, and its door is not the end of the room. */
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
    /** 16 through the breach: the second room has a pit in the way, and the first three tiles are safe. */
    @Test fun level16RunningStraightThroughTheBreachMeetsThePit() {
        val through = climbed16().left(0.7f).landLeft().rightJump(0.5f).landRight().rightTo(7.4f).rightJump(0.35f).landRight()
            .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightUntil(4f) { it.cracks.isNotEmpty() }
            .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 3f))
        through.expect(WorldState.PLAYING)
        through.right(3f).expect(WorldState.DEAD)
    }
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
            .rightTo(21.3f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.35f).landRight().wait(1f)
        ledge.expect(WorldState.PLAYING)
        assertEquals(13.4f, ledge.world.door.box.y, 0.05f)
        // 4: the second slab falls where the wall-top hop lands: sprinting on is fatal
        b(4).rightTo(13.12f).wait(0.7f).leftTo(10.4f).wait(0.4f).rightTo(11.6f).rightJump(0.35f).landRight()
            .rightTo(17.3f).waitFor { it.player.grounded }.right(2f).expect(WorldState.DEAD)
        // 5: the wall top is a bad place to keep walking
        b(5).rightTo(18.5f).jump(0.3f).wait(0.5f).leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f)
            .right(2f).expect(WorldState.DEAD)
    }

    @Test
    fun actOneLevelsFromThreeOnChainTwoToFourTraps() {
        for (n in 3..16) {
            val traps = World1Part1.levels[n - 1].traps.size
            assertTrue("level $n has $traps traps", traps in 2..4)
        }
        assertEquals(2, World1Part1.levels[0].traps.size)
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
    @Test fun level25() = b(25).wait(2.2f).right(0.06f).wait(1.25f).left(0.06f).wait(1.3f).right(2f).expect(WorldState.WON)
    @Test fun level26() = b(26).rightTo(2.9f)
        .waitFor(14f) { it.solidLeft('a') > 1.2f }.rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('b') > 1.2f }.rightTo(8.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('c') > 1.2f }.rightTo(13.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('d') > 1.2f }.rightTo(18.6f).rightJump(0.35f).landRight()
        .rightTo(21.8f).rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightTo(10.7f).rightJump(0.35f).landRight().rightTo(16.7f).rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level28() = b(28).rightUntilSaw(4.5f).rightJump(0.35f).landRight()
        .rightTo(13.3f).rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level29() = b(29).hopR(5.7f).hopR(11.7f).hopR(17.7f).hopR(21.3f).right(2f).expect(WorldState.WON)
    /** Running past the stairs before they are built must not strand the player on the floor behind them. */
    @Test fun level30RunningPastTheStairsDies() = b(30).right(3f).wait(5f).expect(WorldState.DEAD)
    @Test fun level30() = b(30).waitUntil(8.3f).hopR(12.2f).hopR(15.0f).hopR(16.9f).hopR(19.7f).rightTo(24.8f).rightJump(0.35f).landRight().left(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightTo(4.3f).rightTo(7.4f).waitFor { it.saws[0].y < 6.3f }
        .rightTo(9.7f).rightJump(0.35f).landRight()
        .waitFor { it.saws[1].y < 6.3f }.rightTo(16.6f).rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightTo(5.8f)
        .fidgetUntil(10f) { it.solidLeft('a') > 1.6f }
        .rightTo(13.4f).fidgetUntil(10f) { it.solidLeft('b') > 1.2f }
        .rightTo(22.5f).rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 3: Mephi schummelt ----------
    @Test fun level33() = b(33).hopR(18.6f).right(2f)
        .also { assertEquals(FakeEnd.CLEAR, it.world.fake?.end) }
        .waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .hopL(22.6f).hopL(17.4f).hopL(8.6f).left(4f).expect(WorldState.WON)
    @Test fun level34() = b(34).wait(0.5f).tapPause().also { assertEquals(1, it.world.dodges) }
        .pauseResume().wait(0.1f).hopR(10.6f).right(4f).expect(WorldState.WON)
    @Test fun level35() = b(35).waitUntil(11.5f).right(2f).expect(WorldState.WON)
    @Test fun level36() = b(36).rightTo(10f).wait(1.6f).hopR(14.4f).hopR(19.7f).right(4f).expect(WorldState.WON)
    @Test fun level37() = b(37).hopR(10.7f).hopR(16.5f).rightTo(23.5f).wait(2.5f).hopL(20.5f).hopL(13.3f).hopL(8.5f).left(3f).expect(WorldState.WON)
    @Test fun level38() = b(38).rightTo(6.5f)
        .waitWhile(1f) { it.viewTurn() < 1f }
        .leftKeyRightTo(11.4f).leftJump(0.35f).landLeft()
        .waitWhile(5f) { it.viewTurn() > 0f }
        .hopR(16.6f).right(4f).expect(WorldState.WON)
    @Test fun level39() = b(39).rightTo(9.2f).wait(0.2f).tilt(1f).wait(2.4f).right(2f).expect(WorldState.WON)
    @Test fun level40() = b(40).rightTo(4.6f).wait(1.0f).leftKeyRightTo(17.7f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level41() = b(41).rightTo(10.2f).wait(1.2f).rightTo(20.7f).wait(1.2f).right(3f).expect(WorldState.WON)
    @Test fun level42() = b(42).hopR(9.2f).hopR(14.3f).hopR(20.5f).hopR(25.4f).right(1f).expect(WorldState.WON)
    @Test fun level43() = b(43).right(4f).also { it.expect(WorldState.DEAD) }
        .retry().hopR(13.6f).hopR(19.3f).hopR(24.4f).right(2f).expect(WorldState.WON)
    @Test fun level44() = b(44).rightTo(18f).shake().hopR(22.8f).right(3f).expect(WorldState.WON)
    @Test fun level45() = b(45).rightJump(0.35f).landRight().hopR(8f).hopR(18.2f).right(2f).expect(WorldState.WON)
    @Test fun level46() = b(46).left(1.2f).hopR(23.6f).right(2f).expect(WorldState.WON)
    @Test fun level47() = b(47).rightTo(10.5f).jump(0.3f).wait(0.4f).hopR(15.2f).rightJump(0.35f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level48() = b(48).rightTo(9f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .right(4f).also { assertEquals(FakeEnd.CREDITS, it.world.fake?.end) }
        .waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded }
        .leftTo(27.6f).leftJump(0.35f).landLeft()
        .leftTo(22.4f).leftJump(0.35f).landLeft()
        .leftTo(15.4f).leftJump(0.35f).landLeft()
        .left(3f).expect(WorldState.WON)

    // ---------- Acts 2 and 3: chains around the mechanics ----------

    /** Level 26 up to standing on the third step ('d'), ready for the last jump. */
    private fun skyscraperToD() = b(26).rightTo(2.9f)
        .waitFor(14f) { it.solidLeft('a') > 1.2f }.rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('b') > 1.2f }.rightTo(8.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('c') > 1.2f }.rightTo(13.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('d') > 1.2f }.rightTo(18.6f).rightJump(0.35f).landRight()

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
        // 22: the first beam stops whoever walks in on it, and so does the second
        b(22).rightTo(10.6f).right(3f).expect(WorldState.DEAD)
        b(22).rightTo(10.6f).waitFor { it.gateOpen('A') }.rightTo(25.5f).rightTo(28.0f).landRight().leftTo(21.0f).left(3f).expect(WorldState.DEAD)
        // 23: the first saw on the top plank, and the knot above the start
        b(23).rightTo(5.0f).right(3f).expect(WorldState.DEAD)
        b(23).rightTo(6.0f).jump(0.5f).wait(2f).expect(WorldState.DEAD)
        // 24: HEAD catches whoever stands still, and the second branch whoever walks into it
        b(24).hopR(6.8f, 0.3f).wait(4f).expect(WorldState.DEAD)
        b(24).hopR(6.8f, 0.3f).hopR(17.4f, 0.3f).rightTo(24.4f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().left(5f).expect(WorldState.DEAD)
        // 25: sitting too long and stepping too far to the left are both fatal
        b(25).wait(6f).expect(WorldState.DEAD)
        b(25).leftTo(1.5f).wait(0.5f).expect(WorldState.DEAD)
        // 26: running to the end of the last step lands on the ledge's spikes
        skyscraperToD().rightTo(21.8f).rightJump(0.35f).landRight().right(3f).expect(WorldState.DEAD)
        skyscraperToD().rightTo(23.6f).wait(0.5f).expect(WorldState.DEAD)
        // 27: the last stone crumbles under a runner
        b(27).rightTo(10.7f).rightJump(0.35f).landRight().rightTo(16.7f).rightJump(0.35f).landRight().right(2f).expect(WorldState.DEAD)
        // 28: a hidden pit right behind the first saw
        b(28).rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.DEAD)
        // 29: running across the fourth platform meets its spikes
        b(29).hopR(5.7f).hopR(11.7f).hopR(17.7f).hopR(23.7f).right(2f).expect(WorldState.DEAD)
        // 30: the top of the stairs
        b(30).waitUntil(8.3f).hopR(12.2f).hopR(15.0f).hopR(16.9f).hopR(19.7f).right(3f).expect(WorldState.DEAD)
        // 31: nobody waits on the island
        b(31).rightTo(4.3f).rightTo(7.4f).waitFor { it.saws[0].y < 6.3f }.rightTo(9.7f).rightJump(0.35f).landRight().wait(2.5f).expect(WorldState.DEAD)
        // 33: a hidden pit on the way to the door
        b(33).right(3f).expect(WorldState.DEAD)
        // 36: the block from the frame has spikes behind it
        b(36).rightTo(10f).wait(1.6f).hopR(14.4f).right(4f).expect(WorldState.DEAD)
        // 38: the spikes behind the landing
        b(38).rightTo(6.5f).waitWhile(1f) { it.viewTurn() < 1f }.leftKeyRightTo(11.4f).leftJump(0.35f).landLeft()
            .waitWhile(5f) { it.viewTurn() > 0f }.right(4f).expect(WorldState.DEAD)
        // 39: hopping onto the platform or off it is punished, only walking is not
        b(39).rightTo(5f).rightJump(0.35f).landRight().wait(0.5f).expect(WorldState.DEAD)
        b(39).rightTo(9.2f).wait(0.2f).tilt(1f).wait(2.4f).rightTo(23.6f).rightJump(0.35f).landRight().wait(0.5f).expect(WorldState.DEAD)
        // 40: the keys come back while you are over the hole in the ceiling
        val pushedBack = b(40).rightTo(4.6f).wait(1.0f).leftKeyRightTo(17.7f).leftJump(0.35f).landLeft()
        pushedBack.expect(WorldState.PLAYING)
        assertTrue(pushedBack.world.player.box.cx < 17f)   // still pressing the old key: sent back
        b(40).rightTo(4.6f).wait(1.0f).leftKeyRightTo(19f).wait(1f).expect(WorldState.DEAD)
        // 42: the obvious jump over the first pit lands in the spikes
        b(42).hopR(10.2f).wait(0.5f).expect(WorldState.DEAD)
        // 43: the ghost's attempt teaches the first hop, then the second one has spikes
        b(43).right(4f).also { it.expect(WorldState.DEAD) }.retry().hopR(13.6f).hopR(20.5f).right(2f).expect(WorldState.DEAD)
        // 44: jumping at the wall
        b(44).rightTo(17f).rightJump(0.35f).landRight().wait(0.5f).expect(WorldState.DEAD)
        // 45: climbing onto the shelf and running on
        b(45).rightJump(0.35f).landRight().right(2f).expect(WorldState.DEAD)
        // 47: the wall is down, the spikes behind it are not
        b(47).rightTo(10.5f).jump(0.3f).wait(0.4f).right(4f).expect(WorldState.DEAD)
        // 48: jumping off the bridge's end
        b(48).rightTo(9f).waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }.rightTo(15.4f).rightJump(0.35f).landRight().wait(0.5f).expect(WorldState.DEAD)
    }

    @Test
    fun actTwoAndThreeChainTwoOrMoreTrapsAndAverageTwoAndAHalf() {
        val all = World1Part2.levels + World1Part3.levels
        all.forEachIndexed { i, l -> assertTrue("level ${i + 17} has ${l.traps.size} traps", l.traps.size >= 2) }
        val counts = all.map { it.traps.size }
        assertTrue("average ${counts.average()}", counts.average() >= 2.5)
        assertTrue("act 2 average ${World1Part2.levels.map { it.traps.size }.average()}", World1Part2.levels.map { it.traps.size }.average() >= 2.5)
        // the meta twists already surprise: their chains stay short
        for (n in listOf(33, 34, 36, 37, 38, 40, 41, 42, 43, 45, 46, 48)) {
            assertTrue("twist level $n has ${World1.levels[n - 1].traps.size} traps", World1.levels[n - 1].traps.size <= 3)
        }
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
        b(31).rightTo(4.3f).rightTo(7.4f).waitFor { it.saws[0].y < 6.3f }.rightTo(9.7f).rightJump(0.35f).landRight().wait(3f)
            .also { assertTrue(it.world.player.box.y > it.world.rows - 1f) }.expect(WorldState.DEAD)
    }
}
