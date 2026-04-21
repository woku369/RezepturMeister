using RezepturMeister.Models;
using RezepturMeister.Services;
using Xunit;

namespace RezepturMeister.Tests;

public class RohstoffServiceTests : IDisposable
{
    private readonly Microsoft.Data.Sqlite.SqliteConnection _connection;
    private readonly RezepturMeister.Data.AppDbContext _context;
    private readonly RohstoffService _service;

    public RohstoffServiceTests()
    {
        (_context, _connection) = TestHelper.CreateInMemoryContext();
        _service = new RohstoffService(_context);
    }

    [Fact]
    public void Add_NewRohstoff_ShouldBeSaved()
    {
        // Arrange
        var rohstoff = new Rohstoff { Name = "Zucker", Dichte = 1.59, Alkoholgehalt = 0 };

        // Act
        _service.Add(rohstoff);

        // Assert
        Assert.Single(_context.Rohstoffe);
        Assert.Equal("Zucker", _context.Rohstoffe.First().Name);
    }

    [Fact]
    public void GetAll_ReturnsAllRohstoffe()
    {
        // Arrange
        _service.Add(new Rohstoff { Name = "Wasser", Dichte = 1.0 });
        _service.Add(new Rohstoff { Name = "Ethanol", Dichte = 0.789, Alkoholgehalt = 96.0 });

        // Act
        var result = _service.GetAll().ToList();

        // Assert
        Assert.Equal(2, result.Count);
    }

    [Fact]
    public void GetById_ExistingId_ReturnsRohstoff()
    {
        var rohstoff = new Rohstoff { Name = "Honig", Dichte = 1.4 };
        _service.Add(rohstoff);

        var result = _service.GetById(rohstoff.Id);

        Assert.NotNull(result);
        Assert.Equal("Honig", result.Name);
    }

    [Fact]
    public void GetById_NonExistingId_ReturnsNull()
    {
        var result = _service.GetById(9999);
        Assert.Null(result);
    }

    [Fact]
    public void Update_ExistingRohstoff_ShouldPersistChanges()
    {
        var rohstoff = new Rohstoff { Name = "Alt", Dichte = 1.0 };
        _service.Add(rohstoff);

        rohstoff.Name = "Neu";
        _service.Update(rohstoff);

        var updated = _service.GetById(rohstoff.Id);
        Assert.Equal("Neu", updated?.Name);
    }

    [Fact]
    public void Delete_ExistingRohstoff_ShouldRemove()
    {
        var rohstoff = new Rohstoff { Name = "Tempor\u00e4r", Dichte = 1.0 };
        _service.Add(rohstoff);

        _service.Delete(rohstoff.Id);

        Assert.Empty(_context.Rohstoffe);
    }

    [Fact]
    public void Delete_NonExistingId_ShouldNotThrow()
    {
        // Darf keinen Fehler werfen
        var exception = Record.Exception(() => _service.Delete(9999));
        Assert.Null(exception);
    }

    public void Dispose()
    {
        _context.Dispose();
        _connection.Dispose();
    }
}
