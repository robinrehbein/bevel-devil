# Level-Design V2: Rätselräume statt Ein-Fallen-Level

Verbindliche Vorgabe für den Umbau aller drei Welten. Jeder Agent, der Level baut oder prüft, liest dieses Dokument zuerst.

---

## 1. Warum

Feedback nach dem Playtest (Gründer):

> „Ab Level 10 in Welt 2 gibt es keine Überraschung mehr und keinen Drang weiterzumachen. Bei Level Devil sind die Level mehr ein Puzzle oder Labyrinth. Man ist binnen Sekunden durch.“

Die Bestandsaufnahme aller 144 Level bestätigt das:

| | Welt 1 (Höllenkeller) | Welt 2 (Höllen-Rechenzentrum) | Welt 3 (Platine) |
|---|---|---|---|
| Mittlerer sauberer Lauf | ca. 4 s | ca. 3,7 s | ca. 4–8 s |
| Level mit „Stacheln wachsen“ (`Show`) | 32 von 48 | 36 von 48 | 1 von 48 |
| Wiederkehrendes Schlussmuster | Hüpfer, Stacheln hinter der Landung (ca. 30-mal) | Dasselbe | Heiße Landefläche (`HeatSpike`) in fast jedem Level |
| Hauptmechanik lange am Stück | Blink/PathSaw in 11 von 16 Leveln (Akt 2) | Band, Laser oder Portal in allen 16 Leveln (Akt 2) | Hitze 17–26, Lüfter 33–42, Strom 6–11 |
| Echte Rätsel- oder Routen-Elemente | 8 Level | 6 Level | ca. 15 Level (Pads, Kühlen, Tür-Rückweg) |
| Ungenutzte Bausteine | Portale, Bänder, Laser, Pads | **Pads und Schalter: 0-mal**. Reroute nur als Einmalfalle, `DoorTo` 3-mal, `Move` 3-mal | Laser, `Move`, Pad→Lüfter und Pad→Tür |

**Kern des Problems:** Jedes Level ist **eine Reaktion** (laufen, hüpfen, Falle, Tür). Die Falle stammt fast immer aus derselben Familie. Wer das Muster einmal durchschaut hat, sieht nichts Neues mehr. Level Devil wirkt anders, obwohl jeder Raum auf einen Bildschirm passt:

- Der Raum ist ein kleines **Labyrinth**: Der Weg führt nach oben, zurück und durch Sackgassen.
- Es kommen **mehrere Überraschungen hintereinander**, und jede bricht die Regel, die die vorige gerade gelehrt hat.
- Die Fallen kommen aus **vielen verschiedenen Familien**: Der Boden rutscht weg, die Decke fällt, die Tür flieht, die Wand schiebt sich heran, die Plattform wird weggezogen, das Level spiegelt sich.

## 2. Harte Regeln

Diese Regeln gelten für jedes Level ab W1-7. Wo eine Regel prüfbar ist, wird sie per Test durchgesetzt.

