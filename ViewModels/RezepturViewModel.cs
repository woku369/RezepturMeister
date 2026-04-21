using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.Win32;
using RezepturMeister.Data;
using RezepturMeister.Models;
using RezepturMeister.Services;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;

namespace RezepturMeister.ViewModels;

public partial class RezepturViewModel : ObservableObject, IDisposable
{
    private readonly RezepturService _rezepturService;
    private readonly RohstoffService _rohstoffService;
    private readonly AppDbContext _context;
    private readonly ExportService _exportService = new();

    public ObservableCollection<Rezeptur> Rezepturen { get; } = new();
    public ObservableCollection<Rohstoff> VerfuegbareRohstoffe { get; } = new();
    public ICollectionView RezepturenView { get; }

    [ObservableProperty]
    private Rezeptur? selectedRezeptur;

    [ObservableProperty]
    private Rezeptur? currentRezeptur;

    [ObservableProperty]
    private Zutat? selectedZutat;

    [ObservableProperty]
    private string suchText = string.Empty;

    partial void OnSuchTextChanged(string value)
    {
        RezepturenView.Refresh();
    }

    [RelayCommand]
    private void ClearSuchText() => SuchText = string.Empty;

    public RezepturViewModel()
    {
        _context = new AppDbContext();
        _rezepturService = new RezepturService(_context);
        _rohstoffService = new RohstoffService(_context);

        RezepturenView = CollectionViewSource.GetDefaultView(Rezepturen);
        RezepturenView.Filter = obj =>
        {
            if (obj is not Rezeptur r) return false;
            if (string.IsNullOrWhiteSpace(SuchText)) return true;
            return r.Nummer.Contains(SuchText, StringComparison.OrdinalIgnoreCase)
                || r.Name.Contains(SuchText, StringComparison.OrdinalIgnoreCase)
                || r.Chargennummer.Contains(SuchText, StringComparison.OrdinalIgnoreCase)
                || r.Bemerkungen.Contains(SuchText, StringComparison.OrdinalIgnoreCase);
        };

        LoadRezepturen();
        LoadRohstoffe();
    }

    private void LoadRezepturen()
    {
        Rezepturen.Clear();
        foreach (var rezeptur in _rezepturService.GetAll())
        {
            Rezepturen.Add(rezeptur);
        }
    }

    private void LoadRohstoffe()
    {
        VerfuegbareRohstoffe.Clear();
        foreach (var rohstoff in _rohstoffService.GetAll())
        {
            VerfuegbareRohstoffe.Add(rohstoff);
        }
    }

    [RelayCommand]
    private void NewRezeptur()
    {
        LoadRohstoffe();

        CurrentRezeptur = new Rezeptur
        {
            Name = "Neue Rezeptur",
            Nummer = "1.0",
            Erstellungsdatum = DateTime.Now,
            Zutaten = new()
        };
    }

