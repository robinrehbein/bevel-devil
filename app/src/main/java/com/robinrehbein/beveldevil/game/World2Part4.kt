package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

private val hidden = Glyph(spike = true, hidden = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)

/** World 2, levels 49-64: "Storage". */
internal val world2Part4: List<Level> = listOf(
    // 49 — EASTER EGG: click of death (hard disk head crash)
    Level(
        name = T("Click of Death", "Klick des Todes"),
        intro = T("Click. Click. Click. That's the sound of your data.", "Klick. Klick. Klick. So klingen deine Daten."),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Head crash on platter 1.", "Head-Crash auf Platte 1."), delay = 0.3f),
            trap(Touch('b'), Fall('b'), delay = 0.3f),
            trap(Touch('c'), Fall('c'), delay = 0.3f),
            trap(PastX(11f), Saw(15.5f, -1f, 0f, 14f)),
            trap(PastX(16f), Saw(20.5f, 19f, 0f, -13f)),
        ),
    ) {
        border()
        fill(0..8, 15..17); fill(24..31, 15..17)
        fill(10..12, 15..15, 'a'); fill(15..17, 15..15, 'b'); fill(20..22, 15..15, 'c')
        fill(13..19, 1..2)
        spawn(); door(); bits(49, x0 = 4)
    },

    // 50 — EASTER EGG: RAID 0 (one drive fails, all drives fail)
    Level(
        name = T("RAID 0", "RAID 0"),
        intro = T("Twice the speed. Zero the redundancy.", "Doppelte Geschwindigkeit. Null Redundanz."),
        traps = ('a'..'e').flatMap { t ->
            ('a'..'e').map { g -> trap(Touch(t), Fall(g), delay = 2.2f) }
        } + trap(Touch('a'), Play(Card.COLLAPSE), say("RAID 0: if one drive dies, they all die.", "RAID 0: Stirbt eine Platte, sterben alle.")),
    ) {
        border()
        fill(0..5, 15..17); fill(22..31, 15..17)
        fill(7..8, 15..15, 'a'); fill(10..11, 15..15, 'b'); fill(13..14, 15..15, 'c'); fill(16..17, 15..15, 'd'); fill(19..20, 15..15, 'e')
        spawn(); door(); bits(50)
    },

    // 51 — EASTER EGG: RAID 1 (mirror; the second half is mirrored)
    Level(
        name = T("RAID 1", "RAID 1"),
        intro = T("Everything twice. Mirrored, of course.", "Alles doppelt. Gespiegelt, natürlich."),
        traps = listOf(
            trap(PastX(15.5f), Play(Card.TWISTED), Swap(true), say("Mirroring drive 2...", "Spiegele Laufwerk 2...")),
            trap(PastX(26.5f), Swap(false), say("Mirror degraded.", "Spiegel degradiert.")),
        ),
    ) {
        border(); floor()
        put(9, 14, '^'); pit(12..13)
        pit(18..19); put(22, 14, '^')
        rack(25, 1, 2)
        spawn(); door(); bits(51)
    },

    // 52 — EASTER EGG: RAID 5 (one in five drives may fail)
    Level(
        name = T("RAID 5", "RAID 5"),
        intro = T("Parity protects you. From one failure. Probably.", "Parität schützt dich. Vor einem Ausfall. Vermutlich."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Drive 1 failed. Rebuilding...", "Laufwerk 1 ausgefallen. Rebuild..."), delay = 0.25f),
            trap(Touch('b'), Move('b', 0f, 12f, 8f), delay = 0.1f),
            trap(Touch('d'), Show('A'), say("Drive 4: parity error.", "Laufwerk 4: Paritätsfehler.")),
            trap(Touch('e'), Fall('e'), delay = 0.25f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(27..31, 15..17)
        fill(7..8, 15..15, 'a'); fill(10..11, 14..14, 'b'); fill(14..15, 15..15, 'c'); fill(18..20, 14..14, 'd'); fill(23..24, 15..15, 'e')
        put(19, 13, 'A')
        spawn(); door(); bits(52)
    },

    // 53 — EASTER EGG: "No space left on device" (the floor rises to fill the disk)
    Level(
        name = T("Disk Full", "Festplatte voll"),
        intro = T("No space left on device. Especially above you.", "Kein Speicherplatz mehr. Vor allem über dir."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(After(2.4f), Play(Card.SINKING), Move('f', 0f, -12f, 1.3f), say("Disk usage: 100%.", "Datenträgerbelegung: 100 %.")),
            trap(Zone(24f, 0f, 31f, 6f), Show('A')),
            trap(After(7.0f), Saw(23f, 8.5f, 6f, 0f), say("Swapping in a saw. Sorry, a swap file.", "Lagere eine Säge aus. Pardon, eine Auslagerungsdatei.")),
            trap(After(8.4f), Saw(34f, 5.5f, -8f, 0f)),
        ),
    ) {
        border()
        fill(1..30, 15..17, 'f')
        put(12, 7, 'v'); put(13, 7, 'v'); put(20, 10, 'v'); put(21, 10, 'v')
        put(27, 2, 'A')
        spawn(); put(29, 2, 'D')
    },

    // 54 — EASTER EGG: OOM killer (it picks its victim: you)
    Level(
        name = T("OOM Killer", "OOM-Killer"),
        intro = T("Out of memory. Somebody has to go.", "Speicher voll. Einer muss gehen."),
        traps = listOf(3f, 8f, 13f, 18f, 23f, 28f).mapIndexed { i, x ->
            trap(PastX(3.6f), *(if (i == 0) arrayOf(Play(Card.DEVIL_SAW), say("Killed process 1337 (you).", "Prozess 1337 (du) beendet.")) else emptyArray()), Saw(x, 22f, 0f, -2.4f, 3f))
        },
    ) {
        border(); floor()
        fill(5..7, 13..13); fill(10..12, 11..11); fill(15..17, 9..9); fill(20..22, 7..7); fill(25..28, 5..5)
        spawn(); put(27, 4, 'D')
    },

    // 55 — EASTER EGG: swap space (swapped controls and swapped gravity)
    Level(
        name = T("Swap Space", "Auslagerungsdatei"),
        intro = T("Your controls have been paged out to disk.", "Deine Steuerung wurde auf die Festplatte ausgelagert."),
        traps = listOf(
            trap(PastX(8f), Play(Card.TWISTED), Swap(true), Gravity(true), say("Swapping... everything.", "Auslagern... alles.")),
            trap(PastX(20f), Swap(false), say("Page fault. Controls restored.", "Seitenfehler. Steuerung zurück.")),
            trap(PastX(26.5f), Gravity(false)),
        ),
    ) {
        border(); floor()
        leds(9..24)
        put(12, 1, 'v'); put(17, 1, 'v'); put(18, 1, 'v'); put(23, 1, 'v')
        spawn(); door()
    },

    // 56 — EASTER EGG: defragmentation (the platforms move to make room)
    Level(
        name = T("Defragmenting", "Defragmentierung"),
        intro = T("Defragmenting... 3% (estimated time: 3 years)", "Defragmentiere... 3 % (Restzeit: 3 Jahre)"),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('a', -3f, 0f, 3f), say("Moving block 7 to block 4.", "Verschiebe Block 7 nach Block 4."), delay = 0.35f),
            trap(Touch('b'), Move('b', -3f, 0f, 3f), delay = 0.35f),
            trap(Touch('c'), Move('c', -3f, 0f, 3f), delay = 0.35f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(25..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(13..15, 15..15, 'b'); fill(19..21, 15..15, 'c')
        spawn(); door(); bits(56)
    },

    // 57 — EASTER EGG: backup restore (the restore runs on the ceiling)
    Level(
        name = T("Restore from Backup", "Aus Backup wiederherstellen"),
        intro = T("Backup last tested: never.", "Backup zuletzt getestet: nie."),
        traps = listOf(
            trap(Touch('a'), Fall('a'), delay = 0.35f),
            trap(Touch('b'), Fall('b'), delay = 0.35f),
            trap(Touch('c'), Fall('c'), delay = 0.35f),
            trap(PastX(24.5f), Play(Card.UPSIDE_DOWN), Gravity(true), DoorTo(2, 1, speed = 20f, hanging = true), say("Restoring... upside down. The tape was inserted wrong.", "Stelle wieder her... kopfüber. Das Band war falsch herum.")),
        ),
    ) {
        border()
        fill(0..7, 15..17); fill(25..31, 15..17)
        fill(9..10, 15..15, 'a'); fill(14..15, 15..15, 'b'); fill(19..21, 15..15, 'c')
        put(8, 1, 'v'); put(15, 1, 'v'); put(16, 1, 'v')
        spawn(); door()
    },

    // 58 — EASTER EGG: tape drive (slow seek, rewinding gates)
    Level(
        name = T("Tape Drive", "Bandlaufwerk"),
        intro = T("Seeking... seeking... seeking...", "Suche... suche... suche..."),
        traps = listOf(
            trap(After(2.0f), Play(Card.SINKING), Move('g', 0f, -6f, 6f), say("Rewinding.", "Spule zurück.")),
            trap(After(3.6f), Move('g', 0f, 6f, 9f)),
            trap(After(2.8f), Move('h', 0f, -6f, 6f)),
            trap(After(4.4f), Move('h', 0f, 6f, 9f)),
            trap(After(3.6f), Move('i', 0f, -6f, 6f)),
            trap(After(5.2f), Move('i', 0f, 6f, 9f)),
        ),
    ) {
        border(); floor()
        fill(12..12, 9..14, 'g'); fill(19..19, 9..14, 'h'); fill(26..26, 9..14, 'i')
        spawn(); door(); bits(58)
    },

    // 59 — EASTER EGG: bit rot (the zeros and ones flip)
    Level(
        name = T("Bit Rot", "Bitfäule"),
        intro = T("The floor is stored on very old magnetic tape.", "Der Boden liegt auf sehr altem Magnetband."),
        legend = mapOf('Q' to hidden),
        traps = blink('X', 1.6f, 1.4f, 3.6f, 4, first = listOf(Play(Card.SPIKE_SEED), say("Bit 4711 flipped from 1 to 0.", "Bit 4711 von 1 auf 0 gekippt."))) +
            blinkOn('Q', 3.0f, 2.2f, 3.6f, 4),
    ) {
        border(); floor()
        for (x in listOf(8, 14, 20)) leds(x..x + 2, c = 'X')
        for (x in listOf(11, 17, 23)) leds(x..x + 2, c = 'Q')
        spawn(); door(); bits(59)
    },

    // 60 — EASTER EGG: dangling pointer (use after free: the door lives in the freed pit)
    Level(
        name = T("Dangling Pointer", "Hängender Zeiger"),
        intro = T("The door is at address 0xDEADBEEF.", "Die Tür liegt an Adresse 0xDEADBEEF."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Zone(11f, 15.2f, 18f, 18f), Show('A'), say("Use after free.", "Use after free.")),
        ) + doorTrail(
            PastX(20f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(14, 1, 26f, hanging = true), DoorTo(14, 16, 26f)),
            first = listOf(Play(Card.SHY_DOOR), say("free(door); door->open();", "free(tuer); tuer->oeffnen();")),
        ),
    ) {
        border(); floor()
        fill(11..17, 15..16, '.')
        put(12, 16, 'A'); put(16, 16, 'A')
        rack(6, 1, 2); rack(23, 1, 2)
        spawn(); door()
    },

    // 61 — EASTER EGG: tape robot (two lifts, one library)
    Level(
        name = T("Tape Robot", "Bandroboter"),
        intro = T("The robot fetches tape #42. Slowly.", "Der Roboter holt Band Nr. 42. Langsam."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('m'), Play(Card.SINKING), Move('m', 0f, -10f, 4f), say("Robot arm: going up.", "Roboterarm: fährt hoch."), delay = 0.2f),
            trap(Touch('n'), Move('n', 0f, 10f, 4f), say("Robot arm: going down.", "Roboterarm: fährt runter."), delay = 0.3f),
            trap(Zone(14f, 0f, 18f, 5f), Show('A')),
        ),
    ) {
        border(); floor()
        fill(9..11, 14..14, 'm')
        fill(14..17, 4..14)
        fill(20..22, 4..4, 'n')
        put(15, 3, 'A')
        spawn(); door(28); bits(61, x0 = 24, y = 1)
    },

    // 62 — EASTER EGG: cosmic ray bit flip (gravity flips once, at a fixed time)
    Level(
        name = T("Cosmic Ray", "Kosmische Strahlung"),
        intro = T("A single bit flips. Somewhere in the gravity register.", "Ein einzelnes Bit kippt. Irgendwo im Schwerkraft-Register."),
        traps = listOf(
            trap(After(3.0f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Single-event upset detected.", "Single-Event-Upset erkannt.")),
            trap(After(9.0f), Gravity(false)),
        ),
    ) {
        border(); floor()
        leds(9..11); leds(16..18)
        put(14, 1, 'v'); put(21, 1, 'v'); put(22, 1, 'v'); put(26, 1, 'v')
        spawn(); put(29, 1, 'D')
    },

    // 63 — EASTER EGG: checksum mismatch (crc32 says: everything is wrong)
    Level(
        name = T("Checksum Mismatch", "Prüfsummenfehler"),
        intro = T("sha256sum: FAILED. Every bit is wrong.", "sha256sum: FEHLGESCHLAGEN. Jedes Bit ist falsch."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(4f), Play(Card.TWISTED), Saw(-1.5f, 14.4f, 6f, 0f), Swap(true), say("Expected 0xCAFEBABE, got 0xDEADC0DE.", "Erwartet 0xCAFEBABE, erhalten 0xDEADC0DE.")),
            trap(PastX(11f), Gravity(true)),
            trap(PastX(21f), Swap(false)),
            trap(PastX(26.5f), Gravity(false)),
        ),
    ) {
        border(); floor()
        leds(13..24)
        put(17, 1, 'v'); put(20, 1, 'v')
        spawn(); door(30)
    },

    // 64 — EASTER EGG: disaster recovery (everything at once; act finale)
    Level(
        name = T("Disaster Recovery", "Katastrophenfall"),
        intro = T("The recovery plan is in the building that just burned down.", "Der Notfallplan liegt im Gebäude, das gerade abgebrannt ist."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(3f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6f, 0f), say("Disaster recovery, step 1: panic.", "Notfallwiederherstellung, Schritt 1: Panik.")),
            trap(PastX(8f), Fall('a')),
            trap(PastX(14.5f), Show('A'), say("Step 2: blame the intern.", "Schritt 2: dem Praktikanten die Schuld geben.")),
            trap(PastX(19.5f), Fall('b')),
            trap(PastX(26f), Gravity(true), DoorTo(20, 1, speed = 16f, hanging = true), say("Step 3: restore from the cloud. (Upwards.)", "Schritt 3: aus der Cloud wiederherstellen. (Nach oben.)")),
        ),
    ) {
        border(); floor()
        fill(9..11, 15..17, 'a'); fill(22..24, 15..17, 'b')
        put(17, 14, 'A'); put(18, 14, 'A')
        put(14, 1, 'v'); put(17, 1, 'v')
        spawn(); door()
    },
)
