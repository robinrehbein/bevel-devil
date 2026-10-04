package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Deliberate sloppiness for a [Bot], see [DesignRules.solutionToleratesSlop]: [time] seconds are added to every timed
 * hold ([Bot.right], [Bot.rightJump], [Bot.wait], …) and to [Bot.waitUntil]; every [Bot.rightTo]/[Bot.leftTo] target
 * (and so every [Bot.hopR]/[Bot.hopL] take-off) moves [tiles] tiles further along the direction of travel. Waits on a
 * condition ([Bot.waitFor], [Bot.rightUntil], [Bot.waitWhile], [Bot.fidgetUntil], [Bot.untilSaw], …) react [time]
 * seconds late (the same keys stay down that much longer) or, with negative [time], stop that much before the
 * condition first held: a condition on `player.box.cx` or `world.time` is a target like any other. Negative values
 * make the bot early. Only landing ([Bot.landRight]) is physics, not a decision, and stays exact.
 */
data class Slop(val time: Float = 0f, val tiles: Float = 0f) {
    companion object {
        val NONE = Slop()
    }
}

/**
 * Plays [level], or [round] of it (see [Level.rounds]), as precisely as scripted, or with [slop]. With [skipIdle] every
 * command that stands still (wait, waitUntil, waitFor, waitWhile, untilSaw, fidgetUntil, a jump on the spot) is
 * skipped: [DesignRules.cleanRunViolations] uses it to see whether a solution is padded with idle time.
 */
class Bot(level: Level, round: Int = 0, val slop: Slop = Slop.NONE, val skipIdle: Boolean = false) {
    private val stage = level.rounds[round]
    private var past: Trail? = null
    var world = World(stage)
        private set
    private val input = Controls()
    private val trace = StringBuilder()

    /** Everything that happened to [world] since it was made, so an early [Slop] can play it again up to a cut. */
    private sealed interface Entry {
        data class Step(val left: Boolean, val right: Boolean, val jump: Boolean, val jumpPressed: Boolean, val tilt: Float, val shake: Boolean) : Entry
        data object Pause : Entry
        data object Resume : Entry
    }
    private val log = ArrayList<Entry>()

    private fun step() {
        log += Entry.Step(input.left, input.right, input.jump, input.jumpPressed, input.tilt, input.shake)
        world.step(DT, input)
    }

    /** Rebuilds [world] from the first [cut] entries of [log]. */
    private fun replay(cut: Int) {
        val keep = log.subList(0, cut).toList()
        log.clear()
        world = World(stage, past)
        val c = Controls()
        for (e in keep) {
            when (e) {
                is Entry.Step -> {
                    c.left = e.left; c.right = e.right; c.jump = e.jump; c.jumpPressed = e.jumpPressed; c.tilt = e.tilt; c.shake = e.shake
                    world.step(DT, c)
                }
                Entry.Pause -> world.pausePressed()
                Entry.Resume -> world.resumed()
            }
            log += e
        }
        trace.append("--- early by %.2f s\n".format(-slop.time))
    }

    /**
     * Steps with [keys] until [cond] holds (at most [max] seconds of world time), then applies [slop]: late keeps the
     * keys down for [Slop.time] more, early rewinds to [Slop.time] before the condition first held.
     */
    private fun react(left: Boolean, right: Boolean, max: Float, cond: (World) -> Boolean): Bot {
        if (skipIdle && !left && !right) return this
        val from = log.size
        val end = world.time + max
        input.left = left; input.right = right; input.jump = false
        while (!cond(world) && world.state == WorldState.PLAYING && world.time < end) step()
        slopAfter(from, cond(world)) { input.left = left; input.right = right; input.jump = false; step() }
        return hold(0f, left = left, right = right)
    }

    /** Late: [more] for [Slop.time] seconds after the condition held. Early: rewind to [Slop.time] before it held. */
    private fun slopAfter(from: Int, met: Boolean, more: () -> Unit) {
        if (!met || from >= log.size || slop.time == 0f) return
        val n = kotlin.math.round(kotlin.math.abs(slop.time) / DT).toInt()
        if (slop.time > 0f) {
            val stop = log.size + n
            while (log.size < stop && world.state == WorldState.PLAYING) more()
        } else replay(maxOf(from, log.size - n))
    }

