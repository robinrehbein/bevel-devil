package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The guard-rail tests of docs/LEVEL_DESIGN_V2.md §9 for one world; see [DesignRules] for the rules and for how a
 * rebuilt level is registered. The table tests run on the whole §8 table from day one; everything that plays a level
 * runs only for [rebuilt] levels, so the rebuild stays green block by block. There is no allowlist: a rebuilt level
 * either follows every rule or its test is red.
 */
abstract class DesignTestBase {
    abstract val world: Int
    abstract val levels: List<Level>
    /** §8, level number → design. */
    abstract val design: Map<Int, Design>
    /** Levels that already follow the V2 rules. */
    abstract val rebuilt: Set<Int>
    /** Level number → one solution per round. */
    abstract val solutions: Map<Int, List<Solution>>
    /** Act → its lead mechanic, exempt from "3 of 4" in H6 (§8, World 3). */
    open val lead: Map<Int, String> = emptyMap()

    private fun level(n: Int) = levels[n - 1]

    private fun assertNone(what: String, violations: List<String>) =
        assertTrue("$what:\n" + violations.joinToString("\n"), violations.isEmpty())

    // ---------- the §8 table ----------

    @Test
    fun designTableCoversTheWorldWithItsActStructure() {
        assertTrue(levels.size == 48)
        assertNone("§8 table of world $world", DesignRules.tableViolations(world, design, (1..48).toSet()))
    }

    /** "U16 (Frame-Crack)" → "U16:FrameCrack"; other notes in brackets are prose. */
    private fun twistCodes(cell: String) = Regex("""U\d+(?:\s*\(([^)]*)\))?""").findAll(cell).map { m ->
        val code = m.value.substringBefore('(').trim()
        val variant = m.groupValues[1].filter { it.isLetter() }
        if (code == "U16" && variant.isNotEmpty()) "$code:$variant" else code
    }.toList()

    /** The table here is §8 of the doc, transcribed: same codes in the same order, same meta variants, same ★. */
    @Test
    fun designTableMatchesTheDoc() {
        val doc = listOf("../docs/LEVEL_DESIGN_V2.md", "docs/LEVEL_DESIGN_V2.md").map(::File).first { it.exists() }.readText()
        val section = doc.split("\n### Welt ")[world].substringBefore("\n## ")
        val row = Regex("""^\|(\d+)\|([^|]*)\|([^|]*)\|([^|]*)\|""", RegexOption.MULTILINE)
        val fromDoc = row.findAll(section).associate { m ->
            val (n, name, r, u) = m.destructured
            n.toInt() to Triple(Regex("""R\d+""").findAll(r).map { it.value }.toList(), twistCodes(u), '★' in name)
        }
        val here = design.mapValues { (_, d) -> Triple(d.blocks.toList(), d.twists.toList(), d.breather) }
        val diff = (fromDoc.keys + here.keys).sorted().filter { fromDoc[it] != here[it] }.map { "level $it: doc ${fromDoc[it]}, test ${here[it]}" }
        assertNone("§8 in the doc and in World${world}DesignTest differ", diff)
    }

    @Test
    fun noSameTwistTwiceInARow() {
        assertNone("H6 in the §8 table of world $world", DesignRules.h6Violations(design, from = if (world == 1) 7 else 1, lead = lead))
    }

    // ---------- the rules that play levels: one per v2 rule, for any set of levels ----------

    /** A finding of one rule: the rule's code, the level (null: an act), what is wrong. */
    data class Finding(val rule: String, val level: Int?, val text: String)

    /** The levels a rule looks at: their numbers and a solution per round (gaps reported by [rebuiltLevelsHaveADesignAndASolutionPerRound]). */
    class Scope(val numbers: Set<Int>, val solutions: Map<Int, List<Solution>>, val levelOf: (Int) -> Level) {
        val levels get() = numbers.sorted().associateWith(levelOf)
        /** Every round of every level, with its solution if registered. */
        val rounds get() = numbers.sorted().flatMap { n -> levelOf(n).rounds.indices.map { r -> Triple(n, r, solutions[n]?.getOrNull(r)) } }
        val solved get() = rounds.mapNotNull { (n, r, s) -> s?.let { Triple(n, r, it) } }
    }

    private fun per(rule: String, scope: Scope, f: (Int, Level) -> List<String>) =
        scope.levels.flatMap { (n, l) -> f(n, l).map { Finding(rule, n, it) } }
    private fun perRound(rule: String, scope: Scope, f: (Int, Int, Solution) -> List<String>) =
        scope.solved.flatMap { (n, r, s) -> f(n, r, s).map { Finding(rule, n, it) } }

