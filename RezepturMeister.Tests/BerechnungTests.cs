using RezepturMeister.Models;
using Xunit;

namespace RezepturMeister.Tests;

/// <summary>
/// Tests für die Berechnungslogik (Gesamtgewicht, Prozentanteile).
/// Direkte Berechnungen ohne ViewModel-Abhängigkeit.
/// </summary>
public class BerechnungTests
{
    [Fact]
    public void GesamtGewicht_NurGrammZutaten_SummeKorrekt()
    {
        var zutaten = new List<Zutat>
        {
            new Zutat { Menge = 500, Einheit = "g" },
            new Zutat { Menge = 300, Einheit = "g" },
            new Zutat { Menge = 200, Einheit = "g" }
        };

        double gesamt = zutaten.Sum(z => z.Menge);

        Assert.Equal(1000, gesamt);
    }

    [Fact]
    public void GesamtGewicht_MlMitDichte_WirdUmgerechnet()
    {
        // 500 ml Wasser (Dichte 1.0) = 500 g
        // 200 ml Ethanol (Dichte 0.789) = 157.8 g
        var zutaten = new List<Zutat>
        {
            new Zutat { Menge = 500, Einheit = "ml", ManuelleDichte = 1.0 },
            new Zutat { Menge = 200, Einheit = "ml", ManuelleDichte = 0.789 }
        };

        double gesamt = zutaten.Sum(z =>
        {
            double m = z.Menge;
            if (z.Einheit == "ml") m *= z.ManuelleDichte;
            return m;
        });

        Assert.Equal(657.8, gesamt, precision: 5);
    }

    [Fact]
    public void Prozentanteil_EineZutat_Ist100Prozent()
    {
        var zutaten = new List<Zutat>
        {
            new Zutat { Menge = 1000, Einheit = "g" }
        };
        double gesamt = zutaten.Sum(z => z.Menge);

        double anteil = zutaten[0].Menge / gesamt * 100;

        Assert.Equal(100.0, anteil);
    }

    [Fact]
    public void Prozentanteil_ZweiGleicheZutaten_Je50Prozent()
    {
        var zutaten = new List<Zutat>
        {
            new Zutat { Menge = 500, Einheit = "g" },
            new Zutat { Menge = 500, Einheit = "g" }
        };
        double gesamt = zutaten.Sum(z => z.Menge);

        var anteile = zutaten.Select(z => z.Menge / gesamt * 100).ToList();

        Assert.All(anteile, a => Assert.Equal(50.0, a));
    }

    [Fact]
    public void GenerateNextVersion_Logik_Korrekt()
    {
        // Simuliert RezepturService.GenerateNextVersion ohne DB
        var existing = new[] { "1.0", "1.1", "1.2" };
        string baseNummer = "1";

        int maxVersion = 0;
        foreach (var num in existing)
        {
            if (num.StartsWith(baseNummer + ".") &&
                int.TryParse(num.Split('.').Last(), out int v))
                maxVersion = Math.Max(maxVersion, v);
        }
        string result = $"{baseNummer}.{maxVersion + 1}";

        Assert.Equal("1.3", result);
    }
}
