package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** One scripted solution per level of World 2 (48 levels, three acts of 16), played with the real physics. */
class World2Test {
    private fun b(n: Int) = Bot(World2.levels[n - 1])

    private fun actions(l: Level) = l.start + l.traps.flatMap { it.actions }

    private fun net(a: Action) = a is Action.Belt || a is Action.Laser || a is Action.Portal || a is Action.Reroute || a is Action.Power
    private fun meta(a: Action) =
        a is Action.FakeWin || a is Action.PauseTrap || a is Action.FrameCrack || a is Action.Flip || a is Action.Roll || a is Action.Ghost

    // ---------- structure ----------

    @Test
    fun worldHas48LevelsInThreeActsThatParse() {
        assertEquals(48, World2.levels.size)
        assertEquals(16, World2Part1.levels.size)
        assertEquals(16, World2Part2.levels.size)
        assertEquals(16, World2Part3.levels.size)
        World2.levels.forEach { World(it) }
    }

    @Test
    fun standingStillIsSafeForTwoSeconds() {
        // the keep-alive level punishes idling on purpose (by dropping the floor further on, not under the start)
        World2.levels.filter { l -> l.traps.none { it.trigger is Trigger.Idle } }.forEach { l -> Bot(l).wait(2.2f).expect(WorldState.PLAYING) }
        b(26).wait(2.2f).expect(WorldState.PLAYING)
    }

    @Test
    fun namesAreUniqueAndShortEnoughForTheHud() {
        val names = World2.levels.map { it.name.en }
        assertEquals(names.size, names.toSet().size)
        assertEquals(names.size, World2.levels.map { it.name.de }.toSet().size)
        World2.levels.forEachIndexed { i, l ->
            assertTrue("level ${i + 1} name too long", l.name.en.length <= 26 && l.name.de.length <= 26)
        }
    }

    @Test
    fun namesAndIntrosAreFilledInBothLanguages() {
        World2.levels.forEach { l ->
            assertTrue(l.name.en.isNotBlank() && l.name.de.isNotBlank() && l.intro.en.isNotBlank() && l.intro.de.isNotBlank())
            assertTrue("${l.name.en}: intro too long", l.intro.en.length <= 66 && l.intro.de.length <= 66)
        }
    }

    /** Every round of a trap level plays exactly one card (a bluff counts). */
    @Test
    fun everyTrapLevelPlaysExactlyOneCard() {
        World2.levels.forEachIndexed { i, l ->
            l.rounds.forEachIndexed { r, round ->
                if (round.traps.isNotEmpty()) {
                    val plays = round.traps.sumOf { t -> t.actions.count { it is Action.Play || it is Action.Bluff } }
                    assertEquals("level ${i + 1} round ${r + 1} should play exactly one card", 1, plays)
                }
            }
        }
    }

    /** The obvious thing to do, running right and never letting go, must not win any level. */
    @Test
    fun holdingRightAloneWinsNothing() {
        val winners = World2.levels.withIndex().filter { (_, l) -> Bot(l).right(14f).world.state == WorldState.WON }.map { it.index + 1 }
        assertTrue("holding right wins levels $winners", winners.isEmpty())
    }

    @Test
    fun actOneBringsPortalsEarlyAndTheDnsTrap() {
        assertTrue(actions(World2.levels[1]).any { it is Action.Portal })
        val portalLevels = World2Part1.levels.filter { l -> actions(l).any { it is Action.Portal } }
        assertTrue(portalLevels.size >= 4)
        assertTrue(World2Part1.levels.any { l -> actions(l).any { it is Action.Reroute } })
        // the act does not touch belts or lasers yet
        assertTrue(World2Part1.levels.none { l -> actions(l).any { it is Action.Belt || it is Action.Laser } })
    }

    @Test
    fun actTwoIsBuiltOnBeltsAndLasers() {
        val tagged = World2Part2.levels.withIndex().filter { (_, l) -> actions(l).any { it is Action.Belt || it is Action.Laser || it is Action.Portal } }.map { it.index + 17 }
        assertTrue("only $tagged use a network mechanic", tagged.size >= 10)
        // each new mechanic shows up alone before it is mixed in (the traps around it are classics and its own tricks)
        assertTrue(actions(World2.levels[16]).filter(::net).all { it is Action.Belt })
        assertTrue(actions(World2.levels[17]).filter(::net).all { it is Action.Laser || it is Action.Power })
        assertTrue(World2Part2.levels.any { l -> actions(l).any { it is Action.Belt } && actions(l).any { it is Action.Portal } })
        assertTrue(World2Part2.levels.any { l -> actions(l).any { it is Action.Laser } && actions(l).any { it is Action.Portal } })
        assertTrue(World2Part2.levels.any { l -> actions(l).any { it is Action.Power } || l.traps.any { t -> t.actions.any { it is Action.Laser } } })
        // the classics show up in act 2 as well, with a network spin
        val cards = World2Part2.levels.flatMap { l -> actions(l).filterIsInstance<Action.Play>().map { it.card } }.toSet()
        assertTrue(Card.HEADBUTT in cards && Card.DEVIL_SAW in cards && Card.CRUMBLE in cards)
    }

