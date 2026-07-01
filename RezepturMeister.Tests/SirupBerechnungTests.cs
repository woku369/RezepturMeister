using RezepturMeister.Models;
using RezepturMeister.Services;
using Xunit;

namespace RezepturMeister.Tests;

/// <summary>
/// Tests für die Siruprechner-Berechnungslogik (ABV, Verdünnung, Dosierungsvorschlag).
/// Direkte Berechnungen ohne ViewModel-/DB-Abhängigkeit.
/// </summary>
public class SirupBerechnungTests
{
    private static SirupRezeptur NeueRezeptur(double grundmenge = 1000) => new()
    {
        GrundmengeMl = grundmenge,
        VerduennungTeileSirup = 1,
        VerduennungTeileSodawasser = 6
    };

    [Fact]
    public void Berechne_OhneMazerateUndDestillate_IstAlkoholfrei()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.Equal(1000, ergebnis.GesamtvolumenSirupMl);
        Assert.Equal(0, ergebnis.AbvSirup);
        Assert.Equal(0, ergebnis.EndAbv);
        Assert.True(ergebnis.IstAlkoholfrei);
    }

    [Fact]
    public void Berechne_MitMazeratUndDestillat_GesamtvolumenUndAlkoholKorrekt()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 20, ManuellerAlkoholgehalt = 53 });
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Destillat", MengeMl = 10, ManuellerAlkoholgehalt = 75 });
        rezeptur.SirupMengeVerduennungMl = 1030;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.Equal(1030, ergebnis.GesamtvolumenSirupMl, precision: 5);
        Assert.Equal(18.1, ergebnis.ReinerAlkoholMl, precision: 5);
        Assert.Equal(1.7573, ergebnis.AbvSirup, precision: 4);
    }

    [Fact]
    public void Berechne_Verduennung1Zu6_EndAbvIstEinSiebtelDesSirupAbv()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 20, ManuellerAlkoholgehalt = 53 });
        rezeptur.SirupMengeVerduennungMl = 500; // beliebige Teilmenge — für den End-ABV zählt nur das Verhältnis

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.Equal(ergebnis.AbvSirup / 7.0, ergebnis.EndAbv, precision: 6);
        Assert.Equal(3000, ergebnis.SodawasserMl, precision: 5); // 500 * 6
        Assert.Equal(3500, ergebnis.GesamtGetraenkMl, precision: 5);
    }

    [Fact]
    public void Berechne_NiedrigeDosierung_GiltAlsAlkoholfrei()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 20, ManuellerAlkoholgehalt = 53 });
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Destillat", MengeMl = 10, ManuellerAlkoholgehalt = 75 });
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl + 30;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.True(ergebnis.EndAbv < SirupBerechnungService.AlkoholfreiGrenzeAbv);
        Assert.True(ergebnis.IstAlkoholfrei);
    }

    [Fact]
    public void Berechne_HoheDosierung_GiltNichtAlsAlkoholfrei()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 300, ManuellerAlkoholgehalt = 53 });
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl + 300;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.False(ergebnis.IstAlkoholfrei);
        Assert.True(ergebnis.EndAbv >= SirupBerechnungService.AlkoholfreiGrenzeAbv);
    }

    [Fact]
    public void Berechne_DeutlichUnterGrenze_AmpelIstGruen()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 5, ManuellerAlkoholgehalt = 53 });
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl + 5;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.Equal(AlkoholAmpel.Gruen, ergebnis.Ampel);
    }

    [Fact]
    public void Berechne_NaheGrenze_AmpelIstGelb()
    {
        // Reproduziert näherungsweise das Grenzfall-Beispiel "Alpenkräuter Frisch" (Pfefferminze 40 ml + Zitronenmelisse 30 ml, 1+6)
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 40, ManuellerAlkoholgehalt = 53 });
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 30, ManuellerAlkoholgehalt = 53 });
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl + 70;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.True(ergebnis.EndAbv >= SirupBerechnungService.AlkoholfreiWarnschwelleAbv);
        Assert.NotEqual(AlkoholAmpel.Gruen, ergebnis.Ampel);
    }

    [Fact]
    public void Berechne_UeberGrenze_AmpelIstRot()
    {
        var rezeptur = NeueRezeptur();
        rezeptur.Positionen.Add(new SirupPosition { Typ = "Mazerat", MengeMl = 300, ManuellerAlkoholgehalt = 53 });
        rezeptur.SirupMengeVerduennungMl = rezeptur.GrundmengeMl + 300;

        var ergebnis = SirupBerechnungService.Berechne(rezeptur);

        Assert.Equal(AlkoholAmpel.Rot, ergebnis.Ampel);
    }

    [Fact]
    public void Dosierungsvorschlag_SkaliertMitGrundmenge()
    {
        var komponente = new SirupKomponente { DosierungsempfehlungMlProLiter = 10 };

        Assert.Equal(10, SirupBerechnungService.Dosierungsvorschlag(komponente, 1000));
        Assert.Equal(20, SirupBerechnungService.Dosierungsvorschlag(komponente, 2000));
        Assert.Equal(5, SirupBerechnungService.Dosierungsvorschlag(komponente, 500));
    }
}
