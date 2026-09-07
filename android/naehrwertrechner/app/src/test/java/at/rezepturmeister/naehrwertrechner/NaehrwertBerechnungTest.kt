package at.rezepturmeister.naehrwertrechner

import at.rezepturmeister.naehrwertrechner.data.NaehrwertQuelle
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.data.SeedData
import at.rezepturmeister.naehrwertrechner.domain.Bezugsgroesse
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NaehrwertBerechnungTest {

    private val zucker = Rohstoff(
        name = "Zucker", energieKj = 1700.0, energieKcal = 400.0, fett = 0.0,
        gesaettigteFettsaeuren = 0.0, kohlenhydrate = 100.0, zucker = 100.0,
        ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0, quelle = NaehrwertQuelle.BERECHNET
    )

    private val wasser = Rohstoff(
        name = "Wasser", energieKj = 0.0, energieKcal = 0.0, fett = 0.0,
        gesaettigteFettsaeuren = 0.0, kohlenhydrate = 0.0, zucker = 0.0,
        ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0, quelle = NaehrwertQuelle.BERECHNET
    )

    @Test
    fun `Zucker-Wasser-Mischung 1 zu 1 ergibt halbe Zuckerwerte pro 100g`() {
        val ergebnis = NaehrwertBerechnung.berechne(
            listOf(
                NaehrwertBerechnung.ZutatMenge(zucker, 50.0),
                NaehrwertBerechnung.ZutatMenge(wasser, 50.0)
            )
        )
        assertEquals(200.0, ergebnis.energieKcal, 0.001)
        assertEquals(50.0, ergebnis.kohlenhydrate, 0.001)
        assertTrue(ergebnis.istVollstaendig)
    }

    @Test
    fun `fehlende Rohstoffdaten werden als unvollstaendig markiert`() {
        val unvollstaendig = Rohstoff(name = "Unbekannt", quelle = NaehrwertQuelle.UNBEKANNT)
        val ergebnis = NaehrwertBerechnung.berechne(
            listOf(NaehrwertBerechnung.ZutatMenge(unvollstaendig, 10.0))
        )
        assertFalse(ergebnis.istVollstaendig)
        assertEquals(listOf("Unbekannt"), ergebnis.fehlendeDaten)
    }

    @Test
    fun `Rezeptur-Energie wird aus aggregierten Makros berechnet, nicht aus Rohstoff-Energiewerten`() {
        // Nachbildung eines realen Falls (USDA-Chili): der "gemessene" Energiewert des
        // Rohstoffs (166 kJ) weicht von der Anhang-XIV-Rückrechnung aus seinen eigenen
        // Makros ab (37*0,4 + 17*8,8 + 17*1,9 + 8*1,5 = 208,7 kJ). Die Rezeptur-Energie
        // MUSS die Anhang-XIV-Rückrechnung verwenden, sonst entsteht exakt der
        // Etikettierungsfehler aus dem Beanstandungsbescheid (Amt der Kärntner
        // Landesregierung, 05-LMA-BBM-469/2025-279): eine deklarierte Energie, die
        // rechnerisch nicht zu den deklarierten Fett-/Kohlenhydrat-/Eiweiß-/
        // Ballaststoffwerten passt.
        val chiliArtig = Rohstoff(
            name = "Chili-artig", energieKj = 166.0, energieKcal = 40.0, fett = 0.4,
            gesaettigteFettsaeuren = 0.0, kohlenhydrate = 8.8, zucker = 5.3,
            ballaststoffe = 1.5, eiweiss = 1.9, salz = 0.0
        )
        val ergebnis = NaehrwertBerechnung.berechne(
            listOf(NaehrwertBerechnung.ZutatMenge(chiliArtig, 100.0))
        )
        assertEquals(208.7, ergebnis.energieKj, 0.01)
        assertFalse("Der gespeicherte, nicht formelkonsistente Energiewert (166) darf nicht durchgereicht werden", 166.0 == ergebnis.energieKj)
    }

    @Test
    fun `organische Saeuren und Alkohol tragen zur Energie bei`() {
        val essig13Prozent = Rohstoff(
            name = "Essig-artig", energieKj = 0.0, energieKcal = 0.0, fett = 0.0,
            gesaettigteFettsaeuren = 0.0, kohlenhydrate = 0.0, zucker = 0.0,
            ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0, organischeSaeuren = 13.0
        )
        val essigErgebnis = NaehrwertBerechnung.berechne(
            listOf(NaehrwertBerechnung.ZutatMenge(essig13Prozent, 100.0))
        )
        // 13 g organische Säure * 13 kJ/g bzw. 3 kcal/g (Anhang XIV)
        assertEquals(169.0, essigErgebnis.energieKj, 0.01)
        assertEquals(39.0, essigErgebnis.energieKcal, 0.01)

        val wein = Rohstoff(
            name = "Wein-artig", energieKj = 0.0, energieKcal = 0.0, fett = 0.0,
            gesaettigteFettsaeuren = 0.0, kohlenhydrate = 0.0, zucker = 0.0,
            ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0, alkoholGramm = 10.0
        )
        val weinErgebnis = NaehrwertBerechnung.berechne(
            listOf(NaehrwertBerechnung.ZutatMenge(wein, 100.0))
        )
        // 10 g Alkohol * 29 kJ/g bzw. 7 kcal/g (Anhang XIV)
        assertEquals(290.0, weinErgebnis.energieKj, 0.01)
        assertEquals(70.0, weinErgebnis.energieKcal, 0.01)
    }

    @Test
    fun `Bezugsgroesse pro 100ml normiert auf das gemessene Gesamtvolumen statt auf das Gewicht`() {
        // 100 g reiner Zucker in einem Ansatz mit gemessenem Gesamtvolumen 200 ml (z. B.
        // weil die Zutaten sich beim Mischen nicht additiv zu einem Volumen summieren) ->
        // pro 100 ml müssen es 50 g Zucker sein (nicht 100 g wie bei "pro 100 g").
        val ergebnis = NaehrwertBerechnung.berechne(
            zutaten = listOf(NaehrwertBerechnung.ZutatMenge(zucker, 100.0)),
            bezugsgroesse = Bezugsgroesse.PRO_100_ML,
            gesamtvolumenMl = 200.0
        )
        assertEquals(50.0, ergebnis.kohlenhydrate, 0.001)
        assertEquals(Bezugsgroesse.PRO_100_ML, ergebnis.bezugsgroesse)
        assertEquals(200.0, ergebnis.gesamtvolumenMl)
        // Gesamtgewicht (physische Masse) bleibt unabhängig von der Bezugsgröße korrekt.
        assertEquals(100.0, ergebnis.gesamtGewichtGramm, 0.001)
    }

    @Test
    fun `Bezugsgroesse pro 100ml ohne Gesamtvolumen wirft statt still falsch zu rechnen`() {
        assertThrows(IllegalArgumentException::class.java) {
            NaehrwertBerechnung.berechne(
                zutaten = listOf(NaehrwertBerechnung.ZutatMenge(zucker, 100.0)),
                bezugsgroesse = Bezugsgroesse.PRO_100_ML,
                gesamtvolumenMl = null
            )
        }
    }

    @Test
    fun `als vernachlaessigbar markierter Rohstoff loest keine unvollstaendig-Warnung aus`() {
        val mazerat = Rohstoff(
            name = "Kraeutermazerat 53 Prozent",
            alkoholGramm = NaehrwertBerechnung.alkoholGrammAusVol(53.0),
            vernachlaessigbar = true
            // fett/kohlenhydrate/etc. bleiben null - unbekannt, aber bewusst akzeptiert
        )
        val ergebnis = NaehrwertBerechnung.berechne(
            listOf(NaehrwertBerechnung.ZutatMenge(mazerat, 100.0))
        )
        assertTrue(
            "Ein als vernachlässigbar markierter Rohstoff darf die Deklaration nicht blockieren",
            ergebnis.istVollstaendig
        )
        assertEquals(emptyList<String>(), ergebnis.fehlendeDaten)
        assertEquals(listOf("Kraeutermazerat 53 Prozent"), ergebnis.alsVernachlaessigbarAkzeptiert)
        // Der bekannte Alkoholgehalt fließt trotzdem in die Energie ein (53*0,789 = 41,817 g).
        assertEquals(41.817, ergebnis.alkohol, 0.001)
    }

    @Test
    fun `alkoholGrammAusVol leitet Alkoholgramm aus Vol-Prozent und Dichte her`() {
        // 53 %vol, Standarddichte 1,0 g/ml -> 53 * 0,789 = 41,817 g/100g
        assertEquals(41.817, NaehrwertBerechnung.alkoholGrammAusVol(53.0), 0.0001)
        // 12 %vol bei Produktdichte 0,99 g/ml (z. B. Rotwein) -> 12*0,789/0,99
        assertEquals(9.564, NaehrwertBerechnung.alkoholGrammAusVol(12.0, 0.99), 0.001)
    }

    @Test
    fun `SeedData enthaelt keine doppelten Rohstoffnamen`() {
        val namen = SeedData.initialeRohstoffe().map { it.name }
        assertEquals(
            "Rohstoff.name hat einen eindeutigen DB-Index – doppelte Namen in SeedData " +
                "würden beim Einfügen fehlschlagen bzw. stillschweigend ignoriert werden.",
            namen.distinct().size,
            namen.size
        )
    }
}
