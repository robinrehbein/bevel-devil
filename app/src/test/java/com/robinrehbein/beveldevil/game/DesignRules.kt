package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue

/**
 * Guard rails of docs/LEVEL_DESIGN_V2.md §9: the quality rules of the V2 rebuild as reusable checks.
 *
 * Every rule is a function that returns its violations as readable lines (empty: the rule holds), so the same check
 * serves the design tests (assert empty) and the kit's own tests (assert that a bad room is caught).
 *
 * **How a rebuilt level gets checked** (the world's test class, e.g. `World2DesignTest`, extends [DesignTestBase]):
 *
 * 1. `design` already holds the level's row from §8 (block and twist codes). If you swap within a block as §8 allows,
 *    change the row here *and* in the doc; the table tests (H6, act structure) then tell you whether the swap is legal.
 * 2. Add the level's bot solution to `SOLUTIONS` (companion of the world's design test), one [Solution] per round:
 *    round 1 first, then each rematch round. Prefer `rightTo`/`hopR`/`waitFor` over raw hold times: the slop test
 *    perturbs every timed hold by ±0.15 s and every `rightTo`/`leftTo` target by ±1 tile.
 * 3. Add the level number to `REBUILT`. From then on every rule below runs for it.
 *
 *     val SOLUTIONS: Map<Int, List<Solution>> = mapOf(
 *         12 to listOf(
 *             { hopR(4.2f).leftTo(2.6f).rightTo(20f).waitFor { !it.beams[0].lit }.hopR(24f).right(2f) },  // round 1
 *             { rightTo(9f).hopL(6f).rightTo(27f) },                                                   // rematch
 *         ),
 *     )
 *     val REBUILT: Set<Int> = setOf(11, 12)
 *
 * The world's level test can play the same script, so the solution lives in one place:
 * `@Test fun level12() = World2DesignTest.play(12)` (and `play(12, round = 2)` in the deck test).
 *
 * The rules (all for rebuilt levels only, except H6 and the table checks, which run on the whole §8 table):
 * - H3 [cleanRunTime] ≥ [minDuration] (§6; ★ breathers and W1 1–6 exempt),
 * - H2 [holdRightWithHopsViolations]: holding right, plain or hopping every 0.4 / 0.7 / 1.0 s, never wins,
 * - H7 [slopViolations]: the solution still wins with all its timings late *or* all early,
 * - H5 [spikePopupCount] ≤ 1 per round, and at most [ACT_QUOTA] rebuilt levels per act with any (W3: also [heatSpikeFinaleCount]),
 * - H6 [h6Violations] on the §8 table,
 * - §7 [cardSpreadViolations]: no card more than 3 times in an act (GRAND_FINALE in a finale does not count).
 */
typealias Solution = Bot.() -> Unit

/**
 * A level's row in §8: its puzzle blocks (R1–R12) and its surprise families (U1–U17), the main one first. A meta twist
 * keeps its variant (`U16:Ghost`): the meta tricks have nothing in common but the label, so H6 compares the variant.
 */
data class Design(val blocks: Set<String>, val twists: Set<String>, val breather: Boolean = false) {
    val mainBlock get() = blocks.firstOrNull()
    val mainTwist get() = twists.firstOrNull()
    val codes get() = blocks + twists

    override fun toString() = "${blocks.joinToString("+").ifEmpty { "–" }}/${twists.joinToString("+")}${if (breather) " ★" else ""}"
}

/** Builds a [Design] the way §8 writes it: `d("R1+R5", "U7+U4")`, `d("–", "U9", breather = true)`, `d("R5", "U16:Ghost")`. */
fun d(blocks: String, twists: String, breather: Boolean = false) = Design(
    blocks.split('+').map { it.trim() }.filter { it.isNotEmpty() && it != "–" && it != "-" }.toCollection(LinkedHashSet()),
    twists.split('+').map { it.trim() }.filter { it.isNotEmpty() }.toCollection(LinkedHashSet()),
    breather,
)