    @Test
    fun metaTwistsOnlyInActThreeAndOnlyThreeKinds() {
        assertTrue((World2Part1.levels + World2Part2.levels).none { l -> actions(l).any(::meta) })
        val kinds = World2Part3.levels.flatMap { l -> actions(l).filter(::meta).map { it::class.simpleName!! } }.toSet()
        assertEquals(setOf("Ghost", "PauseTrap", "Roll"), kinds)
        // none of the twists World 1 leads with (fake clear, fake credits, frame crack, flip)
        assertTrue(World2.levels.none { l -> actions(l).any { it is Action.FakeWin || it is Action.FrameCrack || it is Action.Flip } })
    }

    @Test
    fun atMostOneLevelUsesPhoneMotion() {
        val motion = World2.levels.withIndex().filter { (_, l) -> l.usesMotion }.map { it.index + 1 }
        assertEquals(listOf(43), motion)
        assertTrue(World2.levels[42].usesShake)
    }

    @Test
    fun finaleCombinesTheNetworkMechanics() {
        val a = actions(World2.levels[47])
        assertTrue(a.any { it is Action.Portal } && a.any { it is Action.Belt } && a.any { it is Action.Laser })
        assertTrue(a.any { it is Action.Swap } && a.any { it is Action.DoorTo })
        assertEquals("shutdown -h now", World2.levels[47].name.en)
        // the last door ends up below the floor: down to layer 3
        val w = World(World2.levels[47])
        val door = a.filterIsInstance<Action.DoorTo>().last()
        assertEquals(16, door.row)
        assertNotNull(w.door)
    }

    @Test
    fun everyLevelOfActThreeAndTwoIsDifferentFromItsNeighbours() {
        // no two consecutive levels share a card and a network mechanic set
        World2.levels.zipWithNext().forEachIndexed { i, (x, y) ->
            val cx = actions(x).filterIsInstance<Action.Play>().map { it.card }
            val cy = actions(y).filterIsInstance<Action.Play>().map { it.card }
            val nx = actions(x).filter(::net).map { it::class }.toSet()
            val ny = actions(y).filter(::net).map { it::class }.toSet()
            assertFalse("levels ${i + 1} and ${i + 2} look the same (${x.name.en} / ${y.name.en})", cx == cy && nx == ny && cx.isNotEmpty())
        }
    }

    // ---------- trap chains ----------

    /** Traps that spring on the same trigger (a door flying in several hops, blocks falling one after the other) count once. */
    private fun chain(l: Level) = l.traps.map { it.trigger }.distinct().size

    @Test
    fun everyLevelChainsAtLeastTwoTrapsAndTheWorldAveragesTwoAndAHalf() {
        World2.levels.forEachIndexed { i, l -> assertTrue("level ${i + 1} (${l.name.en}) has only ${chain(l)} traps", chain(l) >= 2) }
        val avg = World2.levels.map(::chain).average()
        assertTrue("average chain length $avg", avg >= 2.5)
        // the act averages, as a guard against one act carrying the others
        for (act in listOf(World2Part1.levels, World2Part2.levels, World2Part3.levels)) assertTrue(act.map(::chain).average() >= 2.3)
    }

    @Test
    fun metaTwistLevelsKeepShortChains() {
        // the shake level stays at two traps; the rebuilt rooms of the ghost, the pause and the lag roll (36, 39, 40) carry the meta trick
        // beside two to four real traps (docs/LEVEL_DESIGN_V2.md H4), so their chains are as long as the density rules ask
        for (n in listOf(43)) assertEquals("level $n", 2, chain(World2.levels[n - 1]))
        for (n in listOf(36, 39, 40)) assertTrue("level $n", chain(World2.levels[n - 1]) in 2..6)
    }

    /** The counter just learned (hop the obstacle) followed by the old reflex (keep running) is what the next trap of a chain waits for. */
    @Test
    fun theObviousRunDiesInTheChain() {
        // 1: the landing after the first pit is a pit too
        b(1).hopR(10.2f).right(2f).expect(WorldState.DEAD)
        // 2: the hop over the loopback grows a spike in the lane behind it
        b(2).hopR(15.5f).right(3f).expect(WorldState.DEAD)
        // 3: running on, down the hole and into the lower floor, the saw that rolls at you finds you
        b(3).hopR(7.2f).hopR(12.8f).hopR(17.2f).right(6f).expect(WorldState.DEAD)
        // 4: the bulb over the upper lane drops as you come near
        b(4).rightTo(3.0f).rightJump(0.4f).landRight().rightJump(0.4f).landRight().rightJump(0.4f).landRight().right(2f).expect(WorldState.DEAD)
        // 17: running along the roof of the duct runs under the loose piece of the shelf
        b(17).rightTo(1.9f).rightJump(0.15f).landRight().wait(0.1f).rightJump(0.4f).landRight().right(2f).expect(WorldState.DEAD)
        // 18: lingering on the rack after pressing the pad runs into the port scan
        World2Rooms.l18ToPad(b(18)).wait(2.5f).expect(WorldState.DEAD)
        // 20: running on right after the ID scan drops you into the queue at the exit gate
        World2Rooms.l20ToScanner(b(20)).right(3f).expect(WorldState.DEAD)
        // 28: the tunnel drops you in front of the first gate, and running on runs into it
        b(28).right(3f).expect(WorldState.DEAD)
        // 29: the first pendulum saw sits on the floor for a moment, and running on runs into it
        b(29).right(3f).expect(WorldState.DEAD)
        // 31: running on falls into the trench of spikes before the first stone is up
        b(31).right(3f).expect(WorldState.DEAD)
        // 32: running on along the belt runs into the LED
        b(32).right(3f).expect(WorldState.DEAD)
    }

