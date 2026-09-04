package at.rezepturmeister.naehrwertrechner.data

/**
 * Start-Rohstoffdatenbank für die ersten drei Produktgruppen
 * (eingelegtes Gemüse, Gemüsezubereitungen, Senf).
 *
 * WICHTIG – Datenqualität: Diese Werte wurden bei Erstanlage der App per
 * Websuche zusammengestellt, NICHT durch direkten Abgleich mit den amtlichen
 * Datenbanken ÖNWT/BLS oder mit den tatsächlich eingekauften Herstelleretiketten.
 * Sie sind ein Startpunkt für die Struktur, nicht für die produktive
 * Nährwertdeklaration. Jeder Datensatz trägt `quelle` + `quelleHinweis`, damit
 * vor der ersten realen Deklaration gezielt nachgeprüft/ersetzt werden kann
 * (siehe Art. 31 LMIV: Berechnung nur auf Basis bekannter/anerkannter Daten).
 *
 * Fehlt ein einzelnes Feld (z. B. Salz bei Paprika/Chili, gesättigte
 * Fettsäuren bei Speck), wurde es bewusst NULL belassen, statt einen
 * unsicheren Wert zu erfinden – das macht die App-eigene
 * Vollständigkeitsprüfung (`Rohstoff.istVollstaendig()`) direkt sichtbar.
 */
object SeedData {

    private const val QUELLE_BERECHNET_HINWEIS =
        "Berechnet nach Anhang XIV VO (EU) 1169/2011 (organische Säuren = 13 kJ/g bzw. 3 kcal/g), " +
            "angenommener reiner Essigsäuregehalt lt. Rohstoffname. Vor Verwendung gegen das Etikett " +
            "des tatsächlich eingekauften Speiseessigs prüfen (Gesamtsäure kann leicht abweichen)."

    private const val QUELLE_WEB_HINWEIS =
        "Websuche September 2026, Verbraucher-Nährwertportale (nicht amtlich, teils widersprüchliche " +
            "Einzelquellen) – vor produktiver Nutzung durch ÖNWT/BLS-Wert oder Herstelleretikett ersetzen."

    private const val QUELLE_USDA_HINWEIS =
        "Websuche September 2026 mit Verweis auf USDA FoodData Central, nicht direkt aus der " +
            "Primärquelle fdc.nal.usda.gov gegengeprüft – vor produktiver Nutzung dort verifizieren."

