---
name: testing
description: 'Unit-Tests für RezepturMeister Services und ViewModels. Verwende diesen Skill bei: Unit-Tests schreiben, xUnit, NUnit oder MSTest einrichten, Services testen, ViewModels testen, SQLite In-Memory für Tests, Mocking mit Moq, TestProjekt erstellen, Code Coverage. Schlüsselwörter: Unit Test, xUnit, MSTest, NUnit, Moq, InMemory, Assert, Arrange Act Assert, RohstoffService testen, RezepturService testen, ViewModel testen.'
argument-hint: 'Was soll getestet werden? (z.B. "RohstoffService", "RezepturViewModel", "Berechnungen")'
---

# Testing Skill – RezepturMeister

## Projektkontext

- Hauptprojekt: `RezepturMeister.csproj` (.NET 8, WPF)
- Zu testende Klassen: `Services/RohstoffService.cs`, `Services/RezepturService.cs`, `ViewModels/`
- Empfohlenes Test-Framework: **xUnit** + **Moq** (für Mocking)
- DB für Tests: **SQLite In-Memory** (kein echtes File, kein EF InMemory-Provider)

## Testprojekt einrichten

### Neues Testprojekt erstellen
```powershell
dotnet new xunit -n RezepturMeister.Tests -o RezepturMeister.Tests
cd RezepturMeister.Tests
dotnet add reference ../RezepturMeister.csproj
dotnet add package Microsoft.EntityFrameworkCore.Sqlite
dotnet add package Moq
```

### Zur Solution hinzufügen
```powershell
dotnet sln ../RezepturMeister.sln add RezepturMeister.Tests.csproj
```

## SQLite In-Memory DbContext für Tests

```csharp
// TestDbContext-Helper
public static AppDbContext CreateInMemoryContext()
{
    var connection = new SqliteConnection("Data Source=:memory:");
    connection.Open();
    
    var options = new DbContextOptionsBuilder<AppDbContext>()
        .UseSqlite(connection)
        .Options;
    
    var context = new AppDbContext(options);
    context.Database.EnsureCreated();
    return context;
}
```

**Wichtig:** `AppDbContext` muss dafür einen Konstruktor mit `DbContextOptions` akzeptieren:
```csharp
public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }
public AppDbContext() { } // bestehender Konstruktor bleibt
```

## Service Tests (Arrange-Act-Assert)

```csharp
public class RohstoffServiceTests : IDisposable
{
    private readonly AppDbContext _context;
    private readonly RohstoffService _service;

    public RohstoffServiceTests()
    {
        _context = TestHelper.CreateInMemoryContext();
        _service = new RohstoffService(_context);
    }

    [Fact]
    public void Add_NewRohstoff_ShouldBeSaved()
    {
        // Arrange
        var rohstoff = new Rohstoff { Name = "Zucker", Dichte = 1.59 };

        // Act
        _service.Add(rohstoff);

        // Assert
        Assert.Single(_context.Rohstoffe);
        Assert.Equal("Zucker", _context.Rohstoffe.First().Name);
    }

    [Fact]
    public void Delete_ExistingRohstoff_ShouldRemove()
    {
        var rohstoff = new Rohstoff { Name = "Wasser", Dichte = 1.0 };
        _service.Add(rohstoff);

        _service.Delete(rohstoff.Id);

        Assert.Empty(_context.Rohstoffe);
    }

    public void Dispose() => _context.Dispose();
}
```

## RezepturService Tests

```csharp
[Fact]
public void GenerateNextVersion_WithExisting_ShouldIncrement()
{
    var context = TestHelper.CreateInMemoryContext();
    var service = new RezepturService(context);
    context.Rezepturen.Add(new Rezeptur { Nummer = "1.0", Erstellungsdatum = DateTime.Now });
    context.Rezepturen.Add(new Rezeptur { Nummer = "1.1", Erstellungsdatum = DateTime.Now });
    context.SaveChanges();

    var result = service.GenerateNextVersion("1");

    Assert.Equal("1.2", result);
}
```

## ViewModel Tests (ohne WPF-Dispatcher)

WPF-ViewModels können ohne UI getestet werden, solange kein MessageBox/Dispatcher verwendet wird.

```csharp
[Fact]
public void LoadRohstoffe_FillsCollection()
{
    var context = TestHelper.CreateInMemoryContext();
    context.Rohstoffe.AddRange(
        new Rohstoff { Name = "A", Dichte = 1.0 },
        new Rohstoff { Name = "B", Dichte = 1.5 }
    );
    context.SaveChanges();

    // ViewModel muss DbContext-Injektion unterstützen für Tests
    var vm = new RohstoffViewModel(context);

    Assert.Equal(2, vm.Rohstoffe.Count);
}
```

## Berechnungs-Tests (GesamtMenge, Alkohol)

```csharp
[Fact]
public void GesamtMenge_SummeAllerZutaten()
{
    var rezeptur = new Rezeptur
    {
        Nummer = "1.0",
        Erstellungsdatum = DateTime.Now,
        Zutaten = new List<Zutat>
        {
            new Zutat { Menge = 500, Einheit = "g" },
            new Zutat { Menge = 250, Einheit = "g" }
        }
    };
    // Berechnungslogik direkt testen
    double gesamt = rezeptur.Zutaten.Sum(z => z.Menge);
    Assert.Equal(750, gesamt);
}
```

## Tests ausführen

```powershell
dotnet test
dotnet test --verbosity normal
dotnet test --collect:"XPlat Code Coverage"   # Code Coverage
```

## Checkliste

1. Testprojekt mit `dotnet new xunit` erstellen
2. Referenz auf Hauptprojekt hinzufügen
3. `AppDbContext` um Options-Konstruktor erweitern
4. `TestHelper.CreateInMemoryContext()` für jeden Test-Case
5. `IDisposable` implementieren um Context freizugeben
6. Arrange-Act-Assert Pattern einhalten
7. MessageBox-Aufrufe in ViewModels durch Events/Delegates ersetzen
