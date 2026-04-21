namespace RezepturMeister.Models;

public class NaehrwertErgebnis
{
    // Werte pro 100 g Rezeptur
    public double Energie_kJ { get; set; }
    public double Energie_kcal { get; set; }
    public double Fett { get; set; }
    public double GesaettigteFettsaeuren { get; set; }
    public double Kohlenhydrate { get; set; }
    public double Zucker { get; set; }
    public double Ballaststoffe { get; set; }
    public double Eiweiss { get; set; }
    public double Salz { get; set; }

    // Rezepturdichte (g/ml) für die per-100-ml-Berechnung
    public double Dichte { get; set; }

    // Abgeleitete Werte pro 100 ml
    public double Energie_kJ_per100ml => Energie_kJ * Dichte;
    public double Energie_kcal_per100ml => Energie_kcal * Dichte;
    public double Fett_per100ml => Fett * Dichte;
    public double GesaettigteFettsaeuren_per100ml => GesaettigteFettsaeuren * Dichte;
    public double Kohlenhydrate_per100ml => Kohlenhydrate * Dichte;
    public double Zucker_per100ml => Zucker * Dichte;
    public double Ballaststoffe_per100ml => Ballaststoffe * Dichte;
    public double Eiweiss_per100ml => Eiweiss * Dichte;
    public double Salz_per100ml => Salz * Dichte;

    // Zutatenliste absteigend nach Gewicht (für Deklaration)
    public List<ZutatNaehrwertInfo> Zutatenliste { get; set; } = new();

    // Rohstoffe ohne hinterlegte Nährwertdaten
    public List<string> FehlendeDaten { get; set; } = new();
}

public class ZutatNaehrwertInfo
{
    public string Name { get; set; } = string.Empty;
    public double Gewicht_g { get; set; }
    public double Prozent { get; set; }
    public bool HatNaehrwertdaten { get; set; }
}
