using System.ComponentModel.DataAnnotations;

namespace RezepturMeister.Models;

// "Datenbank" der auswählbaren Mazerate und Destillate für den Siruprechner
public class SirupKomponente
{
    [Key]
    public int Id { get; set; }

    [Required]
    public string Name { get; set; } = string.Empty;

    // "Mazerat" oder "Destillat"
    public string Typ { get; set; } = "Mazerat";

    public double Alkoholgehalt { get; set; } // % vol.

    public double DosierungsempfehlungMlProLiter { get; set; } // ml je Liter Grundmenge Basissirup

    public string Bemerkung { get; set; } = string.Empty;
}
