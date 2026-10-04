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
        assertEquals(emptyList<String>(), DesignRules.holdRightWithHopsViolations(puzzle, 0))
        assertEquals(emptyList<String>(), DesignRules.slopViolations(puzzle, 0, solution))
        assertEquals(0, DesignRules.spikePopupCount(puzzle))
        assertEquals(0, DesignRules.heatSpikeFinaleCount(puzzle))
        assertEquals(emptyList<String>(), DesignRules.spikeQuotaViolations(mapOf(20 to puzzle)))
        assertEquals(emptyList<String>(), DesignRules.cardSpreadViolations(mapOf(20 to puzzle)))
    }

    @Test
    fun thePuzzleRoomHasItsSurprisesWhereThePlanSaysSo() {
        // the switch is the only way: without it the copper wall stays up, whatever you do
        Bot(puzzle).right(3f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(3f).expect(WorldState.PLAYING)
        // and it drops the upper floor behind you (U1)
        val pressed = Bot(puzzle).right(3f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().leftJump(0.35f).landLeft().leftTo(2.6f)
        assertFalse(pressed.world.circuits.getValue('w').powered)
        assertEquals(Card.COLLAPSE, pressed.world.lastCard)
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
    fun aPixelPerfectSolutionFailsTheSlopTest() {
        val needle = DesignDemos.needle
        val exact: Solution = { rightTo(10.1f).waitUntil(3.2f).right(4f) }
        assertTrue(DesignRules.cleanRunTime(needle, 0, exact) > 3f)
        assertFalse(DesignRules.solutionToleratesSlop(needle, 0, exact))
    }

    @Test
    fun slopMovesTargetsAndHoldsButNotConditions() {
        val corridor = DesignDemos.corridor()
        val exact = Bot(corridor).rightTo(10f).world.player.box.cx
        val late = Bot(corridor, slop = Slop(0f, 1f)).rightTo(10f).world.player.box.cx
        assertEquals(exact + 1f, late, 0.1f)
        assertEquals(1.15f, Bot(corridor, slop = Slop(0.15f)).wait(1f).world.time, 0.01f)
        assertEquals(0.85f, Bot(corridor, slop = Slop(-0.15f)).wait(1f).world.time, 0.01f)
        assertEquals(1f, Bot(corridor, slop = Slop(0.15f)).waitFor { it.time >= 1f }.world.time, 0.01f)
        // a sloppy jump is still a jump
        assertTrue(Bot(corridor, slop = Slop(-0.15f)).jump(0.05f).waitFor { it.time >= 0.1f }.world.player.box.b < 15f)
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
    fun theTableCheckWantsAPuzzleOrAStar() {
        val noBlock = World2DesignTest.DESIGN + (1 to d("–", "U1"))
        assertEquals(
            listOf("level 1: no puzzle block and no ★ (H2)"),
            DesignRules.tableViolations(2, noBlock, (1..48).toSet() - World2DesignTest.PILOT),
        )
        // W1's tutorial may do without
        assertEquals(emptyList<String>(), DesignRules.tableViolations(1, World1DesignTest.DESIGN, (1..48).toSet()))
    }

    @Test
    fun minimumDurationsFollowTheCurve() {
        val plain = d("R1", "U1")
        val star = d("–", "U1", breather = true)
        assertEquals(0f, DesignRules.minDuration(1, 6, d("–", "U7")))
        assertEquals(6f, DesignRules.minDuration(1, 7, plain))
        assertEquals(8f, DesignRules.minDuration(1, 17, plain))
        assertEquals(8f, DesignRules.minDuration(2, 10, plain))
        assertEquals(10f, DesignRules.minDuration(2, 25, plain))
        assertEquals(10f, DesignRules.minDuration(3, 1, plain))
        for (w in 1..3) for (n in DesignRules.FINALES) assertEquals(15f, DesignRules.minDuration(w, n, d("R1+R5", "U1+U4")))
        assertEquals(0f, DesignRules.minDuration(2, 40, star))
    }
}
