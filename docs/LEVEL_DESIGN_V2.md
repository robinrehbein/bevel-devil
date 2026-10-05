# Level-Design V2: Rätselräume statt Ein-Fallen-Level

Verbindliche Vorgabe für den Umbau aller drei Welten. Jeder Agent, der Level baut oder prüft, liest dieses Dokument zuerst.

**Stand: Rezept v2** (nach dem Pilot W2 11–24 und drei Reviews; vom Gründer freigegeben). Kurz gesagt: **Dichte statt Dauer.** Ein Raum ist kurz (6–9 s) und voller echter Fallen, nicht lang durch Warten und Rückwege. Was sich gegenüber v1 geändert hat, steht in §1a.

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

## 1a. Was der Pilot gezeigt hat (Rezept v2)

Der Pilot (W2 11–24) hat die Räume abwechslungsreicher gemacht, aber drei Reviewer (Fach, Level Devil, frische Augen) fanden dieselben Lücken:

- **Die Dichte ist um etwa den Faktor 3 gefallen.** Saubere Läufe von 8,5–14,6 s, aber echte Fallen nur etwa alle 4–5 s. Level Devil hat etwa einen Auslöser pro Sekunde und Läufe von 3–6 s. Die Länge kam aus Warten auf Takte und aus Rückwegen, nicht aus Verstehen.
- **Neue Monotonie statt der alten:** Die Tür flieht zum Start (13, 15, 16, 21, 24), Blink-Warten viermal in Folge (13–16), getaktete Laser-Tore (18, 20, 24), Pads in 7 von 14 Räumen.
- **Zahnlose Fallen:** Fallen, die Warten *und* Durchrennen überleben (12, 18r2, 20, 14r2).
- **Unsichtbares als Pflicht:** unsichtbare Pflicht-Blöcke (16, 22), eine unsichtbare Taktänderung (20).
- **Füller-Tode:** Wer nur rechts hält, stirbt in den ersten Sekunden an sichtbaren Stacheln oder Strahlen (15, 16, 19, 20, 21, 23).
- **Schwache Revanchen:** leichter (11r2), kürzer (11r2, 14r2) oder nur verschoben (18r2, 20r2).
- **Der Hinweis nach 9 s** verriet bei Läufen von 8–15 s jedes Rätsel im ersten Versuch.

Rezept v2 antwortet darauf mit den Regeln H3, H8, H9, H12–H18 in §2, dem Hinweis nach Toden (§7) und der Zuordnung „Rätselraum oder Fallenraum“ in §8. Die Leitplanken-Tests (§9) prüfen das meiste automatisch. Es gibt keine Ausnahmeliste: Ein umgebautes Level erfüllt jede Regel, oder sein Test ist rot (§9).

## 2. Harte Regeln

Diese Regeln gelten für jedes Level ab W1-7. Wo eine Regel prüfbar ist, wird sie per Test durchgesetzt (§9).

| # | Regel | Test |
|---|---|---|
| H1 | **Ein Bildschirm** (32×18), kein Scrollen. Einzige Ausnahme: U18, siehe §5a. | vorhanden (Grid), `RoomsTest` |
| H2 | **Nie nur rechts:** Rechts halten, gerade oder mit Hüpfern alle 0,4 / 0,7 / 1,0 s, gewinnt nie. Auch nicht, wenn man vorher 8 s still steht. | `holdRightWithHopsNeverWins` |
| H3 | **Dichte statt Dauer:** Im sauberen Bot-Lauf vergehen nie mehr als **3 s ohne echte Falle** (Definition §6a; Deko zählt nicht, siehe „Deko“ in §6a): vom Start bis zur ersten, zwischen zwei Fallen, von der letzten bis zur Tür. **Stillstehen höchstens 40 %** des Laufs und **höchstens 1,5 s am Stück.** Die Untergrenze aus §6 ist nur noch eine Plausibilitätsprüfung. | `cleanRunIsDense` |
| H4 | **2–4 echte Fallen pro Runde.** Sie bauen aufeinander auf: Jede bestraft die Lösung, die die vorige nahegelegt hat. Zustandswechsel („Pad öffnet“, „Band dreht dir zuliebe“) und reine Sprüche zählen nicht. | über H3 |
| H5 | **Höchstens eine** `Show`-Stachelfalle pro Runde. Höchstens 25 % der Level eines Aktes haben überhaupt eine. Für W3 gilt dasselbe für `HeatSpike` als Schlussfalle. | `spikePopupQuota` |
| H6 | **Abwechslung:** Zwei aufeinanderfolgende Level (Akt-Finale ausgenommen) teilen weder die Hauptüberraschung noch den Haupt-Baustein aus der Zuordnung in §8. Kein Baustein oder keine Überraschung kommt in drei von vier aufeinanderfolgenden Leveln vor. | `noSameTwistTwiceInARow` |
| H7 | **Deterministisch und fair:** Jeder Versuch läuft identisch ab. Lösungen vertragen etwa 1 Tile und etwa 0,15 s Spielraum. Jede Falle ist nach einem Tod verstanden. Kein Tod ohne sichtbare Ursache. | `solutionToleratesSlop` |
| H8 | **Genau eine Karte pro Runde, und sie passt zur Falle**, auf der sie liegt (Karten-Lint, §7): `GHOST_BLOCK` auf versteckten oder Kopfstoß-Blöcken, `SHY_DOOR` auf `DoorTo`, `STALKER` auf `Chase`, `DEVIL_SAW` auf Sägen, `HEADBUTT` auf fallenden Decken. Bluffs bleiben, wo sie sind. | `cardsFitTheirTraps`, vorhanden |
| H9 | **Revanche gegen Runde 1** (wo vorhanden, §7): Die Revanche arbeitet gegen das Verhalten aus Runde 1 (Vorbilder: 14r2 Konzept, 20r2 Bluff), braucht eine andere Route, ist mindestens so lang wie Runde 1 und nie leichter. Die Runde-1-Lösung gewinnt sie nie. | `rematchWorksAgainstRoundOne`, vorhanden |
| H10 | **Still stehen ist 2 s lang sicher**, außer in ausgewiesenen Idle-Leveln. | vorhanden |
| H11 | **Name, Gag und Story bleiben.** Wenn die Mechanik wechselt, schreibst du den Gag passend neu, auf Englisch und Deutsch, in Mephis Ton. Der Name hat höchstens 26 Zeichen. | vorhanden |
| H12 | **Rotation pro Akt** (16 Level), gezählt aus den Aktionen im Level-Code, nicht aus der Tabelle: Tür flieht (`DoorTo`) **höchstens 1-mal**, Pads und Schalter (`Pad`) **3-mal** (das Akt-Finale darf einen Schalter obendrauf bringen, es kombiniert den Akt absichtlich), Laser-Tore **3-mal**, Blink-Warten (`Blink`) **3-mal**, Schwerkraft-Flip **1-mal**, vertauschte Steuerung (`Swap`) **2-mal**. Ein **Laser-Tor** ist jeder Strahl, dessen Leuchtfenster höchstens 2 s lang ist, egal wodurch er an- und ausgeht: jeder Laser, der mit einer Auszeit unter 10 s taktet; jeder Laser, der höchstens 2 s am Stück leuchtet und taktet (auch ein Start-Laser mit Auszeit ≥ 10 s) oder den eine Falle zündet, egal womit sie auslöst (`Landed`, `PastX`, `Zone`, aber auch `Pressed`, `After`, `Idle` …; ein einmaliger Blitz ist ein Tor); jeder Laser, den Fallen an- **und** ausschalten (`Power` an oder ein `Laser` aus einer Falle, dazu `Power` aus), außer beide hängen am selben Auslöser und der Strahl bleibt länger als 2 s an (an- und ausschalten auf verschiedenen Auslösern zählt immer: dann bestimmt der Spieler das Fenster); ein `Power(an)` aus einer Falle auf einen Laser mit kurzem Fenster; und eine Strom-Leiterbahn (Großbuchstabe) auf einem `Clock` mit Auszeit unter 10 s oder höchstens 2 s an. Ein `Clock` auf einer Kupferschiene (Kleinbuchstabe) ist kein Tor. Die Tabelle in §8 hält dieselben Grenzen für R6/U4, R1/R2/R4 und U9 ein. | `mechanicsRotatePerAct`, Tabelle |
| H13 | **Höchstens 2 Effekt-Familien pro Raum** (Level-Devil-Median: 2). Ein Akt-Finale darf eine dritte kombinieren, ein U18-Level hat 2 pro Raum. Die Familien stehen in §6a. | `atMostTwoEffectFamiliesPerRoom` |
| H14 | **Nichts Unsichtbares ist Pflicht:** kein unsichtbarer Pflicht-Block, keine unsichtbare Pflicht-Stufe. Getarnt ist erlaubt: ein sichtbares Hinweis-Glyph (Riss, Glitch-Pixel) höchstens 1 Tile daneben. Keine unsichtbare Regeländerung (zum Beispiel ein Tor, das still seinen Takt ändert). Was fällt, verschwindet oder sich bewegt, hat ein lesbares Vorzeichen (Riss, Wackeln, Flackern). | Review, `TrapInvisibilityTest` |
| H15 | **Zähne:** Für jede Falle dürfen „stehen bleiben und abwarten“ und „einfach durchlaufen“ nicht beide gewinnen, **und was sie schlägt, muss die Falle selbst sein:** Stirbt die Probe an etwas anderem (Stacheln, die sowieso da sind, ein späterer Takt), hat die Falle keine Zähne. Gemessen per Ablation: Dieselbe Probe ohne die tödlichen Aktionen der Falle muss anders enden. Eine Falle, die Warten oder Rennen mühelos besiegt, ist Deko. | `everyTrapHasTeeth` |
| H16 | **Rückweg nur mit neuer Regel** (Vorbild 2-13: kopfüber zurück). Denselben Weg durch dieselben Hindernisse zurücklaufen ist verboten. | Review |
| H17 | **Kein Füller-Tod in den ersten 2 s:** Wer vom Spawn aus nur rechts hält, stirbt in den ersten 2 s nicht, es sei denn, eine echte Falle hat ihn **getötet**. Sichtbare Stacheln oder ein Strahl direkt vor dem Spawn sind kein Gag. Eine Falle, die irgendwo anders auslöst (ein `Toggle`, `Swap`, `Reroute` oder `Fall` weit weg, Deko), entschuldigt den Tod nicht: Ohne die echten Aktionen der Fallen, die vorher ausgelöst haben, muss der Lauf anders enden (überleben, oder anderswo oder zu anderer Zeit sterben). **Ein Pad-Druck (Auslöser `Pressed`) zählt nicht als ausgelöste Falle:** Ein Pad am Start entschuldigt keinen Tod beim Rechtshalten. | `noFillerDeathAtTheStart` |
| H18 | **Rätsel in etwa der Hälfte:** Pro Akt ist etwa die Hälfte der Level ein **Rätselraum** (ein Baustein R1–R12), die andere Hälfte ein dichter **Fallenraum** im Level-Devil-Stil (in §8: R = „–“). | Tabelle |
| H19 | **Keine Zeile doppelt im Akt (Say-Lint):** Kein Spruch (`Say`), keine Intro und kein Hinweis steht wörtlich in zwei Leveln desselben Aktes, weder auf Englisch noch auf Deutsch (Groß-/Kleinschreibung, Satzzeichen und Leerzeichen zählen nicht: „Nope.“ und „nope!“ sind dieselbe Zeile). Dieselbe Zeile zweimal im selben Level (Revanche) ist erlaubt. Jedes Level hat seine eigenen Pointen. Nur als Warnung (gedruckt, nie rot): drei oder mehr gemeinsame Wörter hintereinander in zwei Leveln desselben Aktes, sehr häufige Wörter (Artikel, „you“, „du“ …) nicht mitgezählt. | `noRepeatedLinesInAnAct` |
| H20 | **Nachbarn spielen sich anders (Raum-Abwechslung):** H6 liest nur die Tabelle. Zusätzlich gilt eine **binäre Regel**: Zwei aufeinanderfolgende Level eines Aktes (das zweite kein Finale) müssen sich in der **dominanten Effektfamilie** unterscheiden, und zwar **jede Runde gegen jede Runde** (auch die Revanche gegen den Nachbarn). Dominant ist die Familie mit den meisten tödlichen Momenten im sauberen Lauf der Runde; ein Moment zählt nur für die Familien seiner tödlichen Aktionen, Deko (§6a) zählt nicht. **Gleichstand:** Alle gleichauf liegenden Familien sind dominant, und jede davon darf der Nachbar nicht haben (ein billiger früher Moment nimmt der eigentlichen Familie nichts weg). `Move` wird nach Bewegung und Objekt getrennt: eine Gruppe, die an der Decke hängt, ist `ceiling-move`, wenn sie flach ist oder senkrecht fährt; was seitwärts fährt (dx ≠ 0) und mindestens 2 Tiles hoch ist, ist `wall-move` (auch eine flache breite Wand); sonst nach unten (dy > 0) `drop`, senkrecht mit mehr freien Seiten als Oberseiten `wall-move`, sonst `floor-move`. `Chase` ist immer `wall-move`. Ein `Fall` einer Gruppe, die an der Decke hängt, ist `ceiling-move`. Die frühere Prozentzahl zur Ähnlichkeit der Lösungsform bleibt nur ein gedruckter Bericht und lässt nie einen Test fehlschlagen. | `neighboursPlayDifferently` |
| H21 | **Bewegte Wände sind gedeckelt:** Pro Akt haben **höchstens 3 Level** `wall-move` als dominante Familie (Zählung, kein Anteil), in irgendeiner Runde, auch im Gleichstand. | `atMostThreeMovingWallLevelsPerAct` |

