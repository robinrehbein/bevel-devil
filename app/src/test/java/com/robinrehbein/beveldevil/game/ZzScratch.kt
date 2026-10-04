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
    fun go() = slopTrace(15)
}