    @Test
    fun theBusTurnsAroundAtThePadAndTheScanWarmsUpBeforeItFires() {
        assertEquals(-9f, b(17).world.group('b').belt)
        val shelf = World2Rooms.l17(b(17))
        assertEquals(5f, shelf.world.group('b').belt)
        // the port scan glows before it fires: there is time to see it coming from the rack
        val glow = World2Rooms.l18ToPad(b(18)).wait(0.3f).world.beams.first { it.laser.id == 'K' }
        assertTrue(glow.warn > 0f && !glow.lit)
    }

    /** A chain may end you, but never leaves you alive where the door cannot be reached. */
    @Test
    fun noChainStrandsYouAliveAwayFromTheDoor() {
        // 3: the hop over the LEDs is long enough to land clear of them
        b(3).hopR(7.2f).hopR(12.8f).hopR(17.2f).expect(WorldState.PLAYING)
        // every portal and every re-pointed exit that a trap creates opens onto the ground (or into spikes), never into a closed room
        World2.levels.forEachIndexed { i, l ->
            val exits = l.traps.flatMap { it.actions }.mapNotNull { a ->
                when (a) { is Action.Portal -> a.to; is Action.Reroute -> a.to; else -> null }
            }
            for ((x, y) in exits) {
                val below = l.map.grid[y + 1][x]
                assertTrue("level ${i + 1}: exit ($x,$y) floats over nothing", below != '.')
            }
        }
        // a trap-made portal never sits where the player could be locked into a dead end: its tiles are free in the plain map
        World2.levels.forEach { World(it) }
        // 31: whoever dawdles on the deck is run over by the class wall
        b(31).rightTo(7.5f).wait(6f).expect(WorldState.DEAD)
    }

    // ---------- the levels that react to the player in unusual ways ----------

    @Test
    fun theDnsEntryAtTheStartLeadsHomeAndTheLastBlockOfTheStairRepointsAndRestartsIt() {
        // the portal next to the start sends you back to the start
        val loop = b(6).right(0.5f).wait(0.2f)
        loop.expect(WorldState.PLAYING)
        assertTrue("x=${loop.world.player.box.cx}", loop.world.player.box.cx < 13.6f && loop.world.links[0].to == 12 to 14)
        // the block at the far end of the stair re-points it and takes it down for a moment
        val touched = b(6).leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft()
            .leftUntil { it.player.box.cx < 2.5f }.wait(0.2f)
        assertEquals(18 to 14, touched.world.links[0].to)
        assertFalse(touched.world.links[0].on)
    }

    @Test
    fun theFloorBeneathBobbyTablesIsAWormhole() {
        // falling into the hole is the way on; jumping over it leaves you in front of the LED field
        b(38).hopR(5.9f).right(1f).expect(WorldState.DEAD)
        val fell = b(38).right(0.6f).wait(1.4f)
        assertTrue("x=${fell.world.player.box.cx}", fell.world.player.box.cx in 23f..26f)
        fell.expect(WorldState.PLAYING)
    }

    @Test
    fun theFloorLinkAtHomeIsTheLoopback() {
        // 14: walking into the obvious link on the floor drops you back at home, alive
        val loop = b(14).rightTo(12.9f).right(0.2f).wait(0.2f)
        loop.expect(WorldState.PLAYING)
        assertTrue("x=${loop.world.player.box.cx}", loop.world.links[0].hopTime > 0f && loop.world.player.box.cx < 6f)
    }

    @Test
    fun theFirewallOnlyGoesDownWhenYouPauseAndResume() {
        val bot = b(39).wait(0.5f).tapPause()
        assertEquals(1, bot.world.dodges)
        b(39).rightTo(13f).wait(0.5f).right(2f).expect(WorldState.DEAD)
        assertFalse(b(39).wait(0.5f).pauseResume().wait(0.05f).world.beams[0].on)
    }

    @Test
    fun theQueueCatchesWhoStandsStillOnTheMiddleDeck() {
        b(26).leftTo(23.6f).leftJump(0.35f).landLeft().leftUntil { it.player.box.b > 8f }.wait(6f).expect(WorldState.DEAD)
    }