## 3. Weiche Regeln (Handwerk)

- **Lehren, dann brechen:** Die erste Überraschung lehrt eine Regel, zum Beispiel: Der Schalter öffnet die Tür. Die zweite nutzt sie aus: Der Schalter öffnet die Tür, aber er schließt den Rückweg. Die dritte dreht sie um.
- **Man sieht die Tür sofort, der Weg ist nicht offensichtlich.** Der Spieler soll beim Betreten denken: „Wie komme ich da hin?“, und nicht: „Lauf nach rechts.“
- **Der kürzeste Weg ist der Köder.** Mindestens die Hälfte der Level hat einen offensichtlichen Weg, der falsch ist.
- **Überraschung in der Mitte, nicht nur am Ende:** Die Schlussfalle direkt vor der Tür ist erlaubt, aber nicht in jedem Level. In höchstens einem Drittel der Level eines Aktes fällt die stärkste Falle in den letzten 3 Tiles vor der Tür.
- **Verschnaufpausen:** Pro Akt gibt es bis zu 2 kurze Gag-Level (3–5 s, keine Untergrenze). Pflicht ist eine einzige, starke Pointe, wie bei Level Devil. Sie stehen in §8 mit ★. Die Dichte (H3) gilt auch für sie.
- **Lesbarkeit:** Schalter, Portale, Tore und Türen sind am Raumstart sichtbar. Versteckt sind nur die Fallen, nicht das Rätsel, und auch die nur mit Vorzeichen (H14).
- **Fallenraum:** Ein Raum ohne Rätsel-Baustein lebt von 3–4 echten Fallen aus höchstens zwei Familien, die aufeinander aufbauen. Kein Schalter, kein Warten, kein Rückweg: laufen, reagieren, lachen, nochmal.
- **Mephi kommentiert das Rätsel**, nicht nur den Tod: zum Beispiel ein Spruch, wenn man einen Schalter drückt, und einer, wenn man in die Sackgasse läuft.

## 3a. Musterlevel und Anti-Muster

- **Musterlevel:** 2-13 „Works on My Machine“, 2-14 „127.0.0.1“ und 2-15 „Greeting“, jeweils in der Fassung nach dem Pilot-Fix. 2-13 zeigt den Rückweg mit neuer Regel (kopfüber zurück) und den sofortigen Lacher (Schwerkraft nach 0,4 s). 2-14 zeigt Portal-Routing mit echter Routenwahl: eine Spirale aus Einbahn-Links, der naheliegende Link (Loopback) schickt dich heim, dazu eine Revanche gegen das Verhalten aus Runde 1. 2-15 zeigt ein Schalter-Rätsel mit Reihenfolge (zwei Pads, erst die Brücke, dann die Treppe zur Tür) und eine Falle, die die Hilfe ist (die Brücke bröckelt, wer stehen bleibt).
- **Anti-Muster:** lineare Korridore mit getakteten Toren, wie die alten 2-23 „Information Superhighway“ und 2-24 „Uplink“. Gerade Spuren ohne Routenwahl und Takt-Strahlen hoch und wieder runter sind kein Rätselraum und auch kein Fallenraum, sondern Warten.

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
| U18 | Raum geht weiter | Die Tür am Ende ist nicht das Ende: Die Wand bricht auf, dahinter liegt ein zweiter (oder dritter) Raum. Selten, siehe §5a |

## 5a. Ausnahme: Mephi erweitert den Raum (U18 „Raum geht weiter“)

H1 bleibt die harte Regel: **Die Kamera zeigt immer genau einen ganzen Raum (32×18).** Es gibt kein freies Scrollen. Die einzige Ausnahme ist selbst eine Überraschung von Mephi: Der Spieler erreicht, was wie das Ende aussieht (meist die Tür am rechten Rand). Dann bricht die Wand dahinter auf, die Tür rutscht durch das Loch in den nächsten Raum, und Mephi lacht: „Wer sagt, dass der Raum hier aufhört?“

**Regeln**

- **Höchstens etwa 10 Level im ganzen Spiel.** Zugeteilt in §8: W1 16, 32, 33, 48 · W2 30, 48 · W3 16, 32, 47, 48. Mehr nur nach Absprache, sonst nutzt sich der Gag ab.
- **Ein Raum, ein Bild:** Ein Level hat 2 oder 3 Räume nebeneinander. Die Kamera folgt dem Spieler raumweise. Sobald seine Mitte eine Viertelkachel im Nachbarraum steht, schwenkt sie in 0,5 s hinüber. **Währenddessen steht das Spiel still** (wie beim Revanche-Freeze: Zeit, Sägen, Takte, Laser). Jeder Raum ist also ganz zu sehen, bevor man darin handelt. Zurücklaufen funktioniert genauso.
- **Tod und Revanche:** Nach einem Tod beginnt der Versuch im Raum des Spawns, die Kamera steht wieder dort. Revanche-Runden starten ebenfalls im Spawn-Raum. Checkpoint-Verhalten unverändert.
- **Fairness am Eingang:** Die ersten 3 Kacheln hinter dem Durchbruch sind sicher (keine Stacheln, keine Säge, kein Loch). Der Spieler steht nach dem Schwenk dort und muss erst schauen dürfen.
- **Die Wand ist ehrlich:** Bis die Falle feuert, sieht die Trennwand genau aus wie der Rahmen eines normalen Levels (`TrapInvisibilityTest`). Während sie bricht (0,6 s), ist die Tür gesperrt.
- **Karte:** `Card.ANNEX` („Anbau“ / „Annex“, selten, „Das Ende war eine tragende Lüge.“). Pro Level höchstens einmal, auf dem Durchbruch. Solange kein ausgeliefertes Level sie spielt, ist sie in `everyLevelPlaysACard` als ausstehend markiert; mit dem ersten U18-Level die Ausnahme dort entfernen.
- Kombinationen: Im zweiten Raum gilt alles wie gewohnt (Sägen, Portale, Bänder, Laser, Lüfter, Hitze arbeiten über die ganze Breite). Ein Portal darf auch in einen anderen Raum führen, die Kamera folgt mit Schwenk. Nicht kombinieren mit `FakeEnd.CREDITS`.

