package at.rezepturmeister.naehrwertrechner

import at.rezepturmeister.naehrwertrechner.data.NaehrwertQuelle
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.data.SeedData
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
