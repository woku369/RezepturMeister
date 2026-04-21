using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister;

public static class RohstoffSeeder
{
    public static void Seed()
    {
        using var context = new AppDbContext();
        var namen = new[]
        {
            "Zitronensäure",
            "Zucker",
            "Agrana AGENABON 20.160",
            "Esarom 106150 Neutral-Emulsion",
            "Esarom 680051 Gelborange 85%",
            "Gurktaler Kräuterauszug Underberg",
            "Vögele Ingredients Sanddorn-Aroma 200 0721",
            "Esarom 911734 Pfirsich-Aroma NT",
            "Tastepoint AROMA BLUTORANGE 22213",
            "Frischkräutermazerat SWSK",
            "Ethanol 96% - Lohnabfüller Bubee"
        };
        foreach (var name in namen)
        {
            if (!context.Rohstoffe.Any(r => r.Name == name))
            {
                context.Rohstoffe.Add(new Rohstoff {
                    Name = name,
                    Dichte = 1.0 // Pflichtfeld, Dummywert
                });
            }
        }
        context.SaveChanges();
    }
}

// Zum Ausführen: In MainWindow.xaml.cs oder App.xaml.cs temporär einfügen:
// RohstoffSeeder.Seed();
