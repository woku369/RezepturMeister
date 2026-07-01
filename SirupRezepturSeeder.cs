using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister;

// Startrezepturen für den Siruprechner — die 5 Gurktaler-Alpenkräuter-Kernrezepte
// plus der Bittersirup "Wermut-Zitrus" (Wissensbasis "Alkoholfreie Sirupe", Stand Juli 2026).
// Referenzwerte/Startpunkte für Piloten, noch nicht final validiert.
// Idempotent: bereits vorhandene Rezepturnamen werden nicht erneut angelegt.
// Setzt voraus, dass SirupKomponentenSeeder.Seed() bereits gelaufen ist.
public static class SirupRezepturSeeder
{
    public static void Seed()
    {
        using var context = new AppDbContext();

        int? KomponenteId(string name) => context.SirupKomponenten.FirstOrDefault(k => k.Name == name)?.Id;

        SirupPosition Position(string typ, string komponentenName, double mengeMl)
        {
            var id = KomponenteId(komponentenName);
            return new SirupPosition
            {
                Typ = typ,
                KomponenteId = id,
                ManuellerAlkoholgehalt = id.HasValue ? context.SirupKomponenten.First(k => k.Id == id).Alkoholgehalt : 0,
                MengeMl = mengeMl
            };
        }

        void AddIfMissing(SirupRezeptur rezeptur)
        {
            if (!context.SirupRezepturen.Any(r => r.Name == rezeptur.Name))
                context.SirupRezepturen.Add(rezeptur);
        }

        AddIfMissing(new SirupRezeptur
        {
            Name = "Alpenkräuter Frisch",
            Erstellungsdatum = DateTime.Now,
            Bemerkungen = "Kühlend-frisch, Zitrus-Kräuter, leicht mentholig. Referenz, nicht final validiert. " +
                "Achtung: Alkohol-Check bei 1+6 rechnerisch ≈ 0,5 % vol. im Endprodukt — Grenzwert wird erreicht! " +
                "Mazerat-Anteil ggf. reduzieren oder Mazerat-ABV vorab messtechnisch verifizieren.",
            Positionen = new()
            {
                Position("Mazerat", "Pfefferminze-Mazerat", 40),
                Position("Mazerat", "Zitronenmelisse-Mazerat", 30),
            }
        });

        AddIfMissing(new SirupRezeptur
        {
            Name = "Mediterran-Alpin",
            Erstellungsdatum = DateTime.Now,
            Bemerkungen = "Warm-würzig, mediterrane Kräuter, Orangenfrische. Referenz, nicht final validiert.",
            Positionen = new()
            {
                Position("Mazerat", "Oregano-Mazerat", 22),
                Position("Mazerat", "Thymian-Mazerat", 18),
                Position("Mazerat", "Salbei-Mazerat", 13),
                Position("Destillat", "Orange-Destillat", 10),
            }
        });

        AddIfMissing(new SirupRezeptur
        {
            Name = "Zitrus-Alpin",
            Erstellungsdatum = DateTime.Now,
            Bemerkungen = "Citral-intensiv, frisch-blumig, leicht scharf (Ingwer). Referenz, nicht final validiert.",
            Positionen = new()
            {
                Position("Destillat", "Zitronengras-Destillat", 13),
                Position("Destillat", "Zitronenverbene-Destillat", 10),
                Position("Mazerat", "Zitronenmelisse-Mazerat", 18),
                Position("Mazerat", "Ingwer-Mazerat", 7),
            }
        });

        AddIfMissing(new SirupRezeptur
        {
            Name = "Sanddorn & Wellness",
            Erstellungsdatum = DateTime.Now,
            ZitronensaeureG = 2, // Sanddorn bringt Eigensäure — Basissirup-Zitronensäure reduziert
            Bemerkungen = "Beerensäuerlich, Wellness-Komplex, leicht frisch. Referenz, nicht final validiert. " +
                "Sanddorn bringt Fruchtfarbe und Eigensäure — Zitronensäure im Basissirup deshalb reduziert.",
            Positionen = new()
            {
                Position("Mazerat", "Sanddorn-Mazerat", 48),
                Position("Mazerat", "Echinacea-Mazerat", 18),
                Position("Mazerat", "Koriander-Mazerat", 10),
                Position("Mazerat", "Pfefferminze-Mazerat", 9),
            }
        });

        AddIfMissing(new SirupRezeptur
        {
            Name = "Bitter Alpin",
            Erstellungsdatum = DateTime.Now,
            Bemerkungen = "Herb-bitter, Kräuter-Komplex, warm-würzig — Zielgruppe Erwachsene. Referenz, nicht final validiert. " +
                "Wermut + Eberreis ergeben die stärkste Bitterkeit; Menge nach tatsächlichem Bitterwert des Mazerats anpassen.",
            Positionen = new()
            {
                Position("Mazerat", "Wermut-Mazerat", 10),
                Position("Mazerat", "Eberrauten-Mazerat", 7),
                Position("Mazerat", "Salbei-Mazerat", 13),
                Position("Mazerat", "Zimt-Mazerat", 10),
                Position("Mazerat", "Gewürznelken-Mazerat", 4),
            }
        });

        AddIfMissing(new SirupRezeptur
        {
            Name = "Bittersirup Wermut-Zitrus",
            Erstellungsdatum = DateTime.Now,
            Bemerkungen = "Konzept aus dem JUFA-Workshop-Kontext (\"Gurktaler zum Nachbauen\"), Alternative zu \"Bitter Alpin\" mit Eberraute statt Salbei/Zimt/Nelken. " +
                "Offener Punkt: Thujon-Grenzwert für alkoholfreie Getränke vor Serienproduktion verifizieren (Anhang III VO 1334/2008, korrekte Lebensmittelkategorie statt Spirituosen-Wert).",
            Positionen = new()
            {
                Position("Mazerat", "Wermut-Mazerat", 9),
                Position("Mazerat", "Eberrauten-Mazerat", 7),
                Position("Destillat", "Zitrusdestillat", 13),
            }
        });

        context.SaveChanges();
    }
}