    @Test
    fun replayAttackPunishesStandingStillWhileYourLastRunComes() {
        // first attempt: run right until the first saw gets you; the log keeps the run
        val first = b(36).right(3f).also { it.expect(WorldState.DEAD) }
        // second attempt: stand in front of the saw's wake and wait: the replay of the last attempt starts at the spawn and walks into you
        val second = first.retry().right(0.9f).wait(2.5f)
        second.expect(WorldState.DEAD)
        assertEquals(Card.DEVIL_SAW, second.world.lastCard)
        // without a previous attempt nothing replays: standing there for as long is fine
        b(36).right(0.9f).wait(2.5f).expect(WorldState.PLAYING)
    }

    @Test
    fun theBeltCarriesYouButOnlyOnTheGround() {
        // the bus in the duct runs against you, faster than you run: it pushes you back out
        val bot = b(17).rightTo(1.9f).rightJump(0.15f).landRight().right(2f)
        bot.expect(WorldState.PLAYING)
        assertTrue("x=${bot.world.player.box.cx}", bot.world.player.box.cx < 7.6f)
    }

    // ---------- Act 1: Handshake ----------
    @Test fun level01() { World2DesignTest.play(1) }
    /** Hello, World!: hopping the first pit and running on lands in the pit that opens as you touch down. */
    @Test fun level01TheLandingIsAPitToo() = b(1).hopR(10.2f).right(2f).expect(WorldState.DEAD)
    @Test fun level02() { World2DesignTest.play(2) }
    /** Open Port: the floor portal in the middle is the loopback; the hop over it grows a spike where you run on. */
    @Test fun level02TheLoopbackSendsYouHome() = b(2).rightTo(16.9f).right(0.15f).wait(0.2f).also { assertTrue("x=${it.world.player.box.cx}", it.world.player.box.cx < 8f) }.expect(WorldState.PLAYING)
    @Test fun level03() { World2DesignTest.play(3) }
    /** Reception: hopping the hole in the upper floor, like the door's old neighbour, leaves you holding right at the wall of an upper floor with no door. */
    @Test fun level03HoppingTheHoleIsTheWrongWay() = b(3).hopR(7.2f).hopR(12.8f).hopR(17.2f).hopR(24.6f).right(3f).expect(WorldState.PLAYING)
    @Test fun level04() { World2DesignTest.play(4) }
    /** String Lights: the bulb over the lane drops when you come near, so dashing under it is the end. */
    @Test fun level04DashingUnderTheBulbIsFatal() = b(4).rightTo(3.0f).rightJump(0.4f).landRight().rightJump(0.4f).landRight().rightJump(0.4f).landRight()
        .right(2f).expect(WorldState.DEAD)
    @Test fun level05() { World2DesignTest.play(5) }
    /** Null Pointer: the saw on the top floor rolls at you; running into it is the end. */
    @Test fun level05RunningIntoTheFirstSawIsFatal() = b(5).right(4f).expect(WorldState.DEAD)
    @Test fun level06() { World2DesignTest.play(6) }
    @Test fun level07() { World2DesignTest.play(7) }
    @Test fun level08() { World2DesignTest.play(8) }
    @Test fun level08RunningOnIntoTheUnpluggedBlockIsFatal() = b(8).right(4f).expect(WorldState.DEAD)
    @Test fun level09() { World2DesignTest.play(9) }
    @Test fun level09HoldingRightWalksIntoTheWallAndTheSawFindsYou() = b(9).right(6f).expect(WorldState.DEAD)
    @Test fun level10() { World2DesignTest.play(10) }
    @Test fun level10RunningStraightAtTheWallFindsTheFirstHole() = b(10).left(3f).expect(WorldState.DEAD)
    @Test fun level11() { World2DesignTest.play(11) }
    @Test fun level12() { World2DesignTest.play(12) }
    @Test fun level13() { World2DesignTest.play(13) }
    @Test fun level14() { World2DesignTest.play(14) }
    @Test fun level15() { World2DesignTest.play(15) }
    @Test fun level16() { World2DesignTest.play(16) }

    // ---------- Act 1, levels 11-16: rebuilt after recipe v2 (the rules are checked by World2DesignTest, rounds by World2DeckTest) ----------

    /** Running right and hopping now and then gets nowhere in the rebuilt rooms. */
    @Test
    fun theRoomsFallToNoRunner() {
        for (n in 11..16) {
            val runner = b(n)
            repeat(40) { runner.right(0.3f).rightJump(0.25f) }
            assertTrue("level $n falls to running right and hopping", runner.world.state != WorldState.WON)
        }
    }

    @Test
    fun theRoomsHardlyGrowHiddenSpikes() {
        // at most one "spikes where you land" per room, and most rooms have none at all
        val shows = (11..16).associateWith { n ->
            World2.levels[n - 1].rounds.sumOf { r ->
                r.traps.sumOf { t -> t.actions.count { a -> a is Action.Show && r.glyph(a.group)?.spike == true } }
            }
        }
        assertTrue("$shows", shows.values.all { it <= 1 } && shows.values.count { it == 0 } >= 3)
    }

