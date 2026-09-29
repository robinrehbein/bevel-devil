package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 1, levels 65-80: moving parts, undo buttons and misdirection. */
object World1Part5 {

    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)

    /** A wall with a 1-tile tunnel below it that closes and opens again. */
    private fun chomp(g: Char, phase: Float): List<Trap> = (0..3).flatMap { k ->
        listOf(
            trap(After(phase + 2.6f * k), Move(g, 0f, 1f, 6f)),
            trap(After(phase + 2.6f * k + 1.0f), Move(g, 0f, -1f, 6f)),
        )
    }

    /** A platform that shuttles right and left forever (well, four times). */
    private fun shuttle(g: Char, dx: Float, start: Float, speed: Float): List<Trap> {
        val leg = dx / speed
        return (0..3).flatMap { k ->
            listOf(
                trap(After(start + 2 * leg * k), Move(g, dx, 0f, speed)),
                trap(After(start + 2 * leg * k + leg), Move(g, -dx, 0f, speed)),
            )
        }
    }

    val levels: List<Level> = listOf(
        // 65 — the ferry takes you to the middle; the second boat sinks as soon as you board
        Level(
            name = T("Ferry Rush", "Fähre in Eile"),
            intro = T("Two boats, one crossing. Have a nice trip.", "Zwei Boote, eine Überfahrt. Gute Reise."),
            traps = listOf(
                trap(After(2.4f), Move('f', 10f, 0f, 4f), Say(T("All aboard!", "Alles einsteigen!"))),
                trap(Touch('g'), Play(Card.SINKING), Move('g', 0f, 14f, 8f), Say(T("Boat two is... a submarine.", "Boot zwei ist... ein U-Boot.")), delay = 0.4f),
            ),
        ) {
            border(); floor(); pit(1..27)
            fill(1..5, 15..15, 'f'); fill(19..23, 15..15, 'g')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 66 — the floor is deleted, then restored; the trick is simply waiting for the undo
        // EASTER EGG: Ctrl+Z / Ctrl+Y
        Level(
            name = T("Ctrl+Z", "Strg+Z"),
            intro = T("Oops. I deleted something. Hold on.", "Ups. Ich habe was gelöscht. Moment."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(9.5f), Play(Card.COLLAPSE), Hide('a'), Say(T("Oops. Floor deleted.", "Ups. Boden gelöscht."))),
                trap(PastX(9.5f), Show('a'), Say(T("Ctrl+Z! ...and Ctrl+Y for the spikes.", "Strg+Z! ...und Strg+Y für die Spikes.")), delay = 3.2f),
                trap(PastX(19f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(12..19, 15..17, 'a')
            put(23, 14, 'A'); put(24, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 67 — a staircase down: the middle ledge crumbles, hidden spikes wait on the lower one
        Level(
            name = T("Descent", "Abstieg"),
            intro = T("Downhill is easy. That's what they all say.", "Bergab ist leicht. Das sagen alle."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('b'), Play(Card.CRUMBLE), Fall('b'), Say(T("Downhill... faster.", "Bergab... schneller.")), delay = 0.75f),
                trap(PastX(17f), Show('A'), Say(T("Mind the landing.", "Achte auf die Landung."))),
            ),
        ) {
            border()
            fill(23..30, 15..17)
            fill(1..7, 5..5); fill(9..15, 8..8, 'b'); fill(17..22, 11..11)
            put(20, 10, 'A'); put(21, 10, 'A')
            put(3, 4, 'P'); put(29, 14, 'D')
        },

        // 68 — a confident, wrong door on the right. The real one is up on the left.
        // NOD: AI hallucination (confidently wrong)
        Level(
            name = T("Hallucination", "Halluzination"),
            intro = T("Certainly! The door is on the right. (It is not.)", "Sicher! Die Tür ist rechts. (Ist sie nicht.)"),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(22f), Play(Card.DECOY), Show('A'), Say(T("As an AI, I assure you: it was a door.", "Als KI versichere ich dir: Das war eine Tür."))),
            ),
        ) {
            border(); floor()
            fill(9..11, 13..13); fill(6..8, 11..11); fill(1..4, 9..9)
            // a very convincing door-shaped wall
            fill(26..26, 11..14); fill(28..28, 11..14); fill(26..28, 10..10)
            put(23, 14, 'A'); put(24, 14, 'A')
            put(14, 14, 'P'); put(2, 8, 'D')
        },

        // 69 — the lift swaps your controls; spikes wait on the ledge
        Level(
            name = T("Twisted Lift", "Verdrehter Aufzug"),
            intro = T("Up is up. Left is... something else.", "Oben ist oben. Links ist... etwas anderes."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('a'), Play(Card.TWISTED), Swap(true), Move('a', 0f, -8f, 4.5f), Say(T("Going up. Left is right.", "Es geht hoch. Links ist rechts."))),
                trap(Zone(11f, 4.5f, 16f, 6.6f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(11..15, 14..14, 'a')
            fill(17..30, 6..6)
            put(17, 5, 'A'); put(18, 5, 'A')
            put(2, 14, 'P'); put(29, 5, 'D')
        },

        // 70 — the landing platform leaves as you approach and comes back later
        Level(
            name = T("Moving Goalposts", "Bewegliche Torpfosten"),
            intro = T("Jump when ready. The far side will be ready too. Eventually.", "Spring, wenn du bereit bist. Die andere Seite ist es auch. Irgendwann."),
            traps = listOf(
                trap(PastX(9f), Play(Card.SINKING), Move('g', 4f, 0f, 3f), Say(T("Sorry, the far side is busy.", "Tut mir leid, die andere Seite ist belegt."))),
                trap(PastX(9f), Move('g', -4f, 0f, 3f), Say(T("Back in a moment!", "Gleich wieder da!")), delay = 3f),
            ),
        ) {
            border(); floor(); pit(12..19)
            fill(15..19, 15..17, 'g')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 71 — a staircase with a spike on every tread; hop, hop, hop, and the fourth landing crumbles
        Level(
            name = T("Spike Steps", "Stachelstufen"),
            intro = T("Mind the steps. All of them.", "Achte auf die Stufen. Auf alle."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(14f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Step three has a surprise.", "Stufe drei hat eine Überraschung."))),
                trap(Touch('d'), Fall('d'), Say(T("Step five is optional. Sort of.", "Stufe fünf ist optional. Irgendwie.")), delay = 0.45f),
            ),
        ) {
            border(); floor(); pit(22..25)
            fill(6..9, 14..14); fill(10..13, 13..14); fill(14..17, 12..14); fill(18..21, 11..14)
            fill(22..25, 10..10, 'd'); fill(26..30, 9..14)
            put(9, 13, '^'); put(13, 12, '^'); put(17, 11, 'A'); put(21, 10, '^'); put(25, 9, '^')
            put(2, 14, 'P'); put(29, 8, 'D')
        },

        // 72 — parkour over the rooftops; one roof crumbles, one has a hidden spike
        Level(
            name = T("Rooftops", "Über den Dächern"),
            intro = T("Nice view up here. Don't look down.", "Schöne Aussicht hier oben. Schau nicht runter."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('c'), Play(Card.CRUMBLE), Fall('c'), Say(T("That roof was not up to code.", "Dieses Dach war nicht genehmigt.")), delay = 0.3f),
                trap(PastX(20.5f), Show('A'), Say(T("Chimney sweep!", "Schornsteinfeger!"))),
            ),
        ) {
            border()
            fill(1..5, 13..13); fill(8..11, 12..12); fill(13..17, 14..14, 'c'); fill(19..22, 13..13)
            fill(24..27, 11..11); fill(29..30, 12..12)
            put(26, 10, 'A')
            put(3, 12, 'P'); put(30, 11, 'D')
        },

        // 73 — two crushing walls with a one-tile tunnel below; the tunnel opens and closes
        Level(
            name = T("Chomp", "Mampf"),
            intro = T("There is a tunnel. It is just very hungry.", "Da ist ein Tunnel. Er hat nur sehr viel Hunger."),
            traps = listOf(trap(After(1.5f), Play(Card.HEADBUTT), Say(T("Chomp chomp.", "Mampf mampf.")))) +
                chomp('w', 1.5f) + chomp('x', 3.9f),
        ) {
            border(); floor()
            fill(15..17, 8..13, 'w'); fill(22..24, 8..13, 'x')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 74 — the ground ahead collapses just as you reach the ledge; there was only ever one road
        Level(
            name = T("Pick a Road", "Such dir einen Weg aus"),
            intro = T("Ground or ledge? Take your time to choose.", "Boden oder Sims? Lass dir Zeit mit der Entscheidung."),
            traps = listOf(
                trap(PastX(9.5f), Play(Card.COLLAPSE), Fall('f'), Say(T("Too late. Roads closed.", "Zu spät. Straße gesperrt."))),
            ),
        ) {
            border(); floor()
            fill(13..24, 15..17, 'f')
            fill(11..30, 13..13)
            put(16, 12, '^'); put(17, 12, '^'); put(23, 12, '^')
            put(2, 14, 'P'); put(29, 12, 'D')
        },

        // 75 — the scariest level is completely honest
        // EASTER EGG: Rickroll
        Level(
            name = T("Never Gonna Give You Up", "Never Gonna Give You Up"),
            intro = T("I promise nothing happens here. Never gonna lie to you.", "Ich verspreche, hier passiert nichts. Never gonna lie to you."),
            traps = listOf(
                trap(PastX(6f), Shake(0.4f), Say(T("Never gonna give you up", "Never gonna give you up"))),
                trap(PastX(13f), Shake(0.4f), Say(T("Never gonna let you down", "Never gonna let you down"))),
                trap(PastX(21f), Shake(0.4f), Say(T("Never gonna run around and desert you", "Never gonna run around and desert you"))),
                trap(Zone(26f, 10f, 31f, 16f), Play(Card.DECOY), Say(T("Never gonna make you cry. (You were rickrolled.)", "Never gonna make you cry. (Du wurdest gerickrollt.)"))),
            ),
        ) {
            border(); floor(); pit(9..10); pit(20..21)
            put(15, 14, '^'); put(16, 14, '^')
            fill(25..27, 4..5)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 76 — a platform shuttles back and forth; board it on its way home and jump off at the other end
        Level(
            name = T("Cloud Hopping", "Wolkenhüpfen"),
            intro = T("The bus leaves at 0:00. Pity. Take the next one.", "Der Bus fährt um 0:00. Schade. Nimm den nächsten."),
            traps = listOf(trap(After(0.3f), Play(Card.SINKING), Say(T("Next stop: over there.", "Nächster Halt: da drüben.")))) +
                shuttle('h', 10f, 0.3f, 4f),
        ) {
            border(); floor(); pit(6..23)
            fill(9..11, 15..15, 'h')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 77 — the ferry ride with swapped controls
        Level(
            name = T("Twisted Ferry", "Verdrehte Fähre"),
            intro = T("A quiet boat ride. Nothing can go wrong. Wrong. Go.", "Eine ruhige Bootsfahrt. Nichts kann schiefgehen. Gehen. Schief."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(2f), Play(Card.TWISTED), Swap(true), Say(T("Port is starboard now.", "Backbord ist jetzt Steuerbord."))),
                trap(After(2.4f), Move('f', 10f, 0f, 4f)),
                trap(Zone(11f, 10f, 16f, 15f), Show('A')),
            ),
        ) {
            border(); floor(); pit(1..18)
            fill(1..5, 15..15, 'f')
            put(23, 14, 'A'); put(24, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 78 — climb the cake; the far side of it is not what it seems
        // NOD: Portal, "the cake is a lie"
        Level(
            name = T("The Cake Is A Lie", "Der Kuchen ist eine Lüge"),
            intro = T("Reach the cake for a reward. Definitely a cake.", "Erreiche den Kuchen für eine Belohnung. Ganz sicher ein Kuchen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Show('A'), Say(T("There is no cake. There was never cake.", "Es gibt keinen Kuchen. Es gab nie Kuchen."))),
            ),
        ) {
            border(); floor()
            fill(22..26, 13..13, 'k'); fill(22..26, 14..14)
            put(28, 14, 'A'); put(29, 14, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 79 — a chain of platforms: each reveals the next and deletes the previous. The last one is null.
        // EASTER EGG: NullPointerException / linked list
        Level(
            name = T("NullPointerException", "NullPointerException"),
            intro = T("node.next.next.next... what could be null?", "node.next.next.next... was soll schon null sein?"),
            legend = mapOf('b' to hiddenSolid, 'c' to hiddenSolid, 'd' to hiddenSolid),
            traps = listOf(
                trap(Touch('a'), Play(Card.GHOST_BLOCK), Show('b'), Say(T("a.next = b", "a.next = b"))),
                trap(Touch('b'), Show('c'), Hide('a'), Say(T("b.next = c", "b.next = c"))),
                trap(Touch('c'), Show('d'), Hide('b'), Say(T("c.next = d", "c.next = d"))),
                trap(Touch('d'), Hide('c'), Hide('d'), Say(T("d.next = null. Exception in thread \"main\"!", "d.next = null. Exception in thread \"main\"!")), delay = 0.5f),
            ),
        ) {
            border(); floor(); pit(6..29)
            fill(7..9, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(19..21, 14..14, 'c'); fill(25..27, 14..14, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 80 — the chapter finale: the screen turns blue and everything goes wrong in order
        // EASTER EGG: Blue Screen of Death
        Level(
            name = T("Blue Screen of Death", "Bluescreen des Todes"),
            intro = T("Your level ran into a problem and needs to restart.", "Dein Level hat ein Problem und muss neu gestartet werden."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(6f), Play(Card.GRAND_FINALE), Shake(2f), Say(T(":(  Collecting error info... 0%", ":(  Sammle Fehlerinformationen... 0 %"))),
                trap(PastX(10f), Fall('a'), Say(T("STOP: 0x0000007B", "STOP: 0x0000007B"))),
                trap(PastX(17f), Show('A'), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f)),
                trap(PastX(24.5f), Fall('c'), Say(T("Press any key to continue. Any. Really.", "Drücke eine beliebige Taste. Irgendeine. Wirklich."))),
            ),
        ) {
            border(); floor()
            fill(13..15, 15..17, 'a')
            put(20, 14, 'A'); put(21, 14, 'A')
            fill(26..28, 15..17, 'c')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