**So baust du es (DSL)**

```kotlin
Level(
    T("Annex", "Anbau"), T("Almost done. Surely.", "Fast geschafft. Sicher."),
    rooms = 2,                                   // 2 Räume = 64 Spalten
    traps = listOf(
        // Tür erreicht: Karte, Wand zwischen Raum 0 und 1 bricht (Zeilen 12..14), Tür rutscht nach Raum 1, Spalte 28
        trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14)),
        // Trigger und Sägen nehmen globale x-Werte: roomX(raum, lokal) = 32 × raum + lokal
        trap(PastX(roomX(1, 6f)), Fall('a')),
    ),
) {
    border(); floor()                            // ohne room(): jeder Raum bekommt seinen eigenen Rahmen
    room(0) { spawn(); door(); fill(14..15, 13..14) }   // in room(i) gelten lokale Koordinaten 0..31
    room(1) { pit(12..15); fill(9..11, 15..15, 'a') }
}
```

- `Extend(into, top = 12, bottom = 14, warn = 0.6f, door = null, line = …)`: bricht die Doppelwand zwischen Raum `into - 1` und `into` in den Zeilen `top..bottom` auf (nur `#`-Kacheln). `line` ist Mephis Spruch (Standard: „Wer sagt, dass der Raum hier aufhört?“), `door` das neue Ziel der Tür.
- Drei Räume: zwei `AtDoor`-Fallen hintereinander, die zweite mit `Extend(into = 2, door = roomX(2, 29) to 14)`. Die erste unverbrauchte `AtDoor`-Falle feuert zuerst.
- Beispiele mit Bot-Lösungen: `RoomDemos.kt` und `RoomsTest.kt` (nur Tests, nicht im Spiel).

## 6. Kurve, Dichte und Untergrenze

| Abschnitt | Sauberer Lauf (Ziel) | Untergrenze (Plausibilität) | Echte Fallen pro Runde | Bausteine pro Level |
|---|---|---|---|---|
| W1 Level 1–6 (Tutorial) | 3–6 s | 4 s | 1–3 | 0–1 (R5/R6 dürfen schon auftauchen) |
| Alle anderen Level (W1 ab 7, W2, W3) | 6–9 s | 6 s | 2–4 | Rätselraum 1–2, Fallenraum 0 |
| Akt-Finale (16, 32, 48) | 10–12 s, nicht 15 | 10 s | 4 | 2–3, kombiniert das Beste aus dem Akt |
| ★ Verschnaufpause (max. 2 pro Akt) | 3–5 s | keine | 1–2, aber eine starke Pointe | 0–1 |

Für alle gilt H3: nie mehr als 3 s ohne echte Falle, Stillstehen höchstens 40 % des Laufs und höchstens 1,5 s am Stück. Gemessen wird der saubere Bot-Lauf. Gewinnt die Lösung auch ohne ihre Warte-Befehle, zählt dieser schnellere Lauf: Warten, das der Raum nicht verlangt, streckt nichts.

Eine neue Mechanik wird im ersten Level allein und sicher eingeführt. Im zweiten Level wird sie gebrochen, im dritten mit etwas Altem kombiniert.

## 6a. Was eine echte Falle ist, und welche Familien es gibt

Eine Falle (`trap(…)`) zählt nach ihrer schwersten Aktion (auch in `FakeWin(…, then)`). Fallen mit demselben Auslöser, die im selben Moment auslösen, sind **ein** Moment (eine Tür, die über `doorTrail` in mehreren Sprüngen flieht, zählt einmal).

| Gewicht | Aktionen |
|---|---|
| **tödlich** (echte Falle) | `Fall`, `Move`, `Chase`, `Saw`, `PathSaw`, `Blink` und `Clock` (der Boden fängt an zu blinken), `Laser` (neuer Strahl, auch einmalig), `HeatSpike`, `Ghost`, `FrameCrack`; `Show` einer Stachelgruppe; `Hide` einer festen Gruppe mit begehbarer Kachel auf oder unter Fußhöhe, höchstens 6 Tiles vom Spieler (der Boden verschwindet; ein Käfig oder eine Wand, die aufgeht, zählt nicht); `Power(an)` eines Lasers oder einer Strom-Leiterbahn, `Power(aus)` einer Kupferschiene; `Circuit`, das eine Leiterbahn einschaltet oder eine Schiene aus; `Belt`, das vom Ziel weg schiebt |
| **routenändernd** (echte Falle) | `DoorTo`, `Gravity`, `Swap` (auch zurück), `Reroute`, `Power` eines Portals oder Lüfters, `FanSet`, `Toggle`, `BitFlip`, `FakeWin`, `Extend`, `Undo`, `Tilt`, `Slope` |
| **keine Falle** | `Say`, `Play`, `Bluff`, `Shake`, `Pad`, `Portal`, `Heat`, `Heatsink`, `Fan`, `Flip`, `Roll`, `PauseTrap`; `Show` einer festen Gruppe (Brücke oder Block erscheint: der Schalter hat getan, was er soll); `Hide` von Stacheln; `Power(aus)` eines Lasers, `Power(an)` einer Schiene, `Power` eines Bands; ein `Belt`, das zur Tür hin schiebt |

**Deko (Ablation):** Eine Falle ist nur dann echt, wenn sie etwas ändert. Für jede Falle, die etwas wiegt und in einem Probelauf auslöst, spielt der Test den Raum ohne sie (Fallen mit demselben Auslöser zusammen, sie sind ein Moment): den sauberen Lauf, den Lauf ohne Warte-Befehle, Rechtshalten, Rechtshalten mit Hüpfern alle 0,7 s, 8 s Warten und dann Rechtshalten, und ab ihrem Auslöser die beiden Zähne-Proben (stehen bleiben, durchlaufen). Endet keiner dieser Läufe anders (Zustand, Zeit, Ort), ist die Falle **Deko**: Sie zählt nicht für die Dichte (H3), nicht für die Zähne (H15), nicht als Falle, die einen Tod am Start entschuldigt (H17), und nicht für die dominante Familie (H20, H21). Deko ist erlaubt (ein Spruch, ein Effekt), sie füllt nur keine Lücke.

**Effekt-Familien (H13)**, aus den Aktionen einer Runde (Start eingeschlossen) und ihrer Karte. Sprüche, Karten und Pads sind kein Effekt (ein Pad ist nur der Schalter für einen Effekt). Eine Kupferschiene, die am Start steht und nur von Pads geschaltet wird, ist ein Schloss (R1) und auch kein Effekt.

| Familie | Aktionen |
|---|---|
| Boden/Decke fällt | `Fall`, `Hide` einer festen Gruppe |
| Stacheln | `Show` von Stacheln |
| Bewegung | `Move`, `Chase`, `Tilt`, `Slope` |
| Tür | `DoorTo`, `FakeWin`, `Extend` |
| Säge | `Saw`, `PathSaw` |
| Steuerung | `Swap` |
| Schwerkraft | `Gravity`, `Flip` |
| Portal | `Portal`, `Reroute`, `Power` eines Portals |
| Band | `Belt`, `Power` eines Bands |
| Laser | `Laser`, `Power` eines Lasers |
| Takt | `Blink` |
| Hitze | `Heat`, `Heatsink`, `HeatSpike` |
| Strom | `Clock`, `Toggle`, `BitFlip`, `Power` einer Schiene oder Leiterbahn, eine Leiterbahn, `Circuit` aus einer Falle |
| Lüfter | `Fan`, `FanSet`, `Power` eines Lüfters |
| Meta | `Undo`, `Ghost`, `PauseTrap`, `FrameCrack`, `Roll` |
| Geheimblock | versteckter fester oder Kopfstoß-Block in der Karte, `Show` einer festen Gruppe |

## 7. Karten, Revanche und Hinweis

- **Karten-Verteilung:** In einem Akt wird keine Karte öfter als 3-mal gespielt (Ausnahme: GRAND_FINALE im Finale).
- **Karten-Lint (H8):** Die Karte passt zu der Aktion, auf der sie liegt (in derselben `trap(…)`):

| Karte | liegt auf |
|---|---|
| COLLAPSE | `Fall`, `Move`, `Hide` einer festen Gruppe |
| CRUMBLE | `Fall`, `Blink`, `Hide` einer festen Gruppe |
| SINKING | `Fall`, `Move`, `Hide` einer festen Gruppe |
| SPIKE_SEED | `Show` von Stacheln, `Laser` (ein Strahl „wächst“) |
| HEADBUTT | `Fall`, `Move` (die Decke kommt) |
| SHY_DOOR | `DoorTo` |
| DECOY | `FakeWin`, `DoorTo`, `Reroute` |
| UPSIDE_DOWN | `Gravity(true)`, `Flip` |
| TWISTED | `Swap(true)` |
| DEVIL_SAW | `Saw`, `PathSaw` |
| GHOST_BLOCK | `Show`, `Hide`, `Fall` oder `Move` eines versteckten festen oder Kopfstoß-Blocks, oder eine Falle, die das Berühren eines solchen Blocks auslöst |
| STALKER | `Chase` |
| UNDO | `Undo` |
| ANNEX | `Extend` |
| SHORT_CIRCUIT | `Power`, `Circuit`, `Clock`, `Toggle`, `BitFlip` |
| BIT_FLIP | `BitFlip`, `Toggle`, `Swap` |
| OVERCLOCKED | `HeatSpike`, `Heat` |
| THROTTLE | `Heat`, `Fan`, `FanSet`, `Laser` |
| BACKDRAFT | `Fan`, `FanSet`, `Belt`, `Power` eines Lüfters |
| BIOS | `BitFlip`, `Toggle`, `Power`, `FakeWin`, `PauseTrap` |
| GRAND_FINALE | alles |

