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

- **BERECHNET**: rechnerisch eindeutig (Zucker, Salz, Wasser, verdünnte
  Speiseessige aus dem Anhang-XIV-Faktor für organische Säuren).
- **USDA**: für die vier Rohgemüse (Gurke, Paprika, Chili, Zwiebel) – laut
  Websuche über USDA FoodData Central, aber in dieser Sitzung nicht direkt an
  der Primärquelle fdc.nal.usda.gov gegengeprüft (Netzwerkzugriff war in der
  Entwicklungsumgebung blockiert).
- **WEBRECHERCHE**: Balsamico-Essig, Ahornsirup, Gelierzucker, Rotwein,
  Senfsaat gelb/braun – Werte stammen aus Verbraucher-Nährwertportalen
  (Fddb, Yazio, Wikifit u. ä.), **nicht amtlich**, teils widersprüchlich
  zwischen den Quellen. Vor jeder echten Deklaration ersetzen.
- **UNBEKANNT** (Speck, Pektin): bewusst ohne Werte angelegt, weil die
  gefundenen Angaben zu stark streuten bzw. keine Recherche stattfand. Die App
  markiert solche Rohstoffe automatisch als unvollständig statt einen falschen
  Wert vorzutäuschen.

**Empfehlung für den produktiven Einsatz:** Rohstoffe schrittweise durch
Herstelleretiketten der tatsächlich eingekauften Handelsware ersetzen (am
genauesten und am leichtesten als Nachweis dokumentierbar), ÖNWT (oenwt.at)
für Rohgemüse ohne eigenes Etikett heranziehen, und die Anhang-XIV-Faktoren
(Kohlenhydrate 17 kJ/g, Fett 37 kJ/g, Eiweiß 17 kJ/g, Ballaststoffe 8 kJ/g,
Alkohol 29 kJ/g, organische Säuren 13 kJ/g) einmal gegen den Verordnungstext
verifizieren, bevor sie für eine echte Kennzeichnung verwendet werden.

## Architektur

- Kotlin, Jetpack Compose, Room (lokale SQLite-DB, offline-first)
- `data/`: Room-Entitäten (`Rohstoff`, `Rezeptur`, `RezepturZutat`), DAOs,
  `AppDatabase` (Seeding beim ersten Start), `SeedData`
- `domain/`: `NaehrwertBerechnung` (Aggregationslogik), `NaehrwertErgebnis`,
  `NaehrwertDeklarationFormatter` (Textausgabe in Anhang-XV-Reihenfolge)
- `ui/`: `NaehrwertViewModel`, drei Compose-Screens (Zutaten-Eingabe,
  Rohstoffliste, Ergebnis)

## APK bauen

**Ohne eigene Installation (empfohlen):** Der Workflow
`.github/workflows/android-build.yml` baut bei jedem Push in diesen Ordner
automatisch eine Debug-APK und führt die Unit-Tests aus (GitHub-Runner haben
normalen Internetzugang, das Netzwerkproblem der Entwicklungssitzung entfällt
dort). Die fertige APK liegt danach unter GitHub → Reiter **Actions** → den
jeweiligen Lauf öffnen → Abschnitt **Artifacts** → `naehrwertrechner-debug-apk`
herunterladen (ZIP mit der `.apk` darin). Schlägt der Build fehl, steht der
Fehler im Log des jeweiligen Schritts.

**Lokal mit Android Studio:** Projekt-Ordner `android/naehrwertrechner/` in
Android Studio öffnen, Gradle-Sync abwarten, dann
`Build → Build Bundle(s)/APK(s) → Build APK(s)`. Die APK landet in
`app/build/outputs/apk/debug/app-debug.apk`.

## Bekannte Einschränkung dieser Erstanlage

Die Entwicklungsumgebung, in der dieses Grundgerüst erstellt wurde, hatte
**keinen Netzwerkzugriff auf dl.google.com/Google-Maven-Repository** und kein
installiertes Android SDK. Der Gradle-Wrapper (`gradlew`, Version 8.7) wurde
lokal erzeugt und funktioniert, aber **ein echter Build (`./gradlew
assembleDebug`) und die Unit-Tests unter `app/src/test` wurden in dieser
Sitzung nicht ausgeführt** – der Code wurde sorgfältig, aber ohne
Compiler-Verifikation geschrieben. Bitte beim ersten Öffnen in Android Studio
(Gradle-Sync mit normalem Internetzugang) auf Fehler prüfen, insbesondere bei
den Compose-Import-Pfaden und der KSP/Room-Codegenerierung.
