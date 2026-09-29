# Bevel Devil

Ein nativer Android-Troll-Platformer im „Höllen-CRT“-Look. Der kleine Würfel **Bevel** will zur Tür, und **Mephi**, der Croupier der Hölle, spielt ihm dabei Fallenkarten aus.

| Titel | Level 1 | Teufelskarte |
|---|---|---|
| ![Titel](docs/screenshots/01-title.png) | ![Level 1](docs/screenshots/04-level1-trap.png) | ![Level 5](docs/screenshots/07-level5-flip.png) |
| **Levelwahl** | **Ab durch die Tür** | **Album** |
| ![Levelwahl](docs/screenshots/02-select.png) | ![Sieg](docs/screenshots/21-win-burst.png) | ![Album](docs/screenshots/11-album.png) |

## Was drin ist

- **128 Level** in Welt 1 „Höllenkeller“ (acht Kapitel à 16): erst ein Trick pro Level (Einsturz, Stachelsaat, fliehende Tür, Kopfnuss, Schwerkraft-Flip, vertauschte Steuerung, Teufelssäge, Geisterblock, Sinkflug, Attrappe, Wackelboden), dann Doppel-Trolle, Warten, Rückwärtslaufen und Kombis mit vielen Nerd-Anspielungen bis zum Finale „Integer Overflow“.
- **Mephi** im goldenen Rahmen mit fünf Stimmungen (lauert, lacht, schmollt, entsetzt). Er kommentiert jeden Tod und jede Falle, auf Deutsch oder Englisch je nach Gerätesprache.
- **Teufelskarten:** Jede Falle wird als Karte ausgespielt, die aus Mephis Rahmen ins Bild fliegt. Gefundene Karten landen im Album, zusammen mit einem Zähler, wie oft sie dich erwischt haben.
- **Höllen-CRT-Look:** 256×144-Spielfeld (8 px pro Tile) in einem Pixelpuffer, der mit ganzzahliger Skalierung jedes Seitenverhältnis ohne Balken füllt, Farbstrudel mit Dithering, Bevel-Kanten, harte Schlagschatten, Scanlines und Vignette.
- **Mauerwerk statt Kacheln:** Berührende Blöcke verschmelzen per Autotiling zu Massen aus unregelmäßigen Goldsteinen, mit Bevel nur an freien Kanten. Fallen sind bis zum Auslösen pixelgleich mit normalem Boden (`TrapInvisibilityTest`).
- **Leben im Bild:** aufsteigende Glut, eine Parallax-Skyline der Hölle, glühende Risse im Fels, Türlicht, blitzende Spikes, Staubwolken, zersplitternder Würfel, Sog in die Tür und Dither-/Iris-Blenden zwischen Screens.
- Coyote-Time, Sprungpuffer, variable Sprunghöhe, Touch-Steuerung (Multitouch) sowie Tastatur und Gamepad.
- Synthetisierte Sounds, keine Audiodateien. Der Fortschritt wird lokal gespeichert. Release-APK rund 80 KB.

## Bauen

Voraussetzungen: JDK 17+ und ein Android-SDK (compileSdk 35). Mit Android Studio einfach das Projekt öffnen, oder:

```bash
./gradlew assembleDebug          # APK unter app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # Logik- und Screenshot-Tests
```

Die Screenshot-Tests rendern echte Screens mit Robolectric nach `app/build/screenshots/`. So lässt sich der Look ohne Gerät prüfen.

## Aufbau

```
app/src/main/java/com/robinrehbein/beveldevil/
├── game/            Reine Spiellogik, ohne Android-Abhängigkeiten
│   ├── Level.kt     Level-DSL: Karte, Glyphen, Trigger, Aktionen
│   ├── Levels.kt    Level-Register (alle Welten hintereinander)
│   ├── Worlds.kt    Welten: Name, Übergangsscreen, Index-Mapping ("2-17")
│   ├── World1*.kt   Die 128 Level von Welt 1 (Part1–Part8, je 16 Level) und Bau-Helfer
│   ├── World.kt     Physik, Kollision, Fallen (ein Versuch)
│   ├── Game.kt      Screens, Mephis Stimmung, Karten, Fortschritt
│   ├── Cards.kt     Die Teufelskarten
│   └── Txt.kt       UI-Texte (DE/EN)
├── render/          Renderer, prozedurales Mephi-Sprite, Karten-Icons
├── audio/Sfx.kt     Chiptune-Synth
├── GameView.kt      Game-Loop (feste 120 Hz), Touch, Tastatur
└── MainActivity.kt
```

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
- Trigger: `PastX`, `BeforeX`, `Zone`, `Touch`, `After`.
- Aktionen: `Fall`, `Show`, `Hide`, `Move`, `DoorTo`, `Gravity`, `Swap`, `Saw`, `Say`, `Shake`, `Play`.

Jedes Level hat in `World1Test` einen Bot, der es mit der echten Physik durchspielt. Wer ein Level ändert, sieht sofort, ob es noch lösbar ist.

## Lizenzen

Pixel-Schrift: [Silkscreen](https://github.com/googlefonts/silkscreen), SIL Open Font License 1.1 (siehe `licenses/`).
