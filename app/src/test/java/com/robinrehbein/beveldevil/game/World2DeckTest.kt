package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * World 2 in V2: rematch rounds ([Round]) in the same room. Every round has a scripted solution with the real physics,
 * the invariants of [World2Test] hold for each of them.
 */
class World2DeckTest {
    /** Level [n] (1-based), round [r] (1-based). */
    private fun b(n: Int, r: Int = 1) = Bot(World2.levels[n - 1], round = r - 1)

    private val all get() = World2.levels.flatMap { l -> l.rounds.map { r -> l to r } }

    // ---------- the rematches ----------

    @Test
    fun enoughRematchesAndBluffs() {
        val rematches = World2.levels.withIndex().filter { it.value.rematch.isNotEmpty() }.map { it.index + 1 }
        assertTrue("rematches only in $rematches", rematches.size in 12..18)
        for (act in listOf(1..16, 17..32, 33..48)) assertTrue("act $act: $rematches", rematches.count { it in act } in 4..7)
    }

    private fun cardOnly(t: Trap) = t.actions.any { it is Action.Bluff }
    private fun card(t: Trap) = t.actions.filterIsInstance<Action.Bluff>().firstOrNull()?.card ?: t.actions.filterIsInstance<Action.Play>().firstOrNull()?.card

    /**
     * A bluff is a rematch card that does nothing but talk. Used sparingly: at most one per act, and only where round 1
     * of the same level played that card for real, so the player has a reason to flinch.
     */
    @Test
    fun bluffsAreRareAndEchoAnHonestCard() {
        val bluffs = World2.levels.withIndex().flatMap { (i, l) ->
            l.rematch.flatMap { r -> r.traps.filter(::cardOnly).map { t -> Triple(i + 1, l, card(t)) } }
        }
        assertTrue("bluffs: ${bluffs.map { it.first }}", bluffs.size in 1..3)
        for (act in listOf(1..16, 17..32, 33..48)) assertTrue("act $act: ${bluffs.map { it.first }}", bluffs.count { it.first in act } <= 1)
        for ((n, l, c) in bluffs) assertTrue("level $n bluffs a card round 1 never played for real",
            l.traps.any { t -> card(t) == c && !cardOnly(t) })
    }

    @Test
    fun everyRoundPlaysExactlyOneCard() {
        for ((l, r) in all) if (r.traps.isNotEmpty()) {
            assertEquals("${l.name.en} (${r.intro.en})", 1, r.traps.sumOf { t -> t.actions.count { it is Action.Play || it is Action.Bluff } })
        }
    }

    @Test
    fun aRematchChangesTheHand() {
        for (l in World2.levels) for (r in l.rematch) assertTrue(l.name.en, r.traps.isNotEmpty())
    }

    @Test
    fun rematchIntrosAreShortBilingualAndNeverRepeat() {
        val intros = World2.levels.flatMap { l -> l.rematch.map { it.intro } }
        intros.forEach { assertTrue(it.en, it.en.isNotBlank() && it.de.isNotBlank() && it.en.length <= 66 && it.de.length <= 66) }
        assertEquals(intros.size, intros.map { it.en }.toSet().size)
        assertEquals(intros.size, intros.map { it.de }.toSet().size)
        assertTrue(intros.none { it.en == it.de })
    }

    @Test
    fun everyRoundParsesAndIsSafeToStandIn() {
        for ((l, r) in all) {
            World(r)
            if (r.traps.none { it.trigger is Trigger.Idle }) Bot(l, l.rounds.indexOf(r)).wait(2.2f).expect(WorldState.PLAYING)
        }
    }

    @Test
    fun holdingRightAloneWinsNoRound() {
        val winners = all.filter { (l, r) -> Bot(l, l.rounds.indexOf(r)).right(14f).world.state == WorldState.WON }.map { (l, r) -> "${l.name.en} r${l.rounds.indexOf(r) + 1}" }
        assertTrue("holding right wins $winners", winners.isEmpty())
    }