    [RelayCommand]
    private void SaveRezeptur()
    {
        if (CurrentRezeptur == null) return;

        if (string.IsNullOrWhiteSpace(CurrentRezeptur.Nummer))
        {
            MessageBox.Show("Die Rezepturnummer darf nicht leer sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        if (string.IsNullOrWhiteSpace(CurrentRezeptur.Name))
        {
            MessageBox.Show("Der Rezepturname darf nicht leer sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        if (!CurrentRezeptur.Zutaten.Any())
        {
            MessageBox.Show("Die Rezeptur enthält keine Zutaten.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        // Navigation-Property vor dem Speichern auf null setzen — EF braucht nur RohstoffId (FK)
        // Verhindert, dass EF versucht den Rohstoff mitzuaktualisieren
        foreach (var zutat in CurrentRezeptur.Zutaten)
            zutat.Rohstoff = null;

        if (CurrentRezeptur.Id == 0)
        {
            _rezepturService.Add(CurrentRezeptur);
        }
        else
        {
            _rezepturService.Update(CurrentRezeptur);
        }
        LoadRezepturen();
        CurrentRezeptur = null;
    }

    [RelayCommand]
    private void EditRezeptur()
    {
        if (SelectedRezeptur != null)
        {
            CurrentRezeptur = new Rezeptur
            {
                Id = SelectedRezeptur.Id,
                Nummer = SelectedRezeptur.Nummer,
                Name = SelectedRezeptur.Name,
                Erstellungsdatum = SelectedRezeptur.Erstellungsdatum,
                Chargennummer = SelectedRezeptur.Chargennummer,
                Bemerkungen = SelectedRezeptur.Bemerkungen,
                Zutaten = new System.Collections.ObjectModel.ObservableCollection<Zutat>(SelectedRezeptur.Zutaten.Select(z => {
                    var zz = new Zutat
                    {
                        Id = z.Id,
                        RezepturId = z.RezepturId,
                        RohstoffId = z.RohstoffId,
                        ManuellerName = z.ManuellerName,
                        ManuelleDichte = z.ManuelleDichte,
                        ManuellerAlkoholgehalt = z.ManuellerAlkoholgehalt,
                        Menge = z.Menge,
                        Einheit = z.Einheit,
                        Prozent = z.Prozent
                    };
                    if (zz.RohstoffId.HasValue)
                        zz.Rohstoff = VerfuegbareRohstoffe.FirstOrDefault(r => r.Id == zz.RohstoffId.Value);
                    zz.PropertyChanged += OnZutatPropertyChanged;
                    return zz;
                }))
            };
        }
    }

    [RelayCommand]
    private void DeleteRezeptur()
    {
        if (SelectedRezeptur != null && MessageBox.Show($"Rezeptur '{SelectedRezeptur.Nummer}' löschen?", "Bestätigung", MessageBoxButton.YesNo) == MessageBoxResult.Yes)
        {
            _rezepturService.Delete(SelectedRezeptur.Id);
            LoadRezepturen();
        }
    }

    [RelayCommand]
    private void AddZutat()
    {
        if (CurrentRezeptur != null)
        {
            var newZutat = new Zutat
            {
                RezepturId = CurrentRezeptur.Id,
                Einheit = "g"
            };
            newZutat.PropertyChanged += OnZutatPropertyChanged;
            CurrentRezeptur.Zutaten.Add(newZutat);
        }
    }

    private void OnZutatPropertyChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
    {
        if (sender is not Zutat zutat) return;
        if (e.PropertyName == nameof(Zutat.RohstoffId))
        {
            zutat.Rohstoff = zutat.RohstoffId.HasValue
                ? VerfuegbareRohstoffe.FirstOrDefault(r => r.Id == zutat.RohstoffId.Value)
                : null;
        }
        if (e.PropertyName is nameof(Zutat.Menge) or nameof(Zutat.Einheit) or nameof(Zutat.RohstoffId))
        {
            UpdateBerechnungen();
        }
    }

    [RelayCommand]
    private void RemoveZutat()
    {
        if (CurrentRezeptur != null && SelectedZutat != null)
        {
            CurrentRezeptur.Zutaten.Remove(SelectedZutat);
        }
    }

    [RelayCommand]
    private void CreateSubVersion()
    {
        if (SelectedRezeptur != null)
        {
            string baseNummer = SelectedRezeptur.Nummer.Split('.').First();
            string newNummer = _rezepturService.GenerateNextVersion(baseNummer);

            CurrentRezeptur = new Rezeptur
            {
                Nummer = newNummer,
                Name = SelectedRezeptur.Name,
                Erstellungsdatum = DateTime.Now,
                Chargennummer = SelectedRezeptur.Chargennummer,
                Bemerkungen = SelectedRezeptur.Bemerkungen,
                Zutaten = new System.Collections.ObjectModel.ObservableCollection<Zutat>(SelectedRezeptur.Zutaten.Select(z => new Zutat
                {
                    RohstoffId = z.RohstoffId,
                    Rohstoff = z.Rohstoff,
                    ManuellerName = z.ManuellerName,
                    ManuelleDichte = z.ManuelleDichte,
                    ManuellerAlkoholgehalt = z.ManuellerAlkoholgehalt,
                    Menge = z.Menge,
                    Einheit = z.Einheit
                }))
            };
        }
    }

    [RelayCommand]
    private void CancelEdit()
    {
        CurrentRezeptur = null;
    }

    [ObservableProperty]
    private double gesamtMenge;

    [ObservableProperty]
    private double gesamtAlkohol;

    [ObservableProperty]
    private decimal gesamtPreis;

    partial void OnCurrentRezepturChanged(Rezeptur? value)
    {
        if (value != null)
        {
            LoadRohstoffe();

            foreach (var zutat in value.Zutaten)
            {
                if (zutat.RohstoffId.HasValue)
                    zutat.Rohstoff = VerfuegbareRohstoffe.FirstOrDefault(r => r.Id == zutat.RohstoffId.Value);
                zutat.PropertyChanged += OnZutatPropertyChanged;
            }
        }

        UpdateBerechnungen();
    }

    private void UpdateBerechnungen()
    {
        if (CurrentRezeptur == null) return;

        // Gesamtgewicht in g berechnen
        GesamtMenge = CurrentRezeptur.Zutaten.Sum(z =>
        {
            double menge = z.Menge;
            if (z.Einheit == "ml")
            {
                double dichte = z.Rohstoff?.Dichte ?? z.ManuelleDichte;
                menge *= dichte;
            }
            return menge;
        });

        // Gesamtvolumen in ml berechnen (für % vol.-Berechnung)
        double gesamtVolumen = CurrentRezeptur.Zutaten.Sum(z =>
        {
            double dichte = z.Rohstoff?.Dichte ?? z.ManuelleDichte;
            if (dichte <= 0) dichte = 1.0;
            return z.Einheit == "ml" ? z.Menge : z.Menge / dichte;
        });

        // Alkoholgehalt in % vol. berechnen (näherungsweise, ohne Volumenkontraktionskorrektur)
        // Alkoholgehalt der Rohstoffe ist in % vol. gespeichert
        double volumenEthanol = CurrentRezeptur.Zutaten.Sum(z =>
        {
            double alkohol = z.Rohstoff?.Alkoholgehalt ?? z.ManuellerAlkoholgehalt;
            double dichte = z.Rohstoff?.Dichte ?? z.ManuelleDichte;
            if (dichte <= 0) dichte = 1.0;
            double volZutat = z.Einheit == "ml" ? z.Menge : z.Menge / dichte;
            return alkohol * volZutat / 100.0;
        });
        GesamtAlkohol = gesamtVolumen > 0 ? volumenEthanol / gesamtVolumen * 100.0 : 0.0;

        // Prozente berechnen (Gewichtsanteil)
        if (GesamtMenge > 0)
        {
            foreach (var zutat in CurrentRezeptur.Zutaten)
            {
                double zutatGewicht = zutat.Menge;
                if (zutat.Einheit == "ml")
                {
                    double dichte = zutat.Rohstoff?.Dichte ?? zutat.ManuelleDichte;
                    zutatGewicht *= dichte;
                }
                zutat.Prozent = (zutatGewicht / GesamtMenge) * 100;
            }
        }

        // Rohstoffkosten berechnen (nur Zutaten mit verknüpftem Rohstoff und Preis > 0)
        GesamtPreis = CurrentRezeptur.Zutaten.Sum(z =>
        {
            if (z.Rohstoff == null || z.Rohstoff.Preis == 0) return 0m;
            double mengeGram = z.Menge;
            if (z.Einheit == "ml") mengeGram = z.Menge * (z.Rohstoff.Dichte);
            return z.Rohstoff.Preis * (decimal)(mengeGram / 1000.0);
        });
    }

    [RelayCommand]
    private void ExportPdf()
    {
        if (SelectedRezeptur == null) return;
        var dialog = new SaveFileDialog
        {
            Filter = "PDF-Datei (*.pdf)|*.pdf",
            FileName = $"Rezeptur_{SelectedRezeptur.Nummer.Replace(".", "-")}",
            DefaultExt = ".pdf"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportRezepturPdf(SelectedRezeptur, dialog.FileName);
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
        if (SelectedRezeptur == null) return;
        var dialog = new SaveFileDialog
        {
            Filter = "Excel-Datei (*.xlsx)|*.xlsx",
            FileName = $"Rezeptur_{SelectedRezeptur.Nummer.Replace(".", "-")}",
            DefaultExt = ".xlsx"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportRezepturXlsx(SelectedRezeptur, dialog.FileName);
                MessageBox.Show("Excel-Datei erfolgreich exportiert.", "Export");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Fehler beim Export: {ex.Message}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    [RelayCommand]
    private void ExportNaehrwertePdf()
    {
        if (SelectedRezeptur == null) return;
        var nw = NaehrwertService.Berechne(SelectedRezeptur);
        var dialog = new SaveFileDialog
        {
            Filter = "PDF-Datei (*.pdf)|*.pdf",
            FileName = $"Naehrwerte_{SelectedRezeptur.Nummer.Replace(".", "-")}",
            DefaultExt = ".pdf"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportNaehrwertePdf(SelectedRezeptur, nw, dialog.FileName);
                MessageBox.Show("Nährwert-PDF erfolgreich exportiert.", "Export");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Fehler beim Export: {ex.Message}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    [RelayCommand]
    private void ExportNaehrwerteXlsx()
    {
        if (SelectedRezeptur == null) return;
        var nw = NaehrwertService.Berechne(SelectedRezeptur);
        var dialog = new SaveFileDialog
        {
            Filter = "Excel-Datei (*.xlsx)|*.xlsx",
            FileName = $"Naehrwerte_{SelectedRezeptur.Nummer.Replace(".", "-")}",
            DefaultExt = ".xlsx"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportNaehrwerteXlsx(SelectedRezeptur, nw, dialog.FileName);
                MessageBox.Show("Nährwert-Excel erfolgreich exportiert.", "Export");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Fehler beim Export: {ex.Message}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }

    [RelayCommand]
    private void ImportCsv()
    {
        var dialog = new OpenFileDialog
        {
            Filter = "CSV-Datei (*.csv)|*.csv|Textdatei (*.txt)|*.txt",
            Title = "Rezeptur importieren (CSV)"
        };
        if (dialog.ShowDialog() != true) return;

        try
        {
            var lines = File.ReadAllLines(dialog.FileName);
            // Erwartetes Format: Name;Nummer;Datum;Charge;Bemerkungen;
            //                    ZutatName;Menge;Einheit
            var rezeptur = new Rezeptur
            {
                Erstellungsdatum = DateTime.Now,
                Zutaten = new()
            };

            foreach (var line in lines)
            {
                if (string.IsNullOrWhiteSpace(line) || line.StartsWith("#")) continue;
                var parts = line.Split(';');
                if (parts.Length < 2) continue;

                switch (parts[0].Trim().ToLowerInvariant())
                {
                    case "name":    rezeptur.Name = parts[1].Trim(); break;
                    case "nummer":  rezeptur.Nummer = parts[1].Trim(); break;
                    case "datum":   if (DateTime.TryParse(parts[1].Trim(), out var d)) rezeptur.Erstellungsdatum = d; break;
                    case "charge":  rezeptur.Chargennummer = parts[1].Trim(); break;
                    case "bemerkung": rezeptur.Bemerkungen = parts[1].Trim(); break;
                    default:
                        // Zutat: Rohstoffname;Menge;Einheit
                        if (parts.Length >= 3 && double.TryParse(parts[1].Trim().Replace(',', '.'),
                            System.Globalization.NumberStyles.Any,
                            System.Globalization.CultureInfo.InvariantCulture, out var menge))
                        {
                            var rohstoffName = parts[0].Trim();
                            var rohstoff = VerfuegbareRohstoffe.FirstOrDefault(r =>
                                r.Name.Equals(rohstoffName, StringComparison.OrdinalIgnoreCase));
                            string einheit = parts.Length > 2 ? parts[2].Trim() : "g";
                            if (einheit != "g" && einheit != "ml") einheit = "g";
                            rezeptur.Zutaten.Add(new Zutat
                            {
                                RohstoffId = rohstoff?.Id,
                                Rohstoff = rohstoff,
                                ManuellerName = rohstoff == null ? rohstoffName : string.Empty,
                                Menge = menge,
                                Einheit = einheit
                            });
                        }
                        break;
                }
            }

            if (string.IsNullOrWhiteSpace(rezeptur.Nummer))
                rezeptur.Nummer = Path.GetFileNameWithoutExtension(dialog.FileName);

            CurrentRezeptur = rezeptur;
            MessageBox.Show($"Rezeptur '{rezeptur.Name}' ({rezeptur.Zutaten.Count} Zutaten) importiert. Bitte prüfen und speichern.",
                "Import erfolgreich", MessageBoxButton.OK, MessageBoxImage.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Fehler beim Import: {ex.Message}", "Importfehler", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    [RelayCommand]
    private void Drucken()
    {
        if (SelectedRezeptur == null) return;
        var pd = new PrintDialog();
        if (pd.ShowDialog() != true) return;

        var doc = new FlowDocument { FontFamily = new System.Windows.Media.FontFamily("Arial"), FontSize = 11 };
        doc.PageWidth  = pd.PrintableAreaWidth;
        doc.PageHeight = pd.PrintableAreaHeight;
        doc.PagePadding = new System.Windows.Thickness(60);

        doc.Blocks.Add(new Paragraph(new Run($"Rezeptur {SelectedRezeptur.Nummer}"))
            { FontSize = 16, FontWeight = FontWeights.Bold, Margin = new System.Windows.Thickness(0, 0, 0, 4) });
        doc.Blocks.Add(new Paragraph(new Run($"Datum:  {SelectedRezeptur.Erstellungsdatum:d}")));
        doc.Blocks.Add(new Paragraph(new Run($"Charge: {SelectedRezeptur.Chargennummer}")));
        if (!string.IsNullOrWhiteSpace(SelectedRezeptur.Bemerkungen))
            doc.Blocks.Add(new Paragraph(new Run($"Bemerkungen: {SelectedRezeptur.Bemerkungen}")));

        var table = new Table { CellSpacing = 0, BorderBrush = System.Windows.Media.Brushes.Black, BorderThickness = new System.Windows.Thickness(1) };
        table.Columns.Add(new TableColumn { Width = new System.Windows.GridLength(220) });
        table.Columns.Add(new TableColumn { Width = new System.Windows.GridLength(80) });
        table.Columns.Add(new TableColumn { Width = new System.Windows.GridLength(60) });
        table.Columns.Add(new TableColumn { Width = new System.Windows.GridLength(80) });

        var headerGroup = new TableRowGroup();
        var headerRow = new TableRow { Background = System.Windows.Media.Brushes.LightGray };
        foreach (var h in new[] { "Zutat", "Menge", "Einheit", "Anteil %" })
            headerRow.Cells.Add(new TableCell(new Paragraph(new Run(h)) { FontWeight = FontWeights.Bold, Margin = new System.Windows.Thickness(4, 2, 4, 2) }));
        headerGroup.Rows.Add(headerRow);
        table.RowGroups.Add(headerGroup);

        double gesamtMenge = SelectedRezeptur.Zutaten.Sum(z =>
        {
            double m = z.Menge;
            if (z.Einheit == "ml") m *= (z.Rohstoff?.Dichte ?? z.ManuelleDichte);
            return m;
        });

        var bodyGroup = new TableRowGroup();
        foreach (var zutat in SelectedRezeptur.Zutaten)
        {
            double gewicht = zutat.Menge;
            if (zutat.Einheit == "ml") gewicht *= (zutat.Rohstoff?.Dichte ?? zutat.ManuelleDichte);
            double anteil = gesamtMenge > 0 ? gewicht / gesamtMenge * 100 : 0;
            var r = new TableRow();
            r.Cells.Add(new TableCell(new Paragraph(new Run(zutat.Rohstoff?.Name ?? zutat.ManuellerName)) { Margin = new System.Windows.Thickness(4, 2, 4, 2) }));
            r.Cells.Add(new TableCell(new Paragraph(new Run(zutat.Menge.ToString("F2"))) { Margin = new System.Windows.Thickness(4, 2, 4, 2) }));
            r.Cells.Add(new TableCell(new Paragraph(new Run(zutat.Einheit)) { Margin = new System.Windows.Thickness(4, 2, 4, 2) }));
            r.Cells.Add(new TableCell(new Paragraph(new Run(anteil.ToString("F1") + " %")) { Margin = new System.Windows.Thickness(4, 2, 4, 2) }));
            bodyGroup.Rows.Add(r);
        }
        table.RowGroups.Add(bodyGroup);
        doc.Blocks.Add(table);
        doc.Blocks.Add(new Paragraph(new Run($"Gesamtgewicht: {gesamtMenge:F2} g"))
            { FontWeight = FontWeights.Bold, Margin = new System.Windows.Thickness(0, 8, 0, 0) });

        pd.PrintDocument(((IDocumentPaginatorSource)doc).DocumentPaginator,
            $"Rezeptur {SelectedRezeptur.Nummer}");
    }

    public void Dispose() => _context.Dispose();
}