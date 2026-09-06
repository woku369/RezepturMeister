package at.rezepturmeister.naehrwertrechner

import at.rezepturmeister.naehrwertrechner.ocr.EtikettParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EtikettParserTest {

    @Test
    fun `Standard-Naehrwerttabelle mit Zeilenumbruechen zwischen Bezeichnung und Zahl wird korrekt gelesen`() {
        // Simuliert typischen ML-Kit-Rohtext: jede Bezeichnung und ihr Wert stehen im
        // Originalfoto oft in getrennten Zeilen/Spalten der Tabelle.
        val rohtext = """
            Nährwertdeklaration
            Durchschnittliche Nährwerte je 100 g
            Brennwert
            1.490 kJ / 356 kcal
            Fett
            0,0 g
            davon gesättigte Fettsäuren
            0,0 g
            Kohlenhydrate
            89 g
            davon Zucker
            89 g
            Ballaststoffe
            0,0 g
            Eiweiß
            0,0 g
            Salz
            0,01 g
        """.trimIndent()

        val ergebnis = EtikettParser.parse(rohtext)

        assertEquals(1490.0, ergebnis.energieKj)
        assertEquals(356.0, ergebnis.energieKcal)
        assertEquals(0.0, ergebnis.fett)
        assertEquals(0.0, ergebnis.gesaettigteFettsaeuren)
        assertEquals(89.0, ergebnis.kohlenhydrate)
        assertEquals(89.0, ergebnis.zucker)
        assertEquals(0.0, ergebnis.ballaststoffe)
        assertEquals(0.0, ergebnis.eiweiss)
        assertEquals(0.01, ergebnis.salz)
        assertEquals(9, ergebnis.anzahlErkannt())
    }

    @Test
    fun `Spurenmenge mit kleiner-als-Zeichen und verlorene Umlaute werden trotzdem erkannt`() {
        val rohtext = """
            Energie 169kJ/40kcal
            Fett 0,4g
            davon gesattigte Fettsauren 0,1g
            Kohlenhydrate 8,8g
            davon Zucker 5,3g
            Ballaststoffe 1,5g
            Eiweiss 1,9g
            Salz <0,01g
        """.trimIndent()

        val ergebnis = EtikettParser.parse(rohtext)

        assertEquals(169.0, ergebnis.energieKj)
        assertEquals(40.0, ergebnis.energieKcal)
        assertEquals(0.4, ergebnis.fett)
        assertEquals(0.1, ergebnis.gesaettigteFettsaeuren)
        assertEquals(1.9, ergebnis.eiweiss)
        assertEquals(0.01, ergebnis.salz)
    }

    @Test
    fun `fehlende Felder werden als null ausgewiesen statt geraten`() {
        val rohtext = "Brennwert 100 kJ / 24 kcal\nEiweiß 1,0 g"

        val ergebnis = EtikettParser.parse(rohtext)

        assertEquals(100.0, ergebnis.energieKj)
        assertEquals(1.0, ergebnis.eiweiss)
        assertNull(ergebnis.fett)
        assertNull(ergebnis.kohlenhydrate)
        assertNull(ergebnis.salz)
        assertEquals(3, ergebnis.anzahlErkannt())
    }
}
