# Level Devil als Referenz für Reviewer

Zweck: Wer unsere Level (Mephi the Daemon, siehe `docs/LEVEL_DESIGN_V2.md`) prüft, soll wissen, was Level Devil (Unept / Denda Games) tatsächlich tut. Dieses Dokument beschreibt Beobachtungen, keine Assets und keine Leveldaten.

**Herkunftsmarken** (jede Aussage trägt eine):

- **[Daten]** aus den entpackten Spieldateien der Android-Version 1.4.2 (Defold, Tiled-Level als JSON) gezählt.
- **[Spiel]** aus der Spielsitzung im Browser (Poki-Web-Version, Welt 1, Stage 1 bis 3, 15 Level, 33 Tode, Beginn von Stage 4). Der Spieler war ein Bot mit Screenshot-Auswertung, kein Mensch.
- **[Web]** aus öffentlichen Kurzbeschreibungen. Dünn und ungeprüft.
- **[Annahme]** Schluss von uns. Nicht belegt.

---

## 1. Gesamtstruktur

- **Umfang.** Die Store-Beschreibung nennt rund 200 Level in 3 Welten [Web]. In den Dateien liegen 271 Level-Karten [Daten]. Darin stecken auch Bonus-, Endlos-, Editor-Vorlagen und Sonderkarten. Welche Karten zur Hauptfolge gehören, lässt sich aus den Daten nicht sauber ablesen [Annahme].
- **Aufbau.** Welt, darin Stages mit je **5 Leveln**. Die Ids heißen `1-<Stage>-<Level>` [Spiel]. Oben im Bild zeigen 5 Kästchen den Stand in der Stage [Spiel]. Es gibt Bonus-Portale mit Schlüsseln und versteckten Zusatzlevels [Daten, Web].
- **Sitzung.** Das Spiel startet direkt auf der Weltkarte, ohne Titelbild. Bis zum ersten Sprung vergehen etwa 2 Eingaben [Spiel]. Nach jeder Stage geht es zurück zur Karte. Zwischen zwei Leveln läuft ein kurzer Übergang mit Teufelsgesicht von etwa 2 s [Spiel].
- **Tod und Neustart.** Es gibt keine Checkpoints. Ein Tod setzt nur das aktuelle Level zurück, nie die Stage [Spiel]. Ablauf: Partikelexplosion mit leichtem Wackeln, nach etwa 1,0 s erscheint „PRESS ANY KEY“, nach dem Tastendruck fliegen die Partikel 300 bis 400 ms zur Figur zurück. **Vom Tod bis zur Kontrolle vergehen etwa 1,4 s plus Tastendruck** [Spiel]. Unser automatischer Respawn nach 0,7 s ist schneller.
- **Tode pro Level.** In den 15 gespielten Leveln: 2, 2, 0, 1, 1 | 2, 1, 4, 3, 1 | 1, 2, 3, 6, 4, im Mittel etwa 2,2 [Spiel]. Ein Mensch stirbt vermutlich eher öfter [Annahme].
- **Hilfe.** Nach etwa 5 Toden im selben Level erscheint ein „Skip“-Knopf mit Video-Symbol, wohl ein Werbe-Überspringen [Spiel]. Werbung läuft vor jedem Levelstart und jedem Neustart, die Plattform drosselt sie [Spiel].
- **Telemetrie.** Bei jedem Tod wird die Todesposition gesendet [Spiel]. Die Entwickler pflegen ihre Fallen also anhand von Todes-Heatmaps [Annahme].

## 2. Anatomie eines Levels

