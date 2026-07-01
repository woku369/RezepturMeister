using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister;

// Startdaten für die Mazerat-/Destillat-"Datenbank" des Siruprechners —
// realer Gurktaler-Rohstoffbestand (Wissensbasis "Alkoholfreie Sirupe", Stand Juli 2026).
// Idempotent: bereits vorhandene Namen werden nicht erneut angelegt.
public static class SirupKomponentenSeeder
{
    public static void Seed()
    {
        using var context = new AppDbContext();

        var komponenten = new[]
        {
            // Mazerate, ca. 53 % vol. (Frischkräutermazerat SWSK-Referenz), Dosierung = Mittelwert der Kernrezept-Bandbreiten
            new SirupKomponente { Name = "Oregano-Mazerat",     Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 22.5 },
            new SirupKomponente { Name = "Salbei-Mazerat",      Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 12.5 },
            new SirupKomponente { Name = "Thymian-Mazerat",     Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 17.5 },
            new SirupKomponente { Name = "Pfefferminze-Mazerat",       Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 40   },
            new SirupKomponente { Name = "Zitronenmelisse-Mazerat",    Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 24   },
            new SirupKomponente { Name = "Echinacea-Mazerat",   Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 17.5 },
            new SirupKomponente { Name = "Zimt-Mazerat",        Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 10   },
            new SirupKomponente { Name = "Gewürznelken-Mazerat", Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 4   },
            new SirupKomponente { Name = "Koriander-Mazerat",   Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 10   },
            new SirupKomponente { Name = "Wermut-Mazerat",      Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 9.5,
                Bemerkung = "Max. 2–4 Tage mazerieren (Absinthin), Thujon-Grenzwert vor Serienproduktion prüfen" },
            new SirupKomponente { Name = "Eberrauten-Mazerat",  Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 6.75,
                Bemerkung = "Thujon-Grenzwert vor Serienproduktion prüfen" },
            new SirupKomponente { Name = "Ingwer-Mazerat",      Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 6.5  },
            new SirupKomponente { Name = "Sanddorn-Mazerat",    Typ = "Mazerat", Alkoholgehalt = 53, DosierungsempfehlungMlProLiter = 47.5,
                Bemerkung = "Bringt Eigensäure — Zitronensäure im Basissirup ggf. reduzieren" },

            // Destillate, ca. 72 % vol. (Zitrusdestillat abweichend, siehe Bemerkung)
            new SirupKomponente { Name = "Zitronengras-Destillat",   Typ = "Destillat", Alkoholgehalt = 72, DosierungsempfehlungMlProLiter = 12.5 },
            new SirupKomponente { Name = "Zitronenverbene-Destillat", Typ = "Destillat", Alkoholgehalt = 72, DosierungsempfehlungMlProLiter = 10   },
            new SirupKomponente { Name = "Orange-Destillat",         Typ = "Destillat", Alkoholgehalt = 72, DosierungsempfehlungMlProLiter = 10   },
            new SirupKomponente { Name = "Frischkräutercocktail-Destillat", Typ = "Destillat", Alkoholgehalt = 72, DosierungsempfehlungMlProLiter = 10,
                Bemerkung = "Aus den ersten 5 Mazeraten" },
            new SirupKomponente { Name = "Zitrusdestillat",          Typ = "Destillat", Alkoholgehalt = 55, DosierungsempfehlungMlProLiter = 12.5,
                Bemerkung = "M2c-Ansatz, abweichender Alkoholgehalt (~55 % statt ~72 %)" },
        };

        foreach (var komponente in komponenten)
        {
            if (!context.SirupKomponenten.Any(k => k.Name == komponente.Name))
                context.SirupKomponenten.Add(komponente);
        }
        context.SaveChanges();
    }
}