    @Test
    fun rematchesStayOutOfTheMetaTwistsAndThePhoneSensors() {
        for ((l, r) in all) if (r !== l) {
            assertTrue(l.name.en, !r.usesMotion)
            assertTrue(l.name.en, (r.start + r.traps.flatMap { it.actions }).none {
                it is Action.FakeWin || it is Action.PauseTrap || it is Action.FrameCrack || it is Action.Flip || it is Action.Roll || it is Action.Ghost
            })
        }
    }

    // ---------- the rematch must be felt ----------

    /**
     * Whatever won round 1 must not win round 2: the scripted round-1 solution of every rematch level (the same
     * scripts as in [World2Test]) is played on round 2 and must not reach the door.
     */
    @Test
    fun theRoundOneSolutionLosesRoundTwo() {
        val rematches = World2.levels.withIndex().filter { it.value.rematch.isNotEmpty() }.map { it.index + 1 }
        assertEquals("every rematch level has its round-1 script here", rematches.toSet(), roundOne.keys)
        for ((n, script) in roundOne) {
            assertEquals("level $n: the round-1 script must win round 1", WorldState.WON, script(b(n)).world.state)
            for (r in 2..World2.levels[n - 1].rounds.size) {
                val state = script(b(n, r)).world.state
                assertTrue("level $n: the round-1 solution also wins round $r", state != WorldState.WON)
            }
        }
    }

    /** The card should not give the trap away: at least half the rematch rounds play another card (or a bluff) than round 1. */
    @Test
    fun atLeastHalfTheRematchRoundsPlayAnotherCard() {
        val rounds = World2.levels.withIndex().flatMap { (i, l) -> l.rematch.indices.map { r -> i + 1 to l.rounds[r + 1] } }
        fun dealt(l: Level) = l.traps.flatMap { it.actions }.firstNotNullOfOrNull { a ->
            when (a) { is Action.Bluff -> Card.BLUFF; is Action.Play -> a.card; else -> null }
        }
        val same = rounds.filter { (n, r) -> dealt(r) == dealt(World2.levels[n - 1]) }.map { it.first }
        assertTrue("rounds with round 1's card again: $same of ${rounds.size}", same.size * 2 <= rounds.size)
    }

    // ---------- Act 1: Handshake ----------

