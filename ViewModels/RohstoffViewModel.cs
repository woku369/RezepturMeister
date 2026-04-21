using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.Win32;
using RezepturMeister.Data;
using RezepturMeister.Models;
using RezepturMeister.Services;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows;
using System.Windows.Data;

namespace RezepturMeister.ViewModels;

public partial class RohstoffViewModel : ObservableObject, IDisposable
{
    private readonly RohstoffService _rohstoffService;
    private readonly AppDbContext _context;
    private readonly ExportService _exportService = new();

    public ObservableCollection<Rohstoff> Rohstoffe { get; } = new();
    public ICollectionView RohstoffeView { get; }

    [ObservableProperty]
    private Rohstoff? selectedRohstoff;

    [ObservableProperty]
    private string suchText = string.Empty;

    partial void OnSuchTextChanged(string value)
    {
        RohstoffeView.Refresh();
    }

    public RohstoffViewModel()
    {
        _context = new AppDbContext();
        _rohstoffService = new RohstoffService(_context);

        RohstoffeView = CollectionViewSource.GetDefaultView(Rohstoffe);
        RohstoffeView.Filter = obj =>
        {
            if (obj is not Rohstoff r) return false;
            if (string.IsNullOrWhiteSpace(SuchText)) return true;
            return r.Name.Contains(SuchText, StringComparison.OrdinalIgnoreCase)
                || r.Kategorie.Contains(SuchText, StringComparison.OrdinalIgnoreCase);
        };

        LoadRohstoffe();
    }

    private void LoadRohstoffe()
    {
        Rohstoffe.Clear();
        foreach (var rohstoff in _rohstoffService.GetAll())
        {
            Rohstoffe.Add(rohstoff);
        }
    }

    [RelayCommand]
    private void ClearSuchText() => SuchText = string.Empty;

    [RelayCommand]
    private void AddRohstoff()
    {
        var newRohstoff = new Rohstoff
        {
            Name = "Neuer Rohstoff",
            Kategorie = string.Empty,
            Dichte = 1.0,
            Alkoholgehalt = 0.0
        };
        _rohstoffService.Add(newRohstoff);
        LoadRohstoffe();
        SelectedRohstoff = Rohstoffe.FirstOrDefault(r => r.Id == newRohstoff.Id);
    }

