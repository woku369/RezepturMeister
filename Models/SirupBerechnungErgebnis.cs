namespace RezepturMeister.Models;

// Ampel-Status je nach Abstand des End-Alkoholgehalts zum 0,5 %-Grenzwert
public enum AlkoholAmpel
{
    Gruen,
    Gelb,
    Rot
}

// Ergebnis der Siruprechner-Berechnung (Block 2 und Block 3)
public class SirupBerechnungErgebnis
{
    public double MazeratVolumenMl { get; set; }
    public double DestillatVolumenMl { get; set; }

    // Basissirup (Grundmenge) + Mazerate + Destillate
    public double GesamtvolumenSirupMl { get; set; }

    public double ReinerAlkoholMl { get; set; }

    // Alkoholgehalt des aromatisierten Sirups, vor Verdünnung (für die Alkoholfrei-Einstufung nicht relevant)
    public double AbvSirup { get; set; }

    public double SodawasserMl { get; set; }
    public double GesamtGetraenkMl { get; set; }

    // Alkoholgehalt des fertig verdünnten Getränks (1 Teil Sirup + n Teile Sodawasser)
    public double EndAbv { get; set; }

    // true, wenn EndAbv < 0,5 % vol. (Grenze für "alkoholfrei")
    public bool IstAlkoholfrei { get; set; }

    // Grün = deutlich unter der Grenze, Gelb = nahe der Grenze (Puffer prüfen), Rot = Grenze erreicht/überschritten
    public AlkoholAmpel Ampel { get; set; }
}
