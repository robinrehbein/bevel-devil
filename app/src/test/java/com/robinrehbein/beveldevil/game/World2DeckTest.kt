package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * World 2 in V2: rematch rounds ([Round]) in the same room. Every round has a scripted solution with the real physics,
 * the invariants of [World2Test] hold for each of them, and the room is the same on every attempt (no [Deal]s).
 */
class World2DeckTest {
    /** Level [n] (1-based), round [r] (1-based). */
    private fun b(n: Int, r: Int = 1) = Bot(World2.levels[n - 1], round = r - 1)

    private val all get() = World2.levels.flatMap { l -> l.rounds.map { r -> l to r } }

    // ---------- the rematches ----------

    @Test
    fun enoughRematchesAndBluffs() {
        val rematches = World2.levels.withIndex().filter { it.value.rematch.isNotEmpty() }.map { it.index + 1 }
        assertTrue("rematches only in $rematches", rematches.size >= 20)
        for (act in listOf(1..16, 17..32, 33..48)) assertTrue("act $act: $rematches", rematches.count { it in act } >= 6)
    }

    private fun cardOnly(t: Trap) = t.actions.any { it is Action.Play } && t.actions.all { it is Action.Play || it is Action.Say }
    private fun card(t: Trap) = t.actions.filterIsInstance<Action.Play>().firstOrNull()?.card

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

    /** The level is the same on every attempt: Mephi changes the hand only between rounds. */
    @Test
    fun noRoundDealsByAttempt() {
        for ((l, r) in all) assertTrue(l.name.en, r.traps.all { it.deal == Deal.ALWAYS })
    }