| # | Regel | Test |
|---|---|---|
| H1 | **Ein Bildschirm** (32×18), kein Scrollen. | vorhanden (Grid) |
| H2 | **Rätselraum:** mindestens ein Rätsel-Baustein aus §4 (R1–R12). Nur nach rechts laufen reicht nie. | `holdRightWithHopsNeverWins` für alle Level |
| H3 | **Dauer:** Ein sauberer, informierter Bot-Lauf dauert mindestens die Mindestzeit aus §6. Gemessen wird `world.time` beim Sieg. | `cleanRunLastsLongEnough` |
| H4 | **2–4 Überraschungen** pro Runde. Sie bauen aufeinander auf: Jede bestraft die Lösung, die die vorige nahegelegt hat. | Trigger-Zählung (vorhanden, verschärfen) |
| H5 | **Höchstens eine** `Show`-Stachelfalle pro Runde. Höchstens 25 % der Level eines Aktes haben überhaupt eine. Für W3 gilt dasselbe für `HeatSpike` als Schlussfalle. | `spikePopupQuota` |
| H6 | **Abwechslung:** Zwei aufeinanderfolgende Level (Akt-Finale ausgenommen) teilen weder die Hauptüberraschung noch den Haupt-Baustein aus der Zuordnung in §8. Kein Baustein oder keine Überraschung kommt in drei von vier aufeinanderfolgenden Leveln vor. | `noSameTwistTwiceInARow` |
| H7 | **Deterministisch und fair:** Jeder Versuch läuft identisch ab. Lösungen vertragen etwa 1 Tile und etwa 0,15 s Spielraum. Jede Falle ist nach einem Tod verstanden. Kein Tod ohne sichtbare Ursache. | Bot-Toleranztests (Lösung mit ±0,15 s gewinnt) |
| H8 | **Genau eine Karte pro Runde**, auf der Falle, die am meisten zählt. Bluffs bleiben, wo sie sind. | vorhanden |
| H9 | **Revanche** (wo vorhanden, §7): Die Runde-1-Lösung gewinnt Runde 2 nie. Runde 2 ist ein neues Rätsel im selben Raum. | vorhanden |
| H10 | **Still stehen ist 2 s lang sicher**, außer in ausgewiesenen Idle-Leveln. | vorhanden |
| H11 | **Name, Gag und Story bleiben.** Wenn die Mechanik wechselt, schreibst du den Gag passend neu, auf Englisch und Deutsch, in Mephis Ton. Der Name hat höchstens 26 Zeichen. | vorhanden |

## 3. Weiche Regeln (Handwerk)

- **Lehren, dann brechen:** Die erste Überraschung lehrt eine Regel, zum Beispiel: Der Schalter öffnet die Tür. Die zweite nutzt sie aus: Der Schalter öffnet die Tür, aber er schließt den Rückweg. Die dritte dreht sie um.
- **Man sieht die Tür sofort, der Weg ist nicht offensichtlich.** Der Spieler soll beim Betreten denken: „Wie komme ich da hin?“, und nicht: „Lauf nach rechts.“
- **Der kürzeste Weg ist der Köder.** Mindestens die Hälfte der Level hat einen offensichtlichen Weg, der falsch ist.
- **Überraschung in der Mitte, nicht nur am Ende:** Die Schlussfalle direkt vor der Tür ist erlaubt, aber nicht in jedem Level. In höchstens einem Drittel der Level eines Aktes fällt die stärkste Falle in den letzten 3 Tiles vor der Tür.
- **Verschnaufpausen:** Pro Akt dürfen bis zu 2 kurze Gag-Level die Mindestzeit unterschreiten. Pflicht ist dann eine einzige, starke Pointe, wie bei Level Devil. Sie stehen in §8 mit ★.
- **Lesbarkeit:** Schalter, Portale, Tore und Türen sind am Raumstart sichtbar. Versteckt sind nur die Fallen, nicht das Rätsel.
- **Mephi kommentiert das Rätsel**, nicht nur den Tod: zum Beispiel ein Spruch, wenn man einen Schalter drückt, und einer, wenn man in die Sackgasse läuft.

## 4. Rätsel-Bausteine (R)

