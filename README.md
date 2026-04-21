# RezepturMeister

Eine Windows Desktop-Anwendung zur Verwaltung von Rezepturen für die Herstellung alkoholischer Getränke, Liköre, Brände und Sirupe.

**Version 1.0.0**

## Funktionen

- **Rohstoffdatenbank**: Verwaltung von Zutaten mit Kategorien, Dichte, Alkoholgehalt und erweiterbaren Eigenschaften.
- **Rezepturerstellung**: Erstellung von Rezepturen aus Rohstoffen oder manuellen Zutaten mit Mengen in g oder ml.
- **Berechnungen**: Automatische Berechnung von Summen und prozentualen Anteilen (Gewichtsanteil je Zutat, Gesamtalkohol).
- **Versionierung**: Unterrezepturen für Verfeinerungen (z.B. 1.0 → 1.1 → 1.2).
- **Suche & Filter**: Live-Filter in Rohstoff- und Rezepturliste.
- **Export**: PDF- und XLSX-Export für Rohstoff- und Rezepturlisten.
- **Druck**: Ganzseitiger Druck von Rezepturen auf DIN A4.

## Technologien

| Bereich   | Technologie                              |
|-----------|------------------------------------------|
| UI        | WPF (.NET 8), MVVM, CommunityToolkit.Mvvm 8.2.2 |
| Datenbank | Entity Framework Core 8 + SQLite         |
| Export    | PdfSharpCore 1.3.1, ClosedXML 0.102.1   |
| Tests     | xUnit 2.7.0, SQLite In-Memory            |

## Installation & Start (Entwicklung)

```bash
# Repository klonen
git clone <url>

# Abhängigkeiten wiederherstellen
dotnet restore

# Anwendung starten
dotnet run --project RezepturMeister.csproj
```

## Release-Build (portabler Single-File EXE)

```bash
dotnet publish -c Release -r win-x64 --self-contained true -o publish
```

### Deployment-Struktur nach dem Build

Das fertige Release befindet sich im Ordner `publish\`:

```text
publish\
├── RezepturMeister.exe          ← Hauptanwendung (~190 MB, alles enthalten)
├── D3DCompiler_47_cor3.dll      ┐
├── e_sqlite3.dll                │
├── PenImc_cor3.dll              │ Native WPF/SQLite-DLLs (können nicht
├── PresentationNative_cor3.dll  │ in die EXE eingebettet werden)
├── vcruntime140_cor3.dll        │
├── wpfgfx_cor3.dll              ┘
└── rezepturmeister.db           ← Datenbank (wird beim ersten Start erstellt)
```

**Hinweis zur EXE-Größe**: Die `RezepturMeister.exe` ist bewusst ~190 MB groß —
sie enthält das .NET 8 Runtime und alle Abhängigkeiten, sodass keine Installation
auf dem Zielrechner erforderlich ist. Die 6 nativen DLLs daneben sind ein
technisch unvermeidlicher Bestandteil von WPF/.NET und können nicht eingebettet werden.

Die SQLite-Datenbank (`rezepturmeister.db`) wird beim ersten Start automatisch
neben der EXE erstellt.

## Tests ausführen

```bash
dotnet test RezepturMeister.Tests/RezepturMeister.Tests.csproj
```

## Projektstruktur

```
Models/         Rohstoff.cs, Rezeptur.cs (inkl. Zutat)
Data/           AppDbContext.cs  →  DB: AppContext.BaseDirectory/rezepturmeister.db
Services/       RohstoffService, RezepturService, ExportService
ViewModels/     RohstoffViewModel, RezepturViewModel, MainViewModel
Views/          RohstoffView.xaml, RezepturView.xaml
Converters/     DecimalConverter, NullToVisibilityConverter, PercentageConverter
Themes/         AppTheme.xaml  (Farbpalette, Button/DataGrid/Tab-Styles)
RezepturMeister.Tests/  xUnit-Tests (17 Tests)
publish/        Fertige Release-Version (Single-File EXE + 6 native DLLs)
```

## Roadmap

- [x] Grundlegende Projektstruktur implementiert
- [x] Datenbankmodell definiert
- [x] Models für Rohstoffe und Rezepturen erstellt
- [x] Basis-UI für Rohstoffe und Rezepturen
- [x] CRUD-Operationen für Rohstoffe und Rezepturen
- [x] Rezepturerstellungslogik mit Versionierung
- [x] Berechnungsfunktionen (Gewicht, Prozentanteile, Alkohol)
- [x] Exportfunktionen (PDF, XLSX, Druck)
- [x] Suchfilter für Rohstoffe und Rezepturen
- [x] Eingabevalidierung
- [x] Unit-Tests (17 Tests)
- [x] UI-Design (dezentes appübergreifendes Theme, Header, Hover-Effekte)
- [x] Release v1.0.0 (Single-File EXE, portabel)
- [ ] Benutzerhandbuch
