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
        Rohstoff(
            name = "Weingeistessig 20% Säure", kategorie = "Einlegeflüssigkeit",
            energieKj = 260.0, energieKcal = 60.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET, quelleHinweis = QUELLE_BERECHNET_HINWEIS
        ),
        Rohstoff(
            name = "Einlegeessig \"Pikfein\" (Markenprodukt)", kategorie = "Einlegeflüssigkeit",
            quelle = NaehrwertQuelle.UNBEKANNT,
            quelleHinweis = "TODO: fertig gewürzter/vorbereiteter Markenessig für Sofort-Einlegesud " +
                "(Rezeptur \"Paprika gelb, rot, Chili Sud\"). Anders als bei reinem Speiseessig NICHT " +
                "als verdünnte Essigsäure rechenbar, da solche Fertigprodukte häufig bereits Zucker/" +
                "Salz/Gewürze enthalten – keine Websuche für dieses konkrete Markenprodukt " +
                "durchgeführt. Herstelleretikett zwingend erforderlich, bevor diese Rezeptur " +
                "vollständig berechnet werden kann."
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
        Rohstoff(
            name = "Tomate, roh", kategorie = "Gemüse",
            energieKj = 75.0, energieKcal = 18.0, kohlenhydrate = 3.89, ballaststoffe = 1.2,
            eiweiss = 0.88,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Werte für \"Tomatoes, red, ripe, raw\" (FDC 170457) " +
                "– als Näherung auch für Datteltomaten-Sorten (Cupido, Dasher) verwendet, da keine " +
                "sortenspezifischen Werte gefunden wurden. Fett und Salz nicht in den Suchtreffern " +
                "enthalten, bewusst NULL belassen."
        ),
        Rohstoff(
            name = "Rauchsalz", kategorie = "Grundstoff",
            energieKj = 0.0, energieKcal = 0.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 100.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Chemisch NaCl mit Raucharoma (durch Räuchern oder Zusatz von " +
                "Raucharoma) – der Rauchanteil ist mengenmäßig vernachlässigbar, Nährwert daher wie " +
                "reines Speisesalz angesetzt. Keine gesonderte Websuche durchgeführt, da keine " +
                "andere Zusammensetzung zu erwarten ist."
        ),

        // ---- Kräuter/Gewürze ----
        Rohstoff(
            name = "Rosmarin, getrocknet", kategorie = "Gewürz/Kräuter",
            energieKj = 1385.0, energieKcal = 331.0, fett = 15.22, kohlenhydrate = 64.06,
            ballaststoffe = 43.0, eiweiss = 4.88,
            quelle = NaehrwertQuelle.USDA, quelleHinweis = QUELLE_USDA_HINWEIS
        ),
        Rohstoff(
            name = "Thymian, getrocknet", kategorie = "Gewürz/Kräuter",
            energieKj = 1155.0, energieKcal = 276.0, fett = 7.43, kohlenhydrate = 63.94,
            ballaststoffe = 37.0, eiweiss = 9.11,
            quelle = NaehrwertQuelle.USDA, quelleHinweis = QUELLE_USDA_HINWEIS
        ),
        Rohstoff(
            name = "Zimt, gemahlen", kategorie = "Gewürz/Kräuter",
            energieKj = 1035.0, energieKcal = 247.0, fett = 1.2, kohlenhydrate = 80.6, zucker = 2.2,
            ballaststoffe = 53.1, eiweiss = 4.0,
            quelle = NaehrwertQuelle.USDA, quelleHinweis = QUELLE_USDA_HINWEIS
        ),
        Rohstoff(
            name = "Kreuzkümmel, gemahlen", kategorie = "Gewürz/Kräuter",
            energieKj = 1708.0, energieKcal = 408.0, fett = 22.27, kohlenhydrate = 34.0, zucker = 6.8,
            ballaststoffe = 10.5, eiweiss = 17.8, salz = 0.53,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Salzwert aus Natriumangabe (0,21 g/100 g) über " +
                "die Standardumrechnung Salz = Natrium × 2,5 abgeleitet. Ein gefundener Wert für " +
                "gesättigte Fettsäuren (0,1 g) wirkte im Verhältnis zu 22,27 g Gesamtfett " +
                "unplausibel niedrig und wurde NICHT übernommen (NULL belassen), statt eine " +
                "wahrscheinlich fehlerhafte Zahl zu verwenden."
        ),
        Rohstoff(
            name = "Italienische Kräutermischung, getrocknet (Markenprodukt \"JK\")",
            kategorie = "Gewürz/Kräuter",
            quelle = NaehrwertQuelle.UNBEKANNT,
            quelleHinweis = "TODO: zwei Websuche-Treffer widersprachen sich stark (1255 kJ/299 kcal/" +
                "7,0 g Fett vs. 219 kJ/52 kcal/7,4 g Kohlenhydrate/0,4 g Fett – vermutlich unterschiedliche " +
                "Produkttypen, z. B. Trockenmischung vs. Öl-Kräuterpaste) und wurden daher bewusst NICHT " +
                "übernommen. Menge in der Rezeptur \"Datteltomaten geschmort\" ist mit ca. 1,3 % des " +
                "Ansatzes gering, aber ohne verlässlichen Wert bleibt die Deklaration unvollständig – " +
                "Herstellerdatenblatt des konkret verwendeten Produkts \"JK\" erforderlich."
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
            name = "Speck, geräuchert, ausgelassen (Grieben)", kategorie = "Fleischprodukt",
            energieKj = 3620.0, energieKcal = 865.0, fett = 91.5, kohlenhydrate = 0.0, eiweiss = 2.5,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "Mittelwert aus 2 unabhängigen Websuche-Treffern September 2026 für " +
                "ausgelassenen/ausgebratenen Speck bzw. Grieben (879 kcal/91,0 g Fett/3,0 g Eiweiß; " +
                "855 kcal/92 g Fett/1,9 g Eiweiß – als Griebenschmalz) – keine amtliche Quelle. " +
                "Deutlich fett- und energiereicher als roher/geräucherter Speck vor dem Auslassen, " +
                "da beim Auslassen Wasser verdunstet und Fett konzentriert wird – bewusst als " +
                "eigener Rohstoff angelegt statt den Wert für rohen Speck zu verwenden. Salz und " +
                "gesättigte Fettsäuren in keiner Quelle ausgewiesen, daher NULL belassen."
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
        Rohstoff(
            name = "Olivenöl", kategorie = "Fett/Öl",
            energieKj = 3699.0, energieKcal = 884.0, fett = 100.0, gesaettigteFettsaeuren = 13.8,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Reines Pflanzenöl – Fettwert definitorisch, " +
                "gesättigte Fettsäuren laut USDA-Referenz für natives/raffiniertes Olivenöl."
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
