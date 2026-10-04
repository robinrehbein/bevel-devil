package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Plays [level], or [round] of it (see [Level.rounds]). */
class Bot(level: Level, round: Int = 0) {
    private val stage = level.rounds[round]
    var world = World(stage)
        private set
    private val input = Controls()
    private val trace = StringBuilder()

    private fun hold(seconds: Float, left: Boolean = false, right: Boolean = false, jump: Boolean = false): Bot {
        input.left = left
        input.right = right
        input.jump = jump
        input.jumpPressed = jump
        var t = 0f
        while (t < seconds && world.state == WorldState.PLAYING) {
            world.step(DT, input)
            t += DT
        }
        val b = world.player.box
        trace.append("t=%.2f x=%.2f y=%.2f grounded=%s state=%s\n".format(world.time, b.cx, b.b, world.player.grounded, world.state))
        return this
    }

    fun right(s: Float) = hold(s, right = true)
    fun left(s: Float) = hold(s, left = true)
    fun wait(s: Float) = hold(s)
    fun rightJump(s: Float) = hold(s, right = true, jump = true)
    fun leftJump(s: Float) = hold(s, left = true, jump = true)
    fun jump(s: Float) = hold(s, jump = true)

    /** Hold a key until the player center passes [x] in the given direction (keys may be swapped). */
    private fun until(x: Float, left: Boolean, goingRight: Boolean): Bot {
        input.left = left; input.right = !left; input.jump = false
        while ((if (goingRight) world.player.box.cx < x else world.player.box.cx > x) &&
            world.state == WorldState.PLAYING && world.time < 60f
        ) world.step(DT, input)
        return hold(0f, left = left, right = !left)
    }

    /** Hold a key until the player is grounded again (used after a jump command). */
    private fun untilGrounded(left: Boolean, max: Float): Bot {
        input.left = left; input.right = !left; input.jump = false
        var t = 0f
        while (!world.player.grounded && world.state == WorldState.PLAYING && t < max) { world.step(DT, input); t += DT }
        return hold(0f, left = left, right = !left)
    }

    /** Keep running right / left until the player lands (max [max] seconds). */
    fun landRight(max: Float = 1.6f) = untilGrounded(left = false, max)
    fun landLeft(max: Float = 1.6f) = untilGrounded(left = true, max)

    /** Run to [x], jump (held [hold] seconds) and keep running until landing. */
    fun hopR(x: Float, hold: Float = 0.35f) = rightTo(x).rightJump(hold).landRight()
    fun hopL(x: Float, hold: Float = 0.35f) = leftTo(x).leftJump(hold).landLeft()
    /** Same as [hopR] while the controls are swapped (pressing left moves right). */
    fun hopS(x: Float, hold: Float = 0.35f) = leftKeyRightTo(x).leftJump(hold).landLeft()
    /** Swapped controls, moving left (pressing right). */
    fun hopSL(x: Float, hold: Float = 0.35f) = rightKeyLeftTo(x).rightJump(hold).landRight()

    /** Stand still until the world clock reaches [t] seconds. */
    fun waitUntil(t: Float): Bot {
        input.left = false; input.right = false; input.jump = false
        while (world.time < t && world.state == WorldState.PLAYING) world.step(DT, input)
        return hold(0f)
    }

    /** Stand still until a saw is within [d] tiles horizontally of the player (either side). */
    fun untilSaw(d: Float): Bot {
        input.left = false; input.right = false; input.jump = false
        while (world.state == WorldState.PLAYING && world.time < 60f &&
            world.saws.none { kotlin.math.abs(it.x - world.player.box.cx) <= d }
        ) world.step(DT, input)
        return hold(0f)
    }

    /** Keep running right until a saw ahead of the player is within [d] tiles. */
    fun rightUntilSaw(d: Float): Bot {
        input.left = false; input.right = true; input.jump = false
        while (world.state == WorldState.PLAYING && world.time < 60f &&
            world.saws.none { it.x > world.player.box.cx && it.x - world.player.box.cx <= d }
        ) world.step(DT, input)
        return hold(0f, right = true)
    }

    /** Holds the phone at [v] from now on: -1 left edge down .. 1 right edge down. */
    fun tilt(v: Float) = apply { input.tilt = v }

    /** Shakes the phone once, then stands still for [s] seconds. */
    fun shake(s: Float = 0.1f): Bot {
        input.shake = true
        return wait(s)
    }

    /** Stands still until [cond] holds (at most [max] seconds). */
    fun waitFor(max: Float = 10f, cond: (World) -> Boolean): Bot {
        input.left = false; input.right = false; input.jump = false
        val end = world.time + max
        while (!cond(world) && world.state == WorldState.PLAYING && world.time < end) world.step(DT, input)
        return hold(0f)
    }

