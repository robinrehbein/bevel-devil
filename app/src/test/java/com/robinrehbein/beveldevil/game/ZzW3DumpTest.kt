package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Test

/** Scratch: dumps the World 3 maps (delete before commit). */
class ZzW3DumpTest {
    private fun only(): Set<Int> = try {
        File("build/w3lv.txt").readText().split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
    } catch (_: Throwable) { emptySet() }

    /** A copy of NaiveProbes.drive with a trace (scratch). */
    private fun drive(stage: Level, dir0: Int, greedy: Boolean, zigzag: Boolean = false, edges: Boolean = true, sb: StringBuilder) {
        var dir = dir0
        var groundY = Float.NaN
        var turned = false
        val w = World(stage)
        val c = Controls()
        var jumpT = 0f
        val hist = ArrayDeque<Float>()
        val seenQ = ArrayDeque<Boolean>()
        val reactSteps = Math.round(NaiveProbes.REACT / Bot.DT)
        var steps = 0
        fun solidAt(x: Float, y: Float) = w.pieces.any { it.solid && x >= it.box.x && x <= it.box.r && y >= it.box.y && y <= it.box.b }
        while (w.state == WorldState.PLAYING && w.time < 20f && steps++ < 4000) {
            c.left = dir < 0; c.right = dir > 0; c.jump = false; c.jumpPressed = false
            if (greedy && dir != 0) {
                val p = w.player
                val b = p.box
                val lead = kotlin.math.abs(p.vx) * NaiveProbes.REACT
                val seen = p.grounded && run {
                    val footY = if (w.gravity >= 0f) b.b + 0.25f else b.y - 0.25f
                    val edge = !solidAt(b.cx + dir * (b.w / 2 + 0.35f + lead), footY)
                    val x0 = if (dir > 0) b.r + lead else b.x - 1.3f - lead
                    (edges && edge) || w.pieces.any { it.spike && it.visible && it.box.r > x0 && it.box.x < x0 + 1.3f && it.box.b > b.y - 0.2f && it.box.y < b.b + 0.3f }
                }
                seenQ.addLast(seen)
                val react = if (seenQ.size > reactSteps) seenQ.removeFirst() else false
                if (jumpT > 0f) { c.jump = true; jumpT -= Bot.DT }
                else if (p.grounded) {
                    val blocked = hist.size >= 6 && kotlin.math.abs(hist.last() - hist.first()) < 0.02f
                    if (blocked || react) { c.jump = true; c.jumpPressed = true; jumpT = 0.35f - Bot.DT; hist.clear(); seenQ.clear(); sb.append("  jump t=%.2f x=%.2f y=%.2f blocked=%s\n".format(w.time, b.cx, b.b, blocked)) }
                }
            }
            w.step(Bot.DT, c)
            if (zigzag && !w.player.grounded && !turned && groundY.isFinite() && (w.player.box.b - groundY) * (if (w.gravity >= 0f) 1f else -1f) > 1.5f) {
                dir = -dir; hist.clear(); turned = true; sb.append("  turn t=%.2f x=%.2f\n".format(w.time, w.player.box.cx))
            }
            if (w.player.grounded) { groundY = w.player.box.b; turned = false }
            hist.addLast(w.player.box.cx)
            if (hist.size > 6) hist.removeFirst()
            if (steps % 15 == 0) sb.append("  t=%.2f x=%.2f y=%.2f g=%s\n".format(w.time, w.player.box.cx, w.player.box.b, w.player.grounded))
        }
        sb.append("  end ${w.state} t=%.2f x=%.2f y=%.2f\n".format(w.time, w.player.box.cx, w.player.box.b))
    }

    @Test
    fun trace() {
        val f = File("build/w3trace.txt")
        if (!f.exists()) return
        val (n, r, kind) = f.readText().trim().split(' ')
        val stage = World3.levels[n.toInt() - 1].rounds[r.toInt() - 1]
        val sb = StringBuilder("trace $n $r $kind\n")
        when (kind) {
            "P1R" -> drive(stage, 1, false, sb = sb); "P1L" -> drive(stage, -1, false, sb = sb)
            "P2R" -> drive(stage, 1, true, sb = sb); "P2L" -> drive(stage, -1, true, sb = sb)
            "Z1R" -> drive(stage, 1, true, true, false, sb); "Z1L" -> drive(stage, -1, true, true, false, sb)
            "Z2R" -> drive(stage, 1, true, true, true, sb); "Z2L" -> drive(stage, -1, true, true, true, sb)
        }
        File("build/reports/w3trace.txt").writeText(sb.toString())
    }

