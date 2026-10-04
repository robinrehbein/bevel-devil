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

    /** A copy of [base] (one round) whose fan-1 trap uses [saw]. */
    private fun withSaw(base: Level, saw: Action.PathSaw): Level {
        val traps = base.traps.map { t ->
            if (t.actions.any { it is Action.PathSaw && it.loop }) Trap(t.trigger, t.actions.map { if (it is Action.PathSaw && it.loop) saw else it }, t.delay) else t
        }
        return Level(base.name, base.intro, base.legend, traps, base.start, hint = base.hint) {
            for (y in 0 until base.rows) for (x in 0 until base.cols) put(x, y, base.map.grid[y][x])
        }
    }

    @Test
    fun go() {
        val base = World2.levels[10].rounds[1]
        for (delay in listOf(0.4f, 0.6f, 0.8f, 1.0f, 1.2f)) {
            for (sp in listOf(5f, 6f)) {
                val saw = Action.PathSaw(sp, 5.5f to 10.3f, 8.5f to 10.3f, 8.5f to 6.5f, 5f to 6.5f, 5f to 10.3f, loop = true, delay = delay)
                val l = withSaw(base, saw)
                val imm = Bot(l).hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft().leftTo(2.2f)
                val land = Bot(l).hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft().world.time
                var t = land
                val ok1 = ArrayList<Float>()
                while (t < land + 3f) {
                    val b = Bot(l).hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft().waitUntil(t).leftTo(2.2f)
                    if (b.world.state == WorldState.PLAYING) ok1.add(t)
                    t += 0.05f
                }
                println("delay=$delay sp=$sp land=%.2f immDead=${imm.world.state == WorldState.DEAD} firstOk=${ok1.firstOrNull()} lastOk=${ok1.lastOrNull()} count=${ok1.size}".format(land))
                val t1 = ok1.firstOrNull() ?: continue
                val use = t1 + 0.1f
                val pre = { Bot(l).hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft().waitUntil(use).leftTo(2.2f) }
                val tp = pre().world.time
                val ok2 = ArrayList<Float>()
                t = tp
                while (t < tp + 3.5f) {
                    val b = pre().waitUntil(t).rightTo(12.6f)
                    if (b.world.state == WorldState.PLAYING) ok2.add(t)
                    t += 0.05f
                }
                val wins = ArrayList<String>()
                var start: Float? = null
                var prev = 0f
                for (v in ok2) { if (start == null) start = v else if (v - prev > 0.06f) { wins.add("%.2f-%.2f".format(start, prev)); start = v }; prev = v }
                if (start != null) wins.add("%.2f-%.2f".format(start, prev))
                println("   pad at %.2f; safe return starts: %s".format(tp, wins))
            }
        }
    }
}
