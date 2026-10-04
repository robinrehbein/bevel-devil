package com.robinrehbein.beveldevil.game

import org.junit.Test

class ZzScratch {
    private fun show(b: Bot, tag: String) {
        val w = b.world
        println("%-10s t=%.2f x=%.2f y=%.2f %s grav=%s".format(tag, w.time, w.player.box.cx, w.player.box.cy, w.state, w.gravity))
    }

    private fun sawStr(b: Bot) = b.world.saws.map { "%.1f,%.1f".format(it.x, it.y) }

    private fun slopTrace(n: Int, round: Int = 0) {
        val l = World2.levels[n - 1]
        val sol = World2DesignTest.SOLUTIONS.getValue(n)[round]
        for (sl in DesignRules.SLOPS) {
            val b = DesignRules.play(l, round, sol, sl)
            try { b.expect(WorldState.WON) } catch (e: AssertionError) { println("SLOP $sl: " + e.message) }
        }
    }

    @Test
    fun go() {
        val b = Bot(World2.levels[13], 1)
        b.right(1.65f).hopL(9.2f, 0.5f).waitFor { it.links[2].to.first == 30 }.left(0.65f); show(b, "TR")
        b.leftTo(24.1f).wait(0.45f).hopL(23.5f, 0.5f); show(b, "hopH")
        b.leftUntil { it.player.box.cy > 10f }; show(b, "BR")
        b.left(0.4f); show(b, "edge")
        b.leftJump(0.5f); show(b, "j1")
        b.landLeft(); show(b, "onK")
        b.leftJump(0.5f); show(b, "j2")
        b.landLeft(); show(b, "off")
        b.left(1f); show(b, "end")
    }
}