    /** Address Space: each page you step on is freed, standing on one is a fall. */
    @Test fun level12StandingOnAPageIsUseAfterFree() = b(12).rightTo(8f).wait(1.5f).expect(WorldState.DEAD)
    /** Greeting: the right pit is too wide until the client says SYN. */
    @Test fun level15WithoutSynThePitWins() = b(15).right(3f).expect(WorldState.DEAD)
    /** Through Traffic: stepping into the cell encrypts the connection. */
    @Test fun level16TheCellIsEncrypted() {
        val bot = b(16).hopR(5.6f).right(0.85f).waitFor { it.swapped }
        assertTrue(bot.world.swapped)
    }

    // ---------- Act 2: Traffic ----------
    @Test fun level17() = World2Rooms.l17(b(17)).expect(WorldState.WON)
    @Test fun level18() = World2Rooms.l18(b(18)).expect(WorldState.WON)
    @Test fun level19() = World2Rooms.l19(b(19)).expect(WorldState.WON)
    @Test fun level20() = World2Rooms.l20(b(20)).expect(WorldState.WON)
    @Test fun level21() = World2Rooms.l21(b(21)).expect(WorldState.WON)
    @Test fun level22() = World2Rooms.l22(b(22)).expect(WorldState.WON)
    @Test fun level23() = World2Rooms.l23(b(23)).expect(WorldState.WON)
    @Test fun level24() = World2Rooms.l24(b(24)).expect(WorldState.WON)
    @Test fun level25() { World2DesignTest.play(25) }
    @Test fun level25WalkingAcrossTheNodesIsFatal() = b(25).right(4f).expect(WorldState.DEAD)
    @Test fun level26() { World2DesignTest.play(26) }
    @Test fun level26TheQueueFollowsAndTheNodeIsAnObstacle() = b(26).leftTo(23.6f).leftJump(0.35f).landLeft().leftUntil { it.player.box.b > 8f }.rightUntil { it.player.box.cx > 19.6f }.right(4f).expect(WorldState.DEAD)
    @Test fun level27() { World2DesignTest.play(27) }
    @Test fun level27RunningStraightOnMeetsTheFirstPacket() = b(27).right(3f).expect(WorldState.DEAD)
    @Test fun level28() { World2DesignTest.play(28) }
    @Test fun level29() { World2DesignTest.play(29) }
    @Test fun level30() { World2DesignTest.play(30) }
    @Test fun level30TheLedgeHoleSwallowsWhoRunsOn() = b(30).rightUntil { it.player.box.b < 9.5f }.right(3f).expect(WorldState.DEAD)
    @Test fun level30TheBreachMovesTheDoorIntoTheSecondRoom() {
        val bot = b(30).rightUntil { it.player.box.b < 9.5f }.hopR(17.4f).rightUntil(4f) { it.cracks.isNotEmpty() }.rightUntil(3f) { it.cracks.any { c -> c.fell } }
        assertEquals(2, bot.world.level.rooms)
        assertTrue(bot.world.door.tx > 32f)
    }
    @Test fun level31() { World2DesignTest.play(31) }
    @Test fun level32() { World2DesignTest.play(32) }

    // ---------- Act 2, levels 17-24: one-screen puzzle rooms ----------

    /** Run right and jump every [period] seconds, for 14 seconds: the reflex, in a few rhythms. */
    private fun hammer(n: Int, period: Float) = b(n).also { bot -> repeat((14f / (period + 0.35f)).toInt() + 1) { bot.right(period).rightJump(0.35f) } }

    @Test
    fun puzzleRoomsTakeTimeAndHoldingRightWithJumpsWinsNone() {
        for ((n, solve) in World2Rooms.solutions) {
            val run = solve(b(n))
            run.expect(WorldState.WON)
            assertTrue("level $n: clean run only ${run.world.time} s", run.world.time >= 6f)
            for (period in listOf(0.15f, 0.4f, 0.8f, 1.3f)) {
                assertTrue("level $n: running right, jumping every $period s, wins", hammer(n, period).world.state != WorldState.WON)
            }
        }
    }

    @Test
    fun puzzleRoomsUseFewSproutingSpikesAndOneCardEach() {
        val rooms = (17..24).map { World2.levels[it - 1] }
        val shows = rooms.map { l -> l.rounds.sumOf { r -> r.traps.sumOf { t -> t.actions.count { it is Action.Show } } } }
        assertTrue("Show per room: $shows", shows.all { it <= 1 } && shows.count { it == 0 } >= 4)
        // no two neighbours lead with the same card
        val cards = rooms.map { l -> l.traps.flatMap { it.actions }.filterIsInstance<Action.Play>().single().card }
        cards.zipWithNext().forEach { (x, y) -> assertTrue("$cards", x != y) }
    }

    // 17: the duct's ceiling has spikes, the bus pushes you out, the loose shelf piece drops on runners
    @Test fun l17HoppingInTheDuctHitsItsCeiling() = b(17).rightTo(1.9f).rightJump(0.15f).landRight().right(0.6f).rightJump(0.3f).wait(0.5f).expect(WorldState.DEAD)
    @Test fun l17RunningUnderTheLooseShelfPieceIsFatal() = b(17).rightTo(1.9f).rightJump(0.15f).landRight().wait(0.1f).rightJump(0.4f).landRight().right(2f).expect(WorldState.DEAD)

