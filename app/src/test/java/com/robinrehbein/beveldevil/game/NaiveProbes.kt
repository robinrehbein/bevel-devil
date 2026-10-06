package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import kotlin.math.hypot

/**
 * The naive probes, shared by [NaiveProbeReport] (the printed measurement) and the round rules A-C and Q of
 * [DesignRules.roundRules] (the gate), so there is one implementation of each probe. Part of the locked kit
 * ([KitLock]).
 *
 * Probes, each from the round's start until death, door or [TIMEOUT] s:
 * - P1 hold right / hold left, never jump.
 * - P2 hold right / hold left and jump whenever grounded and blocked, at an edge or in front of visible spikes, with a
 *   human reaction time of [REACT] s (it looks that much further ahead, so static edges and spikes are hopped in time,
 *   but a hole or spikes that appear in front of it are seen [REACT] s late).
 * - P3 stand still.
 * - Z1/Z2 like P2 starting right or left, but turning around each time they drop (fall 1.5 tiles below the floor they
 *   left): the naive way through a deck room. Z2 hops edges like P2; Z1 hops only walls and spikes and walks off every
 *   edge. They count for the helpful-trap test and raise ZIGZAG_CLEAR, not NAIVE_CLEAR.
 * - P4 the registered solution, frozen (no input) for [FREEZE] s right after one real trap moment fired; one run per
 *   real moment of the clean run (the freezes are not cumulative).
 * - P5 (rematches) round 1's solution played on the rematch.
 * - P6 the solution against the room with one trap unit's delay shifted by +/-[SHIFT] s (early only if it has a delay).
 * - P7 the solution, but from the trigger of one real moment on, keep holding the key for 2 s without jumping
 *   ([DesignRules.recklessProbe]). threats = real moments of the clean run whose P4 or P7 dies; ONE_THREAT: at most one.
 * - learn: a P1/P2/Z2 runner with perfect memory: after each death the trap unit that killed it is taken out and it
 *   runs again; the fewest deaths until the door over those runners (ONE_DEATH: one death, then through). A static
 *   death or a timeout ends the attempt ("-").
 *
 * Flags: NAIVE_CLEAR (a P1/P2/P3 runner reaches the door), ZIGZAG_CLEAR (a Z runner does), HELPFUL_TRAP (a trap that
 * went off makes the door reachable, or 0.3 s faster, for a naive runner, compared to the room without it),
 * PASSIVE_SAFE (the clean run wins and every P4 freeze still wins: no trap kills the player who stops), OLD_SOLUTION_WORKS
 * (P5 wins), HELPFUL_PROGRESS, LOW_THREAT (under 2 distinct trap killers), ONE_THREAT, ONE_DEATH, SOLUTION_FAILS.
 *
 * Killer attribution: a death is explained by trap ablation. Traps with the same trigger are one unit (as in
 * [DesignRules.decorations]); every unit that went off before the death is taken out ([DesignRules.without]) and the
 * same probe replayed. A unit whose removal changes the outcome is causal. Among the causal units, the killer is the
 * one whose actions drive the lethal element seen at the moment of death (the spike group that hurts, the laser, the
 * saw, the floor that went away for a fall...); if none matches, all causal units count. No causal unit: the death is
 * static level geometry ("static:<what>").
 */
object NaiveProbes {
    const val TIMEOUT = 20f
    const val FREEZE = 2f
    const val SHIFT = 0.3f
    /** Reaction time of the hopping runners (P2, Z2): they act on what they saw this long ago. */
    const val REACT = 0.2f

    /** One round measured: the report [line], its [flags] and the probes behind them. */
    class Measured(
        val key: String, val line: String, val flags: Set<String>, val distinct: Int, val verbose: String?,
        val naive: List<Probe>, val p4: List<Probe>, val p5: Probe?, val p6runs: Int, val p6broke: List<String>,
        val learn: Int?, val trapKillers: List<String>, val cleanWon: Boolean,
    )

    private val cache = java.util.concurrent.ConcurrentHashMap<Pair<Level, Int>, Measured>()

