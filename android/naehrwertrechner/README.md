# Nährwertrechner (Android)

Android-App zur Nährwertberechnung von Lebensmittelrezepturen durch Eingabe von
Zutaten und Mengen. Erste Zielgruppen: eingelegtes Gemüse (Gurken, Paprika,
Chili) in Essig-Zucker-Salz-Lake, Gemüsezubereitungen (Zwiebel, Chili, Paprika,
Speck, Essig, Zucker, Ahornsirup, Gelierzucker, Pektin, Rotwein), Senf und
(seit September 2026) auch (alkoholische) Getränke/Mazerate.

Eigenständiges Modul innerhalb des RezepturMeister-Repos, technisch getrennt
vom bestehenden WPF/.NET-Code (unterschiedlicher Stack, kein gemeinsamer
Build). Fachlich an dasselbe Nährwert-Datenmodell wie
`Models/Rohstoff.cs`/`Models/NaehrwertErgebnis.cs` angelehnt.

## Rechtlicher Rahmen

Anders als bei Spirituosen (>1,2 % vol) gilt für diese Produkte **keine
Ausnahme**: volle Nährwertdeklaration ist nach Art. 9 Abs. 1 lit. l LMIV
(VO (EU) 1169/2011) Pflicht. Die Berechnung folgt Art. 31 LMIV (Variante
"Berechnung anhand bekannter/durchschnittlicher Werte der Zutaten"), die
Energie-Umrechnung Anhang XIV, die Darstellungsreihenfolge Anhang XV.

**Wichtiger fachlicher Punkt:** Bei eingelegtem Gemüse in Lake findet über die
Reifezeit Diffusion statt (Zucker/Salz wandern ins Gemüse, Wasser in die
Lake). Die einfache Zutaten-Additionsrechnung dieser App ist für das
**Gesamtgebinde "wie verkauft" (Gemüse + Lake)** korrekt, weil die Gesamtmasse
erhalten bleibt. Für ein separat deklariertes **Abtropfgewicht** reicht sie
NICHT aus – das wäre eine spätere Erweiterung (Diffusionsmodell oder
Laboranalyse des Enderzeugnisses).

**Energieberechnung (September 2026 korrigiert – wichtig für das
Grundverständnis der App):** Der Brennwert einer Rezeptur wird **immer** nach
Anhang XIV aus den aggregierten Fett-/Kohlenhydrat-/Ballaststoff-/Eiweiß-/
Alkohol-/Säurewerten der gesamten Rezeptur neu berechnet – **nicht** durch
Aufsummieren der einzelnen `Rohstoff.energieKj`/`energieKcal`-Werte. Grund:
reale Lebensmitteldatenbanken (z. B. USDA) geben für zusammengesetzte
Lebensmittel oft einen eigenständig gemessenen Energiewert an, der von der
Anhang-XIV-Rückrechnung aus den übrigen Nährwerten abweicht (z. B. Chili:
USDA nennt 166 kJ, die Rückrechnung aus USDAs eigenen Fett-/Kohlenhydrat-/
Ballaststoff-/Eiweißwerten ergibt 208,7 kJ). Für eine LMIV-konforme
Deklaration ist ausschließlich die Rückrechnung zulässig – genau eine solche
Abweichung war Kern einer echten amtlichen Beanstandung (Amt der Kärntner
Landesregierung, Zahl 05-LMA-BBM-469/2025-279 vom 27.08.2026) gegen ein
Etikett mit exakt diesem Fehler. `Rohstoff.energieKj`/`energieKcal` sind
seither reine Referenz-/Anzeigewerte (z. B. für die Rohstoffliste); die
Rezeptur-Gesamtenergie hängt nicht mehr von ihnen ab. Details und
Herleitung: Kommentar in `domain/NaehrwertBerechnung.kt`.

Zwei zusätzliche Rohstoff-Felder tragen seither zur Energieformel bei (beide
`Double`, Default 0.0, siehe `data/Rohstoff.kt`):
- `organischeSaeuren` (g/100 g) – für Essig/saure Zutaten; ohne dieses Feld
  würde die Energie von reinem Speiseessig in der Neuberechnung auf 0 fallen.
