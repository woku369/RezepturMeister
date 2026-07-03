using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister;

public static class RezepturSeeder
{
    private record ZutatDaten(string RohstoffName, double Menge);

    // Beide Rezepturen aus dem Alpen-Spritz-Projektwissen (Stand 03.07.2026), je auf 1.000 g/ml
    // Gesamtansatz normiert. Quantitativer Stand: Original 28.10.2024, Weinbasis 03.07.2026
    // (aktueller Arbeitsstand, Ziel-ABV noch offen — siehe Bemerkungen).

    private static readonly ZutatDaten[] Original =
    {
        new("Zitronensäure", 5.130),
        new("Zucker", 78.261),
        new("Agrana AGENABON 20.160", 46.957),
        new("Esarom 106150 Neutral-Emulsion", 0.783),
        new("Gurktaler Kräuterauszug Underberg", 11.721),
        new("Vögele Ingredients Sanddorn-Aroma 200 0721", 2.739),
        new("Esarom 911734 Pfirsich-Aroma NT", 0.939),
        new("Tastepoint AROMA BLUTORANGE 22213", 0.350),
        new("Frischkräutermazerat SWSK", 0.447),
        new("Ethanol 96% - Lohnabfüller Bubee", 35.000),
        new("Wasser", 817.673)
    };

    private static readonly ZutatDaten[] Weinbasis =
    {
        new("Wein (Alpen-Spritz Weinbasis)", 510.000),
        new("Zucker", 78.261),
        new("Zitronensäure", 5.130),
        new("Agrana AGENABON 20.160", 46.957),
        new("Esarom 106150 Neutral-Emulsion", 0.783),
        new("Sanddorndestillat", 7.000),
        new("Vögele Ingredients Sanddorn-Aroma 200 0721", 2.739),
        new("Kräuterdestillat (Alpen-Spritz Weinbasis)", 13.000),
        new("Esarom 911734 Pfirsich-Aroma NT", 0.5),
        new("Tastepoint AROMA BLUTORANGE 22213", 0.15),
        new("Wasser", 335.480)
    };

    public static void Seed()
    {
        // Rohstoffe müssen vorhanden sein, damit die Zutaten per RohstoffId verknüpft werden können.
        RohstoffSeeder.Seed();

        using var context = new AppDbContext();

        AddRezeptur(
            context,
            nummer: "1.0",
            name: "Alpen-Spritz (Original, Neutralalkohol-Basis)",
            produktgruppe: "Spirituosenmischgetränk",
            erstellungsdatum: new DateTime(2024, 10, 28),
            bemerkungen:
                "Sachbezeichnung „Alkoholisches Mischgetränk mit Sanddornlikör (42 %)“. 250-ml-Dose, " +
                "4,5 % vol (Label-Wert), 4,4386 % vol rechnerisch. UVP 2,59 € brutto zzgl. 0,25 € " +
                "Einwegpfand (separat, USt-frei). Rohstoffkosten 9,0 Cent + Alkoholsteuer 13,5 Cent = " +
                "22,5 Cent/Dose. Referenzrezeptur für den Vergleich mit der Weinbasis-Variante (1.1).",
            zutaten: Original);

        AddRezeptur(
            context,
            nummer: "1.1",
            name: "Alpen-Spritz Weinbasis (Rebsorte/Lieferant offen)",
            produktgruppe: "Weinhaltiges Getränk (finale Kategorie noch offen, s. Bemerkungen)",
            erstellungsdatum: new DateTime(2026, 7, 3),
            bemerkungen:
                "Reformulierung auf Weinbasis, Ziel: Wegfall der Alkoholsteuer (AlkStG 2022 nicht " +
                "einschlägig, KN 2204/2205/2206). Kategorie voraussichtlich „aromatisierter " +
                "weinhaltiger Cocktail“ (VO (EU) 251/2014: Weinanteil ≥ 50 %, kein Alkoholzusatz über " +
                "den Wein hinaus zulässig). Sanddorn- und Kräuterdestillat hier als reiner Aromaträger " +
                "nach Art. 8 Abs. 2 dosiert — RECHTLICH NOCH NICHT AGES-GEPRÜFT, Destillat-Summe " +
                "bewusst auf max. 2 % gedeckelt. End-ABV rechnerisch ~7,11 % vol; Zielkorridor " +
                "6,5–7 % vol wird sensorisch im 1-L-Pilotansatz entschieden, nicht rechnerisch. " +
                "Zitronensäure-Menge = Original übernommen, ggf. zu hoch da Wein Eigensäure mitbringt " +
                "(noch zu prüfen). Rohstoffkosten 21,8 Cent + 0 Cent Alkoholsteuer = 21,8 Cent/Dose " +
                "(Netto-Vorteil ggü. Original nur ca. 0,7 Cent/Dose — hochsensitiv gegenüber Wein- und " +
                "Destillatpreis). Offene Punkte: Wein-Rebsorte/-Lieferant, Farbstoff-E110-Dosierung, " +
                "KN-Einreihung (vZTA empfohlen), Nährwerttabelle nach Laboranalyse der finalen Rezeptur.",
            zutaten: Weinbasis);
    }

    private static void AddRezeptur(
        AppDbContext context,
        string nummer,
        string name,
        string produktgruppe,
        DateTime erstellungsdatum,
        string bemerkungen,
        ZutatDaten[] zutaten)
    {
        if (context.Rezepturen.Any(r => r.Nummer == nummer))
            return;

        var rezeptur = new Rezeptur
        {
            Nummer = nummer,
            Name = name,
            Produktgruppe = produktgruppe,
            Erstellungsdatum = erstellungsdatum,
            Chargennummer = string.Empty,
            Bemerkungen = bemerkungen
        };

        foreach (var z in zutaten)
        {
            var rohstoffId = context.Rohstoffe.First(r => r.Name == z.RohstoffName).Id;
            rezeptur.Zutaten.Add(new Zutat
            {
                RohstoffId = rohstoffId,
                Menge = z.Menge,
                Einheit = "g"
            });
        }

        context.Rezepturen.Add(rezeptur);
        context.SaveChanges();
    }
}

// Zum Ausführen: In MainWindow.xaml.cs oder App.xaml.cs temporär einfügen:
// RezepturSeeder.Seed();