    /** Holds right (or left with [left]) until [cond] holds, at most [max] seconds: ride a fan or a belt until far enough. */
    fun rightUntil(max: Float = 10f, left: Boolean = false, cond: (World) -> Boolean): Bot {
        input.left = left; input.right = !left; input.jump = false
        val end = world.time + max
        while (!cond(world) && world.state == WorldState.PLAYING && world.time < end) world.step(DT, input)
        return hold(0f, left = left, right = !left)
    }

    fun leftUntil(max: Float = 10f, cond: (World) -> Boolean) = rightUntil(max, left = true, cond)

    /** Stands still until circuit [id] is powered (or dead, with [on] false). */
    fun waitPowered(id: Char, on: Boolean = true, max: Float = 10f) = waitFor(max) { it.circuits[id]?.powered == on }

    /** Stands still until heated group [id] is down to [below] heat, e.g. on a heatsink. */
    fun waitCooled(id: Char, below: Float = 0f, max: Float = 10f) = waitFor(max) { (it.heaters[id]?.heat ?: 0f) <= below }

    /** Like [waitFor], but hops on the spot so that idle triggers never fire. */
    fun fidgetUntil(max: Float = 10f, cond: (World) -> Boolean): Bot {
        val end = world.time + max
        while (!cond(world) && world.state == WorldState.PLAYING && world.time < end) hold(0.1f, jump = true)
        return hold(0f)
    }

    fun rightTo(x: Float) = until(x, left = false, goingRight = true)
    fun leftTo(x: Float) = until(x, left = true, goingRight = false)
    /** For swapped controls: press left, move right. */
    fun leftKeyRightTo(x: Float) = until(x, left = true, goingRight = true)
    /** For swapped controls: press right, move left. */
    fun rightKeyLeftTo(x: Float) = until(x, left = false, goingRight = false)

    /** Stand still while [cond] holds (at most [max] seconds), e.g. through a fake win. */
    fun waitWhile(max: Float = 20f, cond: (World) -> Boolean): Bot {
        input.left = false; input.right = false; input.jump = false
        var t = 0f
        while (cond(world) && world.state == WorldState.PLAYING && t < max) { world.step(DT, input); t += DT }
        return hold(0f)
    }

    /** Taps the HUD pause button; if it really paused, resumes right away. */
    fun tapPause(): Bot {
        if (world.pausePressed()) world.resumed()
        return hold(0f)
    }

    /** Pauses (via back, which always works) and resumes. */
    fun pauseResume(): Bot {
        world.resumed()
        return hold(0f)
    }

    /** Next attempt after a death, carrying this attempt's trail for a ghost, as the game does. */
    fun retry(): Bot {
        world = World(stage, world.trail)
        trace.append("--- retry\n")
        return this
    }

    fun expect(state: WorldState) {
        assertEquals("${stage.name.en}\n$trace", state, world.state)
    }

    companion object {
        const val DT = 1f / 120f
    }
}

class LevelsTest {
    private fun bot(i: Int) = Bot(Levels.all[i - 1])

    @Test
    fun allLevelsWithSpawnAndDoor() {
        assertTrue(Levels.all.size >= 12)
        Levels.all.forEach { World(it) }
    }

    @Test
    fun standingStillIsSafeAtTheStart() {
        // levels that punish idling on purpose are the exception
        Levels.all.filter { l -> l.traps.none { it.trigger is Trigger.Idle } }.forEach { l -> Bot(l).wait(2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun everyLevelPlaysACard() {
        // a bluff deals the Bluff card
        val played = Levels.all.flatMap { it.rounds }.flatMap { l -> l.traps.flatMap { it.actions } }
            .mapNotNull { a -> if (a is Action.Play) a.card else if (a is Action.Bluff) Card.BLUFF else null }
        // ANNEX belongs to the U18 levels ("who says the room ends here?", docs/LEVEL_DESIGN_V2.md), which are not
        // built yet: until then only the test demos deal it. Remove it from here as soon as one level plays it.
        val pending = setOf(Card.ANNEX)
        assertEquals(Card.entries.toSet() - pending, played.toSet() - pending)
        val demos = RoomDemos.all.flatMap { l -> l.traps.flatMap { it.actions } }.filterIsInstance<Action.Play>().map { it.card }
        assertTrue(pending.all { it in played || it in demos })
    }

    @Test
    fun naiveRunIsPunished() {
        for (i in listOf(1, 2, 4, 6, 16)) bot(i).right(8f).expect(WorldState.DEAD)
    }

    @Test
    fun cardsAreRememberedPerAttempt() {
        val b = bot(1).right(8f)
        b.expect(WorldState.DEAD)
        assertTrue(b.world.lastCard == Card.COLLAPSE)
    }
}
