using System.IO;
using Microsoft.EntityFrameworkCore;
using RezepturMeister.Models;

namespace RezepturMeister.Data;

public class AppDbContext : DbContext
{
    // Produktions-Konstruktor (kein Parameter = eigene OnConfiguring-Logik)
    public AppDbContext() { }

    // Test-Konstruktor (SQLite InMemory oder beliebige Options von außen)
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<Rohstoff> Rohstoffe { get; set; }
    public DbSet<Rezeptur> Rezepturen { get; set; }
    public DbSet<Zutat> Zutaten { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        // Nur konfigurieren wenn noch keine Options von außen gesetzt wurden (z.B. im Test)
        if (!optionsBuilder.IsConfigured)
        {
            string dbPath = Path.Combine(AppContext.BaseDirectory, "rezepturmeister.db");
            optionsBuilder.UseSqlite($"Data Source={dbPath}");
        }
    }
}