# Play-Store-Eintrag: Mephi the Daemon

Alles, was für den Store-Eintrag gebraucht wird, liegt hier. Die Ordner spiegeln das Fastlane-`supply`-Layout (`listings/<locale>/title.txt` usw.), damit `scripts/publish_play.py` sie später hochladen könnte. Bis dahin: per Hand in die Play Console kopieren.

## Dateien und wohin sie gehören

| Datei | Play Console |
|---|---|
| `listings/de-DE/title.txt` (Standardsprache), `listings/en-US/title.txt` | Store-Präsenz > Hauptseite des Store-Eintrags > App-Name (max. 30 Zeichen) |
| `listings/*/short_description.txt` | Kurze Beschreibung (max. 80 Zeichen) |
| `listings/*/full_description.txt` | Vollständige Beschreibung (max. 4000 Zeichen) |
| `release-notes/de-DE.txt`, `en-US.txt` | Produktion/Test > Release > „Versionshinweise“ (max. 500 Zeichen je Sprache) |
| `icon-512.png` | App-Symbol (512 × 512) |
| `feature-graphic.png` | Funktionsgrafik (1024 × 500) |
| `screenshots/phone/01..08.png` | Smartphone-Screenshots, deutsche Oberfläche, ohne Text |
| `screenshots/phone-captioned/01..08.png` | dieselben mit deutscher Bildunterschrift |
| `screenshots/phone-en/`, `screenshots/phone-captioned-en/` | englische Oberfläche (für die Sprachvariante en-US) |
| `../privacy-policy.md` | Datenschutzerklärung, muss als öffentliche URL erreichbar sein (siehe unten) |

Die Bilder sind 24-Bit-PNGs ohne Alphakanal, 1920 × 1080 (16:9, innerhalb der Play-Grenzen: Seitenverhältnis höchstens 2:1, je Datei unter 8 MB). Das Spiel ist Querformat, deshalb sind auch die Screenshots Querformat. Tablet-Screenshots (7"/10") sind optional; dieselben Bilder genügen, falls Play danach fragt.

Empfehlung: die Variante mit Bildunterschrift als Hauptsatz nehmen (Reihenfolge 01 bis 08 so lassen, Titelbild zuerst), die Variante ohne Text als Reserve. Der Text `de-DE` ist Standard, `en-US` eine zusätzliche Sprache (Store-Präsenz > Übersetzungen verwalten).

## Bilder neu erzeugen

Die Grafiken werden vom Spiel selbst gerendert (Robolectric, echte Painter). `StoreAssetsTest` schreibt nur nach `docs/play`, wenn die Umgebungsvariable gesetzt ist; normale Testläufe fassen die Docs nicht an:

```bash
STORE_ASSETS=1 gradle testDebugUnitTest --tests '*StoreAssetsTest' -q
```

(alternativ `-DstoreAssets=1` als JVM-Property des Testprozesses). Nach Änderungen an Theme oder UI danach die Bilder ansehen und mit einchecken. Die Szenen stehen in `app/src/test/java/com/robinrehbein/beveldevil/render/StoreAssetsTest.kt` (Liste `scenes`, Bildunterschriften inklusive).

## Checkliste Play Console

Reihenfolge wie im Dashboard unter „App-Einrichtung“ und „Store-Präsenz“.

### 1. App-Einrichtung (Richtlinien-Formulare)

- [ ] **Datenschutzerklärung:** Platzhalter `[KONTAKT-E-MAIL]`, `[NAME/FIRMA, ADRESSE]` und `[DATUM]` in `docs/privacy-policy.md` ersetzen, den Hinweis am Anfang löschen, dann öffentlich hosten. Optionen: GitHub Pages (Repo-Einstellungen > Pages > Quelle `main`, Ordner `/docs`; die URL wäre dann `https://<nutzer>.github.io/<repo>/privacy-policy`, evtl. ist dafür ein Jekyll-Theme oder eine `index.html` nötig, damit Markdown als Seite erscheint) oder die Firmenwebsite. Nichts davon ist aktiviert. URL in Play Console > App-Inhalte > Datenschutzerklärung eintragen.
- [ ] **Werbung:** „Nein, meine App enthält keine Werbung“. Stimmt: keine Werbe-Bibliothek in `app/build.gradle.kts`, nur JUnit und Robolectric als Testabhängigkeiten.
- [ ] **App-Zugriff:** „Alle Funktionen ohne Zugriffsbeschränkung verfügbar“ (kein Login, keine Konten).
- [ ] **Zielgruppe und Inhalt:** siehe Abschnitt unten.
- [ ] **Datensicherheit:** siehe Abschnitt unten.
- [ ] **Inhaltsbewertung (IARC):** siehe Abschnitt unten.
- [ ] **Nachrichten-App:** nein. **COVID-19-Kontaktverfolgung/Status:** nein. **Finanzfunktionen, Gesundheit, Regierungs-App:** nein.
- [ ] **Werbe-ID:** „Nein“, die App verwendet sie nicht (Pflichtangabe für Apps mit Target Android 13 und höher; die Berechtigung `AD_ID` steht nicht im Manifest).
- [ ] **Kontoerstellung löschen:** entfällt, es gibt keine Konten.

