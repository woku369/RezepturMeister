using System.Globalization;
using System.IO;
using System.Threading;
using System.Windows;
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

        // Setze Kultur auf Deutsch für Komma als Dezimaltrennzeichen
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

        if (storedVersion != SchemaVersion && File.Exists(dbPath))
        {
            string backup = dbPath + $".bak_v{storedVersion}_{DateTime.Now:yyyyMMddHHmmss}";
            File.Copy(dbPath, backup, overwrite: true);
            DeleteDbWithWalFiles(dbPath);
            MessageBox.Show(
                $"Das Datenbankschema wurde aktualisiert (v{storedVersion} → v{SchemaVersion}).\n" +
                $"Eine Sicherungskopie der alten Datenbank wurde angelegt:\n{backup}",
                "Datenbankupdate", MessageBoxButton.OK, MessageBoxImage.Information);
        }

        InitializeOrRepairDatabase(dbPath);
        File.WriteAllText(versionPath, SchemaVersion.ToString());
    }

    // Erstellt die DB und validiert das Schema — repariert bei Bedarf (z.B. nach hartem Prozess-Kill)
    private static void InitializeOrRepairDatabase(string dbPath)
    {
        bool schemaValid = false;
        using (var ctx = new AppDbContext())
        {
            ctx.Database.EnsureCreated();
            try
            {
                _ = ctx.Rohstoffe.Any();
                _ = ctx.Rezepturen.Any();
                _ = ctx.Zutaten.Any();
                schemaValid = true;
            }
            catch (Microsoft.Data.Sqlite.SqliteException) { }
        }

        if (!schemaValid)
        {
            DeleteDbWithWalFiles(dbPath);
            using var freshCtx = new AppDbContext();
            freshCtx.Database.EnsureCreated();
        }
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