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

    /** Holding right and never letting go wins no level: even the first lessons end with a surprise. */
    @Test
    fun holdingRightAloneWinsNothing() {
        val winners = World3.levels.withIndex().filter { (_, l) -> Bot(l).right(14f).world.state == WorldState.WON }.map { it.index + 1 }
        assertEquals(emptyList<Int>(), winners)
    }

    @Test
    fun theTrollLevelsPunishTheNaiveRun() {
        for (n in listOf(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 14, 16, 17, 19, 20, 21, 22, 23, 25, 26, 27, 28, 30, 31, 32, 34, 35, 36, 37, 39, 40, 42, 43, 45, 46, 47, 48)) {
            b(n).right(14f).expect(WorldState.DEAD)
        }
        // in these the naive runner is only stopped: the chip under load, a slab, a wall, a ledge he cannot reach
        for (n in listOf(5, 12, 13, 15, 18, 24, 29, 33, 38, 41, 44)) b(n).right(14f).expect(WorldState.PLAYING)
    }

    /** The obvious way through, hopping where it looks natural and then running on, meets the second trap of the chain. */
    @Test
    fun theObviousRunDiesAtTheSecondTrap() {
        b(1).rightTo(22f).rightJump(0.55f).landRight().wait(1.5f).expect(WorldState.DEAD)
        b(4).hopR(11.5f).right(3f).expect(WorldState.DEAD)
        b(5).rightTo(14.3f).rightJump(0.55f).landRight().right(6f).expect(WorldState.DEAD)
        b(8).hopR(14f).right(3f).expect(WorldState.DEAD)
        b(11).hopR(11.5f).right(3f).expect(WorldState.DEAD)
        b(20).hopR(13f).wait(1.5f).expect(WorldState.DEAD)
        b(24).hopR(3.6f).wait(1.6f).expect(WorldState.DEAD)
        b(27).rightTo(5f).rightTo(15.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.DEAD)
        b(31).rightTo(15.5f).waitCooled('h').rightTo(19f).rightJump(0.55f).landRight().wait(1.5f).expect(WorldState.DEAD)
        b(34).hopR(8f).right(3f).expect(WorldState.DEAD)
        b(37).rightTo(9f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(3f).expect(WorldState.DEAD)
        b(43).rightTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).right(6f).expect(WorldState.DEAD)
    }

    // ---------- the hardware cards ----------

    @Test
    fun theHardwareCardsAreDealtHereFirst() {
        val hardware = Card.entries.drop(12)
        assertEquals(6, hardware.size)
        // each new card is played somewhere in world 3, and nowhere in the other worlds
        val here = World3.levels.flatMap { l -> actions(l).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertTrue(here.containsAll(hardware))
        val elsewhere = (World1.levels + World2.levels).flatMap { l -> (l.start + l.traps.flatMap { it.actions }).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertTrue(elsewhere.none { it in hardware })
        // first use of each: the level where its mechanic first appears
        fun first(c: Card) = World3.levels.indexOfFirst { l -> actions(l).any { it is Action.Play && it.card == c } } + 1
        assertEquals(2, first(Card.SHORT_CIRCUIT))
        assertEquals(1, first(Card.OVERCLOCKED))
        assertEquals(8, first(Card.BIT_FLIP))
        assertEquals(17, first(Card.THROTTLE))
        assertEquals(35, first(Card.BACKDRAFT))
        assertEquals(46, first(Card.BIOS))
    }

    @Test
    fun theHardwareCardsHaveBothLanguagesAndFitTheCard() {
        for (c in Card.entries.drop(12)) {
            assertTrue(c.title.en.isNotBlank() && c.title.de.isNotBlank() && c.flavor.en.isNotBlank() && c.flavor.de.isNotBlank())
            assertTrue("${c.name} title too long for the card", c.title.en.length <= 13 && c.title.de.length <= 13)
            assertTrue("${c.name} flavor too long", c.flavor.en.length <= 44 && c.flavor.de.length <= 44)
        }
        assertEquals(Card.entries.size, Card.entries.map { it.title.en }.toSet().size)
    }

    @Test
    fun dyingToTheCardTrapCollectsTheCard() {
        // 8: the second cosmic ray; 34-ish levels collect the same way as every world
        val bot = b(8).hopR(14f).right(3f)
        bot.expect(WorldState.DEAD)
        assertEquals(Card.BIT_FLIP, bot.world.lastCard)
        val hot = b(20).hopR(13f).wait(1.5f)
        hot.expect(WorldState.DEAD)
        assertEquals(Card.OVERCLOCKED, hot.world.lastCard)
    }

    // ---------- reverse trolls: the scary thing is harmless, the calm thing bites ----------

    @Test
    fun aGlowingLiveCableThatWasNeverLive() {
        val bot = b(14)
        assertTrue(bot.world.circuits['Z']!!.powered)
        bot.rightTo(7.2f).rightJump(0.55f).landRight().rightTo(18f)
        assertFalse(bot.world.circuits['Z']!!.powered)
        bot.expect(WorldState.PLAYING)
    }

    @Test
    fun aGlowingPlateThatIsCoolWhileThePlainFloorBurns() {
        val bot = b(20).rightTo(11f)
        assertTrue(bot.world.heaters['h']!!.declared && bot.world.heaters['h']!!.heat < 0.15f)
        bot.expect(WorldState.PLAYING)
        assertFalse(b(20).hopR(13f).world.heaters['f']!!.declared)
    }

    @Test
    fun aFanThatLooksDeadlyCarriesYou() {
        for (n in listOf(36, 40)) {
            val bot = b(n).rightTo(8.6f).rightJump(0.55f)
            bot.expect(WorldState.PLAYING)
            assertTrue("level $n: over the spikes", bot.world.player.box.b < 14.5f)
        }
        assertTrue(actions(World3.levels[35]).none { it is Action.Fan && it.dir != Dir.UP })
    }

    @Test
    fun aButtonThatDoesNothingAndACeilingThatIsReal() {
        val bot = b(12).rightTo(9f)
        assertEquals(1, bot.world.pads.sumOf { it.presses })
        bot.expect(WorldState.PLAYING)
        assertTrue(World3.levels[11].traps.first { it.trigger is Trigger.Pressed }.actions.none { it !is Action.Say })
    }

    // ---------- nothing strands the player alive ----------

    @Test
    fun noFanIsSwitchedOffForGood() {
        World3.levels.forEachIndexed { i, l ->
            val a = actions(l).flatMap { if (it is Action.FakeWin) listOf(it) + it.then else listOf(it) }
            for (off in a.filterIsInstance<Action.Power>().filter { !it.on }) {
                if (a.none { it is Action.Fan && it.id == off.id }) continue
                assertTrue("level ${i + 1} switches fan ${off.id} off for good", a.any { it is Action.Power && it.on && it.id == off.id })
            }
        }
    }

    @Test
    fun aPadStillPowersTheBridgeAfterItsTimerCutIt() {
        // 14 and 16: the bridge is cut on a timer; stepping off and on the pad again powers it for good
        val l14 = b(14).rightTo(5f).wait(3.6f)
        assertFalse(l14.world.circuits['a']!!.powered)
        l14.leftTo(3f).rightTo(5f)
        assertTrue(l14.world.circuits['a']!!.powered)
        val l16 = b(16).rightTo(5f).wait(3.2f)
        assertFalse(l16.world.circuits['a']!!.powered)
        l16.leftTo(3f).rightTo(5f)
        assertTrue(l16.world.circuits['a']!!.powered)
    }

    @Test
    fun landingOnTheFloorBelowTheWiringDiagramCanBeRetried() {
        // walking off the ledge cuts the bridge and lands on the floor; the fan and the pad still work: up again, jump
        b(43).rightTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(17.5f).landRight().wait(0.3f)
            .leftTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(14.6f).rightJump(0.55f).landRight().rightTo(24.8f).rightJump(0.35f).landRight().right(4f).expect(WorldState.WON)
    }

    @Test
    fun fallingThroughABrokenLedgeLeavesTheWayUpOpen() {
        // 33: the ledge breaks under a leap that is too short; 41: the top ledge breaks under a walk. The fans keep blowing.
        b(33).rightTo(12.6f).waitFor { it.player.box.cy < 5.6f }.rightTo(21.4f).rightJump(0.25f).landRight().wait(1f)
            .leftTo(13.3f).waitFor { it.player.box.cy < 5.6f }.rightTo(21.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
        b(41).rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight()
            .rightTo(22.3f).right(1.5f).wait(0.8f)
            .hopL(25.5f).leftTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight()
            .rightTo(21.2f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    }

    // ---------- acts ----------

    @Test
    fun actOneIsCircuits() {
        assertTrue(World3Part1.levels.all { l -> actions(l).none { it is Action.Heat || it is Action.Heatsink || fans(it) } })
        // the first three levels teach a pad, a live trace and a clock, each with one surprise at the end
        assertTrue(actions(World3.levels[0]).any { it is Action.Pad })
        assertTrue(actions(World3.levels[1]).any { it is Action.Circuit && it.group.isUpperCase() })
        assertTrue(actions(World3.levels[2]).any { it is Action.Clock })
        assertTrue(World3Part1.levels.take(3).all { it.traps.isNotEmpty() })
        val a = World3Part1.levels.flatMap(::actions)
        assertTrue(a.any { it is Action.Pad && it.mode == PadMode.HOLD } && a.any { it is Action.Pad && it.mode == PadMode.OFF })
        assertTrue(a.any { it is Action.BitFlip } && a.any { it is Action.Power && !it.on })
    }

    @Test
    fun trapsComeInChains() {
        // every level from the fourth on is a chain of at least two traps; the first three each spring at least one surprise
        val short = World3.levels.withIndex().filter { (i, l) -> i >= 3 && l.traps.size < 2 }.map { it.index + 1 }
        assertTrue("levels without a chain: $short", short.isEmpty())
        assertTrue(World3.levels.take(3).all { it.traps.isNotEmpty() })
        val average = World3.levels.sumOf { it.traps.size } / World3.levels.size.toFloat()
        assertTrue("only $average traps per level", average >= 2.5f)
        // the tight-timing triggers carry the chains
        val timed = World3.levels.count { l -> l.traps.any { it.trigger is Trigger.Airborne || it.trigger is Trigger.Landed } }
        assertTrue("only $timed levels use jump triggers", timed >= 24)
    }

    /** No single gimmick carries the world: a hidden spike sprouting is the answer to a trap in a handful of levels at most. */
    @Test
    fun chainsAreVaried() {
        val hiddenSpikes = World3.levels.count { l -> actions(l).any { it is Action.Show } }
        assertTrue("$hiddenSpikes levels still sprout hidden spikes", hiddenSpikes <= 3)
        val families = mapOf<String, (Action) -> Boolean>(
            "power cut" to { a -> a is Action.Power && !a.on },
            "clock change" to { a -> a is Action.Clock },
            "bit flip" to { a -> a is Action.BitFlip },
            "overclock" to { a -> a is Action.HeatSpike },
            "heat rate" to { a -> a is Action.Heat },
            "fan set" to { a -> a is Action.FanSet },
            "live cable" to { a -> a is Action.Power && a.on },
        )
        for ((name, f) in families) {
            val levels = World3.levels.count { l -> l.traps.flatMap { it.actions }.any(f) }
            assertTrue("$name in only $levels levels", levels >= 2)
        }
        assertTrue(World3.levels.any { l -> l.traps.flatMap { it.actions }.any { it is Action.Fan } })
        // every trap springs on the spot: no level opens with a hidden spike right after the spawn
        World3.levels.forEachIndexed { i, l ->
            val early = l.traps.filter { t -> (t.trigger as? Trigger.PastX)?.x?.let { it < 5f } == true && t.actions.any { it is Action.Show || it is Action.HeatSpike } }
            assertTrue("level ${i + 1} springs something right at the spawn", early.isEmpty())
        }
    }

    @Test
    fun namesAndIntrosDoNotSpoilTheTraps() {
        val banned = listOf("spike", "trap", "collapse", "falls", "drop", "cut", "flip", "reverse", "Falle", "Stachel", "Spike", "stürz", "kapp")
        World3.levels.forEachIndexed { i, l ->
            for (t in listOf(l.name.en, l.name.de, l.intro.en, l.intro.de)) {
                assertTrue("level ${i + 1} gives away a trap: $t", banned.none { t.contains(it, ignoreCase = true) })
            }
        }
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
        b(20).hopR(13f).wait(1.5f).expect(WorldState.DEAD)
        val hop = b(20).hopR(13f).right(1.5f)
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
    @Test fun level01() = b(1).rightTo(22f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level02() = b(2).rightTo(23.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level03() = b(3).rightTo(8.3f).waitPowered('a', false).waitPowered('a').rightTo(23.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level04() = b(4).hopR(11.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level05() = b(5).rightTo(14.3f).rightJump(0.55f).landRight().rightTo(20.8f).rightJump(0.55f).landRight().waitPowered('Z').waitPowered('Z', false).right(3f).expect(WorldState.WON)
    @Test fun level06() = b(6).rightTo(5.3f).rightJump(0.55f).landRight().rightTo(16.6f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level07() = b(7).rightTo(10.1f).leftUntil { !it.circuits['Z']!!.powered }.rightTo(14f).rightTo(18.5f).fidgetUntil { !it.circuits['Y']!!.powered }.right(3f).expect(WorldState.WON)
    @Test fun level08() = b(8).hopR(14f).hopR(18.9f).right(3f).expect(WorldState.WON)
    @Test fun level09() = b(9).rightTo(16.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level10() = b(10).rightTo(6f).rightJump(0.55f).landRight().rightTo(12.3f).waitPowered('b')
        .rightJump(0.55f).landRight().rightTo(19.3f).waitPowered('c').rightJump(0.55f).landRight().rightTo(26.3f).right(3f).expect(WorldState.WON)
    @Test fun level11() = b(11).hopR(11.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level12() = b(12).rightTo(18f).rightJump(0.55f).landRight().waitFor { it.saws[0].x < 21.5f }.right(3f).expect(WorldState.WON)
    @Test fun level13() = b(13).rightTo(5.3f).rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightJump(0.3f).landRight()
        .rightJump(0.3f).landRight().rightJump(0.3f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level14() = b(14).rightTo(7.2f).rightJump(0.55f).landRight().rightTo(25f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level15() = b(15).rightUntil { it.pads[0].presses >= 1 }.hopS(7.3f).leftUntil { it.pads[1].presses >= 1 }.hopR(20f).rightUntil { it.pads[2].presses >= 1 }.leftKeyRightTo(31f).expect(WorldState.WON)
    @Test fun level16() = b(16).rightTo(5f).rightTo(15.5f).waitPowered('Z', false).rightTo(21f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 2: Überhitzung ----------
    @Test fun level17() = b(17).rightTo(15.5f).waitCooled('h').rightTo(24.6f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level18() = b(18).hopR(3f).waitCooled('c').rightTo(19f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level19() = b(19).hopR(6.8f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level20() = b(20).hopR(13f).rightTo(21.6f).rightJump(0.55f).landRight().wait(0.1f).waitWhile(3f) { it.door.moving }.waitCooled('f').waitCooled('g').left(4f).expect(WorldState.WON)
    @Test fun level21() = b(21).rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').rightJump(0.55f).landRight().rightTo(24.6f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level22() = b(22).rightUntilSaw(4.3f).rightJump(0.55f).landRight().rightTo(23.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level23() = b(23).rightTo(7f).waitPowered('a').rightTo(24.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level24() = b(24).hopR(3.6f).hopR(8.9f).hopR(14.9f).hopR(20.9f).hopR(26.9f).right(2f).expect(WorldState.WON)
    @Test fun level25() = b(25).rightTo(11.4f).rightJump(0.55f).landRight().waitCooled('h').rightTo(15.5f).rightJump(0.55f).landRight().rightTo(24.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level26() = b(26).rightTo(13.5f).waitCooled('h').waitPowered('Z', false).rightTo(23.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightTo(5f).rightTo(15.4f).rightJump(0.55f).landRight().rightTo(23.6f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level28() = b(28).rightTo(16.5f).waitCooled('h').rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level29() = b(29).rightTo(10.5f).waitFor { !it.group('w').visible }.rightTo(17.3f).rightJump(0.55f).landRight().rightTo(24.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level30() = b(30).rightTo(6.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }.leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightTo(15.5f).waitCooled('h').rightTo(19f).rightJump(0.55f).landRight().rightTo(24.3f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightTo(5.5f).waitCooled('c').rightTo(25.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)

    // ---------- Act 3: Lüfter ----------
    @Test fun level33() = b(33).rightTo(12.6f).waitFor { it.player.box.cy < 5.6f }.rightTo(21.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level34() = b(34).hopR(8f).rightTo(21.6f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(1f).left(3f).expect(WorldState.WON)
    @Test fun level35() = b(35).rightTo(22f).waitFor { it.fans[0].wind == 0f }.rightTo(24.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level36() = b(36).hopR(8f).rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(1f).left(3f).expect(WorldState.WON)
    @Test fun level37() = b(37).rightTo(9f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().rightTo(22.4f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(1f).left(3f).expect(WorldState.WON)
    @Test fun level38() = b(38).rightTo(6f).rightTo(10f).waitFor { it.player.box.cy < 7f }.rightTo(16.5f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level39() = b(39).rightTo(14.2f).leftTo(10.3f).waitFor { it.fans[0].wind == 0f }.rightTo(14.3f).rightJump(0.55f).landRight().rightTo(21.3f).rightJump(0.55f).landRight().rightUntilSaw(3.2f).rightJump(0.55f).landRight().right(1f).left(3f).expect(WorldState.WON)
    @Test fun level40() = b(40).rightTo(11.35f).waitFor { it.player.grounded }.waitFor { it.fans[0].wind > 5f }.rightTo(25.5f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level41() = b(41).rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight().rightTo(21.2f).rightJump(0.55f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level42() = b(42).rightTo(12.4f).waitFor { it.player.box.cy < 5.8f }.rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level43() = b(43).rightTo(8f).waitFor { it.player.box.cy < 6.9f }.rightTo(14.6f).rightJump(0.55f).landRight().rightTo(24.8f).rightJump(0.35f).landRight().right(4f).expect(WorldState.WON)
    @Test fun level44() = b(44).rightTo(6.5f).waitWhile(1f) { it.viewTurn() < 1f }.leftKeyRightTo(15f).waitWhile(5f) { it.viewTurn() > 0f }.rightTo(22.4f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level45() = b(45).rightTo(8.8f).waitFor { it.player.box.cy < 6.8f }.rightTo(13f).landRight().waitCooled('h').rightTo(15.6f).rightJump(0.55f).landRight().rightTo(24.6f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level46() = b(46).hopR(8.4f).rightTo(16.5f).waitCooled('c').rightTo(26.8f).waitFor { it.player.box.cy < 6.6f }.right(3f).expect(WorldState.WON)
    @Test fun level47() = b(47).rightTo(8.5f).waitPowered('Z').waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .hopL(25.3f).leftTo(12.2f).waitPowered('Z').waitPowered('Z', false).leftTo(8f).left(4f).waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .rightTo(8.5f).waitPowered('Z').waitPowered('Z', false).rightTo(12f).rightTo(17f).waitFor { it.player.box.cy < 6.6f }.right(6f).expect(WorldState.WON)
    @Test fun level48() = b(48).rightTo(5f).waitPowered('Z', false).rightTo(8.4f).rightJump(0.55f).landRight().rightTo(19.3f).rightJump(0.12f)
        .waitFor { it.player.box.cy < 6.4f }.right(6f).expect(WorldState.WON)
}
