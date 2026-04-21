using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Threading;
using System.Windows;
using Microsoft.Data.Sqlite;
using RezepturMeister.Data;

namespace RezepturMeister;

/// <summary>
/// Interaction logic for App.xaml
/// </summary>
public partial class App : Application
{
    private const int SchemaVersion = 3; // Phase 8: Nährwertdaten pro Rohstoff

    public App()
    {
        this.DispatcherUnhandledException += App_DispatcherUnhandledException;
        AppDomain.CurrentDomain.UnhandledException += CurrentDomain_UnhandledException;
    }

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        var culture = new CultureInfo("de-DE");
        Thread.CurrentThread.CurrentCulture = culture;
        Thread.CurrentThread.CurrentUICulture = culture;
        CultureInfo.DefaultThreadCurrentCulture = culture;
        CultureInfo.DefaultThreadCurrentUICulture = culture;

        EnsureDatabase();
    }

    private static void EnsureDatabase()
    {
        string dbPath = Path.Combine(AppContext.BaseDirectory, "rezepturmeister.db");
        string versionPath = Path.Combine(AppContext.BaseDirectory, "rezepturmeister.schema_version");

        int storedVersion = 0;
        if (File.Exists(versionPath) && int.TryParse(File.ReadAllText(versionPath).Trim(), out int v))
            storedVersion = v;

        // Neue DB anlegen wenn noch keine existiert
        using (var ctx = new AppDbContext())
            ctx.Database.EnsureCreated();

        // Fehlende Spalten per ALTER TABLE nachrüsten (idempotent, Daten bleiben erhalten)
        if (storedVersion < SchemaVersion)
        {
            string backup = dbPath + $".bak_v{storedVersion}_{DateTime.Now:yyyyMMddHHmmss}";
            File.Copy(dbPath, backup, overwrite: true);
        }
        ApplySchemaMigrations(dbPath);

        File.WriteAllText(versionPath, SchemaVersion.ToString());
    }

    // Fügt fehlende Spalten hinzu — löscht niemals Daten
    private static void ApplySchemaMigrations(string dbPath)
    {
        using var connection = new SqliteConnection($"Data Source={dbPath}");
        connection.Open();

        var rohstoffe = GetColumns(connection, "Rohstoffe");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "DatenblattPfad",          "TEXT NOT NULL DEFAULT ''");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "ZusatzlicheEigenschaften", "TEXT NOT NULL DEFAULT '{}'");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Preis",                    "REAL NOT NULL DEFAULT 0.0");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Lieferant",                "TEXT NOT NULL DEFAULT ''");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Energie_kJ",               "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Energie_kcal",             "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Fett",                     "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "GesaettigteFettsaeuren",   "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Kohlenhydrate",            "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Zucker",                   "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Ballaststoffe",            "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Eiweiss",                  "REAL");
        AddColumnIfMissing(connection, "Rohstoffe", rohstoffe, "Salz",                     "REAL");

        var zutaten = GetColumns(connection, "Zutaten");
        AddColumnIfMissing(connection, "Zutaten", zutaten, "ManuellerName",           "TEXT NOT NULL DEFAULT ''");
        AddColumnIfMissing(connection, "Zutaten", zutaten, "ManuelleDichte",          "REAL NOT NULL DEFAULT 0.0");
        AddColumnIfMissing(connection, "Zutaten", zutaten, "ManuellerAlkoholgehalt",  "REAL NOT NULL DEFAULT 0.0");
        AddColumnIfMissing(connection, "Zutaten", zutaten, "Prozent",                 "REAL NOT NULL DEFAULT 0.0");
    }

    private static HashSet<string> GetColumns(SqliteConnection connection, string table)
    {
        var cols = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        using var cmd = connection.CreateCommand();
        cmd.CommandText = $"PRAGMA table_info({table})";
        using var reader = cmd.ExecuteReader();
        while (reader.Read())
            cols.Add(reader.GetString(1));
        return cols;
    }

    private static void AddColumnIfMissing(SqliteConnection connection, string table,
        HashSet<string> existing, string column, string colDef)
    {
        if (existing.Contains(column)) return;
        using var cmd = connection.CreateCommand();
        cmd.CommandText = $"ALTER TABLE \"{table}\" ADD COLUMN \"{column}\" {colDef}";
        cmd.ExecuteNonQuery();
        existing.Add(column);
    }

    private static void DeleteDbWithWalFiles(string dbPath)
    {
        foreach (string suffix in new[] { "", "-wal", "-shm" })
        {
            string file = dbPath + suffix;
            if (File.Exists(file)) File.Delete(file);
        }
    }

    private void App_DispatcherUnhandledException(object sender, System.Windows.Threading.DispatcherUnhandledExceptionEventArgs e)
    {
        MessageBox.Show($"UNBEHANDELTE AUSNAHME (UI-Thread):\n{e.Exception.Message}\n\nStackTrace:\n{e.Exception.StackTrace}\n\nInnerException:\n{e.Exception.InnerException}", "Fataler Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
        e.Handled = true;
    }

    private void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
    {
        var ex = e.ExceptionObject as Exception;
        MessageBox.Show($"UNBEHANDELTE AUSNAHME (AppDomain):\n{ex?.Message}\n\nStackTrace:\n{ex?.StackTrace}\n\nInnerException:\n{ex?.InnerException}", "Fataler Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
    }
}