- **Raumgröße.** **251 von 271 Karten sind genau 33 × 21 Kacheln** (16 px Kachel), also ein fester Bildschirm [Daten]. Die Figur ist etwa 1 × 1,5 Kacheln groß, der Sprung etwa 1,3 Kacheln hoch und 2,5 bis 3 Kacheln weit [Spiel]. Keine Kamera, kein Scrollen in der Hauptspur [Spiel].
- **Ausnahmen mit mehr als einem Bildschirm.** 20 Karten (7 %) sind größer: 71 bis 168 Kacheln breit oder 40 bis 120 hoch. 13 davon haben keine Tür, 4 Karten nutzen einen Kamera-Effekt (Autoscroll/Verfolgung) [Daten]. Das sind vermutlich Sonderlevel, Bonus- oder Endlos-Abschnitte [Annahme]. **In den 15 gespielten Hauptleveln verlässt Level Devil den einen Bildschirm nie** [Spiel].
- **Laufzeit.** Reine Laufzeit sauber: 3 bis 6 s. Eine Raumdurchquerung dauert 3 bis 4 s (etwa 5,3 Kacheln pro Sekunde). Mit Toden dauert ein Level 10 bis 60 s [Spiel]. **Level Devil ist also kein langes Puzzle. Die Zeit entsteht durch Sterben und Verstehen, nicht durch einen langen sauberen Weg.**
- **Anzahl der Fallen.** Pro Level liegen **im Median 4 Auslöser** (Mittel 5,1). Verteilung: 1 Auslöser in 24 Leveln, 2 in 33, 3 in 44, 4 in 42, 5 in 37, mindestens 6 in 66 [Daten]. Ein Auslöser aktiviert im Median 2 Fallen-Objekte mit einer Verzögerung von median 1,0 s (Mittel 2,4 s, Ausreißer bis 30 s) [Daten]. Ein Level hat im Median nur 2 verschiedene Effekt-Arten [Daten]. **Es sind viele Auslöser, aber wenige verschiedene Ideen pro Raum.** Zum Vergleich: Wir sprechen von 2 bis 4 Überraschungen (H4).
- **Lehren, dann brechen.** Die Reihenfolge innerhalb einer Stage ist sehr bewusst [Spiel]:
  - S1L1: eine Grube öffnet sich vor der Tür. S1L2: derselbe Raum, jetzt öffnet sich eine zweite Grube direkt nach dem Sprung über die erste. S1L3: die Grube ist sichtbar und gleitet auf den Spieler zu. S1L4: der Boden frisst sich hinter dem Spieler her. S1L5: der Boden erodiert schneller, als man läuft.
  - Innerhalb einer Stage kommt ein Baustein neu hinzu (Gruben, dann Spikes, dann Schiebeblöcke), danach wird er mit dem Gelernten kombiniert.
- **Raum-Wiederverwendung.** Der flache Raum mit Tür rechts erscheint in S1L1, S1L2, S1L4, S3L2 und S4L1, jedes Mal mit neuer oder geschichteter Falle [Spiel]. Das ist das Vorbild für unsere Revanche-Runden: gleicher Raum, neues Blatt.
- **Tür.** Meist sichtbar am anderen Ende. In **15 % der Karten (38 von 251) bewegt sich die Tür selbst** (Bewegungs-Effekt auf der Tür) [Daten]. Die Tür steht überwiegend ruhig; Betrug entsteht meist durch die Umgebung der Tür: Grube kurz davor, Spikes dahinter, ein Block, der die Landung versperrt [Spiel]. Die Tür zählt auch im Flug [Spiel].

## 3. Katalog der Überraschungen

Häufigkeit = Anteil der 251 Ein-Bildschirm-Karten, in denen der Effekt mindestens einmal vorkommt [Daten]. Die Deutung des Effekts („Boden rutscht weg“ usw.) stammt aus der Spielsitzung, wo sichtbar [Spiel], sonst aus dem Namen des Effektskripts [Annahme].

