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

/** World 1, levels 81-96: three tricks at a time, plenty of geometry. */
object World1Part6 {

    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    val levels: List<Level> = listOf(
        // 81 — the platforms are unsorted; Mephi sorts them (swapping positions) and you climb the result
        // EASTER EGG: bubble sort, O(n^2)
        Level(
            name = T("Bubble Sort", "Bubblesort"),
            intro = T("Unsorted platforms. Give me a second to sort them.", "Unsortierte Plattformen. Gib mir kurz Zeit zum Sortieren."),
            traps = listOf(
                trap(After(2.5f), Play(Card.SINKING), Move('a', 4f, 0f, 2f), Move('b', -4f, 0f, 2f), Say(T("swap(a, b);", "swap(a, b);"))),
                trap(After(5f), Move('a', 4f, 0f, 2f), Move('c', -4f, 0f, 2f), Say(T("swap(a, c);  Sorted. O(n^2), baby.", "swap(a, c);  Sortiert. O(n²), Baby."))),
            ),
        ) {
            border(); floor()
            fill(8..10, 11..11, 'a'); fill(12..14, 13..13, 'b'); fill(16..18, 12..12, 'c')
            fill(21..30, 9..9)
            put(2, 14, 'P'); put(29, 8, 'D')
        },

        // 82 — gaps of 1, 1, 2, 3 and then 5: the next Fibonacci number comes closer if you wait
        // EASTER EGG: Fibonacci sequence
        Level(
            name = T("Fibonacci", "Fibonacci"),
            intro = T("Gaps: 1, 1, 2, 3, and next comes... 5.", "Lücken: 1, 1, 2, 3, und als Nächstes... 5."),
            traps = listOf(
                trap(PastX(21f), Play(Card.SHY_DOOR), Move('f', -2f, 0f, 3f), Say(T("The sum of the last two. Eventually.", "Die Summe der letzten beiden. Irgendwann."))),
            ),
        ) {
            border(); floor(); pit(4..27)
            fill(5..7, 14..14); fill(9..11, 14..14); fill(14..16, 14..14); fill(20..22, 14..14)
            fill(28..30, 15..17, 'f')
            put(2, 14, 'P'); put(27, 14, 'D')
        },

        // 83 — the duck explains each trap, and is wrong every time
        // EASTER EGG: rubber duck debugging
        Level(
            name = T("Rubber Duck", "Quietscheente"),
            intro = T("Explain your plan to the duck. Quack.", "Erkläre der Ente deinen Plan. Quak."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Duck: \"No spikes here.\" Quack.", "Ente: \"Hier sind keine Spikes.\" Quak."))),
                trap(PastX(17f), Fall('c'), Say(T("Duck: \"The floor is fine.\" Quack.", "Ente: \"Der Boden ist in Ordnung.\" Quak."))),
                trap(PastX(23.5f), Fall('S'), Say(T("Duck: \"The ceiling is fine.\" Quack.", "Ente: \"Die Decke ist in Ordnung.\" Quak."))),
            ),
        ) {
            border(); floor(); pit(12..30)
            fill(12..30, 13..13); fill(20..22, 13..13, 'c')
            put(8, 14, 'A'); put(9, 14, 'A')
            put(27, 1, 'S'); put(28, 1, 'S')
            put(2, 14, 'P'); put(30, 12, 'D')
        },

        // 84 — three tiers, back and forth: right along the floor, left along the middle, right along the top
        Level(
            name = T("Switchback", "Serpentinen"),
            intro = T("The scenic route. All three floors of it.", "Die Panoramastrecke. Alle drei Etagen."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(PastX(11.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Floor one: no floor.", "Etage eins: kein Boden."))),
                trap(Zone(14f, 7f, 22f, 9.5f), Show('A'), Say(T("Floor two: spikes.", "Etage zwei: Spikes."))),
                trap(Zone(14f, 3f, 22f, 5.5f), Show('B'), Say(T("Floor three: more spikes.", "Etage drei: mehr Spikes."))),
            ),
        ) {
            border(); floor()
            fill(14..16, 15..17, 'a')
            fill(27..28, 13..14); fill(29..30, 11..14)
            fill(1..28, 9..9)
            fill(1..3, 7..7); fill(6..30, 5..5)
            put(11, 8, 'A'); put(12, 8, 'A'); put(21, 4, 'B'); put(22, 4, 'B')
            put(2, 14, 'P'); put(29, 4, 'D')
        },

        // 85 — five tiles of nothing. Jump at the very last moment.
        Level(
            name = T("Pixel Perfect", "Pixelgenau"),
            intro = T("It is only five tiles wide. Coyote time is your friend.", "Es ist nur fünf Felder breit. Koyotenzeit ist dein Freund."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(15f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Now do it again, but pointier.", "Und jetzt nochmal, nur spitzer."))),
            ),
        ) {
            border(); floor(); pit(13..17)
            put(20, 14, 'A'); put(21, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 86 — the floor collapses right behind you, faster than you would like
        // EASTER EGG: speedrun "Any%"
        Level(
            name = T("Any%", "Any%"),
            intro = T("Speedrun time! Frame-perfect, no glitches. (Glitches allowed.)", "Speedrun-Zeit! Frame-perfekt, keine Glitches. (Glitches erlaubt.)"),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Timer started. Don't stop.", "Timer gestartet. Nicht stehen bleiben."))),
                trap(PastX(5.5f), Fall('b'), delay = 0.57f),
                trap(PastX(5.5f), Fall('c'), delay = 0.92f),
                trap(PastX(5.5f), Fall('d'), delay = 1.51f),
                trap(PastX(5.5f), Fall('e'), delay = 1.86f),
                trap(PastX(5.5f), Fall('f'), delay = 2.22f),
                trap(PastX(5.5f), Fall('g'), delay = 2.8f),
                trap(PastX(5.5f), Fall('h'), delay = 3.15f),
                trap(PastX(5.5f), Fall('i'), delay = 3.45f),
            ),
        ) {
            border(); floor(); pit(10..11); pit(21..22)
            fill(1..3, 15..17, 'a'); fill(4..6, 15..17, 'b'); fill(7..9, 15..17, 'c')
            fill(12..14, 15..17, 'd'); fill(15..17, 15..17, 'e'); fill(18..20, 15..17, 'f')
            fill(23..25, 15..17, 'g'); fill(26..28, 15..17, 'h'); fill(29..30, 15..17, 'i')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 87 — the same tricks, from the other side: you walk right to left
        Level(
            name = T("Mirror Match", "Spiegelmatch"),
            intro = T("Same level. Other direction. Same spikes.", "Gleiches Level. Andere Richtung. Gleiche Spikes."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(BeforeX(24f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Spikes read right to left, too.", "Spikes lesen auch von rechts nach links."))),
                trap(BeforeX(16.5f), Fall('a'), Say(T("Left is where the floor ends.", "Links endet der Boden."))),
                trap(BeforeX(11f), Fall('S'), Say(T("And the ceiling starts.", "Und die Decke fängt an."))),
            ),
        ) {
            border(); floor()
            put(19, 14, 'A'); put(20, 14, 'A')
            fill(11..13, 15..17, 'a')
            put(5, 1, 'S'); put(6, 1, 'S')
            put(29, 14, 'P'); put(2, 14, 'D')
        },

        // 88 — up up down down, left right left right, B A: gravity and controls, then two hops
        // EASTER EGG: Konami code
        Level(
            name = T("UP UP DOWN DOWN", "HOCH HOCH RUNTER RUNTER"),
            intro = T("30 extra lives. If you can type it.", "30 Extraleben. Wenn du es eintippen kannst."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("UP UP", "HOCH HOCH"))),
                trap(PastX(12f), Gravity(false), Say(T("DOWN DOWN", "RUNTER RUNTER"))),
                trap(PastX(15f), Swap(true), Say(T("LEFT", "LINKS"))),
                trap(PastX(18f), Swap(false), Say(T("RIGHT ... LEFT RIGHT", "RECHTS ... LINKS RECHTS"))),
                trap(PastX(23f), Show('A'), Say(T("B A  START", "B A  START"))),
            ),
        ) {
            border(); floor(); pit(21..22)
            fill(6..10, 14..14, '^')
            put(8, 1, 'v')
            put(26, 14, 'A'); put(27, 14, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 89 — the pit is the pipe: the door zips down into it as you approach
        // EASTER EGG: Super Mario warp zone
        Level(
            name = T("Warp Zone", "Warpzone"),
            intro = T("Everything to the right is a distraction. Trust me.", "Alles rechts ist Ablenkung. Vertrau mir."),
            traps = listOf(
                trap(PastX(6f), Play(Card.SHY_DOOR), DoorTo(9, 16, speed = 40f), Say(T("Welcome to Warp Zone!", "Willkommen in der Warpzone!"))),
            ),
        ) {
            border(); floor()
            fill(8..10, 15..16, '.')
            fill(15..16, 14..14, '^'); pit(20..21)
            fill(26..27, 9..14)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 90 — a thin bridge over nothing: spikes, stalactites and a missing plank
        Level(
            name = T("Sky Bridge", "Himmelsbrücke"),
            intro = T("What a view. Try not to look at your feet.", "Was für eine Aussicht. Schau nicht auf deine Füße."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(9.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Rail-less and spiky.", "Ohne Geländer, dafür mit Spikes."))),
                trap(PastX(16.6f), Fall('S'), Say(T("Rock fall. Wait for it.", "Steinschlag. Warte kurz."))),
                trap(PastX(20f), Fall('c'), Say(T("Missing plank: the classic.", "Fehlendes Brett: der Klassiker."))),
            ),
        ) {
            border()
            fill(1..30, 9..9); fill(24..26, 9..9, 'c')
            put(14, 8, 'A'); put(15, 8, 'A')
            put(21, 1, 'S'); put(22, 1, 'S')
            put(2, 8, 'P'); put(29, 8, 'D')
        },

        // 91 — the spiked floor gets swapped for the ceiling, which has holes of its own
        Level(
            name = T("Inside Out", "Innen und Außen"),
            intro = T("The floor is spiky. The ceiling is more of a suggestion.", "Der Boden ist spitz. Die Decke eher ein Vorschlag."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Everything you know is upside down.", "Alles, was du kennst, steht kopf."))),
                trap(PastX(20.5f), Gravity(false), Say(T("And back. Nausea included.", "Und zurück. Übelkeit inklusive."))),
            ),
        ) {
            border(); floor()
            fill(8..18, 14..14, '^')
            put(10, 1, 'v'); put(11, 1, 'v')
            put(14, 0, '.'); put(15, 0, '.')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 92 — the ceiling of spikes comes down; the bridge appears only just in time
        Level(
            name = T("Deadline", "Deadline"),
            intro = T("I need it done by end of day. Or end of ceiling.", "Ich brauche es bis Feierabend. Oder bis Deckenende."),
            legend = mapOf('C' to ceilingSpike, 'b' to hiddenSolid),
            traps = listOf(
                trap(After(0.5f), Play(Card.HEADBUTT), Move('C', 0f, 12.6f, 1.1f), Say(T("Deadline approaching.", "Deadline naht."))),
                trap(After(8f), Show('b'), Say(T("Bridge deployed. Just in time. As always.", "Brücke ausgeliefert. Gerade so pünktlich. Wie immer."))),
            ),
        ) {
            border(); floor(); pit(12..19)
            fill(1..30, 1..1, 'C')
            fill(12..19, 15..15, 'b')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 93 — two of these floor segments are bad sectors: they vanish when touched
        // EASTER EGG: bad sector
        Level(
            name = T("Bad Sector", "Defekter Sektor"),
            intro = T("Reading floor... Read error, retrying.", "Lese Boden... Lesefehler, neuer Versuch."),
            traps = listOf(
                trap(Touch('b'), Play(Card.CRUMBLE), Hide('b'), Say(T("Bad sector detected.", "Defekten Sektor erkannt.")), delay = 0.1f),
                trap(Touch('e'), Hide('e'), Say(T("Another one. Backup, anyone?", "Noch einer. Hat jemand ein Backup?")), delay = 0.1f),
            ),
        ) {
            border(); floor(); pit(6..17)
            fill(6..7, 15..17); fill(8..9, 15..17, 'b'); fill(10..11, 15..17)
            fill(12..13, 15..17); fill(14..15, 15..17, 'e'); fill(16..17, 15..17)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 94 — a gap that is too wide for one jump: a short hop reveals a platform to jump from again
        // EASTER EGG: double jump (DLC)
        Level(
            name = T("Double Jump", "Doppelsprung"),
            intro = T("Seven tiles. Double jump is a separate DLC.", "Sieben Felder. Doppelsprung ist ein separates DLC."),
            legend = mapOf('p' to hiddenSolid),
            traps = listOf(
                trap(Zone(11f, 0f, 13.4f, 13.9f), Play(Card.GHOST_BLOCK), Show('p'), Say(T("Double jump unlocked! DLC: 4.99.", "Doppelsprung freigeschaltet! DLC: 4,99.")), delay = 0.12f),
            ),
        ) {
            border(); floor(); pit(12..18)
            fill(14..16, 14..14, 'p')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 95 — the floor is lava; the islands are furniture, one of them wobbles
        // EASTER EGG: "The floor is lava"
        Level(
            name = T("The Floor Is Lava", "Der Boden ist Lava"),
            intro = T("Don't touch the floor! Couch, table, chair. Go!", "Nicht den Boden berühren! Sofa, Tisch, Stuhl. Los!"),
            traps = listOf(
                trap(Touch('c'), Play(Card.CRUMBLE), Fall('c'), Say(T("That couch was not rated for adults.", "Das Sofa war nicht für Erwachsene zugelassen.")), delay = 0.35f),
            ),
        ) {
            border(); floor()
            fill(1..30, 14..14, '^')
            fill(1..4, 14..14); fill(7..9, 13..13); fill(12..15, 14..14, 'c'); fill(18..19, 12..12)
            fill(22..25, 13..13); fill(28..30, 14..14)
            put(2, 13, 'P'); put(30, 13, 'D')
        },

        // 96 — chapter finale: five tricks in a row
        Level(
            name = T("Boss Rush", "Bossrush"),
            intro = T("Everything you learned. In one go. No breaks.", "Alles, was du gelernt hast. In einem Rutsch. Ohne Pause."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(6f), Play(Card.COLLAPSE), Fall('a'), Say(T("Round one: floor.", "Runde eins: Boden."))),
                trap(PastX(12.5f), Show('A'), Say(T("Round two: spikes.", "Runde zwei: Spikes."))),
                trap(PastX(17.5f), Fall('S'), Say(T("Round three: ceiling.", "Runde drei: Decke."))),
                trap(PastX(23f), Swap(true), Say(T("Final round: left is right.", "Letzte Runde: links ist rechts."))),
            ),
        ) {
            border(); floor(); pit(26..27)
            fill(9..11, 15..17, 'a')
            put(16, 14, 'A'); put(17, 14, 'A')
            put(21, 1, 'S'); put(22, 1, 'S')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