    // 18: the hole in the floor, the gate that counts to one, the scan on the rack, the beam over the stairs coming back
    @Test fun l18RunningStraightPastTheHoleFalls() = b(18).right(4f).expect(WorldState.DEAD)
    @Test fun l18LingeringOnTheRackAfterThePadIsFatal() = World2Rooms.l18ToPad(b(18)).wait(2.5f).expect(WorldState.DEAD)
    @Test fun l18PausingOnTheMiddleStepIsFatal() = World2Rooms.l18ToPad(b(18)).leftTo(17.0f).leftJump(0.35f).landLeft()
        .leftTo(13.6f).leftJump(0.4f).landLeft().wait(2f).expect(WorldState.DEAD)
    @Test fun l18ThePadPutsTheFloorBack() {
        val bot = World2Rooms.l18ToPad(b(18)).leftTo(24f)
        assertEquals(0f, bot.world.group('a').oy, 0.01f)
    }

    // 19: the stairs swap the controls, the top floor restores them, and a wall drives toward the runner
    @Test fun l19TheStairsSwapTheControls() = assertTrue(b(19).hopR(6.8f).wait(0.1f).world.swapped)
    @Test fun l19TheTopFloorRestoresTheControls() = assertTrue(!World2Rooms.l19ToShelf(b(19)).wait(0.2f).world.swapped)
    @Test fun l19RunningStraightAlongTheTopFloorIsPushedBackByTheWall() {
        val bot = World2Rooms.l19ToShelf(b(19)).leftTo(25.6f).leftJump(0.35f).landLeft().left(3f)
        assertTrue("x=${bot.world.player.box.cx}", bot.world.state != WorldState.WON && bot.world.player.box.cx > 14f)
    }
    @Test fun l19WaitingOnTheStairsLetsTheWallParkOnTheLanding() {
        val bot = World2Rooms.l19ToShelf(b(19)).wait(3f)
        assertTrue("wall at ${World2Rooms.wallX(bot.world, 'w')}", World2Rooms.wallX(bot.world, 'w') in 19.5f..21.5f)
    }

    // 20: gate 2 flashes, the second check closes gate 3, the stairs and the floor go dark behind you, the queue waits at the exit
    @Test fun l20RunningStraightAtTheFirstGateIsFatal() = b(20).right(3f).expect(WorldState.DEAD)
    @Test fun l20RunningOffTheLedgeAfterTheScanIsFatal() = World2Rooms.l20ToScanner(b(20)).right(3f).expect(WorldState.DEAD)
    @Test fun l20TheStairsGoDarkBehindYou() {
        assertTrue(b(20).rightTo(3.6f).wait(0.2f).world.circuits['w']?.powered == true)
        assertTrue(World2Rooms.l20ToScanner(b(20)).wait(0.3f).world.circuits['w']?.powered == false)
    }
    @Test fun l20TheFloorBehindYouGoesDarkOnTheFirstStep() {
        assertTrue(b(20).rightTo(3.6f).wait(0.2f).world.circuits['x']?.powered == true)
        assertTrue(World2Rooms.l20ToScanner(b(20)).world.circuits['x']?.powered == false)
    }
    @Test fun l20TheHopperWhoSkipsTheSecondCheckMeetsGateThree() = b(20).rightJump(0.35f).landRight().right(8f).expect(WorldState.DEAD)
    @Test fun l20TheFirstCheckPowersTheStairs() {
        assertTrue(b(20).world.circuits['w']?.powered == false)
        assertTrue(b(20).rightTo(6.3f).wait(0.1f).world.circuits['w']?.powered == true)
    }

    // 21: the far portal is dead until the closet (the near portal) opens the port, the treadmill drags you toward the LED, the portal next to the door is a captive portal
    @Test fun l21TheNearPortalIsTheControlRoom() {
        val bot = World2Rooms.l21ToCloset(b(21)).wait(0.5f)
        assertTrue("x=${bot.world.player.box.cx}", bot.world.player.box.cx in 14f..17f)
        assertTrue("the port opens in the closet", bot.world.links[1].on)
        bot.right(3f).expect(WorldState.PLAYING)
    }
    @Test fun l21TheFarPortalIsDeadAtFirst() {
        val bot = b(21).hopR(3.6f).rightTo(10.5f).wait(0.5f)
        assertTrue("x=${bot.world.player.box.cx} y=${bot.world.player.box.cy}", bot.world.player.box.cy > 13f && !bot.world.links[1].on)
    }
    @Test fun l21TheRouteNeedsBothPortals() {
        val run = World2Rooms.l21(b(21))
        run.expect(WorldState.WON)
        assertTrue("the solution goes through the near portal", run.world.links[0].hopTime > 0f || run.world.sprung.any { it.trap.actions.any { a -> a is Action.Power } })
    }
    @Test fun l21TheCaptivePortalDoesNotFireAtTheStart() {
        val bot = b(21).right(1.5f).wait(0.5f)
        assertTrue(bot.world.sprung.none { s -> s.trap.actions.any { it is Action.Reroute && it.id == 'r' } })
    }
    @Test fun l21StandingOnTheTreadmillIsFatal() = World2Rooms.l21Up(b(21)).wait(4f).expect(WorldState.DEAD)
    @Test fun l21JumpingUnderTheSpikedCeilingIsFatal() = World2Rooms.l21ToShelf(b(21)).jump(0.3f).wait(0.5f).expect(WorldState.DEAD)
    @Test fun l21WalkingIntoTheCaptivePortalSendsYouBackUp() {
        val bot = World2Rooms.l21ToShelf(b(21)).rightUntil { it.player.box.b > 8.5f }.waitFor { it.player.grounded }.rightTo(25.9f).wait(0.3f)
        assertTrue("x=${bot.world.player.box.cx} y=${bot.world.player.box.b}", bot.world.player.box.cx < 5f && bot.world.player.box.b < 9f)
    }