    fun initialeRohstoffe(): List<Rohstoff> = listOf(
        // ---- Chemisch einfache Grundstoffe: rechnerisch/definitorisch eindeutig ----
        Rohstoff(
            name = "Zucker (Haushaltszucker, raffiniert)", kategorie = "Grundstoff",
            energieKj = 1700.0, energieKcal = 400.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 100.0, zucker = 100.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Reine Saccharose – allgemein anerkannter Referenzwert, unstrittig."
        ),
        Rohstoff(
            name = "Salz (Speisesalz, NaCl)", kategorie = "Grundstoff",
            energieKj = 0.0, energieKcal = 0.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 100.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Definitionsgemäß Reinstoff NaCl."
        ),
        Rohstoff(
            name = "Wasser", kategorie = "Grundstoff",
            energieKj = 0.0, energieKcal = 0.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Definitionsgemäß."
        ),

        // ---- Essigsorten: als verdünnte Essigsäure gerechnet ----
        Rohstoff(
            name = "Speiseessig 13% Säure", kategorie = "Einlegeflüssigkeit",
            energieKj = 169.0, energieKcal = 39.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET, quelleHinweis = QUELLE_BERECHNET_HINWEIS
        ),
        Rohstoff(
            name = "Speiseessig 2% Säure", kategorie = "Einlegeflüssigkeit",
            energieKj = 26.0, energieKcal = 6.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET, quelleHinweis = QUELLE_BERECHNET_HINWEIS
        ),
        Rohstoff(
            name = "Essigessenz 80% Säure", kategorie = "Grundstoff",
            energieKj = 1040.0, energieKcal = 240.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET, quelleHinweis = QUELLE_BERECHNET_HINWEIS
        ),
        Rohstoff(
            name = "Balsamico-Essig 6% Säure", kategorie = "Einlegeflüssigkeit",
            energieKj = 368.0, energieKcal = 88.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 17.0, zucker = 14.9, ballaststoffe = 0.0, eiweiss = 0.5, salz = 0.0,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Balsamico enthält Traubenmost/Zucker – NICHT wie " +
                "einfacher Speiseessig rein rechnerisch aus dem Säuregehalt ableitbar. Zuckergehalt " +
                "schwankt stark je Marke/Qualität – Herstelleretikett zwingend erforderlich."
        ),

        // ---- Rohgemüse ----
        Rohstoff(
            name = "Gurke, roh", kategorie = "Gemüse",
            energieKj = 67.0, energieKcal = 16.0, fett = 0.1, kohlenhydrate = 2.8, eiweiss = 0.6,
            quelle = NaehrwertQuelle.USDA, quelleHinweis = QUELLE_USDA_HINWEIS
        ),
        Rohstoff(
            name = "Paprika, rot, roh", kategorie = "Gemüse",
            energieKj = 109.0, energieKcal = 26.0, fett = 0.3, kohlenhydrate = 6.0, eiweiss = 1.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Erneut recherchiert September 2026: Energie/Fett/" +
                "Kohlenhydrate/Eiweiß bestätigt, Zucker/Ballaststoffe/Salz weiterhin nicht in " +
                "verlässlicher Form gefunden – bewusst NULL belassen."
        ),
        Rohstoff(
            name = "Chili/Peperoni, rot, roh", kategorie = "Gemüse",
            energieKj = 166.0, energieKcal = 40.0, fett = 0.4, kohlenhydrate = 8.8, zucker = 5.3,
            ballaststoffe = 1.5, eiweiss = 1.9,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Erneut recherchiert September 2026 (USDA FDC 170106) " +
                "– Energie/Fett/Kohlenhydrate/Ballaststoffe/Eiweiß bestätigt. Salzgehalt (Natrium) " +
                "war in den Suchtreffern nicht enthalten, bewusst NULL belassen statt zu schätzen."
        ),
        Rohstoff(
            name = "Zwiebel, roh", kategorie = "Gemüse",
            energieKj = 167.0, energieKcal = 40.0, fett = 0.1, kohlenhydrate = 9.3, zucker = 4.2,
            ballaststoffe = 1.7, eiweiss = 1.1,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Zuckerwert (4,2 g) September 2026 ergänzt " +
                "(vorherige Recherche hatte hier fälschlich Ballaststoffe statt Zucker geliefert)."
        ),

        // ---- Weitere Zutaten der zweiten Produktgruppe ----
        Rohstoff(
            name = "Speck (Bauchspeck, geräuchert)", kategorie = "Fleischprodukt",
            energieKj = 1590.0, energieKcal = 380.0, fett = 33.3, kohlenhydrate = 0.0,
            eiweiss = 16.7, salz = 2.5,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "Mittelwert aus 3 unabhängigen, sich überschneidenden Websuche-Treffern " +
                "September 2026 für geräucherten Bauchspeck (372 kcal/18,0 g Eiweiß/33,3 g Fett; " +
                "320 kcal/16,0 g Eiweiß/28,9 g Fett; 405 kcal/16,1 g Eiweiß/37,8 g Fett/2,5 g Salz) – " +
                "keine amtliche Quelle, Streuung ±25 kcal. Ein separater Treffer für ROHEN, ungeräucherten " +
                "Bauchspeck (796 kcal, 88,7 g Fett, nur 2,9 g Eiweiß – praktisch reines Fettgewebe) wurde " +
                "bewusst NICHT verwendet, da für Gemüsezubereitungen typischerweise der geräucherte, " +
                "durchwachsene Speck mit Fleischanteil eingesetzt wird. Vor produktiver Nutzung durch das " +
                "Etikett der tatsächlich eingekauften Speckware ersetzen – Salzgehalt schwankt stark je " +
                "Pökelverfahren. Gesättigte Fettsäuren in keiner der Quellen ausgewiesen, daher NULL " +
                "belassen (Rohstoff bleibt bis zur Etikettdaten-Übernahme als unvollständig markiert)."
        ),
        Rohstoff(
            name = "Ahornsirup", kategorie = "Süßungsmittel",
            energieKj = 1088.0, energieKcal = 260.0, fett = 0.06, kohlenhydrate = 67.0, zucker = 60.0,
            eiweiss = 0.04,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Andere Quellen nennen bis zu 333 kcal/100g " +
                "(Reinheitsgrad/Wassergehalt variiert) – Etikett der verwendeten Marke prüfen."
        ),
        Rohstoff(
            name = "Gelierzucker 3:1", kategorie = "Süßungsmittel",
            energieKj = 1654.0, energieKcal = 395.0, fett = 0.3, kohlenhydrate = 97.0, zucker = 97.0,
            eiweiss = 0.7,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Markenprodukt als Referenz – bei anderem " +
                "Mischungsverhältnis (z. B. 2:1) oder Hersteller abweichende Werte, Etikett prüfen."
        ),
        Rohstoff(
            name = "Gelierzucker 2:1", kategorie = "Süßungsmittel",
            energieKj = 1658.0, energieKcal = 396.0, fett = 0.0, kohlenhydrate = 98.5, zucker = 98.0,
            eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Werte für Aldi-Markenprodukt; andere Marken (Dr. " +
                "Oetker u. a.) lagen zwischen 391–401 kcal bzw. 95,5–98,5 g Kohlenhydrate/100 g – " +
                "Streuung gering, aber vor Verwendung Etikett des konkreten Produkts prüfen."
        ),
        Rohstoff(
            name = "Pektin (Apfel-/Citruspektin, Pulver)", kategorie = "Geliermittel",
            energieKj = 820.0, energieKcal = 196.0, fett = 1.0, kohlenhydrate = 44.5, zucker = 0.0,
            ballaststoffe = 36.5, eiweiss = 2.3,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "Websuche September 2026, Einzelquelle (Fddb, Handelsprodukt " +
                "\"Apfelpektin Flocken\", kein Reinstoff-Pektin). Reines Pektinpulver ist praktisch " +
                "100 % lösliche Ballaststoffe – Handelsprodukte werden aber häufig mit Traubenzucker " +
                "gestreckt, um die Gelierkraft zu standardisieren, daher der Kohlenhydratanteil. " +
                "Salz nicht ausgewiesen (0 angenommen ist hier NICHT belegt, daher NULL belassen). " +
                "Für das konkret verwendete Produkt unbedingt Herstellerdatenblatt heranziehen, da " +
                "der Streckungsgrad je Hersteller stark variiert."
        ),
        Rohstoff(
            name = "Rotwein, trocken", kategorie = "Sonstige Zutat",
            alkoholGehaltVol = 12.0,
            energieKj = 356.0, energieKcal = 85.0, fett = 0.1, kohlenhydrate = 2.6, zucker = 1.5,
            eiweiss = 0.1,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Rotwein-Nährwerte streuen stark nach Sorte/" +
                "Restzuckergehalt/Alkoholgehalt (Quellen nannten 0,8–2,6 g Kohlenhydrate/100 ml) – " +
                "für die konkret verwendete Weinmarke Herstellerangabe verwenden. Energiewert enthält " +
                "bereits den Alkoholanteil; alkoholGehaltVol dient nur der Kennzeichnung."
        ),

        // ---- Fett/Öl ----
        Rohstoff(
            name = "Sonnenblumenöl", kategorie = "Fett/Öl",
            energieKj = 3700.0, energieKcal = 884.0, fett = 100.0, gesaettigteFettsaeuren = 10.3,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Reines Pflanzenöl – Fettwert (100 g/100 g) " +
                "definitorisch, gesättigte Fettsäuren laut USDA-Referenz für linolsäurereiches " +
                "Sonnenblumenöl (\"linoleic\"); High-Oleic-Sonnenblumenöl hat einen anderen Anteil " +
                "gesättigter/einfach ungesättigter Fettsäuren – bei Verwendung Etikett prüfen."
        ),

        // ---- Senf ----
        Rohstoff(
            name = "Senfsaat, gelb", kategorie = "Gewürz/Saat",
            energieKj = 1987.0, energieKcal = 475.0, fett = 28.8, kohlenhydrate = 28.4,
            ballaststoffe = 6.55, eiweiss = 24.94,
            quelle = NaehrwertQuelle.WEBRECHERCHE, quelleHinweis = QUELLE_WEB_HINWEIS
        ),
        Rohstoff(
            name = "Senfsaat, braun", kategorie = "Gewürz/Saat",
            energieKj = 2039.0, energieKcal = 487.0, fett = 28.8, kohlenhydrate = 28.4,
            ballaststoffe = 6.5, eiweiss = 24.9,
            quelle = NaehrwertQuelle.WEBRECHERCHE, quelleHinweis = QUELLE_WEB_HINWEIS
        )
    )
}
