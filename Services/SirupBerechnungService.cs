using RezepturMeister.Models;

namespace RezepturMeister.Services;

public static class SirupBerechnungService
{
    // Unterhalb dieser Grenze (% vol.) gilt ein mit Sodawasser verdünntes Getränk als alkoholfrei
    public const double AlkoholfreiGrenzeAbv = 0.5;

    // Ab dieser Schwelle (80 % der Grenze) wechselt die Ampel von Grün auf Gelb — Warnpuffer vor dem Grenzwert
    public const double AlkoholfreiWarnschwelleAbv = AlkoholfreiGrenzeAbv * 0.8;

    public static SirupBerechnungErgebnis Berechne(SirupRezeptur rezeptur)
    {
        var ergebnis = new SirupBerechnungErgebnis();

        ergebnis.MazeratVolumenMl = rezeptur.Positionen
            .Where(p => p.Typ == "Mazerat")
            .Sum(p => p.MengeMl);
        ergebnis.DestillatVolumenMl = rezeptur.Positionen
            .Where(p => p.Typ == "Destillat")
            .Sum(p => p.MengeMl);

        // Der Sirup wird vorgelegt (Grundmenge) und danach mit Mazeraten/Destillaten aromatisiert —
        // die Grundmenge bildet daher die Basis, auf die die Aromenmengen aufgeschlagen werden.
        ergebnis.GesamtvolumenSirupMl = rezeptur.GrundmengeMl + ergebnis.MazeratVolumenMl + ergebnis.DestillatVolumenMl;

        ergebnis.ReinerAlkoholMl = rezeptur.Positionen.Sum(p => p.MengeMl * PositionAlkoholgehalt(p) / 100.0);

        ergebnis.AbvSirup = ergebnis.GesamtvolumenSirupMl > 0
            ? ergebnis.ReinerAlkoholMl / ergebnis.GesamtvolumenSirupMl * 100.0
            : 0.0;

        // Verdünnung: 1 Teil Sirup + n Teile Sodawasser (Sodawasser enthält keinen Alkohol)
        double teileSirup = rezeptur.VerduennungTeileSirup > 0 ? rezeptur.VerduennungTeileSirup : 1;
        ergebnis.SodawasserMl = rezeptur.SirupMengeVerduennungMl * (rezeptur.VerduennungTeileSodawasser / teileSirup);
        ergebnis.GesamtGetraenkMl = rezeptur.SirupMengeVerduennungMl + ergebnis.SodawasserMl;

        ergebnis.EndAbv = ergebnis.GesamtGetraenkMl > 0
            ? ergebnis.AbvSirup * rezeptur.SirupMengeVerduennungMl / ergebnis.GesamtGetraenkMl
            : 0.0;

        ergebnis.IstAlkoholfrei = ergebnis.EndAbv < AlkoholfreiGrenzeAbv;
        ergebnis.Ampel = ergebnis.EndAbv >= AlkoholfreiGrenzeAbv
            ? AlkoholAmpel.Rot
            : ergebnis.EndAbv >= AlkoholfreiWarnschwelleAbv
                ? AlkoholAmpel.Gelb
                : AlkoholAmpel.Gruen;

        return ergebnis;
    }

    // ManuellerAlkoholgehalt ist die maßgebliche Zahl für die Berechnung — bei Auswahl einer Komponente
    // wird sie im ViewModel als Vorschlag übernommen, bleibt aber pro Position frei überschreibbar
    // (z.B. bei Chargenschwankungen).
    public static double PositionAlkoholgehalt(SirupPosition position) => position.ManuellerAlkoholgehalt;

    // Dosierungsvorschlag (ml) für die eingestellte Grundmenge, skaliert von "ml je Liter"
    public static double Dosierungsvorschlag(SirupKomponente komponente, double grundmengeMl) =>
        komponente.DosierungsempfehlungMlProLiter * grundmengeMl / 1000.0;
}
