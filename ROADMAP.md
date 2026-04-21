# Roadmap für RezepturMeister

## Phase 1: Grundstruktur (Abgeschlossen)
- [x] Projektstruktur mit WPF und MVVM erstellt
- [x] Datenbankmodell mit Entity Framework definiert
- [x] Basis-Models: `Rohstoff`, `Rezeptur`, `Zutat`
- [x] `AppDbContext` mit SQLite (EF Core 8)
- [x] `RohstoffService` und `RezepturService` (CRUD)
- [x] `RohstoffViewModel` und `RezepturViewModel` (CommunityToolkit.Mvvm)
- [x] Converters: `DecimalConverter`, `NullToVisibilityConverter`, `PercentageConverter`
- [x] `RezepturView.xaml` vollständig verdrahtet (Editor, Zutaten-DataGrid, Berechnungen)

## Phase 2: Kernfunktionalität (Abgeschlossen)
- [x] CRUD-Operationen für Rohstoffe (Add/Edit/Delete mit Commands)
- [x] `RohstoffView.xaml` vollständig verdrahtet (Commands + DataBindings + Spalten)
- [x] Rezepturerstellung mit Zutatenverwaltung
- [x] Berechnungen: Gesamtgewicht, Prozentanteile, Alkohol
- [x] Versionierung von Rezepturen (`GenerateNextVersion`)
- [x] `CancelEditCommand`, `GesamtMenge`, `GesamtAlkohol` in `RezepturViewModel`
- [x] `RezepturView`-Tab in `MainWindow.xaml` ergänzt
- [x] DB-Pfad auf `AppContext.BaseDirectory` umgestellt (portabel/Netzlaufwerk)

## Phase 3: Erweiterte Funktionen (Abgeschlossen)
- [x] `ExportService` erstellt (`Services/ExportService.cs`)
- [x] PDF-Export: Rohstoffliste und Rezeptur (PdfSharpCore, DIN A4, Seitenumbruch)
- [x] XLSX-Export: Rohstoffliste und Rezeptur (ClosedXML, Spaltenbreite automatisch)
- [x] Druckfunktion: Rezeptur via `FlowDocument` + `PrintDialog`
- [x] Export-Commands in `RohstoffViewModel` (`ExportPdfCommand`, `ExportXlsxCommand`)
- [x] Export-Commands in `RezepturViewModel` (`ExportPdfCommand`, `ExportXlsxCommand`, `DruckenCommand`)
- [x] Export-Buttons in `RohstoffView.xaml` und `RezepturView.xaml`
- [ ] Suchfunktion / Filter für Rohstoffe
- [ ] Erweiterbare Eigenschaften für Rohstoffe (JSON-Feld vorhanden, kein UI)

## Phase 4: Skills & Projektinfrastruktur (Abgeschlossen)
- [x] Skill `wpf-mvvm` erstellt
- [x] Skill `ef-sqlite` erstellt
- [x] Skill `testing` erstellt
- [x] Skill `export-print` erstellt
- [x] Skill `meister-suite` erstellt (Roadmap-Autopflege + Projektworkflow)

## Phase 5: UI/UX und Tests (In Arbeit)
- [x] Suchfunktion / Filter für Rohstoffe (`ICollectionView`, Live-Filter nach Name/Kategorie)
- [x] Eingabevalidierung in `RohstoffViewModel` (Name leer, Dichte ≤ 0)
- [x] Eingabevalidierung in `RezepturViewModel.SaveRezeptur` (Nummer leer, keine Zutaten)
- [x] Suchfunktion / Filter für Rezepturen (`ICollectionView`, Live-Filter nach Nummer/Charge/Bemerkung)
- [x] `AppDbContext` um Options-Konstruktor für Tests erweitert (mit `IsConfigured`-Guard)
- [x] Test-Projekt erstellt (`RezepturMeister.Tests`, xUnit, net8.0-windows, InMemory SQLite)
- [x] Unit-Tests für `RohstoffService` (6 Tests: Add, GetAll, GetById, Update, Delete)
- [x] Unit-Tests für `RezepturService` (6 Tests: Add, GetAll+Zutaten, Delete, GenerateNextVersion)
- [x] Unit-Tests für Berechnungslogik (5 Tests: Gesamtgewicht, ml-Umrechnung, Prozentanteile, Version)
- [x] `AddRohstoff`-Command korrigiert (fehlende `newRohstoff`-Definition ergänzt)
- [x] `EditRohstoff`-Command korrigiert (doppelte Calls + stray `}` entfernt)
- [x] Dezentes Design implementiert (`Themes/AppTheme.xaml`: Farbpalette, Button/DataGrid/Tab-Styles, Hover-Effekte)
- [x] App-Header in `MainWindow.xaml` (Titel + Untertitel auf dunkelblauem Banner)
- [x] `DangerButton`-Style für Löschen-Buttons (rot)
- [x] `SecondaryButton`-Style für Abbrechen/Export/Sekundar-Aktionen

