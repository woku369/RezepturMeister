using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister;

public static class RohstoffSeeder
{
    private record RohstoffDaten(
        string Name,
        string Kategorie,
        double Alkoholgehalt,
        decimal Preis,
        string ZusatzlicheEigenschaften = "{}");

    // Werte aus dem Alpen-Spritz-Projektwissen (Stand 03.07.2026): reale Einkaufspreise
    // aus COGS_Gurktaler2.xlsx (Alpen-Aperitif-Einkauf) + Planwerte für die neuen
    // Weinbasis-Rohstoffe. Dichte bleibt Dummywert 1.0, da im Projektwissen nicht erhoben.
    private static readonly RohstoffDaten[] Daten =
    {
        new("Zitronensäure", "Säuerungsmittel", 0, 5.91m),
        new("Zucker", "Süßungsmittel", 0, 0.59m),
        new("Agrana AGENABON 20.160", "Glukosesirup", 0, 0.555m),
        new("Esarom 106150 Neutral-Emulsion", "Trübungsmittel", 0, 31.14m),
        new("Esarom 680051 Gelborange 85%", "Farbstoff", 0, 0,
            "{\"Hinweis\":\"E110 Gelborange S, Dosierung fuer Alpen-Spritz nicht verifiziert\"}"),
        new("Gurktaler Kräuterauszug Underberg", "Kräuterauszug", 90, 7.261m),
        new("Vögele Ingredients Sanddorn-Aroma 200 0721", "Aroma", 0, 29.18m),
        new("Esarom 911734 Pfirsich-Aroma NT", "Aroma", 0, 29.01m),
        new("Tastepoint AROMA BLUTORANGE 22213", "Aroma", 0, 20.50m),
        new("Frischkräutermazerat SWSK", "Mazerat", 53, 11.908m),
        new("Ethanol 96% - Lohnabfüller Bubee", "Alkohol", 96, 0.812m),
        new("Wasser", "Sonstiges", 0, 0),

        // Neu für Alpen-Spritz Weinbasis (Reformulierung, Wegfall Alkoholsteuer)
        new("Wein (Alpen-Spritz Weinbasis)", "Wein", 11, 0.80m,
            "{\"Status\":\"Planwert 0,80 EUR/L, Rebsorte und Lieferant noch offen\"}"),
        new("Sanddorndestillat", "Destillat", 75, 12m,
            "{\"Status\":\"Planwert, kein realer Vergleichspreis vorhanden\"}"),
        new("Kräuterdestillat (Alpen-Spritz Weinbasis)", "Destillat", 75, 12m,
            "{\"Status\":\"Bewusste Nutzerentscheidung fuer 12 EUR/L, abweichend vom realen Gurktaler Kraeuterdestillat 161 (4,76 EUR/L, anderes Produkt)\"}")
    };

    public static void Seed()
    {
        using var context = new AppDbContext();

        foreach (var d in Daten)
        {
            var bestehend = context.Rohstoffe.FirstOrDefault(r => r.Name == d.Name);
            if (bestehend == null)
            {
                context.Rohstoffe.Add(new Rohstoff
                {
                    Name = d.Name,
                    Kategorie = d.Kategorie,
                    Dichte = 1.0, // Pflichtfeld, Dummywert
                    Alkoholgehalt = d.Alkoholgehalt,
                    Preis = d.Preis,
                    ZusatzlicheEigenschaften = d.ZusatzlicheEigenschaften
                });
            }
            else if (string.IsNullOrWhiteSpace(bestehend.Kategorie))
            {
                // Nur bisher unbearbeitete (noch nicht kategorisierte) Datensätze anreichern,
                // um manuelle Änderungen des Nutzers nicht zu überschreiben.
                bestehend.Kategorie = d.Kategorie;
                bestehend.Alkoholgehalt = d.Alkoholgehalt;
                bestehend.Preis = d.Preis;
                bestehend.ZusatzlicheEigenschaften = d.ZusatzlicheEigenschaften;
            }
        }

        context.SaveChanges();
    }
}

// Zum Ausführen: In MainWindow.xaml.cs oder App.xaml.cs temporär einfügen:
// RohstoffSeeder.Seed();
