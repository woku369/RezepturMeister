using RezepturMeister.Models;

namespace RezepturMeister.Services;

public static class NaehrwertService
{
    public static NaehrwertErgebnis Berechne(Rezeptur rezeptur)
    {
        var ergebnis = new NaehrwertErgebnis();

        double gesamtGewicht_g = 0;
        double gesamtVolumen_ml = 0;

        foreach (var zutat in rezeptur.Zutaten)
        {
            double dichte = zutat.Rohstoff?.Dichte ?? zutat.ManuelleDichte;
            if (dichte <= 0) dichte = 1.0;
            gesamtGewicht_g  += zutat.Einheit == "ml" ? zutat.Menge * dichte : zutat.Menge;
            gesamtVolumen_ml += zutat.Einheit == "ml" ? zutat.Menge : zutat.Menge / dichte;
        }

        if (gesamtGewicht_g <= 0) return ergebnis;

        ergebnis.Dichte = gesamtVolumen_ml > 0 ? gesamtGewicht_g / gesamtVolumen_ml : 1.0;

        // Zutaten absteigend nach Gewicht sortieren (LMIV-Anforderung)
        var sortiert = rezeptur.Zutaten
            .Select(z =>
            {
                double d = z.Rohstoff?.Dichte ?? z.ManuelleDichte;
                if (d <= 0) d = 1.0;
                return (Zutat: z, Gewicht_g: z.Einheit == "ml" ? z.Menge * d : z.Menge);
            })
            .OrderByDescending(x => x.Gewicht_g)
            .ToList();

        double sum_kJ = 0, sum_kcal = 0, sum_fett = 0, sum_gs = 0;
        double sum_kh = 0, sum_zk = 0, sum_bf = 0, sum_ew = 0, sum_sz = 0;

        foreach (var (zutat, gewicht_g) in sortiert)
        {
            string name = zutat.Rohstoff?.Name ?? zutat.ManuellerName;
            bool hatDaten = zutat.Rohstoff?.Energie_kJ.HasValue == true;

            if (hatDaten)
            {
                double f = gewicht_g / 100.0;
                sum_kJ   += zutat.Rohstoff!.Energie_kJ!.Value * f;
                sum_kcal += (zutat.Rohstoff.Energie_kcal ?? 0) * f;
                sum_fett += (zutat.Rohstoff.Fett ?? 0) * f;
                sum_gs   += (zutat.Rohstoff.GesaettigteFettsaeuren ?? 0) * f;
                sum_kh   += (zutat.Rohstoff.Kohlenhydrate ?? 0) * f;
                sum_zk   += (zutat.Rohstoff.Zucker ?? 0) * f;
                sum_bf   += (zutat.Rohstoff.Ballaststoffe ?? 0) * f;
                sum_ew   += (zutat.Rohstoff.Eiweiss ?? 0) * f;
                sum_sz   += (zutat.Rohstoff.Salz ?? 0) * f;
            }
            else
            {
                ergebnis.FehlendeDaten.Add(name);
            }

            ergebnis.Zutatenliste.Add(new ZutatNaehrwertInfo
            {
                Name = name,
                Gewicht_g = gewicht_g,
                Prozent = gewicht_g / gesamtGewicht_g * 100,
                HatNaehrwertdaten = hatDaten
            });
        }

        // Normieren auf 100 g Rezeptur
        double n = 100.0 / gesamtGewicht_g;
        ergebnis.Energie_kJ             = sum_kJ   * n;
        ergebnis.Energie_kcal           = sum_kcal * n;
        ergebnis.Fett                   = sum_fett * n;
        ergebnis.GesaettigteFettsaeuren = sum_gs   * n;
        ergebnis.Kohlenhydrate          = sum_kh   * n;
        ergebnis.Zucker                 = sum_zk   * n;
        ergebnis.Ballaststoffe          = sum_bf   * n;
        ergebnis.Eiweiss                = sum_ew   * n;
        ergebnis.Salz                   = sum_sz   * n;

        return ergebnis;
    }
}