## Phase 5: UI/UX und Tests (Abgeschlossen)

## Phase 6: Dokumentation und Release (Abgeschlossen)
- [x] README.md vollständig aktualisiert (Funktionen, Technologien, Struktur, Roadmap-Häkchen)
- [x] Release-Build-Befehl dokumentiert (`dotnet publish -c Release -r win-x64 --self-contained`)
- [x] Test-Ausführungsbefehl dokumentiert (`dotnet test`)
- [x] `dotnet publish` Release-Build: win-x64 self-contained EXE erstellt
- [ ] Benutzerhandbuch

## Phase 7: Praxisfeedback – Erweiterungen (Abgeschlossen)
- [x] **Button-Farben** softer: PrimaryBrush `#4A7FAB`, AccentBrush `#3A8ED8` (war zu dunkel)
- [x] **Rohstoff – Preis**: neues Feld `decimal Preis` (€ pro kg oder l) in Model + DataGrid-Spalte
- [x] **Rohstoff – Lieferant**: neues Feld `string Lieferant` in Model + DataGrid-Spalte
- [x] **Rohstoff – Datenblatt**: Pfad-Feld + Buttons „Datenblatt verknüpfen" / „Datenblatt öffnen" (PDF/XLSX via ShellExecute)
- [x] **Rezeptur – Name**: neues Feld `string Name`, Eingabefeld im Editor, Spalte in Listenansicht
- [x] **Rezeptur-Gesamtpreis**: `GesamtPreis` aus Rohstoffpreisen berechnet + Anzeige im Editor
- [x] **Import CSV**: `ImportCsvCommand` – Rezeptur aus CSV-Datei einlesen (Format: Schlüssel;Wert / Zutat;Menge;Einheit)
- [x] **Suche nach Namen**: Rezeptur-Filter auch nach `Name`-Feld
- [x] **DB-Schema-Migration**: `schema_version`-Datei neben DB, bei Versionsabweichung Backup + Neuanlage
- [x] Schemaversion 2 aktiv (`App.xaml.cs`)

## Phase 8: Meister-Suite CI (Abgeschlossen)
- [x] **CI SKILL.md** analysiert (`CI VS/.github/skills/meister-suite-ci/SKILL.md`)
- [x] **CI-Farben** in `Themes/AppTheme.xaml` übernommen:
  - `PrimaryBrush` `#2C5282` (Tiefblau, RezepturMeister-Markenfarbe)
  - `AccentBrush` `#5B8DD4` (aufgehellt, für Icons auf dunklem BG)
  - `BackgroundBrush` `#F8F6F2` (warmes Creme, MeisterBackground)
  - `BorderBrush` `#D4C9B8` (warmes Beige, MeisterBorder)
  - `TextPrimaryBrush` `#2C2C2C`, `TextSecondaryBrush` `#6B6B6B`
  - `RowAltBrush` `#F0EDE7`, `RowHoverBrush` `#E3DDD5`, `HeaderBrush` `#EAE6DF`
- [x] **CI CornerRadius 8** (MeisterRadius) auf alle Button-Styles gesetzt
- [x] **BrandFont** `EskapadeFraktur-Regular.ttf` als WPF-Resource eingebunden
- [x] **Wortmarke** in `MainWindow.xaml`: „Rezeptur" (weiß) + „Meister" (`#5B8DD4`) in EskapadeFraktur 24pt
- [x] **Schriftdatei** nach `Resources/Fonts/EskapadeFraktur-Regular.ttf` kopiert
- [x] **Bugfix** `RezepturViewModel.cs`: fehlende schließende `}` in `UpdateBerechnungen()` behoben

