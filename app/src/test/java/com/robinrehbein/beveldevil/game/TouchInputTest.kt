package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchInputTest {
    private fun input(scheme: Scheme = Scheme.BUTTONS, lefty: Boolean = false) = TouchInput().apply {
        width = 2000f; density = 2f; splitX = if (lefty) 1500f else 500f
        this.scheme = scheme; leftHanded = lefty
    }

    private fun TouchInput.state() = Triple(left, right, jump)

    @Test fun buttonsSplitAtMidpointAndHalf() {
        val t = input()
        t.down(1, 100f, 900f); assertEquals(Triple(true, false, false), t.state())
        t.up(1); t.down(1, 900f, 100f); assertEquals(Triple(false, true, false), t.state())
        t.up(1); t.down(1, 1200f, 5f)
        assertEquals(Triple(false, false, true), t.state()); assertTrue(t.jumpPressed)
        t.up(1); assertEquals(Triple(false, false, false), t.state())
    }

    @Test fun multiTouchMoveAndJump() {
        val t = input()
        t.down(1, 100f, 900f); t.down(2, 1800f, 900f)
        assertEquals(Triple(true, false, true), t.state())
        t.up(1); assertEquals(Triple(false, false, true), t.state())
    }

    @Test fun rollingThumbLastPressedWins() {
        val t = input()
        t.down(1, 100f, 900f)
        t.down(2, 900f, 900f)
        assertEquals(Triple(false, true, false), t.state())
        t.up(2); assertEquals(Triple(true, false, false), t.state())
        t.down(2, 900f, 900f); t.up(1)
        assertEquals(Triple(false, true, false), t.state())
    }

    @Test fun slidingSwitchesImmediately() {
        val t = input()
        t.down(1, 100f, 900f); t.move(1, 700f)
        assertEquals(Triple(false, true, false), t.state())
        t.move(1, 200f); assertEquals(Triple(true, false, false), t.state())
    }

    @Test fun movementFingerNeverBecomesJump() {
        val t = input()
        t.down(1, 100f, 900f); t.jumpPressed = false
        t.move(1, 1900f)
        assertEquals(Triple(false, true, false), t.state()); assertFalse(t.jumpPressed)
        t.up(1); t.down(2, 1900f, 900f); t.jumpPressed = false
        t.move(2, 50f)
        assertEquals(Triple(false, false, true), t.state()); assertFalse(t.jumpPressed)
    }

    @Test fun leftHandedSwap() {
        val t = input(lefty = true)
        t.down(1, 100f, 900f); assertEquals(Triple(false, false, true), t.state())
        t.up(1); t.down(1, 1400f, 900f); assertEquals(Triple(true, false, false), t.state())
        t.up(1); t.down(1, 1900f, 900f); assertEquals(Triple(false, true, false), t.state())
    }

    @Test fun stickDeadZoneAndDirection() {
        val t = input(Scheme.STICK)
        t.down(1, 400f, 700f)
        assertEquals(Triple(false, false, false), t.state()); assertTrue(t.stickActive)
        t.move(1, 400f + 15f); assertEquals(Triple(false, false, false), t.state())
        t.move(1, 400f + 17f); assertEquals(Triple(false, true, false), t.state())
        t.move(1, 400f - 17f); assertEquals(Triple(true, false, false), t.state())
        t.up(1); assertFalse(t.stickActive); assertEquals(Triple(false, false, false), t.state())
    }

    @Test fun stickBaseFollowsFinger() {
        val t = input(Scheme.STICK)
        t.down(1, 400f, 700f); t.move(1, 400f + 300f)
        assertEquals(t.stickRadius, t.knobX - t.stickX, 0.01f)
        t.move(1, 400f + 300f - 20f) // moving back a little still reads as right
        assertTrue(t.right)
        t.move(1, 400f + 300f - t.stickRadius - 40f); assertTrue(t.left)
    }

    @Test fun stickHalfIsMovementAndOtherHalfJump() {
        val t = input(Scheme.STICK)
        t.down(1, 300f, 100f); t.down(2, 1500f, 100f)
        assertTrue(t.jump); assertTrue(t.jumpPressed)
        val l = input(Scheme.STICK, lefty = true)
        l.down(1, 300f, 100f); assertTrue(l.jump)
    }

    @Test fun cancelClears() {
        val t = input(); t.down(1, 100f, 100f); t.down(2, 1800f, 100f); t.cancel()
        assertEquals(Triple(false, false, false), t.state()); assertFalse(t.jumpPressed)
    }
}