    /** [measure], computed once per round of [level] (the rules of every world and the report share it within one JVM). */
    fun measured(world: Int, n: Int, level: Level, round: Int, solutions: List<Solution>?): Measured =
        cache.getOrPut(level to round) { measure(world, n, level, round, solutions) }

    /** A finished probe run: the world as it ended (null: the run threw), closest the player came to the door. */
    class Res(val w: World?, val minDoor: Float) {
        val state get() = w?.state
        val time get() = w?.time ?: 0f
        val won get() = w?.state == WorldState.WON
        val dead get() = w?.state == WorldState.DEAD
        val outcome: DesignRules.Outcome get() = w?.let {
            DesignRules.Outcome(it.state, Math.round(it.time / Bot.DT), Math.round(it.player.box.cx * 100), Math.round(it.player.box.b * 100))
        } ?: DesignRules.Outcome.THREW
    }

    private fun doorDist(w: World) = hypot(w.player.box.cx - (w.door.box.x + w.door.box.w / 2), w.player.box.cy - (w.door.box.y + w.door.box.h / 2))

    private fun solidAt(w: World, x: Float, y: Float) = w.pieces.any { it.solid && x >= it.box.x && x <= it.box.r && y >= it.box.y && y <= it.box.b }

    /**
     * P1/P2/P3 on [stage]: [dir0] +1 right, -1 left, 0 stand still; [greedy] hops (at edges too if [edges]); [zigzag]
     * turns around every time it falls more than 1.5 tiles below the floor it left (a deck room walked the naive way).
     */
    private fun drive(stage: Level, dir0: Int, greedy: Boolean, zigzag: Boolean = false, edges: Boolean = true): Res = try {
        var dir = dir0
        var groundY = Float.NaN
        var turned = false
        val w = World(stage)
        val c = Controls()
        var minDoor = doorDist(w)
        var jumpT = 0f
        val hist = ArrayDeque<Float>()
        val seenQ = ArrayDeque<Boolean>()
        val reactSteps = Math.round(REACT / Bot.DT)
        var steps = 0
        val maxSteps = (TIMEOUT * 1.5f / Bot.DT).toInt()
        while (w.state == WorldState.PLAYING && w.time < TIMEOUT && steps++ < maxSteps) {
            c.left = dir < 0; c.right = dir > 0; c.jump = false; c.jumpPressed = false
            if (greedy && dir != 0) {
                val p = w.player
                val b = p.box
                // what the runner sees now (an edge or spikes ahead, looking [REACT] s of running further), acted on [REACT] s later
                val lead = abs(p.vx) * REACT
                val seen = p.grounded && run {
                    val footY = if (w.gravity >= 0f) b.b + 0.25f else b.y - 0.25f
                    val edge = !solidAt(w, b.cx + dir * (b.w / 2 + 0.35f + lead), footY)
                    val x0 = if (dir > 0) b.r + lead else b.x - 1.3f - lead
                    (edges && edge) || w.pieces.any { it.spike && it.visible && it.box.r > x0 && it.box.x < x0 + 1.3f && it.box.b > b.y - 0.2f && it.box.y < b.b + 0.3f }
                }
                seenQ.addLast(seen)
                val react = if (seenQ.size > reactSteps) seenQ.removeFirst() else false
                if (jumpT > 0f) { c.jump = true; jumpT -= Bot.DT }
                else if (p.grounded) {
                    val blocked = hist.size >= 6 && abs(hist.last() - hist.first()) < 0.02f
                    if (blocked || react) { c.jump = true; c.jumpPressed = true; jumpT = 0.35f - Bot.DT; hist.clear(); seenQ.clear() }
                }
            }
            w.step(Bot.DT, c)
            // a drop: turn around as soon as it falls 1.5 tiles below the floor it left (once per drop)
            if (zigzag && !w.player.grounded && !turned && groundY.isFinite() && (w.player.box.b - groundY) * (if (w.gravity >= 0f) 1f else -1f) > 1.5f) {
                dir = -dir; hist.clear(); turned = true
            }
            if (w.player.grounded) { groundY = w.player.box.b; turned = false }
            hist.addLast(w.player.box.cx)
            if (hist.size > 6) hist.removeFirst()
            minDoor = minOf(minDoor, doorDist(w))
        }
        Res(w, minDoor)
    } catch (_: Throwable) { Res(null, Float.NaN) }

