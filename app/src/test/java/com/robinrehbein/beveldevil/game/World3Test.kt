package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** One scripted solution per level of World 3 (48 levels, three acts of 16), played with the real physics. */
class World3Test {
    private fun b(n: Int) = Bot(World3.levels[n - 1])

    private fun actions(l: Level) = l.start + l.traps.flatMap { it.actions }

    private fun circuits(a: Action) = a is Action.Circuit || a is Action.Clock || a is Action.Pad || a is Action.Toggle || a is Action.BitFlip
    private fun heat(a: Action) = a is Action.Heat || a is Action.Heatsink || a is Action.HeatSpike
    private fun fans(a: Action) = a is Action.Fan || a is Action.FanSet
    private fun hardware(a: Action) = circuits(a) || heat(a) || fans(a) || a is Action.Power
    private fun meta(a: Action) =
        a is Action.FakeWin || a is Action.PauseTrap || a is Action.FrameCrack || a is Action.Flip || a is Action.Roll || a is Action.Ghost

    // ---------- structure ----------

    @Test
    fun worldHas48LevelsInThreeActsThatParse() {
        assertEquals(48, World3.levels.size)
        assertEquals(16, World3Part1.levels.size)
        assertEquals(16, World3Part2.levels.size)
        assertEquals(16, World3Part3.levels.size)
        World3.levels.forEach { World(it) }
    }

    @Test
    fun worldIsRegisteredAfterWorldTwo() {
        assertEquals(3, Worlds.all.size)
        val w = Worlds.get(3)
        assertEquals(48, w.size)
        assertEquals(96, w.firstLevel)
        assertEquals("3-1", Worlds.label(96))
        assertEquals("3-48", Worlds.label(143))
        assertEquals(144, Levels.all.size)
        assertTrue(Levels.all[96] === World3.levels[0])
    }

