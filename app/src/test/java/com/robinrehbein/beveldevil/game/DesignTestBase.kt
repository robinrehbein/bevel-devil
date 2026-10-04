package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The guard-rail tests of docs/LEVEL_DESIGN_V2.md §9 for one world; see [DesignRules] for the rules and for how a
 * rebuilt level is registered. The table tests run on the whole §8 table from day one; everything that plays a level
 * runs only for [rebuilt] levels, so the rebuild stays green block by block.
 */
abstract class DesignTestBase {
    abstract val world: Int
    abstract val levels: List<Level>
    /** §8, level number → design. */
    abstract val design: Map<Int, Design>
    /** Rows §8 does not fix yet (the pilot): no row in [design], skipped by H6. */
    open val pending: Set<Int> = emptySet()
    /** Levels that already follow the V2 rules. */
    abstract val rebuilt: Set<Int>
    /** Level number → one solution per round. */
    abstract val solutions: Map<Int, List<Solution>>
    /** Act → its lead mechanic, exempt from "3 of 4" in H6 (§8, World 3). */
    open val lead: Map<Int, String> = emptyMap()

    private fun level(n: Int) = levels[n - 1]
    private val rebuiltLevels get() = rebuilt.sorted().associateWith(::level)
    /** Every round of every rebuilt level, with its solution if registered ([rebuiltLevelsHaveADesignAndASolutionPerRound] reports the gaps). */
    private val rebuiltRounds get() = rebuilt.sorted().flatMap { n -> level(n).rounds.indices.map { r -> Triple(n, r, solutions[n]?.getOrNull(r)) } }
    private val solvedRounds get() = rebuiltRounds.mapNotNull { (n, r, s) -> s?.let { Triple(n, r, it) } }

    private fun assertNone(what: String, violations: List<String>) =
        assertTrue("$what:\n" + violations.joinToString("\n"), violations.isEmpty())

    // ---------- the §8 table ----------

    @Test
    fun designTableCoversTheWorldWithItsActStructure() {
        assertTrue(levels.size == 48)
        assertNone("§8 table of world $world", DesignRules.tableViolations(world, design, (1..48).toSet() - pending))
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

    @Test
    fun cleanRunLastsLongEnough() {
        assertNone("H3", solvedRounds.flatMap { (n, r, s) -> DesignRules.cleanRunViolations(level(n), r, s, DesignRules.minDuration(world, n, design[n])) })
    }

    @Test
    fun holdRightWithHopsNeverWins() {
        assertNone("H2", rebuiltRounds.flatMap { (n, r, _) -> DesignRules.holdRightWithHopsViolations(level(n), r) })
    }

    @Test
    fun solutionToleratesSlop() {
        assertNone("H7", solvedRounds.flatMap { (n, r, s) -> DesignRules.slopViolations(level(n), r, s) })
    }

    @Test
    fun spikePopupQuota() {
        assertNone("H5", DesignRules.spikeQuotaViolations(rebuiltLevels))
        if (world == 3) assertNone("H5 (W3)", DesignRules.spikeQuotaViolations(rebuiltLevels, DesignRules::heatSpikeFinaleCount, "HeatSpike finales"))
    }

    @Test
    fun cardsSpreadPerAct() {
        assertNone("§7", DesignRules.cardSpreadViolations(rebuiltLevels))
    }
}
