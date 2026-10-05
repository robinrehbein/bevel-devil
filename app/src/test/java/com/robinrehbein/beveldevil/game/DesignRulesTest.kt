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
        val clearRoad = World1.levels.single { it.name.en == "Clear Road" }
        assertEquals(1, DesignRules.spikePopupCount(clearRoad))
        for (name in listOf("Burn-in Test", "Boot Order")) {
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
        assertEquals(emptyList<String>(), DesignRules.tableViolations(2, World2DesignTest.DESIGN, (1..48).toSet() - World2DesignTest.PILOT))
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
        // the door flees in 2-3 and 2-4: twice in act 1
        val levels = mapOf(3 to World2.levels[2], 4 to World2.levels[3])
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
        // the cycling gate is still one
        assertTrue(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash.copy(on = 1f, off = 2f))))
    }

    @Test
    fun aLongBeamOrAnAlwaysLitLaserIsNoGate() {
        val flash = Action.Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f)
        // lit for 5 s: a wall of light, not a gate; a laser from the start that is never dark is a fixture
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.PastX(10f), flash.copy(on = 5f))))
        assertFalse(DesignRules.hasLaserGate(gateRoom(Trigger.After(1f), flash, start = listOf(flash.copy(id = 'H', off = 0f)))))
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
        // 2-24: "a gate flashes where you land" (Zone trigger, on 0.7 s, off 40 s)
        assertTrue(DesignRules.hasLaserGate(World2.levels[23]))
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
        // the allowlist silences a known line
        assertEquals(emptyList<String>(), DesignRules.sayViolations(mapOf(17 to a, 18 to a), allow = setOf("mind your head.", "kopf einziehen.", "one")))
    }

    // ---------- adjacent rooms (H20) ----------

    private fun sig(dominant: String, major: Set<String>, shape: String, vararg families: String) =
        DesignRules.Signature(dominant, major, families.toList(), shape.split(' '))

    @Test
    fun theWallRollsInHopTwiceInARowIsCaught() {
        // 2-22 and 2-23 as the review found them: one wall of four traps in one room, all of them in the next, run and hop
        val l22 = sig("drop", setOf("drop", "move"), "R^ L^", "drop", "drop", "move", "drop")
        val l23 = sig("move", setOf("move"), "R^", "move", "move", "move")
        val v = DesignRules.adjacentViolations(mapOf(22 to l22, 23 to l23))
        assertEquals(1, v.size)
        assertTrue(v[0], v[0].startsWith("levels 22 and 23: both lean on [move] and play alike"))
        // H6 alone (first U-code) did not see it
        assertEquals(emptyList<String>(), DesignRules.h6Violations(mapOf(22 to d("–", "U2+U3"), 23 to d("–", "U3"))).filter { "main twist" in it })
    }

    @Test
    fun neighboursThatDifferInEffectOrInPlayAreFine() {
        val wall = sig("move", setOf("move"), "R^ L^", "move", "move")
        // another effect, same moves
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to wall, 18 to sig("drop", setOf("drop"), "R^ L^", "drop"))))
        // same effect, a different way of playing it (standing, going back, two stretches vs five)
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to wall, 18 to sig("move", setOf("move"), "L W R W L", "move"))))
        // same everything, but a finale (it combines the act on purpose), or the next act
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(31 to wall, 32 to wall)))
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(16 to wall, 17 to wall)))
        // not neighbours
        assertEquals(emptyList<String>(), DesignRules.adjacentViolations(mapOf(17 to wall, 19 to wall)))
    }

    @Test
    fun theSignatureOfARoomReadsItsTrapsAndItsMoves() {
        val s = DesignRules.signature(puzzle, solution)
        assertEquals("drop", s.dominant)
        assertEquals(setOf("drop", "spikes"), s.major)
        assertEquals(listOf("spikes", "drop", "drop", "drop"), s.families)
        // right, wait for the block, right, back left: no stretch under a quarter second counts
        assertTrue(s.shape.toString(), s.shape.first() == "R^" && s.shape.last().startsWith("L"))
        assertEquals(1f, DesignRules.similarity(listOf("a", "b"), listOf("a", "b")))
        assertEquals(0.5f, DesignRules.similarity(listOf("a", "b"), listOf("b")))
        assertEquals(0f, DesignRules.similarity(emptyList(), listOf("b")))
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
}