- **Revanche-Level** bleiben die bisherigen 47. Jede Revanche-Runde arbeitet **gegen das Verhalten aus Runde 1** (H9): Was in Runde 1 richtig war, ist jetzt die Falle. Vorbilder sind 14r2 (die Links sind getauscht, und wer wie in Runde 1 auf die Brücke wartet, trifft die Firewall) und 20r2 (der Scanner auf dem Sims ist ein Bluff, der Sims geht weiter, und wer wie in Runde 1 an derselben Stelle hinunterfällt, landet in den LEDs). Eine verschobene Tür oder ein verlegter Schalter (so war 18r2 früher) oder ein Schalter, den man suchen muss (11r2), reicht nicht. Die Revanche braucht eine andere Route, ist mindestens so lang wie Runde 1 und nie leichter.
- Mindestens 50 % der Revanchen spielen eine andere Karte als Runde 1. Die Regel gilt weiter.
- **Hinweis nach Toden:** Mephis Tipp (`hint`) kommt nach **2 Toden in der aktuellen Runde**, beim Respawn, einmal pro Runde (Neustart aus dem Pausenmenü zählt als Tod). Er kommt nicht mehr nach 9 s am Leben: Bei Läufen von 6–9 s hätte er das Rätsel im ersten Versuch verraten.

## 8. Zuordnung aller Level

Die Zuordnung ist eine Vorgabe für Abwechslung. Die konkrete Umsetzung entscheidet der Bauende. Ein Tausch innerhalb eines Blocks ist erlaubt, solange H6 erfüllt bleibt. Name und Gag bleiben. Wo die bisherige Idee schon gut passt, wird sie ausgebaut statt ersetzt.

Spalten: Haupt-Baustein (R) · Hauptüberraschung (U) · Raum: **Rätsel** (Rätselraum mit Baustein), **Falle** (Fallenraum im Level-Devil-Stil, R = „–“), ★ = Verschnaufpause, Finale, Tutorial. Bei mehreren Codes steht der Haupt-Code vorn. Die Meta-Familie U16 zählt je Variante (Pause, Ghost, Undo, Shake, Tilt, Roll, Frame-Crack) als eigene Überraschung, weil die Tricks außer dem Etikett nichts gemeinsam haben. Dieselbe Variante zweimal hintereinander verstößt gegen H6.

Korrekturen nach dem ersten Lauf der Leitplanken-Tests: W2-1 ist jetzt ★ (vorher ohne Baustein, verstieß gegen H2). W2-42 führt mit R5 statt R7 (vorher derselbe Haupt-Baustein wie W2-41). W2-45 nutzt R2 statt R9 (vorher derselbe Haupt-Baustein wie W2-44).

Korrekturen für Rezept v2:
- **Rätsel- und Fallenräume (H18):** In jedem Akt ist etwa die Hälfte der Level ein Fallenraum (R = „–“). Ausgewählt wurden Level, deren Idee ohnehin eine Falle ist (Takt-Steine, Lift fährt falsch, Meta-Tricks), und Level, die sonst die Rotation sprengen würden.
- **Rotation (H12) in der Tabelle:** pro Akt höchstens eine Zeile mit fliehender Tür (R6 oder U4), höchstens drei mit Schalter (R1, R2, R4), höchstens zwei mit vertauschter Steuerung (U9). Deshalb verlieren die Finales mit Raum-Erweiterung (U18) die Tür-Flucht U4 (der Durchbruch ist ihre Tür-Pointe), W2-32 ebenso (in W2 Akt 2 hat kein Level eine Tür-Flucht, das eine `DoorTo` des Aktes ist frei). Schalter wurden in W1 Akt 3, W2 Akt 2/3 und W3 gestrichen, wo zu viele waren.
- **Pilot-Zeilen (W2 11–24)** stehen jetzt in der Tabelle. Sie teilen sich die Rotation mit dem Rest ihres Aktes, siehe die Hinweise bei Welt 2.
- **Pilot nach der Härtung der Leitplanken** (Deko per Ablation, Zähne und Füller-Tod mit Ursache, H20 über alle Runden und Gleichstände): Sechs Pilot-Level wurden minimal angepasst. 2-12: Die Lücke zwischen Startboden und erster Seite ist zu (Rechtshalten fiel dort hinein, die Seite „entschuldigte“ den Tod). 2-14 Runde 1: Statt des Steins fällt ein Paket von der Decke darüber auf den Stein (vorher teilten 14 und 15 die Familie `drop`). 2-15: Das Kühlrack senkt sich 0,15 s später, sodass es wirklich auf Durchrennende fällt (vorher blockierte es nur). 2-11 Runde 2: Lüfter 1 startet bei der Landung auf dem Regal, sodass Wegrennen in ihn läuft. 2-21: Der Rest des Regals hinter dem Laufband ist ein langsameres Band, das Stehenbleibende zurück aufs Laufband gibt (das Umleiten von Portal b war Deko). 2-23: Die Geisterfahrer-Spur beginnt zwei Kacheln früher, direkt an der LED, und dreht erst, wenn du auf ihr bist: Wer stehen bleibt, landet in der LED.

### Welt 1: Höllenkeller (Keller, Burg; Thema: „Mephis Hausregeln“)

**Akt 1 „Die Karten“**

| # | Name | R | U | Raum | Notiz |
|---|---|---|---|---|---|
|1|Warm-up|–|U1|Tutorial|Tutorial, bleibt|
|2|The Hallway|–|U5|Tutorial|Tutorial, bleibt|
|3|Stairwell|R5|U4|Tutorial|bleibt (schon gut)|
|4|House Rules|–|U2|Tutorial|Tutorial|
|5|Obstacle Course|R12|U15|Tutorial|bleibt, Hinweis-Tipp schon da|
|6|Cozy|–|U7|Tutorial|Tutorial|
|7|Prefab|–|U1|Falle|Fertigteil-Plattformen sacken unter dir weg, eine nach der anderen|
|8|Down to Earth|R5|U10|Rätsel|Decke als zweite Ebene, Rückweg über Kopf|
|9|Potholes|R1|U9|Rätsel|Schalter unten rechts öffnet die Kupferwand vor der Tür; Schlagloch im Deck, die Tasten tauschen nach der Landung dahinter und am Schalter wieder zurück (Swap auf dem Hinweg: mit Swap auf dem Rückweg gewänne Rechtshalten)|
|10|Homeward|–|U3|Falle|Betonwand im Nacken (Chase), Treppe, Stachelbett oben; die Deko-Tür entfällt (die Karte kennt nur eine Tür)|
|11|The Creek|–|U1|Falle|Steine sinken weg, sobald du drauf landest|
|12|Return Trip ★|–|U9|★|gespiegelt, eine starke Pointe|
|13|Wednesday|R7|U14|Rätsel|der Bluff sind die Stacheln in der Decke (Wackeln), die glatten Deckenteile sind echt; kein `Bluff`/`FakeWin` (Budget 0, Akt 1 ohne Meta)|
|14|Performance Review|–|U12|Falle|Lift fährt erst richtig, dann falsch|
|15|Loop|R6|U6|Rätsel|der eine `DoorTo` des Akts: Tür wandert durch den Raum, Stacheln aus der Wand|
|16|Number 16 (Finale)|R1+R5|U7+U18|Finale|Schalter oben, Säge, die Wand bricht auf (U18 statt Tür-Flucht)|

**Akt 2 „Neue Regeln“ (Blink, PathSaw, Idle)**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|17|Night Shift|–|U1|Falle|
|18|On the Hour|–|U7|Falle|
|19|Waiting Room|R2|U2|Rätsel|
|20|Disco Night|–|U6|Falle|
|21|Foundation|R9|U1|Rätsel|
|22|Airlock|R1|U13 (Laser-Tor, pulsierend; statt Blinkwand, weil Blink im Block schon in 20 steckt)|Rätsel|
|23|Carpentry|–|U7|Falle|
|24|Merge Conflict|R9|U3|Rätsel|
|25|Rush Hour|R6|U15|Rätsel|
|26|Skyscraper|–|U8|Falle|
|27|42 ★|–|U1 (Bit-Flip-Pointe)|★|
|28|Gym Class|–|U7|Falle|
|29|Hike|R10|U3|Rätsel|
|30|Arcade|–|U2|Falle|
|31|Meadow|R7|U7|Rätsel|
|32|Beta Test (Finale)|R1+R5+R8|U1+U7+U18|Finale|

**Akt 3 „Mephi schummelt“ (Meta)**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|33|Clear Road|R7|U14+U18|Rätsel|
|34|Monday Morning|–|U16 (Pause)|Falle|
|35|Home Network ★|–|U4|★|
|36|Gallery|–|U16 (Frame-Crack)|Falle|
|37|git push --force|–|U1|Falle|
|38|Clear View|R5|U10|Rätsel|
|39|Hardware Store|R10|U16 (Tilt)|Rätsel|
|40|Boot Sequence|–|U9|Falle|
|41|TV Night|R12|U2|Rätsel|
|42|Tailwind|–|U8|Falle|
|43|git blame|R2|U16 (Ghost)|Rätsel|
|44|Wallflower|–|U16 (Shake)|Falle|
|45|sudo rm -rf /|–|U1|Falle|
|46|Home Stretch|R1|U15|Rätsel|
|47|sudo make me a sandwich|R12|U3|Rätsel|
|48|Exit (Finale)|R1+R5+R7|U14+U18|Finale|

