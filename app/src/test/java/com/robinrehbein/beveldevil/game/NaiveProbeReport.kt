package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Test

/**
 * A measurement, not a guard rail: plays every round of every level of the three worlds with the naive, scripted
 * players of [NaiveProbes] and reports how much the traps threaten them. It prints a table and writes it to [OUT] (or
 * the file named by the system property / env var `naiveProbeOut`); it never fails, so it cannot break CI. The probes
 * and their flags are defined once, in [NaiveProbes]; the round rules A-C ([DesignRules.roundRules]) gate on the same
 * flags.
 */
class NaiveProbeReport {
    companion object {
        const val TIMEOUT = NaiveProbes.TIMEOUT
        const val FREEZE = NaiveProbes.FREEZE
        const val SHIFT = NaiveProbes.SHIFT
        const val OUT = "build/reports/naive-probe-report.txt"
        /** Rounds that get a verbose per-probe block: "world-level". */
        val VERBOSE = setOf("2-24", "2-25", "2-26", "2-27", "2-29")
    }

    private class Row(val world: Int, val n: Int, val round: Int, val line: String, val flags: Set<String>, val distinct: Int, val verbose: String?)

    private fun measure(world: Int, n: Int, level: Level, round: Int, solutions: List<Solution>?): Row {
        val m = NaiveProbes.measure(world, n, level, round, solutions, verbose = "$world-$n" in VERBOSE)
        return Row(world, n, round, m.line, m.flags, m.distinct, m.verbose)
    }

    @Test
    fun report() {
        val started = System.nanoTime()
        val out = System.getProperty("naiveProbeOut") ?: System.getenv("naiveProbeOut") ?: OUT
        val only = (System.getProperty("naiveProbeWorlds") ?: System.getenv("naiveProbeWorlds"))?.split(',')?.mapNotNull { it.trim().toIntOrNull() }?.toSet()?.takeIf { it.isNotEmpty() }
        val worlds = listOf(
            Triple(1, { World1.levels }, { World1DesignTest.ALL_SOLUTIONS }),
            Triple(2, { World2.levels }, { World2DesignTest.SOLUTIONS }),
            Triple(3, { World3.levels }, { World3DesignTest.SOLUTIONS }),
        ).filter { only == null || it.first in only }
        val rows = ArrayList<Row>()
        val errors = ArrayList<String>()
        for ((world, levelsOf, solutionsOf) in worlds) {
            try {
                val levels = levelsOf()
                val solutions = solutionsOf()
                val jobs = levels.withIndex().flatMap { (i, l) -> l.rounds.indices.map { r -> Triple(i + 1, l, r) } }
                rows += jobs.parallelStream().map { (n, l, r) ->
                    try { measure(world, n, l, r, solutions[n]) } catch (t: Throwable) {
                        Row(world, n, r, "$world-$n-${r + 1} ERROR ${t::class.simpleName}: ${t.message}", setOf("ERROR"), -1, null)
                    }
                }.toList()
            } catch (t: Throwable) { errors += "world $world: ${t::class.simpleName}: ${t.message}" }
        }
        rows.sortWith(compareBy({ it.world }, { it.n }, { it.round }))
        val text = buildString {
            append("Naive probe report (timeout ${TIMEOUT.toInt()} s, P4 freeze ${FREEZE.toInt()} s, P6 shift ${SHIFT} s)\n")
            append("Columns: P1 hold R/L, P2 greedy hopper R/L, P3 stand still: door@t / died@t[lethal element<-killer] / timeout.\n")
            append("Killers: T<k>:<Trigger> = trap unit whose first trap is the k-th of the round; static:<what> = no trap explains it.\n\n")
            rows.forEach { append(it.line).append('\n') }
            append("\n=== Summary ===\n")
            val flagNames = listOf("NAIVE_CLEAR", "ZIGZAG_CLEAR", "ONE_DEATH", "ONE_THREAT", "PASSIVE_SAFE", "OLD_SOLUTION_WORKS", "HELPFUL_TRAP", "HELPFUL_PROGRESS", "LOW_THREAT", "SOLUTION_FAILS", "ERROR")
            for (w in rows.map { it.world }.distinct()) {
                val ws = rows.filter { it.world == w }
                val rematches = ws.count { it.round > 0 }
                append("World $w: ${ws.size} rounds (${ws.map { it.n }.distinct().size} levels, $rematches rematch rounds)\n")
                append("  flags: " + flagNames.joinToString(", ") { f -> "$f=${ws.count { f in it.flags }}" } + "\n")
                val d = ws.filter { it.distinct >= 0 }.groupingBy { minOf(it.distinct, 4) }.eachCount()
                append("  distinctKillers: " + (0..4).joinToString(", ") { k -> "${if (k == 4) "4+" else "$k"}=${d[k] ?: 0}" } + "\n")
            }
            val dAll = rows.filter { it.distinct >= 0 }.groupingBy { minOf(it.distinct, 4) }.eachCount()
            append("All: distinctKillers " + (0..4).joinToString(", ") { k -> "${if (k == 4) "4+" else "$k"}=${dAll[k] ?: 0}" } + "\n")
            append("\n=== Flagged rounds, worst first (score: NAIVE_CLEAR 3, OLD_SOLUTION_WORKS 3, HELPFUL_TRAP 2, ONE_DEATH 2, PASSIVE_SAFE 2, ONE_THREAT 1, ZIGZAG_CLEAR 1, LOW_THREAT 1, 0 killers +1) ===\n")
            fun score(r: Row) = (if ("NAIVE_CLEAR" in r.flags) 3 else 0) + (if ("OLD_SOLUTION_WORKS" in r.flags) 3 else 0) +
                (if ("HELPFUL_TRAP" in r.flags) 2 else 0) + (if ("ONE_DEATH" in r.flags) 2 else 0) + (if ("ONE_THREAT" in r.flags) 1 else 0) + (if ("ZIGZAG_CLEAR" in r.flags) 1 else 0) + (if ("PASSIVE_SAFE" in r.flags) 2 else 0) + (if ("LOW_THREAT" in r.flags) 1 else 0) + (if (r.distinct == 0) 1 else 0)
            rows.filter { score(it) > 0 }.sortedWith(compareByDescending<Row> { score(it) }.thenBy { it.world }.thenBy { it.n }.thenBy { it.round })
                .forEach { append("  [${score(it)}] ${it.world}-${it.n}-${it.round + 1} d=${it.distinct} ${it.flags.joinToString(" ")}\n") }
            val verbose = rows.mapNotNull { it.verbose }
            if (verbose.isNotEmpty()) { append("\n=== Verbose (sanity rounds) ===\n"); verbose.forEach { append(it) } }
            errors.forEach { append("ERROR $it\n") }
            append("\nelapsed %.1f s\n".format((System.nanoTime() - started) / 1e9))
        }
        println(text)
        try { File(out).apply { parentFile?.mkdirs() }.writeText(text) } catch (t: Throwable) { println("could not write $out: $t") }
    }
}
