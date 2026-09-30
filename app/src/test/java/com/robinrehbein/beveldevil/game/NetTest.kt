package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class NetTest {
    private fun near(expected: Float, actual: Float, eps: Float = 1e-3f) =
        assertTrue("expected $expected, was $actual", abs(expected - actual) <= eps)

    private fun World.hops() = events.count { it == Event.Hop }

    @Test
    fun everyDemoBuilds() {
        for (l in NetDemos.all) World(l)
    }

    // ---------- portals ----------

    @Test
    fun portalHopsThroughTheWall() = Bot(NetDemos.portal).right(5f).expect(WorldState.WON)

    @Test
    fun wallWithoutPortalBlocks() {
        val b = Bot(NetDemos.portal)
        b.world.links.clear()
        b.right(5f).expect(WorldState.PLAYING)
        assertTrue(b.world.player.box.r <= 12f + 1e-3f)
    }

    @Test
    fun hopKeepsVelocityAndSpot() {
        val b = Bot(NetDemos.portal).rightTo(7.4f)
        val w = b.world
        val c = Controls().apply { right = true }
        var vx = 0f
        while (w.hops() == 0) { vx = w.player.vx; w.step(Bot.DT, c) }
        // out of the other end at full run, on the same floor row
        near(vx, w.player.vx, 0.01f)
        assertTrue("${w.player.box.x}", w.player.box.x in 17f..17.2f)
        near(15f, w.player.box.b)
    }

    @Test
    fun standingInTheExitDoesNotLoop() {
        val b = Bot(NetDemos.portal).rightTo(8.05f).wait(3f)
        assertEquals(1, b.world.hops())
        assertTrue(b.world.player.box.cx in 17f..18f)
        // stepping out and back in goes home (two-way)
        b.right(0.3f).leftTo(17.5f).wait(1f)
        assertEquals(2, b.world.hops())
        assertTrue(b.world.player.box.cx in 8f..9f)
        b.expect(WorldState.PLAYING)
    }

    @Test
    fun portalCooldown() {
        // two gates back to back: coming out of the first right in front of the second waits out the cooldown
        val l = Level(T("Chain", "Kette"), T("", ""), start = listOf(Portal('1', 6 to 14, 16 to 14), Portal('2', 17 to 14, 26 to 14))) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D'); fill(12..12, 1..14); fill(21..21, 1..14)
        }
        val w = World(l)
        val c = Controls().apply { right = true }
        val times = ArrayList<Float>()
        while (w.time < 4f && w.state == WorldState.PLAYING) {
            val n = w.hops()
            w.step(Bot.DT, c)
            if (w.hops() > n) times += w.time
        }
        assertEquals(WorldState.WON, w.state)
        assertEquals(2, times.size)
        assertTrue("${times[1] - times[0]}", times[1] - times[0] >= Net.COOLDOWN - 1e-4f)
    }

    @Test
    fun placeKeepsTheBoxInsideTheExit() {
        val b = Box(7.9f, 14.14f, Physics.PLAYER_W, Physics.PLAYER_H)
        Net.place(b, 8 to 14, 20 to 4)
        near(20f, b.x); near(5f, b.b)
        val c = Box(8.5f, 14.14f, Physics.PLAYER_W, Physics.PLAYER_H)
        Net.place(c, 8 to 14, 3 to 9)
        near(3f + 1f - c.w, c.x)
        val oneWay = Link('1', 1 to 1, 5 to 5, twoWay = false)
        assertEquals(5 to 5, oneWay.exit(1 to 1)); assertEquals(null, oneWay.exit(5 to 5))
        oneWay.on = false
        assertEquals(null, oneWay.exit(1 to 1))
    }

    @Test
    fun poweredOffPortalIsJustAPicture() {
        val l = Level(T("Down", "Aus"), T("", ""), start = listOf(Portal('1', 8 to 14, 17 to 14), Power('1', false))) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D'); fill(12..12, 1..14)
        }
        val b = Bot(l).right(3f)
        assertEquals(0, b.world.hops())
        b.expect(WorldState.PLAYING)
    }

    @Test
    fun dnsChangeSendsYouOntoSpikes() {
        val b = Bot(NetDemos.dns).rightTo(6.2f)
        assertEquals(5 to 3, b.world.links[0].to)
        b.right(3f).expect(WorldState.DEAD)
        assertTrue(b.world.player.box.cx in 3f..9f && b.world.player.box.b < 5f)
    }

    @Test
    fun dnsFlushedByTheSwitch() {
        val b = Bot(NetDemos.dns).rightTo(6.2f).leftTo(3f).jump(0.4f).wait(0.4f)
        assertEquals(17 to 14, b.world.links[0].to)
        b.right(4f).expect(WorldState.WON)
    }

    @Test(expected = IllegalArgumentException::class)
    fun portalIntoAWallIsRejected() {
        World(Level(T("Bad", "Bad"), T("", ""), start = listOf(Portal('1', 8 to 14, 12 to 14))) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D'); fill(12..12, 1..14)
        })
    }

    // ---------- belts ----------

    @Test
    fun beltCarriesAStandingPlayer() {
        val b = Bot(NetDemos.belt).rightTo(8f).wait(0.3f)
        val x0 = b.world.player.box.cx
        b.wait(1f)
        near(x0 + 3f, b.world.player.box.cx, 0.04f)
        b.expect(WorldState.PLAYING)
        // standing still ends on the spikes that ride along
        b.wait(8f).expect(WorldState.DEAD)
    }

    @Test
    fun beltAddsToRunningAndNotInTheAir() {
        val b = Bot(NetDemos.belt).rightTo(8f).wait(0.3f)
        val x0 = b.world.player.box.cx
        b.right(0.5f)
        // running 8.5 plus the belt's 3 (minus the speed-up)
        assertTrue(b.world.player.box.cx - x0 > 5.2f)
        // mid-jump the belt does not pull
        val j = Bot(NetDemos.belt).rightTo(8f).wait(0.3f).jump(0.02f).wait(0.08f)
        val xa = j.world.player.box.cx
        assertFalse(j.world.player.grounded)
        j.wait(0.2f)
        near(xa, j.world.player.box.cx)
    }

    @Test
    fun beltLevel() = Bot(NetDemos.belt).hopR(21.6f).right(3f).expect(WorldState.WON)

    @Test
    fun reorderedBeltDragsTheRunnerBack() {
        val b = Bot(NetDemos.reorder).rightTo(10.5f)
        assertEquals(-10f, b.world.group('b').belt)
        b.right(10f).expect(WorldState.DEAD)
        assertTrue(b.world.player.box.cx < 3f)
    }

    @Test
    fun reorderedBeltHopping() = Bot(NetDemos.reorder).hopR(9.5f).hopR(0f).hopR(0f).hopR(0f).right(2f).expect(WorldState.WON)

    @Test
    fun beltPowerAndRun() {
        val w = World(NetDemos.belt)
        val g = w.group('b')
        val c = Controls()
        repeat(120) { w.step(Bot.DT, c) }
        near(3f, g.beltRun, 0.01f)
        val l = Level(T("Off", "Aus"), T("", ""), start = listOf(Action.Belt('b', 3f), Power('b', false))) {
            border(); floor(); put(8, 14, 'P'); put(29, 14, 'D'); fill(4..20, 15..15, 'b')
        }
        val b = Bot(l).wait(1f)
        near(8.5f, b.world.player.box.cx)
        assertEquals(0f, b.world.group('b').beltSpeed)
    }

    // ---------- lasers ----------

    @Test
    fun laserCycle() {
        val l = Laser('L', 3 to 1, 3 to 9, on = 1f, off = 1.5f)
        assertTrue(l.litAt(0f)); assertTrue(l.litAt(0.99f)); assertFalse(l.litAt(1.01f)); assertFalse(l.litAt(2.49f))
        assertTrue(l.litAt(2.51f)); assertTrue(l.litAt(5.1f)); assertFalse(l.litAt(6f))
        // glow only in the last 0.6 s before it fires
        near(0f, l.warnAt(1.5f)); near(0.5f, l.warnAt(2.2f)); near(0f, l.warnAt(0.5f))
        // a delay starts dark and glows before the first shot
        val d = Laser('L', 3 to 1, 3 to 9, off = 0f, delay = 1f)
        assertFalse(d.litAt(0.5f)); near(0.5f, d.warnAt(0.7f)); assertTrue(d.litAt(1f)); assertTrue(d.litAt(99f))
        near(0.3f, Laser('L', 3 to 1, 3 to 9, on = 1f, off = 0.5f).telegraph)
        assertFalse(Laser('L', 3 to 1, 3 to 9, on = 1f, off = 1f, phase = 1f).litAt(0f))
    }

    @Test
    fun laserGeometry() {
        val v = Laser('L', 5 to 9, 5 to 1)
        assertTrue(v.vertical)
        near(5.375f, v.x0); near(5.625f, v.x1); near(1.625f, v.y0); near(9.375f, v.y1)
        val h = Laser('L', 2 to 4, 9 to 4)
        near(2.625f, h.x0); near(9.375f, h.x1); near(4.375f, h.y0); near(4.625f, h.y1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun diagonalLaserIsRejected() { Laser('L', 1 to 1, 4 to 4) }

    @Test
    fun beamFollowsItsClock() {
        val w = World(NetDemos.laser)
        val beam = w.beams[0]
        val c = Controls()
        while (w.time < 6f) {
            w.step(Bot.DT, c)
            assertEquals("t=${w.time}", beam.laser.litAt(w.time - beam.t0), beam.lit)
        }
    }

    @Test
    fun runningThroughALitLaserDies() = Bot(NetDemos.laser).right(4f).expect(WorldState.DEAD)

    @Test
    fun waitingForTheGapPasses() = Bot(NetDemos.laser).rightTo(13f)
        .waitFor { it.beams[0].lit }.waitFor { !it.beams[0].lit }.right(3f).expect(WorldState.WON)

    @Test
    fun maintenanceOpensTheFirewall() {
        val b = Bot(NetDemos.firewall)
        assertTrue(b.world.beams[0].lit)
        b.rightTo(10.2f)
        assertFalse(b.world.beams[0].lit)
        b.rightTo(19.2f)
        // warming up behind you
        assertFalse(b.world.beams[0].lit); assertTrue(b.world.beams[0].warn > 0f)
        b.right(4f).expect(WorldState.WON)
        // jumping into the horizontal beam overhead is fatal
        Bot(NetDemos.firewall).rightTo(10.2f).waitFor { it.beams[1].lit }.hopR(22f).expect(WorldState.DEAD)
    }

    @Test
    fun laserSwitchedOnWarnsThenKills() {
        val l = Level(T("Reboot", "Neustart"), T("", ""), start = listOf(Laser('L', 5 to 1, 5 to 14), Power('L', false)),
            traps = listOf(trap(Trigger.After(1f), Power('L', true)))) {
            border(); floor(); put(5, 14, 'P'); put(29, 14, 'D')
        }
        val b = Bot(l).wait(1.3f)
        b.expect(WorldState.PLAYING)
        assertTrue(b.world.beams[0].warn > 0.3f)
        b.wait(0.4f).expect(WorldState.DEAD)
        near(1f + Laser.TELEGRAPH, b.world.stateTime, 0.02f)
    }
}
