package com.robinrehbein.beveldevil.game

/** Mephi's remarks after the 3rd, 6th and 10th death in one level: teasing, mock sympathy, grudging respect. */
object DevilQuips {
    /** Deaths in one level that earn a quip. */
    val milestones = listOf(3, 6, 10)
    /** Longest line that still fits the speech bubble on two lines. */
    const val MAX_LEN = 56

    /** Per world, per tier (3 / 6 / 10 deaths): three lines each. */
    val pools: List<List<List<T>>> = listOf(
        listOf( // world 1: hell's cellar
            listOf(
                T("Three already? The cellar is warming up.", "Schon drei? Der Keller wird gerade warm."),
                T("Hell has a welcome mat. You found the spikes.", "Die Hölle hat einen Fußabtreter. Du fandest die Spikes."),
                T("Three deaths. I've seen sinners do worse.", "Drei Tode. Ich hab schon Sünder mit weniger gesehen."),
            ),
            listOf(
                T("Six! Don't worry, I'll keep your spot warm.", "Sechs! Keine Sorge, dein Platz bleibt warm."),
                T("There, there. The floor didn't mean it.", "Na na. Der Boden meint's nicht böse."),
                T("Six deaths. I'm touched. Truly. Ahem.", "Sechs Tode. Ich bin gerührt. Wirklich. Hüstel."),
            ),
            listOf(
                T("Ten! I'm almost impressed. Almost.", "Zehn! Ich bin fast beeindruckt. Fast."),
                T("Ten deaths. You'd make a fine demon.", "Zehn Tode. Du gäbst einen feinen Dämon ab."),
                T("Ten times! Stubborn. I like that.", "Zehnmal! Stur. Das mag ich an dir."),
            ),
        ),
        listOf( // world 2: crystals and portals
            listOf(
                T("Three portals, three exits. None survivable.", "Drei Portale, drei Ausgänge. Keiner überlebbar."),
                T("Crystals are forever. So is this level.", "Kristalle sind für ewig. Dieses Level auch."),
                T("Packet loss: you. Again.", "Paketverlust: du. Schon wieder."),
            ),
            listOf(
                T("Six! Even the portals feel sorry now.", "Sechs! Sogar die Portale haben Mitleid."),
                T("Sweet of you to test every shard for me.", "Nett, dass du alle Scherben für mich testest."),
                T("Ping: timeout. Pong: you, again.", "Ping: Timeout. Pong: du, schon wieder."),
            ),
            listOf(
                T("Ten! Crystal clear: almost impressive.", "Zehn! Kristallklar: fast beeindruckend."),
                T("Ten portal trips. I'm proud. Ish.", "Zehn Reisen durchs Portal. Ich bin stolz. Ungefähr."),
                T("Ten! You outshine the gems. Almost.", "Zehn! Du glänzt mehr als die Kristalle. Fast."),
            ),
        ),
        listOf( // world 3: circuit board, hacker terminal
            listOf(
                T("segfault #3. It'll work any second now.", "segfault #3. Gleich klappt's bestimmt."),
                T("sudo survive. Permission denied.", "sudo überleben. Zugriff verweigert."),
                T("Three crashes. Tried a reboot?", "Drei Abstürze. Schon mal neu gebootet?"),
            ),
            listOf(
                T("segfault #6. Not you. The silicon.", "segfault #6. Liegt nicht an dir. Silizium."),
                T("Six short circuits. I'd hug you. Sparks.", "Sechs Kurzschlüsse. Ich würd dich drücken. Funken."),
                T("Error 6: pity not found. Kidding. Mostly.", "Fehler 6: Mitleid nicht gefunden. Scherz. Fast."),
            ),
            listOf(
                T("segfault #10. Okay, that's some uptime.", "segfault #10. Okay, das ist Durchhaltevermögen."),
                T("Ten crashes, no rage quit. Respect.exe.", "Zehn Abstürze, kein Ragequit. Respekt.exe."),
                T("Ten! You debug like a true daemon.", "Zehn! Du debuggst wie ein echter Daemon."),
            ),
        ),
    )

    /** The tier (0..2) for the [deaths]th death, or null when it is no milestone. */
    fun tier(deaths: Int): Int? = milestones.indexOf(deaths).takeIf { it >= 0 }

    /** The quip for [deaths] in level [levelIndex], seeded by level and tier, never equal to [last]; null if no milestone. */
    fun pick(levelIndex: Int, deaths: Int, last: String? = null): String? {
        val tier = tier(deaths) ?: return null
        val lines = pools[(Worlds.of(levelIndex).number - 1).coerceIn(0, pools.lastIndex)][tier]
        var i = java.util.Random(levelIndex * 31L + tier).nextInt(lines.size)
        repeat(lines.size) {
            if (lines[i].toString() != last) return lines[i].toString()
            i = (i + 1) % lines.size
        }
        return lines[i].toString()
    }
}
