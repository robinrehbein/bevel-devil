package com.robinrehbein.beveldevil.game

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §11 of docs/LEVEL_DESIGN_V2.md: the rollout blocks and their budgets ([DesignRules.ROLLOUT]). The budgets live in the
 * locked kit; this test checks that the doc says the same, that the budgets of an act fit its caps together with the
 * levels outside the blocks, and that every block whose levels are all rebuilt keeps its budget (the world design tests
 * check the same per world).
 */
class RolloutBudgetTest {
    private val worlds = listOf(
        Triple(1, World1.levels, World1DesignTest.REBUILT to World1DesignTest.SOLUTIONS),
        Triple(2, World2.levels, World2DesignTest.REBUILT to World2DesignTest.SOLUTIONS),
        Triple(3, World3.levels, World3DesignTest.REBUILT to World3DesignTest.SOLUTIONS),
    )

    private fun signatures(levels: List<Level>, ns: Collection<Int>, solutions: Map<Int, List<Solution>>) =
        ns.filter { solutions[it]?.isNotEmpty() == true }.sorted().associateWith { n ->
            solutions.getValue(n).withIndex().filter { it.index < levels[n - 1].rounds.size }.map { (r, s) -> DesignRules.signature(levels[n - 1], s, r) }
        }

    @Test
    fun everyCompleteBlockKeepsItsBudget() {
        val v = worlds.flatMap { (w, levels, rs) ->
            val (rebuilt, solutions) = rs
            DesignRules.ROLLOUT.filter { it.world == w && it.levels.all { n -> n in rebuilt } }.flatMap { block ->
                DesignRules.budgetViolations(block, block.levels.associateWith { levels[it - 1] }, signatures(levels, block.levels, solutions))
            } + DesignRules.partialBlocks(w, rebuilt)
        }
        assertTrue(v.joinToString("\n"), v.isEmpty())
    }

    @Test
    fun theBlocksCoverTheRolloutOnce() {
        val expected = mapOf(
            1 to (7..48).toSet(),
            2 to ((1..10) + listOf(18, 20) + (23..48)).toSet(),
            3 to (1..48).toSet(),
        )
        for ((w, want) in expected) {
            val blocks = DesignRules.ROLLOUT.filter { it.world == w }
            val all = blocks.flatMap { it.levels }
            assertEquals("world $w: a level in two blocks", all.size, all.toSet().size)
            assertEquals("world $w", want, all.toSet())
            // a block never spans two acts
            for (b in blocks) assertEquals("$b", 1, b.levels.map(DesignRules::act).toSet().size)
        }
    }

    /** The caps of one act: H12, H21, H5 and §7. */
    private val caps = mapOf("pad" to 3, "gate" to 3, "blink" to 3, "door" to 1, "gravity" to 1, "swap" to 2, "wall" to DesignRules.WALL_MOVE_CAP,
        "spikes" to DesignRules.ACT_QUOTA, "heatspike" to DesignRules.ACT_QUOTA)

