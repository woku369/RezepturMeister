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

public partial class SirupViewModel : ObservableObject, IDisposable
{
    private readonly SirupRezepturService _rezepturService;
    private readonly SirupKomponentenService _komponentenService;
    private readonly AppDbContext _context;
    private readonly ExportService _exportService = new();

    public ObservableCollection<SirupRezeptur> Rezepturen { get; } = new();
    public ICollectionView RezepturenView { get; }

    // Volle Komponenten-Datenbank (Verwaltung) sowie nach Typ gefilterte Listen für die Auswahlfelder
    public ObservableCollection<SirupKomponente> VerfuegbareKomponenten { get; } = new();
    public ObservableCollection<SirupKomponente> MazerateKomponenten { get; } = new();
    public ObservableCollection<SirupKomponente> DestillateKomponenten { get; } = new();

    // Nach Typ aufgeteilte Ansicht der Positionen von CurrentRezeptur (für die zwei DataGrids in Block 2)
    public ObservableCollection<SirupPosition> MazeratPositionen { get; } = new();
    public ObservableCollection<SirupPosition> DestillatPositionen { get; } = new();

    [ObservableProperty]
    private SirupRezeptur? selectedRezeptur;

    [ObservableProperty]
    private SirupRezeptur? currentRezeptur;

    [ObservableProperty]
    private SirupKomponente? selectedKomponente;

    [ObservableProperty]
    private string suchText = string.Empty;

    [ObservableProperty]
    private SirupBerechnungErgebnis ergebnis = new();

    partial void OnSuchTextChanged(string value) => RezepturenView.Refresh();

    [RelayCommand]
    private void ClearSuchText() => SuchText = string.Empty;

    public SirupViewModel()
    {
        _context = new AppDbContext();
        _rezepturService = new SirupRezepturService(_context);
        _komponentenService = new SirupKomponentenService(_context);

        RezepturenView = CollectionViewSource.GetDefaultView(Rezepturen);
        RezepturenView.Filter = obj =>
        {
            if (obj is not SirupRezeptur r) return false;
            if (string.IsNullOrWhiteSpace(SuchText)) return true;
            return r.Name.Contains(SuchText, StringComparison.OrdinalIgnoreCase);
        };

        LoadRezepturen();
        LoadKomponenten();
    }

    private void LoadRezepturen()
    {
        Rezepturen.Clear();
        foreach (var rezeptur in _rezepturService.GetAll())
            Rezepturen.Add(rezeptur);
    }

    private void LoadKomponenten()
    {
        VerfuegbareKomponenten.Clear();
        MazerateKomponenten.Clear();
        DestillateKomponenten.Clear();
        foreach (var komponente in _komponentenService.GetAll())
        {
            VerfuegbareKomponenten.Add(komponente);
            if (komponente.Typ == "Destillat")
                DestillateKomponenten.Add(komponente);
            else
                MazerateKomponenten.Add(komponente);
        }
    }

    // ── Rezeptur-Verwaltung ──────────────────────────────────────────────────

    [RelayCommand]
    private void NeueRezeptur()
    {
        LoadKomponenten();
        CurrentRezeptur = new SirupRezeptur
        {
            Name = "Neue Sirup-Rezeptur",
            Erstellungsdatum = DateTime.Now,
            Positionen = new()
        };
    }

