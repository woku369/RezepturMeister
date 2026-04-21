---
name: ef-sqlite
description: 'Entity Framework Core mit SQLite für RezepturMeister. Verwende diesen Skill bei: Migrations erstellen/anwenden, DbContext-Lifecycle, neue Entities/Tabellen hinzufügen, Relationships konfigurieren, Include/ThenInclude, DbContext-Fehler, Datenbankpfad konfigurieren, EF Core Abfragen optimieren. Schlüsselwörter: Entity Framework, EF Core, SQLite, Migration, DbContext, DbSet, Include, ThenInclude, OnConfiguring, AppDbContext, SaveChanges.'
argument-hint: 'Beschreibe die EF/Datenbank-Aufgabe (z.B. "neue Migration erstellen", "Relation hinzufügen")'
---

# Entity Framework Core + SQLite Skill – RezepturMeister

## Projektkontext

- Paket: `Microsoft.EntityFrameworkCore.Sqlite` 8.0.0
- DbContext: `Data/AppDbContext.cs`
- DB-Datei: `rezepturmeister.db` neben der `.exe` (`AppContext.BaseDirectory`)
- Models: `Models/Rohstoff.cs`, `Models/Rezeptur.cs` (enthält auch `Zutat`)
- Services: `Services/RohstoffService.cs`, `Services/RezepturService.cs`

## AppDbContext

```csharp
public class AppDbContext : DbContext
{
    public DbSet<Rohstoff> Rohstoffe { get; set; }
    public DbSet<Rezeptur> Rezepturen { get; set; }
    public DbSet<Zutat> Zutaten { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        // DB-Datei neben der .exe
        string dbPath = Path.Combine(
            AppContext.BaseDirectory, "rezepturmeister.db");
        optionsBuilder.UseSqlite($"Data Source={dbPath}");
    }
}
```

**Wichtig:** DB-Pfad relativ zu `AppContext.BaseDirectory` setzen, nicht zum Working Directory — damit funktioniert die App auch nach dem Publish.

## Migrations

### Neue Migration erstellen
```powershell
dotnet ef migrations add <MigrationName>
```

### Migration anwenden
```powershell
dotnet ef database update
```

### Alle Migrations anzeigen
```powershell
dotnet ef migrations list
```

### Automatic Migration (im Code, beim App-Start)
In `App.xaml.cs`:
```csharp
using var context = new AppDbContext();
context.Database.Migrate(); // Erstellt DB + wendet pending Migrations an
```
Oder einfacher (ohne Migrations-Dateien):
```csharp
context.Database.EnsureCreated(); // Nur sinnvoll ohne formelle Migrations
```

## DbContext Lifecycle

**Problem:** Ein globaler DbContext in ViewModel führt zu Tracking-Konflikten.

**Lösung im aktuellen Projekt:** Pro ViewModel eine eigene DbContext-Instanz — akzeptabel für eine Desktop-App.

```csharp
public RohstoffViewModel()
{
    _context = new AppDbContext();
    _rohstoffService = new RohstoffService(_context);
    LoadRohstoffe();
}
```

Für größere Refactorings: DbContext per `using` in jeder Service-Methode erzeugen:
```csharp
public IEnumerable<Rohstoff> GetAll()
{
    using var ctx = new AppDbContext();
    return ctx.Rohstoffe.ToList();
}
```

## Abfragen mit Navigation Properties

```csharp
// Mit Include (Zutaten laden)
_context.Rezepturen
    .Include(r => r.Zutaten)
    .ThenInclude(z => z.Rohstoff)
    .ToList();

// Einzeln laden
_context.Rezepturen
    .Include(r => r.Zutaten)
    .ThenInclude(z => z.Rohstoff)
    .FirstOrDefault(r => r.Id == id);
```

## Neues Model/Tabelle hinzufügen

1. Klasse in `Models/` erstellen mit `[Key]`-Property
2. `DbSet<NeueKlasse>` in `AppDbContext` ergänzen
3. Migration erstellen: `dotnet ef migrations add AddNeueKlasse`
4. Migration anwenden: `dotnet ef database update`

## Relationships

### One-to-Many (Rezeptur → Zutaten)
```csharp
// Parent
public virtual ICollection<Zutat> Zutaten { get; set; } = new List<Zutat>();

// Child
public int RezepturId { get; set; }
public virtual Rezeptur Rezeptur { get; set; } = null!;
```

### Optional Foreign Key (Zutat → Rohstoff, nullable)
```csharp
public int? RohstoffId { get; set; }
public virtual Rohstoff? Rohstoff { get; set; }
```

## Häufige Fehler

| Fehler | Ursache | Lösung |
|--------|---------|--------|
| `no such table` | Migration nicht angewendet | `dotnet ef database update` ausführen |
| Tracking-Fehler beim Update | Objekt außerhalb des Context bearbeitet | `_context.Entry(obj).State = EntityState.Modified` |
| DB-Datei am falschen Ort | Relativer Pfad ohne BaseDirectory | `AppContext.BaseDirectory` verwenden |
| Navigation Property leer | Include vergessen | `.Include(r => r.Zutaten)` ergänzen |

## Checkliste für neue Entity

1. Model-Klasse mit `[Key]`, `[Required]` Annotations
2. `DbSet<T>` in `AppDbContext`
3. Falls Navigation Property: FK-Property als `int` oder `int?`
4. Migration erstellen + anwenden
5. Service-Methoden (GetAll, GetById, Add, Update, Delete)