| Code | Baustein | Engine | Beispiel |
|---|---|---|---|
| R1 | **Schalter öffnet Tür:** Die Tür ist verschlossen, der Schalter liegt woanders, oft auf einer anderen Ebene. | `Pad` + `Circuit`/`Toggle`, verschlossene Tür als Block vor der Tür | Schalter oben links, Tür unten rechts |
| R2 | **Halteschalter:** Offen bleibt es nur, solange man oder etwas auf dem Schalter steht. Man braucht einen Trick, Timing oder einen zweiten Weg. | `Pad(mode = HOLD)` | Draufstehen öffnet die Wand. Loslaufen, und sie schließt nach 1 s. |
| R3 | **Portal-Routing:** mehrere Portale, nur eine Route führt zur Tür. Die naheliegende führt in die Sackgasse. | `Portal` (Einbahn oder zwei Richtungen) | Drei Portale, „Routing-Tabelle“ als Gag |
| R4 | **Umschalter:** Ein Schalter ändert Portalziel, Bandrichtung, Laser oder Lüfter. Der Spieler stellt die Route selbst ein. | `Pad` + `Reroute`/`Belt`/`Power`/`FanSet` | Pad links schickt das Portal nach oben, Pad rechts nach unten |
| R5 | **Etagen und Rückweg:** Der Weg führt hoch, zurück nach links, durch Sackgassen. Der Raum hat zwei oder drei Ebenen. | Map-Layout | Treppe hoch, über die Decke zurück, Loch nach unten |
| R6 | **Wandernde Tür:** Die Tür wechselt einmal oder mehrmals den Ort. Die neue Route muss man finden. | `DoorTo` | Die Tür springt auf die obere Ebene, der Weg dahin führt zurück |
| R7 | **Köder:** falsche Tür, falscher Schalter, offensichtlicher Weg als Falle. | `FakeWin`, `Bluff`, Deko-Tür | Zwei Türen, die nähere ist ein Mimic |
| R8 | **Taktung plus Weg:** Tore, Blinken oder Takte, kombiniert mit einer Routenwahl. Man wartet nicht nur, man wählt, wo man wartet. | `Blink`/`Clock`/Laser | Insel zwischen zwei Toren, eine Insel ist eine Falle |
| R9 | **Raumumbau:** Wände und Plattformen verschieben sich. Dadurch entsteht ein Weg, oder einer verschwindet. | `Move` | Die Wand fährt beiseite, sobald man umdreht |
| R10 | **Transport:** Lift, Band oder Lüfter als Fahrstuhl zu einer anderen Ebene. | `Move` (vertikal), `Belt`, Lüfter | Band trägt dich zum Schalter |
| R11 | **Hitze- und Strommanagement** (W3): kühlen, Last verteilen, Strom umleiten. | `Heat`/`Heatsink`/`Circuit` | Erst Kühlkörper aktivieren, dann über die Platte |
| R12 | **Geheimweg:** versteckter Block per Kopfstoß, unsichtbare Brücke. Nur in Kombination mit einem sichtbaren Hinweis. | Hidden Solid, `Touch` | Riss in der Decke als Hinweis |

## 5. Überraschungsfamilien (U)

| Code | Familie | Typische Umsetzung |
|---|---|---|
| U1 | Boden weg | Fall, Crumble, Sinking, ein ganzer Bodenstreifen rutscht seitlich weg (`Move`) |
| U2 | Decke fällt | Block, Platte, Stalaktiten, Decke senkt sich langsam |
| U3 | Wand oder Raum schiebt sich | Wand fährt heran, Raum schließt sich, Plattform wird weggezogen |
| U4 | Tür flieht | `DoorTo`, die Tür versteckt sich, springt hoch, läuft zum Start |
| U5 | Stacheln wachsen beim Landen | `Show`, höchstens nach H5 |
| U6 | Stacheln aus Wand oder Decke | seitlich oder von oben, oder Stacheln fahren heran (`Move`) |
| U7 | Säge | rollend, Pendel, Pfad, von vorne oder hinten |
| U8 | Verfolger | `Chase`, Wand mit Zähnen, Ghost |
| U9 | Steuerung | `Swap` |
| U10 | Welt kippt | Schwerkraft, Bild dreht oder rollt |
| U11 | Route manipuliert | `Reroute`, Portal geht aus, Ziel wechselt |
| U12 | Transport dreht um | Band, Lüfter, Lift fährt in die falsche Richtung |
| U13 | Laser oder Firewall | Tor, Scan-Strahl, Strahl wärmt sich auf |
| U14 | Fake | Bluff, falsche Tür, Fake-Sieg, Abspann |
| U15 | Hilfe wird Falle | Der Schalter, den man braucht, löst die Falle aus. Die rettende Plattform ist die Falle. |
| U16 | Meta | Pause, Ghost, Undo, Shake, Tilt, Roll, Frame-Crack. Höchstens die vorhandenen Quoten in Akt 3 |
| U17 | Hitze oder Strom (W3) | Überhitzen, Kurzschluss, Bit-Flip, Lüfter aus |

## 6. Kurve und Mindestdauer

