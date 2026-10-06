package com.robinrehbein.beveldevil.game

/**
 * The rounds that still fail a round rule of docs/LEVEL_DESIGN_V2.md §9a (A–K, [DesignRules.roundRules]) as of the
 * commit that introduced the rules: one line per round and rule, `"<world>-<level>-<round> <rule>"`.
 *
 * **Shrink-only.** `roundRulesHoldOrArePending` (in every `World{n}DesignTest`) checks both ways: a failing rule of a
 * round that is not listed here is red, and a listed line whose rule that round passes now is red too (stale). Fix a
 * round, then delete its line; never add, edit or reorder lines (a later diff of this file contains removed lines
 * only). The file is deliberately not in the [KitLock] hash set, so that deleting lines needs no new hash.
 */
object PendingRounds {
    val ENTRIES: List<String> = listOf(
        // World 1
        // World 2
        // World 3
    )
}
