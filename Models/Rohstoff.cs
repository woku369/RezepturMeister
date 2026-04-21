using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RezepturMeister.Models;

public class Rohstoff
{
    [Key]
    public int Id { get; set; }

    [Required]
    public string Name { get; set; } = string.Empty;

    public string Kategorie { get; set; } = string.Empty;

    [Required]
    public double Dichte { get; set; } // g/ml

    public double Alkoholgehalt { get; set; } // % vol.

    public decimal Preis { get; set; } // € pro kg oder l

    public string Lieferant { get; set; } = string.Empty;

    public string DatenblattPfad { get; set; } = string.Empty; // Lokaler Pfad zu PDF/XLSX

    // Erweiterbare Eigenschaften als JSON-String
    public string ZusatzlicheEigenschaften { get; set; } = "{}";

    // Nährwertdaten pro 100 g (null = noch nicht eingepflegt)
    public double? Energie_kJ { get; set; }
    public double? Energie_kcal { get; set; }
    public double? Fett { get; set; }
    public double? GesaettigteFettsaeuren { get; set; }
    public double? Kohlenhydrate { get; set; }
    public double? Zucker { get; set; }
    public double? Ballaststoffe { get; set; }
    public double? Eiweiss { get; set; }
    public double? Salz { get; set; }
}