| Abschnitt | Mindestdauer sauberer Lauf | Überraschungen | Bausteine pro Level |
|---|---|---|---|
| W1 Level 1–6 (Tutorial) | keine | 1–3 | 0–1 (R5/R6 dürfen schon auftauchen) |
| W1 Akt 1 ab Level 7 | 6 s | 2–3 | 1 |
| W1 Akt 2 und 3 | 8 s | 2–4 | 1–2 |
| W2 alle Akte | 8 s (Akt 1), 10 s (Akt 2 und 3) | 2–4 | 1–2 |
| W3 alle Akte | 10 s | 3–4 | 2 |
| Akt-Finale (16, 32, 48) | 15 s | 4 | 2–3, kombiniert alles aus dem Akt |
| ★ Verschnaufpause (max. 2 pro Akt) | keine | 1–2, aber eine starke Pointe | 0–1 |

Eine neue Mechanik wird im ersten Level allein und sicher eingeführt. Im zweiten Level wird sie gebrochen, im dritten mit etwas Altem kombiniert.

## 7. Karten und Revanche

- **Karten-Verteilung:** In einem Akt wird keine Karte öfter als 3-mal gespielt (Ausnahme: GRAND_FINALE im Finale). Die Karte passt zur Falle, auf der sie liegt.
- **Revanche-Level** bleiben die bisherigen 47. Jede Revanche-Runde bekommt einen neuen Baustein oder eine neue Überraschungsfamilie gegenüber Runde 1. Zum Beispiel wandert der Schalter, der alte Weg wird zur Sackgasse oder das Portal zeigt woandershin.
- Mindestens 50 % der Revanchen spielen eine andere Karte als Runde 1. Die Regel gilt weiter.

## 8. Zuordnung aller Level

Die Zuordnung ist eine Vorgabe für Abwechslung. Die konkrete Umsetzung entscheidet der Bauende. Ein Tausch innerhalb eines Blocks ist erlaubt, solange H6 erfüllt bleibt. Name und Gag bleiben. Wo die bisherige Idee schon gut passt, wird sie ausgebaut statt ersetzt.

Spalten: Haupt-Baustein (R) · Hauptüberraschung (U) · ★ = Verschnaufpause. Bei mehreren Codes steht der Haupt-Code vorn. Die Meta-Familie U16 zählt je Variante (Pause, Ghost, Undo, Shake, Tilt, Roll, Frame-Crack) als eigene Überraschung, weil die Tricks außer dem Etikett nichts gemeinsam haben. Dieselbe Variante zweimal hintereinander verstößt gegen H6.

Korrekturen nach dem ersten Lauf der Leitplanken-Tests: W2-1 ist jetzt ★ (vorher ohne Baustein, verstieß gegen H2). W2-42 führt mit R5 statt R7 (vorher derselbe Haupt-Baustein wie W2-41). W2-45 nutzt R2 statt R9 (vorher derselbe Haupt-Baustein wie W2-44).

### Welt 1: Höllenkeller (Keller, Burg; Thema: „Mephis Hausregeln“)

**Akt 1 „Die Karten“**

| # | Name | R | U | Notiz |
|---|---|---|---|---|
|1|Warm-up|–|U1|Tutorial, bleibt|
|2|The Hallway|–|U5|Tutorial, bleibt|
|3|Stairwell|R5|U4|bleibt (schon gut)|
|4|House Rules|–|U2|Tutorial|
|5|Obstacle Course|R12|U15|bleibt, Hinweis-Tipp schon da|
|6|Cozy|–|U7|Tutorial|
|7|Prefab|R10|U1|Plattformen als Lift hoch zu zweiter Ebene|
|8|Down to Earth|R5|U10|Decke als zweite Ebene, Rückweg über Kopf|
|9|Potholes|R1|U9|Schalter hinter den Löchern öffnet Tür; Swap auf dem Rückweg|
|10|Homeward|R6|U3|bleibt Köder-Tür, dazu Wand schiebt Rückweg zu|
|11|The Creek|R8|U1|Steine im Takt, Weg nach oben zur Tür|
|12|Return Trip ★|–|U9|gespiegelt, eine starke Pointe|
|13|Wednesday|R7|U14|zwei Wege, der sichere ist der Bluff|
|14|Performance Review|R10|U12|Lift fährt erst richtig, dann falsch|
|15|Loop|R6|U6|Tür wandert durch den Raum, Stacheln aus der Wand|
|16|Number 16 (Finale)|R1+R5+R6|U7+U4|Schalter oben, Tür flieht, Säge|