### Welt 2: Höllen-Rechenzentrum (Thema: Routing, Firewalls, Ports)

Für Welt 2 gilt zusätzlich: Mindestens 8 Level haben Portal-Routing mit echter Routenwahl (R3 oder R4). Schalter (R1, R2, R4) gibt es wegen der Rotation (H12) höchstens 3 pro Akt, den Pilot eingerechnet.

**Akt 1 „Handshake“** (11–16: Pilot, nach Rezept v2 neu gebaut; 11 und 14 haben eine Revanche. Rotation im Akt: Die Tür-Flucht hat schon 3, ein Schalter steht in 6. Der Pilot bekommt also keinen `DoorTo`, höchstens 2 Pad-Level, höchstens 3 Blink-Level, etwa 3 Rätsel- und 3 Fallenräume.)

| # | Name | R | U | Raum |
|---|---|---|---|---|
|1|Hello, World! ★|–|U1|★|
|2|Open Port|R3|U5|Rätsel|
|3|Reception|R6|U4|Rätsel|
|4|String Lights|–|U6|Falle|
|5|Null Pointer|–|U7|Falle|
|6|Address Book|R4|U11|Rätsel|
|7|Sky Blue|–|U2|Falle|
|8|Memory Test ★|–|U1|★|
|9|Cable Mess|–|U9|Falle|
|10|VPN Tunnel|R3|U1|Rätsel|
|11|Server Room|R1|U15|Rätsel|
|12|Address Space|–|U1|Falle|
|13|Works on My Machine|R5|U10|Rätsel|
|14|127.0.0.1|R3|U11|Rätsel|
|15|Greeting|R1|U3|Rätsel|
|16|Through Traffic (Finale)|R3+R5|U9+U2|Finale|

**Akt 2 „Traffic“** (17–24: nach Rezept v2 neu gebaut, die Zeilen stehen unten. Rotation im Akt: Die eine Tür-Flucht (`DoorTo`, H12) ist noch frei, kein Level von 17–24 nutzt sie (21 „Flat Rate“ ist ein Portal-Level). **Pad-Deckel, Auflösung:** Der Deckel von 3 `Pad`-Leveln pro Akt (H12) gilt auch in Akt 2. Der Pilot darf ihn ausschöpfen (heute 18 und 20; 17, 21 und 23 schalten ohne `Pad`). Das Finale 32 behält seinen Schalter (R4), weil ein Akt-Finale den Akt absichtlich kombiniert und einen Schalter **obendrauf** bringen darf (H12, `Cap.finaleExtra`). Zwischen 25 und 31 steht deshalb kein weiteres Pad-Level, und die Tabelle hält höchstens eine Schalter-Zeile (32) außerhalb des Piloten. Laser-Tore (H12, Definition dort) höchstens 3 im ganzen Akt, jeder kurze Strahl einer Falle zählt mit, egal womit sie auslöst. **Laser-Tor-Budget:** Der Pilot verbraucht zwei (18 und 20). Für 25–32 bleibt damit **genau ein** Level mit Laser-Tor. Heute haben 26, 28 und 32 noch eines (alter Stand): Beim Umbau behält nur eines davon sein Tor. **Pilot 17–24 nach der Prüfung:** Das Muster „Paket fällt, warten, hüpfen“ (`Card.HEADBUTT`) steht in 17 und in 24, in keinem weiteren Level des Aktes. Bewegte Wände (`wall-move`) sind keine dominante Familie eines Pilot-Levels: 22 rollt Sägen, 23 sind Bänder und Portale (R10/U12), 24 hat statt der Stachelwand im Rücken ein Paket von der Decke. Genau zwei Level (18, 20) haben ein Laser-Tor. In 20 zündet nicht der Schalter am Start das Tor, sondern die Landung auf der ersten Stufe, damit Rechtshalten (H17) nicht in den ersten 2 s stirbt.)

| # | Name | R | U | Raum |
|---|---|---|---|---|
|17|Data Bus|R10|U12|Rätsel|
|18|Firewall|R5|U13|Rätsel|
|19|Delivery|–|U9+U3|Falle|
|20|Stateful Inspection|R8|U13|Rätsel|
|21|Flat Rate|R3|U11|Rätsel|
|22|Bouncer|–|U7|Falle|
|23|Information Superhighway|R10|U12|Rätsel|
|24|Uplink|–|U2+U1|Falle|
|25|Load Balancer|–|U1|Falle|
|26|Ticket Number|–|U8|Falle|
|27|DDoS|R10|U2|Rätsel|
|28|Split Tunnel|R3|U13|Rätsel|
|29|Race Condition|–|U7|Falle|
|30|Hop Limit|R3+R5|U11+U18|Rätsel|
|31|Detention|R8|U3|Rätsel|
|32|Core Switch (Finale)|R4+R3+R8|U12+U13|Finale|

**Akt 3 „Root“**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|33|sudo !!|–|U14 (Bluff bleibt)|Falle|
|34|Reverse Proxy|R3+R5|U10|Rätsel|
|35|Pipeline|–|U12|Falle|
|36|Access Log|–|U16 (Ghost)|Falle|
|37|Two-Factor Auth|R1 (zwei Schalter)|U15|Rätsel|
|38|Bobby Tables|R3|U1|Rätsel|
|39|Contingency Plan|–|U16 (Pause)|Falle|
|40|Ping Pong ★|–|U16 (Roll)|★|
|41|Security Audit|–|U1|Falle|
|42|Gold Mine|R5+R7|U15|Rätsel|
|43|Workshop|–|U16 (Shake)|Falle|
|44|Rebase|–|U16 (Undo)|Falle|
|45|Playground|R2 (Wippe als Halteschalter)|U3|Rätsel|
|46|Privilege Escalation|R10|U12|Rätsel|
|47|Math Problem|R5|U9+U10|Rätsel|
|48|shutdown -h now (Finale)|R4+R3|U11+U9+U18|Finale|

### Welt 3: Platine (Thema: Strom, Hitze, Lüfter)

Für Welt 3 gilt zusätzlich: Die Zehnerblöcke einer Mechanik werden aufgebrochen. Jeder Akt behält seine Leitmechanik (Akt 1 Strom, Akt 2 Hitze, Akt 3 Lüfter). Spätestens jedes dritte Level bringt eine Überraschung aus einer anderen Familie (U1–U4, U6–U8, U12, U14). `HeatSpike` als Schlussfalle gibt es höchstens in 25 % der Level. Auch die Schalter-Rotation gilt: höchstens 3 Pad-Level pro Akt, auch im Strom-Akt. Strom schaltet sonst über Takt (`Clock`), Berührung (`Touch` mit `Toggle`) oder `BitFlip`. Die Leitmechanik eines Aktes (R11 in Akt 2, R10 in Akt 3) ist von der „3 von 4“-Regel in H6 ausgenommen, nicht aber von „zweimal hintereinander“.

**Akt 1 „Stromkreise“**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|1|First Copper|R1|U17|Rätsel|
|2|Live Wire|–|U6|Falle|
|3|Clock Cycle|R8|U1|Rätsel|
|4|Solid Copper|–|U7|Falle|
|5|Turnstile|–|U2|Falle|
|6|Two Buttons|R1 (zwei Schalter, einer Falle)|U15|Rätsel|
|7|Loose Contact|R8+R5|U2|Rätsel|
|8|Memory ★|–|U17 (Bit-Flip)|★|
|9|Side Effect|–|U3|Falle|
|10|Metronome|R8|U4|Rätsel|
|11|Loose Cables|R5|U17|Rätsel|
|12|The Button|R7|U14|Rätsel|
|13|Fuse Box|R5|U1|Rätsel|
|14|Power Supply|–|U6|Falle|
|15|Connector|–|U9|Falle|
|16|Motherboard (Finale)|R1+R5+R8|U17+U1+U18|Finale|

**Akt 2 „Überhitzung“**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|17|Hot Plate|R11|U17|Rätsel|
|18|Full Load|R1+R11|U7|Rätsel|
|19|Melt Fuse|–|U1|Falle|
|20|Cold Start|R6|U4|Rätsel|
|21|Relay Race|R11+R5|U8|Rätsel|
|22|Warm-up|–|U3|Falle|
|23|Waiting Room|–|U2|Falle|
|24|Cooling Fins|–|U17|Falle|
|25|Cooling Tower|R10|U12|Rätsel|
|26|Hot Wire|–|U13|Falle|
|27|Wall Socket ★|–|U17|★|
|28|Break Time|–|U7|Falle|
|29|Thermostat|R2|U15|Rätsel|
|30|Burn-in Test|–|U14|Falle|
|31|Pit Stop|R11+R5|U3|Rätsel|
|32|Thermal Runaway (Finale)|R11+R1|U17+U2+U18|Finale|

**Akt 3 „Lüfter“**

| # | Name | R | U | Raum |
|---|---|---|---|---|
|33|Updraft|R10|U1|Rätsel|
|34|Tailwind|R5|U12|Rätsel|
|35|Headwind|R4 (Pad stellt Lüfter)|U7|Rätsel|
|36|Air Cushion|–|U6|Falle|
|37|Lull|–|U3|Falle|
|38|Silence|–|U15|Falle|
|39|Downdraft|R10|U2|Rätsel|
|40|Air Castle|–|U12|Falle|
|41|Air Bridge|–|U14|Falle|
|42|Exhaust|–|U8|Falle|
|43|Wiring Diagram|R1+R10|U17|Rätsel|
|44|Display ★|–|U10|★|
|45|Cold Air|–|U3|Falle|
|46|POST|R5|U16|Rätsel|
|47|Boot Order|R7+R6|U14+U4+U18|Rätsel|
|48|BIOS Setup (Finale)|R4+R10+R11|U12+U10+U18|Finale|

