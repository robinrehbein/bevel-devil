# Mephi the Daemon

Ein nativer Android-Troll-Platformer im „Höllen-CRT“-Look. Der kleine Würfel **Bevel** will zur Tür, und **Mephi**, der Croupier der Hölle, spielt ihm dabei Fallenkarten aus.

| Titel | Level 1 | Teufelskarte |
|---|---|---|
| ![Titel](docs/screenshots/01-title.png) | ![Level 1](docs/screenshots/04-level1-trap.png) | ![Level 5](docs/screenshots/07-level5-flip.png) |
| **Levelwahl** | **Ab durch die Tür** | **Album** |
| ![Levelwahl](docs/screenshots/02-select.png) | ![Sieg](docs/screenshots/21-win-burst.png) | ![Album](docs/screenshots/11-album.png) |

## Was drin ist

- **48 Level** in Welt 1 „Höllenkeller“, drei Akte à 16: **Die Karten** (die zwölf klassischen Tricks: Einsturz, Stachelsaat, fliehende Tür, Kopfnuss, Schwerkraft-Flip, vertauschte Steuerung, Teufelssäge, Geisterblock, Sinkflug, Attrappe, Wackelboden, Finale), **Neue Regeln** (blinkende Plattformen, Pfad-Sägen, der Idle-Trigger, erst einzeln, dann mit den Klassikern) und **Mephi schummelt** (Meta-Twists wie Fake-Abspann, ausweichender Pause-Knopf, Rahmenbruch, Kopfstand, Geister-Versuch; nur zwei Level nutzen Handy-Neigung und Schütteln; Finale „Abspann“). Dazu Nerd-Anspielungen von Segfault bis sudo.
- **48 Level** in Welt 2 „Höllen-Rechenzentrum“ (Schicht 2: Netzwerk und Server), wieder drei Akte à 16: **Handshake** (die besten Nerd-Witze von Hello World bis 404, erste Portale, der DNS-Trick), **Traffic** (Förderbänder als Datenbus und Laser als Firewall, erst einzeln, dann mit Portalen und Klassikern) und **Root** (Kombinationen, drei Meta-Twists in neuer Verkleidung: Replay-Angriff, Pause als Lösung, Lag-Spitze; ein Level mit Schütteln; Finale „shutdown -h now“, bei dem Mephi nach Schicht 3 flieht).
- **48 Level** in Welt 3 „Platine“ (Schicht 3: Hardware, grünes Board in Akt 1 und 2, blaues in Akt 3), drei Akte à 16: **Stromkreise** (Kupferbahnen, Schaltknöpfe, Takte, Leiterbahnen unter Strom, Mephi kappt den Strom unter dir, Bit-Flip), **Überhitzung** (Herdplatten, Chips unter Last, Kühlkörper, übertakteter Boden, Schmelzplatten, dann mit den Stromkreisen kombiniert) und **Lüfter** (Aufwind, Seitenwind, Gegenwind im Takt, Schubumkehr, Kombinationen; Finale in drei Stufen: Selbsttest, Boot-Reihenfolge, BIOS-Setup). Meta-Twists: falsche Tür, verkehrter Monitor, Bildrollen. Nach 3-48 folgt ein echtes Ende: `kill -9` auf Mephi, das Netz läuft wieder.
- **Mephi** im goldenen Rahmen mit fünf Stimmungen (lauert, lacht, schmollt, entsetzt). Er kommentiert jeden Tod und jede Falle, auf Deutsch oder Englisch je nach Gerätesprache.
- **Teufelskarten:** Jede Falle wird als Karte ausgespielt, die aus Mephis Rahmen ins Bild fliegt. Gefundene Karten landen im Album, zusammen mit einem Zähler, wie oft sie dich erwischt haben.
- **Höllen-CRT-Look:** 256×144-Spielfeld (8 px pro Tile) in einem Pixelpuffer, der mit ganzzahliger Skalierung jedes Seitenverhältnis ohne Balken füllt, Farbstrudel mit Dithering, Bevel-Kanten, harte Schlagschatten, Scanlines und Vignette.
- **Mauerwerk statt Kacheln:** Berührende Blöcke verschmelzen per Autotiling zu Massen aus unregelmäßigen Goldsteinen, mit Bevel nur an freien Kanten. Fallen sind bis zum Auslösen pixelgleich mit normalem Boden (`TrapInvisibilityTest`).
- **Leben im Bild:** aufsteigende Glut, eine Parallax-Skyline der Hölle, glühende Risse im Fels, Türlicht, blitzende Spikes, Staubwolken, zersplitternder Würfel, Sog in die Tür und Dither-/Iris-Blenden zwischen Screens.
- Coyote-Time, Sprungpuffer, variable Sprunghöhe, Touch-Steuerung (Multitouch) sowie Tastatur und Gamepad.
- Synthetisierte Sounds, keine Audiodateien. Der Fortschritt wird lokal gespeichert. Release-APK rund 80 KB.

