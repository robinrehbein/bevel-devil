package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.PathSaw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MechanicsTest {
    private fun near(expected: Float, actual: Float, eps: Float = 1e-3f) =
        assertTrue("expected $expected, was $actual", abs(expected - actual) <= eps)

    private fun near(expected: Pair<Float, Float>, actual: Pair<Float, Float>) {
        near(expected.first, actual.first); near(expected.second, actual.second)
    }

    // ---------- blinking platforms ----------

    @Test
    fun blinkCycle() {
        val b = Blink('a', on = 1.2f, off = 0.8f)
        assertEquals(2f, b.period, 0f)
        assertTrue(b.solidAt(0f)); assertTrue(b.solidAt(1.19f)); assertFalse(b.solidAt(1.21f)); assertFalse(b.solidAt(1.99f))
        assertTrue(b.solidAt(2.01f)); assertTrue(b.solidAt(10.5f)); assertFalse(b.solidAt(11.5f))
        // flicker only in the last 0.45 s of the on phase
        near(0f, b.warnAt(0.7f)); near(0.5f, b.warnAt(1.2f - 0.225f)); near(0f, b.warnAt(1.5f))
        assertFalse(b.soonAt(1.3f)); assertTrue(b.soonAt(1.8f))
        // a phase starts it later in the cycle
        val shifted = Blink('a', on = 1.2f, off = 0.8f, phase = 1.2f)
        assertFalse(shifted.solidAt(0f)); assertTrue(shifted.solidAt(0.81f))
        // a short on phase flickers for half of it
        near(0.2f, Blink('a', on = 0.4f, off = 1f).telegraph)
    }

    @Test
    fun blinkingGroupFollowsItsClock() {
        val w = World(Demo.blink)
        val g = w.group('a')
        val c = Controls()
        var t = 0f
        while (w.time < 6f) {
            w.step(Bot.DT, c)
            t = w.time
            assertEquals("t=$t", g.blink!!.solidAt(t - g.blinkT0), g.visible)
        }
    }

    @Test
    fun standingOnAVanishingPlatformFalls() {
        val b = Bot(Demo.blink).rightTo(15f).wait(3f)
        b.expect(WorldState.DEAD)
    }

    @Test
    fun blinkBridge() = Bot(Demo.blink).rightTo(10.2f)
        .waitFor { !it.group('a').visible }.waitFor { it.group('a').visible }
        .rightTo(22f).right(2f).expect(WorldState.WON)

    @Test
    fun blinkWaitsForThePlayerToStepOut() {
        val b = Bot(Demo.blinkWall).rightTo(8.8f).waitFor { !it.group('a').visible }.rightTo(10.4f)
        val g = b.world.group('a')
        // stand inside the wall while it should come back: it stays away instead of crushing
        b.waitFor(3f) { g.blink!!.solidAt(it.time - g.blinkT0) }.wait(0.3f)
        b.expect(WorldState.PLAYING)
        assertFalse(g.visible)
        assertTrue(g.soon)
        b.rightTo(11.6f).wait(0.05f)
        assertTrue(g.visible)
        b.right(3f).expect(WorldState.WON)
    }

    // ---------- saws on paths ----------

    @Test
    fun pathSawPingPong() {
        val s = PathSaw(2f, 0f to 0f, 4f to 0f, 4f to 2f, delay = 0.5f)
        near(0f to 0f, s.at(0f)); near(0f to 0f, s.at(0.5f))
        near(2f to 0f, s.at(1.5f)); near(4f to 1f, s.at(3f)); near(4f to 2f, s.at(3.5f))
        // and back the same way
        near(4f to 1f, s.at(4f)); near(1f to 0f, s.at(6f)); near(0f to 0f, s.at(6.5f)); near(2f to 0f, s.at(7.5f))
    }

    @Test
    fun pathSawLoop() {
        val s = PathSaw(4f, 0f to 0f, 2f to 0f, 2f to 2f, 0f to 2f, loop = true)
        near(1f to 0f, s.at(0.25f)); near(2f to 1f, s.at(0.75f)); near(1f to 2f, s.at(1.25f)); near(0f to 1f, s.at(1.75f))
        near(0f to 0f, s.at(2f)); near(1f to 0f, s.at(2.25f))
        // a saw that stands still
        near(3f to 3f, PathSaw(0f, 3f to 3f, 5f to 3f).at(9f)); near(1f to 1f, PathSaw(5f, 1f to 1f).at(2f))
    }

    @Test
    fun pathSawsStayAndMove() {
        val w = World(Demo.pathSaw)
        assertEquals(2, w.saws.size)
        near(16f to 14.4f, w.saws[0].x to w.saws[0].y)
        val c = Controls()
        repeat((120 * 30f).toInt()) { w.step(Bot.DT, c) }
        // still there after half a minute, and where the path says
        assertEquals(2, w.saws.size)
        val s = w.saws[1]
        near(s.path!!.at(w.time - s.t0), s.x to s.y)
    }

    @Test
    fun pathSawKillsTheHastyAndLetsThePatientPass() {
        Bot(Demo.pathSaw).right(3f).expect(WorldState.DEAD)
        Bot(Demo.pathSaw).rightTo(13f)
            .waitFor { w -> w.saws[0].y < 11.5f }.waitFor { w -> w.saws[0].y > 13.3f }.waitFor { w -> w.saws[0].y < 12.5f }
            .right(3f).expect(WorldState.WON)
    }

    // ---------- idle ----------

    @Test
    fun idleFiresAfterStandingStill() {
        val b = Bot(Demo.idle).wait(1.1f)
        assertEquals(GroupMode.IDLE, b.world.group('a').mode)
        b.wait(0.2f)
        assertEquals(GroupMode.FALL, b.world.group('a').mode)
        b.wait(2f).expect(WorldState.DEAD)
    }

    @Test
    fun idleResetsOnInput() {
        val b = Bot(Demo.idle).wait(1f).jump(0.05f).wait(1f).left(0.05f).wait(1f)
        b.expect(WorldState.PLAYING)
        assertEquals(GroupMode.IDLE, b.world.group('a').mode)
        near(1f, b.world.idle, 0.02f)
    }

    @Test
    fun idleLevel() = Bot(Demo.idle).right(4f).expect(WorldState.WON)

    // ---------- tilt and shake ----------

    @Test
    fun tiltPlatformCarriesAcrossThePit() {
        Bot(Demo.tilt).right(6f).expect(WorldState.DEAD)
        val b = Bot(Demo.tilt).rightTo(9.2f).wait(0.2f).tilt(1f).wait(2.4f)
        near(13f, b.world.group('a').ox)
        b.expect(WorldState.PLAYING)
        b.right(2f).expect(WorldState.WON)
    }

    @Test
    fun tiltFollowsTheLevelAndClamps() {
        val w = World(Demo.tilt)
        val g = w.group('a')
        val c = Controls().apply { tilt = 0.5f }
        repeat(240) { w.step(Bot.DT, c) }
        near(6.5f, g.ox)
        c.tilt = 3f
        repeat(240) { w.step(Bot.DT, c) }
        near(13f, g.ox); near(1f, w.tilt)
        c.tilt = -1f
        repeat(480) { w.step(Bot.DT, c) }
        near(0f, g.ox)
    }

    @Test
    fun slopeCarriesTheJump() {
        Bot(Demo.slope).hopR(9.6f).right(3f).expect(WorldState.DEAD)
        Bot(Demo.slope).tilt(1f).hopR(9.4f).right(3f).expect(WorldState.WON)
        // tilted the other way it pushes back
        Bot(Demo.slope).tilt(-1f).right(1f).also { assertTrue(it.world.player.box.cx < 2.5f + 3f) }
    }

    @Test
    fun shakeOpensTheWall() {
        Bot(Demo.shake).right(4f).expect(WorldState.PLAYING)
        Bot(Demo.shake).rightTo(18f).shake().right(3f).expect(WorldState.WON)
    }

    @Test
    fun levelsKnowWhatMotionTheyUse() {
        assertTrue(Demo.tilt.usesTilt); assertFalse(Demo.tilt.usesShake)
        assertTrue(Demo.slope.usesTilt)
        assertTrue(Demo.shake.usesShake); assertFalse(Demo.shake.usesTilt)
        assertFalse(Demo.blink.usesMotion)
        assertEquals(3, Levels.all.count { it.usesMotion })  // World 1 has two surprise levels, World 2 one (the rack quake)
    }

    @Test
    fun tiltWithoutMotionLevelDoesNothing() {
        val a = Bot(Levels.all[0]).tilt(1f).shake(0.5f)
        val b = Bot(Levels.all[0]).wait(0.6f)
        near(b.world.player.box.x, a.world.player.box.x)
        assertTrue(a.world.events.none { it is Event.Shake })
    }

    @Test
    fun everyDemoBuilds() {
        for (l in Demo.all) World(l)
        assertEquals(7, Demo.all.size)
    }

    // ---------- sensor mapping ----------

    @Test
    fun tiltFromGravityPerRotation() {
        val g = 9.81f
        val s = kotlin.math.sin(Math.toRadians(30.0)).toFloat() * g
        val c = kotlin.math.cos(Math.toRadians(30.0)).toFloat() * g
        // flat on the table: level
        near(0f, Motion.tilt(0f, 0f, g, Motion.ROT_90))
        // portrait, right edge 30° down: the reading leans to the left
        near(1f, Motion.tilt(-s, c, 0f, Motion.ROT_0))
        // landscape, top of the phone to the left (ROTATION_90): screen right is device -y
        near(1f, Motion.tilt(c, s, 0f, Motion.ROT_90))
        near(-1f, Motion.tilt(c, -s, 0f, Motion.ROT_90))
        // turned the other way (ROTATION_270) the same roll reads the opposite on device y
        near(1f, Motion.tilt(-c, -s, 0f, Motion.ROT_270))
        near(-1f, Motion.tilt(-c, s, 0f, Motion.ROT_270))
        near(1f, Motion.tilt(s, -c, 0f, Motion.ROT_180))
    }

    @Test
    fun tiltDeadZoneAndScale() {
        fun roll(deg: Double) = kotlin.math.sin(Math.toRadians(deg)).toFloat()
        fun rollZ(deg: Double) = kotlin.math.cos(Math.toRadians(deg)).toFloat()
        // held in landscape (ROTATION_90), screen facing up-ish: roll shows on device y
        near(0f, Motion.tilt(0f, roll(2.0), rollZ(2.0), Motion.ROT_90))
        near(0.5f, Motion.tilt(0f, roll(12.5), rollZ(12.5), Motion.ROT_90), 0.01f)
        near(-0.5f, Motion.tilt(0f, roll(-12.5), rollZ(12.5), Motion.ROT_90), 0.01f)
        near(1f, Motion.tilt(0f, roll(80.0), rollZ(80.0), Motion.ROT_90))
        near(0f, Motion.tilt(0f, 0f, 0f, Motion.ROT_0))
    }

    @Test
    fun shakeDebounce() {
        val d = ShakeDetector(threshold = 2.2f, cooldown = 0.8)
        val hard = 30f
        assertFalse(d.feed(0f, 9.81f, 0f, 0.0))
        assertTrue(d.feed(hard, 0f, 0f, 1.0))
        // the rest of the same shake
        assertFalse(d.feed(-hard, 0f, 0f, 1.1))
        assertFalse(d.feed(hard, 0f, 0f, 1.7))
        assertFalse(d.feed(0f, 9.81f, 0f, 2.0))
        // a new shake after a calm moment
        assertTrue(d.feed(hard, 0f, 0f, 2.6))
    }
}