object DesignRules {
    const val SLOP_TIME = 0.15f
    const val SLOP_TILES = 1f
    /** The two sloppy players of [slopViolations]: late (holds longer, jumps further on) and early. */
    val SLOPS = listOf(Slop(SLOP_TIME, SLOP_TILES), Slop(-SLOP_TIME, -SLOP_TILES))
    /** Hop rhythms (seconds between jumps) of the naive player. */
    val HOP_RHYTHMS = listOf(0.4f, 0.7f, 1.0f)
    /** How long the naive player keeps at it. */
    const val NAIVE_SECONDS = 30f
    val FINALES = setOf(16, 32, 48)
    /** H5: rebuilt levels per act (of 16) with a spike popup, 25 %. */
    const val ACT_QUOTA = 4
    /** §7: plays of one card per act. */
    const val CARD_LIMIT = 3

    val BLOCKS = (1..12).map { "R$it" }.toSet()
    val TWISTS = (1..17).map { "U$it" }.toSet()
    /** The families W3 has to bring in at least every third level (§8, World 3). */
    val OTHER_FAMILIES = setOf("U1", "U2", "U3", "U4", "U6", "U7", "U8", "U12", "U14")

    fun act(n: Int) = (n - 1) / 16 + 1
    fun isFinale(n: Int) = n in FINALES
    /** The levels the V2 rules apply to: all from W1-7 on. */
    fun ruled(world: Int, n: Int) = world != 1 || n >= 7

    /** §6: seconds a clean run of level [n] of world [world] has to last (0: no minimum). */
    fun minDuration(world: Int, n: Int, design: Design?): Float = when {
        design?.breather == true -> 0f
        isFinale(n) -> 15f
        world == 1 && n <= 6 -> 0f
        world == 1 && act(n) == 1 -> 6f
        world == 1 -> 8f
        world == 2 && act(n) == 1 -> 8f
        else -> 10f
    }

    // ---------- playing ----------

    /** Plays [solution] on [round] (0-based) of [level]. */
    fun play(level: Level, round: Int, solution: Solution, slop: Slop = Slop.NONE): Bot = Bot(level, round, slop).apply(solution)

    /** H3: plays the solution, asserts the win and returns [World.time] at the win. */
    fun cleanRunTime(level: Level, round: Int, solution: Solution): Float {
        val bot = play(level, round, solution)
        bot.expect(WorldState.WON)
        return bot.world.time
    }

    /** H3 as violations: the solution does not win, or wins faster than [min] seconds. */
    fun cleanRunViolations(level: Level, round: Int, solution: Solution, min: Float): List<String> {
        val bot = play(level, round, solution)
        val where = "${level.name.en} round ${round + 1}"
        return when {
            bot.world.state != WorldState.WON -> listOf("$where: the solution ends ${bot.world.state} at t=%.2f".format(bot.world.time))
            bot.world.time < min -> listOf("$where: clean run %.2f s, needs %.0f s".format(bot.world.time, min))
            else -> emptyList()
        }
    }

    /** The naive player: holding right, plainly or hopping every [rhythm] seconds, for [NAIVE_SECONDS]. */
    fun naive(level: Level, round: Int, rhythm: Float?): Bot {
        val bot = Bot(level, round)
        if (rhythm == null) return bot.right(NAIVE_SECONDS)
        val jump = minOf(0.35f, rhythm / 2)
        while (bot.world.state == WorldState.PLAYING && bot.world.time < NAIVE_SECONDS) bot.rightJump(jump).right(rhythm - jump)
        return bot
    }

    /** H2: the naive strategies that win [round] of [level] (empty: none does). */
    fun holdRightWithHopsViolations(level: Level, round: Int): List<String> =
        (listOf<Float?>(null) + HOP_RHYTHMS).mapNotNull { r ->
            val bot = naive(level, round, r)
            if (bot.world.state != WorldState.WON) null
            else "${level.name.en} round ${round + 1}: holding right${r?.let { " hopping every $it s" } ?: ""} wins at t=%.2f".format(bot.world.time)
        }

