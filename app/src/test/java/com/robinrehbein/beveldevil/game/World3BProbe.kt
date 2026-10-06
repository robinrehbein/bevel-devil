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
