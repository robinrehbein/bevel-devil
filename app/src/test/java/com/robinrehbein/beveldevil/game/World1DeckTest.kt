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

    /** A trap that plays a card and does nothing else that could hurt: Mephi bluffs. */
    private fun bluff(t: Trap) = t.actions.any { it is Action.Play } &&
        t.actions.all { it is Action.Play || it is Action.Say || it is Action.Shake || it is Action.Roll }

    // ---------- structure ----------

    @Test
    fun rematchesAreSpreadOverAllThreeActsButSpareTheOnboarding() {
        val withRounds = World1.levels.withIndex().filter { it.value.rounds.size > 1 }.map { it.index + 1 }
        assertTrue("rematch levels $withRounds", withRounds.size >= 20)
        assertTrue(withRounds.none { it <= 2 })
        for (act in listOf(1..16, 17..32, 33..48)) assertTrue("act $act: $withRounds", withRounds.count { it in act } >= 5)
        World1.levels.forEach { l -> assertTrue("${l.name.en} has ${l.rounds.size} rounds", l.rounds.size <= 3) }
    }

    /** Levels stay the same on every attempt: what changes is the round, never the try. */
    @Test
    fun noLevelDealsByAttempt() {
        World1.levels.forEachIndexed { i, l ->
            l.rounds.forEach { r -> assertTrue("level ${i + 1} deals by attempt", r.traps.all { it.deal == Deal.ALWAYS }) }
        }
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
            val card = r.traps.first(::bluff).actions.filterIsInstance<Action.Play>().single().card
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

    @Test fun level08Rematch() = b(8, 1).rightTo(9.5f).rightJump(0.2f).rightTo(15.8f).rightJump(0.25f).rightTo(20.8f).rightJump(0.25f).right(4f)
        .expect(WorldState.WON)
    @Test fun level08RematchWalkingInIsSpikes() = b(8, 1).rightTo(10f).wait(1f).right(2f).expect(WorldState.DEAD)

    @Test fun level09Rematch() = b(9, 1).rightTo(11f).rightJump(0.35f).landRight()
        .rightTo(20f).rightJump(0.22f).leftJump(0.2f).landLeft().left(3f).expect(WorldState.WON)
    @Test fun level09RematchFirstSwapIsABluff() = b(9, 1).rightTo(11f).rightJump(0.22f).leftJump(0.2f).landLeft().left(1f).wait(1f)
        .expect(WorldState.DEAD)

    @Test fun level12Rematch() = b(12, 1).hopL(24f).left(4f).expect(WorldState.WON)
    @Test fun level12RematchTheOldHopsLandOnSpikes() =
        b(12, 1).hopL(24f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().left(2f).expect(WorldState.DEAD)

    @Test fun level13Rematch() = b(13, 1).rightTo(8.3f).wait(1.2f).rightTo(25.6f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun level13RematchOldHabitsDie() {
        b(13, 1).right(4f).expect(WorldState.DEAD)
        // jumping the floor that fell in round 1: the spike before the door slides under the landing
        b(13, 1).rightTo(8.3f).wait(1.2f).hopR(21f).right(1f).expect(WorldState.DEAD)
    }

    @Test fun level15Rematch() = b(15, 1).rightTo(13.3f).rightJump(0.35f).landRight().rightTo(25.5f).rightJump(0.35f).right(1f).expect(WorldState.WON)
    @Test fun level15RematchTheOldJumpLandsInTheShiftedHole() = b(15, 1).rightTo(11.8f).rightJump(0.35f).landRight().right(1f)
        .expect(WorldState.DEAD)

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

    @Test fun level18Rematch() = b(18, 1).rightTo(7.3f).waitFor { it.saws[0].y < 10.5f }
        .rightTo(11.4f).waitFor { it.saws[1].y < 10.5f }
        .rightTo(19.8f).waitFor { it.saws[2].y < 10.5f }
        .right(3f).expect(WorldState.WON)
    @Test fun level18RematchWaitingForTheFrontSawDies() = b(18, 1).rightTo(7.3f).waitFor { it.saws[0].y < 10.5f }
        .rightTo(11.4f).waitFor { it.saws[1].y < 10.5f }
        .rightTo(19.8f).waitFor { it.saws[2].y < 10.5f }
        .rightTo(23.5f).wait(1.5f).expect(WorldState.DEAD)

    @Test fun level21Rematch() = b(21, 1).rightTo(4.6f).rightJump(0.35f).landRight().rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level21RematchTheOldHopsFall() = b(21, 1).hopR(7.7f).hopR(15.7f).right(2f).expect(WorldState.DEAD)

    private fun bridge23() = b(23, 1).rightTo(5.8f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .rightTo(12.9f).waitFor { it.saws[0].y < 9.5f }

    @Test fun level23Rematch() = bridge23().right(4f).expect(WorldState.WON)
    @Test fun level23RematchJumpingTheMendedGapDies() = bridge23().rightTo(13.3f).rightJump(0.35f).landRight().right(4f).expect(WorldState.DEAD)

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

    @Test fun level36Rematch() = b(36, 1).rightTo(19.3f).rightJump(0.35f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level36RematchWaitingForTheFrameDies() = b(36, 1).rightTo(10f).wait(1.6f).expect(WorldState.DEAD)

    @Test fun level37Rematch() = b(37, 1).hopR(10.7f).rightTo(14.2f).hopL(13.8f).hopL(8.6f).left(2f).expect(WorldState.WON)
    @Test fun level37RematchWaitingAtTheOldSpotDies() = b(37, 1).hopR(10.7f).hopR(16.5f).right(2f).expect(WorldState.DEAD)

    @Test fun level38Rematch() = b(38, 1).rightTo(11.4f).rightJump(0.17f).leftJump(0.2f).landLeft()
        .waitWhile(5f) { it.viewTurn() > 0f }.right(3f).expect(WorldState.WON)
    @Test fun level38RematchHoldingOnIsPushedBack() {
        val bot = b(38, 1).rightTo(11.4f).rightJump(0.35f).landRight().wait(1f)
        assertTrue(bot.world.state == WorldState.DEAD || bot.world.player.box.cx < 13f)
    }

    @Test fun level40Rematch() = b(40, 1).rightTo(4.6f).wait(1.0f).rightTo(17.7f).leftJump(0.35f).landLeft().left(3f).expect(WorldState.WON)
    @Test fun level40RematchTheOldKeysWalkBack() {
        val bot = b(40, 1).rightTo(4.6f).wait(1.0f).left(1f)
        assertTrue(bot.world.player.box.cx < 4.6f)
    }

    @Test fun level41Rematch() = b(41, 1).rightTo(10.2f).wait(1.2f).rightTo(21.6f).wait(1.2f).right(3f).expect(WorldState.WON)
    @Test fun level41RematchTheOldPauseIsTooEarly() = b(41, 1).rightTo(10.2f).wait(1.2f).rightTo(20.7f).wait(1.2f).right(3f).expect(WorldState.DEAD)

    @Test fun level45Rematch() = b(45, 1).rightJump(0.35f).landRight().hopR(18.2f).right(2f).expect(WorldState.WON)
    @Test fun level45RematchTheOldHopLandsOnSpikes() = b(45, 1).rightJump(0.35f).landRight().hopR(8f).wait(0.5f).expect(WorldState.DEAD)

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
}