    @Test
    fun hopTrace() {
        val f = File("build/w3hop.txt")
        if (!f.exists()) return
        val (n, r, rh, wt) = f.readText().trim().split(' ')
        val l = World3.levels[n.toInt() - 1]
        var last = -1f
        val tr = StringBuilder("hop $n $r $rh $wt\n")
        val probe = Bot.Probe(Bot.Probe.Kind.WAIT, 0f) { bb -> val w = bb.world; if (w.time - last >= 0.1f) { last = w.time; tr.append(" %.1f:%.2f,%.2f".format(w.time, w.player.box.cx, w.player.box.b)) }; false }
        val bot = Bot(l, r.toInt() - 1, probe = probe)
        val wait = wt.toFloat(); val rhythm = rh.toFloat()
        if (wait > 0f) bot.wait(wait)
        val jump = minOf(0.35f, rhythm / 2)
        while (bot.world.state == WorldState.PLAYING && bot.world.time < wait + 10f) bot.rightJump(jump).right(rhythm - jump)
        tr.append("\nend ${bot.world.state} t=%.2f\n".format(bot.world.time))
        File("build/reports/w3hop.txt").writeText(tr.toString())
    }

    @Test
    fun dump() {
        val only = only()
        val sb = StringBuilder()
        World3.levels.forEachIndexed { i, l ->
            val n = i + 1
            if (only.isNotEmpty() && n !in only) return@forEachIndexed
            l.rounds.forEachIndexed { r, round ->
                sb.append("=== 3-$n-${r + 1} ${l.name.en}\n")
                val g = round.map.grid
                sb.append("    " + (0 until round.cols).joinToString("") { (it % 10).toString() } + "\n")
                g.forEachIndexed { y, row -> sb.append("%3d ".format(y)).append(String(row)).append('\n') }
                val sol = World3DesignTest.SOLUTIONS[n]?.getOrNull(r)
                if (sol != null) {
                    try {
                        val b = DesignRules.play(l, r, sol)
                        sb.append("clean: ${b.world.state} t=%.2f x=%.2f y=%.2f\n".format(b.world.time, b.world.player.box.cx, b.world.player.box.b))
                        sb.append(DesignRules.timeline(l, r, sol)).append('\n')
                        if (b.world.state != WorldState.WON || File("build/w3always.txt").exists()) {
                            val bot = Bot(l, r)
                            var last = -1f
                            val probe = Bot.Probe(Bot.Probe.Kind.WAIT, 0f) { bb -> val w = bb.world; if (w.time - last >= 0.1f) { last = w.time; sb.append("   t=%.2f x=%.2f y=%.2f g=%s\n".format(w.time, w.player.box.cx, w.player.box.b, w.player.grounded)) }; false }
                            try { Bot(l, r, probe = probe).apply(sol) } catch (_: Throwable) {}
                        }
                    } catch (t: Throwable) { sb.append("clean threw $t\n") }
                }
                if (only.isNotEmpty() && sol != null && File("build/w3slop.txt").exists()) {
                    for (sl in DesignRules.SLOPS) {
                        var last = -1f
                        val tr = StringBuilder()
                        val probe = Bot.Probe(Bot.Probe.Kind.WAIT, 0f) { bb -> val w = bb.world; if (w.time - last >= 0.2f) { last = w.time; tr.append(" %.1f:%.2f,%.2f".format(w.time, w.player.box.cx, w.player.box.b)); val gx = File("build/w3group.txt").takeIf { it.exists() }?.readText()?.trim(); if (gx != null) gx.forEach { c -> tr.append("[$c=%.1f]".format(w.group(c).let { it.homeX + it.ox })) } }; false }
                        val bb = try { Bot(l, r, sl, probe = probe).apply(sol) } catch (t: Throwable) { null }
                        sb.append("SLOP ${sl.time} ${bb?.world?.state}:").append(tr).append('\n')
                    }
                }
                if (only.isNotEmpty() && sol != null) {
                    try {
                        val min = if (n % 16 == 0) 10f else 4f
                        (DesignRules.slopViolations(l, r, sol) + DesignRules.teethViolations(l, r, sol) + DesignRules.densityViolations(l, r, sol, min) +
                            DesignRules.fillerDeathViolations(l, r) + DesignRules.holdRightWithHopsViolations(l, r)).forEach { sb.append("CHECK ").append(it).append('\n') }
                    } catch (t: Throwable) { sb.append("CHECK threw $t\n") }
                }
                if (only.isNotEmpty()) {
                    try {
                        val m = NaiveProbes.measure(3, n, l, r, World3DesignTest.SOLUTIONS[n], verbose = true)
                        sb.append(m.line).append('\n').append(m.verbose ?: "").append('\n')
                    } catch (t: Throwable) { sb.append("probe threw $t\n") }
                }
            }
        }
        File("build/reports/w3dump.txt").writeText(sb.toString())
    }
}