    @Test fun l01r2() = b(1, 2).rightTo(17.5f).hopR(18.9f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun l01r2HoppingTheBluffLandsInSpikes() = b(1, 2).hopR(10.7f).right(0.5f).expect(WorldState.DEAD)
    @Test fun l04r2() = b(4, 2).rightTo(12.2f).wait(0.4f).rightJump(0.22f).wait(0.5f).rightJump(0.35f).landRight()
        .rightTo(19.4f).rightJump(0.35f).landRight().rightTo(22.4f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    /** Sky Blue, round 2: the first ceiling stalks you and drops where round 1 dropped it; run under it, stop before the second. */
    @Test fun l07r2() = b(7, 2).rightTo(14.6f).wait(0.8f).hopR(15.3f).rightTo(21.7f).leftTo(20.8f).wait(1f).hopR(21.3f).right(1f).expect(WorldState.WON)
    @Test fun l07r2TheCeilingFollowsYouAndStandingUnderItIsSafe() {
        val bot = b(7, 2).rightTo(6f).wait(1.5f)
        bot.expect(WorldState.PLAYING)
        assertTrue("the ceiling hovers over Bevel", bot.world.group('c').ox < -2.5f)
    }
    @Test fun l07r2WaitingForTheFirstCeilingIsFatal() = b(7, 2).rightTo(7.6f).wait(1f).expect(WorldState.DEAD)
    /** Server Room, round 2: one fan left, and a floor tile drops in front of the runner; jump the hole. */
    @Test fun l11r2() = b(11, 2).hopR(16.6f).right(2f).expect(WorldState.WON)
    @Test fun l11r2StandingAtTheEdgeAndJumpingWorksToo() = b(11, 2).rightTo(17f).wait(0.6f).rightJump(0.45f).landRight().right(2f).expect(WorldState.WON)
    @Test fun l14r2() =b(14, 2).hopR(8.5f).right(2f).expect(WorldState.WON)
    @Test fun l14r2JumpingTheLoopbackLandsInSpikes() = b(14, 2).hopR(8.5f).rightTo(13.8f).rightJump(0.55f).landRight().expect(WorldState.DEAD)
    // ---------- Act 2: Traffic ----------

    @Test fun l18r2() = b(18, 2).hopR(7f).rightTo(14.3f).waitFor { !it.beams[0].lit }.right(3f).expect(WorldState.WON)
    @Test fun l18r2WaitingBehindTheGateIsFatal() = b(18, 2).hopR(7f).rightTo(14.3f).waitFor { it.beams[0].lit }.waitFor { !it.beams[0].lit }
        .rightTo(19.2f).wait(1.5f).expect(WorldState.DEAD)
    @Test fun l20r2() = b(20, 2).waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).right(3f).expect(WorldState.WON)
    @Test fun l20r2TheRoundOneWaitIsFatal() = b(20, 2).waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).wait(1.3f).expect(WorldState.DEAD)
    @Test fun l25r2() = b(25, 2).hopR(4.2f).hopR(8f).hopR(14f).hopR(20f).right(1f).expect(WorldState.WON)
    @Test fun l25r2TheRoundOneTimingDies() = b(25, 2).right(0.60f).rightJump(0.55f).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.DEAD)
    /** Ticket Number, round 2: the queue (a spike) creeps after you up to the gate; hop over it and back until the gate opens. */
    private fun queue(w: World) = w.group('S').homeX + w.group('S').ox
    @Test fun l26r2() = b(26, 2).rightTo(13.6f).waitFor { queue(it) > it.player.box.cx - 1.6f }.leftJump(0.35f).landLeft()
        .waitFor { !it.beams[0].lit || queue(it) < it.player.box.cx + 1.6f }.rightJump(0.35f).landRight()
        .waitFor { !it.beams[0].lit }.hopR(16.5f).hopR(22f).right(1f).expect(WorldState.WON)
    @Test fun l26r2KeepAliveHopsLandOnTheQueue() = b(26, 2).rightTo(13.6f).fidgetUntil { !it.beams[0].lit }.expect(WorldState.DEAD)
    @Test fun l26r2StandingStillAnywhereIsFineUntilTheQueueComes() = b(26, 2).rightTo(8f).wait(3f).expect(WorldState.PLAYING)
    /** Race Condition, round 2: the landing behind the first pendulum turns the belt forward; ride it, slip under the second one, jump. */
    @Test fun l29r2() = b(29, 2).right(0.60f).rightJump(0.55f).rightJump(0.12f).right(0.10f).left(0.10f).rightJump(0.55f).landRight()
        .waitFor { w -> w.saws.filter { kotlin.math.abs(it.x - 21f) < 0.5f }.all { it.y < 12.2f } }.rightTo(22.6f).rightJump(0.55f).landRight()
        .right(1f).expect(WorldState.WON)
    @Test fun l29r2TheBeltTurnsForwardOnTheLanding() {
        val bot = b(29, 2).right(0.60f).rightJump(0.55f).rightJump(0.12f).right(0.10f).left(0.10f).rightJump(0.55f).landRight().wait(0.05f)
        assertEquals(3f, bot.world.group('b').belt)
    }
    // ---------- Act 3: Root ----------

    /** sudo !!, round 2: the LED pair slides onto where a full jump lands; a short hop comes down in front of it. */
    @Test fun l33r2() = b(33, 2).rightTo(17.6f).rightJump(0.1f).landRight().rightJump(0.55f).landRight().right(1f).expect(WorldState.WON)
    @Test fun l33r2TheLedsSlideOntoTheLanding() = b(33, 2).hopR(18.5f).right(0.3f).expect(WorldState.DEAD)
    /** Reverse Proxy, round 2: a firewall gate wakes up between the second portal's exit and the door; stop after the exit and wait. */
    @Test fun l34r2() = b(34, 2).right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .waitFor { it.beams.isNotEmpty() && !it.beams[0].lit }.right(1f).expect(WorldState.WON)
    /** Security Audit, round 2: the ground behind the stones sinks; take the last stone (honest now) and jump to the door from it. */
    @Test fun l41r2() = b(41, 2).rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f).rightJump(0.55f).landRight()
        .rightJump(0.15f).landRight().rightJump(0.55f).right(1f).expect(WorldState.WON)
    @Test fun l41r2RoundOnesLeapOntoTheGroundSinks() = roundOne.getValue(41)(b(41, 2)).wait(1.5f).expect(WorldState.DEAD)
    @Test fun l42r2() = b(42, 2).hopR(5f).right(3f).expect(WorldState.WON)
    @Test fun l42r2TheHighRoadIsTheTrap() = b(42, 2).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(1.20f).expect(WorldState.DEAD)
    @Test fun l46r2() = b(46, 2).hopR(2.6f).hopR(7.5f).hopR(15.2f).hopR(22.2f).right(1f).expect(WorldState.WON)
    @Test fun l46r2TheOldClimbOvershoots() = b(46, 2).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.03f).expect(WorldState.DEAD)
    /** Rebase, round 2: Ctrl+Z on the wall throws Bevel back past the spikes; the second climb is the real one. */
    @Test fun l44r2() = b(44, 2).right(0.60f).rightJump(0.55f).left(0.03f).left(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f)
        .right(0.03f).right(0.03f).leftJump(0.12f).rightJump(0.55f).wait(0.3f).rightJump(0.4f).landRight().wait(0.1f).right(0.1f).wait(0.6f)
        .also { assertTrue("undone to x=${it.world.player.box.cx}", it.world.player.box.cx < 21f) }
        .rightTo(23f).rightJump(0.4f).landRight().right(2f).expect(WorldState.WON)

    companion object {
        /** The round-1 solutions of the rematch levels, copied from [World2Test], keyed by level number. */
        val roundOne: Map<Int, (Bot) -> Bot> = mapOf(
            1 to { b -> b.right(1.20f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).right(1.20f) },
            4 to { b -> b.right(0.60f).right(0.60f).rightJump(0.25f).rightJump(0.12f).left(0.10f).right(0.03f)
                .left(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f) },
            7 to { b -> b.right(0.60f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).left(0.03f).left(0.03f)
                .rightJump(0.55f).right(0.25f).right(0.10f).left(0.03f).left(0.03f).right(0.03f).left(0.03f).left(0.03f).right(0.03f)
                .left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).left(0.03f)
                .right(0.03f).right(0.03f).left(0.10f).rightJump(0.55f).right(0.60f) },
            11 to { b -> b.right(1.20f).right(0.60f).right(0.10f).right(0.10f).left(0.03f).left(0.03f).left(0.03f).right(1.20f) },
            14 to { b -> b.right(0.60f).rightJump(0.55f).rightJump(0.55f).right(0.60f).right(0.25f).rightJump(0.55f).right(0.60f) },
            18 to { b -> b.right(0.60f).rightJump(0.55f).right(0.25f).right(0.10f).leftJump(0.25f).right(0.60f)
                .rightJump(0.12f).right(0.03f).leftJump(0.12f).left(0.03f).left(0.03f).left(0.10f).rightJump(0.55f)
                .right(0.60f).right(0.60f) },
            20 to { b -> b.waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).wait(1.3f).right(3f) },
            25 to { b -> b.right(0.60f).rightJump(0.55f).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f) },
            26 to { b -> b.rightTo(13.6f).fidgetUntil { !it.beams[0].lit }.hopR(16.5f).hopR(22f).right(1f) },
            29 to { b -> b.right(0.60f).rightJump(0.55f).rightJump(0.12f).right(0.10f).left(0.10f)
                .rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f) },
            33 to { b -> b.right(0.60f).right(0.25f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).right(0.03f) },
            34 to { b -> b.right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).right(0.60f) },
            41 to { b -> b.rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f) },
            42 to { b -> b.rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(1.20f) },
            44 to { b -> b.right(0.60f).rightJump(0.55f).left(0.03f).left(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f)
                .right(0.03f).right(0.03f).leftJump(0.12f).rightJump(0.55f).right(1.20f) },
            46 to { b -> b.rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f)
                .rightJump(0.55f).right(0.03f) },
        )
    }
}