**Akt 2 „Neue Regeln“ (Blink, PathSaw, Idle)**

| # | Name | R | U |
|---|---|---|---|
|17|Night Shift|R8|U1|
|18|On the Hour|R5|U7|
|19|Waiting Room|R2|U2|
|20|Disco Night|R8|U6|
|21|Foundation|R9|U1|
|22|Airlock|R1|U13 (Blinkwand als Tor)|
|23|Carpentry|R5|U7|
|24|Merge Conflict|R9|U3|
|25|Rush Hour|R6|U15|
|26|Skyscraper|R5|U8|
|27|42 ★|–|U1 (Bit-Flip-Pointe)|
|28|Gym Class|R1|U7|
|29|Hike|R10|U3|
|30|Arcade|R9|U2|
|31|Meadow|R7|U7|
|32|Beta Test (Finale)|R1+R5+R8|U1+U7+U4|

**Akt 3 „Mephi schummelt“ (Meta)**

| # | Name | R | U |
|---|---|---|---|
|33|Clear Road|R7|U14|
|34|Monday Morning|R1|U16 (Pause)|
|35|Home Network ★|–|U4|
|36|Gallery|R5|U16 (Frame-Crack)|
|37|git push --force|R6|U1|
|38|Clear View|R5|U10|
|39|Hardware Store|R10|U16 (Tilt)|
|40|Boot Sequence|R1|U9|
|41|TV Night|R12|U2|
|42|Tailwind|R5|U8|
|43|git blame|R2|U16 (Ghost)|
|44|Wallflower|R9|U16 (Shake)|
|45|sudo rm -rf /|R5|U1|
|46|Home Stretch|R1|U15|
|47|sudo make me a sandwich|R12|U3|
|48|Exit (Finale)|R1+R5+R7|U14+U4|

### Welt 2: Höllen-Rechenzentrum (Thema: Routing, Firewalls, Ports)

Für Welt 2 gilt zusätzlich: Mindestens 12 Level nutzen Schalter (R1, R2 oder R4). Mindestens 8 Level haben Portal-Routing mit echter Routenwahl (R3 oder R4). Bisher waren es 0 und etwa 2.

**Akt 1 „Handshake“** (11–16: Pilot läuft)

| # | Name | R | U |
|---|---|---|---|
|1|Hello, World! ★|–|U1|
|2|Open Port|R3|U5|
|3|Reception|R6|U4|
|4|String Lights|R8|U6|
|5|Null Pointer|R12|U1|
|6|Address Book|R4|U11|
|7|Sky Blue|R5|U2|
|8|Memory Test ★|–|U1|
|9|Cable Mess|R1|U9|
|10|VPN Tunnel|R3|U7|
|11–16|Pilot|siehe Pilot-Bericht||

**Akt 2 „Traffic“** (17–24: Pilot läuft)

| # | Name | R | U |
|---|---|---|---|
|17–24|Pilot|siehe Pilot-Bericht||
|25|Load Balancer|R4 (Pad stellt Bänder um)|U1|
|26|Ticket Number|R2|U8|
|27|DDoS|R10|U2|
|28|Split Tunnel|R3|U13|
|29|Race Condition|R1|U7|
|30|Hop Limit|R3+R5|U11|
|31|Detention|R8|U3|
|32|Core Switch (Finale)|R4+R3+R8|U12+U13+U4|

**Akt 3 „Root“**