    [RelayCommand]
    private void EditRezeptur()
    {
        if (SelectedRezeptur == null) return;

        var kopie = new SirupRezeptur
        {
            Id = SelectedRezeptur.Id,
            Name = SelectedRezeptur.Name,
            Erstellungsdatum = SelectedRezeptur.Erstellungsdatum,
            Bemerkungen = SelectedRezeptur.Bemerkungen,
            GrundmengeMl = SelectedRezeptur.GrundmengeMl,
            WasserMl = SelectedRezeptur.WasserMl,
            ZuckerG = SelectedRezeptur.ZuckerG,
            ZitronensaeureG = SelectedRezeptur.ZitronensaeureG,
            VerduennungTeileSirup = SelectedRezeptur.VerduennungTeileSirup,
            VerduennungTeileSodawasser = SelectedRezeptur.VerduennungTeileSodawasser,
            SirupMengeVerduennungMl = SelectedRezeptur.SirupMengeVerduennungMl,
            Positionen = new ObservableCollection<SirupPosition>(SelectedRezeptur.Positionen.Select(p => new SirupPosition
            {
                Id = p.Id,
                SirupRezepturId = p.SirupRezepturId,
                Typ = p.Typ,
                KomponenteId = p.KomponenteId,
                Komponente = p.Komponente,
                ManuelleBezeichnung = p.ManuelleBezeichnung,
                ManuellerAlkoholgehalt = p.ManuellerAlkoholgehalt,
                MengeMl = p.MengeMl
            }))
        };
        CurrentRezeptur = kopie;
    }

