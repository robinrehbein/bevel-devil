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

    @Test
    fun everyTrapLevelPlaysExactlyOneCard() {
        World2.levels.forEachIndexed { i, l ->
            if (l.traps.isNotEmpty()) {
                val plays = l.traps.sumOf { t -> t.actions.count { it is Action.Play } }
                assertEquals("level ${i + 1} should play exactly one card", 1, plays)
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
        assertTrue(actions(World2.levels[17]).filter(::net).all { it is Action.Laser })
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
        // the three meta twists (ghost, pause dodge, lag roll) and the shake level stay at two traps
        for (n in listOf(36, 39, 40, 43)) assertEquals("level $n", 2, chain(World2.levels[n - 1]))
    }

    /** The counter just learned (hop the obstacle) followed by the old reflex (keep running) is what the next trap of a chain waits for. */
    @Test
    fun theObviousRunDiesInTheChain() {
        // 1: the landing behind the second pit has two spikes that only grow while you fly
        b(1).hopR(10.7f).hopR(18.6f).right(2f).expect(WorldState.DEAD)
        // 2: the first spike grows a second one in front of the portal
        b(2).hopR(2.6f).right(2f).expect(WorldState.DEAD)
        // 3: after the second rack the spikes are there, even if the room is plain
        b(3).hopR(12.9f).hopR(17.8f).right(3f).expect(WorldState.DEAD)
        // 4: the gap you aim for is plugged while you fly at it
        b(4).hopR(9.4f).right(1f).expect(WorldState.DEAD)
        // 17: landing after the first spike turns the belt around, and it carries you back into the spike
        b(17).hopR(10.2f).right(3f).expect(WorldState.DEAD)
        // 18: waiting in front of the gate is waiting in front of a second beam
        b(18).hopR(7f).rightTo(14.3f).wait(1.3f).expect(WorldState.DEAD)
        // 20: the landing resets the rhythm of the second gate, and there are spikes in front of it
        b(20).waitUntil(2.05f).hopR(12f).right(2f).expect(WorldState.DEAD)
        // 28: the tunnel drops you next to the IPS beam, and running on runs into it
        b(28).right(3f).expect(WorldState.DEAD)
        // 29: the belt turns against you after the first hop, and the saw behind you does not
        b(29).hopR(8.6f).right(3f).expect(WorldState.DEAD)
        // 31 and 32: the hops land in spikes that grew in mid-air
        b(31).hopR(5.3f).right(5f).expect(WorldState.DEAD)
        b(32).hopR(4.5f).right(3f).expect(WorldState.DEAD)
    }

    @Test
    fun beltsTurnAroundWhereYouLandAndLasersWarmUpWhereYouWait() {
        assertEquals(-6f, b(17).hopR(10.2f).wait(0.05f).world.group('b').belt)
        val firewall = b(18).hopR(7f).wait(0.1f)
        assertTrue(firewall.world.beams.any { it.laser.id == 'M' })
        // the beam warms up first: the emitters glow before it fires
        val glow = b(18).hopR(7f).wait(0.6f).world.beams.first { it.laser.id == 'M' }
        assertTrue(glow.warn > 0f && !glow.lit)
    }

    /** A chain may end you, but never leaves you alive where the door cannot be reached. */
    @Test
    fun noChainStrandsYouAliveAwayFromTheDoor() {
        // 3: between the second rack and its spikes there is room to hop out, forwards
        b(3).hopR(12.9f).hopR(17.8f).hopR(25.2f).expect(WorldState.PLAYING)
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
        // 31: whoever is too slow for the closing gate does not wait in front of it
        val late = b(31).rightTo(24f).waitFor(10f) { it.time > 5.2f }.wait(1.0f)
        late.expect(WorldState.DEAD)
    }

    // ---------- the levels that react to the player in unusual ways ----------

    @Test
    fun dnsChangeSendsYouToTheSpikesAndFlushingFixesIt() {
        b(6).rightTo(7.5f).wait(0.4f).right(3f).expect(WorldState.DEAD)
        b(6).rightTo(6.3f).leftTo(3.4f).jump(0.4f).wait(0.4f).also { assertEquals(17 to 14, it.world.links[0].to) }.rightTo(9f).hopR(20.8f).right(1.5f).expect(WorldState.WON)
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
    fun loopbackSendsYouHomeUntilYouJumpOverIt() {
        val loop = b(14).rightTo(8.5f).rightJump(0.55f).landRight().rightTo(14.9f).right(0.3f).wait(0.3f)
        assertTrue("x=${loop.world.player.box.cx} hop=${loop.world.links[0].hopTime}", loop.world.links[0].hopTime > 0f && loop.world.player.box.cx < 13f)
        b(14).rightTo(8.5f).rightJump(0.55f).landRight().rightTo(13.8f).rightJump(0.55f).landRight().rightTo(19.5f).expect(WorldState.PLAYING)
    }

    @Test
    fun theFirewallOnlyGoesDownWhenYouPauseAndResume() {
        val bot = b(39).wait(0.5f).tapPause()
        assertEquals(1, bot.world.dodges)
        b(39).rightTo(13f).wait(0.5f).right(2f).expect(WorldState.DEAD)
        assertFalse(b(39).wait(0.5f).pauseResume().wait(0.05f).world.beams[0].on)
    }

    @Test
    fun keepAliveDropsTheFloorWhenYouStandStill() {
        b(26).rightTo(12f).wait(2.3f).expect(WorldState.DEAD)
        b(26).rightTo(12f).fidgetUntil { !it.beams[0].lit }.hopR(16.5f).hopR(22f).right(1f).expect(WorldState.WON)
    }

    @Test
    fun replayAttackPunishesRepeatingYourself() {
        val first = { b(36).rightTo(9f).waitFor { !it.beams[0].lit }.rightTo(19f) }
        first().wait(0.8f).expect(WorldState.PLAYING)
        // same plan again: the replay of the last attempt catches you on the island
        val second = first().right(5f).also { it.expect(WorldState.DEAD) }.retry()
        second.rightTo(9f).waitFor { !it.beams[0].lit }.rightTo(19f).wait(2.5f).expect(WorldState.DEAD)
        assertEquals(Card.GHOST_BLOCK, second.world.lastCard)
    }

    @Test
    fun theBeltCarriesYouButOnlyOnTheGround() {
        val bot = b(17).rightTo(7f).wait(1f)
        assertTrue(bot.world.player.box.cx > 9f)
    }

    // ---------- Act 1: Handshake ----------
    @Test fun level01() = b(1).right(1.20f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .right(1.20f).expect(WorldState.WON)
    @Test fun level02() = b(2).right(0.25f).rightJump(0.40f).right(0.03f).left(0.10f).rightJump(0.55f).leftJump(0.55f)
        .right(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level03() = b(3).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).left(0.60f).right(0.25f).left(0.03f) .left(0.25f).leftJump(0.55f).leftJump(0.55f).left(1.20f).leftJump(0.55f).expect(WorldState.WON)
    @Test fun level04() = b(4).right(0.60f).right(0.60f).rightJump(0.25f).rightJump(0.12f).left(0.10f).right(0.03f)
        .left(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level05() = b(5).right(0.60f).right(0.60f).rightJump(0.25f).left(0.10f).right(0.03f).right(0.03f)
        .left(0.03f).rightJump(0.12f).leftJump(0.25f).right(0.10f).left(0.03f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.55f).left(0.60f).expect(WorldState.WON)
    @Test fun level06() = b(6).right(0.60f).left(0.60f).rightJump(0.55f).right(0.25f).hopR(20.8f).right(1.5f).expect(WorldState.WON)
    @Test fun level07() = b(7).right(0.60f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).left(0.03f) .right(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).right(0.10f) .left(0.03f).left(0.03f).right(0.03f).left(0.03f).left(0.03f).right(0.03f) .left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).right(0.03f).right(0.03f) .left(0.03f).left(0.03f).left(0.03f).right(0.03f).right(0.03f).left(0.10f) .rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level08() = b(8).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.25f).right(0.03f).rightJump(0.55f) .right(1.20f).expect(WorldState.WON)
    @Test fun level09() = b(9).right(0.60f).leftJump(0.55f).leftJump(0.55f).leftJump(0.55f).left(0.25f)
        .rightJump(0.25f).left(0.10f).right(0.03f).left(0.03f).left(0.10f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level10() = b(10).right(0.60f).rightJump(0.55f).right(0.25f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level11() = b(11).right(1.20f).right(0.60f).right(0.10f).right(0.10f).left(0.03f).left(0.03f) .left(0.03f).right(1.20f).expect(WorldState.WON)
    @Test fun level12() = b(12).right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.25f).rightJump(0.55f) .right(0.25f).expect(WorldState.WON)
    @Test fun level13() = b(13).right(0.60f).right(0.60f).right(0.25f).rightJump(0.12f).right(0.03f).right(0.03f)
        .leftJump(0.12f).left(0.10f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).left(0.60f).expect(WorldState.WON)
    @Test fun level14() = b(14).right(0.60f).rightJump(0.55f).rightJump(0.55f).right(0.60f).right(0.25f)
        .rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level15() = b(15).rightTo(5.6f).leftTo(2.4f).rightTo(4.7f).rightJump(0.4f).rightTo(13.2f).rightJump(0.5f) .right(2f).expect(WorldState.WON)
    @Test fun level16() = b(16).right(0.60f).right(0.60f).right(0.60f).rightJump(0.25f).right(0.10f).left(0.03f)
        .leftJump(0.12f).right(0.03f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)

    // ---------- Act 2: Traffic ----------
    @Test fun level17() = b(17).right(0.60f).right(0.25f).rightJump(0.55f).rightJump(0.25f).rightJump(0.12f)
        .leftJump(0.12f).right(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level18() = b(18).right(0.60f).rightJump(0.55f).right(0.25f).right(0.10f).leftJump(0.25f).right(0.60f)
        .rightJump(0.12f).right(0.03f).leftJump(0.12f).left(0.03f).left(0.03f).left(0.10f).rightJump(0.55f)
        .right(0.60f).right(0.60f).expect(WorldState.WON)
    @Test fun level19() = b(19).right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level20() = b(20).waitUntil(2.05f).hopR(12f).wait(0.1f).waitFor { !it.beams[1].lit }.hopR(17.3f).wait(1.3f)
        .right(3f).expect(WorldState.WON)
    @Test fun level21() = b(21).right(0.25f).leftJump(0.12f).left(0.03f).right(0.10f).left(0.03f).right(0.03f)
        .rightJump(0.12f).right(0.10f).left(0.10f).rightJump(0.12f).right(0.03f).right(0.03f).rightJump(0.12f)
        .right(0.10f).rightJump(0.12f).left(0.10f).right(0.03f).left(0.03f).left(0.03f).rightJump(0.12f).right(0.03f)
        .right(0.03f).rightJump(0.12f).right(0.10f).rightJump(0.12f).left(0.10f).right(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.12f).right(0.03f).right(0.03f).rightJump(0.12f).right(0.10f).rightJump(0.12f).left(0.10f)
        .right(0.03f).left(0.03f).left(0.03f).rightJump(0.12f).right(0.03f).right(0.03f).rightJump(0.25f).right(0.03f)
        .leftJump(0.12f).right(0.60f).expect(WorldState.WON)
    @Test fun level22() = b(22).right(0.60f).rightJump(0.55f).right(0.60f).rightJump(0.12f).leftJump(0.12f)
        .left(0.10f).rightJump(0.12f).right(0.03f).left(0.10f).left(0.03f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.25f).right(0.60f).expect(WorldState.WON)
    @Test fun level23() = b(23).rightJump(0.55f).rightJump(0.55f).right(0.25f).right(0.03f).rightJump(0.55f)
        .left(0.10f).rightJump(0.25f).rightJump(0.12f).jump(0.16f).rightJump(0.55f).rightJump(0.25f).left(0.10f)
        .leftJump(0.12f).rightJump(0.55f).left(0.03f).left(0.03f).rightJump(0.25f).left(0.60f).expect(WorldState.WON)
    @Test fun level24() = b(24).rightJump(0.55f).rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.25f)
        .rightJump(0.12f).rightJump(0.25f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level25() = b(25).right(0.60f).rightJump(0.55f).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level26() = b(26).rightTo(13.6f).fidgetUntil { !it.beams[0].lit }.hopR(16.5f).hopR(22f).right(1f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level28() = b(28).right(0.60f).right(0.60f).rightJump(0.25f).rightJump(0.12f).jump(0.16f)
        .rightJump(0.55f).expect(WorldState.WON)
    @Test fun level29() = b(29).right(0.60f).rightJump(0.55f).rightJump(0.12f).right(0.10f).left(0.10f)
        .rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).expect(WorldState.WON)
    @Test fun level30() = b(30).hopR(3.2f).hopR(15f).hopR(25f).right(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightJump(0.55f).rightJump(0.55f).rightJump(0.25f).left(0.10f).right(0.03f)
        .right(0.03f).leftJump(0.12f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightJump(0.55f).rightJump(0.25f).right(0.03f).left(0.10f).left(0.10f)
        .rightJump(0.55f).right(0.10f).left(0.03f).left(0.03f).rightJump(0.25f).jump(0.16f).leftJump(0.12f)
        .rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)

    // ---------- Act 3: Root ----------
    @Test fun level33() = b(33).right(0.60f).right(0.25f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f) .right(0.03f).expect(WorldState.WON)
    @Test fun level34() = b(34).right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .right(0.60f).expect(WorldState.WON)
    @Test fun level35() = b(35).rightJump(0.55f).right(0.10f).left(0.10f).left(0.03f).left(0.10f).rightJump(0.55f)
        .right(0.10f).right(0.03f).left(0.03f).left(0.10f).left(0.03f).rightJump(0.25f).left(0.10f).right(0.03f)
        .right(0.03f).left(0.25f).rightJump(0.55f).rightJump(0.12f).right(0.10f).right(0.03f).right(0.03f)
        .right(0.10f).leftJump(0.12f).left(0.10f).left(0.10f).rightJump(0.55f).rightJump(0.55f).right(0.60f).expect(WorldState.WON)
    @Test fun level36() = b(36).rightTo(9f).waitFor { !it.beams[0].lit }.rightTo(19f).waitFor { !it.beams[1].lit }.right(6f).retry().waitUntil(2.7f).rightTo(24.2f).rightJump(0.55f).landRight().right(3f).expect(WorldState.WON)
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
