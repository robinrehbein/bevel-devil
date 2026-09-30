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
        World1.levels.forEachIndexed { i, l ->
            if (l.traps.isNotEmpty()) {
                val plays = l.traps.sumOf { t -> t.actions.count { it is Action.Play } }
                assertEquals("level ${i + 1} should play exactly one card", 1, plays)
            }
        }
    }

    @Test
    fun actOneShowsAllTwelveCards() {
        val cards = World1Part1.levels.flatMap { l -> actions(l).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertEquals(Card.entries.toSet(), cards)
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
        val tagged = listOf(17, 18, 19, 20, 22, 23, 25, 26, 28, 31, 32)
        assertTrue(tagged.size >= 10)
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
        // each mechanic shows up alone before it is mixed in
        assertTrue(actions(World1.levels[16]).all { it is Action.Blink })
        assertTrue(actions(World1.levels[17]).all { it is Action.PathSaw })
        assertTrue(World1.levels[18].traps.any { it.trigger is Trigger.Idle })
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
        b(19).wait(2.5f).expect(WorldState.DEAD)
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
    @Test fun level01() = b(1).rightTo(17.4f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level02() = b(2).rightTo(8.6f).rightJump(0.3f).rightTo(22.4f).rightJump(0.3f).right(3f).expect(WorldState.WON)
    @Test fun level03() = b(3).rightTo(26f).wait(0.4f)
        .leftTo(24.9f).leftJump(0.35f).wait(0.3f)
        .leftTo(20.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(14.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(8.7f).leftJump(0.35f).left(1.5f)
        .expect(WorldState.WON)
    @Test fun level04() = b(4).rightTo(12.9f).wait(1.2f)
        .rightTo(13.2f).rightJump(0.35f).right(0.3f).wait(0.2f)
        .rightTo(21.4f).wait(1.2f).rightTo(21.9f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level05() = b(5).rightTo(18.5f).jump(0.3f).wait(0.5f)
        .leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level06() = b(6).rightTo(12.8f).rightJump(0.3f).rightTo(20.8f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level07() = b(7).rightTo(6.6f).rightJump(0.3f).rightTo(11.6f).rightJump(0.3f)
        .rightTo(16.4f).rightJump(0.3f).rightTo(21.3f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level08() = b(8).rightTo(10f).wait(1f)
        .rightTo(15.8f).rightJump(0.25f).rightTo(20.8f).rightJump(0.25f).right(3f)
        .expect(WorldState.WON)
    @Test fun level09() = b(9).rightTo(7.2f)
        .leftKeyRightTo(11.2f).leftJump(0.35f).left(0.3f).leftKeyRightTo(17.8f)
        .rightTo(20.3f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level10() = b(10).rightTo(22.5f).wait(1.5f).leftTo(19.2f).leftJump(0.3f).leftTo(13.9f).leftJump(0.3f).left(3f)
        .expect(WorldState.WON)
    @Test fun level11() = b(11).hopR(5.7f).hopR(10.7f).hopR(15.7f).hopR(20.5f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level12() = b(12).hopL(22.3f).hopL(11.0f).left(3f).expect(WorldState.WON)
    @Test fun level13() = b(13).hopR(20.3f).right(2f).expect(WorldState.WON)
    @Test fun level14() = b(14).rightTo(10.2f).rightJump(0.3f).landRight().wait(1.9f).hopR(15.7f).right(2f).expect(WorldState.WON)
    @Test fun level15() = b(15).rightTo(25.5f).rightJump(0.35f).right(1f).expect(WorldState.WON)
    @Test fun level16() = b(16).rightTo(7.3f).rightJump(0.35f).rightTo(15.4f).rightJump(0.3f).rightTo(24.7f).wait(1.2f).left(3f)
        .expect(WorldState.WON)

    // ---------- Act 2: Neue Regeln ----------
    @Test fun level17() = b(17).rightTo(10.2f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .rightTo(22f).right(2f).expect(WorldState.WON)
    @Test fun level18() = b(18).rightTo(7.3f).waitFor { it.saws[0].y < 10.5f }
        .rightTo(13.3f).waitFor { it.saws[1].y < 10.5f }
        .rightTo(19.8f).waitFor { it.saws[2].y < 10.5f }
        .right(3f).expect(WorldState.WON)
    @Test fun level19() = b(19).fidgetUntil(10f) { it.time > 6.2f }.right(3f).expect(WorldState.WON)
    @Test fun level20() = b(20).hopR(5.7f).hopR(10.7f).hopR(15.7f).hopR(20.5f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level21() = b(21).hopR(7.7f).hopR(15.7f).right(2f).expect(WorldState.WON)
    @Test fun level22() = b(22).rightTo(7.2f).waitFor { it.gapLeft('a') > 1f }
        .rightTo(9.4f).rightJump(0.35f).landRight().waitFor { it.gapLeft('b') > 1f }
        .rightTo(15.4f).rightJump(0.35f).landRight().waitFor { it.gapLeft('c') > 1f }
        .rightTo(21.4f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level23() = b(23).rightTo(8f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .rightTo(13.6f).waitFor { it.saws[0].y < 10.5f }
        .rightTo(17f).right(3f).expect(WorldState.WON)
    @Test fun level24() = b(24).waitUntil(3.3f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level25() = b(25).wait(5f).right(3f).expect(WorldState.WON)
    @Test fun level26() = b(26).rightTo(2.9f)
        .waitFor(14f) { it.solidLeft('a') > 1.2f }.rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('b') > 1.2f }.rightTo(8.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('c') > 1.2f }.rightTo(13.6f).rightJump(0.35f).landRight()
        .waitFor(14f) { it.solidLeft('d') > 1.2f }.rightTo(18.6f).rightJump(0.35f).landRight()
        .rightTo(23.6f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightTo(10.7f).rightJump(0.35f).landRight().rightTo(16.7f).rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level28() = b(28).rightUntilSaw(4.5f).rightJump(0.35f).landRight()
        .rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level29() = b(29).hopR(5.7f).hopR(11.7f).hopR(17.7f).hopR(23.7f).right(2f).expect(WorldState.WON)
    @Test fun level30() = b(30).waitUntil(8.3f).hopR(12.2f).hopR(15.7f).hopR(17.7f).hopR(19.7f).right(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightTo(4.3f).rightTo(7.4f).waitFor { it.saws[0].y < 6.3f }
        .rightTo(9.7f).rightJump(0.35f).landRight()
        .waitFor { it.saws[1].y < 6.3f }.rightTo(16.6f).rightJump(0.35f).landRight()
        .right(3f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightTo(5.8f)
        .fidgetUntil(10f) { it.solidLeft('a') > 1.6f }
        .rightTo(13.4f).fidgetUntil(10f) { it.solidLeft('b') > 1.2f }
        .rightTo(22.5f).rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 3: Mephi schummelt ----------
    @Test fun level33() = b(33).right(4f)
        .also { assertEquals(FakeEnd.CLEAR, it.world.fake?.end) }
        .waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .leftTo(18f).hopL(17.4f).hopL(8.6f).left(4f).expect(WorldState.WON)
    @Test fun level34() = b(34).wait(0.5f).tapPause().also { assertEquals(1, it.world.dodges) }
        .pauseResume().wait(0.1f).hopR(10.6f).right(4f).expect(WorldState.WON)
    @Test fun level35() = b(35).waitUntil(11.5f).right(2f).expect(WorldState.WON)
    @Test fun level36() = b(36).rightTo(10f).wait(1.6f).hopR(14.4f).right(4f).expect(WorldState.WON)
    @Test fun level37() = b(37).hopR(10.7f).hopR(16.5f).rightTo(23.5f).wait(2.5f).hopL(20.5f).hopL(13.3f).hopL(8.5f).left(3f).expect(WorldState.WON)
    @Test fun level38() = b(38).rightTo(6.5f)
        .waitWhile(1f) { it.viewTurn() < 1f }
        .leftKeyRightTo(11.4f).leftJump(0.35f).landLeft()
        .waitWhile(5f) { it.viewTurn() > 0f }
        .right(4f).expect(WorldState.WON)
    @Test fun level39() = b(39).rightTo(9.2f).wait(0.2f).tilt(1f).wait(2.4f).right(2f).expect(WorldState.WON)
    @Test fun level40() = b(40).rightTo(4.6f).wait(1.0f).hopS(17.7f).left(3f).expect(WorldState.WON)
    @Test fun level41() = b(41).rightTo(10.2f).wait(1.2f).rightTo(20.7f).wait(1.2f).right(3f).expect(WorldState.WON)
    @Test fun level42() = b(42).hopR(10.2f).hopR(15.6f).hopR(20.5f).hopR(25.4f).right(1f).expect(WorldState.WON)
    @Test fun level43() = b(43).right(4f).also { it.expect(WorldState.DEAD) }
        .retry().hopR(13.6f).hopR(21.0f).right(3f).expect(WorldState.WON)
    @Test fun level44() = b(44).rightTo(18f).shake().right(3f).expect(WorldState.WON)
    @Test fun level45() = b(45).rightJump(0.35f).landRight().hopR(18.2f).right(2f).expect(WorldState.WON)
    @Test fun level46() = b(46).left(1.2f).hopR(23.6f).right(2f).expect(WorldState.WON)
    @Test fun level47() = b(47).rightTo(10.5f).jump(0.3f).wait(0.4f).right(4f).expect(WorldState.WON)
    @Test fun level48() = b(48).rightTo(9f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .right(4f).also { assertEquals(FakeEnd.CREDITS, it.world.fake?.end) }
        .waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded }
        .leftTo(27.6f).leftJump(0.35f).landLeft()
        .leftTo(22.4f).leftJump(0.35f).landLeft()
        .leftTo(15.4f).leftJump(0.35f).landLeft()
        .left(3f).expect(WorldState.WON)
}