### 2. Datensicherheit (Antworten aus dem Code abgeleitet)

Grundlage: `AndroidManifest.xml` enthält keine einzige `uses-permission` (auch nicht `INTERNET`), keine Drittanbieter-SDKs, Fortschritt nur in lokalen SharedPreferences (`PrefsProgress.kt`), Sensoren nur im Arbeitsspeicher (`GameView.kt`).

| Frage | Antwort |
|---|---|
| Erhebt oder teilt deine App Nutzerdaten? | **Nein** |
| Daten werden bei der Übertragung verschlüsselt? | entfällt (keine Übertragung) |
| Können Nutzer die Löschung ihrer Daten beantragen? | entfällt (keine Kontodaten); Daten lassen sich per „App-Daten löschen“ oder Deinstallation entfernen |

Hinweise dazu:
- Die lokale Speicherung von Spielstand und Einstellungen zählt nicht als „Erhebung“, solange nichts das Gerät verlässt.
- Die Sensorwerte (Beschleunigung/Schwerkraft für Neigen und Schütteln) werden nur flüchtig verarbeitet: nicht erhoben.
- `allowBackup="true"` im Manifest: Android-Geräte-Backup kann Spielstand ins Google-Konto des Nutzers sichern. Das ist eine Systemfunktion und zählt nicht als Weitergabe durch die App. Falls du es vermeiden willst, `android:allowBackup="false"` setzen (Quelltext-Änderung, hier nicht gemacht).
- Sollte später etwas hinzukommen (Crash-Reporting, Ads, Online-Bestenliste, Cloud-Save), müssen Formular und Datenschutzerklärung vorher angepasst werden.

### 3. Inhaltsbewertung (IARC-Fragebogen)

Kategorie „Spiel“ wählen. Antworten, die zum Code passen:

| Thema | Antwort | Begründung |
|---|---|---|
| Gewalt | **Nein** bzw. nur niedrigste Stufe („Cartoon-/Fantasy-Gewalt“, falls der Fragebogen nachfragt) | Bevel, ein Würfel, stirbt an Stacheln und Sägen, er zerspringt in Pixel. Kein Blut, keine menschlichen oder tierischen Figuren, keine Darstellung von Verletzungen |
| Blut | Nein | |
| Sexualität / Nacktheit | Nein | |
| Kraftausdrücke | Nein | Mephis Sprüche sind Nerd-Witze, keine Flüche |
| Drogen, Alkohol, Tabak | Nein | |
| Glücksspiel / simuliertes Glücksspiel | **Nein** | „Croupier“ und „Karten“ sind nur Motiv: es gibt keinen Einsatz, keine Gewinnchance, keine Zufallsbelohnung. Die Teufelskarten sind ein Sammelalbum ohne Kauf |
| Furcht erregende Inhalte | Nein, höchstens „leicht“ | Teufel-Maskottchen als Comicfigur (rot, Hörner, grinst). Falls der Fragebogen Horror/Okkultes abfragt: es ist ein humorvoller Cartoon-Teufel ohne Schockeffekte |
| Nutzerinteraktion / Chat | Nein | |
| Teilen von Standort | Nein | |
| Digitale Käufe | Nein | |
| Kinder-/Jugendinhalte im Web | Nein | kein Browser, kein Internet |

Erwartetes Ergebnis: USK 0 oder 6, PEGI 3 oder 7, ESRB Everyone (10+, falls „Fantasy-Gewalt“ angekreuzt wird). Die Angaben müssen wahrheitsgemäß sein; im Zweifel lieber die höhere Stufe wählen. Wegen des Teufelsmotivs kann ein Bewerter „Okkultismus/Horror-Themen“ anmerken; das Spiel ist bewusst komödiantisch.

