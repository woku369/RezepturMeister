package at.rezepturmeister.naehrwertrechner.domain

import at.rezepturmeister.naehrwertrechner.data.Rohstoff

/**
 * Berechnet den Nährwert einer Rezeptur nach Art. 31 LMIV, Variante "Berechnung
 * anhand bekannter/durchschnittlicher Werte der verwendeten Zutaten".
 *
 * Massenbilanz: die Summe der Zutatengewichte ergibt das Gesamtgewicht des
 * Ansatzes (z. B. Gemüse + Einlegeflüssigkeit im Glas). Für eingelegtes Gemüse
 * ist das die Deklaration "wie verkauft" (inkl. Lake) – Diffusionseffekte
 * zwischen Gemüse und Lake während der Reifung ändern die GESAMTmasse nicht,
 * nur die Verteilung innerhalb des Glases, und sind für diese Berechnung
 * daher unerheblich. Für ein separat deklariertes Abtropfgewicht reicht diese
 * einfache Zutatenrechnung NICHT aus.
 */
object NaehrwertBerechnung {

    data class ZutatMenge(val rohstoff: Rohstoff, val mengeGramm: Double)

    fun berechne(zutaten: List<ZutatMenge>): NaehrwertErgebnis {
        require(zutaten.isNotEmpty()) { "Eine Rezeptur benötigt mindestens eine Zutat." }

        val gesamtGewicht = zutaten.sumOf { it.mengeGramm }
        require(gesamtGewicht > 0.0) { "Gesamtgewicht muss größer als 0 sein." }

        fun summe(selector: (Rohstoff) -> Double?): Double =
            zutaten.sumOf { (rohstoff, menge) ->
                val wertPro100g = selector(rohstoff) ?: 0.0
                wertPro100g * menge / 100.0
            }

        val fehlendeDaten = zutaten
            .filter { !it.rohstoff.istVollstaendig() }
            .map { it.rohstoff.name }
            .distinct()

        val zutatenliste = zutaten
            .sortedByDescending { it.mengeGramm }
            .map { (rohstoff, menge) ->
                ZutatNaehrwertInfo(
                    name = rohstoff.name,
                    gewichtGramm = menge,
                    prozent = menge / gesamtGewicht * 100.0,
                    hatVollstaendigeNaehrwertdaten = rohstoff.istVollstaendig()
                )
            }

        // Faktor, um die Summenwerte (bezogen auf Gesamtgewicht) auf "pro 100 g" zu normieren
        val faktorPro100g = 100.0 / gesamtGewicht

        return NaehrwertErgebnis(
            energieKj = summe { it.energieKj } * faktorPro100g,
            energieKcal = summe { it.energieKcal } * faktorPro100g,
            fett = summe { it.fett } * faktorPro100g,
            gesaettigteFettsaeuren = summe { it.gesaettigteFettsaeuren } * faktorPro100g,
            kohlenhydrate = summe { it.kohlenhydrate } * faktorPro100g,
            zucker = summe { it.zucker } * faktorPro100g,
            ballaststoffe = summe { it.ballaststoffe } * faktorPro100g,
            eiweiss = summe { it.eiweiss } * faktorPro100g,
            salz = summe { it.salz } * faktorPro100g,
            gesamtGewichtGramm = gesamtGewicht,
            zutatenliste = zutatenliste,
            fehlendeDaten = fehlendeDaten
        )
    }
}