    private fun bot(run: () -> Bot): Res = try {
        val b = run()
        Res(b.world, Float.NaN)
    } catch (_: Throwable) { Res(null, Float.NaN) }

    /** What the player touched at the moment of death, in the order the engine checks hazards. */
    private fun label(w: World): String {
        val b = w.player.box
        w.pieces.firstOrNull { it.spike && it.visible && it.group?.circuit == null && it.hurts(b) }?.let { return it.group?.let { g -> "spikes:${id(g.id)}" } ?: "spike" }
        val i = Hardware.TRACE_INSET
        w.circuits.values.firstOrNull { c -> c.trace && c.powered && c.group.pieces.any { b.overlaps(it.box.x + i, it.box.y + i, 1f - 2 * i, 1f - 2 * i) } }?.let { return "trace:${id(it.id)}" }
        w.heaters.values.firstOrNull { h -> !h.melted && h.heat >= 1f && h.group.pieces.any { b.overlaps(it.box.x - 0.05f, it.box.y - 0.05f, 1.1f, 1.1f) } }?.let { return "heat:${id(it.group.id)}" }
        w.ghost?.let { g -> if (b.overlaps(g)) return "ghost" }
        w.beams.firstOrNull { it.hits(b) }?.let { return "laser:${id(it.laser.id)}" }
        sawOf(w)?.let { return "saw" }
        if (b.y > w.rows + 1 || b.b < -1) return "pit"
        w.pieces.firstOrNull { it.solid && b.overlaps(it.box) }?.let { return "crush:${it.group?.id?.let(::id) ?: "#"}" }
        return "?"
    }

    private fun id(c: Char) = if (c.code in 32..126) c.toString() else "u%04x".format(c.code)

    private fun acts(unit: List<Trap>) = unit.flatMap { t -> t.actions.flatMap { it.flat() } }

    /** The saw that killed the player in [w], if any. */
    private fun sawOf(w: World): Saw? {
        val b = w.player.box
        return w.saws.firstOrNull { s -> hypot(s.x - s.x.coerceIn(b.x, b.r), s.y - s.y.coerceIn(b.y, b.b)) < s.r * 0.9f }
    }

    /** The unit's actions drive the lethal element [label] ([saw]: the saw that hit). */
    private fun drives(unit: List<Trap>, label: String, saw: Saw?): Boolean {
        val a = acts(unit)
        val kind = label.substringBefore(':')
        val id = label.substringAfter(':', "")
        val ref = Regex("""(group|id)=${Regex.escape(id)}[,)]""")
        return when (kind) {
            "spikes", "trace", "heat", "laser" -> a.any { ref.containsMatchIn(it.toString()) }
            "crush" -> a.any { ref.containsMatchIn(it.toString()) || it is Action.FrameCrack || it is Action.Move || it is Action.Chase }
            "saw" -> a.any { x ->
                if (saw == null) x is Action.Saw || x is Action.PathSaw
                else (saw.path != null && x === saw.path) || (x is Action.Saw && x.vx == saw.vx && x.vy == saw.vy && x.r == saw.r)
            }
            "ghost" -> a.any { it is Action.Ghost }
            "pit" -> a.any {
                it is Action.Fall || it is Action.Hide || it is Action.Move || it is Action.Blink || it is Action.Gravity || it is Action.Tilt ||
                    it is Action.Slope || it is Action.Belt || it is Action.FrameCrack || it is Action.Clock || it is Action.Circuit
            }
            else -> false
        }
    }

    fun units(stage: Level): List<List<Trap>> = stage.traps.groupBy { it.trigger }.values.toList()

    fun unitKey(stage: Level, unit: List<Trap>): String {
        val trig = unit.first().trigger.toString().replace(Regex("""\(.*"""), "")
        return "T${stage.traps.indexOf(unit.first()) + 1}:$trig"
    }

    private fun sprungUnits(stage: Level, r: Res, until: Float): List<List<Trap>> {
        val w = r.w ?: return emptyList()
        val fired = w.sprung.filter { it.time <= until + 1e-4f }.map { it.trap }.toSet()
        return units(stage).filter { u -> u.any { it in fired } }
    }

    /** One probe's run with its ablations: unit → run without it. */
    class Probe(val name: String, val stage: Level, val run: (Level) -> Res, val ablate: Boolean) {
        val base = run(stage)
        val label = base.w?.takeIf { base.dead }?.let(::label)
        val sprung = sprungUnits(stage, base, base.time)
        val without: Map<List<Trap>, Res> = if (ablate || base.dead) sprung.associateWith { u -> run(DesignRules.without(stage, u.toSet())) } else emptyMap()
        /** Units whose removal changes how the death plays out; a cut that throws (a later trap needs it) cannot be judged and is left out. */
        val causal = if (!base.dead) emptyList() else sprung.filter { u -> without.getValue(u).let { it.w != null && it.outcome != base.outcome } }
        /** Ablations that threw, for the report. */
        val threw get() = without.values.count { it.w == null }
        /** The trap units that killed (empty: none, or static). */
        val killerUnits: List<List<Trap>> = if (!base.dead) emptyList() else causal.filter { drives(it, label ?: "?", base.w?.let(::sawOf)) }.ifEmpty { causal }
        val killers: List<String> = when {
            !base.dead -> emptyList()
            causal.isEmpty() -> listOf("static:${label?.substringBefore(':')}")
            else -> killerUnits.map { unitKey(stage, it) }
        }
        val helpfulDoor = if (!ablate) emptyList() else sprung.filter { u ->
            val o = without.getValue(u)
            base.won && (!o.won || o.time > base.time + 0.3f)
        }
        val helpfulProgress = if (!ablate) emptyList() else sprung.filter { u ->
            val o = without.getValue(u)
            !base.won && !o.won && base.minDoor.isFinite() && o.minDoor.isFinite() && base.minDoor < o.minDoor - 2f
        }

        fun short(): String = when {
            base.w == null -> "threw"
            base.won -> "door@%.1f".format(base.time)
            base.dead -> "died@%.1f[%s<-%s]".format(base.time, label, killers.joinToString("+"))
            else -> "timeout"
        }
    }

    private fun shifted(stage: Level, unit: List<Trap>, by: Float): Level {
        val grid = stage.map.grid
        val traps = stage.traps.map { t -> if (t in unit) Trap(t.trigger, t.actions, maxOf(0f, t.delay + by)) else t }
        return Level(stage.name, stage.intro, stage.legend, traps, stage.start, hint = stage.hint, rooms = stage.rooms) {
            for (y in grid.indices) for (x in grid[y].indices) this.grid[y][x] = grid[y][x]
        }
    }

    fun measure(world: Int, n: Int, level: Level, round: Int, solutions: List<Solution>?, verbose: Boolean = false): Measured {
        val stage = level.rounds[round]
        val key = "$world-$n-${round + 1}"
        val sol = solutions?.getOrNull(round)
        val naive = listOf(
            Probe("P1R", stage, { drive(it, 1, false) }, true),
            Probe("P1L", stage, { drive(it, -1, false) }, true),
            Probe("P2R", stage, { drive(it, 1, true) }, true),
            Probe("P2L", stage, { drive(it, -1, true) }, true),
            Probe("P3", stage, { drive(it, 0, false) }, true),
            Probe("Z1R", stage, { drive(it, 1, true, zigzag = true, edges = false) }, true),
            Probe("Z1L", stage, { drive(it, -1, true, zigzag = true, edges = false) }, true),
            Probe("Z2R", stage, { drive(it, 1, true, zigzag = true) }, true),
            Probe("Z2L", stage, { drive(it, -1, true, zigzag = true) }, true),
        )
        // the clean run and P4: one freeze per real moment of it
        val clean = sol?.let { s -> try { Bot(stage, 0).apply(s) } catch (_: Throwable) { null } }
        val cleanWon = clean?.world?.state == WorldState.WON
        val realMoments = if (cleanWon) clean!!.moments.filter { it.real }.distinctBy { it.time } else emptyList()
        val moments = realMoments.map { it.time }
        val p4 = if (sol == null) emptyList() else moments.map { at ->
            Probe("P4@%.1f".format(at), stage, { l -> bot { Bot(l, 0, probe = Bot.Probe(Bot.Probe.Kind.WAIT, FREEZE) { it.world.time >= at - 1e-4f }).apply(sol) } }, false)
        }
        val p5 = if (round > 0) solutions?.getOrNull(0)?.let { s0 -> Probe("P5", stage, { l -> bot { Bot(l, 0).apply(s0) } }, false) } else null
        // P6: the solution against one unit shifted early / late
        var p6runs = 0
        val p6broke = ArrayList<String>()
        if (cleanWon) {
            val real = clean!!.moments.filter { it.real }.flatMap { it.traps }.toSet()
            for (u in units(stage).filter { it.any { t -> t in real } && it.first().trigger != Trigger.AtDoor }) {
                for (by in listOf(-SHIFT, SHIFT)) {
                    if (by < 0f && u.none { it.delay > 0f }) continue
                    p6runs++
                    val r = bot { Bot(shifted(stage, u, by), 0).apply(sol!!) }
                    if (!r.won) p6broke += "${unitKey(stage, u)}${if (by < 0) "-" else "+"}"
                }
            }
        }
        // the learning naive player: dies, then never falls for the trap that killed it again (that unit is taken out),
        // and goes again; how many deaths until the door (null: a static death or a timeout stops it)
        fun learn(run: (Level) -> Res): Int? {
            val removed = LinkedHashSet<Trap>()
            for (k in 0..5) {
                val p = Probe("learn", if (removed.isEmpty()) stage else DesignRules.without(stage, removed), run, false)
                if (p.base.won) return k
                if (!p.base.dead || p.killerUnits.isEmpty()) return null
                removed += p.killerUnits.flatten()
            }
            return null
        }
        val learners = listOf("P1R" to 0, "P1L" to 1, "P2R" to 2, "P2L" to 3, "Z2R" to 7, "Z2L" to 8).map { (nm, i) ->
            nm to (naive[i].base.won.takeIf { it }?.let { 0 } ?: if (naive[i].killerUnits.isEmpty()) null else learn(naive[i].run))
        }
        val bestLearn = learners.filter { it.second != null }.minByOrNull { it.second!! }
        // P7: from each real moment's trigger, keep holding the key (no jump) for 2 s: the player who does not dodge
        val p7 = if (sol == null) emptyList() else realMoments.map { m ->
            Probe("P7@%.1f".format(m.triggered), stage, { l -> bot { DesignRules.recklessProbe(l, 0, sol, m.triggered) } }, false)
        }
        // moments of the clean run where a trap kills a player who freezes (P4) or runs on (P7)
        val threats = realMoments.indices.count { i -> p4[i].killerUnits.isNotEmpty() || p7[i].killerUnits.isNotEmpty() }
        val all = naive + p4 + listOfNotNull(p5)
        val killerSet = all.flatMap { it.killers }.toSet()
        val trapKillers = killerSet.filterNot { it.startsWith("static:") }
        val statics = killerSet.filter { it.startsWith("static:") }
        val real = all.flatMap { p -> p.causal.map { unitKey(stage, it) } }.toSet()
        val helpfulDoor = naive.flatMap { p -> p.helpfulDoor.map { "${p.name}:${unitKey(stage, it)}" } }
        val helpfulProgress = naive.flatMap { p -> p.helpfulProgress.map { "${p.name}:${unitKey(stage, it)}" } }
        val flags = LinkedHashSet<String>()
        if (naive.take(5).any { it.base.won }) flags += "NAIVE_CLEAR"
        if (naive.drop(5).any { it.base.won }) flags += "ZIGZAG_CLEAR"
        if (cleanWon && p4.all { it.base.won }) flags += "PASSIVE_SAFE"
        if (p5?.base?.won == true) flags += "OLD_SOLUTION_WORKS"
        if (helpfulDoor.isNotEmpty()) flags += "HELPFUL_TRAP"
        if (helpfulProgress.isNotEmpty()) flags += "HELPFUL_PROGRESS"
        if (trapKillers.size < 2) flags += "LOW_THREAT"
        if (realMoments.isNotEmpty() && threats <= 1) flags += "ONE_THREAT"
        if (bestLearn != null && bestLearn.second!! in 1..1) flags += "ONE_DEATH"
        if (sol != null && !cleanWon) flags += "SOLUTION_FAILS"
        val p4s = if (sol == null) "n/a" else if (!cleanWon) "clean!" else "${p4.count { it.base.won }}/${p4.size}won"
        val p4deaths = p4.filter { it.base.dead }.joinToString(" ") { "${it.name}${it.short().removePrefix("died")}" }
        val line = buildString {
            append("%-8s %-24s".format(key, stage.name.en.take(24)))
            append(" | ").append(naive.joinToString(" ") { "${it.name}=${it.short()}" })
            append(" | P4 ").append(p4s).append(if (p4deaths.isEmpty()) "" else " ($p4deaths)")
            append(" | P5 ").append(p5?.short() ?: "n/a")
            append(" | P6 ").append(if (p6runs == 0) "n/a" else "${p6broke.size}/${p6runs}broke" + if (p6broke.isEmpty()) "" else " ${p6broke.joinToString(",")}")
            append(" | distinctKillers=${trapKillers.size} {${trapKillers.joinToString(",")}} static={${statics.joinToString(",") { it.removePrefix("static:") }}} realTraps=${real.size}/${units(stage).size}")
            append(" threats=${if (realMoments.isEmpty()) "-" else "$threats/${realMoments.size}"}")
            append(" learn=${bestLearn?.let { "${it.second}(${it.first})" } ?: "-"}")
            (all + p7).sumOf { it.threw }.let { if (it > 0) append(" ablationsThrew=$it") }
            if (helpfulDoor.isNotEmpty()) append(" helpful=${helpfulDoor.joinToString(",")}")
            if (helpfulProgress.isNotEmpty()) append(" helpfulProgress=${helpfulProgress.joinToString(",")}")
            append(" | ").append(flags.joinToString(" ").ifEmpty { "-" })
        }
        val verboseText = if (!verbose) null else buildString {
            append("== $key ${stage.name.en}: ${units(stage).size} trap units; clean run ")
            append(if (clean == null) "n/a" else "%s@%.2f".format(clean.world.state, clean.world.time)).append('\n')
            units(stage).forEach { u -> append("   ${unitKey(stage, u)} delay=${u.map { it.delay }} ${acts(u).filterNot { it is Action.Say || it is Action.Play }.joinToString("+")}\n") }
            for (p in all + p7) {
                append("  ${p.name}: ${p.short()} minDoor=%.1f".format(p.base.minDoor))
                append(" sprung=${p.sprung.map { unitKey(stage, it) }} causal=${p.causal.map { unitKey(stage, it) }}")
                if (p.helpfulDoor.isNotEmpty() || p.helpfulProgress.isNotEmpty()) {
                    append(" helpful=${(p.helpfulDoor + p.helpfulProgress).map { u -> "${unitKey(stage, u)}(without: ${p.without.getValue(u).let { o -> "${o.state}@%.1f minDoor=%.1f".format(o.time, o.minDoor) }})" }}")
                }
                append('\n')
            }
            if (p6broke.isNotEmpty()) append("  P6 broke: $p6broke\n")
        }
        return Measured(key, line, flags, trapKillers.size, verboseText, naive, p4, p5, p6runs, p6broke, bestLearn?.second, trapKillers, cleanWon)
    }
}