    // 22: the first bouncer rolls at you on the top floor, the carpet drops out over LEDs; the second bouncer rolls out of the back door, and the floor in front of the door drops
    @Test fun l22RunningStraightIntoTheFirstBouncerIsFatal() = b(22).hopR(15.0f).right(4f).expect(WorldState.DEAD)
    @Test fun l22StandingStillWhereTheFirstBouncerComesFromIsFatal() = b(22).rightTo(10f).wait(6f).expect(WorldState.DEAD)
    @Test fun l22RunningOverTheCarpetThatDropsLandsOnTheLeds() = b(22).rightTo(14.5f).right(3f).expect(WorldState.DEAD)
    @Test fun l22StandingStillWhereTheSecondBouncerComesFromIsFatal() = World2Rooms.l22ToLane(b(22)).wait(8f).expect(WorldState.DEAD)
    @Test fun l22RunningStraightIntoTheSecondBouncerIsFatal() = World2Rooms.l22ToLane(b(22)).hopL(22.6f).left(4f).expect(WorldState.DEAD)

    // 23: the on-ramp lift carries you up into the spiked ceiling unless you walk off at the deck, a piece of the deck drops, the exit lift drops away
    @Test fun l23StandingOnTheOnRampLiftEndsInTheCeiling() = b(23).rightTo(8.6f).wait(3f).expect(WorldState.DEAD)
    @Test fun l23RunningStraightOnDropsIntoTheRoadworks() = b(23).right(4f).expect(WorldState.DEAD)
    @Test fun l23StandingOnTheExitLiftIsFatal() = b(23).rightUntil { it.player.box.cx > 11f }.hopR(15.8f).rightTo(27.2f).wait(2.5f).expect(WorldState.DEAD)

    // 24: stalactites fall where you run, the deck comes down on the lane, the carpet in front of the door is nothing
    @Test fun l24RunningUnderTheStalactitesIsFatal() = b(24).right(3f).expect(WorldState.DEAD)
    @Test fun l24StandingUnderTheDeckIsFatal() = b(24).rightTo(16.9f).waitFor { it.group('V').oy > 6f }.rightUntil { it.player.box.cx > 28f }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(22f).wait(3f).expect(WorldState.DEAD)
    @Test fun l24RunningStraightIntoTheSawFromTheBackWallIsFatal() = b(24).rightTo(16.9f).waitFor { it.group('V').oy > 6f }.rightUntil { it.player.box.cx > 28f }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.left(5f).expect(WorldState.DEAD)

    // ---------- act 2, levels 17-24: what the reviews asked for ----------

    /** The firewall gates that flash and make you wait appear in two rooms at most (18 and 20). */
    @Test
    fun gateWaitsAreRareInActTwo() {
        val levels = (17..24).filter { n -> World2.levels[n - 1].rounds.any { r -> (r.start + r.traps.flatMap { it.actions }).any { it is Action.Laser && it.off > 0f } } }
        assertTrue("timed or one-shot beams in $levels", levels.size <= 2)
    }

    /** The ceiling packet (fall, wait, hop) in two rooms at most. */
    @Test
    fun ceilingPacketsAreRareInActTwo() {
        val levels = (17..24).filter { n -> Card.HEADBUTT in DesignRules.cards(World2.levels[n - 1]) }
        assertTrue("falling packets in $levels", levels.size <= 2)
    }

    /** Mephi does not repeat himself in the rooms 17-24 (a line, in either language, is said once). */
    @Test
    fun mephisLinesOfActTwoRoomsAreDistinct() {
        val lines = (17..24).flatMap { n -> World2.levels[n - 1].rounds.flatMap { r -> r.traps.flatMap { it.actions }.filterIsInstance<Action.Say>().map { n to it.text } } }
        val en = lines.groupBy { it.second.en }.filterValues { it.size > 1 }
        val de = lines.groupBy { it.second.de }.filterValues { it.size > 1 }
        assertTrue("repeated lines: ${en.keys + de.keys}", en.isEmpty() && de.isEmpty())
    }

