package at.rezepturmeister.naehrwertrechner

import at.rezepturmeister.naehrwertrechner.data.NaehrwertQuelle
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
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
}