| Familie | Häufigkeit | Beispiele |
|---|---|---|
| **Verschieben** (Bewegungs-Effekt, auch mehrstufig, Dauer meist um 0,16 s) | 93 % | Boden gleitet weg (Grube öffnet sich), Grube gleitet auf den Spieler zu, Decke fällt, Spikes rutschen genau auf die Landestelle [Spiel], Wand schiebt sich heran (S3L5), Plattform wird weggezogen, Tür läuft weg |
| **Wachsen und Schrumpfen** | 24 % | Spikes wachsen aus dem Boden, Säule schiebt den Spieler zurück in die Grube [Spiel], ganzer Boden wird zu Spikes (S2L4, ca. 3 s nach der Talsohle) [Spiel] |
| **Bildschirmwackeln** | 10 % | Begleitet Auslöser, kündigt aber nichts an [Annahme] |
| **Umwandeln** (Transform) | 9 % | Tarnung von Boden und Gefahr [Annahme] |
| **Rhythmus** | 8 % | Takt-Fallen [Annahme] |
| **Schiebeblöcke** | 7 % | Block schiebt vom Sims; Block kehrt wie ein Bumerang zurück (S3L3); Block wird zur Plattform, trägt aber nicht mit (S3L4) [Spiel] |
| **Dauer-Bewegung** (Auto-Move) | 7 % | Spikes wie auf einem Förderband (S2L3, etwa 100 px/s) [Spiel] |
| **Drehen** | 5 % | [Annahme] Welt oder Objekte drehen |
| **Bälle, Verfolger, Wächter** | 3 bis 5 % | Ball-Fallen, Objekte, die dem Helden folgen oder auf ihn zuschießen, Patrouillen |
| **Steuerung / Held** (Held skalieren, Held verschwinden, Held einrasten) | 1 bis 4 % | selten, daher jeweils überraschend |
| **Schwerkraft** | 1 % (2 Karten) | Sehr selten, ein Spezialtrick |
| **Sonstiges im Raum** | | Sägeblätter in 19 % der Karten, Spikes als Fallen-Objekt in 44 %, Schalter in 27 %, Portale in 6 %, Schlüssel in 6 %, Federn in 8 %, Bomben, Schwarze Löcher, Kugeln und Werfer jeweils um 3 % |

Folgerungen für Reviewer:

- **Der Kern ist Bewegung von Boden, Wand, Spikes und Tür.** Das Geheimnis ist nicht die Menge der Effekte, sondern dass dieselbe einfache Bewegung immer wieder an einer anderen Stelle und zu einer anderen Zeit gegen die Erwartung läuft.
- **Weiche Randeffekte wie Schwerkraft und Level-Spiegeln sind selten.** Wir dürfen sie ruhig als Highlight behalten, aber nicht für Hauptlevel brauchen.
- **Gefälschte Ausgänge** erwähnen öffentliche Lösungsseiten [Web]. In den 15 gespielten Leveln trat keiner auf. Die Tür war immer echt, ihre Umgebung nicht [Spiel].
- **Keine Versuch-Zufälligkeit.** In zwei Fällen (S2L5, S3L1) wich die Falle zwischen zwei Versuchen ab. Ursache vermutlich Tempo oder Position des Spielers, nicht Zufall [Spiel, Annahme]. Das Spiel ist in der Regel deterministisch.

## 4. Puzzle- und Labyrinth-Charakter

- **Ein Bildschirm, immer.** Siehe oben. Das passt zu unserer Regel H1.
- **Das Labyrinth-Gefühl kommt aus dem Raumbau, nicht aus Länge** [Spiel]:
  - S2L1: Start auf einem oberen Sims, Abstieg in einen Korridor, Tür links.
  - S2L3: Start auf einer Säule mit zwei Schächten, der „sichere“ Schacht wird beim Verlassen der Säule tödlich.
  - S2L4: V-förmiges Tal mit Treppenstufen, Tür oben.
  - S2L5: nur 1 Kachel hoher Tunnel.
  - S3L4: unspringbare Lücke von etwa 10 Kacheln, die nur über einen fliegenden Block überbrückt werden kann.
- **Routen und Rückwege.** Es gibt Abstieg, Aufstieg, Schächte und Sackgassen, aber **kaum klassische Schalter-Tür-Rätsel**. Schalter-Objekte stecken in 27 % der Karten [Daten], meist als Auslöser für Fallen und nicht als Schlüssel [Annahme]. Ein Rückweg über den ganzen Raum kommt selten vor.
- **Die Lösung ist fast immer eine Verhaltensregel**, kein Weg: „Falle durch Annäherung auslösen, dann reagieren“, „stehen bleiben“, „durchrennen“, „im richtigen Takt auf dem Block mitlaufen“ [Spiel].
- **Fazit:** Level Devil ist ein **Reaktions-Rätsel**, in dem man herausfindet, *wann* man etwas tut. Unser V2-Ziel (Schalter, Portal-Routing, Rückwege) geht in die **Weg-Richtung** und ist damit bewusst mehr Puzzle als das Original. Nicht abwerten, aber prüfen, ob die Überraschung dabei nicht verloren geht.