    // ---------- Act 3: Root ----------
    @Test fun level33() { World2DesignTest.play(33) }
    /** sudo !!: the hole opens in the lane as you pass, and holding right runs into it. */
    @Test fun level33RunningOnAlongTheLaneFindsTheHole() = b(33).right(3f).expect(WorldState.DEAD)
    /** sudo !!: stopping where the block falls is right, but running on under it is the end. */
    @Test fun level33RunningOnUnderTheDeckBlockIsFatal() = b(33).hopR(6.9f, 0.5f).hopR(12.8f, 0.5f).rightTo(18.3f).rightJump(0.5f).landRight().rightJump(0.5f).landRight()
        .hopL(23.2f, 0.5f).left(3f).expect(WorldState.DEAD)
    @Test fun level34() { World2DesignTest.play(34) }
    /** Reverse Proxy: the wall of links at the right end of the ceiling is the loopback, it hands you back at the start of the ceiling. */
    @Test fun level34TheObviousLinksLoopYouBack() {
        val bot = b(34).rightTo(14f).rightUntil { it.player.box.cy < 3f }.rightUntil { it.player.box.cx > 24.5f }.wait(0.3f)
        bot.expect(WorldState.PLAYING)
        assertTrue("x=${bot.world.player.box.cx}", bot.world.player.box.cx < 17f)
    }
    /** Reverse Proxy: the dark link in the top left is dead until you pass the middle of the ceiling. */
    @Test fun level34TheDarkLinkIsDeadAtFirst() = assertFalse(b(34).rightTo(14f).rightUntil { it.player.box.cy < 3f }.world.links.first { it.id == '3' }.on)
    @Test fun level35() { World2DesignTest.play(35) }
    /** Pipeline: standing still on the belt in the duct is carried out of it, and the floor behind you has opened: you end up on the lane again. */
    @Test fun level35StandingInTheDuctIsCarriedOutAndDropsToTheLane() {
        val bot = b(35).hopL(24.0f, 0.5f).hopL(18.0f, 0.5f).leftTo(12.7f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().hopR(7.8f, 0.5f)
            .rightUntil { it.player.box.cx > 15f }.wait(3f)
        assertTrue("y=${bot.world.player.box.b}", bot.world.player.box.b > 12f)
    }
    /** Pipeline: running straight on along the lane falls into the first hole. */
    @Test fun level35RunningStraightOnAlongTheLaneFindsTheHole() = b(35).left(3f).expect(WorldState.DEAD)
    @Test fun level36() { World2DesignTest.play(36) }
    @Test fun level37() = b(37).rightJump(0.40f).rightJump(0.40f).rightJump(0.40f).right(1.20f).right(0.25f).rightJump(0.25f) .right(0.60f).expect(WorldState.WON)
    @Test fun level38() = b(38).right(0.60f).wait(1.00f).right(0.10f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level39() = b(39).wait(0.4f).pauseResume().hopR(18.6f).right(3f).expect(WorldState.WON)
    @Test fun level40() = b(40).right(0.60f).rightJump(0.12f).leftJump(0.25f).right(0.60f).rightJump(0.25f)
        .jump(0.16f).right(0.60f).right(0.10f).left(0.03f).leftJump(0.25f).right(0.60f).right(0.25f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level41() = b(41).rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f) .right(0.60f).expect(WorldState.WON)
    @Test fun level42() = b(42).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(1.20f).expect(WorldState.WON)
    @Test fun level43() = b(43).shake().rightTo(23.5f).waitFor { it.beams[0].lit }.waitFor { !it.beams[0].lit }.hopR(24.6f).right(2f).expect(WorldState.WON)
    @Test fun level43WithoutShakingTheCableSendsYouIntoTheSpikes() = b(43).right(3f).expect(WorldState.DEAD)
    @Test fun level44() = b(44).right(0.60f).rightJump(0.55f).left(0.03f).left(0.03f).right(0.03f).rightJump(0.55f) .rightJump(0.55f).right(0.03f).right(0.03f).leftJump(0.12f).rightJump(0.55f).right(1.20f).expect(WorldState.WON)
    @Test fun level45() = b(45).jump(0.16f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).left(0.03f) .right(0.03f).left(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f) .rightJump(0.12f).rightJump(0.55f).right(1.20f).expect(WorldState.WON)
    @Test fun level46() = b(46).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.03f).expect(WorldState.WON)
    @Test fun level47() = b(47).right(0.60f).leftJump(0.40f).leftJump(0.25f).leftJump(0.12f).left(0.10f).left(0.03f) .left(0.03f).rightJump(0.12f).left(0.03f).rightJump(0.12f).right(0.03f).leftJump(0.40f) .leftJump(0.55f).right(1.20f).expect(WorldState.WON)
    @Test fun level48() = b(48).right(0.60f).rightJump(0.12f).right(1.20f).left(0.10f).leftJump(0.55f).right(0.60f).expect(WorldState.WON)
}
