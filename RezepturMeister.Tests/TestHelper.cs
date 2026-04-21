using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using RezepturMeister.Data;

namespace RezepturMeister.Tests;

/// <summary>
/// Erstellt einen AppDbContext mit SQLite In-Memory-Datenbank für Tests.
/// Die Verbindung muss offen gehalten werden, solange der Context genutzt wird.
/// </summary>
public static class TestHelper
{
    public static (AppDbContext Context, SqliteConnection Connection) CreateInMemoryContext()
    {
        var connection = new SqliteConnection("Data Source=:memory:");
        connection.Open();

        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseSqlite(connection)
            .Options;

        var context = new AppDbContext(options);
        context.Database.EnsureCreated();

        return (context, connection);
    }
}
