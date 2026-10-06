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
        "1-7-1 O",
        "1-8-1 O",
        "1-10-1 O",
        "1-11-1 O",
        "1-12-1 M", "1-12-2 M",
        "1-14-1 Q",
        "1-15-1 L", "1-15-2 L",
        "1-16-1 L",
        "1-17-1 O", "1-17-1 Q", "1-17-2 O",
        "1-18-1 L", "1-18-1 O", "1-18-2 O",
        "1-19-1 L", "1-19-1 O",
        "1-20-1 M", "1-20-1 O",
        "1-21-1 L", "1-21-1 O", "1-21-2 O",
        "1-22-1 L",
        "1-23-1 N",
        "1-24-1 L", "1-24-2 L",
        "1-25-1 Q",
        "1-27-1 L",
        "1-29-1 M",
        "1-30-1 Q",
        "1-31-1 L",
        "1-32-1 L",
        "1-33-1 L", "1-33-2 L",
        "1-34-1 O", "1-34-1 Q",
        "1-36-1 Q",
        "1-37-1 L", "1-37-1 O", "1-37-2 O",
        "1-38-1 O",
        "1-40-1 Q",
        "1-43-1 L", "1-43-1 Q",
        "1-44-1 N",
        "1-45-1 L", "1-45-1 Q",
        "1-46-1 L", "1-46-1 M",
        "1-47-1 L", "1-47-1 M", "1-47-2 L", "1-47-2 M",
        "1-48-1 L", "1-48-1 P", "1-48-2 L",
        // World 2
        "2-2-1 O",
        "2-3-1 O",
        "2-4-1 O", "2-4-2 M", "2-4-2 O",
        "2-9-1 L", "2-9-1 O",
        "2-12-1 L", "2-12-1 O",
        "2-13-1 O",
        "2-15-1 L",
        "2-17-1 Q",
        "2-18-1 L", "2-18-1 O", "2-18-2 L", "2-18-2 O",
        "2-19-1 L", "2-19-1 O",
        "2-20-1 L", "2-20-1 M", "2-20-2 L", "2-20-2 Q",
        "2-24-1 L", "2-24-1 Q",
        "2-25-1 M",
        "2-26-1 L", "2-26-2 L",
        "2-27-1 M",
        "2-28-1 M", "2-28-1 O",
        "2-29-1 P",
        "2-30-1 L", "2-30-1 P", "2-30-1 Q",
        "2-31-1 M",
        "2-32-1 P",
        "2-33-1 O", "2-33-2 O",
        "2-34-1 Q", "2-34-2 Q",
        "2-35-1 O",
        "2-39-1 M", "2-39-1 O",
        "2-41-1 O", "2-41-2 O",
        "2-42-1 L", "2-42-1 Q", "2-42-2 L",
        "2-44-1 L", "2-44-2 L",
        "2-45-1 M",
        "2-46-1 P", "2-46-2 O",
        "2-47-1 P", "2-47-1 Q",
        "2-48-1 P", "2-48-1 Q",
        // World 3
        "3-33-1 Q",
        "3-36-1 M",
        "3-37-1 M",
        "3-38-1 Q",
        "3-39-1 P", "3-39-1 Q",
        "3-40-1 Q",
        "3-41-1 P",
        "3-43-1 L", "3-43-1 P",
        "3-44-1 P",
        "3-45-1 M", "3-45-1 P",
        "3-46-1 P", "3-46-1 Q",
        "3-47-1 P",
        "3-48-1 P",
    )
}