    fun holdRightWithHopsNeverWins(level: Level, round: Int) {
        val v = holdRightWithHopsViolations(level, round)
        assertTrue(v.joinToString("\n"), v.isEmpty())
    }

    /** H7: the solution has to win with at least one of [SLOPS]. Empty: it does. */
    fun slopViolations(level: Level, round: Int, solution: Solution): List<String> {
        val results = SLOPS.map { s -> s to play(level, round, solution, s).world }
        if (results.any { it.second.state == WorldState.WON }) return emptyList()
        return listOf("${level.name.en} round ${round + 1}: needs pixel-perfect input, " +
            results.joinToString { (s, w) -> "${if (s.time > 0) "late" else "early"} ${w.state} at x=%.1f t=%.1f".format(w.player.box.cx, w.time) })
    }

    fun solutionToleratesSlop(level: Level, round: Int, solution: Solution): Boolean = slopViolations(level, round, solution).isEmpty()

    // ---------- reading a round ----------

    private fun actions(round: Level) = round.start + round.traps.flatMap { it.actions }

    /** H5: [Action.Show]s in [round] (a level or one of its [Level.rounds]) that grow hidden spikes. */
    fun spikePopupCount(round: Level): Int = actions(round).count { a ->
        a is Action.Show && round.glyph(a.group).let { it != null && it.spike && it.hidden }
    }

    /**
     * H5 for W3: 1 if [round] ends on an overclocked floor ([Action.HeatSpike]), else 0. The final trap is the last one
     * in the list, or any whose trigger sits within 3 tiles of the door.
     */
    fun heatSpikeFinaleCount(round: Level): Int {
        val heat = { t: Trap -> t.actions.any { it is Action.HeatSpike } }
        val doorX = round.map.grid.firstNotNullOfOrNull { row -> row.indexOf('D').takeIf { it >= 0 } } ?: return 0
        val near = round.traps.any { t ->
            heat(t) && when (val tr = t.trigger) {
                is Trigger.PastX -> tr.x >= doorX - 3
                is Trigger.Landed -> tr.x1 >= doorX - 3
                is Trigger.Airborne -> tr.x1 >= doorX - 3
                is Trigger.Zone -> tr.x1 >= doorX - 3
                else -> false
            }
        }
        return if (near || round.traps.lastOrNull()?.let(heat) == true) 1 else 0
    }

    /** Cards played ([Action.Play]) in all rounds of [level]; bluffs do not count. */
    fun cards(level: Level): List<Card> = level.rounds.flatMap { r -> r.traps.flatMap { t -> t.actions.filterIsInstance<Action.Play>().map { it.card } } }

    // ---------- rules over several levels ----------

    /** H5: per round at most one spike popup, per act at most [ACT_QUOTA] of [levels] (number → level) with any. */
    fun spikeQuotaViolations(levels: Map<Int, Level>, count: (Level) -> Int = ::spikePopupCount, what: String = "spike popups"): List<String> {
        val out = ArrayList<String>()
        for ((n, l) in levels) l.rounds.forEachIndexed { r, round ->
            val c = count(round)
            if (c > 1) out += "level $n round ${r + 1}: $c $what"
        }
        levels.keys.groupBy(::act).forEach { (a, ns) ->
            val with = ns.filter { n -> levels.getValue(n).rounds.any { count(it) > 0 } }.sorted()
            if (with.size > ACT_QUOTA) out += "act $a: ${with.size} levels with $what (max $ACT_QUOTA): $with"
        }
        return out
    }

    /** §7: no card more than [CARD_LIMIT] times per act among [levels]; GRAND_FINALE in a finale is free. */
    fun cardSpreadViolations(levels: Map<Int, Level>): List<String> = levels.keys.groupBy(::act).flatMap { (a, ns) ->
        ns.flatMap { n -> cards(levels.getValue(n)).filterNot { it == Card.GRAND_FINALE && isFinale(n) } }
            .groupingBy { it }.eachCount().filter { it.value > CARD_LIMIT }
            .map { (c, k) -> "act $a: $c played $k times (max $CARD_LIMIT)" }
    }

