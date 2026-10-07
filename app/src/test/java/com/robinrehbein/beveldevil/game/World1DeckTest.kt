package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * World 1, V2 "Revanche": after the door Mephi deals a new hand in the same room ([Round]). Every round has a scripted
 * solution played with the real physics, and the habit the round before taught is punished.
 */
class World1DeckTest {
    /** Level [n], round [round] (0 = the level itself). */
    private fun b(n: Int, round: Int = 0) = Bot(World1.levels[n - 1], round)

    /** Mephi bluffs ([Action.Bluff]). A card played without its trap must be a declared bluff. */
    private fun bluff(t: Trap) = t.actions.any { it is Action.Bluff }

    // ---------- the solutions of the round before ----------

    /** Round 1 of every level with a rematch, as scripted in [World1Test]. */
    private val round1: Map<Int, (Bot) -> Bot> = mapOf(
        // the tutorial: round 1 is the clean run in World1DesignTest.TUTORIAL_SOLUTIONS
        4 to { b -> b.also(World1DesignTest.TUTORIAL_SOLUTIONS.getValue(4)[0]) },
        6 to { b -> b.also(World1DesignTest.TUTORIAL_SOLUTIONS.getValue(6)[0]) },
        // levels 7-16 are rebuilt: their round 1 is the registered solution (World1RoomsA)
        8 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(8)[0]) },
        12 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(12)[0]) },
        15 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(15)[0]) },
        17 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(17)[0]) },
        18 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(18)[0]) },
        21 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(21)[0]) },
        24 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(24)[0]) },
        28 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(28)[0]) },
        // levels 33-40 are rebuilt: their round 1 is the registered solution (World1RoomsD)
        33 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(33)[0]) },
        37 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(37)[0]) },
        // levels 41-48 are rebuilt: their round 1 is the registered solution (World1RoomsE)
        42 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(42)[0]) },
        46 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(46)[0]) },
        47 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(47)[0]) },
        48 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(48)[0]) },
    )

    /** Round 2 of the levels with a third round, as in the round tests below (none since the rebuild of 48). */
    private val round2: Map<Int, (Bot) -> Bot> = emptyMap()

    @Test
    fun theScriptsCoverEveryRound() {
        val withRounds = World1.levels.withIndex().filter { it.value.rounds.size > 1 }.map { it.index + 1 }.toSet()
        assertEquals(setOf(4, 6, 8, 12, 15, 17, 18, 21, 24, 28, 33, 37, 42, 46, 47, 48), withRounds)
        assertEquals(withRounds, round1.keys)
        assertEquals(World1.levels.withIndex().filter { it.value.rounds.size > 2 }.map { it.index + 1 }.toSet(), round2.keys)
        for ((n, solve) in round1) solve(b(n)).expect(WorldState.WON)
        for ((n, solve) in round2) solve(b(n, 1)).expect(WorldState.WON)
    }

    /** A rematch is felt: the exact solution of the round before never wins the next round. */
    @Test
    fun theSolutionOfTheRoundBeforeNeverWins() {
        val winners = round1.filter { (n, solve) -> solve(b(n, 1)).world.state == WorldState.WON }.keys.map { "$it/2" } +
            round2.filter { (n, solve) -> solve(b(n, 2)).world.state == WorldState.WON }.keys.map { "$it/3" }
        assertEquals(emptyList<String>(), winners)
    }

    /** The card a round shows: its honest card, or the card it bluffs with. */
    private fun shown(r: Level) = r.traps.flatMap { it.actions }.firstNotNullOfOrNull { (it as? Action.Play)?.card ?: (it as? Action.Bluff)?.card }

    /** The card must not give the trap away: at least half the rematch rounds bluff or show a card the round before did not. */
    @Test
    fun atLeastHalfTheRematchRoundsChangeTheCard() {
        val rounds = World1.levels.flatMap { l -> l.rounds.zipWithNext() }
        val fresh = rounds.count { (a, b) -> b.traps.any(::bluff) || shown(b) != shown(a) }
        assertTrue("only $fresh of ${rounds.size} rematch rounds change the card", fresh * 2 >= rounds.size)
    }

    // ---------- structure ----------

    @Test
    fun rematchesAreSpreadOverAllThreeActsButSpareTheOnboarding() {
        val withRounds = World1.levels.withIndex().filter { it.value.rounds.size > 1 }.map { it.index + 1 }
        assertTrue("rematch levels $withRounds", withRounds.size in 12..18)
        assertTrue(withRounds.none { it <= 2 })
        for (act in listOf(1..16, 17..32, 33..48)) assertTrue("act $act: $withRounds", withRounds.count { it in act } in 4..7)
        World1.levels.forEach { l -> assertTrue("${l.name.en} has ${l.rounds.size} rounds", l.rounds.size <= 3) }
    }

    /**
     * Bluffs are rare: at most one per act, none in act 1, only in a rematch, and only with a card that was honest in
     * round 1 of the same level.
     */
    @Test
    fun mephiBluffsRarelyAndOnlyWithACardThatBitBefore() {
        val bluffs = World1.levels.withIndex().flatMap { (i, l) ->
            // round 1 cards always come with their trick (a ghost block's card announces the block it reveals)
            l.rounds.withIndex().drop(1).filter { (_, r) -> r.traps.any(::bluff) }.map { (k, r) -> Triple(i + 1, k, r) }
        }
        val where = bluffs.map { "${it.first}/${it.second + 1}" }
        assertTrue("bluffs $where", bluffs.size in 1..3)
        assertTrue("act 1 bluffs: $where", bluffs.none { it.first <= 16 })
        for (act in listOf(17..32, 33..48)) assertTrue("act $act: $where", bluffs.count { it.first in act } <= 1)
        for ((n, k, r) in bluffs) {
            assertTrue("level $n bluffs in round 1", k > 0)
            val card = r.traps.first(::bluff).actions.filterIsInstance<Action.Bluff>().single().card
            val honest = World1.levels[n - 1].traps.any { t -> !bluff(t) && t.actions.any { it is Action.Play && it.card == card } }
            assertTrue("level $n bluffs with $card, which was not honest in round 1", honest)
        }
    }

    @Test
    fun rematchIntrosAreFreshAndBilingual() {
        val intros = World1.levels.flatMap { it.rounds.drop(1) }.map { it.intro }
        intros.forEach { assertTrue(it.en.isNotBlank() && it.de.isNotBlank() && it.en != it.de) }
        assertEquals(intros.size, intros.map { it.en }.toSet().size)
        assertEquals(intros.size, intros.map { it.de }.toSet().size)
        val levelIntros = World1.levels.map { it.intro.en }.toSet()
        assertTrue(intros.none { it.en in levelIntros })
    }

    @Test
    fun everyRoundParses() {
        World1.levels.forEach { l -> l.rounds.forEach { World(it) } }
    }

    // ---------- invariants, for every round ----------

    @Test
    fun standingStillIsSafeInEveryRound() {
        World1.levels.forEachIndexed { i, l ->
            l.rounds.forEachIndexed { k, r ->
                if (r.traps.none { it.trigger is Trigger.Idle }) {
                    assertEquals("level ${i + 1} round ${k + 1}", WorldState.PLAYING, Bot(l, k).wait(2.2f).world.state)
                }
            }
        }
    }

    @Test
    fun holdingRightAloneWinsNoRound() {
        val winners = World1.levels.withIndex().flatMap { (i, l) ->
            l.rounds.indices.filter { k -> Bot(l, k).right(14f).world.state == WorldState.WON }.map { "${i + 1}/${it + 1}" }
        }
        assertTrue("holding right wins $winners", winners.isEmpty())
    }

    // ---------- Act 1 ----------

    @Test fun level04Rematch() = b(4, 1).also(World1DesignTest.TUTORIAL_SOLUTIONS.getValue(4)[1]).expect(WorldState.WON)
    /** Round 1 taught waiting at the first slab: now it only rattles, and the piece above the waiting spot comes down. */
    @Test fun level04RematchPunishesWaitingForTheOldSlab() = b(4, 1).rightTo(13.2f).wait(1f).expect(WorldState.DEAD)
    /** The second slab is real, and the one above the spot where you stop to look at it follows. */
    @Test fun level04RematchAdmiringTheSlabIsFatal() = b(4, 1).rightTo(18.3f).wait(2f).expect(WorldState.DEAD)

    @Test fun level06Rematch() = b(6, 1).also(World1DesignTest.TUTORIAL_SOLUTIONS.getValue(6)[1]).expect(WorldState.WON)
    /** The brother saw comes from the front at once: walking on meets it. */
    @Test fun level06RematchWalkingOnMeetsTheFrontSaw() = b(6, 1).rightTo(12.8f).rightJump(0.3f).landRight().right(3f).expect(WorldState.DEAD)
    @Test fun level06RematchTheOldJumpLandsOnSpikes() =
        b(6, 1).rightTo(12.8f).rightJump(0.3f).landRight().rightTo(15.2f).rightJump(0.35f).landRight().right(1f).expect(WorldState.DEAD)

    // levels 8, 12 and 15: the rematch plays against the habit round 1 taught (docs/LEVEL_DESIGN_V2.md H9)
    @Test fun level08Rematch() = World1DesignTest.play(8, 2)
    /** The room is upside down from the first steps: whoever keeps running right flies into the spikes of the ceiling. */
    @Test fun level08RematchRunningOnFliesIntoTheCeilingSpikes() = b(8, 1).right(2f).expect(WorldState.DEAD)
    /** The round 1 habit (walk to the spike and hop it) never gets there: the room turns over at the start. */
    @Test fun level08RematchTheRoomTurnsOverAtOnce() {
        val turned = b(8, 1).rightTo(5.0f).leftUntil { it.player.grounded }
        turned.expect(WorldState.PLAYING)
        assertTrue("upside down", turned.world.gravity < 0f)
        assertTrue("the door hangs at the far end", turned.world.door.box.x > 28f)
    }

    @Test fun level12Rematch() = World1DesignTest.play(12, 2)
    /** Return Trip, round 2: the keys swap in the first steps, so the round 1 start (press left) turns around. */
    @Test fun level12RematchTheOldFirstStepsTurnAround() {
        val turned = b(12, 1).left(0.6f)
        assertTrue("swapped", turned.world.swapped)
        assertTrue("back at the start, x=${turned.world.player.box.cx}", turned.world.player.box.cx > 28f)
    }

    @Test fun level15Rematch() = World1DesignTest.play(15, 2)
    /** Loop, round 2: the door runs to the top left, and when you get close it runs all the way back to where it began. */
    @Test fun level15RematchTheDoorComesBack() {
        val run = b(15, 1).also(World1DesignTest.SOLUTIONS.getValue(15)[1])
        run.expect(WorldState.WON)
        assertTrue("the door is back on the floor", run.world.door.box.x in 10f..14f && run.world.door.box.y > 12f)
    }
    @Test fun level15RematchTheOldRunDoesNotWin() = assertTrue(b(15, 1).also(World1DesignTest.SOLUTIONS.getValue(15)[0]).world.state != WorldState.WON)
    @Test fun level15RematchRunningOnUnderTheBlockFallsThroughTheFloor() = b(15, 1).right(3.5f).expect(WorldState.DEAD)

    // ---------- Act 2 ----------

    // levels 17, 18, 21 and 24: the rematch plays against the habit round 1 taught (docs/LEVEL_DESIGN_V2.md H9)
    private fun solved(n: Int, round: Int, solution: Int) = b(n, round).also(World1DesignTest.SOLUTIONS.getValue(n)[solution])

    @Test fun level17Rematch() = World1DesignTest.play(17, 2)
    /** Night Shift, round 2: hopping in the first stretch drops the floor under the landing, so the old hop falls. */
    @Test fun level17RematchTheOldHopsFall() = solved(17, 1, 0).expect(WorldState.DEAD)
    @Test fun level17RematchHoppingInTheFirstStretchIsFatal() = b(17, 1).hopR(5.8f).wait(2f).expect(WorldState.DEAD)

    @Test fun level18Rematch() = World1DesignTest.play(18, 2)
    /** On the Hour, round 2: the saws come from behind and the keys swap, so the old dodges never win. */
    @Test fun level18RematchTheOldDodgesFail() = assertTrue(solved(18, 1, 0).world.state != WorldState.WON)
    @Test fun level18RematchRunningStraightIsFatal() = b(18, 1).right(3f).expect(WorldState.DEAD)

    @Test fun level21Rematch() = World1DesignTest.play(21, 2)
    /** Foundation, round 2: the landing sinks for hoppers, the plank is a bluff, and the old hops fall. */
    @Test fun level21RematchTheOldHopsFall() = assertTrue(solved(21, 1, 0).world.state != WorldState.WON)
    @Test fun level21RematchTheBluffPlankStillHolds() = b(21, 1).rightTo(25.6f).rightJump(0.3f).landRight().wait(0.5f).expect(WorldState.PLAYING)

    @Test fun level24Rematch() = World1DesignTest.play(24, 2)
    /** Merge Conflict, round 2: no tower, a lift instead, and two branches in a row. */
    @Test fun level24RematchTheOldTowerRouteFails() = assertTrue(solved(24, 1, 0).world.state != WorldState.WON)
    @Test fun level24RematchTheLiftCarriesTheWaiter() = b(24, 1).hopR(6.8f, 0.3f).hopR(17.4f, 0.3f).rightTo(25.0f).rightJump(0.3f).landRight().wait(8f).expect(WorldState.DEAD)

    @Test fun level28Rematch() = World1DesignTest.play(28, 2)
    /** Gym Class, round 2: the same room with a new hand, so the clean run of round 1 never wins it. */
    @Test fun level28RematchTheOldRunFails() = assertTrue(solved(28, 1, 0).world.state != WorldState.WON)

    // ---------- Act 3 ----------

    @Test fun level33Rematch() = World1DesignTest.play(33, 2)
    /** Clear Road, round 2: the road is longer from the first second, and the hop that saved you in round 1 lands on spikes. */
    @Test fun level33RematchTheOldRunFails() = assertTrue(solved(33, 1, 0).world.state != WorldState.WON)
    @Test fun level33RematchTheOldHopLandsOnSpikes() = b(33, 1).hopR(9.0f, 0.4f).wait(0.5f).expect(WorldState.DEAD)

    @Test fun level37Rematch() = World1DesignTest.play(37, 2)
    /** git push --force, round 2: the floor ahead is already deleted, so the old run along the floor falls into the first pit. */
    @Test fun level37RematchTheOldRunFails() = assertTrue(solved(37, 1, 0).world.state != WorldState.WON)
    @Test fun level37RematchStayingOnAStoneDies() = b(37, 1).rightTo(4.0f).rightJump(0.4f).landRight().wait(1.5f).expect(WorldState.DEAD)

    // levels 42, 46, 47 and 48: the rematch plays against the habit round 1 taught (docs/LEVEL_DESIGN_V2.md H9)
    @Test fun level42Rematch() = World1DesignTest.play(42, 2)
    /** Tailwind, round 2: the same room with a new hand (the blinking planks), so the clean run of round 1 never wins it. */
    @Test fun level42RematchTheOldRunFails() = assertTrue(solved(42, 1, 0).world.state != WorldState.WON)

    @Test fun level46Rematch() = World1DesignTest.play(46, 2)
    /** Home Stretch, round 2: the old switch is a bluff and the real one sits on a ledge, so the old run fails. */
    @Test fun level46RematchTheOldRunFails() = assertTrue(solved(46, 1, 0).world.state != WorldState.WON)
    @Test fun level46RematchOldButtonBluffs() {
        val level = World1.levels[45]
        assertTrue("round 2 bluffs with the old button", level.rounds[1].traps.any(::bluff))
        assertTrue("round 1 does not bluff", level.rounds[0].traps.none(::bluff))
    }

    @Test fun level47Rematch() = World1DesignTest.play(47, 2)
    /** sudo make me a sandwich, round 2: the bread and the pickle come down on whoever stands under them, so the old stand-and-butt run dies at the pickle. */
    @Test fun level47RematchTheOldRunFails() = assertTrue(solved(47, 1, 0).world.state != WorldState.WON)

    @Test fun level48Encore() = World1DesignTest.play(48, 2)
    /** Exit, round 2: a second switch lies on the way to the door, wired the other way round, so the old run steps on it. */
    @Test fun level48EncoreTheOldRunFails() = assertTrue(solved(48, 1, 0).world.state != WorldState.WON)
    @Test fun level48EncoreTheSecondSwitchClosesTheWallAgain() {
        val run = b(48, 1).hopR(2.5f, 0.5f).hopR(4.7f, 0.5f).rightTo(8.4f).waitFor { it.group('S').oy >= 7.5f }.rightTo(11.8f)
            .waitFor { World1RoomsE.run { it.ropeUp(14f, 0f, from = 0.643f, to = 0.843f) } }.rightUntil { it.pads[0].down }.right(0.6f)
            .waitFor { it.group('V').oy >= 11.5f }.rightTo(28.6f).right(2f)
        assertTrue("the wall is shut again, x=${run.world.player.box.cx}", run.world.player.box.cx < 26f)
        run.expect(WorldState.PLAYING)
    }
}