| # | Name | R | U |
|---|---|---|---|
|33|sudo !!|R7|U14 (Bluff bleibt)|
|34|Reverse Proxy|R3+R5|U10|
|35|Pipeline|R4|U12|
|36|Access Log|R5|U16 (Ghost)|
|37|Two-Factor Auth|R1 (zwei Schalter)|U15|
|38|Bobby Tables|R3|U1|
|39|Contingency Plan|R1|U16 (Pause)|
|40|Ping Pong ★|–|U16 (Roll)|
|41|Security Audit|R7|U1|
|42|Gold Mine|R5+R7|U15|
|43|Workshop|R4|U16 (Shake)|
|44|Rebase|R9|U16 (Undo)|
|45|Playground|R2 (Wippe als Halteschalter)|U3|
|46|Privilege Escalation|R10+R1|U12|
|47|Math Problem|R5|U9+U10|
|48|shutdown -h now (Finale)|R4+R3+R6|U11+U9+U4|

### Welt 3: Platine (Thema: Strom, Hitze, Lüfter)

Für Welt 3 gilt zusätzlich: Die Zehnerblöcke einer Mechanik werden aufgebrochen. Jeder Akt behält seine Leitmechanik (Akt 1 Strom, Akt 2 Hitze, Akt 3 Lüfter). Spätestens jedes dritte Level bringt eine Überraschung aus einer anderen Familie (U1–U4, U6–U8, U12, U14). `HeatSpike` als Schlussfalle gibt es höchstens in 25 % der Level. Die Leitmechanik eines Aktes (R11 in Akt 2, R10 in Akt 3) ist von der „3 von 4“-Regel in H6 ausgenommen, nicht aber von „zweimal hintereinander“.

**Akt 1 „Stromkreise“**

| # | Name | R | U |
|---|---|---|---|
|1|First Copper|R1|U17|
|2|Live Wire|R2|U6|
|3|Clock Cycle|R8|U1|
|4|Solid Copper|R4|U7|
|5|Turnstile|R2|U2|
|6|Two Buttons|R1 (zwei Schalter, einer Falle)|U15|
|7|Loose Contact|R8+R5|U2|
|8|Memory ★|–|U17 (Bit-Flip)|
|9|Side Effect|R4|U3|
|10|Metronome|R8|U4|
|11|Loose Cables|R5|U17|
|12|The Button|R7|U14|
|13|Fuse Box|R5+R1|U1|
|14|Power Supply|R2|U6|
|15|Connector|R1|U9|
|16|Motherboard (Finale)|R1+R5+R8|U17+U4+U1|

**Akt 2 „Überhitzung“**

| # | Name | R | U |
|---|---|---|---|
|17|Hot Plate|R11|U17|
|18|Full Load|R1+R11|U7|
|19|Melt Fuse|R11|U1|
|20|Cold Start|R6|U4|
|21|Relay Race|R11+R5|U8|
|22|Warm-up|R4|U3|
|23|Waiting Room|R8|U2|
|24|Cooling Fins|R5|U17|
|25|Cooling Tower|R10|U12|
|26|Hot Wire|R1|U13|
|27|Wall Socket ★|–|U17|
|28|Break Time|R11|U7|
|29|Thermostat|R2|U15|
|30|Burn-in Test|R7|U14|
|31|Pit Stop|R11+R5|U3|
|32|Thermal Runaway (Finale)|R11+R1+R6|U17+U4+U2|

**Akt 3 „Lüfter“**

| # | Name | R | U |
|---|---|---|---|
|33|Updraft|R10|U1|
|34|Tailwind|R5|U12|
|35|Headwind|R4 (Pad stellt Lüfter)|U7|
|36|Air Cushion|R5|U6|
|37|Lull|R8|U3|
|38|Silence|R1|U15|
|39|Downdraft|R10|U2|
|40|Air Castle|R4|U12|
|41|Air Bridge|R5|U14|
|42|Exhaust|R10|U8|
|43|Wiring Diagram|R1+R10|U17|
|44|Display ★|–|U10|
|45|Cold Air|R11|U3|
|46|POST|R5+R1|U16|
|47|Boot Order|R7+R6|U14+U4|
|48|BIOS Setup (Finale)|R4+R10+R11|U12+U10+U4|

## 9. Qualitätstests (Leitplanken)

