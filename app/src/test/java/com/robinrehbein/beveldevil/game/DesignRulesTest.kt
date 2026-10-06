package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The guard-rail kit itself ([DesignRules]): a good room passes every rule, the old habits are caught. */
class DesignRulesTest {
    private val puzzle = DesignDemos.puzzle
    private val solution = DesignDemos.puzzleSolution

    // ---------- a V2 room passes ----------

    @Test
    fun thePuzzleRoomPassesEveryRule() {
        val t = DesignRules.cleanRunTime(puzzle, 0, solution)
        assertTrue("clean run $t s", t >= DesignRules.minDuration(3, 20, d("R1+R5", "U1")))
        assertEquals(emptyList<String>(), DesignRules.densityViolations(puzzle, 0, solution, DesignRules.minDuration(3, 20, d("R1+R5", "U1"))))
        assertEquals(emptyList<String>(), DesignRules.holdRightWithHopsViolations(puzzle, 0))
        assertEquals(emptyList<String>(), DesignRules.slopViolations(puzzle, 0, solution))
        assertEquals(emptyList<String>(), DesignRules.teethViolations(puzzle, 0, solution))
        assertEquals(emptyList<String>(), DesignRules.fillerDeathViolations(puzzle, 0))
        assertEquals(emptyList<String>(), DesignRules.familyViolations(20, puzzle))
        assertEquals(emptyList<String>(), DesignRules.cardLintViolations(puzzle))
        assertEquals(emptyList<String>(), DesignRules.rotationViolations(mapOf(20 to puzzle)))
        assertEquals(1, DesignRules.spikePopupCount(puzzle))
        assertEquals(0, DesignRules.heatSpikeFinaleCount(puzzle))
        assertEquals(emptyList<String>(), DesignRules.spikeQuotaViolations(mapOf(20 to puzzle)))
        assertEquals(emptyList<String>(), DesignRules.cardSpreadViolations(mapOf(20 to puzzle)))
    }

    @Test
    fun thePuzzleRoomHasItsSurprisesWhereThePlanSaysSo() {
        // the copper wall stands until the switch: the door below the spawn is locked
        assertTrue(Bot(puzzle).world.circuits.getValue('w').powered)
        val won = DesignRules.cleanRun(puzzle, 0, solution)
        won.expect(WorldState.WON)
        assertFalse(won.world.circuits.getValue('w').powered)
        // four real traps in two families, the switch's block among them (U1), the card on it
        assertEquals(4, won.moments.count { it.real })
        assertEquals(setOf("spikes", "drop"), DesignRules.families(puzzle))
        assertTrue(won.moments.any { m -> m.actions.any { it is Action.Fall && it.group == 'c' } && m.weight == DesignRules.Weight.LETHAL })
        assertEquals(Card.COLLAPSE, DesignRules.cards(puzzle).single())
    }

    // ---------- the old habits are caught ----------

    @Test
    fun aCorridorIsTooShortWinsByHoldingRightAndSpamsSpikes() {
        val corridor = DesignDemos.corridor()
        val run: Solution = { right(5f) }
        assertTrue(DesignRules.cleanRunTime(corridor, 0, run) < 4f)
        assertEquals(1, DesignRules.cleanRunViolations(corridor, 0, run, DesignRules.minDuration(2, 12, d("R5", "U2"))).size)
        assertTrue(DesignRules.holdRightWithHopsViolations(corridor, 0).any { "holding right wins" in it })
        assertEquals(2, DesignRules.spikePopupCount(corridor))
        assertEquals(listOf("level 12 round 1: 2 spike popups"), DesignRules.spikeQuotaViolations(mapOf(12 to corridor)))
    }

    @Test
    fun aFifthSpikeLevelOrAFourthCardInAnActIsCaught() {
        val act = (17..21).associateWith { DesignDemos.corridor(Card.CRUMBLE) }
        assertTrue(DesignRules.spikeQuotaViolations(act).any { it.startsWith("act 2: 5 levels") })
        assertEquals(listOf("act 2: CRUMBLE played 5 times (max 3)"), DesignRules.cardSpreadViolations(act))
        // three are fine, and the grand finale may be played in a finale on top
        assertEquals(emptyList<String>(), DesignRules.cardSpreadViolations((17..19).associateWith { DesignDemos.corridor(Card.CRUMBLE) }))
        val finales = mapOf(16 to DesignDemos.corridor(Card.GRAND_FINALE)) + (1..3).associateWith { DesignDemos.corridor(Card.GRAND_FINALE) }
        assertEquals(emptyList<String>(), DesignRules.cardSpreadViolations(finales))
    }

    @Test
    fun anOverclockedLandingInFrontOfTheDoorCountsAsHeatSpikeFinale() {
        assertEquals(1, DesignRules.heatSpikeFinaleCount(DesignDemos.corridor(heat = true)))
        assertEquals(0, DesignRules.heatSpikeFinaleCount(DesignDemos.corridor()))
    }

    @Test
    fun heatSpikeFinaleLooksAtWhereTheDoorIs() {
        // door at the left, a heat spike far from it at 20 that is not the last trap
        val far = DesignDemos.leftDoor(heatAt = Trigger.PastX(20f))
        assertEquals(0, DesignRules.heatSpikeFinaleCount(far))
        // walking left onto the door, BeforeX is the natural trigger
        assertEquals(1, DesignRules.heatSpikeFinaleCount(DesignDemos.leftDoor(heatAt = Trigger.BeforeX(5f))))
    }

