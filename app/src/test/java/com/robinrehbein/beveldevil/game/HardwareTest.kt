package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Fan
import com.robinrehbein.beveldevil.game.Action.FanSet
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Toggle
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Heated
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class HardwareTest {
    private fun near(expected: Float, actual: Float, eps: Float = 1e-3f) =
        assertTrue("expected $expected, was $actual", abs(expected - actual) <= eps)

    private fun level(start: List<Action> = emptyList(), traps: List<Trap> = emptyList(), build: MapBuilder.() -> Unit) =
        Level(T("Test", "Test"), T("", ""), start = start, traps = traps) { border(); floor(); put(29, 14, 'D'); build() }

    private fun World.run(seconds: Float, c: Controls = Controls()) {
        val end = time + seconds
        while (time < end - 1e-4f) step(Bot.DT, c)
    }

    @Test
    fun everyDemoBuilds() {
        for (l in HardwareDemos.all) World(l)
    }

    @Test
    fun holdingRightWinsNoDemo() {
        // every demo asks for something besides running right; these only prove the pad works, or reward running on
        for (l in HardwareDemos.all - setOf(HardwareDemos.circuit, HardwareDemos.live, HardwareDemos.overclock)) {
            assertTrue(l.name.en, Bot(l).right(10f).world.state != WorldState.WON)
        }
    }

    // ---------- circuits ----------

    @Test
    fun padPowersTheBridge() {
        val b = Bot(HardwareDemos.circuit)
        assertFalse(b.world.circuits['a']!!.powered)
        b.rightTo(7f)
        assertTrue(b.world.circuits['a']!!.powered)
        assertEquals(1, b.world.pads[0].presses)
        b.right(4f).expect(WorldState.WON)
    }

    @Test
    fun deadRailIsNotSolid() {
        val b = Bot(HardwareDemos.circuit)
        b.world.pads.clear()
        b.right(4f).expect(WorldState.DEAD)
        assertTrue(b.world.player.box.cx in 11f..21f)
    }

    @Test
    fun togglePadFlipsOnEveryStep() {
        val b = Bot(HardwareDemos.circuit).rightTo(7.5f).leftTo(4f)
        assertEquals(2, b.world.pads[0].presses)
        assertFalse(b.world.circuits['a']!!.powered)
        b.rightTo(7.5f)
        assertTrue(b.world.circuits['a']!!.powered)
    }

    @Test
    fun holdPadFlipsWhileHeld() {
        val l = level(listOf(Circuit('w'), Pad('1', at = 6 to 14, circuits = "w", mode = PadMode.HOLD))) {
            put(2, 14, 'P'); fill(12..12, 1..14, 'w')
        }
        val b = Bot(l).rightTo(6.5f).wait(0.3f)
        assertTrue(b.world.pads[0].down)
        assertFalse(b.world.circuits['w']!!.powered)
        b.rightTo(9f)
        assertTrue(b.world.circuits['w']!!.powered)
        b.right(2f)
        assertTrue(b.world.player.box.r <= 12f + 1e-3f)
    }

    @Test
    fun railWaitsForThePlayerToLeave() {
        // standing where a dead rail is when the power returns: it stays dark (outline bright) until you step out
        val l = level(listOf(Circuit('a', on = false)), listOf(trap(After(0.5f), Power('a', true)))) {
            fill(7..9, 14..14, 'a'); put(8, 13, 'P')
        }
        val b = Bot(l).wait(1f)
        val c = b.world.circuits['a']!!
        assertFalse(c.powered)
        assertTrue(c.soon)
        b.right(1f)
        assertTrue(c.powered)
        b.expect(WorldState.PLAYING)
    }

    @Test
    fun clockFollowsTheBlinkTiming() {
        val w = World(HardwareDemos.clock)
        val a = w.circuits['a']!!
        val timing = a.clock!!.timing
        while (w.time < 6f) {
            w.step(Bot.DT, Controls())
            assertEquals("t=${w.time}", timing.solidAt(w.time), a.powered)
            if (a.powered) near(timing.warnAt(w.time), a.warn)
        }
    }

    @Test
    fun clockBridgeRunningStraightFalls() = Bot(HardwareDemos.clock).right(6f).expect(WorldState.DEAD)

    @Test
    fun clockBridgeWaitingForThePower() =
        Bot(HardwareDemos.clock).rightTo(8.3f).waitPowered('a', false).waitPowered('a').right(4f).expect(WorldState.WON)

    @Test
    fun liveTraceKillsUntilThePadCutsIt() {
        val b = Bot(HardwareDemos.live)
        b.world.pads.clear()
        b.right(4f).expect(WorldState.DEAD)
        assertTrue(b.world.player.box.cx in 15f..16.5f)
        // the pad cuts it, and a dead trace is not solid either
        Bot(HardwareDemos.live).right(4f).expect(WorldState.WON)
    }

    @Test
    fun deadTraceIsHarmlessAndLiveSpikeGlyphsDoNotHurtAsSpikes() {
        val l = level(listOf(Circuit('Z', on = false))) { put(2, 14, 'P'); fill(10..12, 14..14, 'Z') }
        Bot(l).right(4f).expect(WorldState.WON)
    }

    @Test
    fun powerCutUnderfoot() {
        val b = Bot(HardwareDemos.cut).right(4f)
        b.expect(WorldState.DEAD)
        assertFalse(b.world.circuits['a']!!.powered)
        // it was solid before the trap fired
        val w = World(HardwareDemos.cut)
        assertTrue(w.circuits['a']!!.powered && w.group('a').pieces.all { it.solid })
    }

    @Test
    fun powerCutJumpedAnyway() = Bot(HardwareDemos.cut).hopR(11.5f).right(3f).expect(WorldState.WON)

    @Test
    fun toggleAndBitFlip() {
        val l = level(
            listOf(Circuit('a'), Circuit('b', on = false)),
            listOf(trap(After(0.2f), BitFlip('a', 'b')), trap(After(0.5f), Toggle("ab"))),
        ) { put(2, 14, 'P'); fill(10..11, 10..10, 'a'); fill(14..15, 10..10, 'b') }
        val w = World(l)
        w.run(0.3f)
        assertFalse(w.circuits['a']!!.powered); assertTrue(w.circuits['b']!!.powered)
        w.run(0.3f)
        assertTrue(w.circuits['a']!!.powered); assertFalse(w.circuits['b']!!.powered)
    }

    @Test
    fun pressedTriggerFiresOnThePad() {
        val l = level(
            listOf(Pad('1', at = 6 to 14)),
            listOf(trap(Pressed('1'), Show('x'))),
        ) { put(2, 14, 'P'); fill(10..10, 10..10, 'x') }
        val hidden = Level(l.name, l.intro, legend = mapOf('x' to Glyph(spike = false, hidden = true)), traps = l.traps, start = l.start) {
            for (y in 0 until l.rows) for (x in 0 until l.cols) put(x, y, l.map.grid[y][x])
        }
        val b = Bot(hidden).rightTo(5f)
        assertFalse(b.world.group('x').visible)
        b.rightTo(7f)
        assertTrue(b.world.group('x').visible)
    }

    @Test(expected = IllegalArgumentException::class)
    fun padInAWallIsRejected() {
        World(level(listOf(Pad('1', at = 6 to 15))) { put(2, 14, 'P') })
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownCircuitIsRejected() {
        World(level(listOf(Pad('1', at = 6 to 14, circuits = "q"))) { put(2, 14, 'P'); fill(9..9, 9..9, 'q') })
    }

    // ---------- heat ----------

    @Test
    fun heatRampsWhileStandingAndCoolsOff() {
        val l = level(listOf(Heat('h', rise = 1f, cool = 2f))) { put(5, 14, 'P'); fill(3..7, 15..15, 'h') }
        val w = World(l)
        w.run(0.5f)
        val h = w.heaters['h']!!
        near(0.5f, h.heat, 0.02f)
        // off the plate (walk right onto plain floor) it cools at 1/2 per second
        val c = Controls().apply { right = true }
        while (w.player.box.x < 8f) w.step(Bot.DT, c)
        val at = h.heat
        w.run(0.4f)
        near(at - 0.2f, h.heat, 0.02f)
        assertEquals(WorldState.PLAYING, w.state)
    }

    @Test
    fun fullHeatBurnsOnTime() {
        val l = level(listOf(Heat('h', rise = 1.2f))) { put(5, 14, 'P'); fill(3..7, 15..15, 'h') }
        val w = World(l)
        var sizzles = 0
        while (w.state == WorldState.PLAYING && w.time < 3f) {
            w.step(Bot.DT, Controls())
            sizzles += w.events.count { it == Event.Sizzle }
            w.events.clear()
        }
        assertEquals(WorldState.DEAD, w.state)
        near(1.2f, w.stateTime, 0.03f)
        assertEquals(1, sizzles)
    }

    @Test
    fun meltingPlateVanishes() {
        val l = level(listOf(Heat('h', rise = 0.5f, melt = true))) { put(5, 14, 'P'); fill(3..7, 15..17, '.'); fill(3..7, 15..15, 'h') }
        val b = Bot(l).wait(0.45f)
        b.expect(WorldState.PLAYING)
        b.wait(0.3f)
        assertTrue(b.world.heaters['h']!!.melted)
        b.wait(1f).expect(WorldState.DEAD)
    }

    @Test
    fun hotPlateRunningStraightBurns() = Bot(HardwareDemos.heat).right(6f).expect(WorldState.DEAD)

    @Test
    fun hotPlateCoolingOnTheSink() {
        val b = Bot(HardwareDemos.heat).rightTo(15.5f)
        assertTrue(b.world.heaters['h']!!.heat > 0.6f)
        assertTrue(b.world.sinks[0].active)
        b.waitCooled('h').right(4f).expect(WorldState.WON)
    }

    @Test
    fun chipUnderLoadHeatsWithoutYou() {
        val w = World(HardwareDemos.chip)
        w.run(0.7f)
        near(0.5f, w.heaters['c']!!.heat, 0.02f)
        Bot(HardwareDemos.chip).right(6f).expect(WorldState.DEAD)
    }

    @Test
    fun chipCooledOnTheHeatsinkFirst() =
        Bot(HardwareDemos.chip).rightTo(15.5f).waitCooled('c').right(4f).expect(WorldState.WON)

    @Test
    fun overclockedFloorBurnsWhoStops() {
        val b = Bot(HardwareDemos.overclock).rightTo(16.7f)
        near(0.6f, b.world.heaters['f']!!.heat, 0.05f)
        b.wait(2f).expect(WorldState.DEAD)
        Bot(HardwareDemos.overclock).right(4f).expect(WorldState.WON)
    }

    @Test
    fun heatedTriggerFires() {
        val l = level(listOf(Heat('h', rise = 1f)), listOf(trap(Heated('h', 0.5f), Show('x')))) {
            put(5, 14, 'P'); fill(3..7, 15..15, 'h'); fill(10..10, 10..10, 'x')
        }
        val hidden = Level(l.name, l.intro, legend = mapOf('x' to Glyph(spike = false, hidden = true)), traps = l.traps, start = l.start) {
            for (y in 0 until l.rows) for (x in 0 until l.cols) put(x, y, l.map.grid[y][x])
        }
        val w = World(hidden)
        w.run(0.45f)
        assertFalse(w.group('x').visible)
        w.run(0.1f)
        assertTrue(w.group('x').visible)
    }

    // ---------- fans ----------

    @Test
    fun updraftLiftsToItsSpeedAndHovers() {
        val b = Bot(HardwareDemos.fan).rightTo(12.6f).wait(0.25f)
        val w = b.world
        assertFalse(w.player.grounded)
        // gravity is gone: the player rises at the wind's speed
        near(-9f, w.player.vy, 0.5f)
        b.wait(2f).expect(WorldState.PLAYING)
        // and bobs at the top of the draft
        assertTrue("${w.player.box.cy}", w.player.box.cy in 4f..6f)
    }

    @Test
    fun updraftToTheLedge() =
        Bot(HardwareDemos.fan).rightTo(12.6f).waitFor { it.player.box.cy < 5.6f }.right(3f).expect(WorldState.WON)

    @Test
    fun ledgeIsTooHighWithoutTheFan() {
        val b = Bot(HardwareDemos.fan)
        b.world.fans.clear()
        b.hopR(13f).right(4f).expect(WorldState.PLAYING)
        assertTrue(b.world.player.box.b > 14f)
    }

    @Test
    fun sideWindCarriesTheJump() {
        Bot(HardwareDemos.wind).hopR(8.6f).expect(WorldState.PLAYING)
        Bot(HardwareDemos.wind).hopR(8.6f).right(3f).expect(WorldState.WON)
        val still = Bot(HardwareDemos.wind)
        still.world.fans.clear()
        still.hopR(8.6f).expect(WorldState.DEAD)
    }

    @Test
    fun sideWindDriftsWalkingAndStanding() {
        val l = level(listOf(Fan('w', at = 0 to 12, dir = Dir.RIGHT, reach = 30, speed = 3f, width = 3))) { put(5, 14, 'P') }
        val b = Bot(l).wait(0.2f)
        val x0 = b.world.player.box.cx
        b.wait(1f)
        near(x0 + 3f, b.world.player.box.cx, 0.04f)
    }

    @Test
    fun fanSpinsUpDownAndReverses() {
        val l = level(
            listOf(Fan('w', at = 0 to 9, dir = Dir.RIGHT, reach = 30, speed = 7f)),
            listOf(trap(After(0.5f), FanSet('w', -7f)), trap(After(3f), Power('w', false))),
        ) { put(5, 14, 'P') }
        val w = World(l)
        val f = w.fans[0]
        near(7f, f.wind)
        w.run(0.5f + 7f / Hardware.SPIN)
        near(0f, f.wind, 0.2f)
        w.run(7f / Hardware.SPIN)
        near(-7f, f.wind, 0.2f)
        w.run(2.5f)
        near(0f, f.wind)
    }

    @Test
    fun fanCycleAndHum() {
        val fan = Fan('f', at = 1 to 1, dir = Dir.DOWN, reach = 3, on = 1f, off = 2f)
        assertTrue(fan.runsAt(0.5f)); assertFalse(fan.runsAt(1.5f)); assertTrue(fan.runsAt(3.2f))
        near(1f, fan.x0); near(2f, fan.x1); near(2f, fan.y0); near(5f, fan.y1)
        val l = level(listOf(Fan('f', at = 10 to 15, dir = Dir.UP, reach = 4, speed = 6f, on = 1f, off = 1f))) { put(2, 14, 'P') }
        val w = World(l)
        var hums = 0
        while (w.time < 4.5f) { w.step(Bot.DT, Controls()); hums += w.events.count { it == Event.Hum }; w.events.clear() }
        assertEquals(2, hums)
    }

    @Test
    fun stoppedFanLetsYouFall() {
        val l = level(listOf(Fan('f', at = 5 to 15, dir = Dir.UP, reach = 8, speed = 9f), Power('f', false))) { put(5, 14, 'P') }
        val b = Bot(l).wait(1.5f)
        assertTrue(b.world.player.grounded)
        near(0f, b.world.fans[0].wind)
    }
}
