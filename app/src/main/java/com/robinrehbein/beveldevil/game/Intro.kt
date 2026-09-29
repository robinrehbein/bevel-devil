package com.robinrehbein.beveldevil.game

/** The intro: a boot log of the hijacked network, then Mephi, Bevel and the way down. */
object Intro {
    enum class Kind { BOOT, DAEMON, PACKET, ROOT }
    class Page(val kind: Kind, val text: T?, val mood: Mood)
    enum class Tag(val label: String) { OK("[ OK ] "), FAIL("[FAIL] "), WARN("[WARN] "), WAIT("[ .. ] "), NONE("") }
    class Line(val tag: Tag, val text: T)

    const val CPS = 30f
    const val LINE_T = 0.22f

    val pages = listOf(
        Page(Kind.BOOT, null, Mood.GRIN),
        Page(Kind.DAEMON, T("I'm Mephi. A daemon. Background process, devil... same thing here.", "Ich bin Mephi. Ein Daemon. Oder Dämon. Bei mir ist das dasselbe."), Mood.GRIN),
        Page(Kind.PACKET, T("All packets dropped. Except this one. It just keeps arriving. Annoying.", "Alle Pakete verworfen. Bis auf eins. Das kommt einfach immer wieder an. Nervig."), Mood.SULK),
        Page(Kind.ROOT, T("Come get me. Root is all the way down. Dress warm.", "Komm und hol mich. Root liegt ganz unten. Zieh dir was Warmes an."), Mood.LAUGH),
    )

    val boot = listOf(
        Line(Tag.OK, T("Started network.target", "network.target gestartet")),
        Line(Tag.OK, T("Reached target Multi-User System", "Multi-User-System erreicht")),
        Line(Tag.OK, T("Started Bevel packet service", "Bevel-Paketdienst gestartet")),
        Line(Tag.WARN, T("unknown process at PID 666", "Unbekannter Prozess: PID 666")),
        Line(Tag.FAIL, T("mephi.service: devil detected", "mephi.service: Teufel erkannt")),
        Line(Tag.FAIL, T("sudo: mephi is not in sudoers", "sudo: mephi steht nicht in sudoers")),
        Line(Tag.NONE, T("This incident will be reported. (To Mephi.)", "Dieser Vorfall wird gemeldet. (An Mephi.)")),
        Line(Tag.OK, T("Started hellfire.service", "hellfire.service gestartet")),
        Line(Tag.WAIT, T("Rerouting all traffic to /dev/hell", "Leite den Verkehr nach /dev/hell um")),
        Line(Tag.FAIL, T("firewall.service: he just knocked", "firewall.service: er hat einfach geklingelt")),
        Line(Tag.FAIL, T("Kernel panic: too much fun", "Kernel Panic: zu viel Spaß")),
        Line(Tag.WARN, T("4,096 packets dropped", "4.096 Pakete verworfen")),
        Line(Tag.OK, T("1 packet still alive", "1 Paket lebt noch")),
    )

    /** Seconds until page [i] has finished typing. */
    fun duration(i: Int): Float {
        val p = pages[i]
        return if (p.kind == Kind.BOOT) (boot.size + 2) * LINE_T + 0.4f else p.text.toString().length / CPS + 0.3f
    }
}
