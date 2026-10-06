package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Test

/** Scratch probe for block B (deleted before the end): writes every rule's findings for the levels with a solution. */
class World3BProbe {
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    @Test
    fun probe() {
        val out = StringBuilder()
        val only = File("/tmp/claude-0/pr/only.txt").takeIf { it.exists() }?.readText()?.trim()?.split(",")?.map { it.trim().toInt() }
        val levels = World3.levels
        val mine = World3RoomsB.solutions.keys.sorted().filter { only == null || it in only }
        val allMine = World3RoomsB.solutions.keys.sorted()
        val sols = World3RoomsA.solutions + World3RoomsB.solutions
        for (n in mine) {
            val l = levels[n - 1]
            out.append("===== $n ${l.name.en}  rooms=${l.rooms}\n")
            if (only != null) out.append(l.map.ascii()).append('\n')
            val ss = sols.getValue(n)
            for (r in l.rounds.indices) {
                val s = ss.getOrNull(r)
                if (s == null) { out.append("round ${r + 1}: NO SOLUTION\n"); continue }
                fun rule(name: String, v: () -> List<String>) {
                    val res = try { v() } catch (e: Throwable) { listOf("THREW $e") }
                    if (res.isNotEmpty()) out.append("  [$name r${r + 1}] ${res.joinToString("\n    ")}\n")
                }
                try {
                    val bot = DesignRules.play(l, r, s)
                    out.append("  sprung: ${bot.world.sprung.joinToString { "%.2f:%s".format(it.time, it.trap.trigger.toString().substringAfterLast('.').take(40)) }}\n")
                    if (bot.world.state != WorldState.WON) {
                        try { bot.expect(WorldState.WON) } catch (e: AssertionError) { out.append("  NOT WON r${r + 1}:\n${e.message}\n") }
                        val d = bot.world.door
                        out.append("  door at x=%.2f y=%.2f moving=%s; player x=%.2f y=%.2f t=%.2f\n".format(d.box.x, d.box.y, d.moving, bot.world.player.box.cx, bot.world.player.box.b, bot.world.time))
                        continue
                    }
                } catch (e: Throwable) { out.append("  play THREW $e\n"); continue }
                try { out.append("  r${r + 1}: ${DesignRules.timeline(l, r, s)}\n") } catch (e: Throwable) { out.append("  timeline THREW $e\n") }
                rule("H3") { DesignRules.densityViolations(l, r, s, DesignRules.minDuration(3, n, World3DesignTest.DESIGN[n])) }
                rule("H2") { DesignRules.holdRightWithHopsViolations(l, r) }
                rule("H7") { DesignRules.slopViolations(l, r, s) }
                rule("H15") { DesignRules.teethViolations(l, r, s) }
                rule("H17") { DesignRules.fillerDeathViolations(l, r) }
                try { out.append("  sig r${r + 1}: ${DesignRules.signature(l, s, r)}\n") } catch (e: Throwable) { out.append("  sig THREW $e\n") }
            }
            fun rule(name: String, v: () -> List<String>) {
                val res = try { v() } catch (e: Throwable) { listOf("THREW $e") }
                if (res.isNotEmpty()) out.append("  [$name] ${res.joinToString("\n    ")}\n")
            }
            rule("H9") { DesignRules.rematchViolations(l, ss) }
            rule("H13") { DesignRules.familyViolations(n, l) }
            rule("H8") { DesignRules.cardLintViolations(l) }
        }
        // slop debug: "n r early|late" prints the trace of the solution played sloppily
        File("/tmp/claude-0/pr/slop.txt").takeIf { it.exists() }?.readText()?.trim()?.split("\n")?.forEach { line ->
            val p = line.trim().split(" ")
            val n = p[0].toInt(); val r = p[1].toInt() - 1
            val slop = if (p[2] == "early") Slop(-DesignRules.SLOP_TIME, -DesignRules.SLOP_TILES) else Slop(DesignRules.SLOP_TIME, DesignRules.SLOP_TILES)
            val b = DesignRules.play(levels[n - 1], r, sols.getValue(n)[r], slop)
            out.append("--- slop ${p[2]} ${levels[n - 1].name.en}: ${b.world.state} t=%.2f x=%.2f\n".format(b.world.time, b.world.player.box.cx))
            try { b.expect(WorldState.WON) } catch (e: AssertionError) { out.append(e.message).append('\n') }
        }
        // free-form debug: the solution of "n r" up to the first time its prefix script ends is not available, so dbg2.txt holds "n r count"
        // = play the registered solution to the end but print the world every 0.1 s when replaying with a left-hold afterwards (see below)
        File("/tmp/claude-0/pr/dbg2.txt").takeIf { it.exists() }?.readText()?.trim()?.split("\n")?.forEach { line ->
            val p = line.trim().split(" ")
            val n = p[0].toInt(); val r = p[1].toInt() - 1
            val l = levels[n - 1]
            val bot = Bot(l, r)
            out.append("--- dbg2 ${l.name.en}\n")
            Dbg2.run(n, r, bot, out)
        }
        // ablation debug: "abl n r" lines in abl.txt print, per trap, which probe ends differently without it
        File("/tmp/claude-0/pr/abl.txt").takeIf { it.exists() }?.readText()?.trim()?.split("\n")?.forEach { line ->
            val p = line.trim().split(" ")
            val n = p[0].toInt(); val r = p[1].toInt() - 1
            val l = levels[n - 1]; val s = sols.getValue(n)[r]
            val stage = l.rounds[r]
            val clean = DesignRules.play(l, r, s)
            val fired = clean.moments.flatMap { m -> m.parts.map { it.trap to m.triggered } }.toMap()
            out.append("--- ablation of ${l.name.en} r${r + 1}\n")
            for ((i, t) in stage.traps.withIndex()) {
                val cut = DesignRules.without(stage, setOf(t))
                val probes = listOf<Pair<String, (Level, Int) -> Bot>>(
                    "clean" to { lv, rr -> DesignRules.play(lv, rr, s) },
                    "brisk" to { lv, rr -> Bot(lv, rr, skipIdle = true).apply(s) },
                    "hold" to { lv, rr -> DesignRules.naive(lv, rr, null) },
                    "hop.7" to { lv, rr -> DesignRules.naive(lv, rr, 0.7f) },
                    "wait8" to { lv, rr -> DesignRules.naive(lv, rr, null, DesignRules.PROBE_WAIT) },
                )
                val diffs = probes.filter { (_, f) -> DesignRules.outcome { f(l, r) } != DesignRules.outcome { f(cut, 0) } }.map { it.first }.toMutableList()
                val at = fired[t]
                if (at != null) {
                    if (DesignRules.outcome { DesignRules.patientProbe(l, r, s, at) } != DesignRules.outcome { DesignRules.patientProbe(cut, 0, s, at) }) diffs += "patient"
                    if (DesignRules.outcome { DesignRules.recklessProbe(l, r, s, at) } != DesignRules.outcome { DesignRules.recklessProbe(cut, 0, s, at) }) diffs += "reckless"
                    val pa = DesignRules.patientProbe(l, r, s, at)
                    val pac = DesignRules.patientProbe(cut, 0, s, at)
                    out.append("   trap $i patient: with %s x=%.1f y=%.1f t=%.1f / without %s x=%.1f y=%.1f t=%.1f\n".format(pa.world.state, pa.world.player.box.cx, pa.world.player.box.b, pa.world.time, pac.world.state, pac.world.player.box.cx, pac.world.player.box.b, pac.world.time))
                    val pr = DesignRules.recklessProbe(l, r, s, at)
                    val pc = DesignRules.recklessProbe(cut, 0, s, at)
                    out.append("   trap $i at t=%.2f reckless: with %s x=%.1f t=%.1f / without %s x=%.1f t=%.1f\n".format(at, pr.world.state, pr.world.player.box.cx, pr.world.time, pc.world.state, pc.world.player.box.cx, pc.world.time))
                }
                out.append("  trap $i ${t.trigger.toString().substringAfterLast('.').take(30)} ${t.actions.filterNot { it is Action.Say || it is Action.Play }.joinToString("+") { it::class.simpleName ?: "?" }}: differs in $diffs\n")
            }
        }
        // debug: "n r rhythm wait groups" in dbg.txt replays the naive player and prints a line per step of it
        File("/tmp/claude-0/pr/dbg.txt").takeIf { it.exists() }?.readText()?.trim()?.split("\n")?.forEach { line ->
            val p = line.trim().split(" ")
            val l = levels[p[0].toInt() - 1]
            val round = p[1].toInt() - 1
            val rhythm = p[2].toFloat()
            val wait = p[3].toFloat()
            val groups = p.getOrElse(4) { "" }
            out.append("--- naive ${l.name.en} r${round + 1} rhythm $rhythm wait $wait\n")
            val bot = Bot(l, round)
            if (wait > 0f) bot.wait(wait)
            val end = wait + 12f
            while (bot.world.state == WorldState.PLAYING && bot.world.time < end) {
                if (rhythm <= 0f) bot.right(0.1f) else { val j = minOf(0.35f, rhythm / 2); bot.rightJump(j); bot.right(rhythm - j) }
                val b = bot.world.player.box
                out.append("t=%.2f x=%.2f y=%.2f g=%s %s %s\n".format(bot.world.time, b.cx, b.b, bot.world.player.grounded, bot.world.state, groups.map { c -> "$c@%.1f".format(gx(bot.world, c)) }))
            }
        }
        // set-wide
        val scopeNums = (1..8).toList() + allMine
        val lv = scopeNums.associateWith { levels[it - 1] }
        fun set(name: String, v: () -> List<String>) {
            val res = try { v() } catch (e: Throwable) { listOf("THREW $e") }
            out.append("[$name] ${if (res.isEmpty()) "ok" else res.joinToString("\n    ")}\n")
        }
        set("H12") { DesignRules.rotationViolations(lv) }
        set("H5") { DesignRules.spikeQuotaViolations(lv) + DesignRules.spikeQuotaViolations(lv, DesignRules::heatSpikeFinaleCount, "HeatSpike finales") }
        set("H19") { DesignRules.sayViolations(lv) }
        DesignRules.sayNgramWarnings(lv).forEach { out.append("H19 warning: $it\n") }
        set("cards") { DesignRules.cardSpreadViolations(lv) }
        val sigs = scopeNums.filter { sols[it] != null }.associateWith { n -> sols.getValue(n).withIndex().filter { it.index < levels[n - 1].rounds.size }.map { (r, s) -> DesignRules.signature(levels[n - 1], s, r) } }
        set("H20") { DesignRules.adjacentViolations(sigs) }
        DesignRules.adjacentReport(sigs).filter { it.startsWith("levels 8 ") || it.startsWith("levels 9") || it.startsWith("levels 1") }.forEach { out.append("H20 report: $it\n") }
        set("H21") { DesignRules.wallMoveViolations(sigs) }
        val block = DesignRules.ROLLOUT.first { it.world == 3 && it.id == "B" }
        if (block.levels.all { it in allMine }) {
            set("budget") { DesignRules.budgetViolations(block, block.levels.sorted().associateWith { levels[it - 1] }, block.levels.sorted().associateWith { sigs.getValue(it) }) }
        } else out.append("[budget] skipped, block incomplete: ${block.levels.filter { it !in allMine }}\n")
        File("/tmp/claude-0/pr/probe.txt").writeText(out.toString())
    }
}

/** Hand-written debugging scripts for the probe (scratch). */
object Dbg2 {
    fun run(n: Int, r: Int, bot: Bot, out: StringBuilder) {
        fun log(tag: String) {
            val b = bot.world.player.box
            out.append("%s t=%.2f x=%.2f y=%.2f g=%s %s saws=%s\n".format(tag, bot.world.time, b.cx, b.b, bot.world.player.grounded, bot.world.state, bot.world.saws.map { "(%.1f,%.1f)".format(it.x, it.y) }))
        }
        if (n == 14) {
            bot.rightTo(8.8f)
            for (i in 0 until 50) { bot.wait(0.1f); if (i % 3 == 0) log("stand") }
        }
        if (n == 12) {
            bot.rightUntil { w -> w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= 3.2f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(17.8f)
                .waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.wait(0.25f)
                .leftJump(0.35f).landLeft().leftJump(0.55f).landLeft()
            log("on wall?")
            for (i in 0 until 14) { bot.left(0.1f); log("reckless") }
        }
    }
}