## Bauen

Voraussetzungen: JDK 17+ und ein Android-SDK (compileSdk 36). Mit Android Studio einfach das Projekt öffnen, oder:

```bash
./gradlew assembleDebug          # APK unter app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # Logik- und Screenshot-Tests
```

Die Screenshot-Tests rendern echte Screens mit Robolectric nach `app/build/screenshots/`. So lässt sich der Look ohne Gerät prüfen.

## Test-Releases über GitHub Actions

Nach jedem Push auf `main` (auch nach einem Merge) baut `.github/workflows/play-test-release.yml` einen signierten AAB, führt die Unit-Tests aus und veröffentlicht denselben Build in den Play-Tracks `internal` und `alpha`. Der `versionCode` wird aus der GitHub-Workflow-Laufnummer gebildet. Der Workflow kann auch manuell gestartet werden.

Für den Workflow sind diese Repository-Secrets nötig:

| Secret | Inhalt |
|---|---|
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | Base64-kodierte Upload-Keystore-Datei |
| `ANDROID_UPLOAD_STORE_PASSWORD` | Keystore-Passwort |
| `ANDROID_UPLOAD_KEY_ALIAS` | Alias des Upload-Schlüssels |
| `ANDROID_UPLOAD_KEY_PASSWORD` | Passwort des Upload-Schlüssels |
| `PLAY_SERVICE_ACCOUNT_JSON` | JSON-Schlüssel eines Google-Servicekontos mit Veröffentlichungsrecht für diese App |

Die lokale Upload-Key-Datei und die Zugangsdaten liegen in `release/`, das von Git ignoriert wird. **Beide Dateien sicher sichern:** Ohne den Upload-Schlüssel lassen sich spätere Builds nicht mit demselben Schlüssel hochladen. Der erste signierte AAB muss über die Play Console hochgeladen werden, bevor die Google Play Developer API Updates für eine neue App übernehmen kann. Für den geschlossenen Alpha-Test muss die App-Einrichtung in der Play Console abgeschlossen sein; Tester werden dort verwaltet.

## Aufbau

```
app/src/main/java/com/robinrehbein/beveldevil/
├── game/            Reine Spiellogik, ohne Android-Abhängigkeiten
│   ├── Level.kt     Level-DSL: Karte, Glyphen, Trigger, Aktionen
│   ├── Levels.kt    Level-Register (alle Welten hintereinander)
│   ├── Worlds.kt    Welten: Name, Übergangsscreen, Index-Mapping ("2-17")
│   ├── World1*.kt   Die 48 Level von Welt 1 (Part1–Part3, ein Akt je 16 Level) und Bau-Helfer
│   ├── World2*.kt   Die 48 Level von Welt 2 (Part1–Part3: Handshake, Traffic, Root) und Bau-Helfer (Racks, LEDs)
│   ├── World3*.kt   Die 48 Level von Welt 3 (Part1–Part3: Stromkreise, Überhitzung, Lüfter) und Bau-Helfer (Brücken, Leiterbahnen)
│   ├── World.kt     Physik, Kollision, Fallen (ein Versuch)
│   ├── Game.kt      Screens, Mephis Stimmung, Karten, Fortschritt
│   ├── Cards.kt     Die Teufelskarten
│   └── Txt.kt       UI-Texte (DE/EN)
├── render/          Renderer, prozedurales Mephi-Sprite, Karten-Icons; `Theme.kt`: Look pro Welt (Welt 1 Höllenkeller, Welt 2 Rechenzentrum mit Stahl-Racks, LED-Hintergrund in `DataCenter.kt`; neue Welt = ein `Theme`)
├── audio/Sfx.kt     Chiptune-Synth
├── GameView.kt      Game-Loop (feste 120 Hz), Touch, Tastatur
└── MainActivity.kt
```

Launcher-Icon: adaptives Pixel-Vektor-Icon (`res/drawable/ic_launcher_{background,foreground,monochrome}.xml`, Mephi mit goldenem Rim-Light vor gedithertem Höllen-Glühen mit Scanlines); Play-Store-Grafik in `docs/play/icon-512.png`. `IconScreenshotTest` zeichnet es unter verschiedenen Masken nach `app/build/screenshots/icon-*.png`.