    private fun hold(seconds: Float, left: Boolean = false, right: Boolean = false, jump: Boolean = false, exact: Boolean = false): Bot {
        if (skipIdle && seconds > 0f && !left && !right) return this
        // a sloppy jump is still a jump: it never shrinks below one step
        val seconds = if (exact || seconds <= 0f || slop.time == 0f) seconds
        else maxOf(seconds + slop.time, if (jump) minOf(seconds, DT) else 0f)
        input.left = left
        input.right = right
        input.jump = jump
        input.jumpPressed = jump
        var t = 0f
        while (t < seconds && world.state == WorldState.PLAYING) {
            step()
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
    private fun until(x0: Float, left: Boolean, goingRight: Boolean): Bot {
        val x = if (goingRight) x0 + slop.tiles else x0 - slop.tiles
        input.left = left; input.right = !left; input.jump = false
        while ((if (goingRight) world.player.box.cx < x else world.player.box.cx > x) &&
            world.state == WorldState.PLAYING && world.time < 60f
        ) step()
        return hold(0f, left = left, right = !left)
    }

    /** Hold a key until the player is grounded again (used after a jump command). */
    private fun untilGrounded(left: Boolean, max: Float): Bot {
        input.left = left; input.right = !left; input.jump = false
        var t = 0f
        while (!world.player.grounded && world.state == WorldState.PLAYING && t < max) { step(); t += DT }
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
    fun waitUntil(t0: Float): Bot {
        if (skipIdle) return this
        val t = t0 + slop.time
        input.left = false; input.right = false; input.jump = false
        while (world.time < t && world.state == WorldState.PLAYING) step()
        return hold(0f)
    }

    /** Stand still until a saw is within [d] tiles horizontally of the player (either side). */
    fun untilSaw(d: Float): Bot = react(left = false, right = false, max = 60f) { w ->
        w.saws.any { kotlin.math.abs(it.x - w.player.box.cx) <= d }
    }

    /** Keep running right until a saw ahead of the player is within [d] tiles. */
    fun rightUntilSaw(d: Float): Bot = react(left = false, right = true, max = 60f) { w ->
        w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= d }
    }

    /** Holds the phone at [v] from now on: -1 left edge down .. 1 right edge down. */
    fun tilt(v: Float) = apply { input.tilt = v }

    /** Shakes the phone once, then stands still for [s] seconds. */
    fun shake(s: Float = 0.1f): Bot {
        input.shake = true
        return wait(s)
    }

    /** Stands still until [cond] holds (at most [max] seconds). */
    fun waitFor(max: Float = 10f, cond: (World) -> Boolean): Bot = react(left = false, right = false, max = max, cond = cond)

    /** Holds right (or left with [left]) until [cond] holds, at most [max] seconds: ride a fan or a belt until far enough. */
    fun rightUntil(max: Float = 10f, left: Boolean = false, cond: (World) -> Boolean): Bot = react(left = left, right = !left, max = max, cond = cond)

    fun leftUntil(max: Float = 10f, cond: (World) -> Boolean) = rightUntil(max, left = true, cond)

    /** Stands still until circuit [id] is powered (or dead, with [on] false). */
    fun waitPowered(id: Char, on: Boolean = true, max: Float = 10f) = waitFor(max) { it.circuits[id]?.powered == on }

    /** Stands still until heated group [id] is down to [below] heat, e.g. on a heatsink. */
    fun waitCooled(id: Char, below: Float = 0f, max: Float = 10f) = waitFor(max) { (it.heaters[id]?.heat ?: 0f) <= below }

    /** Like [waitFor], but hops on the spot so that idle triggers never fire. */
    fun fidgetUntil(max: Float = 10f, cond: (World) -> Boolean): Bot {
        if (skipIdle) return this
        val from = log.size
        val end = world.time + max
        while (!cond(world) && world.state == WorldState.PLAYING && world.time < end) hold(0.1f, jump = true, exact = true)
        slopAfter(from, cond(world)) { hold(DT / 2, jump = true, exact = true) }
        return hold(0f)
    }

    fun rightTo(x: Float) = until(x, left = false, goingRight = true)
    fun leftTo(x: Float) = until(x, left = true, goingRight = false)
    /** For swapped controls: press left, move right. */
    fun leftKeyRightTo(x: Float) = until(x, left = true, goingRight = true)
    /** For swapped controls: press right, move left. */
    fun rightKeyLeftTo(x: Float) = until(x, left = false, goingRight = false)

    /** Stand still while [cond] holds (at most [max] seconds), e.g. through a fake win. */
    fun waitWhile(max: Float = 20f, cond: (World) -> Boolean): Bot = react(left = false, right = false, max = max) { !cond(it) }

    /** Taps the HUD pause button; if it really paused, resumes right away. */
    fun tapPause(): Bot {
        log += Entry.Pause
        if (world.pausePressed()) { log += Entry.Resume; world.resumed() }
        return hold(0f)
    }

    /** Pauses (via back, which always works) and resumes. */
    fun pauseResume(): Bot {
        log += Entry.Resume
        world.resumed()
        return hold(0f)
    }

    /** Next attempt after a death, carrying this attempt's trail for a ghost, as the game does. */
    fun retry(): Bot {
        past = world.trail
        world = World(stage, past)
        log.clear()
        trace.append("--- retry\n")
        return this
    }

    fun expect(state: WorldState) {
        assertEquals("${stage.name.en}${if (slop == Slop.NONE) "" else " with $slop"}\n$trace", state, world.state)
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
