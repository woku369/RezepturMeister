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

    /**
     * @param mengeGramm kanonische Menge in Gramm – einzige für die Berechnung
     *   verwendete Größe.
     * @param eingegebeneMenge wie vom Nutzer eingegeben (z. B. in ml), nur für die
     *   Anzeige/Nachvollziehbarkeit. Bei Einheit GRAMM identisch zu [mengeGramm].
     * @param eingegebeneEinheit Einheit von [eingegebeneMenge].
     */
    data class ZutatMenge(
        val rohstoff: Rohstoff,
        val mengeGramm: Double,
        val eingegebeneMenge: Double = mengeGramm,
        val eingegebeneEinheit: Mengeneinheit = Mengeneinheit.GRAMM
    )

    // Dichte von reinem Ethanol, für die Herleitung von alkoholGramm aus einem
    // bekannten Alkoholgehalt in %vol (z. B. bei einem Kräutermazerat ohne eigene
    // Nährwertdaten) – siehe alkoholGrammAusVol().
    const val DICHTE_ETHANOL_G_PRO_ML = 0.789

    /**
     * Leitet den Alkoholgehalt in Gramm pro 100 g Produkt aus dem Alkoholgehalt in
     * %vol her (dieselbe Rechnung, die bisher händisch für Rotwein in SeedData.kt
     * dokumentiert war: %vol × Ethanoldichte ÷ Produktdichte). [dichteProdukt] ist
     * die Dichte des GESAMTEN Produkts in g/ml (Default 1,0 – für ein wässrig-
     * alkoholisches Mazerat eine übliche Näherung, aber keine Messung; bei
     * abweichender tatsächlicher Dichte diese angeben statt den Default zu übernehmen).
     */
    fun alkoholGrammAusVol(volProzent: Double, dichteProdukt: Double = 1.0): Double =
        volProzent * DICHTE_ETHANOL_G_PRO_ML / dichteProdukt

    // Anhang XIV VO (EU) 1169/2011 Umrechnungsfaktoren. Bewusst nicht privat, damit
    // z. B. der XLSX-Export dieselben Faktoren für eine nachvollziehbare
    // Berechnungsdarstellung verwenden kann, statt sie ein zweites Mal zu duplizieren
    // (Duplizierung hätte in dieser Codebasis bereits einmal zu einer vergessenen
    // Aktualisierung geführt).
    const val KJ_PRO_G_FETT = 37.0
    const val KJ_PRO_G_KOHLENHYDRATE = 17.0
    const val KJ_PRO_G_EIWEISS = 17.0
    const val KJ_PRO_G_BALLASTSTOFFE = 8.0
    const val KJ_PRO_G_ALKOHOL = 29.0
    const val KJ_PRO_G_ORGANISCHE_SAEUREN = 13.0

    const val KCAL_PRO_G_FETT = 9.0
    const val KCAL_PRO_G_KOHLENHYDRATE = 4.0
    const val KCAL_PRO_G_EIWEISS = 4.0
    const val KCAL_PRO_G_BALLASTSTOFFE = 2.0
    const val KCAL_PRO_G_ALKOHOL = 7.0
    const val KCAL_PRO_G_ORGANISCHE_SAEUREN = 3.0

    fun berechne(
        zutaten: List<ZutatMenge>,
        bezugsgroesse: Bezugsgroesse = Bezugsgroesse.PRO_100_G,
        gesamtvolumenMl: Double? = null
    ): NaehrwertErgebnis {
        require(zutaten.isNotEmpty()) { "Eine Rezeptur benötigt mindestens eine Zutat." }

        val gesamtGewicht = zutaten.sumOf { it.mengeGramm }
        require(gesamtGewicht > 0.0) { "Gesamtgewicht muss größer als 0 sein." }

        // Bei "pro 100 ml" NICHT die eingegebenen Zutatenvolumina aufsummieren: Alkohol
        // und Wasser mischen sich nicht additiv (das Gesamtvolumen einer Mischung ist
        // kleiner als die Summe der Einzelvolumina), eine Rückrechnung aus den
        // Zutatenmengen wäre daher unehrlich genau. Stattdessen wird das tatsächliche,
        // gemessene Gesamtvolumen des fertigen Ansatzes verlangt (analog zum
        // Abtropfgewicht bei eingelegtem Gemüse: gemessen statt zurückgerechnet).
        if (bezugsgroesse == Bezugsgroesse.PRO_100_ML) {
            require(gesamtvolumenMl != null && gesamtvolumenMl > 0.0) {
                "Für die Bezugsgröße 'pro 100 ml' muss das gemessene Gesamtvolumen des " +
                    "fertigen Ansatzes angegeben werden (nicht aus den Zutatenmengen ableitbar)."
            }
        }

        fun summeNullable(selector: (Rohstoff) -> Double?): Double =
            zutaten.sumOf { (rohstoff, menge) ->
                val wertPro100g = selector(rohstoff) ?: 0.0
                wertPro100g * menge / 100.0
            }

        fun summe(selector: (Rohstoff) -> Double): Double =
            zutaten.sumOf { (rohstoff, menge) -> selector(rohstoff) * menge / 100.0 }

        val fehlendeDaten = zutaten
            .filter { it.rohstoff.erfordertWarnhinweis() }
            .map { it.rohstoff.name }
            .distinct()

        val alsVernachlaessigbarAkzeptiert = zutaten
            .filter { !it.rohstoff.istVollstaendig() && it.rohstoff.vernachlaessigbar }
            .map { it.rohstoff.name }
            .distinct()

        val zutatenliste = zutaten
            .sortedByDescending { it.mengeGramm }
            .map { (rohstoff, menge) ->
                ZutatNaehrwertInfo(
                    name = rohstoff.name,
                    gewichtGramm = menge,
                    prozent = menge / gesamtGewicht * 100.0,
                    hatVollstaendigeNaehrwertdaten = rohstoff.istVollstaendig(),
                    alsVernachlaessigbarMarkiert = rohstoff.vernachlaessigbar
                )
            }

        // Faktor, um die Summenwerte (bezogen auf das Gesamtgewicht in Gramm) auf die
        // gewählte Bezugsgröße zu normieren – bei "pro 100 ml" bleibt der Zähler (die
        // Nährstoffmenge in Gramm) unverändert, nur der Nenner wechselt vom
        // Gesamtgewicht auf das gemessene Gesamtvolumen (Standardverfahren für die
        // Nährwertdeklaration von Flüssigkeiten, z. B. "3,5 g Fett pro 100 ml").
        val bezugsmenge = if (bezugsgroesse == Bezugsgroesse.PRO_100_ML) gesamtvolumenMl!! else gesamtGewicht
        val faktor = 100.0 / bezugsmenge

        val fett = summeNullable { it.fett } * faktor
        val gesaettigteFettsaeuren = summeNullable { it.gesaettigteFettsaeuren } * faktor
        val kohlenhydrate = summeNullable { it.kohlenhydrate } * faktor
        val zucker = summeNullable { it.zucker } * faktor
        val ballaststoffe = summeNullable { it.ballaststoffe } * faktor
        val eiweiss = summeNullable { it.eiweiss } * faktor
        val salz = summeNullable { it.salz } * faktor
        val alkohol = summe { it.alkoholGramm } * faktor
        val organischeSaeuren = summe { it.organischeSaeuren } * faktor

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
            alkohol = alkohol,
            organischeSaeuren = organischeSaeuren,
            bezugsgroesse = bezugsgroesse,
            gesamtGewichtGramm = gesamtGewicht,
            gesamtvolumenMl = gesamtvolumenMl,
            zutatenliste = zutatenliste,
            fehlendeDaten = fehlendeDaten,
            alsVernachlaessigbarAkzeptiert = alsVernachlaessigbarAkzeptiert
        )
    }
}
