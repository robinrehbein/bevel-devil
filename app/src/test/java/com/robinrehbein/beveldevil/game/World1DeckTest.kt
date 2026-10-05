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
        4 to { b -> b.rightTo(13.12f).wait(0.7f).leftTo(10.4f).wait(0.4f).rightTo(11.6f).rightJump(0.35f).landRight()
            .rightTo(17.3f).waitFor { it.player.grounded }.wait(0.7f)
            .leftTo(17.6f).rightTo(19f).rightJump(0.35f).landRight().wait(0.5f)
            .rightJump(0.35f).landRight().right(1f).left(2f) },
        6 to { b -> b.rightTo(12.8f).rightJump(0.3f).rightTo(18.6f).rightJump(0.35f).landRight()
            .rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f) },
        // levels 7-16 are rebuilt: their round 1 is the registered solution (World1RoomsA)
        8 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(8)[0]) },
        12 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(12)[0]) },
        15 to { b -> b.also(World1DesignTest.SOLUTIONS.getValue(15)[0]) },
        17 to { b -> b.rightTo(7.5f).waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
            .rightTo(24.8f).rightJump(0.35f).landRight().right(2f) },
        18 to { b -> b.rightTo(7.3f).waitFor { it.saws[0].y < 10.5f }
            .rightTo(11.4f).waitFor { it.saws[1].y < 10.5f }
            .rightTo(19.8f).waitFor { it.saws[2].y < 10.5f }
            .rightTo(23.5f).rightUntilSaw(4.5f).rightJump(0.35f).landRight().left(2f) },
        21 to { b -> b.hopR(7.7f).hopR(15.7f).right(2f) },
        24 to { b -> b.waitUntil(3.3f).rightJump(0.35f).landRight().rightTo(21.4f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(3f) },
        28 to { b -> b.rightUntilSaw(4.5f).rightJump(0.35f).landRight().rightTo(13.3f).rightJump(0.35f).landRight()
            .rightUntilSaw(4.5f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(3f) },
        33 to { b -> b.hopR(18.6f).right(2f).waitWhile { it.fake != null }.waitWhile(2f) { !it.player.grounded || it.door.moving }
            .hopL(22.6f).hopL(17.4f).hopL(8.6f).left(4f) },
        37 to { b -> b.hopR(10.7f).hopR(16.5f).rightTo(23.5f).wait(2.5f).hopL(20.5f).hopL(13.3f).hopL(8.5f).left(3f) },
        42 to { b -> b.hopR(9.2f).hopR(14.3f).hopR(20.5f).hopR(25.4f).right(1f) },
        46 to { b -> b.left(1.2f).hopR(23.6f).right(2f) },
        47 to { b -> b.rightTo(10.5f).jump(0.3f).wait(0.4f).hopR(15.2f).rightJump(0.35f).landRight().right(4f) },
        48 to { b -> b.rightTo(9f).waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
            .right(4f).waitWhile { it.fake != null }.waitWhile(2f) { !it.player.grounded }
            .leftTo(27.6f).leftJump(0.35f).landLeft().leftTo(22.4f).leftJump(0.35f).landLeft()
            .leftTo(15.4f).leftJump(0.35f).landLeft().left(3f) },
    )

    /** Round 2 of the levels with a third round, as in the round tests below. */
    private val round2: Map<Int, (Bot) -> Bot> = mapOf(
        17 to { b -> b.rightTo(7.5f).waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
            .rightTo(19.8f).rightJump(0.35f).landRight().right(3f) },
        28 to { b -> b.hopR(13.3f).rightTo(26.8f).rightJump(0.35f).landRight().right(1f) },
        48 to { b -> b.rightTo(9f).waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
            .rightTo(16.6f).rightJump(0.35f).landRight().right(3f) },
    )

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

    @Test fun level04Rematch() = b(4, 1).rightTo(18.3f).left(0.3f).wait(0.6f).rightTo(18.6f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level04RematchPunishesWaitingForTheOldSlab() = b(4, 1).rightTo(13.2f).wait(1f).right(3f).expect(WorldState.DEAD)

    @Test fun level06Rematch() = b(6, 1).rightTo(12.8f).rightJump(0.3f).right(3f).expect(WorldState.WON)
    @Test fun level06RematchTheOldJumpLandsOnSpikes() =
        b(6, 1).rightTo(12.8f).rightJump(0.3f).rightTo(18.6f).rightJump(0.35f).landRight().right(1f).expect(WorldState.DEAD)

    // levels 8, 12 and 15: the rematch plays against the habit round 1 taught (docs/LEVEL_DESIGN_V2.md H9)
    @Test fun level08Rematch() = World1DesignTest.play(8, 2)
    /** Gravity is opt-in now: walking to the far end turns nothing over, so the way home along the floor is the spike. */
    @Test fun level08RematchWalkingInTurnsNothingOver() {
        val walked = b(8, 1).hopR(19.5f, 0.5f).rightTo(25.5f).landRight().wait(0.5f)
        walked.expect(WorldState.PLAYING)
        assertEquals(1f, walked.world.gravity, 0f)
        walked.leftTo(10f).expect(WorldState.DEAD)
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
    @Test fun level15RematchTheOldWalkEndsOnTheUpperFloorsEdge() = b(15, 1).also(World1DesignTest.SOLUTIONS.getValue(15)[0]).expect(WorldState.PLAYING)

    // ---------- Act 2 ----------

    /** Level 17, [round], waiting before the blinking bridge for it to come back. */
    private fun bridge17(round: Int) = b(17, round).rightTo(7.5f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }

    @Test fun level17Rematch() = bridge17(1).rightTo(19.8f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level17ThirdShift() = bridge17(2).right(3f).expect(WorldState.WON)
    @Test fun level17RoundsPunishTheRoundBefore() {
        b(17, 1).rightTo(9.6f).wait(2.5f).expect(WorldState.PLAYING)   // the ledge bluffs
        bridge17(1).right(3f).expect(WorldState.DEAD)
        b(17, 2).rightTo(9.6f).wait(2.5f).expect(WorldState.DEAD)
        bridge17(2).rightTo(19.8f).rightJump(0.35f).landRight().right(3f).expect(WorldState.DEAD)
    }

    /** Level 18, [round], past the three bobbing saws. */
    private fun saws18(round: Int) = b(18, round).rightTo(7.3f).waitFor { it.saws[0].y < 10.5f }
        .rightTo(11.4f).waitFor { it.saws[1].y < 10.5f }
        .rightTo(19.8f).waitFor { it.saws[2].y < 10.5f }

    /** No fourth saw: the floor before the door collapses, so jump it from its edge. */
    @Test fun level18Rematch() = saws18(1).rightTo(23.2f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun level18RematchRunningOnToMeetTheSawCollapses() = saws18(1).right(3f).expect(WorldState.DEAD)

    @Test fun level21Rematch() = b(21, 1).rightTo(4.6f).rightJump(0.35f).landRight().rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level21RematchTheOldHopsFall() = b(21, 1).hopR(7.7f).hopR(15.7f).right(2f).expect(WorldState.DEAD)

    @Test fun level24Rematch() = b(24, 1).rightTo(20.3f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.35f).landRight().right(2f)
        .expect(WorldState.WON)
    @Test fun level24RematchWaitingForTheOldWallDies() = b(24, 1).waitUntil(3.3f).rightJump(0.35f).landRight().wait(2f).expect(WorldState.DEAD)

    @Test fun level28Rematch() = b(28, 1).hopR(13.3f).rightTo(26.8f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun level28ThirdRound() = b(28, 2).rightTo(18.2f).rightJump(0.35f).landRight().rightTo(26.8f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun level28RoundsPunishTheRoundBefore() {
        b(28, 1).rightTo(26.8f).rightJump(0.35f).landRight().right(1f).expect(WorldState.DEAD)
        b(28, 2).hopR(13.3f).right(1f).expect(WorldState.DEAD)
    }

    // ---------- Act 3 ----------

    @Test fun level33Rematch() = b(33, 1).hopR(18.6f).rightTo(24.4f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun level33RematchNobodyWaitsForTheFake() {
        b(33, 1).hopR(18.6f).right(2f).expect(WorldState.DEAD)
        b(33, 1).hopR(18.6f).wait(1f).expect(WorldState.DEAD)   // the floor erodes behind the landing
    }

    /** The door stays: run through, the floor where round 1 made you wait crumbles. */
    @Test fun level37Rematch() = b(37, 1).hopR(10.7f).hopR(16.5f).right(3f).expect(WorldState.WON)
    @Test fun level37RematchWaitingForThePushDies() = b(37, 1).hopR(10.7f).hopR(16.5f).rightTo(23.5f).wait(1.5f).expect(WorldState.DEAD)

    @Test fun level46Rematch() = b(46, 1).jump(0.3f).wait(0.4f).hopR(23.6f).right(2f).expect(WorldState.WON)
    @Test fun level46RematchOldButtonBluffs() {
        val bot = b(46, 1).left(1.2f).wait(0.5f)
        bot.expect(WorldState.PLAYING)
        assertTrue(bot.world.group('w').visible)
    }

    @Test fun level47Rematch() = b(47, 1).rightTo(9.3f).rightJump(0.35f).landRight().hopR(15.2f).rightJump(0.35f).landRight().right(4f)
        .expect(WorldState.WON)
    @Test fun level47RunningJumpWorksInRoundOneToo() = b(47).rightTo(9.3f).rightJump(0.35f).landRight().hopR(15.2f).rightJump(0.35f).landRight().right(4f)
        .expect(WorldState.WON)
    @Test fun level47RematchStandingUnderTheSandwichDies() = b(47, 1).rightTo(10.5f).jump(0.3f).wait(0.4f).expect(WorldState.DEAD)

    private fun bridge48(round: Int) = b(48, round).rightTo(9f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }

    @Test fun level48Encore() = bridge48(1).rightTo(16.6f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level48SecondEncore() = bridge48(2).rightTo(23.5f).wait(0.3f).leftTo(17.5f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }.left(4f).expect(WorldState.WON)
    @Test fun level48EncoresPunishTheRoundBefore() {
        bridge48(1).right(4f).expect(WorldState.DEAD)
        val stuck = bridge48(2).rightTo(16.6f).rightJump(0.35f).landRight().right(3f)
        assertTrue(stuck.world.state != WorldState.WON)
    }

    /** Tailwind, round 2: the teeth wait over the first pit. Let them come to the start, then outrun them: they are slower. */
    @Test fun level42RematchLetItComeThenRun() = b(42, 1)
        .waitFor { it.group('S').visible && kotlin.math.abs(it.group('S').homeX + it.group('S').ox - it.player.box.cx) < 0.2f }
        .hopR(9.2f).hopR(14.3f).hopR(20.5f).hopR(25.4f).right(1f).expect(WorldState.WON)

    /** Waiting under the pit's edge for them does not help: they settle overhead and the jump goes into them. */
    @Test fun level42RematchJumpingUnderTheStalkerDies() = b(42, 1).rightTo(9.4f).wait(1.5f).rightJump(0.35f).landRight().expect(WorldState.DEAD)
}