    /**
     * H6 on a §8 table: two neighbours (neither an act finale) share neither the main block nor the main twist; no code
     * shows up in 3 of 4 consecutive levels. Finales (they combine the act on purpose), the levels before [from] and
     * the [lead] mechanic of an act (act → code, exempt from "3 of 4" only) stay out. Missing rows are skipped.
     */
    fun h6Violations(table: Map<Int, Design>, from: Int = 1, lead: Map<Int, String> = emptyMap()): List<String> {
        val out = ArrayList<String>()
        val ns = table.keys.filter { it >= from && !isFinale(it) }.sorted()
        for (n in ns) {
            val a = table.getValue(n)
            val b = table[n + 1] ?: continue
            if (isFinale(n + 1)) continue
            if (a.mainBlock != null && a.mainBlock == b.mainBlock) out += "levels $n and ${n + 1} share the main block ${a.mainBlock}"
            if (a.mainTwist != null && a.mainTwist == b.mainTwist) out += "levels $n and ${n + 1} share the main twist ${a.mainTwist}"
        }
        for (n in table.keys.filter { it >= from }.sorted()) {
            val window = (n..n + 3).toList()
            if (window.any { it !in table }) continue
            val counted = window.filterNot(::isFinale)
            val codes = counted.flatMap { table.getValue(it).codes }.groupingBy { it }.eachCount()
            for ((code, k) in codes) if (k >= 3 && window.none { lead[act(it)] == code }) {
                out += "$code in ${counted.filter { code in table.getValue(it).codes }} (3 of levels $n–${n + 3})"
            }
        }
        return out.distinct()
    }

    /**
     * The table itself: valid codes, every [ruled] level that is no breather has a puzzle block (H2), finales combine
     * at least two blocks and two twists, at most two breathers per act, a breather has at most one block.
     */
    fun tableViolations(world: Int, table: Map<Int, Design>, expected: Set<Int>): List<String> {
        val out = ArrayList<String>()
        if (table.keys != expected) out += "rows ${table.keys.sorted()} instead of ${expected.sorted()}"
        for ((n, design) in table.toSortedMap()) {
            val bad = design.blocks.filterNot { it in BLOCKS } + design.twists.filterNot { it.substringBefore(':') in TWISTS }
            if (bad.isNotEmpty()) out += "level $n: unknown codes $bad"
            if (design.twists.isEmpty()) out += "level $n: no surprise"
            if (isFinale(n) && (design.breather || design.blocks.size < 2 || design.twists.size < 2)) out += "level $n: a finale combines 2+ blocks and 2+ twists ($design)"
            if (!design.breather && ruled(world, n) && design.blocks.isEmpty()) out += "level $n: no puzzle block and no ★ (H2)"
            if (design.breather && design.blocks.size > 1) out += "level $n: a breather has at most one block"
        }
        table.filterValues { it.breather }.keys.groupBy(::act).forEach { (a, ns) -> if (ns.size > 2) out += "act $a: ${ns.size} breathers $ns (max 2)" }
        return out
    }

    /** Levels of [table] whose blocks include one of [codes]. */
    fun withBlock(table: Map<Int, Design>, vararg codes: String) = table.filterValues { d -> d.blocks.any { it in codes } }.keys.sorted()

    /** W3: at most two levels in a row without a surprise of [OTHER_FAMILIES]. */
    fun otherFamilyViolations(table: Map<Int, Design>): List<String> = table.keys.sorted().mapNotNull { n ->
        val run = (n..n + 2).toList()
        if (run.any { it !in table }) null
        else if (run.none { m -> table.getValue(m).twists.any { it.substringBefore(':') in OTHER_FAMILIES } }) "levels $run: no surprise from another family"
        else null
    }
}