    @Test
    fun standingStillIsSafeForTwoSeconds() {
        World3.levels.filter { l -> l.traps.none { it.trigger is Trigger.Idle } }.forEach { l -> Bot(l).wait(2.2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun namesAreUniqueAndShortEnoughForTheHud() {
        val names = World3.levels.map { it.name.en }
        assertEquals(names.size, names.toSet().size)
        assertEquals(names.size, World3.levels.map { it.name.de }.toSet().size)
        World3.levels.forEachIndexed { i, l ->
            assertTrue("level ${i + 1} name too long", l.name.en.length <= 26 && l.name.de.length <= 26)
        }
    }

    @Test
    fun namesAndIntrosAreFilledInBothLanguages() {
        World3.levels.forEach { l ->
            assertTrue(l.name.en.isNotBlank() && l.name.de.isNotBlank() && l.intro.en.isNotBlank() && l.intro.de.isNotBlank())
            assertTrue("${l.name.en}: intro too long", l.intro.en.length <= 66 && l.intro.de.length <= 66)
            assertTrue("${l.name.en}: intro must differ between languages", l.intro.en != l.intro.de)
        }
    }

    @Test
    fun everyTrapLevelPlaysExactlyOneCard() {
        World3.levels.forEachIndexed { i, l ->
            if (l.traps.isNotEmpty()) {
                val plays = l.traps.sumOf { t -> t.actions.count { it is Action.Play } }
                assertEquals("level ${i + 1} should play exactly one card", 1, plays)
            }
        }
    }

    @Test
    fun everyLevelUsesHardware() {
        val plain = World3.levels.withIndex().filter { (_, l) -> actions(l).none(::hardware) }.map { it.index + 1 }
        assertTrue("levels without hardware: $plain", plain.isEmpty())
    }

    /** Holding right and never letting go wins only the lessons that are about something else than dodging. */
    @Test
    fun holdingRightAloneWinsOnlyTheFirstLessons() {
        val winners = World3.levels.withIndex().filter { (_, l) -> Bot(l).right(14f).world.state == WorldState.WON }.map { it.index + 1 }
        assertEquals(listOf(1, 2, 9, 36), winners)
    }

    @Test
    fun theTrollLevelsPunishTheNaiveRun() {
        for (n in listOf(4, 6, 8, 11, 12, 14, 20, 22, 23, 27, 28, 31, 32, 40, 46, 48)) b(n).right(14f).expect(WorldState.DEAD)
        // the chip under load is only a wall to the naive runner: the heatsink ledge blocks the way
        b(18).right(14f).expect(WorldState.PLAYING)
    }

    // ---------- acts ----------

    @Test
    fun actOneIsCircuitsOnly() {
        assertTrue(World3Part1.levels.all { l -> actions(l).none { heat(it) || fans(it) } })
        // the first three levels just teach: a pad, a live trace, a clock. No traps, no cards
        assertTrue(World3Part1.levels.take(3).all { it.traps.isEmpty() })
        assertTrue(actions(World3.levels[0]).any { it is Action.Pad })
        assertTrue(actions(World3.levels[1]).any { it is Action.Circuit && it.group.isUpperCase() })
        assertTrue(actions(World3.levels[2]).any { it is Action.Clock })
        val a = World3Part1.levels.flatMap(::actions)
        assertTrue(a.any { it is Action.Pad && it.mode == PadMode.HOLD } && a.any { it is Action.Pad && it.mode == PadMode.OFF })
        assertTrue(a.any { it is Action.BitFlip } && a.any { it is Action.Power && !it.on })
    }

    @Test
    fun actTwoIsHeat() {
        val tagged = World3Part2.levels.withIndex().filter { (_, l) -> actions(l).any(::heat) }.map { it.index + 17 }
        assertTrue("only $tagged use heat", tagged.size >= 14)
        assertTrue(World3Part2.levels.all { l -> actions(l).none(::fans) })
        val a = World3Part2.levels.flatMap(::actions)
        assertTrue(a.any { it is Action.Heat && it.load } && a.any { it is Action.Heat && it.melt } && a.any { it is Action.HeatSpike })
        // later in the act the circuits of act one come back
        assertTrue(World3Part2.levels.drop(8).any { l -> actions(l).any(::circuits) })
    }

    @Test
    fun actThreeIsFansAndTheBiosFinale() {
        val tagged = World3Part3.levels.withIndex().filter { (_, l) -> actions(l).any(::fans) }.map { it.index + 33 }
        assertTrue("only $tagged use fans", tagged.size >= 14)
        val a = World3Part3.levels.flatMap(::actions)
        assertTrue(a.any { it is Action.Fan && it.dir == Dir.UP } && a.any { it is Action.Fan && it.dir == Dir.RIGHT })
        assertTrue(a.any { it is Action.Fan && it.dir == Dir.LEFT } && a.any { it is Action.Fan && it.dir == Dir.DOWN })
        assertTrue(a.any { it is Action.FanSet } && a.any { it is Action.Fan && it.off > 0f })
    }

    @Test
    fun theFinaleIsThreeLevelsOfBios() {
        assertEquals("POST", World3.levels[45].name.en)
        assertEquals("Boot Order", World3.levels[46].name.en)
        assertEquals("BIOS Setup", World3.levels[47].name.en)
        // every mechanic family comes back in the finale
        val f = World3.levels.drop(45).flatMap(::actions)
        assertTrue(f.any(::circuits) && f.any(::heat) && f.any(::fans))
        // the last level plays the grand finale card, like the act-two finale before it
        assertEquals(Card.GRAND_FINALE, actions(World3.levels[47]).filterIsInstance<Action.Play>().single().card)
        val grand = World3.levels.withIndex().filter { (_, l) -> actions(l).any { it is Action.Play && it.card == Card.GRAND_FINALE } }.map { it.index + 1 }
        assertEquals(listOf(16, 32, 48), grand)
        // the boot order has two fake doors and then the real one
        assertEquals(2, World3.levels[46].traps.count { it.trigger == Trigger.AtDoor })
        assertNotNull(World(World3.levels[47]).door)
    }

    @Test
    fun metaTwistsAreFewAndHaveHardwareFlavour() {
        val kinds = World3.levels.flatMap { l -> actions(l).filter(::meta).map { it::class.simpleName!! } }.toSet()
        assertEquals(setOf("FakeWin", "Flip", "Roll"), kinds)
        val levels = World3.levels.withIndex().filter { (_, l) -> actions(l).any(::meta) }.map { it.index + 1 }
        assertEquals(listOf(30, 44, 47, 48), levels)
    }

    @Test
    fun atMostOneLevelUsesPhoneMotion() {
        val motion = World3.levels.withIndex().filter { (_, l) -> l.usesMotion }.map { it.index + 1 }
        assertTrue("motion levels $motion", motion.size <= 1)
    }

    @Test
    fun everyLevelIsDifferentFromItsNeighbours() {
        World3.levels.zipWithNext().forEachIndexed { i, (x, y) ->
            val cx = actions(x).filterIsInstance<Action.Play>().map { it.card }
            val cy = actions(y).filterIsInstance<Action.Play>().map { it.card }
            val hx = actions(x).filter(::hardware).map { it::class }.toSet()
            val hy = actions(y).filter(::hardware).map { it::class }.toSet()
            assertFalse("levels ${i + 1} and ${i + 2} look the same (${x.name.en} / ${y.name.en})", cx == cy && hx == hy && cx.isNotEmpty())
            assertTrue("levels ${i + 1} and ${i + 2} have the same map", x.map.ascii() != y.map.ascii())
        }
    }

    @Test
    fun mapsAreAllDifferent() {
        val maps = World3.levels.map { it.map.ascii() }
        val dupes = World3.levels.indices.filter { i -> maps.indexOf(maps[i]) != i }.map { it + 1 }
        assertTrue("same map as an earlier level: $dupes", dupes.isEmpty())
    }

    // ---------- the levels that react to the player in unusual ways ----------

    @Test
    fun powerCutDropsTheRailUnderTheNaiveRunner() {
        val bot = b(4).right(2f)
        bot.expect(WorldState.DEAD)
        assertFalse(bot.world.circuits['a']!!.powered)
        assertEquals(Card.COLLAPSE, bot.world.lastCard)
    }

    @Test
    fun theSecondPadUndoesTheFirst() {
        val bot = b(6).right(1.4f)
        assertEquals(2, bot.world.pads.sumOf { it.presses })
        assertFalse(bot.world.circuits['a']!!.powered)
    }

    @Test
    fun aCosmicRayFlipsTheRailsAndJumpingEarlySavesYou() {
        b(8).right(2f).expect(WorldState.DEAD)
        val flip = b(8).hopR(14f)
        assertTrue(flip.world.circuits['b']!!.powered && !flip.world.circuits['a']!!.powered)
    }

    @Test
    fun theTimerSwitchCutsTheBridgeAfterThreeSeconds() {
        b(14).rightTo(7.2f).rightJump(0.55f).landRight().wait(2.5f).right(4f).expect(WorldState.DEAD)
    }

    @Test
    fun overclockedFloorOnlyBurnsWhoStops() {
        b(20).rightTo(13.1f).wait(1.5f).expect(WorldState.DEAD)
        val hop = b(20).hopR(13f)
        assertTrue(hop.world.heaters['f']!!.heat < 1f)
    }

    @Test
    fun theHotSecondHeatsinkKillsOnTouch() {
        val bot = b(31).rightTo(15.5f).waitCooled('h').right(6f)
        bot.expect(WorldState.DEAD)
        assertEquals(Card.SINKING, bot.world.lastCard)
    }

    @Test
    fun theStoneOfReverseThrustSavesTheFloater() {
        val bot = b(40).rightTo(11.35f)
        assertTrue(bot.world.fans[0].target < 0f)
        bot.waitFor { it.player.grounded }
        assertTrue(bot.world.fans[0].target > 0f)
        bot.waitFor { it.fans[0].wind > 5f }.rightTo(25.5f).landRight().right(3f).expect(WorldState.WON)
    }

    @Test
    fun bootOrderHasTwoFakeDoorsBeforeTheRealOne() {
        val bot = b(47).rightTo(8.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
            .waitWhile(2f) { !it.player.grounded || it.door.moving }
        bot.expect(WorldState.PLAYING)
        assertEquals(4f, bot.world.door.box.x + 0.1f, 0.01f)
    }

    @Test
    fun theBiosFanTurnsOnYouIfYouLingerAtTheTop() {
        b(48).rightTo(5f).waitPowered('Z', false).rightTo(8.4f).rightJump(0.55f).landRight().rightTo(19.3f).rightJump(0.12f)
            .waitFor { it.player.box.cy < 6.4f }.wait(3f).expect(WorldState.DEAD)
    }

    // ---------- Act 1: Stromkreise ----------
    @Test fun level01() = b(1).right(5f).expect(WorldState.WON)
    @Test fun level02() = b(2).right(5f).expect(WorldState.WON)
    @Test fun level03() = b(3).rightTo(8.3f).waitPowered('a', false).waitPowered('a').right(4f).expect(WorldState.WON)
    @Test fun level04() = b(4).hopR(11.5f).right(3f).expect(WorldState.WON)
    @Test fun level05() = b(5).hopR(19f).right(3f).expect(WorldState.WON)
    @Test fun level06() = b(6).rightTo(5.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level07() = b(7).rightTo(10.6f).waitPowered('Z', false).rightTo(14f).rightTo(18.5f).waitPowered('Y', false).right(3f).expect(WorldState.WON)
    @Test fun level08() = b(8).hopR(14f).right(3f).expect(WorldState.WON)
    @Test fun level09() = b(9).right(5f).expect(WorldState.WON)
    @Test fun level10() = b(10).rightTo(6f).rightJump(0.55f).landRight().rightTo(12.3f).waitPowered('b')
        .rightJump(0.55f).landRight().rightTo(19.3f).waitPowered('a').rightJump(0.55f).landRight().rightTo(26.3f).right(3f).expect(WorldState.WON)
    @Test fun level11() = b(11).hopR(11.5f).right(3f).expect(WorldState.WON)
    @Test fun level12() = b(12).hopR(12f).right(3f).expect(WorldState.WON)
    @Test fun level13() = b(13).rightTo(5.3f).rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightJump(0.3f).landRight()
        .rightJump(0.3f).landRight().rightJump(0.3f).landRight().right(5f).expect(WorldState.WON)
    @Test fun level14() = b(14).rightTo(7.2f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level15() = b(15).rightUntil { it.pads[0].presses >= 1 }.hopS(7.3f).leftUntil { it.pads[1].presses >= 1 }.hopR(20f).right(3f).expect(WorldState.WON)
    @Test fun level16() = b(16).rightTo(5f).rightTo(15.5f).waitPowered('Z', false).rightTo(21f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 2: Überhitzung ----------
    @Test fun level17() = b(17).rightTo(15.5f).waitCooled('h').right(4f).expect(WorldState.WON)
    @Test fun level18() = b(18).hopR(3f).waitCooled('c').right(5f).expect(WorldState.WON)
    @Test fun level19() = b(19).hopR(6.8f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level20() = b(20).hopR(13f).right(3f).expect(WorldState.WON)
    @Test fun level21() = b(21).rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').right(4f).expect(WorldState.WON)
    @Test fun level22() = b(22).rightTo(24f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level23() = b(23).rightTo(7f).waitPowered('a').right(4f).expect(WorldState.WON)
    @Test fun level24() = b(24).hopR(3.6f).hopR(8.9f).hopR(14.9f).hopR(20.9f).hopR(26.9f).right(2f).expect(WorldState.WON)
    @Test fun level25() = b(25).rightTo(11.4f).rightJump(0.55f).landRight().waitCooled('h').right(5f).expect(WorldState.WON)
    @Test fun level26() = b(26).rightTo(13.5f).waitCooled('h').waitPowered('Z', false).right(5f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightTo(5f).rightTo(15.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level28() = b(28).rightTo(16.5f).waitCooled('h').rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level29() = b(29).rightTo(10.5f).waitFor { !it.group('w').visible }.right(4f).expect(WorldState.WON)
    @Test fun level30() = b(30).rightTo(8.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }.leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightTo(15.5f).waitCooled('h').rightTo(19f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightTo(5.5f).waitCooled('c').rightTo(25.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 3: Lüfter ----------
    @Test fun level33() = b(33).rightTo(12.6f).waitFor { it.player.box.cy < 5.6f }.right(3f).expect(WorldState.WON)
    @Test fun level34() = b(34).hopR(8f).right(3f).expect(WorldState.WON)
    @Test fun level35() = b(35).rightTo(22f).waitFor { it.fans[0].wind == 0f }.rightTo(23.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level36() = b(36).hopR(8f).right(3f).expect(WorldState.WON)
    @Test fun level37() = b(37).rightTo(9f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level38() = b(38).rightTo(6f).rightTo(10f).waitFor { it.player.box.cy < 7f }.right(3f).expect(WorldState.WON)
    @Test fun level39() = b(39).rightTo(14.2f).waitFor { it.fans[0].wind == 0f }.rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level40() = b(40).rightTo(11.35f).waitFor { it.player.grounded }.waitFor { it.fans[0].wind > 5f }.rightTo(25.5f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level41() = b(41).rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level42() = b(42).rightTo(12.4f).waitFor { it.player.box.cy < 5.8f }.right(3f).expect(WorldState.WON)
    @Test fun level43() = b(43).rightTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(17f).right(6f).expect(WorldState.WON)
    @Test fun level44() = b(44).rightTo(6.5f).waitWhile(1f) { it.viewTurn() < 1f }.leftKeyRightTo(15f).waitWhile(5f) { it.viewTurn() > 0f }.right(3f).expect(WorldState.WON)
    @Test fun level45() = b(45).rightTo(8.8f).waitFor { it.player.box.cy < 6.8f }.rightTo(13f).landRight().waitCooled('h').right(6f).expect(WorldState.WON)
    @Test fun level46() = b(46).hopR(8.4f).rightTo(16.5f).waitCooled('c').rightTo(26.8f).waitFor { it.player.box.cy < 6.6f }.right(3f).expect(WorldState.WON)
    @Test fun level47() = b(47).rightTo(8.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .hopL(25.3f).leftTo(11.5f).waitPowered('Z', false).leftTo(8f).left(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .rightTo(8.5f).waitPowered('Z', false).rightTo(12f).rightTo(17f).waitFor { it.player.box.cy < 6.6f }.right(6f).expect(WorldState.WON)
    @Test fun level48() = b(48).rightTo(5f).waitPowered('Z', false).rightTo(8.4f).rightJump(0.55f).landRight().rightTo(19.3f).rightJump(0.12f)
        .waitFor { it.player.box.cy < 6.4f }.right(6f).expect(WorldState.WON)
}
