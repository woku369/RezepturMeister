# Nährwertrechner (Android)

Android-App zur Nährwertberechnung von Lebensmittelrezepturen durch Eingabe von
Zutaten und Mengen. Erste Zielgruppen: eingelegtes Gemüse (Gurken, Paprika,
Chili) in Essig-Zucker-Salz-Lake, Gemüsezubereitungen (Zwiebel, Chili, Paprika,
Speck, Essig, Zucker, Ahornsirup, Gelierzucker, Pektin, Rotwein) und Senf.

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
  `Rezeptur`, `RezepturZutat`), DAOs, `AppDatabase` (Migration 1→2), `SeedData`
  – `RohstoffDao.syncSeedDaten()` gleicht bei **jedem** App-Start die
  Standard-Rohstoffe mit `SeedData` ab: neue Namen werden ergänzt, bereits
  vorhandene Namen auf den aktuellen `SeedData`-Stand aktualisiert (Id bleibt
  erhalten, bestehende Rezeptur-Verknüpfungen bleiben also gültig). Das ist
  bewusst so gewählt, weil es aktuell **keine Bearbeiten-Ansicht** für
  Rohstoffe gibt – `SeedData` gilt also als alleinige Wahrheit. Sobald eine
  manuelle Bearbeitung eingeführt wird, muss diese Synchronisation angepasst
  werden (z. B. Flag `vomNutzerBearbeitet`), damit sie keine Nutzerkorrekturen
  mehr überschreibt – siehe Kommentar in `RohstoffDao.kt`.
- `domain/`: `NaehrwertBerechnung` (Aggregationslogik), `NaehrwertErgebnis`,
  `NaehrwertDeklarationFormatter` (Textausgabe in Anhang-XV-Reihenfolge)
- `ui/`: `NaehrwertViewModel`, drei Compose-Screens (Zutaten-Eingabe,
  Rohstoffliste, Ergebnis)
- `export/`: `XlsxExporter` – Button "Als Excel (.xlsx) exportieren" im
  Ergebnis-Screen erzeugt eine Arbeitsmappe (Blatt 1: Nährwertdeklaration in
  Anhang-XV-Reihenfolge, Blatt 2: Zutatenliste) und öffnet den
  Android-Teilen-Dialog (Mail, Drive, lokal speichern, …) über einen
  `FileProvider`. Nutzt die schlanke Bibliothek FastExcel statt Apache POI
  (POI ist auf Android für diesen einfachen Schreibfall unnötig schwer und
  hat bekannte Kompatibilitätsprobleme).

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

## Datenbank-Update (Schema-Version 2)

Ab Schema-Version 2 hat `Rohstoff.name` einen eindeutigen Index; die
Migration 1→2 legt ihn per SQL an. Wer die App vor diesem Update bereits
installiert hatte: beim nächsten Start wird automatisch migriert, bestehende
Rohstoffe/Rezepturen bleiben erhalten.
