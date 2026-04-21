using RezepturMeister.Models;
using RezepturMeister.Services;
using Xunit;

namespace RezepturMeister.Tests;

public class RezepturServiceTests : IDisposable
{
    private readonly Microsoft.Data.Sqlite.SqliteConnection _connection;
    private readonly RezepturMeister.Data.AppDbContext _context;
    private readonly RezepturService _service;

    public RezepturServiceTests()
    {
        (_context, _connection) = TestHelper.CreateInMemoryContext();
        _service = new RezepturService(_context);
    }

    [Fact]
    public void Add_NewRezeptur_ShouldBeSaved()
    {
        var rezeptur = new Rezeptur { Nummer = "1.0", Erstellungsdatum = DateTime.Today };

        _service.Add(rezeptur);

        Assert.Single(_context.Rezepturen);
        Assert.Equal("1.0", _context.Rezepturen.First().Nummer);
    }

    [Fact]
    public void GetAll_IncludesZutaten()
    {
        var rezeptur = new Rezeptur
        {
            Nummer = "1.0",
            Erstellungsdatum = DateTime.Today,
            Zutaten = new List<Zutat>
            {
                new Zutat { Menge = 500, Einheit = "g", ManuellerName = "Wasser" }
            }
        };
        _service.Add(rezeptur);

        var result = _service.GetAll().ToList();

        Assert.Single(result);
        Assert.Single(result[0].Zutaten);
        Assert.Equal("Wasser", result[0].Zutaten.First().ManuellerName);
    }

    [Fact]
    public void Delete_ExistingRezeptur_ShouldRemove()
    {
        var rezeptur = new Rezeptur { Nummer = "1.0", Erstellungsdatum = DateTime.Today };
        _service.Add(rezeptur);

        _service.Delete(rezeptur.Id);

        Assert.Empty(_context.Rezepturen);
    }

    [Fact]
    public void GenerateNextVersion_NoExisting_ReturnsFirst()
    {
        var result = _service.GenerateNextVersion("1");
        Assert.Equal("1.1", result);
    }

    [Fact]
    public void GenerateNextVersion_WithExisting_ShouldIncrement()
    {
        _context.Rezepturen.Add(new Rezeptur { Nummer = "1.0", Erstellungsdatum = DateTime.Today });
        _context.Rezepturen.Add(new Rezeptur { Nummer = "1.1", Erstellungsdatum = DateTime.Today });
        _context.SaveChanges();

        var result = _service.GenerateNextVersion("1");

        Assert.Equal("1.2", result);
    }

    [Fact]
    public void GenerateNextVersion_DifferentBase_IsIsolated()
    {
        _context.Rezepturen.Add(new Rezeptur { Nummer = "2.0", Erstellungsdatum = DateTime.Today });
        _context.SaveChanges();

        // Base "1" hat keine Versionen → soll 1.1 liefern
        var result = _service.GenerateNextVersion("1");
        Assert.Equal("1.1", result);
    }

    public void Dispose()
    {
        _context.Dispose();
        _connection.Dispose();
    }
}
