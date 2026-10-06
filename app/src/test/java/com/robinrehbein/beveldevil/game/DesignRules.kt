package com.robinrehbein.beveldevil.game

import kotlin.math.abs
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
 *    perturbs every timed hold by ±0.15 s, every `rightTo`/`leftTo` target by ±1 tile and every condition wait
 *    (`waitFor`, `rightUntil`, …) by reacting 0.15 s late or early. Do not pad a solution with `wait`s: H3 measures
 *    the run again with every idle command skipped.
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
 * - H3 [densityViolations]: never more than [GAP] s without a real trap ([Weight]) in the clean run, standing still at
 *   most [WAIT_SHARE] of it and at most [WAIT_STRETCH] s in one go, and no shorter than [minDuration], also with idle
 *   waits skipped,
 * - H2 [holdRightWithHopsViolations]: holding right, plain or hopping every 0.4 / 0.7 / 1.0 s, right away or after
 *   standing still [PROBE_WAIT] s, never wins,
 * - H7 [slopViolations]: the solution still wins with all its timings late *or* all early,
 * - H5 [spikePopupCount] ≤ 1 per round, and at most [ACT_QUOTA] rebuilt levels per act with any (W3: also [heatSpikeFinaleCount]),
 * - H6 [h6Violations] on the §8 table, [tableRotationViolations] and the puzzle share on the table,
 * - §7 [cardSpreadViolations]: no card more than 3 times in an act (GRAND_FINALE in a finale does not count),
 * - H8 [cardLintViolations]: the card fits the trap it sits on ([cardFits]),
 * - H9 [rematchViolations]: a rematch round's clean run is at least as long as round 1's, and round 1's solution loses it,
 * - H12 [rotationViolations]: per act at most [CAPS] levels with a door that flees, pads, timed lasers, blinking,
 *   gravity flip, swapped controls (counted from the actions in the level code),
 * - H13 [familyViolations]: at most [MAX_FAMILIES] effect families ([families]) per room,
 * - H15 [teethViolations]: every lethal trap of the clean run beats standing still or running straight on, itself
 *   (the probe ends differently without the trap's lethal actions),
 * - H17 [fillerDeathViolations]: holding right from the spawn does not die in the first [FILLER_WINDOW] s, unless a trap
 *   that went off caused it,
 * - H19 [sayViolations]: no line twice in an act (case, punctuation and spacing aside),
 * - H20 [adjacentViolations] / H21 [wallMoveViolations]: neighbours share no dominant family in any pair of rounds (a tie:
 *   all tied families), at most [WALL_MOVE_CAP] wall-move levels per act,
 * - trap ablation [decorations]: a trap that changes no probe is decoration and counts for none of H3, H15, H17, H20,
 * - §11 [ROLLOUT], [budgetViolations], [partialBlocks]: the rollout blocks and their share of the act caps.
 *
 * The kit is locked during the rollout ([KitLock]): only the orchestrator changes this file and [DesignTestBase].
 */
typealias Solution = Bot.() -> Unit

/**
 * A level's row in §8: its puzzle blocks (R1–R12) and its surprise families (U1–U18), the main one first. A meta twist
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
    val TWISTS = (1..18).map { "U$it" }.toSet()
    /** The families W3 has to bring in at least every third level (§8, World 3). */
    val OTHER_FAMILIES = setOf("U1", "U2", "U3", "U4", "U6", "U7", "U8", "U12", "U14")

    fun act(n: Int) = (n - 1) / 16 + 1
    fun isFinale(n: Int) = n in FINALES
    /** The levels the V2 rules apply to: all from W1-7 on. */
    fun ruled(world: Int, n: Int) = world != 1 || n >= 7

    /**
     * §6 (recipe v2): the floor a clean run must not fall below, a sanity check only (density, not duration, is the
     * goal, see [densityViolations]). 6 s; W1 1–6 4 s; act finales 10 s; ★ breathers none.
     */
    fun minDuration(world: Int, n: Int, design: Design?): Float = when {
        design?.breather == true -> 0f
        isFinale(n) -> 10f
        world == 1 && n <= 6 -> 4f
        else -> 6f
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

    /**
     * H3 as violations: the solution does not win, or wins faster than [min] seconds. Standing still does not make a
     * room longer: the solution is played a second time with every idle command skipped ([Bot.skipIdle]); if it still
     * wins, that (shorter) run is the one measured. Waits the room really needs make the second run lose and count.
     */
    fun cleanRunViolations(level: Level, round: Int, solution: Solution, min: Float): List<String> {
        val bot = play(level, round, solution)
        val where = "${level.name.en} round ${round + 1}"
        if (bot.world.state != WorldState.WON) return listOf("$where: the solution ends ${bot.world.state} at t=%.2f".format(bot.world.time))
        if (bot.world.time < min) return listOf("$where: clean run %.2f s, needs %.0f s".format(bot.world.time, min))
        val brisk = Bot(level, round, skipIdle = true).apply(solution).world
        if (brisk.state == WorldState.WON && brisk.time < min) {
            return listOf("$where: clean run %.2f s, but %.2f s without its idle waits, needs %.0f s".format(bot.world.time, brisk.time, min))
        }
        return emptyList()
    }

    /**
     * The naive player: after standing still [wait] seconds, holding right, plainly or hopping every [rhythm] seconds,
     * for [NAIVE_SECONDS].
     */
    fun naive(level: Level, round: Int, rhythm: Float?, wait: Float = 0f): Bot {
        val bot = Bot(level, round)
        if (wait > 0f) bot.wait(wait)
        val end = wait + NAIVE_SECONDS
        if (rhythm == null) return bot.right(NAIVE_SECONDS)
        val jump = minOf(0.35f, rhythm / 2)
        while (bot.world.state == WorldState.PLAYING && bot.world.time < end) bot.rightJump(jump).right(rhythm - jump)
        return bot
    }

    /**
     * H2 (and the room-wide half of the teeth test H15): the naive strategies that win [round] of [level] (empty: none
     * does). Holding right, plain or hopping, right away and after standing still [PROBE_WAIT] s.
     */
    fun holdRightWithHopsViolations(level: Level, round: Int): List<String> =
        listOf(0f, PROBE_WAIT).flatMap { wait ->
            (listOf<Float?>(null) + HOP_RHYTHMS).mapNotNull { r ->
                val bot = naive(level, round, r, wait)
                if (bot.world.state != WorldState.WON) null
                else "${level.name.en} round ${round + 1}: ${if (wait > 0f) "standing still ${wait.toInt()} s, then " else ""}holding right${r?.let { " hopping every $it s" } ?: ""} wins at t=%.2f".format(bot.world.time)
            }
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

    /** [actions] and everything nested in them ([Action.FakeWin.then]). */
    fun flatten(actions: List<Action>): List<Action> = actions.flatMap { a -> listOf(a) + if (a is Action.FakeWin) flatten(a.then) else emptyList() }

    private fun actions(round: Level) = flatten(round.start + round.traps.flatMap { it.actions })

    /** H5: [Action.Show]s in [round] (a level or one of its [Level.rounds]) that grow hidden spikes. */
    fun spikePopupCount(round: Level): Int = actions(round).count { a ->
        a is Action.Show && round.glyph(a.group).let { it != null && it.spike && it.hidden }
    }

    /**
     * H5 for W3: 1 if [round] ends on an overclocked floor ([Action.HeatSpike], also inside a fake win), else 0. The
     * final trap is the last one in the list, one that fires [Trigger.AtDoor], or one whose trigger lies within 3 tiles
     * of the door (where it starts or where a [Action.DoorTo] moves it), from either side.
     */
    fun heatSpikeFinaleCount(round: Level): Int {
        val heat = { t: Trap -> flatten(t.actions).any { it is Action.HeatSpike } }
        val start = round.map.grid.withIndex().firstNotNullOfOrNull { (y, row) -> row.indexOf('D').takeIf { it >= 0 }?.let { it to y } }
        val doors = listOfNotNull(start) + actions(round).filterIsInstance<Action.DoorTo>().map { it.col to it.row }
        val near = { x0: Float, x1: Float, y0: Float, y1: Float ->
            doors.any { (dx, dy) -> x0 <= dx + 1 + 3 && x1 >= dx - 3 && y0 <= dy + 1 + 3 && y1 >= dy - 3 }
        }
        val all = -100f to 100f
        val atDoor = round.traps.any { t ->
            heat(t) && when (val tr = t.trigger) {
                is Trigger.AtDoor -> true
                is Trigger.PastX -> near(tr.x, tr.x, all.first, all.second)
                is Trigger.BeforeX -> near(tr.x, tr.x, all.first, all.second)
                is Trigger.Landed -> near(tr.x0, tr.x1, all.first, all.second)
                is Trigger.Airborne -> near(tr.x0, tr.x1, all.first, all.second)
                is Trigger.Zone -> near(tr.x0, tr.x1, tr.y0, tr.y1)
                else -> false
            }
        }
        return if (atDoor || round.traps.lastOrNull()?.let(heat) == true) 1 else 0
    }

    /** Cards played ([Action.Play], also inside a fake win) in all rounds of [level]; bluffs do not count. */
    fun cards(level: Level): List<Card> = level.rounds.flatMap { r -> flatten(r.start + r.traps.flatMap { it.actions }).filterIsInstance<Action.Play>().map { it.card } }

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
     * The table itself: valid codes, finales combine at least two blocks and two twists, at most two breathers per act,
     * a breather has at most one block, about half the rows of an act are puzzle rooms ([puzzleShareViolations]) and the
     * rotation caps hold ([tableRotationViolations]).
     */
    fun tableViolations(world: Int, table: Map<Int, Design>, expected: Set<Int>): List<String> {
        val out = ArrayList<String>()
        if (table.keys != expected) out += "rows ${table.keys.sorted()} instead of ${expected.sorted()}"
        for ((n, design) in table.toSortedMap()) {
            val bad = design.blocks.filterNot { it in BLOCKS } + design.twists.filterNot { it.substringBefore(':') in TWISTS }
            if (bad.isNotEmpty()) out += "level $n: unknown codes $bad"
            if (design.twists.isEmpty()) out += "level $n: no surprise"
            if (isFinale(n) && (design.breather || design.blocks.size < 2 || design.twists.size < 2)) out += "level $n: a finale combines 2+ blocks and 2+ twists ($design)"
            if (design.breather && design.blocks.size > 1) out += "level $n: a breather has at most one block"
        }
        table.filterValues { it.breather }.keys.groupBy(::act).forEach { (a, ns) -> if (ns.size > 2) out += "act $a: ${ns.size} breathers $ns (max 2)" }
        out += puzzleShareViolations(world, table)
        out += tableRotationViolations(world, table)
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

    // ---------- recipe v2 ----------

    /** H3: seconds the clean run may go without a real trap. */
    const val GAP = 3f
    /** H3: share of the clean run the player may stand still. */
    const val WAIT_SHARE = 0.4f
    /** H3: seconds of standing still in one go. */
    const val WAIT_STRETCH = 1.5f
    /** H2/H15: the patient player stands still this long. */
    const val PROBE_WAIT = 8f
    /** H15: the reckless player runs straight on this long after a trap went off. */
    const val PROBE_RUN = 2f
    /** H17: holding right must not die before a trap went off in this window. */
    const val FILLER_WINDOW = 2f
    /** H13: effect families per room (Level Devil's median); an act finale may combine one more. */
    const val MAX_FAMILIES = 2

    /**
     * What a trap that went off counts for (H3 density, H15 teeth, H17 filler deaths). The weight of a trap is the
     * heaviest of its actions ([FakeWin.then] included):
     *
     * - [LETHAL], it can kill the player who does not react: `Fall`, `Move`, `Chase`, `Saw`, `PathSaw`, `Blink` and
     *   `Clock` (a floor that starts to blink), `Laser` (a new beam, also a one-shot), `HeatSpike`, `Ghost`,
     *   `FrameCrack`; `Show` of a spike group; `Hide` of a solid group with a floor tile at or below the player's feet
     *   within 6 tiles (the floor vanishes; a cage or wall opening is no trap); `Power(on)` of a laser or live trace and
     *   `Power(off)` of a copper rail; `Circuit` turning a trace on or a rail off; `Belt` that pushes away from the door.
     * - [ROUTE], it changes the way: `DoorTo`, `Gravity`, `Swap` (also back), `Reroute`, `Power` of a portal or fan,
     *   `FanSet`, `Toggle`, `BitFlip`, `FakeWin`, `Extend`, `Undo`, `Tilt`, `Slope`.
     * - [NONE], a state change or talk: `Say`, `Play`, `Bluff`, `Shake`, `Pad`, `Portal`, `Heat`, `Heatsink`, `Fan`,
     *   `Flip`, `Roll`, `PauseTrap`; `Show` of a solid group (a bridge or block appears: the pad did its job); `Hide`
     *   of spikes; `Power(off)` of a laser or trace and `Power(on)` of a rail; `Power` of a belt; a `Belt` towards the door.
     */
    enum class Weight { NONE, ROUTE, LETHAL }

    /**
     * Traps that went off together, as the player sees them: traps with the same trigger that held at the same moment
     * are one moment (a door hopping along a [doorTrail], a page that drops and comes back). [time] is when its first
     * real action ran.
     */
    class Moment(val trigger: Trigger, val triggered: Float, var time: Float, var weight: Weight, val actions: MutableList<Action>) {
        /** One trap of the moment: when its actions ran and what they weighed. */
        class Part(val trap: Trap, val time: Float, val weight: Weight, val actions: List<Action>, val lethal: List<Action> = emptyList(), val real: List<Action> = emptyList())
        val parts = ArrayList<Part>()
        val real get() = weight != Weight.NONE
        val traps get() = parts.map { it.trap }
        /** The actions of the moment that can kill ([Weight.LETHAL]): what H20 counts the families of. */
        val lethalActions get() = parts.flatMap { it.lethal }
        /** The actions of the moment that weigh something ([Weight.ROUTE] or [Weight.LETHAL]). */
        val realActions get() = parts.flatMap { it.real }

        /** This moment as it counts once the traps in [decoration] ([decorations]) are left out. */
        fun without(decoration: Set<Trap>): Moment {
            val keep = parts.filter { it.trap !in decoration }
            val weight = keep.maxOfOrNull { it.weight } ?: Weight.NONE
            val time = keep.firstOrNull { it.weight != Weight.NONE }?.time ?: keep.firstOrNull()?.time ?: time
            return Moment(trigger, triggered, time, weight, keep.flatMap { it.actions }.toMutableList()).also { it.parts += keep }
        }

        override fun toString() = "t=%.2f %s".format(time, actions.filterNot { it is Action.Say || it is Action.Play }.joinToString("+") { it::class.simpleName ?: "?" })
    }

    /** Adds the trap [s] that just sprang in [w] (an attempt at [round]) to [moments]. */
    fun addMoment(moments: MutableList<Moment>, s: World.Sprung, round: Level, w: World) {
        val acts = flatten(s.trap.actions)
        val weights = acts.map { weigh(it, round, w) }
        val weight = weights.maxOrNull() ?: Weight.NONE
        val part = Moment.Part(s.trap, s.time, weight, acts, acts.filterIndexed { i, _ -> weights[i] == Weight.LETHAL },
            acts.filterIndexed { i, _ -> weights[i] != Weight.NONE })
        val same = moments.lastOrNull { it.trigger == s.trap.trigger && abs(it.triggered - s.triggered) < 1e-4f }
        if (same == null) { moments += Moment(s.trap.trigger, s.triggered, s.time, weight, acts.toMutableList()).also { it.parts += part }; return }
        same.actions += acts
        same.parts += part
        if (!same.real && weight != Weight.NONE) same.time = s.time
        if (weight > same.weight) same.weight = weight
    }

    /** What [a] stands for: laser, portal, fan, belt, trace (live spike circuit) or rail (copper circuit). */
    private fun kind(round: Level, id: Char): String {
        val all = flatten(round.start + round.traps.flatMap { it.actions })
        return when {
            all.any { it is Action.Laser && it.id == id } -> "laser"
            all.any { it is Action.Portal && it.id == id } -> "portal"
            all.any { it is Action.Fan && it.id == id } -> "fan"
            all.any { it is Action.Belt && it.group == id } -> "belt"
            id.isUpperCase() -> "trace"
            else -> "rail"
        }
    }

    /** Tiles of [group] in the map of [round]. */
    private fun tiles(round: Level, group: Char) = round.map.grid.withIndex().flatMap { (y, row) -> row.withIndex().filter { it.value == group }.map { it.index to y } }

    private fun solid(round: Level, c: Char) = round.glyph(c)?.let { !it.spike } ?: false

    /** [group] has a tile one can stand on at or below the player's feet, within 6 tiles of them. */
    private fun floorNear(round: Level, group: Char, w: World): Boolean {
        val p = w.player.box
        return tiles(round, group).any { (x, y) ->
            (y == 0 || !solid(round, round.map.grid[y - 1][x])) && abs(x + 0.5f - p.cx) <= 6f && y >= p.b - 0.5f
        }
    }

    /** The weight of one action, see [Weight]; [w] is the world the moment it ran. */
    fun weigh(a: Action, round: Level, w: World): Weight = when (a) {
        is Action.Fall, is Action.Move, is Action.Chase, is Action.Saw, is Action.PathSaw, is Action.Blink, is Action.Clock,
        is Action.Laser, is Action.HeatSpike, is Action.Ghost, is Action.FrameCrack -> Weight.LETHAL
        is Action.Show -> if (round.glyph(a.group)?.spike == true) Weight.LETHAL else Weight.NONE
        is Action.Hide -> if (round.glyph(a.group)?.spike != true && floorNear(round, a.group, w)) Weight.LETHAL else Weight.NONE
        is Action.DoorTo, is Action.Gravity, is Action.Swap, is Action.Reroute, is Action.FanSet, is Action.Toggle, is Action.BitFlip,
        is Action.FakeWin, is Action.Extend, is Action.Undo, is Action.Tilt, is Action.Slope -> Weight.ROUTE
        is Action.Belt -> {
            val xs = tiles(round, a.group).map { it.first + 0.5f }
            val toDoor = w.door.tx + 0.6f - (if (xs.isEmpty()) w.player.box.cx else xs.average().toFloat())
            if (toDoor * a.speed < 0f) Weight.LETHAL else Weight.NONE
        }
        is Action.Power -> when (kind(round, a.id)) {
            "laser", "trace" -> if (a.on) Weight.LETHAL else Weight.NONE
            "portal", "fan" -> Weight.ROUTE
            "belt" -> Weight.NONE
            else -> if (a.on) Weight.NONE else Weight.LETHAL
        }
        is Action.Circuit -> if (a.group.isUpperCase() == a.on) Weight.LETHAL else Weight.NONE
        else -> Weight.NONE
    }

    /** One clean run of [round] of [level], with its moments and idle time. */
    fun cleanRun(level: Level, round: Int, solution: Solution): Bot = play(level, round, solution)

    // ---------- trap ablation: what a trap really does ----------

    /**
     * How a run ended, to tell whether taking a trap out changed anything: the state, the time and where the player
     * was ([Bot.world]), or that the run threw (a trap the rest of the room depends on, say a portal a later trap
     * reroutes, cannot be taken out).
     */
    data class Outcome(val state: WorldState?, val time: Int, val x: Int, val y: Int) {
        companion object {
            fun of(b: Bot) = Outcome(b.world.state, Math.round(b.world.time / Bot.DT), Math.round(b.world.player.box.cx * 100), Math.round(b.world.player.box.b * 100))
            val THREW = Outcome(null, 0, 0, 0)
        }
    }

    /** Plays [run] and returns how it ended; a run that throws is [Outcome.THREW]. */
    fun outcome(run: () -> Bot): Outcome = try { Outcome.of(run()) } catch (_: RuntimeException) { Outcome.THREW }

    /** [round] (one of [Level.rounds], or a level of one round) without the traps in [drop]: same map, start, legend and hint. */
    fun without(round: Level, drop: Set<Trap>): Level {
        val grid = round.map.grid
        return Level(round.name, round.intro, round.legend, round.traps.filterNot { it in drop }, round.start, hint = round.hint, rooms = round.rooms) {
            for (y in grid.indices) for (x in grid[y].indices) this.grid[y][x] = grid[y][x]
        }
    }

    /**
     * [round] with the actions in [drop] (the very instances, also inside a [Action.FakeWin]) taken out of its traps: the
     * trap still goes off and does the rest (a gate it opens, a line it says), only what the dropped actions did is gone.
     */
    fun withoutActions(round: Level, drop: Collection<Action>): Level {
        fun keep(a: Action) = drop.none { it === a }
        fun strip(acts: List<Action>): List<Action> = acts.filter(::keep).map { a -> if (a is Action.FakeWin) a.copy(then = strip(a.then)) else a }
        val grid = round.map.grid
        // a trap keeps its identity when nothing of it is dropped, so traps can still be told apart by instance
        val traps = round.traps.map { t -> if (flatten(t.actions).all(::keep)) t else Trap(t.trigger, strip(t.actions), t.delay) }
        return Level(round.name, round.intro, round.legend, traps, round.start, hint = round.hint, rooms = round.rooms) {
            for (y in grid.indices) for (x in grid[y].indices) this.grid[y][x] = grid[y][x]
        }
    }

    /** The patient probe of the teeth test: from the moment [triggered] held, stand still [PROBE_WAIT] s, then play the rest of [solution]. */
    fun patientProbe(level: Level, round: Int, solution: Solution, triggered: Float): Bot {
        val b = Bot(level, round, probe = Bot.Probe(Bot.Probe.Kind.WAIT, PROBE_WAIT) { it.world.time >= triggered - 1e-4f })
        try { b.solution() } catch (_: Bot.ProbeDone) {}
        return b
    }

    /** The reckless probe of the teeth test: from the moment [triggered] held, keep holding the key (right if none) [PROBE_RUN] s. */
    fun recklessProbe(level: Level, round: Int, solution: Solution, triggered: Float): Bot {
        val b = Bot(level, round, probe = Bot.Probe(Bot.Probe.Kind.RUN, PROBE_RUN) { it.world.time >= triggered - 1e-4f })
        try { b.solution() } catch (_: Bot.ProbeDone) {}
        return b
    }

    /**
     * The probes trap ablation compares ([decorations]): the clean run, the run H3 measures without idle waits, holding
     * right, holding right hopping every 0.7 s, waiting it out ([PROBE_WAIT] s still, then holding right), and for a
     * trap that went off in the clean run the two teeth probes from its trigger (stand still, run through).
     */
    private fun baseProbes(level: Level, round: Int, solution: Solution): List<(Level, Int) -> Bot> = listOf(
        { l, r -> play(l, r, solution) },
        { l, r -> Bot(l, r, skipIdle = true).apply(solution) },
        { l, r -> naive(l, r, null) },
        { l, r -> naive(l, r, 0.7f) },
        { l, r -> naive(l, r, null, PROBE_WAIT) },
    )

    private val decorationCache = java.util.Collections.synchronizedMap(java.util.IdentityHashMap<Level, MutableMap<Any, Set<Trap>>>())

    /**
     * Trap ablation, the principled answer to "a trap that does nothing": for every trap of [round] of [level] that
     * weighs something ([Weight]) and went off in one of the probes (together with the traps on the same trigger: they
     * are one moment) ([baseProbes]: the clean run, the brisk run, holding
     * right plain or hopping, waiting it out, and the teeth probes from its trigger), the room is played again without
     * it. If no probe ends any differently (state, time, place), the trap is **decoration**: it does not count for the
     * density H3, for the teeth H15, for the dominant family H20/H21, nor as the trap that excuses a death at the start
     * H17. A trap whose removal makes a run throw (something later needs it) is no decoration.
     */
    fun decorations(level: Level, round: Int, solution: Solution): Set<Trap> {
        val stage = level.rounds[round]
        return decorationCache.getOrPut(stage) { java.util.Collections.synchronizedMap(java.util.IdentityHashMap()) }.getOrPut(solution) {
            val probes = baseProbes(level, round, solution)
            val runs = probes.map { p -> try { p(level, round) } catch (_: RuntimeException) { null } }
            val base = runs.map { it?.let(Outcome::of) ?: Outcome.THREW }
            val clean = runs[0]
            // what weighed something when it went off, in any probe
            val weighed = runs.filterNotNull().flatMap { b -> b.moments.flatMap { m -> m.parts.filter { it.weight != Weight.NONE }.map { it.trap } } }.toSet()
            val firedAt = clean?.moments?.flatMap { m -> m.parts.map { it.trap to m.triggered } }?.toMap().orEmpty()
            // traps with the same trigger go off together and are one moment: they are taken out together (six tiles of
            // one bridge that crumble one after the other are one trap, not six that each change nothing on their own)
            stage.traps.filter { it in weighed }.groupBy { it.trigger }.values.filter { unit ->
                val cut = without(stage, unit.toSet())
                val same = probes.indices.all { k -> base[k] == outcome { probes[k](cut, 0) } }
                same && (unit.firstNotNullOfOrNull { firedAt[it] }?.let { at ->
                    outcome { patientProbe(level, round, solution, at) } == outcome { patientProbe(cut, 0, solution, at) } &&
                        outcome { recklessProbe(level, round, solution, at) } == outcome { recklessProbe(cut, 0, solution, at) }
                } ?: true)
            }.flatten().toSet()
        }
    }

    /** The moments of [bot] (a run of [round] of [level]) as they count: decoration ([decorations]) left out. */
    fun counted(bot: Bot, level: Level, round: Int, solution: Solution): List<Moment> {
        val deco = decorations(level, round, solution)
        return bot.moments.map { it.without(deco) }
    }

    /**
     * H3 (recipe v2, density over duration): the clean run wins, lasts at least [min] (also with idle waits skipped,
     * see [cleanRunViolations]), never goes more than [GAP] s without a real trap (from the start to the first, between
     * two, from the last to the door), and the player stands still at most [WAIT_SHARE] of it, at most [WAIT_STRETCH] s
     * in one go. Decoration ([decorations]) is no real trap.
     */
    fun densityViolations(level: Level, round: Int, solution: Solution, min: Float): List<String> {
        val clean = cleanRun(level, round, solution)
        val where = "${level.name.en} round ${round + 1}"
        if (clean.world.state != WorldState.WON) return listOf("$where: the solution ends ${clean.world.state} at t=%.2f".format(clean.world.time))
        val out = ArrayList(cleanRunViolations(level, round, solution, min))
        val bot = measured(level, round, solution)
        val w = bot.world
        val run = if (bot.skipIdle) " (without its idle waits, which the room does not need)" else ""
        val marks = (listOf(0f) + counted(bot, level, round, solution).filter { it.real }.map { it.time } + w.time).sorted()
        for ((a, b) in marks.zipWithNext()) if (b - a > GAP + 1e-3f) {
            val what = if (a == 0f && b == w.time) "start to door" else if (a == 0f) "start to first trap" else if (b == w.time) "last trap to door" else "between traps"
            out += "$where: no real trap for %.2f s (t=%.2f–%.2f, %s; max %.0f s)%s".format(b - a, a, b, what, GAP, run)
        }
        if (bot.idleTime > WAIT_SHARE * w.time + 1e-3f) {
            out += "$where: stands still %.1f %% of the run (%.2f of %.2f s, max %.0f %%)".format(100 * bot.idleTime / w.time, bot.idleTime, w.time, 100 * WAIT_SHARE)
        }
        if (bot.longestIdle > WAIT_STRETCH + 1e-3f) out += "$where: stands still %.2f s in one go (max %.1f s)".format(bot.longestIdle, WAIT_STRETCH)
        return out
    }

    /**
     * The run H3 measures: the clean run, or, when the solution still wins with every idle command skipped, that brisk
     * run (waits the room does not need do not stretch the gaps between traps).
     */
    fun measured(level: Level, round: Int, solution: Solution): Bot {
        val clean = cleanRun(level, round, solution)
        if (clean.world.state != WorldState.WON) return clean
        val brisk = Bot(level, round, skipIdle = true).apply(solution)
        return if (brisk.world.state == WorldState.WON && brisk.world.time < clean.world.time - 1e-3f) brisk else clean
    }

    /** The real traps of the run H3 measures ([measured]), for reports; decoration is named apart. */
    fun timeline(level: Level, round: Int, solution: Solution): String {
        val bot = measured(level, round, solution)
        val brisk = if (bot.skipIdle) ", idle waits skipped (not needed)" else ""
        val deco = decorations(level, round, solution)
        val dropped = bot.moments.filter { m -> m.real && m.parts.any { it.trap in deco } }
        return "run %.2f s (%s%s), still %.2f s, real traps: %s%s".format(bot.world.time, bot.world.state, brisk, bot.idleTime,
            counted(bot, level, round, solution).filter { it.real }.joinToString().ifEmpty { "none" },
            if (dropped.isEmpty()) "" else "; decoration (changes nothing): ${dropped.joinToString()}")
    }

    /** H12: a rotating mechanic and how many levels of an act may use it ([has]: the level uses it). */
    class Cap(val what: String, val max: Int, val has: (Level) -> Boolean, val finaleExtra: Int = 0)

    /** A [Cap] test: some action of some round of the level satisfies [p]. */
    private fun uses(p: (Action) -> Boolean): (Level) -> Boolean = { l -> levelActions(l).any(p) }

    /** A laser lit for at most [GATE_ON] s at a time: a gate, whatever fires it and whatever its off time. */
    const val GATE_ON = 2f
    /** A laser (or clocked live trace) that cycles with an off time under this is a gate, whatever its on time. */
    const val GATE_OFF = 10f

    /**
     * H12, timed laser gates: the ids of [round]'s lasers and live traces that are gates. The lit window is what counts,
     * from any trigger ([Trigger.Landed], [Trigger.PastX], zones, but also [Trigger.Pressed], [Trigger.After],
     * [Trigger.Idle] ...):
     *
     * - a [Action.Laser] (start or trap) that cycles with an off time under [GATE_OFF] s;
     * - a [Action.Laser] lit for at most [GATE_ON] s at a time that cycles (off > 0, also off ≥ [GATE_OFF] from the
     *   start) or that a trap fires (a flash as you land, pass or press a pad is a gate, also a one-shot);
     * - a laser a trap switches on ([Action.Power] on, or a [Action.Laser] fired by a trap) and a trap switches off
     *   ([Action.Power] off): the pair is a gate, unless every such pair hangs on the same trigger and the beam stays
     *   lit longer than [GATE_ON] s (delay of the off minus delay of the on). Pairs on different triggers count as a
     *   gate: the player's timing decides how long the beam is lit;
     * - a trap that switches on ([Action.Power]) a laser whose own cycle is lit for at most [GATE_ON] s;
     * - a live trace (an uppercase circuit) on a [Action.Clock] with an off time under [GATE_OFF] s or an on time of at
     *   most [GATE_ON] s: an electric gate.
     *
     * A beam that is never dark (off 0) from the start and that no trap switches is a fixture, no gate.
     */
    fun laserGates(round: Level): Set<Char> {
        val out = LinkedHashSet<Char>()
        val start = flatten(round.start)
        val fired = round.traps.flatMap { t -> flatten(t.actions).map { t to it } }
        val lasers = (start + fired.map { it.second }).filterIsInstance<Action.Laser>()
        val ids = lasers.map { it.id }.toSet()
        for (l in lasers) if (l.off > 0f && l.off < GATE_OFF) out += l.id
        for (l in start.filterIsInstance<Action.Laser>()) if (l.off > 0f && l.on <= GATE_ON) out += l.id
        for ((_, a) in fired) if (a is Action.Laser && a.on <= GATE_ON) out += a.id
        for (id in ids) {
            val ons = fired.filter { (_, a) -> (a is Action.Laser && a.id == id) || (a is Action.Power && a.id == id && a.on) }
            val offs = fired.filter { (_, a) -> a is Action.Power && a.id == id && !a.on }
            if (ons.isNotEmpty() && offs.isNotEmpty()) {
                val long = ons.all { (t1, _) -> offs.all { (t2, _) -> t1.trigger == t2.trigger && t2.delay - t1.delay > GATE_ON } }
                if (!long) out += id
            }
            val short = lasers.any { it.id == id && it.off > 0f && it.on <= GATE_ON }
            if (short && fired.any { (_, a) -> a is Action.Power && a.id == id && a.on }) out += id
        }
        for (a in start + fired.map { it.second }) {
            if (a is Action.Clock && a.group.isUpperCase() && a.group !in ids && a.off > 0f && (a.off < GATE_OFF || a.on <= GATE_ON)) out += a.group
        }
        return out
    }

    /** H12: some round of [level] has a timed laser gate ([laserGates]). */
    fun hasLaserGate(level: Level): Boolean = level.rounds.any { laserGates(it).isNotEmpty() }

    /** H12, counted from the actions in the level code (all rounds), per act of 16. */
    val CAPS = listOf(
        Cap("door flees (DoorTo)", 1, uses { it is Action.DoorTo }),
        // the finale combines the act on purpose (as in H6): it may bring a switch on top of the three (W2 act 2: 17, 18, 20 + 32)
        Cap("pads/switches (Pad)", 3, uses { it is Action.Pad }, finaleExtra = 1),
        Cap("timed laser gates", 3, ::hasLaserGate),
        Cap("Blink-wait", 3, uses { it is Action.Blink }),
        Cap("gravity flip", 1, uses { it is Action.Gravity && it.flipped }),
        Cap("controls swap", 2, uses { it is Action.Swap && it.on }),
    )

    /** Every action of every round of [level], [Action.FakeWin.then] included. */
    fun levelActions(level: Level): List<Action> = level.rounds.flatMap { actions(it) }

    /** H12: per act, no mechanic of [CAPS] in more of [levels] (number → level) than its cap. */
    fun rotationViolations(levels: Map<Int, Level>): List<String> = levels.keys.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
        CAPS.mapNotNull { cap ->
            val with = ns.filter { n -> cap.has(levels.getValue(n)) }.sorted()
            // a finale may add up to finaleExtra levels beyond the cap: it only counts against the cap past that
            val capped = if (with.count(::isFinale) <= cap.finaleExtra) with.filterNot(::isFinale) else with
            if (capped.size > cap.max) "act $a: ${cap.what} in ${with.size} levels $with (max ${cap.max}${if (cap.finaleExtra > 0) ", plus the finale" else ""})" else null
        }
    }

    /**
     * H13: the effect families of a round, from its actions (start included) and its map. Talk, cards and pads are no
     * effect (pads are how the player switches an effect). A copper rail declared at the start that only pads switch is a
     * lock (puzzle block R1), not an effect either.
     *
     * drop: Fall, Hide of a solid group · spikes: Show of spikes · move: Move, Chase, Tilt, Slope (with `split`, see
     * [familyOf]: Move is floor-move, wall-move or ceiling-move, a Move that drops a floor is drop) · door: DoorTo, FakeWin,
     * Extend · saw: Saw, PathSaw · controls: Swap · gravity: Gravity, Flip · portal: Portal, Reroute, Power of a portal ·
     * belt: Belt, Power of a belt · laser: Laser, Power of a laser · blink: Blink · heat: Heat, Heatsink, HeatSpike ·
     * power: Clock, Toggle, BitFlip, Power of a circuit, a live trace, a Circuit set by a trap · fan: Fan, FanSet, Power of
     * a fan · meta: Undo, Ghost, PauseTrap, FrameCrack, Roll · secret: a hidden solid or bonk block in the map, Show of a
     * solid group.
     */
    fun families(round: Level): Set<String> {
        val out = LinkedHashSet<String>()
        val trapActions = flatten(round.traps.flatMap { it.actions })
        for (a in flatten(round.start) + trapActions) familyOf(a, round, trapActions)?.let { out += it }
        val secret = round.map.grid.any { row -> row.any { c -> round.glyph(c)?.let { !it.spike && (it.hidden || it.bonk) } == true } }
        if (secret) out += "secret"
        return out
    }

    /**
     * The effect family of one action (see [families]), null for talk, cards, pads and plain state. [trapActions]: the
     * actions traps fire. With [split] (the adjacency rule H20 and the wall cap H21) a [Action.Move] is told apart by
     * what it moves ([moveFamily]: floor-move, wall-move, ceiling-move) and a floor that moves down counts as drop; H13
     * counts without it, so moving two things stays one family there.
     */
    fun familyOf(a: Action, round: Level, trapActions: List<Action> = flatten(round.traps.flatMap { it.actions }), split: Boolean = false): String? = when (a) {
        // a ceiling that falls is the ceiling coming down, not the floor going away
        is Action.Fall -> if (split && hangs(round, a.group)) "ceiling-move" else "drop"
        is Action.Hide -> if (round.glyph(a.group)?.spike != true) "drop" else null
        is Action.Show -> if (round.glyph(a.group)?.spike == true) "spikes" else "secret"
        is Action.Move -> if (split) moveFamily(a, round) else "move"
        // a stalker is a wall that walks with you: under the split it is a wall-move, whatever shape it has
        is Action.Chase -> if (split) "wall-move" else "move"
        is Action.Tilt, is Action.Slope -> "move"
        is Action.DoorTo, is Action.FakeWin, is Action.Extend -> "door"
        is Action.Saw, is Action.PathSaw -> "saw"
        is Action.Swap -> "controls"
        is Action.Gravity, is Action.Flip -> "gravity"
        is Action.Portal, is Action.Reroute -> "portal"
        is Action.Belt -> "belt"
        is Action.Laser -> "laser"
        is Action.Blink -> "blink"
        is Action.Heat, is Action.Heatsink, is Action.HeatSpike -> "heat"
        is Action.Clock, is Action.Toggle, is Action.BitFlip -> "power"
        is Action.Circuit -> if (a.group.isUpperCase() || a in trapActions) "power" else null
        is Action.Power -> when (kind(round, a.id)) { "rail", "trace" -> "power"; else -> kind(round, a.id) }
        is Action.Fan, is Action.FanSet -> "fan"
        is Action.Undo, is Action.Ghost, is Action.PauseTrap, is Action.FrameCrack, is Action.Roll -> "meta"
        else -> null
    }

    /**
     * What a [Action.Move] moves, by its motion first and its shape second. A group that hangs from the ceiling
     * ([hangs]) is a `ceiling-move` when it is a flat slab (at least as wide as high), however it moves, and when it moves
     * straight up or down (a rack lowered from the ceiling). Anything else that moves sideways (dx != 0) and is at least 2 tiles high is a `wall-move` (a flat wide wall
     * that slides at you is a wall, not a floor). What is left moves down (dy > 0) as a `drop` like a Fall, unless it
     * moves straight up or down (dx == 0) and has more free side than top faces (a column, a block standing on the
     * floor): then it is a `wall-move` too. The rest (a platform, a piece of the ground) is a `floor-move`. A group that
     * is not in the map counts as `floor-move`.
     */
    /**
     * [group] hangs from the ceiling: every tile of its top row has something solid directly above it (a tile of another
     * group, the border) or is the top row of the map. A floor block that merely touches a side wall does not hang.
     */
    fun hangs(round: Level, group: Char): Boolean {
        val ts = tiles(round, group)
        if (ts.isEmpty()) return false
        val top = ts.minOf { it.second }
        return ts.filter { it.second == top }.all { (x, y) -> y == 0 || round.map.grid[y - 1][x].let { it != group && solid(round, it) } }
    }

    fun moveFamily(a: Action.Move, round: Level): String {
        val ts = tiles(round, a.group)
        if (ts.isEmpty()) return "floor-move"
        val grid = round.map.grid
        fun at(x: Int, y: Int): Char? = grid.getOrNull(y)?.getOrNull(x)
        val free = { c: Char? -> c != null && c != a.group && !solid(round, c) }
        val hanging = hangs(round, a.group)
        val h = ts.maxOf { it.second } - ts.minOf { it.second } + 1
        val w = ts.maxOf { it.first } - ts.minOf { it.first } + 1
        if (hanging && (h <= w || a.dx == 0f)) return "ceiling-move"
        if (a.dx != 0f && h >= 2) return "wall-move"
        if (a.dx == 0f) {
            val top = ts.count { (x, y) -> free(at(x, y - 1)) }
            val side = ts.sumOf { (x, y) -> listOf(at(x - 1, y), at(x + 1, y)).count(free) }
            if (side > top) return "wall-move"
        }
        return if (a.dy > 0f) "drop" else "floor-move"
    }

    /** H13: rounds of level [n] with more than [MAX_FAMILIES] effect families per room (an act finale: one more). */
    fun familyViolations(n: Int, level: Level): List<String> = level.rounds.withIndex().mapNotNull { (r, round) ->
        val f = families(round)
        val max = MAX_FAMILIES * level.rooms + if (isFinale(n)) 1 else 0
        if (f.size > max) "${level.name.en} round ${r + 1}: ${f.size} effect families $f (max $max)" else null
    }

    /**
     * H15, teeth: for every lethal moment of the clean run ([Weight.LETHAL], decoration left out, see [decorations]), two
     * probes start from the step its trigger held (the moment the player can still decide; a delayed trap runs its
     * actions later). The patient one stands still [PROBE_WAIT] s and then plays the rest of the solution
     * ([patientProbe]); the reckless one keeps holding the key it held (right if none) for [PROBE_RUN] s without jumping
     * ([recklessProbe]). The trap has teeth only if it is what beats one of them: the patient one loses, or the reckless
     * one dies, **and** the same probe ends differently in the room without the moment's lethal actions ([withoutActions];
     * what else the moment does, say open a gate, stays). A probe
     * that dies to something else (spikes that were there anyway) proves nothing. Route changes (door, gravity,
     * controls, portals) have their teeth in the new way, which H2 checks for the whole room, and are not probed.
     */
    fun teethViolations(level: Level, round: Int, solution: Solution): List<String> {
        val clean = cleanRun(level, round, solution)
        if (clean.world.state != WorldState.WON) return emptyList()
        val stage = level.rounds[round]
        val lethal = counted(clean, level, round, solution).filter { it.weight == Weight.LETHAL }
        return lethal.mapNotNull { m ->
            val patient = patientProbe(level, round, solution, m.triggered)
            val reckless = recklessProbe(level, round, solution, m.triggered)
            // only what can kill is taken out: a moment that opens a gate and fires a beam keeps the open gate
            val cut = withoutActions(stage, m.lethalActions)
            val patientBitten = patient.world.state != WorldState.WON &&
                Outcome.of(patient) != outcome { patientProbe(cut, 0, solution, m.triggered) }
            val recklessBitten = !reckless.ranThrough &&
                Outcome.of(reckless) != outcome { recklessProbe(cut, 0, solution, m.triggered) }
            when {
                patientBitten || recklessBitten -> null
                patient.world.state == WorldState.WON && reckless.ranThrough ->
                    "${level.name.en} round ${round + 1}: the trap at $m has no teeth (standing still ${PROBE_WAIT.toInt()} s wins, running straight on survives)"
                else ->
                    "${level.name.en} round ${round + 1}: the trap at $m has no teeth (what beats the probes is not this trap: without it, standing still ${PROBE_WAIT.toInt()} s and running straight on end the same)"
            }
        }
    }

    /**
     * H9: every rematch round of [level] is at least as long as round 1 (clean runs of [solutions], round 1 first) and
     * round 1's solution does not win it.
     */
    fun rematchViolations(level: Level, solutions: List<Solution>): List<String> {
        if (level.rounds.size < 2 || solutions.isEmpty()) return emptyList()
        val first = cleanRun(level, 0, solutions[0]).world
        val out = ArrayList<String>()
        for (r in 1 until level.rounds.size) {
            if (play(level, r, solutions[0]).world.state == WorldState.WON) out += "${level.name.en} round ${r + 1}: round 1's solution wins it"
            val s = solutions.getOrNull(r) ?: continue
            val w = cleanRun(level, r, s).world
            if (first.state == WorldState.WON && w.state == WorldState.WON && w.time < first.time - 0.05f) {
                out += "${level.name.en} round ${r + 1}: clean run %.2f s, shorter than round 1 (%.2f s)".format(w.time, first.time)
            }
        }
        return out
    }

    /**
     * H17: holding right from the spawn dies within [FILLER_WINDOW] s, and no trap that went off before is what killed
     * the player. Excused is only a death that a real trap ([Weight] above NONE) causes: the room without every real action
     * ([Weight] above NONE) of the traps that went off before the death ([withoutActions]) has to end that run differently (alive, or dead somewhere else or at
     * another time). A trap that changes nothing near the spawn (a Toggle, Swap or Fall far away, decoration) does not
     * excuse spikes in front of it. A pad press does not count as a trap that went off (a [Trigger.Pressed] moment): a
     * pad near the spawn must not excuse a death that happens right at the start.
     */
    fun fillerDeathViolations(level: Level, round: Int): List<String> {
        val bot = Bot(level, round).right(FILLER_WINDOW)
        val w = bot.world
        if (w.state != WorldState.DEAD) return emptyList()
        val causes = bot.moments.filter { it.trigger !is Trigger.Pressed && it.time <= w.time }
            .flatMap { m -> m.parts.filter { it.weight != Weight.NONE && it.time <= w.time }.flatMap { it.real } }
        if (causes.isNotEmpty()) {
            val cut = withoutActions(level.rounds[round], causes)
            if (outcome { Bot(cut, 0).right(FILLER_WINDOW) } != Outcome.of(bot)) return emptyList()
            return listOf("${level.name.en} round ${round + 1}: holding right dies at t=%.2f (x=%.1f), and it dies the same without the traps that went off before (filler death)".format(w.time, w.player.box.cx))
        }
        return listOf("${level.name.en} round ${round + 1}: holding right dies at t=%.2f (x=%.1f) before any trap went off (filler death)".format(w.time, w.player.box.cx))
    }

    private fun hiddenBlock(round: Level, c: Char) = round.glyph(c)?.let { !it.spike && (it.hidden || it.bonk) } == true

    /**
     * H8, the card lint: what a card may sit on, by the other actions of its trap ([Action.FakeWin.then] included) or,
     * for a ghost block, by its trigger.
     *
     * COLLAPSE, CRUMBLE: Fall, Hide of a solid group, Blink (CRUMBLE), Move (COLLAPSE) · SINKING: Fall, Hide, Move ·
     * SPIKE_SEED: Show of spikes, Laser · HEADBUTT: Fall, Move · SHY_DOOR: DoorTo · DECOY: FakeWin, DoorTo, Reroute ·
     * UPSIDE_DOWN: Gravity(true), Flip · TWISTED: Swap(true) · DEVIL_SAW: Saw, PathSaw · GHOST_BLOCK: Show, Hide, Fall
     * or Move of a hidden solid or bonk group, or a trap triggered by touching one · STALKER: Chase · UNDO: Undo ·
     * ANNEX: Extend · SHORT_CIRCUIT: Power, Circuit, Clock, Toggle, BitFlip · BIT_FLIP: BitFlip, Toggle, Swap ·
     * OVERCLOCKED: HeatSpike, Heat · THROTTLE: Heat, Fan, FanSet, Laser · BACKDRAFT: Fan, FanSet, Belt, Power of a fan ·
     * BIOS: BitFlip, Toggle, Power, FakeWin, PauseTrap · GRAND_FINALE: anything.
     */
    fun cardFits(card: Card, trap: Trap, round: Level): Boolean {
        val acts = flatten(trap.actions)
        fun any(p: (Action) -> Boolean) = acts.any(p)
        val solidHide = { a: Action -> a is Action.Hide && round.glyph(a.group)?.spike != true }
        return when (card) {
            Card.COLLAPSE -> any { it is Action.Fall || it is Action.Move || solidHide(it) }
            Card.CRUMBLE -> any { it is Action.Fall || it is Action.Blink || solidHide(it) }
            Card.SINKING -> any { it is Action.Fall || it is Action.Move || solidHide(it) }
            Card.SPIKE_SEED -> any { (it is Action.Show && round.glyph(it.group)?.spike == true) || it is Action.Laser }
            Card.HEADBUTT -> any { it is Action.Fall || it is Action.Move }
            Card.SHY_DOOR -> any { it is Action.DoorTo }
            Card.DECOY -> any { it is Action.FakeWin || it is Action.DoorTo || it is Action.Reroute }
            Card.UPSIDE_DOWN -> any { (it is Action.Gravity && it.flipped) || it is Action.Flip }
            Card.TWISTED -> any { it is Action.Swap && it.on }
            Card.DEVIL_SAW -> any { it is Action.Saw || it is Action.PathSaw }
            Card.GHOST_BLOCK -> (trap.trigger as? Trigger.Touch)?.let { hiddenBlock(round, it.group) } == true || any { a ->
                when (a) {
                    is Action.Show -> hiddenBlock(round, a.group)
                    is Action.Hide -> hiddenBlock(round, a.group)
                    is Action.Fall -> hiddenBlock(round, a.group)
                    is Action.Move -> hiddenBlock(round, a.group)
                    else -> false
                }
            }
            Card.STALKER -> any { it is Action.Chase }
            Card.UNDO -> any { it is Action.Undo }
            Card.ANNEX -> any { it is Action.Extend }
            Card.SHORT_CIRCUIT -> any { it is Action.Power || it is Action.Circuit || it is Action.Clock || it is Action.Toggle || it is Action.BitFlip }
            Card.BIT_FLIP -> any { it is Action.BitFlip || it is Action.Toggle || it is Action.Swap }
            Card.OVERCLOCKED -> any { it is Action.HeatSpike || it is Action.Heat }
            Card.THROTTLE -> any { it is Action.Heat || it is Action.Fan || it is Action.FanSet || it is Action.Laser }
            Card.BACKDRAFT -> any { it is Action.Fan || it is Action.FanSet || it is Action.Belt || (it is Action.Power && kind(round, it.id) == "fan") }
            Card.BIOS -> any { it is Action.BitFlip || it is Action.Toggle || it is Action.Power || it is Action.FakeWin || it is Action.PauseTrap }
            Card.GRAND_FINALE, Card.BLUFF -> true
        }
    }

    /** H8: cards ([Action.Play], bluffs aside) that do not fit the trap they sit on, per round of [level]. */
    fun cardLintViolations(level: Level): List<String> = level.rounds.withIndex().flatMap { (r, round) ->
        round.traps.flatMap { t ->
            flatten(t.actions).filterIsInstance<Action.Play>().filterNot { cardFits(it.card, t, round) }.map { p ->
                val on = flatten(t.actions).filterNot { it is Action.Say || it is Action.Play }.joinToString("+") { it::class.simpleName ?: "?" }.ifEmpty { "nothing" }
                "${level.name.en} round ${r + 1}: ${p.card} sits on $on"
            }
        }
    }

    // ---------- cards still waiting for their first level ----------

    /**
     * Cards that `everyLevelPlaysACard` lets off for now: ANNEX (U18, docs/LEVEL_DESIGN_V2.md §5a), but only while no level
     * plays it ([played]: the cards the shipped levels deal). As soon as one level plays it, nothing is pending any more.
     */
    fun pendingCards(played: Collection<Card>): Set<Card> = if (Card.ANNEX in played) emptySet() else setOf(Card.ANNEX)

    // ---------- say lint ----------

    /** A line of Mephi's in a level: where it comes from ("Say", "intro" or "hint") and the text. */
    class Line(val source: String, val text: T)

    /** Every line a level says: all [Action.Say]s of every round, the intro of every round and the hint. */
    fun lines(level: Level): List<Line> = level.rounds.flatMapIndexed { r, round ->
        listOf(Line("intro (round ${r + 1})", round.intro)) +
            actions(round).filterIsInstance<Action.Say>().map { Line("Say (round ${r + 1})", it.text) }
    } + listOfNotNull(level.hint?.let { Line("hint", it) })

    private fun norm(s: String) = s.trim().lowercase().replace(Regex("\\s+"), " ")

    /** A line as the say lint compares it: case, punctuation and spacing aside ("Nope." and "nope!" are one line). */
    fun sayKey(s: String) = s.lowercase().replace(Regex("[\\p{P}\\p{S}]+"), " ").trim().replace(Regex("\\s+"), " ")

    /**
     * Say lint: no line (a [Action.Say], an intro or a hint) is said word for word in two levels of the same act, in
     * English or in German (case, punctuation and spacing aside: [sayKey]). The same line twice in one level (a rematch repeating itself) is
     * fine. [levels]: number → level. No allowlist: fix the line. Near-duplicates are only warned about,
     * see [sayNgramWarnings].
     */
    fun sayViolations(levels: Map<Int, Level>): List<String> =
        levels.keys.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
            listOf<Pair<String, (T) -> String>>("EN" to { it.en }, "DE" to { it.de }).flatMap { (lang, pick) ->
                val seen = LinkedHashMap<String, MutableMap<Int, MutableSet<String>>>()
                val first = HashMap<String, String>()
                for (n in ns.sorted()) for (l in lines(levels.getValue(n))) {
                    // a line of nothing but punctuation ("...", "?!") still counts, as itself
                    val key = sayKey(pick(l.text)).ifEmpty { norm(pick(l.text)) }
                    if (key.isEmpty()) continue
                    first.putIfAbsent(key, norm(pick(l.text)))
                    seen.getOrPut(key) { LinkedHashMap() }.getOrPut(n) { LinkedHashSet() } += l.source
                }
                seen.filter { it.value.size > 1 }.map { (key, at) ->
                    "act $a $lang: \"${first.getValue(key)}\" in levels ${at.entries.joinToString { "${it.key} (${it.value.joinToString()})" }}"
                }
            }
        }

    /** Words too common to make two lines alike (English and German); the n-gram warning ignores them. */
    val COMMON_WORDS = setOf(
        "the", "a", "an", "and", "or", "but", "of", "to", "in", "on", "at", "for", "with", "is", "are", "was", "were", "be", "it", "its",
        "you", "your", "i", "me", "my", "we", "this", "that", "not", "no", "do", "dont", "don't", "did", "so", "as", "if", "then", "now",
        "der", "die", "das", "den", "dem", "des", "ein", "eine", "einen", "einem", "und", "oder", "aber", "von", "zu", "zum", "zur", "in",
        "im", "auf", "an", "am", "mit", "ist", "sind", "war", "es", "du", "dich", "dir", "dein", "deine", "ich", "mich", "mir", "wir",
        "nicht", "kein", "keine", "so", "wenn", "dann", "jetzt", "doch", "nur", "auch", "noch", "mal", "hier",
    )

    private fun words(s: String) = norm(s).split(Regex("[^\\p{L}\\p{N}']+")).filter { it.isNotEmpty() && it !in COMMON_WORDS }

    /**
     * Say lint, report only: two levels of the same act that share [n] or more consecutive words (very common words
     * ([COMMON_WORDS]) dropped first) in a line, in English or in German. Warnings only, they never fail a test; the
     * gate is [sayViolations] (exact repeats). A level repeating itself is fine.
     */
    fun sayNgramWarnings(levels: Map<Int, Level>, n: Int = 3): List<String> =
        levels.keys.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
            listOf<Pair<String, (T) -> String>>("EN" to { it.en }, "DE" to { it.de }).flatMap { (lang, pick) ->
                val seen = LinkedHashMap<List<String>, MutableMap<Int, String>>()
                for (m in ns.sorted()) for (l in lines(levels.getValue(m))) {
                    val ws = words(pick(l.text))
                    for (i in 0..ws.size - n) seen.getOrPut(ws.subList(i, i + n)) { LinkedHashMap() }.putIfAbsent(m, l.source)
                }
                // one warning per pair of levels, naming the shared phrases, instead of one per phrase
                val pairs = LinkedHashMap<List<Int>, MutableList<String>>()
                for ((gram, at) in seen) if (at.size > 1) pairs.getOrPut(at.keys.sorted()) { ArrayList() } += gram.joinToString(" ")
                pairs.map { (lv, grams) -> "act $a $lang: levels $lv share ${grams.joinToString { "\"$it\"" }}" }
            }
        }

    // ---------- adjacent levels ----------

    /**
     * What a round feels like to play, from its clean run: the effect family of every real trap in the order they went
     * off ([families]), the [dominant] families and the player's direction changes ([Bot.shape]). The dominant family is
     * the one with the most *lethal* moments of the clean run ([Weight.LETHAL]; a moment counts for every family of its lethal
     * actions; decoration, see [decorations], does not count). A tie makes every tied family dominant: a cheap early
     * moment cannot take the lead away from the family the room is really about. Empty: no lethal moment at all.
     */
    class Signature(val dominant: Set<String>, val families: List<String>, val shape: List<String>) {
        constructor(dominant: String?, families: List<String>, shape: List<String>) : this(setOfNotNull(dominant), families, shape)
        override fun toString() = "dominant ${dominant.ifEmpty { null }?.joinToString("+")}, traps $families, moves $shape"
    }

    /** The [Signature] of [round] (0-based) of [level], played with [solution]. */
    fun signature(level: Level, solution: Solution, round: Int = 0): Signature {
        val bot = measured(level, round, solution)
        val stage = level.rounds[round]
        val real = counted(bot, level, round, solution).filter { it.real }
        val familiesOf = { acts: List<Action> -> acts.mapNotNull { familyOf(it, stage, split = true) }.distinct() }
        val seq = real.mapNotNull { familiesOf(it.actions).firstOrNull() }
        // a lethal moment counts for the families of its lethal actions only: a bridge that appears or a line of talk
        // riding along does not make its family dominant
        return Signature(dominantFamilies(real.filter { it.weight == Weight.LETHAL }.map { familiesOf(it.lethalActions) }), seq, bot.shape())
    }

    /** The families in most of [lethal] (one list of families per lethal moment, in order); a tie: all of them. Empty if none. */
    fun dominantFamilies(lethal: List<List<String>>): Set<String> {
        val counts = lethal.flatMap { it.distinct() }.groupingBy { it }.eachCount()
        val top = counts.values.maxOrNull() ?: return emptySet()
        return lethal.flatten().distinct().filter { counts[it] == top }.toCollection(LinkedHashSet())
    }

    /** Longest common subsequence of [a] and [b] over the longer one: 1 for the same sequence, 0 for nothing in common. */
    fun similarity(a: List<String>, b: List<String>): Float {
        if (a.isEmpty() || b.isEmpty()) return 0f
        val t = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices) for (j in b.indices) t[i + 1][j + 1] = if (a[i] == b[j]) t[i][j] + 1 else maxOf(t[i][j + 1], t[i + 1][j])
        return t[a.size][b.size].toFloat() / maxOf(a.size, b.size)
    }

    /** The pairs H20 looks at: n and n + 1 of the same act, the second no finale. */
    private fun neighbours(signatures: Map<Int, List<Signature>>) = signatures.keys.sorted().mapNotNull { n ->
        val b = signatures[n + 1] ?: return@mapNotNull null
        if (isFinale(n + 1) || act(n) != act(n + 1)) null else Triple(n, signatures.getValue(n), b)
    }

    /**
     * H20, binary: two consecutive levels of an act (the second no finale) must differ in their dominant effect family
     * ([Signature.dominant]), every round of the one against every round of the other (a rematch is played right before
     * or after the neighbour too). A tie counts with all its families. [signatures]: level → one signature per round,
     * round 1 first. The percentage of [similarity] never decides, see [adjacentReport].
     */
    fun adjacentViolations(signatures: Map<Int, List<Signature>>): List<String> = neighbours(signatures).flatMap { (n, ra, rb) ->
        ra.withIndex().flatMap { (i, a) ->
            rb.withIndex().mapNotNull { (j, b) ->
                val shared = a.dominant intersect b.dominant
                if (shared.isEmpty()) null
                else "levels $n and ${n + 1}${if (ra.size > 1 || rb.size > 1) " (round ${i + 1} vs round ${j + 1})" else ""}: both are dominated by ${shared.joinToString("+")} " +
                    "(dominant ${a.dominant} vs ${b.dominant}; traps ${a.families} vs ${b.families}; moves ${a.shape} vs ${b.shape})"
            }
        }
    }

    private fun perRound(signatures: Map<Int, Signature>): Map<Int, List<Signature>> = signatures.mapValues { listOf(it.value) }

    /** [adjacentViolations] for levels of one round each. */
    @JvmName("adjacentViolationsOfRoundOne")
    fun adjacentViolations(signatures: Map<Int, Signature>): List<String> = adjacentViolations(perRound(signatures))

    /** H20, report only (printed, never failing): how alike two neighbours play, in per cent ([similarity] of the moves of round 1). */
    fun adjacentReport(signatures: Map<Int, List<Signature>>): List<String> = neighbours(signatures).map { (n, ra, rb) ->
        val a = ra.first()
        val b = rb.first()
        "levels $n and ${n + 1}: dominant ${a.dominant} vs ${b.dominant}, moves %.0f %% alike (${a.shape} vs ${b.shape})".format(100 * similarity(a.shape, b.shape))
    }

    @JvmName("adjacentReportOfRoundOne")
    fun adjacentReport(signatures: Map<Int, Signature>): List<String> = adjacentReport(perRound(signatures))

    /** H21: levels per act whose dominant family is wall-move at most. */
    const val WALL_MOVE_CAP = 3

    /** H21: a level counts as dominated by a moving wall if any of its rounds is ([Signature.dominant], ties included). */
    fun wallDominated(rounds: List<Signature>) = rounds.any { "wall-move" in it.dominant }

    /** H21: per act at most [WALL_MOVE_CAP] of the levels in [signatures] (level → signature per round) are dominated by a moving wall. */
    fun wallMoveViolations(signatures: Map<Int, List<Signature>>): List<String> =
        signatures.keys.groupBy(::act).toSortedMap().mapNotNull { (a, ns) ->
            val walls = ns.filter { wallDominated(signatures.getValue(it)) }.sorted()
            if (walls.size > WALL_MOVE_CAP) "act $a: ${walls.size} levels dominated by a moving wall $walls (max $WALL_MOVE_CAP)" else null
        }

    @JvmName("wallMoveViolationsOfRoundOne")
    fun wallMoveViolations(signatures: Map<Int, Signature>): List<String> = wallMoveViolations(perRound(signatures))

    // ---------- rollout blocks (§11): parallel builders share an act ----------

    /**
     * A rollout block of docs/LEVEL_DESIGN_V2.md §11: the levels one builder rebuilds and the share of every per-act cap it
     * may use, so that two blocks built in parallel cannot both eat the act's budget. [budget] keys are [BUDGET_ITEMS];
     * a missing key is not checked (HeatSpike finales outside World 3). `pad` counts like H12: the act finale may bring
     * one switch on top. `rematch` is exact (the 47 rematch levels stay). [card] is the budget of every card, [cards] the
     * exceptions (cards that levels outside the block already play in the act). [forbidden]: an edge level of the block
     * and the dominant families (H20) it must not have, because the neighbour outside the block has (or plans) them.
     */
    class Block(
        val world: Int, val id: String, val levels: Set<Int>, val budget: Map<String, Int>,
        val card: Int, val cards: Map<Card, Int> = emptyMap(), val forbidden: Map<Int, Set<String>> = emptyMap(),
    ) {
        fun cardBudget(c: Card) = cards[c] ?: card
        override fun toString() = "W$world $id"
    }

    /** The items a [Block] budget names, in the column order of §11. */
    val BUDGET_ITEMS = listOf("pad", "gate", "blink", "door", "gravity", "swap", "wall", "spikes", "heatspike", "rematch", "bluff", "annex")

    /** World 2 pilot levels (11–24, act 1 and 2): rebuilt before the rollout. 18, 20 (their rematches), 23 and 24 belong to a block again. */
    val PILOT = (11..24).toSet()

    private fun b(world: Int, id: String, levels: Iterable<Int>, pad: Int, gate: Int, blink: Int, door: Int, gravity: Int, swap: Int, wall: Int,
        spikes: Int, heatspike: Int?, rematch: Int, bluff: Int, annex: Int, card: Int, cards: Map<Card, Int> = emptyMap(), forbidden: Map<Int, Set<String>> = emptyMap()) =
        Block(world, id, levels.toSet(), listOfNotNull("pad" to pad, "gate" to gate, "blink" to blink, "door" to door, "gravity" to gravity, "swap" to swap,
            "wall" to wall, "spikes" to spikes, heatspike?.let { "heatspike" to it }, "rematch" to rematch, "bluff" to bluff, "annex" to annex).toMap(), card, cards, forbidden)

    /** §11, the rollout blocks and their budgets (the table in the doc must say the same, `RolloutBudgetTest`). */
    val ROLLOUT: List<Block> = listOf(
        //     world id      levels                                pad gate blink door grav swap wall spikes heat rematch bluff annex card
        b(1, "A", 7..16, 3, 3, 3, 1, 1, 2, 3, 4, null, 3, 0, 1, 3, forbidden = mapOf(7 to setOf("saw"))),
        b(1, "B", 17..24, 2, 1, 1, 0, 0, 1, 1, 2, null, 4, 1, 0, 1),
        b(1, "C", 25..32, 1, 2, 2, 1, 1, 1, 2, 2, null, 1, 0, 1, 2, forbidden = mapOf(25 to setOf("wall-move"))),
        b(1, "D", 33..40, 0, 1, 1, 1, 1, 1, 0, 2, null, 2, 0, 1, 1, forbidden = mapOf(40 to setOf("ceiling-move"))),
        b(1, "E", 41..48, 3, 2, 2, 0, 0, 1, 3, 2, null, 4, 1, 1, 2),
        b(2, "A", 1..10, 0, 3, 2, 1, 0, 1, 2, 4, null, 3, 1, 0, 3,
            cards = mapOf(Card.HEADBUTT to 1, Card.DEVIL_SAW to 2, Card.COLLAPSE to 2, Card.SINKING to 2, Card.UPSIDE_DOWN to 2, Card.STALKER to 2, Card.GHOST_BLOCK to 2),
            forbidden = mapOf(10 to setOf("saw"))),
        b(2, "B", listOf(18, 20, 23, 24) + (25..32), 3, 3, 3, 1, 1, 1, 2, 4, null, 5, 1, 1, 3,
            cards = mapOf(Card.HEADBUTT to 2, Card.TWISTED to 2, Card.DECOY to 2, Card.DEVIL_SAW to 2),
            forbidden = mapOf(18 to setOf("drop", "wall-move"), 20 to setOf("drop", "wall-move", "belt"), 23 to setOf("saw"))),
        b(2, "C", 33..40, 1, 1, 1, 0, 1, 0, 1, 2, null, 2, 1, 0, 1, forbidden = mapOf(40 to setOf("drop"))),
        b(2, "D", 41..48, 2, 2, 2, 1, 0, 2, 2, 2, null, 4, 0, 1, 2),
        b(3, "A", 1..8, 2, 1, 1, 0, 0, 0, 1, 2, 2, 3, 0, 0, 1, forbidden = mapOf(8 to setOf("wall-move"))),
        b(3, "B", 9..16, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2),
        b(3, "C", 17..24, 1, 1, 1, 1, 0, 1, 2, 2, 2, 2, 0, 0, 1, forbidden = mapOf(24 to setOf("belt"))),
        b(3, "D", 25..32, 2, 2, 2, 0, 1, 1, 1, 2, 2, 3, 0, 1, 2, forbidden = mapOf(25 to setOf("heat", "power"))),
        b(3, "E", 33..40, 1, 1, 1, 0, 0, 1, 1, 2, 2, 3, 0, 0, 1),
        b(3, "F", 41..48, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 1, 2, 2, forbidden = mapOf(41 to setOf("belt"))),
    )

    /** What [levels] (number → level) of a block use, per [BUDGET_ITEMS] item; `wall` needs [signatures] (level → per round). */
    fun blockUsage(levels: Map<Int, Level>, signatures: Map<Int, List<Signature>>): Map<String, Int> {
        fun count(p: (Int, Level) -> Boolean) = levels.count { (n, l) -> p(n, l) }
        val pads = levels.filter { (_, l) -> CAPS[1].has(l) }.keys
        return mapOf(
            // like H12: the act finale may bring one switch on top
            "pad" to (if (pads.count(::isFinale) <= 1) pads.filterNot(::isFinale) else pads).size,
            "gate" to count { _, l -> hasLaserGate(l) },
            "blink" to count { _, l -> CAPS[3].has(l) },
            "door" to count { _, l -> CAPS[0].has(l) },
            "gravity" to count { _, l -> CAPS[4].has(l) },
            "swap" to count { _, l -> CAPS[5].has(l) },
            "wall" to count { n, _ -> wallDominated(signatures[n].orEmpty()) },
            "spikes" to count { _, l -> l.rounds.any { spikePopupCount(it) > 0 } },
            "heatspike" to count { _, l -> l.rounds.any { heatSpikeFinaleCount(it) > 0 } },
            "rematch" to count { _, l -> l.rematch.isNotEmpty() },
            "bluff" to count { _, l -> levelActions(l).any { it is Action.Bluff } },
            "annex" to count { _, l -> levelActions(l).any { it is Action.Extend } },
        )
    }

    /** Cards played in [levels] (every round, bluffs aside), GRAND_FINALE in a finale left out, as in §7. */
    fun blockCards(levels: Map<Int, Level>): Map<Card, Int> =
        levels.flatMap { (n, l) -> cards(l).filterNot { it == Card.GRAND_FINALE && isFinale(n) } }.groupingBy { it }.eachCount()

    /**
     * §11: a block whose levels are all rebuilt stays within its budget ([Block.budget], `rematch` exactly), plays no card
     * more often than [Block.cardBudget], and its edge levels are not dominated (any round, H20) by a [Block.forbidden] family.
     */
    fun budgetViolations(block: Block, levels: Map<Int, Level>, signatures: Map<Int, List<Signature>>): List<String> {
        val out = ArrayList<String>()
        val used = blockUsage(levels, signatures)
        for ((item, max) in block.budget) {
            val k = used.getValue(item)
            if (item == "rematch") { if (k != max) out += "$block: $k rematch levels, the block keeps exactly $max" }
            else if (k > max) out += "$block: $item in $k levels (budget $max)"
        }
        for ((c, k) in blockCards(levels)) if (k > block.cardBudget(c)) out += "$block: card $c played $k times (budget ${block.cardBudget(c)})"
        for ((n, fs) in block.forbidden) {
            val dominant = signatures[n].orEmpty().flatMap { it.dominant }.toSet()
            val clash = dominant intersect fs
            if (clash.isNotEmpty()) out += "$block: level $n is dominated by $clash, which its neighbour outside the block has"
        }
        return out
    }

    /**
     * §11: rebuilt levels come in whole blocks. A block (its levels outside the [PILOT] in World 2) is either not rebuilt at
     * all or all of it is: leaving one level out of `REBUILT` would quietly switch off its tests and the block's budget.
     */
    fun partialBlocks(world: Int, rebuilt: Set<Int>): List<String> = ROLLOUT.filter { it.world == world }.mapNotNull { block ->
        val own = block.levels - (if (world == 2) PILOT else emptySet())
        val done = own.filter { it in rebuilt }
        if (done.isNotEmpty() && done.size < own.size) "$block: only ${done.sorted()} of ${own.sorted()} are in REBUILT; a block goes in whole" else null
    }

    // ---------- the §8 table, recipe v2 ----------

    /** H12 on the table: per act at most one row with a door that flees (R6/U4), three with a switch (R1/R2/R4), two with swapped controls (U9). */
    fun tableRotationViolations(world: Int, table: Map<Int, Design>): List<String> {
        val caps = listOf(
            Triple("a fleeing door (R6/U4)", 1) { d: Design -> "R6" in d.blocks || "U4" in d.twists },
            Triple("a switch (R1/R2/R4)", 3) { d: Design -> d.blocks.any { it in setOf("R1", "R2", "R4") } },
            Triple("swapped controls (U9)", 2) { d: Design -> "U9" in d.twists },
        )
        return table.keys.filter { ruled(world, it) }.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
            caps.mapNotNull { (what, max, has) ->
                val with = ns.filter { has(table.getValue(it)) }.sorted()
                if (with.size > max) "act $a: ${with.size} rows with $what $with (max $max)" else null
            }
        }
    }

    /**
     * Recipe v2, puzzle in half the levels: per act, of the ruled rows that are neither ★ nor finale, between a third and
     * two thirds are puzzle rooms (with a block R1–R12); the others are trap rooms ("–").
     */
    fun puzzleShareViolations(world: Int, table: Map<Int, Design>): List<String> =
        table.keys.filter { ruled(world, it) && !isFinale(it) && !table.getValue(it).breather }.groupBy(::act).toSortedMap().mapNotNull { (a, ns) ->
            val puzzles = ns.filter { table.getValue(it).blocks.isNotEmpty() }
            if (3 * puzzles.size < ns.size || 3 * puzzles.size > 2 * ns.size) "act $a: ${puzzles.size} of ${ns.size} rows are puzzle rooms (about half)" else null
        }
}
