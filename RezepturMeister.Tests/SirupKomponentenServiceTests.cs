using RezepturMeister.Models;
using RezepturMeister.Services;
using Xunit;

namespace RezepturMeister.Tests;

public class SirupKomponentenServiceTests : IDisposable
{
    private readonly Microsoft.Data.Sqlite.SqliteConnection _connection;
    private readonly RezepturMeister.Data.AppDbContext _context;
    private readonly SirupKomponentenService _service;

    public SirupKomponentenServiceTests()
    {
        (_context, _connection) = TestHelper.CreateInMemoryContext();
        _service = new SirupKomponentenService(_context);
    }

    [Fact]
    public void Add_NewKomponente_ShouldBeSaved()
    {
        var komponente = new SirupKomponente { Name = "Wacholder-Mazerat", Typ = "Mazerat", Alkoholgehalt = 53 };

        _service.Add(komponente);

        Assert.Single(_context.SirupKomponenten);
        Assert.Equal("Wacholder-Mazerat", _context.SirupKomponenten.First().Name);
    }

    [Fact]
    public void GetAll_ReturnsAllKomponenten()
    {
        _service.Add(new SirupKomponente { Name = "Mazerat A", Typ = "Mazerat", Alkoholgehalt = 53 });
        _service.Add(new SirupKomponente { Name = "Destillat A", Typ = "Destillat", Alkoholgehalt = 75 });

        var result = _service.GetAll().ToList();

        Assert.Equal(2, result.Count);
    }

    [Fact]
    public void Update_ExistingKomponente_ShouldPersistChanges()
    {
        var komponente = new SirupKomponente { Name = "Alt", Typ = "Mazerat", Alkoholgehalt = 53 };
        _service.Add(komponente);

        komponente.Name = "Neu";
        _service.Update(komponente);

        var updated = _service.GetById(komponente.Id);
        Assert.Equal("Neu", updated?.Name);
    }

    [Fact]
    public void IsReferenced_KomponenteInPosition_ReturnsTrue()
    {
        var komponente = new SirupKomponente { Name = "Mazerat A", Typ = "Mazerat", Alkoholgehalt = 53 };
        _service.Add(komponente);

        var rezeptur = new SirupRezeptur { Erstellungsdatum = DateTime.Today };
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", KomponenteId = komponente.Id, MengeMl = 20 });
        _context.SirupRezepturen.Add(rezeptur);
        _context.SaveChanges();

        Assert.True(_service.IsReferenced(komponente.Id));
    }

    [Fact]
    public void IsReferenced_UnbenutzteKomponente_ReturnsFalse()
    {
        var komponente = new SirupKomponente { Name = "Mazerat A", Typ = "Mazerat", Alkoholgehalt = 53 };
        _service.Add(komponente);

        Assert.False(_service.IsReferenced(komponente.Id));
    }

    [Fact]
    public void Delete_ExistingKomponente_ShouldRemove()
    {
        var komponente = new SirupKomponente { Name = "Temporär", Typ = "Mazerat", Alkoholgehalt = 53 };
        _service.Add(komponente);

        _service.Delete(komponente.Id);

        Assert.Empty(_context.SirupKomponenten);
    }

    public void Dispose()
    {
        _context.Dispose();
        _connection.Dispose();
    }
}