Diese Tests kommen pro Welt neu dazu, in `World{n}DesignTest.kt`. Sie laufen für alle Level ab W1-7, die schon umgebaut sind. Die Liste der umgebauten Level wächst mit jedem Block, damit der Umbau schrittweise grün bleibt.

1. `cleanRunLastsLongEnough`: Die Bot-Lösung gewinnt und braucht mindestens die Mindestdauer aus §6 (★ ausgenommen).
2. `holdRightWithHopsNeverWins`: Rechts halten mit Hüpfern in den Rhythmen 0,4 / 0,7 / 1,0 s gewinnt nie.
3. `solutionToleratesSlop`: Die Bot-Lösung mit allen Haltezeiten ±0,15 s gewinnt in mindestens einer von zwei Varianten. Das sichert, dass keine pixelgenauen Eingaben nötig sind.
4. `spikePopupQuota`: H5.
5. `noSameTwistTwiceInARow`: H6. Die Zuordnung aus §8 liegt als Tabelle im Test (`DESIGN = mapOf(9 to d("R1", "U9"))`), damit Abweichungen bewusst passieren.
6. `cardsSpreadPerAct`: §7.
7. Weiter gültig: die vorhandenen Tests (eine Karte pro Runde, Runde-1-Lösung verliert Runde 2, 2 s Stillstehen sicher, `TrapInvisibilityTest`, `LayoutTest`).

So prüfen die Tests im Detail:

- **H6** vergleicht bei Nachbarn nur die Haupt-Codes (den ersten Code einer Zeile). „3 von 4“ zählt alle Codes einer Zeile. Akt-Finale zählen bei beiden Regeln nicht mit, weil sie den Akt absichtlich kombinieren. In W1 beginnt die Prüfung bei Level 7. Die Leitmechanik (W3: R11 in Akt 2, R10 in Akt 3) ist nur von „3 von 4“ ausgenommen.
- **Tabelle:** Jedes Level ab W1-7 ohne ★ hat einen Baustein (H2). Ein Finale hat mindestens 2 Bausteine und 2 Überraschungen. Pro Akt gibt es höchstens 2 ★. W2 braucht mindestens 12 Schalter-Level (R1, R2, R4) und 8 Routing-Level (R3, R4). In W3 kommt spätestens jedes dritte Level eine Überraschung aus U1–U4, U6–U8, U12 oder U14.
- **H5:** Eine Spike-Popup-Falle ist ein `Show`, das eine Gruppe mit `Glyph(spike = true, hidden = true)` sichtbar macht. Pro Akt dürfen höchstens 4 der umgebauten Level eine haben. In W3 gilt dasselbe für `HeatSpike` als Schlussfalle (letzte Falle der Liste oder Auslöser in den letzten 3 Tiles vor der Tür).
- **H7 (Spielraum):** Die Lösung läuft zweimal: einmal „spät“ (jede Haltezeit +0,15 s, jedes `rightTo`/`leftTo`-Ziel 1 Tile weiter) und einmal „früh“ (−0,15 s, 1 Tile kürzer). Eine der beiden muss gewinnen. Warten auf eine Bedingung (`waitFor`, `rightUntil`, `fidgetUntil`) bleibt unverändert.
- **§7:** Gezählt werden alle `Play`-Karten aller Runden der umgebauten Level eines Aktes. Bluffs zählen nicht, GRAND_FINALE im Finale auch nicht.

### Leitplanken-Tests: so trägst du ein Level ein

Die Tests liegen in `app/src/test/java/com/robinrehbein/beveldevil/game/`: die Regeln in `DesignRules.kt`, die Testfälle in `DesignTestBase.kt` und pro Welt `World{n}DesignTest.kt` mit der Tabelle aus §8. Solange ein Level nicht in `REBUILT` steht, prüfen die Tests nur die Tabelle. Für ein umgebautes Level gehst du so vor:

