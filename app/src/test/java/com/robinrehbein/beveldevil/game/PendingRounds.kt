package com.robinrehbein.beveldevil.game

/**
 * The rounds that still fail a round rule of docs/LEVEL_DESIGN_V2.md §9a (A–Q, [DesignRules.roundRules]): one line per
 * round and rule, `"<world>-<level>-<round> <rule>"`. A–K as of the commit that introduced them (all fixed since), L–Q
 * as of the commit that introduced those (the three reviews after A–K).
 *
 * **Shrink-only.** `roundRulesHoldOrArePending` (in every `World{n}DesignTest`) checks both ways: a failing rule of a
 * round that is not listed here is red, and a listed line whose rule that round passes now is red too (stale). Fix a
 * round, then delete its line; never add, edit or reorder lines (a later diff of this file contains removed lines
 * only). Lines are added only together with new rules, by the orchestrator, in the commit that introduces them (L–Q
 * did so once). The file is deliberately not in the [KitLock] hash set, so that deleting lines needs no new hash.
 */
object PendingRounds {
    val ENTRIES: List<String> = listOf(
        // World 1
        // World 2
        "2-24-1 L", "2-24-1 Q",
        "2-25-1 M",
        "2-27-1 M",
        "2-28-1 M", "2-28-1 O",
        "2-31-1 M",
        "2-39-1 M",
        // World 3
    )
}