## 9. Qualitätstests (Leitplanken)

Die Tests liegen in `app/src/test/java/com/robinrehbein/beveldevil/game/`: die Regeln in `DesignRules.kt`, die Testfälle in `DesignTestBase.kt` und pro Welt `World{n}DesignTest.kt` mit der Tabelle aus §8. Die Tabellen-Tests laufen immer für die ganze Tabelle. Alles, was Level spielt, läuft für die Level in `REBUILT`, damit der Umbau Block für Block grün bleibt.

**Für jedes umgebaute Level (assertiert):**

| Test | Regel | Was er misst |
|---|---|---|
| `cleanRunIsDense` | H3 | Bot-Lösung gewinnt. Lücken ohne echte Falle (§6a, Deko per Ablation ausgenommen) höchstens 3 s, vom Start bis zur Tür. Stillstehen ≤ 40 % und ≤ 1,5 s am Stück. Untergrenze aus §6, auch ohne Warte-Befehle (`wait`, `waitUntil`, `waitFor`, `waitWhile`, `fidgetUntil`, `untilSaw`, Sprung auf der Stelle). Gewinnt die Lösung auch ohne sie, wird dieser Lauf gemessen. |
| `holdRightWithHopsNeverWins` | H2 | Rechts halten, gerade oder mit Hüpfern (0,4 / 0,7 / 1,0 s), sofort oder nach 8 s Stillstehen, gewinnt nie. |
| `solutionToleratesSlop` | H7 | Die Lösung mit allen Zeiten, Zielen und Reaktionen 0,15 s und 1 Tile später *oder* früher gewinnt. |
| `everyTrapHasTeeth` | H15 | Für jede tödliche Falle des sauberen Laufs zwei Proben ab dem Moment, in dem sie auslöst: (a) 8 s still stehen, dann die Lösung weiterspielen; (b) die gerade gehaltene Taste (sonst rechts) 2 s weiter halten, ohne zu springen. Die Falle hat Zähne, wenn (a) verliert oder (b) stirbt **und** dieselbe Probe ohne die tödlichen Aktionen der Falle anders endet (`DesignRules.withoutActions`: was die Falle sonst tut, etwa ein Tor öffnen, bleibt). Sonst ist sie Deko. Routenänderungen (Tür, Schwerkraft, Steuerung, Portal) haben ihre Zähne in der neuen Route, die H2 für den ganzen Raum prüft. |
| `noFillerDeathAtTheStart` | H17 | Rechts halten vom Spawn stirbt nicht in den ersten 2 s, außer eine echte Falle hat den Tod verursacht: Ohne die echten Aktionen der vorher ausgelösten Fallen muss derselbe Lauf anders enden. Ein Pad-Druck (`Pressed`) zählt nicht als ausgelöste Falle. |
| `rematchWorksAgainstRoundOne` | H9 | Jede Revanche-Runde: sauberer Lauf ≥ Runde 1, Runde-1-Lösung verliert. |
| `atMostTwoEffectFamiliesPerRoom` | H13 | Familien aus §6a pro Runde ≤ 2 (Finale 3, U18 2 pro Raum). |
| `cardsFitTheirTraps` | H8 | Karten-Lint aus §7. |
| `mechanicsRotatePerAct` | H12 | Rotations-Obergrenzen pro Akt, aus den Aktionen im Code. Laser-Tore: jeder Strahl mit einem Leuchtfenster von höchstens 2 s, egal von welchem Auslöser, dazu Takte mit Auszeit unter 10 s, An/Aus-Paare aus Fallen und getaktete Strom-Leiterbahnen (`DesignRules.laserGates`, Definition in H12); das Finale darf einen Schalter obendrauf bringen. |
| `spikePopupQuota` | H5 | ≤ 1 Spike-Popup pro Runde, ≤ 4 Level pro Akt (W3: auch `HeatSpike`-Schluss). |
| `noRepeatedLinesInAnAct` | H19 | Say-Lint: kein `Say`, keine Intro, kein Hinweis wörtlich in zwei Leveln desselben Aktes, EN und DE getrennt geprüft, ohne Satzzeichen (`DesignRules.sayKey`). Zusätzlich eine gedruckte Warnung (kein Fehler) bei 3 oder mehr gemeinsamen Wörtern in Folge (`DesignRules.sayNgramWarnings`). |
| `neighboursPlayDifferently` | H20 | Zwei Nachbarn eines Aktes (das zweite kein Finale) haben in keinem Rundenpaar eine gemeinsame dominante Effektfamilie (meiste tödliche Momente im sauberen Lauf der Runde, nur tödliche Aktionen, ohne Deko; Gleichstand: alle gleichauf liegenden Familien; `Move` nach Bewegung getrennt in `floor-move`, `wall-move`, `ceiling-move`, `drop`; `Chase` ist `wall-move`). Die Ähnlichkeit der Lösungsform in Prozent wird nur gedruckt. |
| `atMostThreeMovingWallLevelsPerAct` | H21 | Höchstens 3 Level pro Akt mit `wall-move` als dominanter Familie (in irgendeiner Runde, auch im Gleichstand). |
| `rolloutBlocksKeepTheirBudgets` | §11 | Jeder Block, dessen Level alle in `REBUILT` stehen, hält seinen Anteil an den Akt-Deckeln, seine Karten und seine Ränder ein (auch `RolloutBudgetTest`). |
| `rebuiltComesInWholeBlocks` | §11 | Ein Block steht ganz oder gar nicht in `REBUILT`. |
| `theKitIsLocked` | §9 | `DesignRules.kt` und `DesignTestBase.kt` haben die SHA-256-Werte aus `KitLock.kt` („kit is locked; only the orchestrator may change it“, auch `KitLockTest`). |
| `cardsSpreadPerAct` | §7 | Keine Karte öfter als 3-mal pro Akt. |

**Für die Tabelle:** `designTableMatchesTheDoc` (Tabelle im Test = §8), `designTableCoversTheWorldWithItsActStructure` (gültige Codes; Finale mit ≥ 2 Bausteinen und ≥ 2 Überraschungen; ≤ 2 ★ pro Akt; pro Akt ein bis zwei Drittel Rätselräume, H18; Rotation R6/U4 ≤ 1, R1/R2/R4 ≤ 3, U9 ≤ 2 pro Akt, H12), `noSameTwistTwiceInARow` (H6), W2: ≥ 8 Routing-Level, W3: spätestens jedes dritte Level eine andere Familie.

Weiter gültig: die vorhandenen Tests (eine Karte pro Runde, Runde-1-Lösung verliert Runde 2, 2 s Stillstehen sicher, `TrapInvisibilityTest`, `LayoutTest`).

So prüfen die Tests im Detail:

- **H6** vergleicht bei Nachbarn nur die Haupt-Codes (den ersten Code einer Zeile). „3 von 4“ zählt alle Codes einer Zeile. Akt-Finale zählen bei beiden Regeln nicht mit, weil sie den Akt absichtlich kombinieren. In W1 beginnt die Prüfung bei Level 7. Die Leitmechanik (W3: R11 in Akt 2, R10 in Akt 3) ist nur von „3 von 4“ ausgenommen.
- **H19 (Say-Lint):** Gezählt werden alle `Say`-Aktionen aller Runden (auch in `FakeWin`), die Intro jeder Runde und der Hinweis. Verglichen wird getrennt auf Englisch und Deutsch, ohne Groß-/Kleinschreibung, ohne Satzzeichen und mit zusammengefassten Leerzeichen (eine Zeile nur aus Satzzeichen, etwa „…“, zählt als sie selbst). Pro Akt darf jeder Text in höchstens einem Level vorkommen. Die n-Gramm-Warnung (`sayNgramWarnings`) zerlegt jede Zeile in Wörter ohne die häufigen (`COMMON_WORDS`), bildet Dreiergruppen und meldet, welche zwei Level der Akt-Gruppe sich eine teilen. Sie wird nur gedruckt.
- **H20 (Nachbar-Check):** Jede Runde mit Lösung hat eine Signatur aus ihrem sauberen Lauf. Gezählt werden die tödlichen Momente (§6a, Gewicht „tödlich“, ohne Deko); jeder Moment zählt für die Familien seiner **tödlichen** Aktionen (eine Brücke, die im selben Moment erscheint, zählt nicht mit). Die dominanten Familien sind die mit den meisten Momenten, bei Gleichstand **alle** gleichauf liegenden. Ohne tödlichen Moment gibt es keine dominante Familie und nichts zu vergleichen. Die Familien sind die aus §6a, mit Ausnahmen (`DesignRules.familyOf` mit `split`): `Move` wird nach Bewegung und Objekt getrennt (`DesignRules.moveFamily`). Hängt die Gruppe an der Decke (jede Kachel ihrer obersten Reihe hat etwas Festes direkt darüber, `DesignRules.hangs`), ist es `ceiling-move`, wenn sie flach ist oder senkrecht fährt. Fährt sie seitwärts und ist mindestens 2 Tiles hoch, ist es `wall-move`. Sonst: nach unten (dy > 0) `drop`; senkrecht mit mehr freien Seiten als Oberseiten (Säule, Block auf dem Boden) `wall-move`; sonst `floor-move`. `Chase` ist `wall-move`, ein `Fall` einer Gruppe an der Decke `ceiling-move`. H13 zählt weiter mit dem groben `move` und `drop`. Zwei Nachbarn verstoßen, wenn irgendeine Runde des einen und irgendeine Runde des anderen eine dominante Familie teilen. Der Bericht (`adjacentReport`) druckt zusätzlich, wie ähnlich die Lösungsformen sind (`Bot.shape()`: ein Zeichen pro Strecke, `R`, `L` oder `W`, `^` bei Sprung; Strecken unter 0,25 s zählen nicht; längste gemeinsame Teilfolge in Prozent). Der Bericht entscheidet nie.
- **H21 (Wand-Deckel):** Gezählt werden die Level eines Akts (aus den Leveln mit Lösung), bei denen in irgendeiner Runde `wall-move` dominant ist (auch im Gleichstand). Mehr als 3 verstoßen.
- **Kit gesperrt:** Während des Rollouts ändert nur der Orchestrator `DesignRules.kt` und `DesignTestBase.kt`. `theKitIsLocked` vergleicht ihre SHA-256-Werte mit `KitLock.kt` und schlägt sonst fehl („kit is locked; only the orchestrator may change it“). Ein rotes Level wird geändert, nie die Regel. Budgets pro Block: §11.
- **Keine Ausnahmeliste:** Es gibt keine Allowlist, keinen TODO-Mechanismus und keinen „nur Bericht“-Modus für umgebaute Level. Ein Test wird nicht ausgenommen, übersprungen, auf ein anderes Level umgelenkt oder aufgeweicht, damit er grün wird. Findet eine Regel etwas, wird das Level geändert (oder, wenn die Regel falsch ist, die Regel mit einem eigenen Test, nie ein einzelner Eintrag). Ein Level, das noch nicht umgebaut ist, steht nicht in `REBUILT`.
- **H5:** Eine Spike-Popup-Falle ist ein `Show`, das eine Gruppe mit `Glyph(spike = true, hidden = true)` sichtbar macht. In W3 gilt dasselbe für `HeatSpike` als Schlussfalle (letzte Falle der Liste, Auslöser `AtDoor` oder Auslöser höchstens 3 Tiles von der Tür entfernt, egal von welcher Seite, auch an der Stelle, an die `DoorTo` die Tür schiebt). Gezählt wird auch, was in einem `FakeWin(..., then)` steckt.
- **H7 (Spielraum):** Die Lösung läuft zweimal: einmal „spät“ (jede Haltezeit +0,15 s, jedes `rightTo`/`leftTo`-Ziel 1 Tile weiter) und einmal „früh“ (−0,15 s, 1 Tile kürzer). Eine der beiden muss gewinnen. Warten auf eine Bedingung (`waitFor`, `rightUntil`, `waitWhile`, `fidgetUntil`, `untilSaw`) reagiert ebenfalls 0,15 s zu spät oder zu früh. Eine Bedingung auf `player.box.cx` oder `world.time` ist also kein Weg um den Test herum. Nur das Landen (`landRight`) bleibt exakt.
- **H3 (Dichte):** Die Zeitpunkte sind die, an denen die Aktionen einer Falle laufen (also nach ihrem `delay`). Mehrere Fallen mit demselben Auslöser im selben Moment sind ein Moment. Stillstehen heißt: weder links noch rechts gedrückt, im Spiel und nicht in einem Fake-Sieg.
- **§7:** Gezählt werden alle `Play`-Karten (auch in `FakeWin`) aller Runden der umgebauten Level eines Aktes. Bluffs zählen nicht, GRAND_FINALE im Finale auch nicht.