1. **Zuordnung prüfen:** `DESIGN` enthält die Zeile aus §8, zum Beispiel `12 to d("R1+R5", "U7")`, ★ als `d("–", "U9", breather = true)` und Meta als `d("R5", "U16:Ghost")`. Wenn du innerhalb deines Blocks tauschst, änderst du die Zeile hier und in §8. Der Test `noSameTwistTwiceInARow` sagt dir, ob der Tausch H6 einhält. Pilot-Level (W2 11–24) tragen ihre Zeile selbst ein und entfernen die Nummer aus `PILOT`.
2. **Bot-Lösung eintragen:** in `SOLUTIONS`, eine Lösung pro Runde. Zuerst kommt Runde 1, dann jede Revanche. Nimm `rightTo`, `hopR`, `waitFor` und Wände als Anschlag statt vieler kurzer Haltezeiten, sonst scheitert der Spielraum-Test.
3. **Level freischalten:** die Nummer in `REBUILT` eintragen.
4. **Lauf:** `./gradlew --offline testDebugUnitTest --tests '*DesignTest*' --tests '*DesignRulesTest*'`. Jede Meldung nennt Level, Runde und den Wert, zum Beispiel `clean run 3.26 s, needs 8 s`.

```kotlin
// World2DesignTest.kt, companion object
val REBUILT: Set<Int> = setOf(25, 26)

val SOLUTIONS: Map<Int, List<Solution>> = mapOf(
    25 to listOf(
        { rightTo(6f).hopR(9.5f).leftTo(3f).waitFor { !it.beams[0].lit }.hopR(14f).rightTo(29f) },  // Runde 1
        { right(3f).rightJump(0.35f).landRight().leftTo(4f).rightTo(29f) },                         // Revanche
    ),
    26 to listOf({ hopR(5f).rightUntil { it.player.box.cx > 20f }.right(2f) }),
)
```

Die Level-Tests spielen dieselbe Lösung, damit sie nur an einer Stelle steht: `@Test fun level25() = World2DesignTest.play(25)` und im Revanche-Test `World2DesignTest.play(25, round = 2)`. Ein Beispielraum, der alle Regeln erfüllt, steht in `DesignDemos.puzzle` (Schalter oben links, Tür hinter einer Kupferwand, Boden fällt nach dem Schalter).

## 10. Ablauf

| Phase | Was | Wer | Ergebnis |
|---|---|---|---|
| 0 | **Pilot** W2 11–24 | 2 Agenten (Opus) | läuft |
| 1 | Pilot auswerten, Plan nachschärfen, Leitplanken-Tests (§9) bauen | Opus | Plan v2, Tests |
| 2 | **Gründer spielt den Pilot an** (interner Track) | Gründer | Go oder Korrektur |
| 3 | **Rollout in Blöcken zu je 8 Leveln**, parallel und dateigetrennt (je Part-Datei ein Agent): W2 Rest → W1 7–48 → W3 | Sonnet | Commits pro Block |
| 4 | **Review pro Welt:** unabhängiger, kritischer Agent spielt alle Level per Bot und Screenshot und prüft §2/§3. Schwache Level gehen zurück in Phase 3. | Opus | Review-Bericht |
| 5 | Zusammenführen, volle Testsuite, ein PR pro Welt, Screenshots im PR | Opus | PR |
| 6 | Playtest Gründer und Kollegen pro Welt, dann Merge und Release | Team | Release |

**Pro Block bekommt der Sonnet-Agent:** dieses Dokument, den Pilot-Bericht als Beispiel, seine 8 Level aus §8 und die Leitplanken-Tests. Er liefert: umgebaute Level, Bot-Lösungen, zwei oder mehr Tod-Tests pro Level, aktualisierte Revanche-Tests, Screenshots jedes Raums und einen Bericht (Rätsel, Überraschungen, Dauer, Karte) auf Deutsch.

**Risiken:**
- **Schwierigkeit steigt zu stark.** Abhilfe: Hint pro Runde (vorhanden), Tutorial-Level bleiben kurz, Verschnaufpausen.
- **Bot-Lösungen werden lang und fragil.** Abhilfe: `rightTo`/`waitFor` statt Zeiten, Toleranztest.
- **Merge-Konflikte.** Abhilfe: ein Agent pro Part-Datei, Engine-Änderungen nur durch Opus.
- **Screenshots und Store-Bilder veralten.** Abhilfe: Store-Screenshots nach Phase 5 neu erzeugen.
