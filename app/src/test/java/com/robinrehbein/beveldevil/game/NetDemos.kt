package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** One tiny level per network mechanic of World 2, proving each with the Bot (not part of the game's levels). */
object NetDemos {
    private fun MapBuilder.ends() { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    /** A wall to the ceiling; the portal hops through it. */
    val portal = Level(T("Hop", "Hop"), T("Next hop.", "Nächster Hop."), start = listOf(Portal('1', 8 to 14, 17 to 14))) {
        ends()
        fill(12..12, 1..14)
    }

    /** Past x 6 the portal's exit moves onto a spike ledge; hitting the switch overhead routes it back. */
    val dns = Level(
        T("DNS", "DNS"), T("Trust the route.", "Vertrau der Route."),
        start = listOf(Portal('1', 8 to 14, 17 to 14)),
        traps = listOf(
            trap(PastX(6f), Reroute('1', 5 to 3), Say(T("DNS changed.", "DNS geändert."))),
            trap(Touch('s'), Reroute('1', 17 to 14), Say(T("Cache flushed. Fine.", "Cache geleert. Na gut."))),
        ),
    ) {
        ends()
        fill(12..12, 1..14)
        fill(3..8, 5..5); fill(3..8, 4..4, '^')
        fill(2..3, 12..12, 's')
    }

    /** A belt carries you right into spikes that sit on it; jump them. */
    val belt = Level(T("Data bus", "Datenbus"), T("Ride the bus.", "Fahr mit dem Bus."), start = listOf(Belt('b', 3f))) {
        ends()
        fill(6..26, 15..15, 'b')
        put(23, 14, '^'); put(24, 14, '^')
    }

    /** Past x 10 the belt reverses faster than you run, back toward the spikes on the wall; hop along it. */
    val reorder = Level(
        T("Reordering", "Umsortiert"), T("Packets arrive in order. Usually.", "Pakete kommen in Reihenfolge an. Meistens."),
        start = listOf(Belt('b', 2f)),
        traps = listOf(trap(PastX(10f), Belt('b', -10f), Say(T("Packet reordering!", "Paket-Umsortierung!")))),
    ) {
        border(); floor(); put(5, 14, 'P'); put(29, 14, 'D')
        fill(1..25, 15..15, 'b')
        put(1, 13, '>'); put(1, 14, '>')
    }

    /** A beam from ceiling to floor: 1 s on, 1.4 s off. Timed so a straight run meets it lit. */
    val laser = Level(
        T("Firewall", "Firewall"), T("Wait for the gap.", "Warte auf die Lücke."),
        start = listOf(Laser('L', 15 to 1, 15 to 14, on = 1f, off = 1.4f, phase = 1.4f)),
    ) {
        ends()
    }

    /** A permanent beam blocks the way until maintenance switches it off; it comes back on behind you. */
    val firewall = Level(
        T("Maintenance", "Wartung"), T("Access denied.", "Zugriff verweigert."),
        start = listOf(Laser('L', 16 to 1, 16 to 14), Laser('H', 20 to 12, 27 to 12, on = 0.8f, off = 0.8f)),
        traps = listOf(
            trap(PastX(10f), Power('L', false), Say(T("Maintenance window.", "Wartungsfenster."))),
            trap(PastX(19f), Power('L', true), Say(T("Back online.", "Wieder online."))),
        ),
    ) {
        ends()
    }

    val all = listOf(portal, dns, belt, reorder, laser, firewall)
}