### 4. Zielgruppe (Entscheidung beim Entwickler)

Empfehlung: **13+ (oder 16+ / 18+) als Zielgruppe wählen, nicht Kinder unter 13**, und „App spricht nicht gezielt Kinder an“ bestätigen.

Gründe:
- Der Humor (Nerd-Witze, Kernel Panic, sudo, Teufel) und der hohe Schwierigkeitsgrad (Troll-Platformer, Dauer-Tod) zielen auf Jugendliche und Erwachsene.
- Wählst du Kinder (unter 13) als Zielgruppe oder mit, gilt die Richtlinie „Families“: zusätzliche Prüfung, Pflicht zu geprüften Werbe-SDKs (hier unkritisch, weil keine Ads), strengere Datenschutzvorgaben und eine Erwähnung im Store-Eintrag. Die App würde das technisch erfüllen, aber der Aufwand und die Prüfungen lohnen sich für dieses Spiel nicht.
- Die Zielgruppe muss zum Inhalt passen: Bunte Pixel-Grafik und ein Cartoon-Teufel sind nicht „zwingend kinderanziehend“, trotzdem sollte die Beschreibung (wie hier) keine Kinder ansprechen.

Wenn du bewusst auch Kinder erreichen willst, die Altersgruppen „6 bis 8“ usw. ankreuzen und die Families-Richtlinie durchgehen; das ist möglich, aber eine eigene Entscheidung.

### 5. Store-Präsenz

- [ ] **App-Kategorie:** App-Typ „Spiel“. Kategorie **Arcade** (passt zu Platformer mit kurzen Runden) oder **Action**. Empfehlung: Arcade.
- [ ] **Tags:** max. 5, zum Beispiel Platformer, Arcade, Pixel-Art, Schwierig/Rage-Game, Retro (nur was die Console anbietet, keine Stichwortlisten im Text).
- [ ] **Kontaktdaten:** E-Mail-Adresse (öffentlich sichtbar, Pflicht), optional Website und Telefon. Die Adresse, die auch in der Datenschutzerklärung steht, verwenden.
- [ ] **Texte:** Dateien aus `listings/` eintragen (de-DE als Standard). Keine Preisangaben, keine Superlative („beste“, „Nr. 1“), kein GROSSBUCHSTABEN-Spam, keine Stichwortlisten. Die vorhandenen Texte halten das ein; nach jeder Änderung selbst gegenlesen.
- [ ] **Grafiken:** Symbol, Funktionsgrafik, mindestens 2, besser alle 8 Smartphone-Screenshots hochladen.
- [ ] **Preis:** kostenlos, keine In-App-Käufe. Die Preisangabe steht in Play, nicht im Beschreibungstext.
- [ ] **Länder:** nach Wunsch; die App braucht keine regionalen Zusatzangaben.

### 6. Vor dem ersten Release

- [ ] Erster signierter AAB manuell in der Play Console hochladen (siehe Haupt-README, Abschnitt Test-Releases), danach übernimmt `publish_play.py`.
- [ ] Geschlossener Test: laut aktuellen Play-Regeln für neue persönliche Entwicklerkonten mindestens 12 Tester über 14 Tage, bevor die Produktion freigeschaltet wird. Bitte in der Console prüfen, ob das für dein Konto gilt.
- [ ] Versionshinweise für 0.6.0 aus `release-notes/` einfügen.
- [ ] Die Zielgruppen-, Datensicherheits- und Werbeangaben einmal bestätigen lassen (Status „Bereit zur Überprüfung“).

## Facts zum Abgleich

| Punkt | Stand im Code |
|---|---|
| Paketname | `com.robinrehbein.beveldevil` |
| Berechtigungen | keine |
| Werbung / In-App-Käufe | keine, keine Bibliotheken dafür |
| Sensoren | Beschleunigung/Schwerkraft (Neigen, Schütteln), optional abschaltbar |
| Speicherung | lokale SharedPreferences (Fortschritt, Einstellungen) |
| Ausrichtung | Querformat (`sensorLandscape`) |
| Sprachen | Deutsch und Englisch, nach Gerätesprache |
| Inhalt | 96 Level in zwei Welten, Welt 3 („Platine“) als gesperrter „Bald“-Reiter |