## Ein Level bauen

Level sind Daten. Die Karte ist 32×18 Tiles groß, der Spieler läuft normalerweise auf Zeile 14 (Bodenoberkante y = 15).

```kotlin
Level(
    name = T("Warm-up", "Aufwärmen"),
    intro = T("Go on, walk to the door.", "Geh ruhig zur Tür."),
    traps = listOf(
        trap(PastX(16.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Floor?", "Boden?"))),
    ),
) {
    border(); floor()
    fill(19..21, 15..17, 'a')          // Gruppe 'a' kann später einstürzen
    put(2, 14, 'P'); put(29, 14, 'D')  // Start und Tür
}
```

- `#` Block, `^ v < >` Spikes, `P` Start, `D` Tür.
- Kleinbuchstaben sind Block-Gruppen, Großbuchstaben Spike-Gruppen. Über `legend` lassen sie sich verstecken (`hidden`) oder erst beim Kopfstoß sichtbar machen (`bonk`).
- Trigger: `PastX`, `BeforeX`, `Zone`, `Touch`, `After`, `Idle(s)` (s Sekunden keine Eingabe), `Shaken` (Handy geschüttelt).
- Aktionen: `Fall`, `Show`, `Hide`, `Move`, `DoorTo`, `Gravity`, `Swap`, `Saw`, `Say`, `Shake`, `Play`, dazu die Mechaniken unten.
- Hardware-Trigger (Welt 3): `Pressed(pad)`, `Heated(group, above)`.
- `start = listOf(...)` führt Aktionen gleich beim Levelstart aus (sonst per Trigger).

Mechaniken (sichtbar, keine versteckten Fallen):

```kotlin
Blink('a', on = 1.8f, off = 1f, phase = 0f)       // Plattform blinkt; flackert rot vor dem Verschwinden,
                                                  // kommt nie im Spieler zurück (wartet, bis er raus ist)
PathSaw(6f, 16f to 14.4f, 16f to 9f, delay = 1.8f) // Säge auf Wegpunkten, Tiles/s, hin und her
PathSaw(4f, 23f to 11f, 26f to 11f, 26f to 13f, loop = true) // oder im Kreis
trap(Idle(1.2f), Fall('a'))                       // „Steh nicht nur rum“
Tilt('a', left = 0f, right = 13f, speed = 6f)     // Wasserwaage: Gruppe rutscht mit der Handyneigung
Slope(8f)                                         // Neigung schiebt Bevel bis 8 Tiles/s zur Seite
trap(Shaken, Hide('a'))                           // Schütteln
```

Netzwerk-Mechaniken für Welt 2 (Laufzeit in `game/Net.kt`, Look in `render/NetPainter.kt`, Demos in `NetDemos`):

```kotlin
Portal('1', from = 8 to 14, to = 17 to 14)        // Tile → Tile, gleiche Geschwindigkeit, gleiche Stelle im Tile;
                                                  // beidseitig (twoWay = false: Einbahn), nimmt erst wieder, wenn man raus ist
trap(PastX(6f), Reroute('1', to = 5 to 3))        // „DNS geändert“: Ausgang springt woandershin
Belt('b', 3f)                                     // Gruppe 'b' ist ein Förderband, Tiles/s (negativ: links), addiert zur Laufgeschwindigkeit
trap(PastX(10f), Belt('b', -10f))                 // „Paket-Umsortierung“: dreht um, schneller als Bevel läuft
Laser('L', 15 to 1, 15 to 14, on = 1f, off = 1.4f) // Strahl zwischen zwei Emittern (gerade Linie), tödlich; off = 0: Dauerfeuer.
                                                  // Emitter glühen 0,6 s vor dem Feuern, dazu eine Punktlinie
trap(PastX(10f), Power('L', false))               // Portal, Laser oder Band aus/an; ein Laser heizt beim Einschalten erst vor
```

Hardware-Mechaniken für Welt 3 (Laufzeit in `game/Hardware.kt`, Look in `render/HardwarePainter.kt`, Demos in `HardwareDemos`):