    [RelayCommand]
    private void SaveRezeptur()
    {
        if (CurrentRezeptur == null) return;

        if (string.IsNullOrWhiteSpace(CurrentRezeptur.Name))
        {
            MessageBox.Show("Der Name darf nicht leer sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        // Navigation-Property vor dem Speichern auf null setzen — EF braucht nur KomponenteId (FK)
        foreach (var position in CurrentRezeptur.Positionen)
            position.Komponente = null;

        if (CurrentRezeptur.Id == 0)
            _rezepturService.Add(CurrentRezeptur);
        else
            _rezepturService.Update(CurrentRezeptur);

        LoadRezepturen();
        CurrentRezeptur = null;
    }

    [RelayCommand]
    private void DeleteRezeptur()
    {
        if (SelectedRezeptur != null && MessageBox.Show($"Sirup-Rezeptur '{SelectedRezeptur.Name}' löschen?", "Bestätigung", MessageBoxButton.YesNo) == MessageBoxResult.Yes)
        {
            _rezepturService.Delete(SelectedRezeptur.Id);
            LoadRezepturen();
        }
    }

    [RelayCommand]
    private void CancelEdit() => CurrentRezeptur = null;

    partial void OnCurrentRezepturChanged(SirupRezeptur? value)
    {
        MazeratPositionen.Clear();
        DestillatPositionen.Clear();

        if (value != null)
        {
            LoadKomponenten();
            value.PropertyChanged += OnRezepturPropertyChanged;
            foreach (var position in value.Positionen)
            {
                WirePosition(position);
                (position.Typ == "Destillat" ? DestillatPositionen : MazeratPositionen).Add(position);
            }
        }
        UpdateBerechnungen();
    }

    private void OnRezepturPropertyChanged(object? sender, PropertyChangedEventArgs e) => UpdateBerechnungen();

    private void WirePosition(SirupPosition position) => position.PropertyChanged += OnPositionPropertyChanged;

    private void OnPositionPropertyChanged(object? sender, PropertyChangedEventArgs e)
    {
        if (sender is not SirupPosition position) return;

        if (e.PropertyName == nameof(SirupPosition.KomponenteId))
        {
            position.Komponente = position.KomponenteId.HasValue
                ? VerfuegbareKomponenten.FirstOrDefault(k => k.Id == position.KomponenteId.Value)
                : null;
            if (position.Komponente != null)
                position.ManuellerAlkoholgehalt = position.Komponente.Alkoholgehalt;
        }

        UpdateBerechnungen();
    }

    private void UpdateBerechnungen()
    {
        Ergebnis = CurrentRezeptur != null ? SirupBerechnungService.Berechne(CurrentRezeptur) : new SirupBerechnungErgebnis();
    }

    // ── Block 2: Mazerate/Destillate hinzufügen/entfernen ──────────────────

    private const int MaxPositionenJeTyp = 5;

    [RelayCommand]
    private void AddMazerat() => AddPosition("Mazerat");

    [RelayCommand]
    private void AddDestillat() => AddPosition("Destillat");

    private void AddPosition(string typ)
    {
        if (CurrentRezeptur == null) return;
        var zielListe = typ == "Destillat" ? DestillatPositionen : MazeratPositionen;
        if (zielListe.Count >= MaxPositionenJeTyp)
        {
            MessageBox.Show($"Es sind maximal {MaxPositionenJeTyp} {typ}e je Rezeptur möglich.", "Grenze erreicht", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var position = new SirupPosition { SirupRezepturId = CurrentRezeptur.Id, Typ = typ };
        WirePosition(position);
        zielListe.Add(position);
        CurrentRezeptur.Positionen.Add(position);
        UpdateBerechnungen();
    }

    [RelayCommand]
    private void RemovePosition(SirupPosition? position)
    {
        if (CurrentRezeptur == null || position == null) return;
        MazeratPositionen.Remove(position);
        DestillatPositionen.Remove(position);
        CurrentRezeptur.Positionen.Remove(position);
        UpdateBerechnungen();
    }

    [RelayCommand]
    private void UebernehmeDosierung(SirupPosition? position)
    {
        if (CurrentRezeptur == null || position?.Komponente == null) return;
        position.MengeMl = Math.Round(SirupBerechnungService.Dosierungsvorschlag(position.Komponente, CurrentRezeptur.GrundmengeMl), 2);
    }

    // ── Block 3: Verdünnung ──────────────────────────────────────────────────

    [RelayCommand]
    private void GanzeChargeUebernehmen()
    {
        if (CurrentRezeptur == null) return;
        CurrentRezeptur.SirupMengeVerduennungMl = Math.Round(Ergebnis.GesamtvolumenSirupMl, 2);
    }

    // ── Komponenten-Datenbank verwalten ──────────────────────────────────────

    [RelayCommand]
    private void AddKomponente()
    {
        var neu = new SirupKomponente { Name = "Neue Komponente", Typ = "Mazerat", Alkoholgehalt = 53 };
        _komponentenService.Add(neu);
        LoadKomponenten();
        SelectedKomponente = VerfuegbareKomponenten.FirstOrDefault(k => k.Id == neu.Id);
    }

    [RelayCommand]
    private void SaveKomponente()
    {
        if (SelectedKomponente == null) return;
        if (string.IsNullOrWhiteSpace(SelectedKomponente.Name))
        {
            MessageBox.Show("Der Name darf nicht leer sein.", "Validierung", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        _komponentenService.Update(SelectedKomponente);
        LoadKomponenten();
    }

    [RelayCommand]
    private void DeleteKomponente()
    {
        if (SelectedKomponente == null) return;

        if (_komponentenService.IsReferenced(SelectedKomponente.Id))
        {
            MessageBox.Show(
                $"'{SelectedKomponente.Name}' wird noch in mindestens einer Sirup-Rezeptur verwendet und kann nicht gelöscht werden.",
                "Löschen nicht möglich", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        if (MessageBox.Show($"Komponente '{SelectedKomponente.Name}' löschen?", "Bestätigung", MessageBoxButton.YesNo) == MessageBoxResult.Yes)
        {
            _komponentenService.Delete(SelectedKomponente.Id);
            LoadKomponenten();
        }
    }

    // ── Export ────────────────────────────────────────────────────────────

    [RelayCommand]
    private void ExportXlsx()
    {
        if (CurrentRezeptur == null)
        {
            MessageBox.Show("Bitte zuerst eine Sirup-Rezeptur anlegen oder öffnen.", "Kein Export möglich", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var dialog = new SaveFileDialog
        {
            Filter = "Excel-Datei (*.xlsx)|*.xlsx",
            FileName = $"Sirup_{CurrentRezeptur.Name.Replace(" ", "_")}",
            DefaultExt = ".xlsx"
        };
        if (dialog.ShowDialog() == true)
        {
            try
            {
                _exportService.ExportSirupRezepturXlsx(CurrentRezeptur, Ergebnis, dialog.FileName);
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