    /** Rule code → its check. [acts] is the scope the per-act rules count over. */
    private fun rules(scope: Scope, acts: Scope): Map<String, () -> List<Finding>> = linkedMapOf(
        "H3 density" to { perRound("H3", scope) { n, r, s -> DesignRules.densityViolations(level(n), r, s, DesignRules.minDuration(world, n, design[n])) } },
        "H2 naive runs" to { scope.rounds.flatMap { (n, r, _) -> DesignRules.holdRightWithHopsViolations(level(n), r).map { Finding("H2", n, it) } } },
        "H7 slop" to { perRound("H7", scope) { n, r, s -> DesignRules.slopViolations(level(n), r, s) } },
        "H15 teeth" to { perRound("H15", scope) { n, r, s -> DesignRules.teethViolations(level(n), r, s) } },
        "H17 filler death" to { scope.rounds.flatMap { (n, r, _) -> DesignRules.fillerDeathViolations(level(n), r).map { Finding("H17", n, it) } } },
        "H9 rematch" to { per("H9", scope) { n, l -> DesignRules.rematchViolations(l, scope.solutions[n].orEmpty()) } },
        "H13 effect families" to { per("H13", scope) { n, l -> DesignRules.familyViolations(n, l) } },
        "H8 card lint" to { per("H8", scope) { _, l -> DesignRules.cardLintViolations(l) } },
        "H12 rotation" to { DesignRules.rotationViolations(acts.levels).map { Finding("H12", null, it) } },
        "H5 spike popups" to {
            (DesignRules.spikeQuotaViolations(acts.levels) +
                if (world == 3) DesignRules.spikeQuotaViolations(acts.levels, DesignRules::heatSpikeFinaleCount, "HeatSpike finales") else emptyList())
                .map { Finding("H5", null, it) }
        },
        "H19 say lint" to {
            // near-duplicate lines (3+ shared words in one act) are a warning only; exact repeats are the gate
            DesignRules.sayNgramWarnings(acts.levels).forEach { println("H19 warning: $it") }
            DesignRules.sayViolations(acts.levels).map { Finding("H19", null, it) }
        },
        "H20 adjacent rooms" to {
            val sigs = signatures(acts)
            // the old percentage similarity is a report only; it never fails
            DesignRules.adjacentReport(sigs).forEach { println("H20 report: $it") }
            DesignRules.adjacentViolations(sigs).map { Finding("H20", null, it) }
        },
        "H21 moving walls" to { DesignRules.wallMoveViolations(signatures(acts)).map { Finding("H21", null, it) } },
        "§7 card spread" to { DesignRules.cardSpreadViolations(acts.levels).map { Finding("§7", null, it) } },
    )

    /** H20: round 1's signature of every level of [scope] that has a solution. */
    private fun signatures(scope: Scope) = scope.numbers.filter { scope.solutions[it]?.isNotEmpty() == true }.sorted()
        .associateWith { DesignRules.signature(level(it), scope.solutions.getValue(it)[0]) }

    private val rebuiltScope get() = Scope(rebuilt, solutions, ::level)
    private fun check(rule: String) {
        assertNone(rule, rules(rebuiltScope, rebuiltScope).getValue(rule)().map { "level ${it.level ?: "-"}: ${it.text}" })
    }

    // ---------- rebuilt levels ----------

    @Test
    fun rebuiltLevelsHaveADesignAndASolutionPerRound() {
        for (n in rebuilt) {
            assertTrue("level $n: V2 rules start at W1-7", DesignRules.ruled(world, n))
            assertTrue("level $n has no row in the design table", n in design)
            val s = solutions[n]
            assertTrue("level $n needs one solution per round (${level(n).rounds.size}), has ${s?.size ?: 0}", s?.size == level(n).rounds.size)
        }
    }

    @Test fun cleanRunIsDense() = check("H3 density")
    @Test fun holdRightWithHopsNeverWins() = check("H2 naive runs")
    @Test fun solutionToleratesSlop() = check("H7 slop")
    @Test fun everyTrapHasTeeth() = check("H15 teeth")
    @Test fun noFillerDeathAtTheStart() = check("H17 filler death")
    @Test fun rematchWorksAgainstRoundOne() = check("H9 rematch")
    @Test fun atMostTwoEffectFamiliesPerRoom() = check("H13 effect families")
    @Test fun cardsFitTheirTraps() = check("H8 card lint")
    @Test fun mechanicsRotatePerAct() = check("H12 rotation")
    @Test fun spikePopupQuota() = check("H5 spike popups")
    @Test fun noRepeatedLinesInAnAct() = check("H19 say lint")
    @Test fun neighboursPlayDifferently() = check("H20 adjacent rooms")
    @Test fun atMostThreeMovingWallLevelsPerAct() = check("H21 moving walls")
    @Test fun cardsSpreadPerAct() = check("§7 card spread")
}