    @Test
    fun everyRoundPlaysExactlyOneCard() {
        for ((l, r) in all) if (r.traps.isNotEmpty()) {
            assertEquals("${l.name.en} (${r.intro.en})", 1, r.traps.sumOf { t -> t.actions.count { it is Action.Play } })
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

    // ---------- Act 1: Handshake ----------

    @Test fun l01r2() = b(1, 2).rightTo(17.5f).hopR(18.9f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun l01r2HoppingTheBluffLandsInSpikes() = b(1, 2).hopR(10.7f).right(0.5f).expect(WorldState.DEAD)
    @Test fun l02r2() = b(2, 2).hopR(3.4f).rightTo(19.6f).hopR(20.3f).hopR(25.1f).right(1f).expect(WorldState.WON)
    @Test fun l02r2TheRoundOneSolutionDies() = b(2, 2).right(0.25f).rightJump(0.40f).right(0.03f).left(0.10f).rightJump(0.55f).leftJump(0.55f)
        .right(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).expect(WorldState.DEAD)
    @Test fun l04r2() = b(4, 2).rightTo(12.2f).wait(0.4f).rightJump(0.22f).wait(0.5f).rightJump(0.35f).landRight()
        .rightTo(19.4f).rightJump(0.35f).landRight().rightTo(22.4f).rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun l06r2() = b(6, 2).rightTo(9f).hopR(20.8f).right(1.5f).expect(WorldState.WON)
    @Test fun l06r2FlushingTheCacheNowPoisonsIt() = b(6, 2).rightTo(6.3f).leftTo(3.4f).jump(0.4f).wait(0.4f).rightTo(9f).right(2f).expect(WorldState.DEAD)
    @Test fun l07r2() = b(7, 2).rightTo(14f).wait(1f).hopR(14.6f).rightTo(21.7f).leftTo(20.8f).wait(1f).hopR(21.3f).right(1f).expect(WorldState.WON)
    @Test fun l07r2WaitingUnderTheFirstCeilingIsFatal() = b(7, 2).rightTo(9.6f).wait(1f).expect(WorldState.DEAD)
    @Test fun l09r2() = b(9, 2).hopR(6.6f).rightTo(10.8f).rightJump(0.35f).landLeft().hopS(15.9f).leftJump(0.23f).landRight()
        .hopR(24.9f).right(1f).expect(WorldState.WON)
    @Test fun l10r2() = b(10, 2).right(0.60f).rightJump(0.55f).landRight().rightJump(0.35f).landRight().right(1f).expect(WorldState.WON)
    @Test fun l11r2() = b(11, 2).right(1.20f).right(0.60f).right(0.10f).right(0.10f).left(0.03f).left(0.03f).left(0.03f).right(1.20f).expect(WorldState.WON)
    @Test fun l11r2BackingOffFallsThroughTheFloor() = b(11, 2).rightTo(14f).leftTo(11f).wait(0.5f).expect(WorldState.DEAD)
    @Test fun l14r2() =b(14, 2).hopR(8.5f).right(2f).expect(WorldState.WON)
    @Test fun l14r2JumpingTheLoopbackLandsInSpikes() = b(14, 2).hopR(8.5f).rightTo(13.8f).rightJump(0.55f).landRight().expect(WorldState.DEAD)
    @Test fun l15r2() = b(15, 2).rightTo(5.2f).hopR(5.25f).right(3f).expect(WorldState.WON)
    @Test fun l15r2HoppingTheOldSpikeIsFatal() = b(15, 2).rightTo(5.2f).hopR(5.25f).hopR(13.2f).expect(WorldState.DEAD)
    @Test fun l16r2() = b(16, 2).rightTo(19.6f).wait(1f).rightJump(0.35f).landRight().hopR(24.7f).right(1f).expect(WorldState.WON)

    // ---------- Act 2: Traffic ----------

    @Test fun l17r2() = b(17, 2).hopR(10.8f).hopR(17.8f).right(2f).expect(WorldState.WON)
    @Test fun l18r2() = b(18, 2).hopR(7f).rightTo(14.3f).waitFor { !it.beams[0].lit }.right(3f).expect(WorldState.WON)
    @Test fun l18r2WaitingBehindTheGateIsFatal() = b(18, 2).hopR(7f).rightTo(14.3f).waitFor { it.beams[0].lit }.waitFor { !it.beams[0].lit }
        .rightTo(19.2f).wait(1.5f).expect(WorldState.DEAD)
    @Test fun l20r2() = b(20, 2).waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).right(3f).expect(WorldState.WON)
    @Test fun l20r2TheRoundOneWaitIsFatal() = b(20, 2).waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).wait(1.3f).expect(WorldState.DEAD)
    @Test fun l25r2() = b(25, 2).hopR(4.2f).hopR(8f).hopR(14f).hopR(20f).right(1f).expect(WorldState.WON)
    @Test fun l25r2TheRoundOneTimingDies() = b(25, 2).right(0.60f).rightJump(0.55f).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.DEAD)
    @Test fun l26r2() = b(26, 2).rightTo(13.6f).waitFor { !it.beams[0].lit }.hopR(16.5f).hopR(22f).right(1f).expect(WorldState.WON)
    @Test fun l26r2KeepAliveHopsDropTheFloor() = b(26, 2).rightTo(12f).fidgetUntil { !it.beams[0].lit }.expect(WorldState.DEAD)
    @Test fun l27r2() = b(27, 2).rightTo(4.2f).leftTo(3f).wait(2.2f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun l27r2TheRoundOneRunDies() = b(27, 2).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.55f).right(0.60f).expect(WorldState.DEAD)
    @Test fun l28r2() = b(28, 2).rightTo(10.5f).right(0.2f).wait(0.4f).rightJump(0.35f).landRight().hopR(25.4f).right(1f).expect(WorldState.WON)
    @Test fun l28r2RunningOutOfTheTunnelIsFatal() = b(28, 2).right(3f).expect(WorldState.DEAD)
    @Test fun l29r2() = b(29, 2).right(0.60f).rightJump(0.55f).rightJump(0.12f).right(0.10f).left(0.10f).rightJump(0.55f).right(0.60f)
        .rightJump(0.55f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun l31r2() = b(31, 2).rightJump(0.55f).rightJump(0.55f).rightJump(0.25f).left(0.10f).right(0.03f).right(0.03f).leftJump(0.12f)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun l31r2SpikesMoved() = b(31, 2).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight()
        .also { assertTrue(it.world.group('C').visible && !it.world.group('A').visible) }.expect(WorldState.PLAYING)

    // ---------- Act 3: Root ----------

    @Test fun l33r2() = b(33, 2).hopR(19.3f).hopR(24.5f).right(1f).expect(WorldState.WON)
    @Test fun l33r2TheLedsSlideOntoTheLanding() = b(33, 2).hopR(18.5f).right(0.3f).expect(WorldState.DEAD)
    @Test fun l34r2() = b(34, 2).right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun l37r2() = b(37, 2).rightJump(0.40f).rightJump(0.40f).rightJump(0.40f).wait(3f).right(1.2f).right(0.25f).rightJump(0.25f).right(0.60f).expect(WorldState.WON)
    @Test fun l37r2RunningAheadMeetsTheSaw() = b(37, 2).rightJump(0.40f).rightJump(0.40f).rightJump(0.40f).right(1.20f).right(0.25f).rightJump(0.25f).right(0.60f).expect(WorldState.DEAD)
    @Test fun l41r2() = b(41, 2).rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun l42r2() = b(42, 2).hopR(5f).right(3f).expect(WorldState.WON)
    @Test fun l42r2TheHighRoadIsTheTrap() = b(42, 2).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(1.20f).expect(WorldState.DEAD)
    @Test fun l45r2() = b(45, 2).jump(0.16f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).left(0.03f).right(0.03f).left(0.03f).right(0.03f)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.12f).rightJump(0.55f).right(1.20f).expect(WorldState.WON)
    @Test fun l45r2TheSandboxShrinksSooner() = b(45, 2).wait(2f).also { assertTrue(it.world.group('l').ox > 0.3f) }.expect(WorldState.PLAYING)
    @Test fun l46r2() = b(46, 2).hopR(2.6f).hopR(7.5f).hopR(15.2f).hopR(22.2f).right(1f).expect(WorldState.WON)
    @Test fun l46r2TheOldClimbOvershoots() = b(46, 2).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.03f).expect(WorldState.DEAD)
    @Test fun l48r2() = b(48, 2).right(0.60f).rightJump(0.12f).right(0.5f).rightTo(15.7f).wait(0.3f).waitFor { it.beams[0].lit }.waitFor { !it.beams[0].lit }
        .hopS(21.4f).right(1.5f).expect(WorldState.WON)
}
