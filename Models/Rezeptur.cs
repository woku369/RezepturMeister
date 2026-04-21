using System.Collections.ObjectModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RezepturMeister.Models;

public class Rezeptur
{
    [Key]
    public int Id { get; set; }

    [Required]
    public string Nummer { get; set; } = string.Empty; // z.B. "1.0", "1.1"

    public string Name { get; set; } = string.Empty; // Rezepturname

    [Required]
    public DateTime Erstellungsdatum { get; set; }

    public string Chargennummer { get; set; } = string.Empty;

    public string Bemerkungen { get; set; } = string.Empty;

    public virtual ObservableCollection<Zutat> Zutaten { get; set; } = new();
}

public partial class Zutat
{
    [Key]
    public int Id { get; set; }

    public int RezepturId { get; set; }

    public virtual Rezeptur Rezeptur { get; set; } = null!;

    // Entweder aus Rohstoff-DB oder manuell
    public int? RohstoffId { get; set; }
    public virtual Rohstoff? Rohstoff { get; set; }

    // Für manuelle Zutaten
    public string ManuellerName { get; set; } = string.Empty;
    public double ManuelleDichte { get; set; }
    public double ManuellerAlkoholgehalt { get; set; }

    [Required]
    public double Menge { get; set; } // g oder ml

    [Required]
    public string Einheit { get; set; } = "g"; // "g" oder "ml"

    private double _prozent;
    public double Prozent
    {
        get => _prozent;
        set
        {
            if (_prozent != value)
            {
                _prozent = value;
                PropertyChanged?.Invoke(this, new System.ComponentModel.PropertyChangedEventArgs(nameof(Prozent)));
            }
        }
    }
}