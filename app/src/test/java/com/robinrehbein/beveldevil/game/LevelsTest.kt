package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Plays scripted inputs through a level with the real physics. */
class Bot(private val level: Level) {
    val world = World(level)
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

    fun rightTo(x: Float) = until(x, left = false, goingRight = true)
    fun leftTo(x: Float) = until(x, left = true, goingRight = false)
    /** For swapped controls: press left, move right. */
    fun leftKeyRightTo(x: Float) = until(x, left = true, goingRight = true)
    /** For swapped controls: press right, move left. */
    fun rightKeyLeftTo(x: Float) = until(x, left = false, goingRight = false)

    fun expect(state: WorldState) {
        assertEquals("${level.name.en}\n$trace", state, world.state)
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
        Levels.all.forEach { l -> Bot(l).wait(2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun everyLevelPlaysACard() {
        val played = Levels.all.flatMap { l -> l.traps.flatMap { it.actions }.filterIsInstance<Action.Play>().map { it.card } }
        assertEquals(Card.entries.toSet(), played.toSet())
    }

    @Test
    fun naiveRunIsPunished() {
        for (i in listOf(1, 2, 4, 7, 12)) bot(i).right(8f).expect(WorldState.DEAD)
    }

    @Test fun level1() = bot(1).rightTo(17.4f).rightJump(0.35f).right(3f).expect(WorldState.WON)

    @Test fun level2() = bot(2).rightTo(8.6f).rightJump(0.3f).rightTo(22.4f).rightJump(0.3f).right(3f).expect(WorldState.WON)

    @Test
    fun level3() = bot(3).rightTo(26f).wait(0.4f)
        .leftTo(24.9f).leftJump(0.35f).wait(0.3f)
        .leftTo(20.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(14.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(8.7f).leftJump(0.35f).left(1.5f)
        .expect(WorldState.WON)

    @Test
    fun level4() = bot(4).rightTo(12.9f).wait(1.2f)
        .rightTo(13.2f).rightJump(0.35f).right(0.3f).wait(0.2f)
        .rightTo(21.4f).wait(1.2f).rightTo(21.9f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level5() = bot(5).rightTo(10f).wait(1f)
        .rightTo(15.8f).rightJump(0.25f).rightTo(20.8f).rightJump(0.25f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level6() = bot(6).rightTo(7.2f)
        .leftKeyRightTo(11.2f).leftJump(0.35f).left(0.3f).leftKeyRightTo(17.8f)
        .rightTo(20.3f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level7() = bot(7).rightTo(12.8f).rightJump(0.3f).rightTo(20.8f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level8() = bot(8).rightTo(18.5f).jump(0.3f).wait(0.5f)
        .leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level9() = bot(9).rightTo(6.6f).rightJump(0.3f).rightTo(11.6f).rightJump(0.3f)
        .rightTo(16.4f).rightJump(0.3f).rightTo(21.3f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)

    @Test
    fun level10() = bot(10).rightTo(22.5f).wait(1.5f).leftTo(19.2f).leftJump(0.3f).leftTo(13.9f).leftJump(0.3f).left(3f)
        .expect(WorldState.WON)

    @Test
    fun level11() = bot(11).right(4.5f).expect(WorldState.WON)

    @Test
    fun level12() = bot(12).rightTo(7.3f).rightJump(0.35f).rightTo(15.4f).rightJump(0.3f).rightTo(24.7f).wait(1.2f).left(3f)
        .expect(WorldState.WON)

    @Test
    fun cardsAreRememberedPerAttempt() {
        val b = bot(1).right(8f)
        b.expect(WorldState.DEAD)
        assertTrue(b.world.lastCard == Card.COLLAPSE)
    }
}
