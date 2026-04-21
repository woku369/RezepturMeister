---
name: meister-suite
description: 'RezepturMeister Projektregeln und Workflow-Skill. IMMER laden bei: jeder Implementierungsaufgabe im RezepturMeister-Projekt, Roadmap aktualisieren, neue Features planen, Status prüfen, Phase abschließen. Schlüsselwörter: RezepturMeister, Roadmap, Phase, Projektfortschritt, nächster Schritt, Status, abgeschlossen, offen.'
argument-hint: 'Aufgabe oder Phase beschreiben'
---

# RezepturMeister Suite – Projektworkflow-Skill

## PFLICHT nach jeder Implementierung

Nach **jeder** abgeschlossenen Implementierungsaufgabe MUSS die `ROADMAP.md` aktualisiert werden:

1. Abgeschlossene Punkte mit `[x]` markieren
2. Laufende Phase als `(In Arbeit)` oder `(Abgeschlossen)` kennzeichnen
3. Neue Themen/Features als eigene Einträge ergänzen
4. Offene Punkte bleiben als `[ ]`

## ROADMAP-Format

```markdown
## Phase N: Titel (Status)
- [x] Erledigter Punkt
- [ ] Offener Punkt
- [~] In Arbeit / teilweise erledigt
```

Status-Bezeichnungen:
- `(Abgeschlossen)` — alle Einträge erledigt
- `(In Arbeit)` — Phase läuft gerade
- `(Offen)` — noch nicht begonnen

## Workflow für jede Aufgabe

1. Relevante Skills laden (wpf-mvvm, ef-sqlite, export-print, testing)
2. Implementierung durchführen
3. `get_errors` aufrufen → Fehler beheben
4. **ROADMAP.md aktualisieren** (dieser Schritt darf nie fehlen)

## Projektstruktur (Kurzreferenz)

```
Models/         Rohstoff.cs, Rezeptur.cs (enthält Zutat)
Data/           AppDbContext.cs  →  DB: AppContext.BaseDirectory/rezepturmeister.db
Services/       RohstoffService, RezepturService, ExportService
ViewModels/     RohstoffViewModel, RezepturViewModel, MainViewModel
Views/          RohstoffView.xaml, RezepturView.xaml
Converters/     DecimalConverter, NullToVisibilityConverter, PercentageConverter
.github/skills/ wpf-mvvm, ef-sqlite, testing, export-print, meister-suite
```

## Verfügbare Skills

| Skill | Wann laden |
|-------|-----------|
| `wpf-mvvm` | XAML, DataBinding, Commands, UserControls |
| `ef-sqlite` | Migrations, DbContext, neue Entities |
| `testing` | Unit-Tests, xUnit, InMemory-DB |
| `export-print` | PDF, XLSX, Drucken |
| `meister-suite` | Immer — für Roadmap-Update und Projektzusammenhang |

## Neue Features / Themen

Wenn ein neues Feature oder Thema auftaucht (z.B. aus Benutzerwunsch), sofort als neuen Eintrag in die ROADMAP.md schreiben — auch wenn noch nicht beauftragt.