- `alkoholGramm` (g/100 g) – für alkoholhaltige Zutaten wie Rotwein; getrennt
  von `alkoholGehaltVol` (%vol, nur Kennzeichnung nach Art. 28 LMIV). Ohne
  dieses Feld würde der mit Abstand größte Energieanteil von Wein rechnerisch
  verschwinden.

## Datenqualität der Start-Rohstoffdatenbank – bitte vor Produktivnutzung lesen

`data/SeedData.kt` enthält eine erste Rohstoffliste, damit die App beim ersten
Start nicht leer ist. Diese Werte wurden per Websuche (Stand September 2026)
zusammengestellt, **nicht** durch direkten Abgleich mit ÖNWT/BLS oder mit
Herstelleretiketten der tatsächlich eingekauften Waren. Jeder Datensatz trägt
ein Pflichtfeld `quelle` + `quelleHinweis`:

- **BERECHNET**: rechnerisch/definitorisch eindeutig – Zucker, Salz, Wasser,
  verdünnte Speiseessige aus dem Anhang-XIV-Faktor für organische Säuren
  (13 kJ/g), reines Pektin (E440) als ~100 % löslicher Ballaststoff über den
  Anhang-XIV-Ballaststofffaktor (8 kJ/g) statt einer mit Traubenzucker
  gestreckten Handelsware. "JK"-Kräutermischung (Rezeptur "Datteltomaten
  geschmort") auf Nutzerwunsch bewusst auf 0 gesetzt, da laut Nutzer eine
  Mischung getrockneter mediterraner Kräuter ohne verfügbare Herstellerdaten
  und mit geringem Mengenanteil (~1,3 %) – das ist eine bewusste
  Vereinfachung, KEIN tatsächlicher Nährwert von Null.
- **USDA**: für Rohgemüse (Gurke, Paprika, Chili, Zwiebel, Tomate) und
  Sonnenblumen-/Olivenöl – laut Websuche über USDA FoodData Central, aber
  nicht direkt an der Primärquelle fdc.nal.usda.gov gegengeprüft
  (Netzwerkzugriff auf dl.google.com/USDA war in der Entwicklungsumgebung
  blockiert). Einzelne Felder (z. B. Salz bei Paprika/Chili) waren in den
  Suchtreffern nicht enthalten und wurden bewusst NULL belassen statt geraten.
- **HERSTELLERETIKETT**: Ahornsirup – auf das konkret verwendete Produkt
  (Spar Natur*pur Bio-Ahornsirup) umgestellt; Wert stammt aus mehreren
  übereinstimmenden Verbraucherportalen, nicht direkt von spar.at geprüft.
- **WEBRECHERCHE**: Balsamico-Essig, Gelierzucker 2:1/3:1, Rotwein, Senfsaat
  gelb/braun, Speck (roh und ausgelassen), Rosmarin/Thymian/Zimt/Kreuzkümmel/
  Schwarzkümmel – Werte stammen aus Verbraucher-Nährwertportalen (Fddb,
  Yazio, Wikifit u. ä.), **nicht amtlich**. Bei Speck wichen mehrere
  unabhängige Treffer um bis zu ±25 kcal/100g voneinander ab – hier wurde
  bewusst ein gekennzeichneter **Mittelwert** eingetragen (kein Einzelwert
  als Wahrheit ausgegeben), siehe `quelleHinweis` im Code. Für einen
  generischen Pauschalwert "getrocknete Gewürze" gilt: bewusst NICHT
  angelegt, da die Streuung zwischen den einzelnen Gewürzen zu groß für einen
  seriösen Durchschnitt ist (Fett z. B. 1,2–22,3 g/100 g). Vor jeder echten
  Deklaration durch Herstelleretikett ersetzen.

**Empfehlung für den produktiven Einsatz:** Rohstoffe schrittweise durch
Herstelleretiketten der tatsächlich eingekauften Handelsware ersetzen (am
genauesten und am leichtesten als Nachweis dokumentierbar), ÖNWT (oenwt.at)
für Rohgemüse ohne eigenes Etikett heranziehen, und die Anhang-XIV-Faktoren
(Kohlenhydrate 17 kJ/g, Fett 37 kJ/g, Eiweiß 17 kJ/g, Ballaststoffe 8 kJ/g,
Alkohol 29 kJ/g, organische Säuren 13 kJ/g) einmal gegen den Verordnungstext
verifizieren, bevor sie für eine echte Kennzeichnung verwendet werden.

## Architektur

- Kotlin, Jetpack Compose, Room (lokale SQLite-DB, offline-first)
- `data/`: Room-Entitäten (`Rohstoff` mit eindeutigem Index auf `name`,
  `Rezeptur`, `RezepturZutat`), DAOs, `AppDatabase` (Migration 1→2→3→4),
  `SeedData` – `RohstoffDao.syncSeedDaten()` gleicht bei **jedem** App-Start
  die Standard-Rohstoffe mit `SeedData` ab: neue Namen werden ergänzt, bereits
  vorhandene Namen auf den aktuellen `SeedData`-Stand aktualisiert (Id bleibt
  erhalten, bestehende Rezeptur-Verknüpfungen bleiben also gültig) – **außer**
  der Rohstoff wurde bereits über den Rohstoff-Editor bearbeitet
  (`vomNutzerBearbeitet = true`, Schema-Version 4): dann fasst die
  Synchronisation ihn nicht an, damit eigene Korrekturen/Ergänzungen nicht bei
  jedem Start wieder überschrieben werden.
- `domain/`: `NaehrwertBerechnung` (Aggregationslogik), `NaehrwertErgebnis`,
  `NaehrwertDeklarationFormatter` (Textausgabe in Anhang-XV-Reihenfolge)
- `ocr/`: `EtikettParser` (reine, Android-unabhängige Textverarbeitung – liest
  Brennwert/Fett/Kohlenhydrate/Ballaststoffe/Eiweiß/Salz per Regex aus dem
  OCR-Rohtext eines fotografierten Etiketts, toleriert typische OCR-Fehler wie
  verlorene Umlaute oder Tausenderpunkte, direkt per JVM-Unit-Test geprüft) und
  `EtikettScanner` (Android/ML-Kit-Anbindung: on-device Texterkennung, kein
  Upload)
- `ui/`: `NaehrwertViewModel`, vier Compose-Screens (Zutaten-Eingabe,
  Rohstoffliste, Rohstoff-Editor, Ergebnis)
- `export/`: `XlsxExporter` – Button "Als Excel (.xlsx) exportieren" im
  Ergebnis-Screen erzeugt eine Arbeitsmappe mit drei Blättern (1:
  Nährwertdeklaration in Anhang-XV-Reihenfolge, 2: Berechnungsschlüssel –
  transparente Herleitung des Brennwerts nach Anhang XIV, Faktor × Menge =
  Beitrag je Nährstoff, Summenzeile – für eine Behördenprüfung ohne
  Rückfrage nachvollziehbar, 3: Zutatenliste) und öffnet den
  Android-Teilen-Dialog (Mail, Drive, lokal speichern, …) über einen
  `FileProvider`. Nutzt die schlanke Bibliothek FastExcel statt Apache POI
  (POI ist auf Android für diesen einfachen Schreibfall unnötig schwer und
  hat bekannte Kompatibilitätsprobleme). Die Anhang-XIV-Faktoren sind als
  öffentliche Konstanten in `NaehrwertBerechnung` definiert und werden von
  hier nur referenziert, nicht dupliziert.

## Rohstoff-Editor & Foto-Etikett-Erkennung (September 2026)

Im Tab "Rohstoffe" lässt sich jetzt ein neuer Rohstoff anlegen oder ein
bestehender bearbeiten (vorher nur eine reine Anzeigeliste, `SeedData` galt
als alleinige Quelle). Der Editor bietet zusätzlich "Foto aufnehmen" bzw.
"Aus Galerie wählen", um die Nährwerttabelle eines Zutatenetiketts per
On-Device-Texterkennung (Google ML Kit, kein Upload, keine
Internetverbindung zur Laufzeit) auszulesen.

**Wichtig – das ist ein Vorschlag, keine automatische Übernahme:** Die
erkannten Werte füllen lediglich die (weiterhin frei editierbaren) Textfelder
vor. Erst der explizite Klick auf "Speichern" schreibt den Rohstoff in die
Datenbank; der erkannte Rohtext wird zur Kontrolle mit angezeigt. Etiketten
unterscheiden sich stark in Layout und Formulierung, und OCR macht
Lesefehler (v. a. bei Kommas/Punkten und Umlauten) – jeder Wert muss vor dem
Speichern gegen das Etikett geprüft werden. Der Parser (`EtikettParser`) ist
bewusst als reine, Android-unabhängige Funktion gebaut und über
`EtikettParserTest` mit realistischen (auch fehlerhaften) OCR-Beispieltexten
abgesichert.

Ein über den Editor angelegter/geänderter Rohstoff wird als
`vomNutzerBearbeitet = true` markiert und dadurch von der
`SeedData`-Synchronisation beim nächsten App-Start nie mehr überschrieben
(Schema-Version 4, siehe unten) – das damit verbundene, seit Projektbeginn
offene Problem (siehe vorheriger Absatz zu `syncSeedDaten()`) ist damit
gelöst.

## Getränke, Mazerate & Bezugsgröße pro 100 ml (September 2026)

Drei zusammenhängende Erweiterungen für (alkoholische) Getränke:

**Zutateneingabe in g oder ml.** Beim Hinzufügen einer Zutat im Tab "Zutaten"
kann die Menge wahlweise in Gramm oder Millilitern eingegeben werden. Die
Umrechnung erfolgt sofort über die Dichte des Rohstoffs (`Rohstoff.dichte`,
g/ml) – intern rechnet `NaehrwertBerechnung` ausschließlich mit Gramm
(`ZutatMenge.mengeGramm`), die ursprünglich eingegebene Menge/Einheit bleibt
zusätzlich für die Anzeige erhalten (`eingegebeneMenge`/`eingegebeneEinheit`).
Ist für einen Rohstoff keine Dichte hinterlegt, wird die ml-Eingabe mit einer
Fehlermeldung blockiert statt stillschweigend eine Dichte zu unterstellen.

**Bezugsgröße pro 100 g oder pro 100 ml.** Im Tab "Zutaten" wählbar (Art. 32
LMIV: bei Flüssigkeiten/Getränken ist pro 100 ml üblich). Bei "pro 100 ml"
verlangt die App zusätzlich das **gemessene Gesamtvolumen des fertigen
Ansatzes** – bewusst keine Summe der eingegebenen Zutatenvolumina, weil sich
Alkohol und Wasser beim Mischen nicht additiv verhalten (das Gesamtvolumen
einer Mischung ist kleiner als die Summe der Einzelvolumina). Dieselbe Logik
wie beim Abtropfgewicht bei eingelegtem Gemüse: gemessen statt
zurückgerechnet. `NaehrwertBerechnung.berechne()` wirft eine Exception, wenn
"pro 100 ml" ohne Gesamtvolumen aufgerufen wird – die UI fängt das ab, indem
sie in diesem Zustand einfach kein Ergebnis anzeigt statt abzustürzen.

**Alkoholgehalt (%vol) → Alkohol (g) für Mazerate ohne eigene Nährwertdaten.**
Im Rohstoff-Editor lässt sich für Zutaten wie ein Kräutermazerat (z. B. 53 %
vol, keine eigene Nährwerttabelle) der Alkoholgehalt in %vol eintragen; ein
Button leitet daraus mit `NaehrwertBerechnung.alkoholGrammAusVol()` den für
die Energieformel nötigen Wert in Gramm/100g her (%vol × Ethanoldichte
0,789 g/ml ÷ Produktdichte – dieselbe Rechnung, die bisher händisch für
Rotwein in `SeedData.kt` dokumentiert war). Wie bei der Foto-Etikett-
Erkennung ist das ein Vorschlag in ein weiterhin editierbares Feld, keine
automatische Übernahme.

**Vernachlässigbar-Checkbox** (`Rohstoff.vernachlaessigbar`, Schema-Version
5). Formalisiert das bisher nur per Freitext in `quelleHinweis` dokumentierte
Muster ("Wert unbekannt, aber Einsatzmenge zu gering für eine seriöse
Schätzung – auf Wunsch vernachlässigt"). `istVollstaendig()` bleibt eine
objektive Aussage über die Datenlage; `erfordertWarnhinweis()` unterdrückt
die "unvollständig"-Warnung nur, wenn die Lücke bewusst als vernachlässigbar
markiert wurde – akzeptierte Lücken werden weiterhin transparent ausgewiesen
(`NaehrwertErgebnis.alsVernachlaessigbarAkzeptiert`), nicht stillschweigend
versteckt. Damit lässt sich z. B. ein Kräutermazerat mit bekanntem
Alkoholgehalt, aber unbekannten sonstigen Nährwerten, korrekt abbilden: der
Alkohol zählt weiter zur Energie, die restlichen unbekannten Felder blockieren
die Deklaration nicht mehr.

## APK bauen

**Ohne eigene Installation (empfohlen):** Der Workflow
`.github/workflows/android-build.yml` baut bei jedem Push in diesen Ordner
automatisch eine Debug-APK und führt die Unit-Tests aus (GitHub-Runner haben
normalen Internetzugang, das Netzwerkproblem der Entwicklungssitzung entfällt
dort). Die fertige APK liegt danach unter GitHub → Reiter **Actions** → den
jeweiligen Lauf öffnen → Abschnitt **Artifacts** → `naehrwertrechner-debug-apk`
herunterladen (ZIP mit der `.apk` darin). Schlägt der Build fehl, steht der
Fehler im Log des jeweiligen Schritts.

**Update über eine bereits installierte Version:** Alle Debug-Builds (CI wie
lokal) werden mit dem fest eingecheckten `app/debug.keystore` signiert, damit
sich eine neue APK als normales Update installieren lässt. Ausnahme: APKs aus
den allerersten drei CI-Läufen (vor diesem Fix) hatten je einen zufälligen
Signierschlüssel – kommt beim Installieren die Meldung *"Paket steht in
Konflikt mit einem bestehenden Paket"*, einmalig die alte App deinstallieren
(dabei gehen lokal gespeicherte Rezepturen/Rohstoffe verloren) und die neue
APK frisch installieren. Ab jetzt sollte das nicht mehr nötig sein.

**Lokal mit Android Studio:** Projekt-Ordner `android/naehrwertrechner/` in
Android Studio öffnen, Gradle-Sync abwarten, dann
`Build → Build Bundle(s)/APK(s) → Build APK(s)`. Die APK landet in
`app/build/outputs/apk/debug/app-debug.apk`.

## Build-Status

Seit dem GitHub-Actions-Workflow (`.github/workflows/android-build.yml`) ist
der Build compiler-verifiziert (nicht nur in Android Studio – die
Entwicklungsumgebung dieser Sitzung selbst hat keinen Netzwerkzugriff auf das
Google-Maven-Repository und kann nicht lokal bauen/testen). Aktueller Stand:
grün, siehe jeweils neuester Lauf unter GitHub → Actions.

## Datenbank-Updates (Schema-Versionen 2–5)

- **Version 2:** `Rohstoff.name` erhält einen eindeutigen Index (Migration 1→2).
- **Version 3:** neue Spalten `alkoholGramm`/`organischeSaeuren` für die
  Anhang-XIV-Energieformel (Migration 2→3).
- **Version 4:** neue Spalte `vomNutzerBearbeitet` (Default `false`/0), damit
  über den Rohstoff-Editor angelegte/geänderte Rohstoffe von der
  `SeedData`-Synchronisation nicht mehr überschrieben werden (Migration 3→4).
- **Version 5:** neue Spalte `vernachlaessigbar` (Default `false`/0) – siehe
  Abschnitt "Getränke, Mazerate & Bezugsgröße pro 100 ml" (Migration 4→5).

Wer die App vor einem dieser Updates bereits installiert hatte: Room führt
beim nächsten Start alle nötigen Migrationen automatisch aus, bestehende
Rohstoffe/Rezepturen bleiben erhalten.