```kotlin
Circuit('a')                                      // Kleinbuchstaben-Gruppe = Kupferschiene: mit Strom fest, ohne nur gestrichelter Umriss
Circuit('Z')                                      // Großbuchstaben-Gruppe = blanke Leiterbahn: unter Strom tödlich, nie fest
Clock('a', on = 1.8f, off = 1f, phase = 0f)       // Takt; flackert rot vor dem Abschalten, kommt nie im Spieler zurück
Pad('1', at = 6 to 14, circuits = "ab", mode = PadMode.TOGGLE) // Druckplatte (TOGGLE, HOLD, ON, OFF), Kappe in der Farbe des Kreises
trap(PastX(12f), Power('a', false))               // Mephi dreht den Strom ab; Toggle("ab"), BitFlip('a', 'b') tauschen
trap(Pressed('1'), ...)                           // Trigger: Platte getreten; Heated('h', 0.5f): Gruppe so heiß
Heat('h', rise = 1.2f, cool = 1.2f)               // Heizplatte: heizt, solange man draufsteht, kühlt sonst ab; voll heiß = tödlich
Heat('c', rise = 3f, load = true, melt = false)   // Chip unter Last: heizt immer; melt: schmilzt statt zu brennen
Heatsink('k', cools = "c")                        // Kühlkörper: draufstehen kühlt die Gruppen schnell ab
trap(PastX(14f), HeatSpike('f', 0.7f))            // „Übertaktet“: normaler Boden wird schlagartig heiß (bis dahin pixelgleich)
Fan('f', at = 12 to 15, dir = Dir.UP, reach = 9, speed = 10f, width = 2) // Lüfter: seitlich Drift, hoch/runter ersetzt er die Schwerkraft
trap(PastX(9f), FanSet('f', -8f))                 // dreht langsam über null um; Power('f', false) läuft aus
```

Im `Bot` gibt es dafür `waitPowered(id)`, `waitCooled(id)` und `rightUntil { … }` (z. B. im Aufwind, bis hoch genug).

Neigung und Schütteln liest `GameView` nur in Leveln, die sie nutzen (Schwerkraftsensor, sonst Beschleunigungssensor). Ohne Sensor oder mit Einstellung „Neigung: Aus“ erscheinen unten zwei Neige-Tasten (einrastend) und eine Schütteltaste; Tastatur Q/E neigen, S schüttelt. Im `Bot` gibt es dafür `tilt(v)` und `shake()`. Mini-Level für jede Mechanik liegen in den Tests (`Demo.kt`).

### Meta-Twists

Fallen, die das Spiel selbst angreifen, jeweils opt-in pro Level (Logik in `game/Twists.kt`, Look in `render/TwistPainter.kt`):

```kotlin
trap(AtDoor, FakeWin(FakeEnd.CREDITS, 'c', DoorTo(1, 8)))  // Fake-Abspann; die letzten Zeilen werden Gruppe 'c'
trap(AtDoor, FakeWin(FakeEnd.CLEAR, null, Hide('f')))     // Fake-„GESCHAFFT!“, danach fehlt der Boden
trap(After(0.3f), PauseTrap(PauseTrick.DODGE))            // Pause-Button weicht aus (auch SPIKE, SWAP)
trap(Resumed(), Hide('w'))                                // feuert nach Pause + Weiter
trap(PastX(4f), FrameCrack(16, 0, 19, 0, warn = 0.9f))   // Stück vom Goldrahmen bricht ab und fällt
trap(PastX(6f), Flip(3f))                                 // Bild steht 3 s Kopf
trap(PastX(21f), Roll(1.2f))                              // CRT verliert den Bildfang
trap(After(0f), Ghost(1f))                                // letzter Versuch läuft als tödlicher Geist mit
```

- `AtDoor` feuert statt des Siegs; ein Fake-Sieg speichert nichts, zählt nichts und schaltet nichts frei. Danach spuckt die Tür Bevel wieder aus (kurz gesperrt).
- Pause bleibt immer echt erreichbar: Zurück-Taste und App-Wechsel pausieren unabhängig vom Trick, und im Pausemenü setzt ein Tipp neben die Buttons fort.
- `Flip` ist nur optisch; links/rechts folgen dem Bildschirm (drückt man rechts, läuft Bevel auf dem Kopfstand-Bild nach rechts), Springen bleibt Springen.
- Demo-Level für jeden Twist liegen nur in den Tests (`TwistsTest`).

Jedes Level hat in `World1Test` und `World2Test` einen Bot, der es mit der echten Physik durchspielt. Wer ein Level ändert, sieht sofort, ob es noch lösbar ist.

## Lizenzen

Pixel-Schrift: [Silkscreen](https://github.com/googlefonts/silkscreen), SIL Open Font License 1.1 (siehe `licenses/`).