## 5. Fairness und Lesbarkeit

- **Optik** [Spiel]: einfarbiger Hintergrund, hellere Raumfläche, schwarze Silhouette, graue Tür. Fallen haben **dieselbe Farbe wie der Boden**, Spikes sind nur leicht abgedunkelt. Es gibt kaum Dekor und keine Telegrafierung.
- **Man sieht den ganzen Raum** vor dem Start. Versteckt ist die Reaktion des Raums, nicht der Raum selbst.
- **Fairness durch Wiederholung.** Jeder Versuch läuft gleich, und das Level setzt sich komplett zurück. Der erste Tod an einer Falle ist Pflicht, der zweite sollte nicht nötig sein [Spiel].
- **Toleranz.** Die Sprungweite ist fest (kein variabler Sprung, keine Auto-Wiederholung beim Halten). Eine 2-Kachel-Grube ist knapp zu schaffen (S3L5, Sprung über 63 px) [Spiel]. Bei Level Devil ist also **Präzision erlaubt**, auf Touch wäre sie härter. Wir haben mit Absicht variable Sprunghöhe und Coyote-Zeit, und Toleranz von etwa 0,15 s (H7).
- **Wie sich ein Tod anfühlt** [Spiel, Annahme]: Er ist sofort, klar zugeordnet („der Boden war weg“, „die Spikes sind da gelandet“) und lustig, weil das Spiel gerade das Gelernte gegen einen verwendet. Der Tod-Neustart dauert länger als bei uns, trägt aber bewusst zum „nochmal“-Gefühl bei.
- **Reaktive Fallen** (Spikes rutschen auf die Landestelle, Block schiebt in die Grube, Boden verfolgt dich) sind der wichtigste Eindruck der Spielsitzung: Die Falle hängt davon ab, *wo man gerade ist und was man tut* [Spiel].

## 6. Tempo und Schwierigkeitskurve

- Stage 1 ist ein sanftes Lehrbuch (0 bis 2 Tode pro Level). Stage 2 springt bei S2L3 (4 Tode). Stage 3 verlangt Timing und Präzision, mit **S3L4 als klarer Spitze** (6 Tode). Stage 4 beginnt mit invertierter Palette [Spiel].
- **Novelty-Rhythmus:** pro Stage ein neuer Baustein (Gruben, Spikes, Schiebeblöcke), in den ersten drei Leveln einer Stage noch einfach, im letzten die Kombination [Spiel].
- **Kontraste:** Je Stage eine leicht andere Hintergrundfarbe, am Ende ein Wechsel (Invertierung), keine Geschichten, keine Dialoge [Spiel].
- **Abwechslung zwischen Leveln:** Der Raum wiederholt sich, die Falle nicht. Zwei Level mit demselben Raum und derselben Falle gab es nicht [Spiel].
- **Ausreißer-Level.** Ein „Skip“-Knopf nach etwa 5 Toden zeigt, dass die Entwickler Spitzen in Kauf nehmen [Spiel].

## 7. Review-Fragen für unsere Level

