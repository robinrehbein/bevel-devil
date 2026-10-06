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
    private fun hardware(a: Action) = circuits(a) || heat(a) || fans(a) || a is Action.Power || a is Action.Swap
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
        // every round counts: a rematch deals exactly one card too (a bluff card counts)
        World3.levels.forEachIndexed { i, l ->
            l.rounds.forEachIndexed { r, round ->
                if (round.traps.isNotEmpty()) {
                    val plays = round.traps.sumOf { t -> t.actions.count { it is Action.Play || it is Action.Bluff } }
                    assertEquals("level ${i + 1} round ${r + 1} should play exactly one card", 1, plays)
                }
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
        for (n in listOf(2, 3, 5, 6, 7, 8, 10, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 25, 26, 27, 28, 29, 31, 33, 34, 36, 37, 38, 39, 41, 44, 45)) {
            b(n).right(14f).expect(WorldState.DEAD)
        }
        // (29: the floor under the hatch pad flickers away under him; 38: the floor cable goes live as soon as he heads for the lift;
        // 44: the floor past the lift is studded)
        // in these the naive runner is only stopped: a slab, a wall, a ledge he cannot reach, a lift that never ran, the right wall
        // he starts against (9, 12, 14, 35, 40, 42: the door is to the left),
        // a wall that stays shut (43), a lift he runs through (46, 47), the way up that he never takes (48)
        for (n in listOf(1, 4, 9, 11, 12, 14, 24, 30, 32, 35, 40, 42, 43, 46, 47, 48)) b(n).right(14f).expect(WorldState.PLAYING)
    }

    /** The obvious way through, hopping where it looks natural and then running on, meets the second trap of the chain (levels 1-8 are checked by the design guard rails, H2). */
    @Test
    fun theObviousRunDiesAtTheSecondTrap() {
        b(20).hopR(13f).wait(1.5f).expect(WorldState.DEAD)
        b(24).leftTo(27f).landLeft().leftTo(12f).wait(0.5f).expect(WorldState.DEAD)
        b(27).rightTo(4.5f).rightTo(21.9f).rightJump(0.5f).landRight().right(3f).expect(WorldState.DEAD)
        b(31).rightTo(9.8f).right(4f).expect(WorldState.DEAD)
        // 33: the leap that is obvious on the bridge (a long one) comes down on the plank that gives way; 36: hugging the right wall of the chute is where the second bar comes from;
        // 39: running on along the roof brings its ceiling down on you
        b(33).rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.rightTo(15.3f).rightJump(0.5f).landRight().wait(2f).expect(WorldState.DEAD)
        b(36).rightTo(7.4f).right(8f).expect(WorldState.DEAD)
        b(39).right(3f).expect(WorldState.DEAD)
        // 42: the vent carries you up and the hood slams down on the shelf ahead: running on under it is the end
        b(42).leftTo(17f).waitFor { it.player.box.cy < 6.8f }.leftUntil { it.player.grounded && it.player.box.b < 7.5f }.left(1.5f).expect(WorldState.DEAD)
    }

    // ---------- the hardware cards ----------

    @Test
    fun theHardwareCardsAreDealtHereFirst() {
        val hardware = Card.entries.drop(12).take(6)
        assertEquals(listOf(Card.SHORT_CIRCUIT, Card.OVERCLOCKED, Card.BIT_FLIP, Card.BACKDRAFT, Card.THROTTLE, Card.BIOS), hardware)
        // each new card is played somewhere in world 3, and nowhere in the other worlds
        val here = World3.levels.flatMap { l -> actions(l).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertTrue(here.containsAll(hardware))
        val elsewhere = (World1.levels + World2.levels).flatMap { l -> (l.start + l.traps.flatMap { it.actions }).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertTrue(elsewhere.none { it in hardware })
        // first use of each: the level where its mechanic first appears (World 3 opens on power: the short circuit is the very
        // first card, the BIOS boots the board in level 2, the overclocked floor waits for the first hot chip)
        fun first(c: Card) = World3.levels.indexOfFirst { l -> actions(l).any { it is Action.Play && it.card == c } } + 1
        assertEquals(1, first(Card.SHORT_CIRCUIT))
        assertEquals(2, first(Card.BIOS))
        assertEquals(11, first(Card.OVERCLOCKED))
        assertEquals(8, first(Card.BIT_FLIP))
        assertEquals(17, first(Card.THROTTLE))
        assertEquals(35, first(Card.BACKDRAFT))
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
        // 21 (rebuilt): the wall that follows you is the card trap of the room
        val wall = b(21).right(14f)
        wall.expect(WorldState.DEAD)
        assertEquals(Card.STALKER, wall.world.lastCard)
    }

    // ---------- reverse trolls: the scary thing is harmless, the calm thing bites ----------

    @Test
    fun aGlowingPlateThatIsCoolWhileThePlainFloorBurns() {
        // 20 (rebuilt): the glowing plate is declared and cool, the plain floor is not declared at all until it flares
        val bot = b(20).rightTo(5.2f)
        assertTrue(bot.world.heaters['h']!!.declared && bot.world.heaters['h']!!.heat < 0.15f)
        bot.expect(WorldState.PLAYING)
        assertTrue(World3.levels[19].start.none { it is Action.Heat && it.group == 'f' })
    }

    @Test
    fun aFanThatLooksDeadlyCarriesYou() {
        // 40: the pit is full of spikes, and the draft carries you across it
        val bot = b(40).leftTo(23.2f).left(0.6f)
        bot.expect(WorldState.PLAYING)
        assertTrue("over the spikes", bot.world.player.box.b < 14f)
    }

    @Test
    fun aButtonThatDoesNothingAndACeilingThatIsReal() {
        val won = DesignRules.play(World3.levels[11], 0, World3DesignTest.SOLUTIONS.getValue(12)[0])
        won.expect(WorldState.WON)
        assertTrue(won.world.pads.sumOf { it.presses } >= 1)
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
    fun aTimerCutBridgeComesBackAndStrandsNobody() {
        // 16: the pad powers the bridge for 3.2 s; it is cut, the player waits alive on the left, and the next window opens by itself
        val l16 = b(16).rightTo(5f).wait(3.4f)
        assertFalse(l16.world.circuits['a']!!.powered)
        l16.expect(WorldState.PLAYING)
        l16.waitPowered('a')
        assertTrue(l16.world.circuits['a']!!.powered)
    }

    @Test
    fun theBridgeOfTheWiringDiagramComesBackWhenYouPressTheSwitchAgain() {
        // 43: the first switch gives the bridge power for a moment; whoever is late waits at a dead bridge, alive, and steps off and on the switch again
        val bot = b(43).rightTo(5.6f).rightTo(7f).wait(2.4f)
        assertFalse(bot.world.circuits['a']!!.powered)
        bot.expect(WorldState.PLAYING)
        bot.leftTo(4.3f).rightTo(5.6f).wait(0.2f)
        assertTrue(bot.world.circuits['a']!!.powered)
        bot.rightTo(14.8f).rightTo(17.8f).waitFor { it.player.box.cy < 7.4f }.expect(WorldState.PLAYING)
    }

    @Test
    fun theFanThatStopsForTheSelfTestComesBack() {
        // 46: the first lift stops for the beep and runs again; whoever waits at its foot is never stranded
        val bot = b(46).rightTo(13.5f).wait(0.5f)
        assertTrue(bot.world.fans[0].target > 0f && !bot.world.fans[0].on)
        bot.expect(WorldState.PLAYING)
        bot.wait(1.6f)
        assertTrue(bot.world.fans[0].on)
        bot.expect(WorldState.PLAYING)
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
        assertTrue(a.any { it is Action.Pad && it.mode == PadMode.ON } && a.any { it is Action.Pad && it.mode == PadMode.OFF })
        assertTrue(a.any { it is Action.BitFlip } && a.any { it is Action.Power && !it.on })
    }

    @Test
    fun trapsComeInChains() {
        // every level from the fourth on (except the single-trap bit flip of 8) is a chain of at least two traps; the first three each spring at least one surprise
        val short = World3.levels.withIndex().filter { (i, l) -> i >= 3 && i != 7 && l.traps.size < 2 }.map { it.index + 1 }
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
        // a trap starts or sets a fan (a fan that wakes up when the player comes near, or the lift that waits for a passenger)
        assertTrue(World3.levels.any { l -> l.traps.flatMap { it.actions }.any { it is Action.Fan || (it is Action.Power && it.on && l.start.any { s -> s is Action.Fan && s.id == it.id }) } })
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
        // a fan blowing right is a LEFT-facing fan on the right wall with a negative speed (34), or a RIGHT-facing one
        assertTrue(a.any { it is Action.Fan && it.dir == Dir.UP } && a.any { it is Action.Fan && (it.dir == Dir.RIGHT || (it.dir == Dir.LEFT && it.speed < 0f)) })
        assertTrue(a.any { it is Action.Fan && it.dir == Dir.LEFT && it.speed > 0f } && a.any { it is Action.Fan && it.dir == Dir.DOWN })
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
        // the last level plays the grand finale card (round 1); the act-two finale plays it in its rematch, the act-one finale plays the annex card
        assertEquals(Card.GRAND_FINALE, actions(World3.levels[47]).filterIsInstance<Action.Play>().single().card)
        val grand = World3.levels.withIndex().filter { (_, l) -> actions(l).any { it is Action.Play && it.card == Card.GRAND_FINALE } }.map { it.index + 1 }
        assertEquals(listOf(48), grand)
        // the act-two finale (rebuilt) keeps its grand finale for the rematch
        assertTrue(Card.GRAND_FINALE in DesignRules.cards(World3.levels[31]))
        // the boot order has two doors that run away (one room: the annex belongs to the finale's gag), the finale has two rooms
        assertEquals(2, World3.levels[46].traps.count { t -> t.actions.any { it is Action.DoorTo } })
        assertEquals(1, World3.levels[46].rooms)
        assertEquals(2, World3.levels[47].rooms)
        assertNotNull(World(World3.levels[47]).door)
    }

    @Test
    fun metaTwistsAreFewAndHaveHardwareFlavour() {
        // the monitor that turns (48), the memory that rewinds and the frame that fails its self-test (46); the fake endings were left out (a fake win takes 3.4 s of nothing, docs/LEVEL_DESIGN_V2.md H3)
        val kinds = World3.levels.flatMap { l -> actions(l).filter { meta(it) || it is Action.Undo }.map { it::class.simpleName!! } }.toSet()
        assertEquals(setOf("Flip", "Undo", "FrameCrack"), kinds)
        val levels = World3.levels.withIndex().filter { (_, l) -> actions(l).any { meta(it) || it is Action.Undo } }.map { it.index + 1 }
        assertEquals(listOf(46, 48), levels)
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
    fun touchingTheSolidRailCutsItsNeighbour() {
        // 4: the rail 'b' looks solid and is, until you touch it: then the rail 'a' goes dark
        assertTrue(World3.levels[3].traps.any { t -> t.trigger is Trigger.Touch && t.actions.any { it is Action.Power && !it.on } })
        World3DesignTest.play(4)
    }

    @Test
    fun theSecondButtonPutsTheWallBack() {
        // 6: the first button cuts the wall of copper, the second (on the way back) restores it: whoever walks over it after
        // the island finds the wall back in place, the solution hops it
        val bot = DesignRules.play(World3.levels[5], 0, World3DesignTest.SOLUTIONS.getValue(6)[0])
        bot.expect(WorldState.WON)
        assertTrue(bot.world.pads[0].presses >= 1 && bot.world.pads[1].presses == 0)
        val wall = Bot(World3.levels[5]).leftTo(7.2f).leftJump(0.55f).landLeft().wait(0.3f).rightTo(3.2f).rightJump(0.55f).landRight()
            .rightTo(9.4f).rightJump(0.55f).landRight().rightTo(23.8f)
        assertTrue(wall.world.pads.all { it.presses >= 1 })
        assertTrue(wall.world.circuits.getValue('w').powered)
    }

    @Test
    fun aCosmicRayFlipsTheRailsAndJumpingEarlySavesYou() {
        Bot(World3.levels[7]).right(8f).expect(WorldState.DEAD)
        val won = DesignRules.play(World3.levels[7], 0, World3DesignTest.SOLUTIONS.getValue(8)[0])
        won.expect(WorldState.WON)
        // the second ray flips the halves back once you stand on the far one: the near half is lit again, the far one dark
        assertTrue(!won.world.circuits['b']!!.powered && won.world.circuits['a']!!.powered)
        // and whoever keeps the reflex of the first ray (jump at once) meets the pins in the low ceiling
        Bot(World3.levels[7]).rightTo(14.4f).rightJump(0.55f).landRight().rightJump(0.3f).landRight().rightJump(0.55f).landRight()
            .expect(WorldState.DEAD)
    }

    @Test
    fun theTimerSwitchCutsTheBridgeAfterThreeSeconds() {
        b(16).rightTo(5f).wait(3.4f).right(5f).expect(WorldState.DEAD)
    }

    @Test
    fun overclockedFloorOnlyBurnsWhoStops() {
        // 20 (rebuilt): standing on the plain floor burns, standing on the glowing plate does not
        b(20).rightTo(8.0f).wait(1.5f).expect(WorldState.DEAD)
        b(20).rightTo(5.2f).wait(1.5f).expect(WorldState.PLAYING)
    }

    @Test
    fun theCraneOverTheSecondHeatsinkFallsOnWhoStays() {
        // 31 (rebuilt): the first heatsink starts the stalker crane (the card), the second one has a crane of its own
        val bot = b(31).rightTo(9.8f).waitCooled('c', 0.05f)
        assertEquals(Card.STALKER, bot.world.lastCard)
        bot.rightTo(25.4f).rightJump(0.5f).landRight()
            .leftTo(27.8f).leftJump(0.5f).landLeft().leftTo(24.0f).leftJump(0.5f).landLeft().wait(2f).expect(WorldState.DEAD)
    }

    @Test
    fun theKeepOfReverseThrustSavesTheFloater() {
        // 40: the draft reverses as you float (a weaker wind pushes down), the stone keep is where you wait, and touching it brings the draft back
        val bot = b(40).leftTo(23.4f)
        assertTrue(bot.world.fans[0].target > 0f)
        bot.leftUntil { it.player.grounded && it.player.box.cx < 17.6f }
        assertTrue(bot.world.fans[0].target < 0f)
        bot.expect(WorldState.PLAYING)
        bot.waitFor { it.fans[0].wind > 4.5f }
        assertTrue(bot.world.fans[0].target > 0f)
        bot.expect(WorldState.PLAYING)
    }

    @Test
    fun theBaitDoorOfBootOrderRunsUpstairsAndTheRealOneRunsBackDown() {
        // 47: coming near the door on the floor sends it to the shelf ...
        val run = b(47).rightTo(16.6f).rightTo(19.5f).wait(1.2f)
        run.expect(WorldState.PLAYING)
        assertTrue("the door is on the shelf", run.world.door.box.y < 6f && run.world.door.box.x > 27f)
        // ... and coming near that one sends it back to the floor, while the lift turns to blow down; the boot loop turns it around once more
        val up = b(47).rightTo(22.5f).waitFor { it.player.box.cy < 5.6f }.rightUntil { it.player.grounded && it.player.box.b < 6.5f }.rightUntil { it.player.box.cx > 27.0f }
        up.wait(0.3f)
        assertTrue("the door is on the floor", up.world.door.box.y > 10f)
        assertTrue("the lift blows down", up.world.fans[0].target < 0f)
        up.wait(1.6f)
        assertTrue("the lift is on again", up.world.fans[0].target > 0f)
    }

    @Test
    fun theSwitchOnTheHotShelfTurnsTheSecondFanAround() {
        // 48: the lift carries you to the hot shelf; the switch on it turns the fan at the far end from sucking to blowing
        val bot = b(48).rightTo(10.5f).waitFor { it.player.box.cy < 6.4f }.rightUntil { it.player.grounded && it.player.box.b < 7.5f }
        val g = bot.world.fans.single { it.id == 'g' }
        assertTrue(g.target < 0f)
        bot.rightTo(15.0f)
        assertTrue(g.target > 0f)
    }

    // ---------- Act 1: Stromkreise ----------
    @Test fun level01() { World3DesignTest.play(1) }
    @Test fun level02() { World3DesignTest.play(2) }
    @Test fun level03() { World3DesignTest.play(3) }
    @Test fun level04() { World3DesignTest.play(4) }
    @Test fun level05() { World3DesignTest.play(5) }
    @Test fun level06() { World3DesignTest.play(6) }
    @Test fun level07() { World3DesignTest.play(7) }
    @Test fun level08() { World3DesignTest.play(8) }
    @Test fun level09() { World3DesignTest.play(9) }
    @Test fun level10() { World3DesignTest.play(10) }
    @Test fun level11() { World3DesignTest.play(11) }
    @Test fun level12() { World3DesignTest.play(12) }
    @Test fun level13() { World3DesignTest.play(13) }
    @Test fun level14() { World3DesignTest.play(14) }
    @Test fun level15() { World3DesignTest.play(15) }
    @Test fun level16() { World3DesignTest.play(16) }

    // ---------- Act 2: Überhitzung ----------
    // 17-24 are the rebuilt block C (World3RoomsC), 25-32 the rebuilt block D (World3RoomsD): the registered bot solutions
    @Test fun level17() { World3DesignTest.play(17) }
    @Test fun level18() { World3DesignTest.play(18) }
    @Test fun level19() { World3DesignTest.play(19) }
    @Test fun level20() { World3DesignTest.play(20) }
    @Test fun level21() { World3DesignTest.play(21) }
    @Test fun level22() { World3DesignTest.play(22) }
    @Test fun level23() { World3DesignTest.play(23) }
    @Test fun level24() { World3DesignTest.play(24) }
    @Test fun level25() { World3DesignTest.play(25) }
    @Test fun level26() { World3DesignTest.play(26) }
    @Test fun level27() { World3DesignTest.play(27) }
    @Test fun level28() { World3DesignTest.play(28) }
    @Test fun level29() { World3DesignTest.play(29) }
    @Test fun level30() { World3DesignTest.play(30) }
    @Test fun level31() { World3DesignTest.play(31) }
    @Test fun level32() { World3DesignTest.play(32) }

    // ---------- Act 3: Lüfter ----------
    @Test fun level33() { World3DesignTest.play(33) }
    @Test fun level34() { World3DesignTest.play(34) }
    @Test fun level35() { World3DesignTest.play(35) }
    @Test fun level36() { World3DesignTest.play(36) }
    @Test fun level37() { World3DesignTest.play(37) }
    @Test fun level38() { World3DesignTest.play(38) }
    @Test fun level39() { World3DesignTest.play(39) }
    @Test fun level40() { World3DesignTest.play(40) }
    @Test fun level41() { World3DesignTest.play(41) }
    @Test fun level42() { World3DesignTest.play(42) }
    @Test fun level43() { World3DesignTest.play(43) }
    @Test fun level44() { World3DesignTest.play(44) }
    @Test fun level45() { World3DesignTest.play(45) }
    @Test fun level46() { World3DesignTest.play(46) }
    @Test fun level47() { World3DesignTest.play(47) }
    @Test fun level48() { World3DesignTest.play(48) }
}
