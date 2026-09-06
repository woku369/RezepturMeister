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
 *
 * WICHTIG zur Energieberechnung (September 2026 korrigiert): Der Brennwert der
 * Rezeptur wird NICHT durch Aufsummieren der einzelnen Rohstoff-Energiewerte
 * ermittelt, sondern IMMER nach Anhang XIV VO (EU) 1169/2011 aus den bereits
 * aggregierten Fett-/Kohlenhydrat-/Ballaststoff-/Eiweiß-/Alkohol-/
 * Säurewerten der GESAMTEN Rezeptur neu berechnet. Grund: reale
 * Lebensmitteldatenbanken (z. B. USDA) geben für zusammengesetzte Lebensmittel
 * oft einen eigenständig gemessenen/tabellierten Energiewert an, der von der
 * simplen Anhang-XIV-Rückrechnung aus Fett/Kohlenhydrate/Eiweiß/Ballaststoffe
 * abweicht (z. B. weil Ballaststoffe physiologisch nicht immer exakt 8 kJ/g
 * liefern). Für eine LMIV-konforme Deklaration ist aber ausschließlich die
 * Anhang-XIV-Rückrechnung zulässig – genau eine solche Abweichung zwischen
 * "gemessener" und "aus den Nährwerten zurückgerechneter" Energie war der
 * Kern einer amtlichen Beanstandung (Amt der Kärntner Landesregierung,
 * Zahl 05-LMA-BBM-469/2025-279 vom 27.08.2026) gegen ein Etikett mit exakt
 * diesem Fehler. Die App hätte denselben Fehlertyp erzeugt, wenn sie weiterhin
 * Rohstoff.energieKj/energieKcal aufsummiert hätte – diese Felder sind daher
 * nur noch Referenz-/Anzeigewerte pro Rohstoff, nicht Grundlage der
 * Rezeptur-Gesamtenergie.
 *
 * Rohstoff.organischeSaeuren und Rohstoff.alkoholGramm (beide Gramm pro
 * 100 g, Default 0.0) fließen dafür zusätzlich zu den vier "klassischen"
 * Makronährstoffen in die Formel ein – ohne sie würde z. B. der
 * Energiebeitrag von Speiseessig oder Wein beim Neuberechnen verloren gehen.
 * NICHT berücksichtigt: Polyole (in keinem aktuellen Rohstoff vorhanden).
 */
object NaehrwertBerechnung {

    data class ZutatMenge(val rohstoff: Rohstoff, val mengeGramm: Double)

    // Anhang XIV VO (EU) 1169/2011 Umrechnungsfaktoren
    private const val KJ_PRO_G_FETT = 37.0
    private const val KJ_PRO_G_KOHLENHYDRATE = 17.0
    private const val KJ_PRO_G_EIWEISS = 17.0
    private const val KJ_PRO_G_BALLASTSTOFFE = 8.0
    private const val KJ_PRO_G_ALKOHOL = 29.0
    private const val KJ_PRO_G_ORGANISCHE_SAEUREN = 13.0

    private const val KCAL_PRO_G_FETT = 9.0
    private const val KCAL_PRO_G_KOHLENHYDRATE = 4.0
    private const val KCAL_PRO_G_EIWEISS = 4.0
    private const val KCAL_PRO_G_BALLASTSTOFFE = 2.0
    private const val KCAL_PRO_G_ALKOHOL = 7.0
    private const val KCAL_PRO_G_ORGANISCHE_SAEUREN = 3.0

    fun berechne(zutaten: List<ZutatMenge>): NaehrwertErgebnis {
        require(zutaten.isNotEmpty()) { "Eine Rezeptur benötigt mindestens eine Zutat." }

        val gesamtGewicht = zutaten.sumOf { it.mengeGramm }
        require(gesamtGewicht > 0.0) { "Gesamtgewicht muss größer als 0 sein." }

        fun summeNullable(selector: (Rohstoff) -> Double?): Double =
            zutaten.sumOf { (rohstoff, menge) ->
                val wertPro100g = selector(rohstoff) ?: 0.0
                wertPro100g * menge / 100.0
            }

        fun summe(selector: (Rohstoff) -> Double): Double =
            zutaten.sumOf { (rohstoff, menge) -> selector(rohstoff) * menge / 100.0 }

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

        val fett = summeNullable { it.fett } * faktorPro100g
        val gesaettigteFettsaeuren = summeNullable { it.gesaettigteFettsaeuren } * faktorPro100g
        val kohlenhydrate = summeNullable { it.kohlenhydrate } * faktorPro100g
        val zucker = summeNullable { it.zucker } * faktorPro100g
        val ballaststoffe = summeNullable { it.ballaststoffe } * faktorPro100g
        val eiweiss = summeNullable { it.eiweiss } * faktorPro100g
        val salz = summeNullable { it.salz } * faktorPro100g
        val alkohol = summe { it.alkoholGramm } * faktorPro100g
        val organischeSaeuren = summe { it.organischeSaeuren } * faktorPro100g

        val energieKj = KJ_PRO_G_FETT * fett + KJ_PRO_G_KOHLENHYDRATE * kohlenhydrate +
            KJ_PRO_G_EIWEISS * eiweiss + KJ_PRO_G_BALLASTSTOFFE * ballaststoffe +
            KJ_PRO_G_ALKOHOL * alkohol + KJ_PRO_G_ORGANISCHE_SAEUREN * organischeSaeuren
        val energieKcal = KCAL_PRO_G_FETT * fett + KCAL_PRO_G_KOHLENHYDRATE * kohlenhydrate +
            KCAL_PRO_G_EIWEISS * eiweiss + KCAL_PRO_G_BALLASTSTOFFE * ballaststoffe +
            KCAL_PRO_G_ALKOHOL * alkohol + KCAL_PRO_G_ORGANISCHE_SAEUREN * organischeSaeuren

        return NaehrwertErgebnis(
            energieKj = energieKj,
            energieKcal = energieKcal,
            fett = fett,
            gesaettigteFettsaeuren = gesaettigteFettsaeuren,
            kohlenhydrate = kohlenhydrate,
            zucker = zucker,
            ballaststoffe = ballaststoffe,
            eiweiss = eiweiss,
            salz = salz,
            gesamtGewichtGramm = gesamtGewicht,
            zutatenliste = zutatenliste,
            fehlendeDaten = fehlendeDaten
        )
    }
}
