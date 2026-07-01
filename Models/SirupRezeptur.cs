using System.Collections.ObjectModel;
using System.ComponentModel.DataAnnotations;
using CommunityToolkit.Mvvm.ComponentModel;

namespace RezepturMeister.Models;

// ObservableObject, damit Änderungen an den Mengenfeldern die Berechnung im ViewModel live auslösen.
public partial class SirupRezeptur : ObservableObject
{
    [Key]
    public int Id { get; set; }

    [ObservableProperty]
    private string name = "Neue Sirup-Rezeptur";

    [Required]
    public DateTime Erstellungsdatum { get; set; }

    [ObservableProperty]
    private string bemerkungen = string.Empty;

    // ── Block 1: Basissirup (Wasser, Zucker, Zitronensäure) ──
    // Grundmenge = Zielvolumen des fertigen, unaromatisierten Basissirups (Standard: 1 l)
    [ObservableProperty]
    private double grundmengeMl = 1000;

    [ObservableProperty]
    private double wasserMl = 630;

    [ObservableProperty]
    private double zuckerG = 310;

    [ObservableProperty]
    private double zitronensaeureG = 4;

    // ── Block 3: Verdünnung mit Sodawasser ──
    [ObservableProperty]
    private double verduennungTeileSirup = 1;

    [ObservableProperty]
    private double verduennungTeileSodawasser = 6;

    // Sirupmenge, für die die Verdünnungsberechnung durchgeführt wird (z.B. ganze Charge oder eine Portion)
    [ObservableProperty]
    private double sirupMengeVerduennungMl = 1000;

    public virtual ObservableCollection<SirupPosition> Positionen { get; set; } = new();
}

// Eine Position im 2. Block: ein Mazerat oder Destillat mit eingesetzter Menge
public partial class SirupPosition : ObservableObject
{
    [Key]
    public int Id { get; set; }

    public int SirupRezepturId { get; set; }

    public virtual SirupRezeptur SirupRezeptur { get; set; } = null!;

    // "Mazerat" oder "Destillat"
    [ObservableProperty]
    private string typ = "Mazerat";

    // Auswahl aus der Komponenten-Datenbank (optional, kann auch manuell befüllt sein)
    [ObservableProperty]
    private int? komponenteId;

    public virtual SirupKomponente? Komponente { get; set; }

    [ObservableProperty]
    private string manuelleBezeichnung = string.Empty;

    [ObservableProperty]
    private double manuellerAlkoholgehalt;

    [ObservableProperty]
    private double mengeMl;
}