    @Test
    fun trapsInsideAFakeWinCount() {
        // a fake win that carries the spikes: Show inside the fake counts as the room's one spike popup
        val fake = Level(T("Fake", "Fake"), T("Fake", "Fake"), legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(trap(Trigger.AtDoor, Action.FakeWin(FakeEnd.CLEAR, null, Action.Show('A'))))) {
            border(); floor(); put(10, 14, 'A'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        assertEquals(1, DesignRules.spikePopupCount(fake))
        for (name in listOf("Burn-in Test")) {
            assertEquals(name, 1, DesignRules.heatSpikeFinaleCount(World3.levels.single { it.name.en == name }))
        }
    }

    @Test
    fun aPixelPerfectSolutionFailsTheSlopTest() {
        val needle = DesignDemos.needle
        val exact: Solution = { rightTo(10.1f).waitUntil(3.2f).right(4f) }
        assertTrue(DesignRules.cleanRunTime(needle, 0, exact) > 3f)
        assertFalse(DesignRules.solutionToleratesSlop(needle, 0, exact))
        // the same needle written as conditions on position and clock is no way around it
        val disguised: Solution = { rightUntil { it.player.box.cx >= 10.1f }.waitFor { it.time >= 3.2f }.right(4f) }
        assertTrue(DesignRules.cleanRunTime(needle, 0, disguised) > 3f)
        assertFalse(DesignRules.solutionToleratesSlop(needle, 0, disguised))
        assertFalse(DesignRules.solutionToleratesSlop(needle, 0) { rightUntil { it.player.box.cx >= 10.1f }.waitWhile { it.time < 3.2f }.right(4f) })
    }

    @Test
    fun idlePaddingDoesNotMakeARoomLonger() {
        val corridor = DesignDemos.corridor()
        val padded: Solution = { wait(9f).right(5f) }
        assertTrue(DesignRules.cleanRunTime(corridor, 0, padded) > 8f)
        assertTrue(DesignRules.cleanRunViolations(corridor, 0, padded, 8f).single().contains("without its idle waits"))
        val clocked: Solution = { waitFor(20f) { it.time >= 9f }.right(5f) }
        assertEquals(1, DesignRules.cleanRunViolations(corridor, 0, clocked, 8f).size)
        // the puzzle needs its one wait (for the block to lie), so skipping every wait loses; a stall up front is still caught by H3
        val stalled: Solution = { wait(20f); solution() }
        val v = DesignRules.densityViolations(puzzle, 0, stalled, 15f)
        assertTrue(v.joinToString("\n"), v.any { "start to first trap" in it } && v.any { "in one go" in it } && v.any { "% of the run" in it })
        // the honest puzzle loses nothing when idle commands are skipped
        assertEquals(emptyList<String>(), DesignRules.cleanRunViolations(puzzle, 0, solution, DesignRules.minDuration(3, 20, d("R1+R5", "U1"))))
    }

    @Test
    fun aWaitTheRoomNeedsStillCounts() {
        // the needle's spikes are only gone after 3 s: skipping the wait walks into them, so the wait is no padding
        val needle = DesignDemos.needle
        val patient: Solution = { waitFor { it.time >= 3.5f }.right(6f) }
        assertEquals(emptyList<String>(), DesignRules.cleanRunViolations(needle, 0, patient, 4f))
    }

    @Test
    fun slopMovesTargetsHoldsAndReactions() {
        val corridor = DesignDemos.corridor()
        val exact = Bot(corridor).rightTo(10f).world.player.box.cx
        val late = Bot(corridor, slop = Slop(0f, 1f)).rightTo(10f).world.player.box.cx
        assertEquals(exact + 1f, late, 0.1f)
        assertEquals(1.15f, Bot(corridor, slop = Slop(0.15f)).wait(1f).world.time, 0.01f)
        assertEquals(0.85f, Bot(corridor, slop = Slop(-0.15f)).wait(1f).world.time, 0.01f)
        // a condition is reacted to late or early as well
        assertEquals(1.15f, Bot(corridor, slop = Slop(0.15f)).waitFor { it.time >= 1f }.world.time, 0.01f)
        assertEquals(0.85f, Bot(corridor, slop = Slop(-0.15f)).waitFor { it.time >= 1f }.world.time, 0.01f)
        val ran = Bot(corridor).rightUntil { it.player.box.cx >= 10f }.world.player.box.cx
        assertTrue(Bot(corridor, slop = Slop(0.15f)).rightUntil { it.player.box.cx >= 10f }.world.player.box.cx > ran + 0.5f)
        assertTrue(Bot(corridor, slop = Slop(-0.15f)).rightUntil { it.player.box.cx >= 10f }.world.player.box.cx < ran - 0.5f)
        // rewinding replays the run exactly: early then on time ends where on time does
        assertEquals(Bot(corridor).right(1f).wait(0.85f).right(0.5f).world.player.box.cx,
            Bot(corridor, slop = Slop(-0.15f)).right(1.15f).waitFor { it.time >= 2f }.right(0.65f).world.player.box.cx, 0.001f)
        // a sloppy jump is still a jump
        assertTrue(Bot(corridor, slop = Slop(-0.15f)).jump(0.05f).right(0.4f).world.player.box.b < 15f)
    }

    // ---------- the §8 table ----------

    @Test
    fun h6CatchesTheRowsTheFirstDraftOfWorld2Act3Had() {
        // §8 as first written (meta lumped into U16, 42 led with R7, 45 repeated R9)
        val draft = mapOf(
            38 to d("R3", "U1"), 39 to d("R1", "U16"), 40 to d("–", "U16", breather = true), 41 to d("R7", "U1"),
            42 to d("R7+R5", "U15"), 43 to d("R4", "U16"), 44 to d("R9", "U16"), 45 to d("R9", "U3"),
        )
        assertEquals(
            listOf(
                "levels 39 and 40 share the main twist U16",
                "levels 41 and 42 share the main block R7",
                "levels 43 and 44 share the main twist U16",
                "levels 44 and 45 share the main block R9",
            ),
            DesignRules.h6Violations(draft),
        )
        assertEquals(emptyList<String>(), DesignRules.h6Violations(World2DesignTest.DESIGN.filterKeys { it in 38..45 }))
    }

    @Test
    fun h6CountsThreeOfFourButSparesFinalesAndTheLeadMechanic() {
        val t = mapOf(1 to d("R11", "U1"), 2 to d("R2", "U2"), 3 to d("R11", "U3"), 4 to d("R1+R11", "U6"))
        assertEquals(listOf("R11 in [1, 3, 4] (3 of levels 1–4)"), DesignRules.h6Violations(t))
        assertEquals(emptyList<String>(), DesignRules.h6Violations(t, lead = mapOf(1 to "R11")))
        val finale = mapOf(13 to d("R1", "U1"), 14 to d("R2", "U2"), 15 to d("R1", "U3"), 16 to d("R1+R5", "U1+U4"), 17 to d("R1", "U9"))
        assertEquals(emptyList<String>(), DesignRules.h6Violations(finale))
    }

    @Test
    fun theTableWantsAboutHalfPuzzleRooms() {
        // a world 2 act 3 where every row is a puzzle room again
        val allPuzzles = World2DesignTest.DESIGN + (33..47).filter { !World2DesignTest.DESIGN.getValue(it).breather }.associateWith { d("R5", "U1") }
        assertEquals(listOf("act 3: 14 of 14 rows are puzzle rooms (about half)"), DesignRules.puzzleShareViolations(2, allPuzzles))
        // and one with only trap rooms
        val noPuzzles = World2DesignTest.DESIGN + (33..47).filter { !World2DesignTest.DESIGN.getValue(it).breather }.associateWith { d("–", "U1") }
        assertEquals(listOf("act 3: 0 of 14 rows are puzzle rooms (about half)"), DesignRules.puzzleShareViolations(2, noPuzzles))
        // a trap room needs no block any more, a breather still has at most one
        assertEquals(emptyList<String>(), DesignRules.tableViolations(2, World2DesignTest.DESIGN, (1..48).toSet()))
        // W1's tutorial does not count
        assertEquals(emptyList<String>(), DesignRules.tableViolations(1, World1DesignTest.DESIGN, (1..48).toSet()))
    }

    @Test
    fun theTableKeepsTheRotation() {
        // the §8 rows before recipe v2: W1 act 1 had the fleeing door three times (10, 15, finale 16)
        val v1 = World1DesignTest.DESIGN + mapOf(10 to d("R6", "U3"), 16 to d("R1+R5+R6", "U7+U4+U18"))
        assertEquals(listOf("act 1: 3 rows with a fleeing door (R6/U4) [10, 15, 16] (max 1)"), DesignRules.tableRotationViolations(1, v1))
        val switches = World3DesignTest.DESIGN + mapOf(2 to d("R2", "U6"), 4 to d("R4", "U7"))
        assertEquals(listOf("act 1: 5 rows with a switch (R1/R2/R4) [1, 2, 4, 6, 16] (max 3)"), DesignRules.tableRotationViolations(3, switches))
        for (t in listOf(World1DesignTest.DESIGN to 1, World2DesignTest.DESIGN to 2, World3DesignTest.DESIGN to 3)) {
            assertEquals(emptyList<String>(), DesignRules.tableRotationViolations(t.second, t.first))
        }
    }

    @Test
    fun minimumDurationsFollowTheCurve() {
        val plain = d("R1", "U1")
        val star = d("–", "U1", breather = true)
        assertEquals(4f, DesignRules.minDuration(1, 6, d("–", "U7")))
        assertEquals(6f, DesignRules.minDuration(1, 7, plain))
        assertEquals(6f, DesignRules.minDuration(1, 17, plain))
        assertEquals(6f, DesignRules.minDuration(2, 10, plain))
        assertEquals(6f, DesignRules.minDuration(2, 25, d("–", "U1")))
        assertEquals(6f, DesignRules.minDuration(3, 1, plain))
        for (w in 1..3) for (n in DesignRules.FINALES) assertEquals(10f, DesignRules.minDuration(w, n, d("R1+R5", "U1+U4")))
        assertEquals(0f, DesignRules.minDuration(2, 40, star))
    }

    // ---------- recipe v2: density, teeth, filler deaths, families, rotation, cards, rematches ----------

    @Test
    fun aRoomThatMakesYouWaitIsNotDense() {
        // the needle: one real trap at 2 s, then 1.5 s of waiting it out and the walk to the door
        val needle = DesignDemos.needle
        val patient: Solution = { waitFor { it.time >= 3.5f }.right(6f) }
        val v = DesignRules.densityViolations(needle, 0, patient, 0f)
        assertTrue(v.joinToString("\n"), v.any { "no real trap for" in it && "last trap to door" in it })
        assertTrue(v.joinToString("\n"), v.any { "stands still" in it && "% of the run" in it })
        assertTrue(v.joinToString("\n"), v.any { "stands still 3." in it && "in one go" in it })
        // the hidden spikes going again (Hide) is no trap: only the Show counts
        val bot = DesignRules.cleanRun(needle, 0, patient)
        assertEquals(listOf(2f), bot.moments.filter { it.real }.map { Math.round(it.time * 10) / 10f })
        assertEquals(3.5f, bot.idleTime, 0.05f)
    }

    @Test
    fun padsTalkAndHelpfulChangesAreNoTraps() {
        val w = World(puzzle)
        fun weigh(a: Action) = DesignRules.weigh(a, puzzle, w)
        assertEquals(DesignRules.Weight.LETHAL, weigh(Action.Show('S')))
        assertEquals(DesignRules.Weight.LETHAL, weigh(Action.Fall('b')))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Say(T("Hi", "Hi"))))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Play(Card.COLLAPSE)))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Pad('2', 3 to 9)))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Show('w')))                 // a block appears: the switch did its job
        assertEquals(DesignRules.Weight.ROUTE, weigh(Action.DoorTo(20, 9)))
        assertEquals(DesignRules.Weight.ROUTE, weigh(Action.Swap(true)))
        // the copper rail switched off under you is a trap, switched on it is help
        assertEquals(DesignRules.Weight.LETHAL, weigh(Action.Power('w', false)))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Power('w', true)))
        // a belt counts when it pushes away from the door (on the left here), not when it helps
        assertEquals(DesignRules.Weight.LETHAL, weigh(Action.Belt('d', 5f)))
        assertEquals(DesignRules.Weight.NONE, weigh(Action.Belt('d', -5f)))
        // a laser switched off is help, a new beam a trap
        val l20 = World2.levels[19]
        assertEquals(DesignRules.Weight.NONE, DesignRules.weigh(Action.Power('K', false), l20, World(l20)))
        assertEquals(DesignRules.Weight.LETHAL, DesignRules.weigh(Action.Power('K', true), l20, World(l20)))
    }

    @Test
    fun aDoorTrailIsOneMoment() {
        // the door hopping along in several traps with the same trigger is one surprise, not three
        // (no World 2 room has a door trail since the v2 rebuild, so a small demo room shows it)
        val trail = Level(
            name = T("Demo: Door trail", "Demo: Türspur"),
            intro = T("", ""),
            traps = listOf(
                trap(Trigger.PastX(10f), Action.DoorTo(20, 14, speed = 30f)),
                trap(Trigger.PastX(10f), Action.DoorTo(4, 14, speed = 30f), delay = 0.6f),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P')
            put(28, 14, 'D')
        }
        val bot = DesignRules.cleanRun(trail, 0) { rightTo(12f).wait(1.5f) }
        val door = bot.moments.filter { m -> m.actions.any { it is Action.DoorTo } }
        assertEquals(1, door.size)
        assertEquals(2, door.single().actions.count { it is Action.DoorTo })
    }

    @Test
    fun aTrapThatWaitingAndRunningBothBeatIsDecoration() {
        val room = DesignDemos.toothless
        val run: Solution = { rightTo(18.2f).hopR(18.4f).right(3f) }
        DesignRules.cleanRun(room, 0, run).expect(WorldState.WON)
        val v = DesignRules.teethViolations(room, 0, run)
        assertEquals(v.joinToString("\n"), 1, v.size)
        assertTrue(v.single(), "has no teeth" in v.single())
    }

    @Test
    fun waitingEightSecondsDoesNotBeatTheRoomEither() {
        // standing still until the trap is gone, then holding right: the patient naive player
        val needle = DesignDemos.needle
        assertTrue(DesignRules.holdRightWithHopsViolations(needle, 0).any { "standing still 8 s, then holding right" in it })
    }

    @Test
    fun spikesInFrontOfTheSpawnAreAFillerDeath() {
        val v = DesignRules.fillerDeathViolations(DesignDemos.spawnSpikes, 0)
        assertEquals(1, v.size)
        assertTrue(v.single(), "before any trap went off" in v.single())
        // dying to a trap that went off is a gag, not filler: the puzzle room's spikes sprout ahead of whoever holds right
        assertEquals(DesignRules.Weight.LETHAL, Bot(puzzle).right(2f).also { it.expect(WorldState.DEAD) }.moments.first().weight)
        assertEquals(emptyList<String>(), DesignRules.fillerDeathViolations(puzzle, 0))
    }

    @Test
    fun effectFamiliesAreCountedPerRoom() {
        assertEquals(setOf("spikes"), DesignRules.families(DesignDemos.corridor()))
        assertEquals(setOf("spikes", "heat"), DesignRules.families(DesignDemos.corridor(heat = true)))
        // 2-16: two portals, pillars that come down, swapped controls: three families, fine for a finale only
        val l16 = World2.levels[15]
        assertEquals(setOf("portal", "move", "controls"), DesignRules.families(l16))
        assertEquals(emptyList<String>(), DesignRules.familyViolations(16, l16))
        assertTrue(DesignRules.familyViolations(15, l16).single().contains("(max 2)"))
    }

    @Test
    fun mechanicsRotateOverAnAct() {
        // the door flees in 2-3 and in a second room of act 1 (built here, 2-4 no longer has a fleeing door): twice in act 1
        val second = Level(T("d", "d"), T("d", "d"), traps = listOf(trap(Trigger.PastX(5f), Action.DoorTo(10, 14)))) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }
        val levels = mapOf(3 to World2.levels[2], 4 to second)
        assertEquals(
            listOf("act 1: door flees (DoorTo) in 2 levels [3, 4] (max 1)"),
            DesignRules.rotationViolations(levels).filter { "DoorTo" in it },
        )
        // one each in two acts is fine
        assertEquals(emptyList<String>(), DesignRules.rotationViolations(mapOf(3 to World2.levels[2], 21 to World2.levels[20])).filter { "DoorTo" in it })
    }

    // ---------- laser gates (H12) ----------

    /** A room whose only trap fires [laser] on [trigger]. */
    private fun gateRoom(trigger: Trigger, laser: Action.Laser, start: List<Action> = emptyList()) =
        Level(T("g", "g"), T("g", "g"), start = start, traps = listOf(trap(trigger, laser))) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    @Test
    fun aLaserFlashedOnATriggerIsAGateWhateverItsOffTime() {
        val flash = Action.Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f)
        // what the old filter (off < 10 s) let through: a one-shot flash on landing, passing, entering a zone
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.Landed(10f, 14f), flash)))
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash)))
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.Zone(10f, 5f, 14f, 9f), flash)))
        // whatever the trigger: a pad press, a timer, standing idle, touching a block
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.Pressed('1'), flash)))
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash)))
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.Idle(1f), flash)))
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.Touch('a'), flash)))
        // a short on-time without any off time (a one-shot) is one too
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash.copy(off = 0f))))
        // the cycling gate is still one
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash.copy(on = 1f, off = 2f))))
    }

    @Test
    fun aLongBeamOrAnAlwaysLitLaserIsNoGate() {
        val flash = Action.Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f)
        // lit for 5 s: a wall of light, not a gate, whatever fires it
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 5f))))
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash.copy(on = 5f))))
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.Pressed('1'), flash.copy(on = 5f))))
        // a laser from the start that is never dark is a fixture
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 5f), start = listOf(flash.copy(id = 'H', off = 0f)))))
        assertFalse(DesignRules.hasLaserGate(DesignDemos.corridor()))
    }

    @Test
    fun aFourthLaserGateInAnActBreaksTheRotation() {
        val flash = Action.Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f)
        val act = (18..21).associateWith { gateRoom(Trigger.Landed(10f, 14f), flash) }
        assertEquals(listOf("act 2: timed laser gates in 4 levels [18, 19, 20, 21] (max 3)"), DesignRules.rotationViolations(act))
        assertEquals(emptyList<String>(), DesignRules.rotationViolations(act.filterKeys { it != 21 }))
    }

    @Test
    fun theFinaleMayBringASwitchOnTopOfThreePadLevels() {
        // W2 act 2: 17, 18 and 20 have pads, and so does the finale 32 (Core Switch): fine
        val pad = DesignDemos.puzzle
        val plain = DesignDemos.corridor()
        val act = mapOf(17 to pad, 18 to pad, 19 to plain, 20 to pad, 32 to pad)
        assertEquals(emptyList<String>(), DesignRules.rotationViolations(act).filter { "Pad" in it })
        // a fourth ordinary level with a pad is not
        val four = act - 32 + (21 to pad)
        assertEquals(listOf("act 2: pads/switches (Pad) in 4 levels [17, 18, 20, 21] (max 3, plus the finale)"),
            DesignRules.rotationViolations(four).filter { "Pad" in it })
        // and the finale does not make a fourth ordinary level legal
        assertEquals(1, DesignRules.rotationViolations(four + (32 to pad)).count { "Pad" in it })
    }

    @Test
    fun theRealUplinkCountsAsALaserGate() {
        // 2-18: the beam over the stairs flashes where you land (PastX trigger, on 0.4 s, off 40 s)
        assertTrue(DesignRules.hasLaserGate(World2.levels[17]))
    }

    // ---------- say lint (H19) ----------

    private fun talker(intro: String, vararg says: Pair<String, String>, hint: T? = null) =
        Level(T(intro, intro), T(intro, intro), hint = hint,
            traps = says.mapIndexed { i, (en, de) -> trap(Trigger.PastX(5f + i), Action.Say(T(en, de))) }) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    @Test
    fun theSameLineInTwoLevelsOfAnActIsCaughtInBothLanguages() {
        val a = talker("one", "Mind your head." to "Kopf einziehen.")
        val b = talker("two", "Mind your head." to "Achtung, Kopf.")      // same English, other German
        val c = talker("three", "Not the same." to "Kopf einziehen.")     // same German, other English
        val v = DesignRules.sayViolations(mapOf(17 to a, 18 to b, 19 to c))
        assertEquals(2, v.size)
        assertTrue(v.joinToString("\n"), v.any { it.startsWith("act 2 EN: \"mind your head.\" in levels 17 (Say (round 1)), 18") })
        assertTrue(v.joinToString("\n"), v.any { it.startsWith("act 2 DE: \"kopf einziehen.\" in levels 17") && "19" in it })
        // case and spacing do not hide it, an intro or a hint counts as a line
        val d = talker("MIND  your head.", "x" to "y")
        assertEquals(1, DesignRules.sayViolations(mapOf(17 to a, 18 to d)).count { it.startsWith("act 2 EN") })
        assertEquals(1, DesignRules.sayViolations(mapOf(17 to talker("p", "q" to "r", hint = T("Hop.", "Spring.")), 18 to talker("s", hint = T("Hop.", "Spring.")))).count { it.startsWith("act 2 EN") })
    }

    @Test
    fun differentLinesOrDifferentActsOrOneLevelRepeatingItselfAreFine() {
        val a = talker("one", "Mind your head." to "Kopf einziehen.")
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to a, 18 to talker("two", "Mind your feet." to "Füße einziehen."))))
        // the same line in act 1 and act 2
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(14 to a, 17 to a)))
        // twice in one level
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to talker("one", "Again." to "Nochmal.", "Again." to "Nochmal."))))
    }

    @Test
    fun threeSharedWordsInAnActAreOnlyAWarning() {
        val a = talker("one", "Watch the loose red wire carefully." to "Pass auf das lose rote Kabel auf.")
        val b = talker("two", "Careful, the loose red wire bites." to "Vorsicht, das Kabel beißt.")
        // not word for word: no violation, but a warning that names the phrase
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to a, 18 to b)))
        val w = DesignRules.sayNgramWarnings(mapOf(17 to a, 18 to b))
        assertEquals(w.toString(), 1, w.size)
        assertTrue(w[0], w[0].startsWith("act 2 EN: levels [17, 18] share") && "loose red wire" in w[0])
        // the German lines share fewer than three words once the common ones are dropped
        assertTrue(w.none { " DE" in it })
    }

    @Test
    fun commonWordsTwoSharedWordsOtherActsAndOneLevelAreNoWarning() {
        // "the end of the day" is all common words and two real ones
        val a = talker("one", "At the end of the day, red wire." to "x")
        val b = talker("two", "In the end of the day, green cable." to "y")
        assertEquals(emptyList<String>(), DesignRules.sayNgramWarnings(mapOf(17 to a, 18 to b)))
        // three shared words in two acts
        val c = talker("three", "Watch the loose red wire carefully." to "z")
        val e = talker("four", "Careful, the loose red wire bites." to "w")
        assertEquals(emptyList<String>(), DesignRules.sayNgramWarnings(mapOf(14 to c, 17 to e)))
        // a level that repeats its own words
        assertEquals(emptyList<String>(), DesignRules.sayNgramWarnings(mapOf(17 to talker("o", "Loose red wire." to "a", "Loose red wire again." to "b"))))
    }

    // ---------- adjacent rooms (H20) ----------

    private fun sig(dominant: String?, shape: String, vararg families: String) =
        DesignRules.Signature(dominant, families.toList(), shape.split(' '))

    @Test
    fun twoNeighboursWithTheSameDominantFamilyAreCaughtHoweverTheyPlay() {
        // 2-22 and 2-23 as the review found them: the wall dominates both
        val l22 = sig("wall-move", "R^ L^", "wall-move", "wall-move", "drop")
        val l23 = sig("wall-move", "R^", "wall-move", "wall-move", "wall-move")
        val v = DesignRules.adjacentViolations(mapOf(22 to l22, 23 to l23))
        assertEquals(v.toString(), 1, v.size)
        assertTrue(v[0], v[0].startsWith("levels 22 and 23: both are dominated by wall-move"))
        // the same dominant family with a completely different way of playing is caught as well
        val other = sig("wall-move", "L W R W L", "wall-move")
        assertEquals(1, DesignRules.adjacentViolations(mapOf(17 to l22, 18 to other)).size)
    }

    @Test
    fun neighboursWithAnotherDominantFamilyAreFineWhateverElseTheyShare() {
        val wall = sig("wall-move", "R^ L^", "wall-move", "wall-move", "drop", "drop")
        // another dominant family, same moves, the other family even shows up in both rooms
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to wall, 18 to sig("drop", "R^ L^", "drop", "drop", "wall-move"))))
        // no lethal moment, no dominant family: nothing to compare
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to sig(null, "R", "door"), 18 to sig(null, "R", "door"))))
        // same dominant family, but a finale (it combines the act on purpose), or the next act
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(31 to wall, 32 to wall)))
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(16 to wall, 17 to wall)))
        // not neighbours
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to wall, 19 to wall)))
    }

    @Test
    fun thePercentageIsOnlyAReport() {
        val a = sig("drop", "R^ L^", "drop")
        val b = sig("floor-move", "R^ L^", "floor-move")
        // the moves are 100 % alike: reported, but no violation
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to a, 18 to b)))
        val r = DesignRules.adjacentReport(mapOf(17 to a, 18 to b))
        assertEquals(1, r.size)
        assertTrue(r[0], "100 % alike" in r[0] && r[0].startsWith("levels 17 and 18"))
        assertEquals(emptyList<String>(), DesignRules.adjacentReport(mapOf(16 to a, 17 to b)))
    }

    @Test
    fun theDominantFamilyIsTheOneWithMostLethalMomentsAndATieMakesAllTiedOnesDominant() {
        assertEquals(setOf("b"), DesignRules.dominantFamilies(listOf(listOf("a"), listOf("b"), listOf("b"))))
        // a tie no longer goes to the earliest: a cheap early moment cannot take the lead, both families dominate
        assertEquals(setOf("a", "b"), DesignRules.dominantFamilies(listOf(listOf("a"), listOf("b"))))
        assertEquals(setOf("b", "a"), DesignRules.dominantFamilies(listOf(listOf("b", "a"), listOf("a", "b"))))
        assertEquals(emptySet<String>(), DesignRules.dominantFamilies(emptyList()))
    }

    /** A room whose clean run meets two door changes (route, not lethal) and one lethal drop: a held-right run. */
    private val routeHeavy = Level(T("r", "r"), T("r", "r"),
        traps = listOf(
            trap(Trigger.PastX(6f), Action.DoorTo(28, 14)),
            trap(Trigger.PastX(8f), Action.DoorTo(29, 14)),
            trap(Trigger.PastX(10f), Action.Fall('a')),
        )) { border(); floor(); fill(14..15, 15..17, 'a'); put(2, 14, 'P'); put(29, 14, 'D') }

    @Test
    fun onlyLethalMomentsMakeAFamilyDominant() {
        val s = DesignRules.signature(routeHeavy, { right(4f) })
        assertEquals(s.toString(), setOf("drop"), s.dominant)
        // the first door move is undone by the second (the door ends where it would have anyway): decoration, not counted
        assertEquals(s.toString(), listOf("door", "drop"), s.families)
        assertEquals(setOf(routeHeavy.traps[0]), DesignRules.decorations(routeHeavy, 0, { right(4f) }))
    }

    @Test
    fun theSignatureOfARoomReadsItsTrapsAndItsMoves() {
        val s = DesignRules.signature(puzzle, solution)
        assertEquals(setOf("drop"), s.dominant)
        assertEquals(listOf("spikes", "drop", "drop", "drop"), s.families)
        // right, wait for the block, right, back left: no stretch under a quarter second counts
        assertTrue(s.shape.toString(), s.shape.first() == "R^" && s.shape.last().startsWith("L"))
        assertEquals(1f, DesignRules.similarity(listOf("a", "b"), listOf("a", "b")))
        assertEquals(0.5f, DesignRules.similarity(listOf("a", "b"), listOf("b")))
        assertEquals(0f, DesignRules.similarity(emptyList(), listOf("b")))
    }

    // ---------- what moves: floor, wall, ceiling ----------

    /** A room with a floor slab 'f', a wall 'w' and a ceiling slab 'c'. */
    private val movers = Level(T("m", "m"), T("m", "m")) {
        border(); floor()
        fill(10..13, 12..12, 'f')      // a platform: air above it
        fill(16..16, 8..11, 'w')       // a column
        fill(20..23, 1..1, 'c')        // hangs under the top border
        fill(24..25, 13..14, 't')      // a truck: a 2 x 2 block standing on the floor
        fill(26..27, 15..17, 'e')      // a piece of the ground itself
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    @Test
    fun aMoveIsToldApartByWhatItMoves() {
        fun fam(g: Char, dx: Float, dy: Float, split: Boolean = true) =
            DesignRules.familyOf(Action.Move(g, dx, dy, 2f), movers, split = split)
        assertEquals("floor-move", fam('f', 3f, 0f))
        assertEquals("floor-move", fam('f', 0f, -2f))
        assertEquals("wall-move", fam('w', 3f, 0f))
        assertEquals("wall-move", fam('w', 0f, -3f))
        assertEquals("ceiling-move", fam('c', 0f, 3f))
        assertEquals("ceiling-move", fam('c', 4f, 0f))
        // a block that stands on the floor is a wall, a piece of the ground is a floor
        assertEquals("wall-move", fam('t', 5f, 0f))
        assertEquals("drop", fam('e', 0f, 8f))
        assertEquals("floor-move", fam('e', 0f, -1f))
        // a floor that moves down is a drop, like a Fall
        assertEquals("drop", fam('f', 0f, 2f))
        assertEquals("drop", DesignRules.familyOf(Action.Fall('f'), movers, split = true))
        // without the split (H13) every Move is "move"
        assertEquals("move", fam('w', 3f, 0f, split = false))
        assertEquals("move", fam('f', 0f, 2f, split = false))
        // a stalker is a wall that walks with you: wall-move under the split, "move" for H13
        assertEquals("wall-move", DesignRules.familyOf(Action.Chase('w', 2f), movers, split = true))
        assertEquals("wall-move", DesignRules.familyOf(Action.Chase('f', 2f), movers, split = true))
        assertEquals("move", DesignRules.familyOf(Action.Chase('w', 2f), movers))
    }

    // ---------- moving walls per act (H21) ----------

    @Test
    fun aFourthMovingWallLevelInAnActIsCaught() {
        val wall = sig("wall-move", "R", "wall-move")
        val floor = sig("floor-move", "R", "floor-move")
        val four = (17..20).associateWith { wall }
        val v = DesignRules.wallMoveViolations(four)
        assertEquals(v.toString(), listOf("act 2: 4 levels dominated by a moving wall [17, 18, 19, 20] (max 3)"), v)
        // three are fine, and so are four when one of them is dominated by something else, or when they sit in two acts
        assertEquals(emptyList<String>(), DesignRules.wallMoveViolations(four - 20))
        assertEquals(emptyList<String>(), DesignRules.wallMoveViolations(four + (20 to floor)))
        assertEquals(emptyList<String>(), DesignRules.wallMoveViolations(mapOf(15 to wall, 16 to wall, 17 to wall, 18 to wall)))
    }

    // ---------- the pad does not excuse a death at the start (H17) ----------

    @Test
    fun aPadPressedRightAtTheStartIsNoTrapThatWentOff() {
        // the pad lies on the way, spikes behind it: holding right presses it (a lethal trap elsewhere fires), then dies
        val room = Level(T("p", "p"), T("p", "p"),
            start = listOf(Action.Pad('1', at = 3 to 14)),
            traps = listOf(trap(Trigger.Pressed('1'), Action.Fall('a')))) {
            border(); floor(); fill(14..15, 15..17, 'a'); put(5, 14, '^'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        val v = DesignRules.fillerDeathViolations(room, 0)
        assertEquals(v.toString(), 1, v.size)
        // a trap on the way that is no pad but drops a floor far away does not excuse the spikes either: without it the
        // run dies just the same (it used to excuse the death, the H17 loophole)
        val farAway = Level(T("p", "p"), T("p", "p"),
            traps = listOf(trap(Trigger.PastX(3f), Action.Fall('a')))) {
            border(); floor(); fill(14..15, 15..17, 'a'); put(5, 14, '^'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        val far = DesignRules.fillerDeathViolations(farAway, 0)
        assertEquals(far.toString(), 1, far.size)
        assertTrue(far.single(), "dies the same without the traps" in far.single())
        // the trap that kills is the gag: the floor right under the runner drops
        val underYou = Level(T("p", "p"), T("p", "p"),
            traps = listOf(trap(Trigger.PastX(3f), Action.Fall('a')))) {
            border(); floor(); fill(4..6, 15..17, 'a'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        assertEquals(DesignRules.Weight.LETHAL, Bot(underYou).right(2f).also { it.expect(WorldState.DEAD) }.moments.first().weight)
        assertEquals(emptyList<String>(), DesignRules.fillerDeathViolations(underYou, 0))
    }

    @Test
    fun aCardFitsTheTrapItSitsOn() {
        assertEquals(emptyList<String>(), DesignRules.cardLintViolations(DesignDemos.corridor(Card.SPIKE_SEED)))
        assertEquals(listOf("Demo: Corridor round 1: GHOST_BLOCK sits on Show"), DesignRules.cardLintViolations(DesignDemos.corridor(Card.GHOST_BLOCK)))
        assertEquals(listOf("Demo: Corridor round 1: SHY_DOOR sits on Show"), DesignRules.cardLintViolations(DesignDemos.corridor(Card.SHY_DOOR)))
        // a ghost block card on the bonk block it is about
        val ghost = Level(T("g", "g"), T("g", "g"), legend = mapOf('k' to Glyph(spike = false, hidden = true, bonk = true)),
            traps = listOf(trap(Trigger.Touch('k'), Action.Play(Card.GHOST_BLOCK)))) { border(); floor(); put(6, 11, 'k'); put(2, 14, 'P'); put(29, 14, 'D') }
        assertEquals(emptyList<String>(), DesignRules.cardLintViolations(ghost))
        // every card has a rule
        for (c in Card.entries) DesignRules.cardFits(c, trap(Trigger.PastX(1f), Action.Play(c)), DesignDemos.corridor())
    }

    @Test
    fun aRematchMustNotBeEasierOrFallToRoundOne() {
        val l = DesignDemos.lazyRematch
        val round1: Solution = { hopR(7.6f).right(4f) }
        val round2: Solution = { right(3f) }
        val v = DesignRules.rematchViolations(l, listOf(round1, round2))
        assertTrue(v.joinToString("\n"), v.any { "round 1's solution wins it" in it })
        assertTrue(v.joinToString("\n"), v.any { "shorter than round 1" in it })
    }

    // ---------- hardening after round 3: laser gates (H12) ----------

    @Test
    fun aGateIsALitWindowOfAtMostTwoSecondsFromAnyTrigger() {
        val flash = Action.Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f)
        // exactly 2 s lit is still a gate (the old filter wanted less than 2 s)
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 2f))))
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 2.5f))))
        // a laser of the start that is lit 1 s and dark 12 s is a gate too, whatever its off time
        val start = Action.Laser('H', 20 to 8, 20 to 10, on = 1f, off = 12f)
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 5f), start = listOf(start))))
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 5f), start = listOf(start.copy(on = 5f)))))
    }

    /** A room with a beam 'H' from the start that is always lit, switched by [traps]. */
    private fun powered(vararg traps: Trap, beam: Action.Laser = Action.Laser('H', 20 to 8, 20 to 10)) =
        Level(T("g", "g"), T("g", "g"), start = listOf(beam), traps = traps.toList()) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    @Test
    fun aBeamThatTrapsSwitchOnAndOffIsAGate() {
        val on = Action.Power('H', true)
        val off = Action.Power('H', false)
        // switched on and off again within 2 s on the same trigger: a gate
        assertTrue(DesignRules.hasLaserGate(powered(trap(Trigger.PastX(5f), on), trap(Trigger.PastX(5f), off, delay = 1.5f))))
        // on the same trigger, but lit for 3 s: a wall of light, no gate
        assertFalse(DesignRules.hasLaserGate(powered(trap(Trigger.PastX(5f), on), trap(Trigger.PastX(5f), off, delay = 3f))))
        // on and off on different triggers: the player's timing decides how long it is lit, a gate
        assertTrue(DesignRules.hasLaserGate(powered(trap(Trigger.PastX(5f), on), trap(Trigger.PastX(9f), off, delay = 3f))))
        assertTrue(DesignRules.hasLaserGate(powered(trap(Trigger.Pressed('1'), on), trap(Trigger.After(4f), off))))
        // a long beam fired by a trap and cut by a trap a second later is a flash
        val long = Action.Laser('G', 15 to 8, 15 to 10, on = 5f, off = 40f)
        assertTrue(DesignRules.hasLaserGate(powered(trap(Trigger.Landed(10f, 14f), long), trap(Trigger.Landed(10f, 14f), Action.Power('G', false), delay = 1f))))
        // switching on a beam that cycles short windows is a gate
        assertTrue(DesignRules.hasLaserGate(powered(trap(Trigger.PastX(5f), on), beam = Action.Laser('H', 20 to 8, 20 to 10, on = 1.5f, off = 20f))))
        // only switched off, or only switched on (and always lit then): no gate
        assertFalse(DesignRules.hasLaserGate(powered(trap(Trigger.PastX(5f), off))))
        assertFalse(DesignRules.hasLaserGate(powered()))
    }

    @Test
    fun aClockedLiveTraceIsAGateAClockedRailIsNot() {
        fun clocked(c: Action.Clock) = Level(T("g", "g"), T("g", "g"), start = listOf(c)) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }
        assertTrue(DesignRules.hasLaserGate(clocked(Action.Clock('Z', on = 1.2f, off = 1.8f))))
        assertTrue(DesignRules.hasLaserGate(clocked(Action.Clock('Z', on = 1.5f, off = 12f))))
        assertFalse(DesignRules.hasLaserGate(clocked(Action.Clock('Z', on = 4f, off = 12f))))
        assertFalse(DesignRules.hasLaserGate(clocked(Action.Clock('Z', on = 1f, off = 0f))))
        // a copper rail on a clock is a blinking floor, not a gate
        assertFalse(DesignRules.hasLaserGate(clocked(Action.Clock('a', on = 1.2f, off = 1.8f))))
    }

    // ---------- say lint: punctuation (H19) ----------

    @Test
    fun punctuationDoesNotHideARepeatedLine() {
        val a = talker("one", "Nope." to "Nö.")
        val b = talker("two", "nope!" to "Nö!!")
        val v = DesignRules.sayViolations(mapOf(17 to a, 18 to b))
        assertEquals(v.joinToString("\n"), 2, v.size)
        assertTrue(v.joinToString("\n"), v.any { it.startsWith("act 2 EN: \"nope.\" in levels 17") })
        assertEquals(DesignRules.sayKey("Wait... what?!"), DesignRules.sayKey("wait what"))
        // other words are other lines, and a line of nothing but punctuation still counts as itself
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to a, 18 to talker("two", "Nope, nope." to "Nö, nö."))))
        assertEquals(1, DesignRules.sayViolations(mapOf(17 to talker("one", "..." to "x"), 18 to talker("two", "..." to "y"))).size)
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to talker("one", "..." to "x"), 18 to talker("two", "?!" to "y"))))
    }

    // ---------- what moves, by motion (H20/H21) ----------

    /** A flat wide wall 'x' standing on the floor, a floor block 'i' at the left wall, a rack 'r' and a column 'q' under the ceiling, a slab 's'. */
    private val movers2 = Level(T("m", "m"), T("m", "m")) {
        border(); floor()
        fill(10..15, 12..14, 'x')      // 6 wide, 3 high, on the floor
        fill(1..6, 15..17, 'i')        // a piece of the ground that touches the left wall
        fill(17..18, 1..3, 'r')        // 2 wide, 3 high, under the ceiling
        fill(21..21, 1..4, 'q')        // a column under the ceiling
        fill(24..27, 1..1, 's')        // a slab under the ceiling
        put(8, 14, 'P'); put(29, 14, 'D')
    }

    @Test
    fun aMoveIsToldApartByItsMotion() {
        fun fam(g: Char, dx: Float, dy: Float) = DesignRules.familyOf(Action.Move(g, dx, dy, 2f), movers2, split = true)
        // a flat wide wall that slides at you is a wall (its faces said floor: as many tops as sides)
        assertEquals("wall-move", fam('x', -4f, 0f))
        // the same block lifted straight up is still read by its faces
        assertEquals("floor-move", fam('x', 0f, -2f))
        // a floor block touching the side wall does not hang from the ceiling: it drops
        assertFalse(DesignRules.hangs(movers2, 'i'))
        assertEquals("drop", fam('i', 0f, 8f))
        // a rack under the ceiling that comes down is the ceiling coming down, a column that slides is a wall
        assertTrue(DesignRules.hangs(movers2, 'r'))
        assertEquals("ceiling-move", fam('r', 0f, 6f))
        assertEquals("wall-move", fam('q', 3f, 0f))
        assertEquals("ceiling-move", fam('q', 0f, 5f))
        // a slab under the ceiling stays ceiling however it moves
        assertEquals("ceiling-move", fam('s', 3f, 0f))
        // a ceiling that falls is a ceiling under the split; for H13 a Fall stays "drop"
        assertEquals("ceiling-move", DesignRules.familyOf(Action.Fall('s'), movers2, split = true))
        assertEquals("drop", DesignRules.familyOf(Action.Fall('s'), movers2))
        assertEquals("drop", DesignRules.familyOf(Action.Fall('i'), movers2, split = true))
    }

    // ---------- neighbours over every round, ties (H20, H21) ----------

    @Test
    fun aRematchRoundMeetsTheNeighbourToo() {
        val drop = sig("drop", "R", "drop")
        val wall = sig("wall-move", "R", "wall-move")
        // round 1 of 17 differs from 18, its rematch does not
        val v = DesignRules.adjacentViolations(mapOf(17 to listOf(drop, wall), 18 to listOf(wall)))
        assertEquals(v.toString(), 1, v.size)
        assertTrue(v[0], v[0].startsWith("levels 17 and 18 (round 2 vs round 1): both are dominated by wall-move"))
        // and the other way round: the neighbour's rematch
        assertEquals(1, DesignRules.adjacentViolations(mapOf(17 to listOf(wall), 18 to listOf(drop, wall))).size)
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to listOf(drop, drop), 18 to listOf(wall, wall))))
    }

    @Test
    fun aTieCountsWithAllItsFamilies() {
        val tie = DesignRules.Signature(setOf("drop", "saw"), listOf("saw", "drop"), listOf("R"))
        // the cheap early saw does not hide the drop
        assertEquals(1, DesignRules.adjacentViolations(mapOf(17 to tie, 18 to sig("drop", "R", "drop"))).size)
        assertEquals(1, DesignRules.adjacentViolations(mapOf(17 to tie, 18 to sig("saw", "R", "saw"))).size)
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to tie, 18 to sig("belt", "R", "belt"))))
        // a tie with a moving wall counts for H21
        val wallTie = DesignRules.Signature(setOf("drop", "wall-move"), listOf("drop", "wall-move"), listOf("R"))
        val ties: Map<Int, DesignRules.Signature> = (17..20).associateWith { wallTie }
        assertEquals(1, DesignRules.wallMoveViolations(ties).size)
    }

    @Test
    fun aLevelWithAWallInAnyRoundCountsForTheWallCap() {
        val wall = sig("wall-move", "R", "wall-move")
        val drop = sig("drop", "R", "drop")
        val act = (17..19).associateWith { listOf(wall) } + (20 to listOf(drop, wall))
        assertEquals(listOf("act 2: 4 levels dominated by a moving wall [17, 18, 19, 20] (max 3)"), DesignRules.wallMoveViolations(act))
        assertEquals(emptyList<String>(), DesignRules.wallMoveViolations(act + (20 to listOf(drop, drop))))
    }

    @Test
    fun aStalkerIsAMovingWallForTheNeighbours() {
        val stalker = Level(T("s", "s"), T("s", "s"), traps = listOf(trap(Trigger.PastX(4f), Action.Chase('w', 3f)))) {
            border(); floor(); fill(12..12, 13..14, 'w'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        assertEquals("wall-move", DesignRules.familyOf(Action.Chase('w', 3f), stalker, split = true))
        // a lethal moment counts for the families of its lethal actions only: the bridge riding along is no family of it
        val bridge = Level(T("b", "b"), T("b", "b"), legend = mapOf('b' to Glyph(spike = false, hidden = true)),
            traps = listOf(trap(Trigger.PastX(4f), Action.Show('b'), Action.Fall('a')))) {
            border(); floor(); fill(10..11, 15..17, 'b'); fill(5..6, 15..17, 'a'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        val s = DesignRules.signature(bridge, { right(1f) })
        assertEquals(s.toString(), setOf("drop"), s.dominant)
    }

    // ---------- trap ablation: decoration (H3, H15, H17) ----------

    /** A plain run to the door, with [traps] and a block 'z' up in the far corner behind the spawn. */
    private fun corner(vararg traps: Trap, more: MapBuilder.() -> Unit = {}) =
        Level(T("c", "c"), T("c", "c"), legend = mapOf('S' to Glyph(spike = true, hidden = true), 'b' to Glyph(spike = false, hidden = true)), traps = traps.toList()) {
            border(); floor(); fill(1..2, 1..1, 'z'); put(4, 14, 'P'); put(29, 14, 'D'); more()
        }

    @Test
    fun aTrapThatChangesNothingIsDecoration() {
        val far = trap(Trigger.After(1.5f), Action.Fall('z'))
        val room = corner(far)
        // touch the back wall first, then run for the door: a bit more than 3 s
        val run: Solution = { leftTo(1.6f).rightTo(28f).right(1f) }
        assertTrue(DesignRules.cleanRunTime(room, 0, run) > 3.2f)
        assertEquals(setOf(far), DesignRules.decorations(room, 0, run))
        // the far Fall used to split the run into two short gaps; as decoration it does not count: start to door, no trap
        val v = DesignRules.densityViolations(room, 0, run, 0f)
        assertTrue(v.joinToString("\n"), v.any { "no real trap for" in it && "start to door" in it })
        assertTrue(DesignRules.timeline(room, 0, run), "decoration (changes nothing)" in DesignRules.timeline(room, 0, run))
    }

    @Test
    fun aTrapThatChangesARunIsNoDecorationAndTrapsOnOneTriggerGoTogether() {
        // the floor drops where you run: the clean run hops it, the naive run falls in
        val pitfall = trap(Trigger.PastX(10f), Action.Fall('a'))
        val room = corner(pitfall) { fill(13..15, 15..17, 'a') }
        val run: Solution = { hopR(12f).rightTo(28f).right(1f) }
        DesignRules.cleanRun(room, 0, run).expect(WorldState.WON)
        assertEquals(emptySet<Trap>(), DesignRules.decorations(room, 0, run))
        // the same Fall twice on one trigger: each alone changes nothing, together they are the trap
        val twice = listOf(trap(Trigger.PastX(10f), Action.Fall('a')), trap(Trigger.PastX(10f), Action.Fall('a')))
        assertEquals(emptySet<Trap>(), DesignRules.decorations(corner(*twice.toTypedArray()) { fill(13..15, 15..17, 'a') }, 0, run))
    }

    @Test
    fun teethMustBeTheTrapsOwn() {
        // the bridge needs the trap, its lethal part (a block far away) bites nobody: the patient player dies to spikes that
        // grow later (After 9 s), the reckless one runs through. The old rule saw the patient die and passed the trap
        val room = corner(
            trap(Trigger.PastX(6f), Action.Show('b'), Action.Fall('z')),
            trap(Trigger.After(9f), Action.Show('S')),
        ) { pit(10..12); fill(10..12, 15..15, 'b'); put(20, 14, 'S') }
        val run: Solution = { rightTo(28f).right(1f) }
        DesignRules.cleanRun(room, 0, run).expect(WorldState.WON)
        val v = DesignRules.teethViolations(room, 0, run)
        assertEquals(v.joinToString("\n"), 1, v.size)
        assertTrue(v.single(), "what beats the probes is not this trap" in v.single())
        // the same moment, but what falls is the floor behind the bridge: the reckless runner falls in, that is its bite
        val biting = corner(
            trap(Trigger.PastX(6f), Action.Show('b'), Action.Fall('y')),
            trap(Trigger.After(9f), Action.Show('S')),
        ) { pit(10..12); fill(10..12, 15..15, 'b'); fill(13..14, 15..17, 'y'); put(20, 14, 'S') }
        val hop: Solution = { hopR(11.8f).rightTo(28f).right(1f) }
        DesignRules.cleanRun(biting, 0, hop).expect(WorldState.WON)
        assertEquals(emptyList<String>(), DesignRules.teethViolations(biting, 0, hop))
    }

    @Test
    fun aFarTrapDoesNotExcuseSpikesAtTheStartASwapIntoThemDoes() {
        // a switch far away (a real trap: it changes the route) flips as you start, the spikes ahead kill you anyway
        val toggle = Level(T("t", "t"), T("t", "t"), start = listOf(Action.Circuit('q')),
            traps = listOf(trap(Trigger.After(0.1f), Action.Toggle("q")))) {
            border(); floor(); fill(28..29, 3..3, 'q'); put(6, 14, '^'); put(2, 14, 'P'); put(29, 14, 'D')
        }
        val v = DesignRules.fillerDeathViolations(toggle, 0)
        assertEquals(v.toString(), 1, v.size)
        // swapped controls walk the runner into the spikes behind him: that death is the gag
        val swap = Level(T("s", "s"), T("s", "s"), traps = listOf(trap(Trigger.After(0.1f), Action.Swap(true)))) {
            border(); floor(); put(1, 14, '^'); put(12, 14, '^'); put(3, 14, 'P'); put(29, 14, 'D')
        }
        assertEquals(WorldState.DEAD, Bot(swap).right(2f).world.state)
        assertEquals(emptyList<String>(), DesignRules.fillerDeathViolations(swap, 0))
    }

    // ---------- ANNEX ----------

    @Test
    fun annexIsPendingOnlyWhileNoLevelPlaysIt() {
        assertEquals(setOf(Card.ANNEX), DesignRules.pendingCards(setOf(Card.COLLAPSE)))
        assertEquals(emptySet<Card>(), DesignRules.pendingCards(setOf(Card.COLLAPSE, Card.ANNEX)))
    }

    // ---------- rollout budgets (§11) ----------

    @Test
    fun aBlockKeepsItsBudgetItsCardsAndItsEdges() {
        val block = DesignRules.Block(2, "X", setOf(17, 18), mapOf("pad" to 1, "rematch" to 0, "spikes" to 2), card = 1,
            cards = mapOf(Card.CRUMBLE to 2), forbidden = mapOf(17 to setOf("drop")))
        val pad = DesignDemos.puzzle            // a pad level that plays COLLAPSE, dominated by drop
        val plain = DesignDemos.corridor(Card.CRUMBLE)
        val sigs = mapOf(17 to listOf(sig("saw", "R", "saw")), 18 to listOf(sig("drop", "R", "drop")))
        assertEquals(emptyList<String>(), DesignRules.budgetViolations(block, mapOf(17 to pad, 18 to plain), sigs))
        // a second pad level, a card over its budget, a rematch the block does not keep, the neighbour's family at the edge
        val v = DesignRules.budgetViolations(block, mapOf(17 to pad, 18 to pad), mapOf(17 to listOf(sig("drop", "R", "drop"))))
        assertTrue(v.joinToString("\n"), v.any { "pad in 2 levels (budget 1)" in it })
        assertTrue(v.joinToString("\n"), v.any { "card COLLAPSE played 2 times (budget 1)" in it })
        assertTrue(v.joinToString("\n"), v.any { "level 17 is dominated by [drop]" in it })
        val r = DesignRules.budgetViolations(block, mapOf(17 to DesignDemos.lazyRematch), emptyMap())
        assertTrue(r.joinToString("\n"), r.any { "1 rematch levels, the block keeps exactly 0" in it })
        // the exception lets CRUMBLE be played twice
        assertEquals(emptyList<String>(), DesignRules.budgetViolations(block, mapOf(17 to plain, 18 to plain), emptyMap()).filter { "CRUMBLE" in it })
    }

    @Test
    fun aBlockGoesIntoRebuiltWhole() {
        assertEquals(emptyList<String>(), DesignRules.partialBlocks(1, emptySet()))
        assertEquals(emptyList<String>(), DesignRules.partialBlocks(1, (7..16).toSet()))
        assertEquals(1, DesignRules.partialBlocks(1, (7..15).toSet()).size)
        // World 2: the pilot alone is fine, the pilot and one more level of block B is not
        assertEquals(emptyList<String>(), DesignRules.partialBlocks(2, DesignRules.PILOT))
        assertEquals(1, DesignRules.partialBlocks(2, DesignRules.PILOT + 25).size)
        assertEquals(emptyList<String>(), DesignRules.partialBlocks(2, DesignRules.PILOT + (25..32)))
    }
}