    @Test
    fun theBudgetsOfAnActFitItsCapsWithTheLevelsOutsideTheBlocks() {
        val out = ArrayList<String>()
        for ((w, levels, rs) in worlds) {
            val (rebuilt, solutions) = rs
            for (act in 1..3) {
                val blocks = DesignRules.ROLLOUT.filter { it.world == w && DesignRules.act(it.levels.first()) == act }
                // rebuilt levels of the act outside every block (the untouched pilot): they count against the caps too
                val fixed = rebuilt.filter { n -> DesignRules.act(n) == act && blocks.none { n in it.levels } }
                val used = DesignRules.blockUsage(fixed.associateWith { levels[it - 1] }, signatures(levels, fixed, solutions))
                for ((item, cap) in caps) {
                    if (blocks.none { item in it.budget }) continue
                    val sum = blocks.sumOf { it.budget[item] ?: 0 } + used.getValue(item)
                    if (sum > cap) out += "world $w act $act: $item budgets ${blocks.map { it.budget[item] }} + ${used[item]} outside the blocks = $sum > $cap"
                }
                val fixedCards = DesignRules.blockCards(fixed.associateWith { levels[it - 1] })
                for (c in Card.entries.filter { it != Card.GRAND_FINALE && it != Card.BLUFF }) {
                    val sum = blocks.sumOf { it.cardBudget(c) } + (fixedCards[c] ?: 0)
                    if (sum > DesignRules.CARD_LIMIT) out += "world $w act $act: card $c budgets $sum > ${DesignRules.CARD_LIMIT}"
                }
            }
            // the rematch levels stay where they are (47 in all), and U18 is where §5a puts it
            val rematches = levels.withIndex().filter { it.value.rematch.isNotEmpty() }.map { it.index + 1 }
            val inBlocks = DesignRules.ROLLOUT.filter { it.world == w }
            val outside = rematches.filter { n -> inBlocks.none { n in it.levels } }
            if (inBlocks.sumOf { it.budget.getValue("rematch") } + outside.size != rematches.size) out += "world $w: rematch budgets do not add up to $rematches"
            for (b in inBlocks) if (b.budget.getValue("rematch") != rematches.count { it in b.levels }) out += "$b: rematch budget ${b.budget["rematch"]}, the level code has ${rematches.filter { it in b.levels }}"
            val annex = mapOf(1 to listOf(16, 32, 33, 48), 2 to listOf(30, 48), 3 to listOf(16, 32, 47, 48)).getValue(w)
            for (b in inBlocks) if (b.budget.getValue("annex") != annex.count { it in b.levels }) out += "$b: U18 budget ${b.budget["annex"]}, §5a has ${annex.filter { it in b.levels }}"
        }
        assertTrue(out.joinToString("\n"), out.isEmpty())
    }

    // ---------- the doc says the same ----------

    private fun levelsOf(cell: String): Set<Int> = cell.split(',').flatMap { part ->
        val p = part.trim()
        val range = Regex("""(\d+)\s*[–-]\s*(\d+)""").matchEntire(p)
        if (range != null) (range.groupValues[1].toInt()..range.groupValues[2].toInt()).toList() else listOf(p.toInt())
    }.toSet()

    private fun number(cell: String): Int? = Regex("""\d+""").find(cell)?.value?.toInt()

    @Test
    fun section11MatchesTheKit() {
        val doc = listOf("../docs/LEVEL_DESIGN_V2.md", "docs/LEVEL_DESIGN_V2.md").map(::File).first { it.exists() }.readText()
        val section = doc.substringAfter("\n## 11.").substringBefore("\n## ")
        val rows = Regex("""^\|W(\d)-([A-F])\|(.*)\|\s*$""", RegexOption.MULTILINE).findAll(section).toList()
        val fromDoc = rows.associate { m ->
            val cells = m.groupValues[3].split('|').map { it.trim() }
            val budget = DesignRules.BUDGET_ITEMS.withIndex().mapNotNull { (i, item) -> number(cells[i + 1])?.let { item to it } }.toMap()
            val cardCell = cells[1 + DesignRules.BUDGET_ITEMS.size]
            val exceptions = Regex("""([A-Z_]+) (\d+)""").findAll(cardCell).associate { Card.valueOf(it.groupValues[1]) to it.groupValues[2].toInt() }
            val forbidCell = cells[2 + DesignRules.BUDGET_ITEMS.size]
            val forbidden = if (forbidCell == "–") emptyMap() else forbidCell.split(';').associate { e ->
                val (n, fs) = e.split(':')
                n.trim().toInt() to fs.split(',').map { it.trim() }.toSet()
            }
            "W${m.groupValues[1]}-${m.groupValues[2]}" to listOf(levelsOf(cells[0]), budget, number(cardCell), exceptions, forbidden)
        }
        val fromKit = DesignRules.ROLLOUT.associate { b -> "W${b.world}-${b.id}" to listOf(b.levels, b.budget, b.card, b.cards, b.forbidden) }
        val diff = (fromDoc.keys + fromKit.keys).sorted().filter { fromDoc[it] != fromKit[it] }.map { "$it: doc ${fromDoc[it]}, kit ${fromKit[it]}" }
        assertTrue("§11 in the doc and DesignRules.ROLLOUT differ:\n" + diff.joinToString("\n"), diff.isEmpty())
    }
}