## Phase 9: Stabilisierung Rezeptur-Workflow (Abgeschlossen)
- [x] **Bugfix Rohstoffauswahl**: Zutaten-ComboBox in `RezepturView.xaml` auf stabile DB-Auswahl umgestellt (`SelectedValue` TwoWay, separate Spalte für manuelle Namen)
- [x] **Rohstoffliste-Aktualisierung**: `RezepturViewModel` lädt verfügbare Rohstoffe beim Anlegen/Bearbeiten von Rezepturen und beim Hinzufügen von Zutaten neu
- [x] **Validierung Rezepturname**: Speichern blockiert bei leerem `Name`; `NewRezeptur` setzt Default-Name "Neue Rezeptur"
- [x] **Build-Ausgabeordner**: `RezepturMeister.csproj` auf dedizierte Projektordner konfiguriert (`build/bin`, `build/obj`, `build/publish`)
- [x] **MSBuild-Fix**: Output-Properties in `Directory.Build.props` verschoben (`BaseOutputPath`, `BaseIntermediateOutputPath`, `MSBuildProjectExtensionsPath`) zur Vermeidung von Warnung `MSB3539`
- [x] **Fix doppelte Attribute (CS0579)**: `Directory.Build.props` erweitert um `DefaultItemExcludes` für altes `obj/**`, damit Legacy-Generates nicht mehr mit kompiliert werden
- [x] **Bereinigung WPF-Temp-Projekt**: verbliebene Datei `RezepturMeister_*_wpftmp.csproj` aus Projektwurzel entfernt
- [x] **Prominenter EXE-Ordner**: Build-Ausgabe auf kurzes Top-Level-Verzeichnis `out/` umgestellt (ohne TFM/RID-Unterordner)
- [x] **Direkter EXE-Pfad**: `OutputPath` auf `out/` gesetzt, damit `RezepturMeister.exe` ohne zusätzliche Unterordner liegt
- [x] **Warnungsbereinigung MainViewModel**: ungenutzte, nicht initialisierte Felder entfernt (`CS8618`, `CS0169`)

## Phase 10: UI-Polish & Stabilisierung (Abgeschlossen)
- [x] **Bugfix Button-Lesbarkeit**: `TextElement.Foreground="{TemplateBinding Foreground}"` auf `ContentPresenter` in allen Button-Templates — globaler `TextBlock`-Style hatte weißen Foreground überschrieben
- [x] **Bugfix Schema-Migration**: `EnsureDatabase` auf `ALTER TABLE` umgestellt statt DB löschen — Daten bleiben bei Schemaupdate erhalten
- [x] **Bugfix XColor.FromArgb**: `ExportService.cs` korrigiert (PdfSharpCore kennt kein `FromRgb`)
- [x] **Logo EskapadeFraktur**: Wortmarke „Rezeptur" + „Meister" in `EskapadeFraktur-Regular.ttf` (28pt)
- [x] **`BrandFont`-Resource**: auf `pack://application:,,,/Resources/Fonts/#Eskapade Fraktur` gesetzt
- [x] **Button-Icons**: Segoe MDL2 Assets Icons auf alle Aktions-Buttons (Hinzufügen, Speichern, Löschen, Bearbeiten, Drucken, PDF, Excel, Import, …)
- [x] **`BtnIcon`/`BtnLabel`-Styles**: in `AppTheme.xaml` — Foreground via `RelativeSource`-Binding an Button gebunden
- [x] **v1.0.0 Release**: `PublishSingleFile=true`, Single-File-EXE ~192 MB, `publish\`-Ordner
- [x] **Git-Repository**: initialisiert, remote `woku369/RezepturMeister` auf GitHub

## Offene Themen / Backlog
- [ ] Endprodukt-Tabelle als eigenes Model (aktuell: Rezeptur = Endprodukt-Näherung)
- [ ] Kategorie-Verwaltung für Rohstoffe (statt Freitext)
- [ ] Chargenprotokoll (Produktionshistorie)
- [ ] Mehrsprachigkeit (de/en)
- [ ] Benutzerhandbuch
- [ ] Einheit „l" und „kg" für Preis unterscheiden (aktuell ein einziges Preisfeld)