### Leitplanken-Tests: so trägst du ein Level ein

1. **Zuordnung prüfen:** `DESIGN` enthält die Zeile aus §8, zum Beispiel `12 to d("R1+R5", "U7")`, einen Fallenraum als `d("–", "U1")`, ★ als `d("–", "U9", breather = true)` und Meta als `d("R5", "U16:Ghost")`. Wenn du innerhalb deines Blocks tauschst, änderst du die Zeile hier und in §8. Die Tabellen-Tests sagen dir, ob der Tausch H6, H12 und H18 einhält.
2. **Bot-Lösung eintragen:** in `SOLUTIONS`, eine Lösung pro Runde. Zuerst kommt Runde 1, dann jede Revanche. Nimm `rightTo`, `hopR`, `waitFor` und Wände als Anschlag statt vieler kurzer Haltezeiten, sonst scheitert der Spielraum-Test. Polstere die Lösung nicht mit `wait`: Der Dichte-Test misst den Lauf ohne Warte-Befehle, wenn er auch so gewinnt, und Stillstehen zählt gegen die 40 %.
3. **Level freischalten:** die Nummer in `REBUILT` eintragen. Ab dann laufen alle Prüfungen aus der Tabelle oben.
4. **Lauf:** `./gradlew --offline testDebugUnitTest --tests '*DesignTest*' --tests '*DesignRulesTest*'`. Jede Meldung nennt Level, Runde und den Wert, zum Beispiel `no real trap for 4.69 s (t=5.65–10.34, last trap to door; max 3 s)`. Die Zeitleiste der echten Fallen einer Runde liefert `DesignRules.timeline(level, round, solution)`.

```kotlin
// World2DesignTest.kt, companion object
val DESIGN: Map<Int, Design> = mapOf(
    // …
    13 to d("R5", "U10"),   // Rätselraum
    14 to d("–", "U1"),     // Fallenraum
)
val REBUILT: Set<Int> = setOf(13, 14)

val SOLUTIONS: Map<Int, List<Solution>> = mapOf(
    13 to listOf({ rightTo(6.5f).waitFor { !it.group('f').visible }.hopR(9.8f).rightTo(30f) }),
    14 to listOf(
        { hopR(5f).rightTo(12f).hopR(16f).right(2f) },        // Runde 1
        { rightTo(3f).hopL(1.5f).rightTo(20f).right(2f) },    // Revanche
    ),
)
```

Die Level-Tests spielen dieselbe Lösung, damit sie nur an einer Stelle steht: `@Test fun level13() = World2DesignTest.play(13)` und im Revanche-Test `World2DesignTest.play(14, round = 2)`. Ein Beispielraum, der alle Regeln erfüllt, steht in `DesignDemos.puzzle` (Schalter oben links, Tür hinter einer Kupferwand, die Decke kommt, der Boden fällt nach dem Schalter).

## 10. Ablauf

| Phase | Was | Wer | Ergebnis |
|---|---|---|---|
| 0 | **Pilot** W2 11–24 | 2 Agenten (Opus) | fertig, drei Reviews |
| 1 | Pilot auswerten, Plan nachschärfen, Leitplanken-Tests (§9) bauen | Opus | Rezept v2, Leitplanken v2 |
| 1b | **Pilot nach Rezept v2 neu bauen** (die roten Leitplanken-Tests abarbeiten), Level wandern nach `REBUILT` | Opus | Pilot v2 |
| 2 | **Gründer spielt den Pilot an** (interner Track) | Gründer | Go oder Korrektur |
| 3 | **Rollout in Blöcken zu je 8 Leveln**, parallel und dateigetrennt (je Part-Datei ein Agent): W2 Rest → W1 7–48 → W3 | Sonnet | Commits pro Block |
| 4 | **Review pro Welt:** unabhängiger, kritischer Agent spielt alle Level per Bot und Screenshot und prüft §2/§3. Schwache Level gehen zurück in Phase 3. | Opus | Review-Bericht |
| 5 | Zusammenführen, volle Testsuite, ein PR pro Welt, Screenshots im PR | Opus | PR |
| 6 | Playtest Gründer und Kollegen pro Welt, dann Merge und Release | Team | Release |

**Review-Regel (gilt für jedes Review, auch für Engine-Änderungen):** Jedes Ergebnis wird von mindestens drei unabhängigen Reviewern geprüft:
1. **Fachreviewer:** Korrektheit, Fairness, Regeln aus §2/§3, Tests.
2. **Level-Devil-Reviewer:** misst das Ergebnis an Level Devil als Referenz (`docs/LEVEL_DEVIL_REFERENCE.md`). Würde das Level dort bestehen? Überrascht es genauso? Ist es genauso rätselhaft und genauso fair?
3. **Reviewer ohne Referenz:** liest weder den Plan noch Level Devil. Er spielt und bewertet wie ein neuer Spieler mit frischen Augen: Macht es Spaß? Versteht man, was passiert? Will man weiterspielen?

Ein Level gilt erst als fertig, wenn keiner der drei einen Blocker oder ein „langweilig“ meldet.

**Pro Block bekommt der Sonnet-Agent:** dieses Dokument, die Musterlevel 2-13, 2-14 und 2-15 (§3a), seine 8 Level aus §8 und die Leitplanken-Tests. Er liefert: umgebaute Level, Bot-Lösungen, zwei oder mehr Tod-Tests pro Level, aktualisierte Revanche-Tests, Screenshots jedes Raums und einen Bericht (Rätsel- oder Fallenraum, echte Fallen mit Zeitleiste, Dauer, Karte) auf Deutsch.

**Risiken:**
- **Schwierigkeit steigt zu stark.** Abhilfe: Hinweis nach 2 Toden in der Runde (vorhanden), kurze Räume (6–9 s, ein Tod kostet wenig), Tutorial-Level bleiben kurz, Verschnaufpausen.
- **Bot-Lösungen werden lang und fragil.** Abhilfe: `rightTo`/`waitFor` statt Zeiten, Toleranztest.
- **Merge-Konflikte.** Abhilfe: ein Agent pro Part-Datei, Engine-Änderungen nur durch Opus.
- **Screenshots und Store-Bilder veralten.** Abhilfe: Store-Screenshots nach Phase 5 neu erzeugen.

## 11. Rollout-Budgets pro Block

