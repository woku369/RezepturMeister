using RezepturMeister.Models;
using RezepturMeister.Services;
using Xunit;

namespace RezepturMeister.Tests;

public class SirupRezepturServiceTests : IDisposable
{
    private readonly Microsoft.Data.Sqlite.SqliteConnection _connection;
    private readonly RezepturMeister.Data.AppDbContext _context;
    private readonly SirupRezepturService _service;

    public SirupRezepturServiceTests()
    {
        (_context, _connection) = TestHelper.CreateInMemoryContext();
        _service = new SirupRezepturService(_context);
    }

    [Fact]
    public void Add_NewRezeptur_ShouldBeSaved()
    {
        var rezeptur = new SirupRezeptur { Name = "Zitrus-Sirup", Erstellungsdatum = DateTime.Today };

        _service.Add(rezeptur);

        Assert.Single(_context.SirupRezepturen);
        Assert.Equal("Zitrus-Sirup", _context.SirupRezepturen.First().Name);
    }

    [Fact]
    public void GetAll_IncludesPositionen()
    {
        var rezeptur = new SirupRezeptur { Name = "Kräuter-Sirup", Erstellungsdatum = DateTime.Today };
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", ManuelleBezeichnung = "Kräutermazerat", ManuellerAlkoholgehalt = 53, MengeMl = 20 });
        _service.Add(rezeptur);

        var result = _service.GetAll().ToList();

        Assert.Single(result);
        Assert.Single(result[0].Positionen);
        Assert.Equal("Kräutermazerat", result[0].Positionen.First().ManuelleBezeichnung);
    }

    [Fact]
    public void Update_EntferntGeloeschtePositionen()
    {
        var rezeptur = new SirupRezeptur { Name = "Sirup", Erstellungsdatum = DateTime.Today };
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 20 });
        _service.Add(rezeptur);

        var geladen = _service.GetById(rezeptur.Id)!;
        geladen.Positionen.Clear();
        _service.Update(geladen);

        var neuGeladen = _service.GetById(rezeptur.Id)!;
        Assert.Empty(neuGeladen.Positionen);
    }

    [Fact]
    public void Delete_ExistingRezeptur_ShouldRemove()
    {
        var rezeptur = new SirupRezeptur { Name = "Sirup", Erstellungsdatum = DateTime.Today };
        _service.Add(rezeptur);

        _service.Delete(rezeptur.Id);

        Assert.Empty(_context.SirupRezepturen);
    }

    public void Dispose()
    {
        _context.Dispose();
        _connection.Dispose();
    }
}