    [RelayCommand]
    private void EditRohstoff()
    {
        if (SelectedRohstoff == null) return;

        if (string.IsNullOrWhiteSpace(SelectedRohstoff.Name))
        {
            MessageBox.Show("Der Name darf nicht leer sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        if (SelectedRohstoff.Dichte <= 0)
        {
            MessageBox.Show("Die Dichte muss größer als 0 sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        _rohstoffService.Update(SelectedRohstoff);
        LoadRohstoffe();
    }

    [RelayCommand]
    private void DeleteRohstoff()
    {
        if (SelectedRohstoff == null) return;

        if (_rohstoffService.IsReferenced(SelectedRohstoff.Id))
        {
            MessageBox.Show(
                $"'{SelectedRohstoff.Name}' wird noch in mindestens einer Rezeptur verwendet und kann nicht gelöscht werden.",
                "Löschen nicht möglich", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        if (MessageBox.Show($"Rohstoff '{SelectedRohstoff.Name}' löschen?", "Bestätigung", MessageBoxButton.YesNo) == MessageBoxResult.Yes)
        {
            _rohstoffService.Delete(SelectedRohstoff.Id);
            LoadRohstoffe();
        }
    }

    [RelayCommand]
    private void LinkDatenblatt()
    {
        if (SelectedRohstoff == null) return;
        var dialog = new OpenFileDialog
        {
            Filter = "Dokumente (*.pdf;*.xlsx;*.xls;*.docx)|*.pdf;*.xlsx;*.xls;*.docx|Alle Dateien (*.*)|*.*",
            Title = "Datenblatt/Produktspezifikation verknüpfen"
        };
        if (dialog.ShowDialog() != true) return;
        SelectedRohstoff.DatenblattPfad = dialog.FileName;
        _rohstoffService.Update(SelectedRohstoff);
        LoadRohstoffe();
    }

    [RelayCommand]
    private void OpenDatenblatt()
    {
        if (SelectedRohstoff == null) return;
        if (string.IsNullOrWhiteSpace(SelectedRohstoff.DatenblattPfad))
        {
            MessageBox.Show("Kein Datenblatt verknüpft.\nBitte zuerst ein Datenblatt verknüpfen.", "Kein Datenblatt", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        if (!System.IO.File.Exists(SelectedRohstoff.DatenblattPfad))
        {
            MessageBox.Show($"Datei nicht gefunden:\n{SelectedRohstoff.DatenblattPfad}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo(SelectedRohstoff.DatenblattPfad) { UseShellExecute = true });
    }

    [RelayCommand]
    private void ImportNaehrwerteCSV()
    {
        var dialog = new OpenFileDialog
        {
            Filter = "CSV-Datei (*.csv)|*.csv|Textdatei (*.txt)|*.txt",
            Title = "Nährwertdaten importieren (CSV)"
        };
        if (dialog.ShowDialog() != true) return;

        try
        {
            var lines = System.IO.File.ReadAllLines(dialog.FileName);
            // Erwartetes Format: Name;Energie_kJ;Energie_kcal;Fett;GesaettigteFettsaeuren;Kohlenhydrate;Zucker;Ballaststoffe;Eiweiss;Salz
            int updated = 0, notFound = 0;
            var notFoundNames = new List<string>();

            foreach (var line in lines)
            {
                if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#")) continue;
                var parts = line.Split(';');
                if (parts.Length < 2) continue;

                // Erste Zeile ist möglicherweise Header
                if (parts[0].Trim().Equals("Name", StringComparison.OrdinalIgnoreCase)) continue;

                string name = parts[0].Trim();
                var rohstoff = Rohstoffe.FirstOrDefault(r =>
                    r.Name.Equals(name, StringComparison.OrdinalIgnoreCase));

                if (rohstoff == null)
                {
                    notFound++;
                    notFoundNames.Add(name);
                    continue;
                }

                static double? ParseField(string[] p, int idx)
                {
                    if (idx >= p.Length || string.IsNullOrWhiteSpace(p[idx])) return null;
                    string s = p[idx].Trim().Replace(',', '.');
                    return double.TryParse(s, System.Globalization.NumberStyles.Any,
                        System.Globalization.CultureInfo.InvariantCulture, out double v) ? v : null;
                }

                rohstoff.Energie_kJ             = ParseField(parts, 1);
                rohstoff.Energie_kcal           = ParseField(parts, 2);
                rohstoff.Fett                   = ParseField(parts, 3);
                rohstoff.GesaettigteFettsaeuren = ParseField(parts, 4);
                rohstoff.Kohlenhydrate          = ParseField(parts, 5);
                rohstoff.Zucker                 = ParseField(parts, 6);
                rohstoff.Ballaststoffe          = ParseField(parts, 7);
                rohstoff.Eiweiss                = ParseField(parts, 8);
                rohstoff.Salz                   = ParseField(parts, 9);

                _rohstoffService.Update(rohstoff);
                updated++;
            }

            LoadRohstoffe();

            string msg = $"{updated} Rohstoff/e aktualisiert.";
            if (notFound > 0)
                msg += $"\n\nNicht gefunden ({notFound}):\n{string.Join(", ", notFoundNames)}";
            MessageBox.Show(msg, "Import abgeschlossen", MessageBoxButton.OK,
                notFound > 0 ? MessageBoxImage.Warning : MessageBoxImage.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Fehler beim Import: {ex.Message}", "Importfehler", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    [RelayCommand]
    private void ExportPdf()
    {
        var dialog = new SaveFileDialog
        {
            Filter = "PDF-Datei (*.pdf)|*.pdf",
            FileName = "Rohstoffliste",
            DefaultExt = ".pdf"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportRohstoffePdf(Rohstoffe, dialog.FileName);
                MessageBox.Show("PDF erfolgreich exportiert.", "Export");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Fehler beim Export: {ex.Message}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    [RelayCommand]
    private void ExportXlsx()
    {
        var dialog = new SaveFileDialog
        {
            Filter = "Excel-Datei (*.xlsx)|*.xlsx",
            FileName = "Rohstoffliste",
            DefaultExt = ".xlsx"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportRohstoffeXlsx(Rohstoffe, dialog.FileName);
                MessageBox.Show("Excel-Datei erfolgreich exportiert.", "Export");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Fehler beim Export: {ex.Message}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    public void Dispose() => _context.Dispose();
}