Im Rollout bauen bis zu 15 Agenten parallel, je einer pro Block. Die Deckel aus §2 gelten pro Akt (16 Level), und zwei Blöcke eines Aktes sehen beim Bauen den jeweils anderen nicht. Damit sie nicht beide denselben Deckel aufbrauchen, hat jeder Block hier seinen **Anteil**. Die Zahlen stehen auch im gesperrten Kit (`DesignRules.ROLLOUT`). `RolloutBudgetTest` prüft, dass diese Tabelle und das Kit übereinstimmen und dass die Anteile eines Aktes zusammen mit den Leveln außerhalb der Blöcke (dem unberührten Pilot) in die Deckel passen. Sobald alle Level eines Blocks in `REBUILT` stehen, prüfen `rolloutBlocksKeepTheirBudgets` (in jedem `World{n}DesignTest`) und `RolloutBudgetTest`, dass der Block seinen Anteil einhält. `rebuiltComesInWholeBlocks` verlangt, dass ein Block **ganz** in `REBUILT` steht oder gar nicht: Wer ein Level weglässt, schaltet sonst still dessen Tests und das Budget des Blocks ab.

**Blöcke:** W1: A 7–16 (1–6 Tutorial bleibt), B 17–24, C 25–32, D 33–40, E 41–48. W2: A 1–10 (11–17, 19, 21, 22 Pilot bleibt), B die Revanchen von 18 und 20 (18r2, 20r2), dazu 23 und 24 zusammen mit 25–32, C 33–40, D 41–48. W3: A 1–8, B 9–16, C 17–24, D 25–32, E 33–40, F 41–48.

**So entstehen die Zahlen** (eine Regel für alle, keine Handarbeit):

- **Deckel pro Akt** wie im Kit: Pad 3 (das Finale darf einen Schalter obendrauf bringen, „+F“), Laser-Tor 3, Blink 3, `DoorTo` 1, Schwerkraft-Flip 1, `Swap` 2, `wall-move`-dominant 3 (H21), Stachel-Popup 4 (H5), W3 `HeatSpike`-Schluss 4, jede Karte 3 (§7, GRAND_FINALE im Finale frei).
- **Fest verbraucht** ist, was Level außerhalb der Blöcke schon im Code nutzen und was das Kit zählt (Level in `REBUILT`): in W2 Akt 1 der Pilot 11–16 (Pads 11, 13, 15; Blink 13; Schwerkraft 13; `Swap` 16; `wall-move` 14r2; Karten HEADBUTT 14 und 16, DEVIL_SAW, COLLAPSE, SINKING, UPSIDE_DOWN, STALKER, GHOST_BLOCK je 1), in W2 Akt 2 die Pilot-Level 17, 19, 21, 22 (`Swap` 19; `wall-move` 19; Karten HEADBUTT 17, TWISTED 19, DECOY 21, DEVIL_SAW 22). 18 und 20 gehören zu W2-B, ihre Runde 1 (Pads, Laser-Tore 18 und 20) zählt also im Anteil von W2-B mit. Das Tutorial W1 1–6 zählt nicht (es steht nie in `REBUILT`).
- **Geplante Zeilen aus §8** bekommen ihr Stück zuerst: `DoorTo` für R6/U4, Pad für R1/R2/R4 (ohne Finale), `Swap` für U9, Schwerkraft für U10, `wall-move` für U3/U8, U18 nach §5a. Sind es mehr Zeilen als der Akt hergibt, bekommt die früheste Zeile den Platz, die andere setzt ihre Überraschung ohne diese Mechanik um (siehe Hinweise unten).
- **Der Rest des Aktes** (Deckel minus fest verbraucht minus geplant) wird geteilt: der frühere Block bekommt die abgerundete Hälfte, der spätere den Rest. Laser-Tor, Blink, Stachel-Popup, `HeatSpike`-Schluss und Karten haben keine geplanten Zeilen und werden nur so geteilt (zwei Blöcke: 1 und 2, bei Stachel-Popups 2 und 2).
- **Revanche (genau):** Die 47 Revanche-Level bleiben, wo sie sind. Ein Block hat genau so viele, wie heute im Code stehen. **Bluff:** höchstens so viele wie heute. **U18:** die Level aus §5a.
- **Verboten am Rand:** die dominante Familie (H20) des Nachbarn außerhalb des Blocks, im selben Akt und nicht über ein Finale hinweg. Bei Pilot-Nachbarn ist es die gemessene Familie aus dem Code (alle Runden), sonst die geplante aus §8 (U1 drop, U2 ceiling-move, U3/U8 wall-move, U5/U6 spikes, U7 saw, U12 belt, U13 laser, U17 heat/power; Tür, Steuerung, Schwerkraft, Portal, Fake und Meta ohne tödliche Familie: keine Vorgabe). Das Randlevel darf in keiner Runde von dieser Familie dominiert sein.

**Budgets** (Zahl = höchstens so viele Level des Blocks; Revanche = genau; Karte = höchstens so oft pro Karte, in Klammern die Ausnahmen; Verboten: Randlevel und Familie):

| Block | Level | Pad | Laser-Tor | Blink | DoorTo | Schwerkraft | Swap | wall-move | Stachel-Popup | HeatSpike-Schluss | Revanche | Bluff | U18 | Karte | Verboten am Rand |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
|W1-A|7–16|3 (+F)|3|3|1|1|2|3|4|–|3|0|1|3|7: saw|
|W1-B|17–24|2|1|1|0|0|1|1|2|–|4|1|0|1|–|
|W1-C|25–32|1 (+F)|2|2|1|1|1|2|2|–|1|0|1|2|25: wall-move|
|W1-D|33–40|0|1|1|1|1|1|0|2|–|2|0|1|1|40: ceiling-move|
|W1-E|41–48|3 (+F)|2|2|0|0|1|3|2|–|4|1|1|2|–|
|W2-A|1–10|0|3|2|1|0|1|2|4|–|3|1|0|3 (HEADBUTT 1, DEVIL_SAW 2, COLLAPSE 2, SINKING 2, UPSIDE_DOWN 2, STALKER 2, GHOST_BLOCK 2)|10: saw|
|W2-B|18, 20, 23–32|3 (+F)|3|3|1|1|1|2|4|–|5|1|1|3 (HEADBUTT 2, TWISTED 2, DECOY 2, DEVIL_SAW 2)|18: drop, wall-move; 20: drop, wall-move, belt; 23: saw|
|W2-C|33–40|1|1|1|0|1|0|1|2|–|2|1|0|1|40: drop|
|W2-D|41–48|2 (+F)|2|2|1|0|2|2|2|–|4|0|1|2|–|
|W3-A|1–8|2|1|1|0|0|0|1|2|2|3|0|0|1|8: wall-move|
|W3-B|9–16|1 (+F)|2|2|1|1|2|2|2|2|2|1|1|2|–|
|W3-C|17–24|1|1|1|1|0|1|2|2|2|2|0|0|1|24: belt|
|W3-D|25–32|2 (+F)|2|2|0|1|1|1|2|2|3|0|1|2|25: heat, power|
|W3-E|33–40|1|1|1|0|0|1|1|2|2|3|0|0|1|–|
|W3-F|41–48|2 (+F)|2|2|1|1|1|2|2|2|2|1|2|2|41: belt|

**Was der Block mit seinem Anteil tun muss (Hinweise, wo §8 und der Code nicht zusammenpassen):**

- **W2-A, Pad 0:** Der Pilot hat in Akt 1 schon drei Pad-Level (11, 13, 15; 13 nutzt seine Pads für die Schwerkraft, obwohl §8 dort R5 sagt). Die §8-Zeile W2-6 (R4, Umschalter) wird deshalb **ohne `Pad`** gebaut: der Schalter ist ein `Touch`-Block oder eine Zone mit `Toggle`/`Reroute`.
- **W2-A, Schwerkraft 0:** 13 hat den einen Flip des Aktes. Keine §8-Zeile in 1–10 plant U10.
- **W2-B:** 18 und 20 bringen aus Runde 1 schon zwei Pads und zwei Laser-Tore mit. Für 23–32 bleiben also **ein** Pad-Level (dazu der Schalter des Finales 32) und **ein** Laser-Tor (§8: 26, 28 und 32 haben heute eines, nur eines behält es). HEADBUTT darf in W2-B noch zweimal vorkommen (17 hat es, 24 hat es heute).
- **W2-C/W2-D, Schwerkraft:** §8 plant U10 in 34 und 47. Nur 34 (W2-C) bekommt den Flip; 47 setzt U10 mit `Flip` (Bild dreht sich) oder `Roll` um, ohne `Gravity(true)`.
- **W3-F, Schwerkraft 1:** §8 plant U10 in 44 und 48. Nur eines von beiden nutzt `Gravity(true)`.
- **W3, Laser-Tor:** Ein Takt-Strom (`Clock` auf einer Leiterbahn mit Großbuchstaben) mit kurzem Fenster ist ein Tor (H12). §8 plant R8 in W3-3, 7, 10 und im Finale 16. Mit nur 3 Toren im Akt (W3-A 1, W3-B 2) setzt mindestens eine dieser Zeilen ihren Takt mit einer **Kupferschiene** (Kleinbuchstabe, `Clock` auf einer festen Gruppe: zählt nicht als Tor) oder einem Laser mit langem Fenster um.
- **Karten:** Ein Block mit 1 pro Karte (der frühere Block eines Aktes) spielt jede Karte höchstens einmal. Pro Runde gibt es genau eine Karte (H8), also bei 8 Leveln mit Revanchen 8–12 Karten: dafür reichen die 22 Karten.

### Hinweis W1-B (Level 17-24): Kartenarithmetik

Die Behauptung in §11, 22 Karten reichten für W1 Akt 2, stimmt so nicht: Block B hat 12 Runden (17, 18, 21, 24 mit Revanche), spielbar sind aber nur 10 Karten plus Bluff (höchstens 1 im Block). Jede Karte höchstens einmal im Block heißt also: eine Runde bekommt GRAND_FINALE (hier 24 Runde 2), eine (21 Runde 2) den Bluff `Bluff(SPIKE_SEED)`. Sollte §11 angepasst werden: entweder Karten dürfen sich innerhalb eines Akts nach 8 Leveln wiederholen, oder Revanchen bekommen keine neue Karte.
