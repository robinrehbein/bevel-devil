package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Trigger.AtDoor

/** "Who says the room ends here?" (U18): levels of two and three rooms, test-only (not part of the game's levels). */
object RoomDemos {
    /**
     * Two rooms. The door at the right edge of room 1 is not the end: the wall behind it breaks open, the door slips
     * into room 2, and a pit there has to be jumped.
     */
    val annex = Level(
        T("Annex", "Anbau"), T("Almost done. Surely.", "Fast geschafft. Sicher."),
        rooms = 2,
        traps = listOf(trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14))),
    ) {
        border(); floor()
        room(0) { spawn(); door(); fill(14..15, 13..14) }
        room(1) { pit(12..15); fill(22..23, 13..14) }
    }

    /**
     * Three rooms: the door flees twice. Room 2 has a pit and a saw bobbing over the path (global coordinates), room 3
     * a belt carrying toward spikes that sit on it.
     */
    val sprawl = Level(
        T("Sprawl", "Zersiedelt"), T("A small flat. Cosy.", "Kleine Wohnung. Gemütlich."),
        rooms = 3,
        start = listOf(PathSaw(5f, roomX(1, 20f) to 14.4f, roomX(1, 20f) to 9f, delay = 1f), Belt('b', 3f)),
        traps = listOf(
            trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 29) to 14)),
            trap(AtDoor, Extend(into = 2, door = roomX(2, 29) to 14, line = T("And another wing!", "Und noch ein Flügel!"))),
        ),
    ) {
        border(); floor()
        room(0) { spawn(); door() }
        room(1) { pit(8..10) }
        room(2) { fill(4..24, 15..15, 'b'); put(16, 14, '^') }
    }

    val all = listOf(annex, sprawl)
}