1. Würde dieses Level in Level Devil als **eigenes Level** durchgehen oder nur als **Teil eines Levels** (zu kurz, nur eine Idee)?
2. Gibt es einen Moment, in dem der Spieler **laut lacht oder flucht**? Welcher?
3. Bricht die zweite Überraschung die Regel, die die erste gerade gelehrt hat?
4. Hängt mindestens eine Falle davon ab, **wo der Spieler gerade steht** oder was er tut (reaktiv), oder stehen alle Fallen nur an festen Stellen?
5. Ist der **kürzeste Weg der Köder**? Was passiert, wenn man einfach nach rechts läuft?
6. Versteht man nach dem **ersten** Tod, was passiert ist, ohne den Hinweistext zu lesen?
7. Ist die Falle **sichtbar fair**: Sieht man den Raum, und war die Ursache des Todes vorher im Bild?
8. Kommt dieselbe Hauptfalle (zum Beispiel „Stacheln wachsen“) in den Leveln davor und danach? Würde ein Spieler sie **vorhersagen**?
9. Gilt der Raum als **neu**, oder ist es derselbe Raum mit anderer Stelle? Wenn derselbe Raum, ist die Revanche eine **neue Regel** und nicht nur die verschobene Falle?
10. Ist **Stillstehen** und **ruhiges Weiterlaufen** zu Beginn sicher (H10)? Bestraft die Falle den Reflex und nicht die Ruhe?
11. Wie lange dauert ein **sauberer** Lauf, und wie lange mit 2 bis 3 Toden? Liegt die Zeit beim Verstehen und nicht beim Warten?
12. Wo ist die **Tür**? Hat sie einen Moment, in dem sie lügt (Umgebung, Bewegung, Falle kurz davor)?
13. Gibt es eine **Spitze** in der Stage (ein klar schwereres Level) und **Verschnaufpausen** (ein sehr leichtes Level mit einer starken Pointe)?
14. Würde ein Spieler nach dem Tod **sofort nochmal** wollen, oder mitten im Level aufhören? Warum?
15. Ist der Teil, der uns von Level Devil unterscheidet (Karte, Mephi-Spruch, Revanche), eine **Verbesserung** oder nur eine Ablenkung vom Gag?

## 8. Wo wir bewusst anders sind

Reviewer sollen diese Unterschiede **nicht** als Mangel werten:

- **Karten und Mephi.** Mephi spielt eine Karte, die Falle kommt aus der Kartenfamilie. Level Devil zeigt nie etwas vorher. Die Karte ist unser Erzählmittel und hat eigenes Gewicht (H8). Sie soll die Falle nicht verraten, aber sie ist gewollt sichtbar.
- **Story, Figur, Album, Sprüche, Humor.** Level Devil ist minimal und stumm. Mephi kommentiert, wir sammeln Karten und haben ein Album.
- **Revanche-Runden.** Level Devil wiederholt Räume über mehrere Level hinweg. Bei uns folgt die Revanche **im selben Level** direkt nach der Tür (H9). Das ist unser Gegenstück zu „gleicher Raum, neue Falle“ in einem anderen Rahmen.
- **Welten mit Themen** (Höllenkeller, Rechenzentrum, Platine) statt wechselnder Hintergrundfarbe. Ihre Mechaniken (Portale, Bänder, Laser, Lüfter, Hitze) sind zusätzliche Bausteine.
- **Mehr Rätselbausteine** (R1 bis R12: Schalter, Halteschalter, Portal-Routing, Rückwege), also **mehr Weg-Puzzle** als Level Devil. Das war die Vorgabe des Gründers.
- **Mindestzeit eines sauberen Laufs** (H3). Level Devil kommt mit 3 bis 6 s aus. Wir wollen mehr, damit ein Level mehr als einen Reflex enthält.
- **Schnellerer Neustart** (0,7 s automatisch) statt „PRESS ANY KEY“, **variable Sprunghöhe** und **Coyote-Zeit**, und ein Tipp nach 9 s Festhängen.
- **Seltene Ausnahme: mehr als ein Raum.** Wir erlauben in wenigen Leveln eine Kamera von Raum zu Raum. Level Devil nutzt Mehrbildschirm-Karten nur für Sonderlevel.
- **Bluff mit Tell.** Level Devil bluffte nie. Unser Bluff (selten, nur in Revanchen, mit Hinweis) ist ein eigener Stilzug.
- **Werbung.** Sie ist bei uns weniger aufdringlich geplant. Nicht Teil der Level-Bewertung.

## 9. Grenzen dieses Dokuments

- Die Spielsitzung deckt nur **15 von etwa 200 Leveln** (Welt 1, Stage 1 bis 3). Aussagen zu späteren Stages und Welten stützen sich nur auf die Dateien (Zählungen), nicht auf Spielerfahrung.
- Die Zählungen aus den Dateien sagen, was **vorhanden** ist, nicht, was ein Spieler **erlebt**. Ein Effekt-Name ist kein Beleg für sein Verhalten, wo oben „Annahme“ steht.
- Die Web-Beschreibungen sind kurze Treffer von Videoseiten und Foren und wurden nicht nachgeprüft.
