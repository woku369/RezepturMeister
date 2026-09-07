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

    // Dichte September 2026 ergänzt: lineare Näherung zwischen reinem Wasser (1,000 g/ml) und
    // reiner Essigsäure (1,049 g/ml bei 25 °C) je nach Säuregehalt – kein Messwert für ein
    // konkretes Produkt, aber ausreichend für die ml->g-Umrechnung bei der Rezeptureingabe. Ändert
    // NICHTS an den Energie-/Säurewerten selbst (die sind bereits als g/100g definiert, siehe
    // QUELLE_BERECHNET_HINWEIS), betrifft nur das neue dichte-Feld.
    private const val DICHTE_ESSIG_HINWEIS =
        "Dichte: lineare Näherung zwischen Wasser (1,000 g/ml) und reiner Essigsäure " +
            "(1,049 g/ml) je nach Säuregehalt, kein Messwert des konkreten Produkts."

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
            dichte = 1.006,
            energieKj = 169.0, energieKcal = 39.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            organischeSaeuren = 13.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "$QUELLE_BERECHNET_HINWEIS $DICHTE_ESSIG_HINWEIS"
        ),
        Rohstoff(
            name = "Speiseessig 2% Säure", kategorie = "Einlegeflüssigkeit",
            dichte = 1.001,
            energieKj = 26.0, energieKcal = 6.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            organischeSaeuren = 2.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "$QUELLE_BERECHNET_HINWEIS $DICHTE_ESSIG_HINWEIS"
        ),
        Rohstoff(
            name = "Essigessenz 80% Säure", kategorie = "Grundstoff",
            dichte = 1.039,
            energieKj = 1040.0, energieKcal = 240.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            organischeSaeuren = 80.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "$QUELLE_BERECHNET_HINWEIS $DICHTE_ESSIG_HINWEIS"
        ),
        Rohstoff(
            name = "Balsamico-Essig 6% Säure", kategorie = "Einlegeflüssigkeit",
            dichte = 1.07,
            energieKj = 368.0, energieKcal = 88.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 17.0, zucker = 14.9, ballaststoffe = 0.0, eiweiss = 0.5, salz = 0.0,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Balsamico enthält Traubenmost/Zucker – NICHT wie " +
                "einfacher Speiseessig rein rechnerisch aus dem Säuregehalt ableitbar. Zuckergehalt " +
                "schwankt stark je Marke/Qualität – Herstelleretikett zwingend erforderlich. " +
                "Dichte 1,07 g/ml September 2026 ergänzt (Näherung, üblicher Bereich 1,06–1,08 g/ml " +
                "laut Handelsanalysen; echter Balsamico-Essig g.g.A. muss laut Spezifikation " +
                "mindestens 1,06 g/ml erreichen). ACHTUNG UNGEKLÄRT: anders als bei Ahornsirup/Rotwein " +
                "konnte NICHT verifiziert werden, ob die obigen Energie-/Nährwerte ursprünglich pro " +
                "100 ml oder pro 100 g recherchiert wurden – bei Verwendung des tatsächlichen Produkts " +
                "unbedingt am Originaletikett gegenprüfen, bevor diese Werte für eine Deklaration " +
                "verwendet werden."
        ),
        Rohstoff(
            name = "Weingeistessig 20% Säure", kategorie = "Einlegeflüssigkeit",
            dichte = 1.010,
            energieKj = 260.0, energieKcal = 60.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            organischeSaeuren = 20.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "$QUELLE_BERECHNET_HINWEIS $DICHTE_ESSIG_HINWEIS"
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
            energieKj = 166.0, energieKcal = 40.0, fett = 0.4, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 8.8, zucker = 5.3, ballaststoffe = 1.5, eiweiss = 1.9, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Erneut recherchiert September 2026 (USDA FDC 170106) " +
                "– Energie/Fett/Kohlenhydrate/Ballaststoffe/Eiweiß bestätigt. Salzgehalt (Natrium) " +
                "und gesättigte Fettsäuren waren in den Suchtreffern nicht enthalten; auf " +
                "Nutzerwunsch als vernachlässigbar auf 0 gesetzt statt weiter offen zu lassen " +
                "(bei 0,4 g Gesamtfett ohnehin kaum relevant)."
        ),
        Rohstoff(
            name = "Zwiebel, roh", kategorie = "Gemüse",
            energieKj = 167.0, energieKcal = 40.0, fett = 0.1, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 9.3, zucker = 4.2, ballaststoffe = 1.7, eiweiss = 1.1, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Zuckerwert (4,2 g) September 2026 ergänzt " +
                "(vorherige Recherche hatte hier fälschlich Ballaststoffe statt Zucker geliefert). " +
                "Salz und gesättigte Fettsäuren auf Nutzerwunsch als vernachlässigbar auf 0 gesetzt " +
                "statt weiter zu recherchieren (bei 0,1 g Gesamtfett ohnehin kaum relevant)."
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
        // Hinweis "Rauchsalz": bewusst KEIN eigener Rohstoff (Absprache September 2026) –
        // Rezepturen, die Rauchsalz verwenden, werden mit "Salz (Speisesalz, NaCl)"
        // gerechnet. Nährwertlich ohnehin identisch (NaCl + geschmacklich vernachlässigbares
        // Raucharoma), das vereinfacht die Rohstoffliste.

        // ---- Kräuter/Gewürze ----
        Rohstoff(
            name = "Rosmarin, getrocknet", kategorie = "Gewürz/Kräuter",
            energieKj = 1385.0, energieKcal = 331.0, fett = 15.22, kohlenhydrate = 64.06,
            ballaststoffe = 43.0, eiweiss = 4.88,
            quelle = NaehrwertQuelle.USDA, quelleHinweis = QUELLE_USDA_HINWEIS
        ),
        Rohstoff(
            name = "Thymian, getrocknet", kategorie = "Gewürz/Kräuter",
            energieKj = 1155.0, energieKcal = 276.0, fett = 7.43, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 63.94, zucker = 0.0, ballaststoffe = 37.0, eiweiss = 9.11, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Salz und gesättigte Fettsäuren auf Nutzerwunsch " +
                "als vernachlässigbar auf 0 gesetzt. Zuckerwert war in den Suchtreffern nicht " +
                "ausgewiesen – aus Konsistenz zur selben Vereinfachung ebenfalls auf 0 gesetzt " +
                "statt offen zu lassen (Einsatzmenge typischerweise wenige Gramm)."
        ),
        Rohstoff(
            name = "Zimt, gemahlen", kategorie = "Gewürz/Kräuter",
            energieKj = 1035.0, energieKcal = 247.0, fett = 1.2, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 80.6, zucker = 2.2, ballaststoffe = 53.1, eiweiss = 4.0, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Salz und gesättigte Fettsäuren auf Nutzerwunsch " +
                "als vernachlässigbar auf 0 gesetzt."
        ),
        Rohstoff(
            name = "Kreuzkümmel, gemahlen", kategorie = "Gewürz/Kräuter",
            energieKj = 1708.0, energieKcal = 408.0, fett = 22.27, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 34.0, zucker = 6.8, ballaststoffe = 10.5, eiweiss = 17.8, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Salz und gesättigte Fettsäuren auf Nutzerwunsch " +
                "als vernachlässigbar auf 0 gesetzt (ersetzt den zuvor aus der Natriumangabe " +
                "abgeleiteten Salzwert von 0,53 g – bei 5 g Einsatzmenge in einem 10-kg-Ansatz " +
                "ohnehin nicht spürbar)."
        ),
        Rohstoff(
            name = "Schwarzkümmel, gemahlen", kategorie = "Gewürz/Kräuter",
            energieKj = 1443.0, energieKcal = 345.0, fett = 15.0, gesaettigteFettsaeuren = 0.5,
            kohlenhydrate = 52.0, eiweiss = 16.0,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "Websuche September 2026 (Wikifit u. a., nicht amtlich) – Zucker, " +
                "Ballaststoffe und Salz nicht ausgewiesen, daher NULL belassen. Auf Anfrage ergänzt " +
                "(kein Kreuzverweis mit Kreuzkümmel/Cuminum cyminum – Schwarzkümmel ist botanisch " +
                "Nigella sativa, ein anderes Gewürz)."
        ),
        // Kein pauschaler Sammelwert "getrocknete Gewürze" angelegt: die Streuung zwischen den
        // oben recherchierten Einzelgewürzen ist zu groß (Fett z. B. 1,2–22,3 g/100 g), ein
        // Durchschnitt wäre keine seriöse Nährwertangabe. Für neue, noch unrecherchierte Gewürze
        // in kleiner Menge (wenige Gramm) gilt daher: einzeln recherchieren, oder wenn nichts
        // Verlässliches auffindbar ist, den Beitrag zur Berechnung vernachlässigen (Menge meist
        // < 1 % des Ansatzes) statt zu schätzen.
        Rohstoff(
            name = "Italienische Kräutermischung, getrocknet (Markenprodukt \"JK\")",
            kategorie = "Gewürz/Kräuter",
            energieKj = 0.0, energieKcal = 0.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Laut Nutzer (September 2026): Mischung aus getrockneten mediterranen " +
                "Kräutern, keine Herstellerdaten bekannt. Zwei zuvor gefundene Websuche-Treffer " +
                "widersprachen sich zudem stark (vermutlich unterschiedliche Produkttypen). Da die " +
                "Menge in der Rezeptur \"Datteltomaten geschmort\" mit ca. 1,3 % des Ansatzes gering " +
                "ist, wird der Nährwertbeitrag auf Wunsch bewusst vernachlässigt (auf 0 gesetzt) " +
                "statt geschätzt – das ist eine bewusste Vereinfachung, KEIN tatsächlicher Nährwert " +
                "von Null. Bei größerer Einsatzmenge oder verfügbarem Herstellerdatenblatt ersetzen."
        ),

        // ---- Weitere Zutaten der zweiten Produktgruppe ----
        Rohstoff(
            name = "Speck (Bauchspeck, geräuchert)", kategorie = "Fleischprodukt",
            energieKj = 1557.0, energieKcal = 372.0, fett = 30.0, kohlenhydrate = 0.4,
            eiweiss = 25.0,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "Auf Wunsch September 2026 umgestellt: die tatsächlich verwendete Ware " +
                "kommt vom Fleischhauer und hat keine eigenen Nährwertangaben. Als Näherung wird " +
                "stattdessen ein reales, verbreitetes Fertigprodukt herangezogen (Handl Tyrol " +
                "Speckwürfel, 372 kcal – übereinstimmend bei Wikifit/Fatsecret/Fettrechner). " +
                "Ersetzt den vorherigen Mittelwert aus 3 widersprüchlichen Websuche-Treffern " +
                "(320-405 kcal). Salz und gesättigte Fettsäuren für dieses Produkt nicht gefunden, " +
                "daher NULL belassen. Weicht die tatsächliche Fleischhauer-Ware deutlich ab (z. B. " +
                "anderer Fettanteil, kein Pökelsalz), gegen ein passenderes Referenzprodukt oder " +
                "eine externe Laboranalyse tauschen."
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
            dichte = 1.322,
            // Etikett/Verbraucherportale (Fddb, Wikifit, Codecheck) geben 356 kcal/1491 kJ/89 g
            // Kohlenhydrate JE 100 ML an (Ahornsirup wird als Flüssigkeit in ml-Gebinden verkauft) -
            // das war zuvor unbemerkt direkt als "pro 100 g" übernommen worden (Fehler, September
            // 2026 durch Nutzer aufgedeckt). Korrekt umgerechnet mit der Dichte 1,322 g/ml (offizieller
            // Ahornsirup-Dichtestandard bei 66° Brix/Raumtemperatur, SCP Science/BMR):
            // 356 kcal ÷ 1,322 = 269,3 kcal; 1491 kJ ÷ 1,322 = 1127,8 kJ; 89 g ÷ 1,322 = 67,3 g.
            // Deckt sich gut mit dem allgemein bekannten USDA-Richtwert für Ahornsirup
            // (~260 kcal/~67 g Kohlenhydrate pro 100 g) - gute Plausibilitätsprobe für die Umrechnung.
            energieKj = 1127.8, energieKcal = 269.3, fett = 0.0, kohlenhydrate = 67.3, zucker = 67.3,
            eiweiss = 0.0,
            quelle = NaehrwertQuelle.HERSTELLERETIKETT,
            quelleHinweis = "Auf Wunsch September 2026 auf das konkret verwendete Produkt " +
                "(Spar Natur*pur Bio-Ahornsirup, 0,5 l) umgestellt. Rohwert stammt nicht direkt vom " +
                "Etikett/spar.at, sondern aus mehreren unabhängigen Verbraucherportalen " +
                "(Fddb, Wikifit, Codecheck), die übereinstimmend dieselben Zahlen für dieses Produkt " +
                "zeigen – höhere Verlässlichkeit als eine Einzelquelle, aber noch keine " +
                "Originaletikett-Prüfung. Die Portale geben die Werte pro 100 ml an, hier per Dichte " +
                "(1,322 g/ml, Näherung über den Industriestandard, nicht am konkreten Produkt " +
                "gemessen) auf pro 100 g umgerechnet – siehe Kommentar oben. Ballaststoffe und Salz " +
                "nicht ausgewiesen, NULL belassen."
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
            energieKj = 800.0, energieKcal = 200.0, fett = 0.0, gesaettigteFettsaeuren = 0.0,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 100.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.BERECHNET,
            quelleHinweis = "Auf Wunsch September 2026 umgestellt: reines Pektinpulver (E440) ist " +
                "chemisch nahezu 100 % löslicher Ballaststoff (ein Polysaccharid) – Energie daher " +
                "über den bereits für Speiseessig verwendeten Anhang-XIV-Ballaststofffaktor " +
                "(8 kJ/g bzw. 2 kcal/g) berechnet, analog zur Essigsäure-Methode. Ersetzt eine " +
                "vorherige Einzelquelle (Fddb, Handelsprodukt \"Apfelpektin Flocken\": 196 kcal, " +
                "44,5 g Kohlenhydrate, 36,5 g Ballaststoffe), die mit Traubenzucker gestrecktes " +
                "Handelsprodukt zeigte statt Reinstoff-Pektin. Falls das konkret verwendete Produkt " +
                "ebenfalls gestreckt ist, weichen die realen Werte davon ab – Herstellerdatenblatt " +
                "prüfen."
        ),
        Rohstoff(
            name = "Rotwein, trocken", kategorie = "Sonstige Zutat",
            alkoholGehaltVol = 12.0,
            dichte = 0.99,
            // 12 %vol × 0,789 g/ml (Ethanoldichte) ÷ 0,99 g/ml (Weindichte, siehe dichte-Feld) ≈
            // 9,56 g Alkohol/100 g – Berechnungsgrundlage für die Energieformel, siehe
            // NaehrwertBerechnung.alkoholGrammAusVol(). Vorher mit ≈1,0 g/ml angenommen (9,47 g);
            // jetzt mit dem dokumentierten Weindichte-Wert neu berechnet, Effekt klein (~1%).
            alkoholGramm = 9.56,
            // Die 2026 gefundenen Quellen nannten diese Werte JE 100 ML (siehe Hinweis unten) und
            // wurden zuvor unbemerkt direkt als "pro 100 g" übernommen. Mit Dichte 0,99 g/ml (OIV-
            // Referenzbereich für Wein: 0,992–1,012 g/ml bei 20°C, trockener Wein mit wenig
            // Restzucker eher am unteren Ende) auf pro 100 g umgerechnet – Effekt ist bei Wein
            // wegen der dichten nahe 1,0 g/ml klein (<2%), aber der Vollständigkeit halber korrigiert.
            energieKj = 359.6, energieKcal = 85.9, fett = 0.10, kohlenhydrate = 2.63, zucker = 1.52,
            eiweiss = 0.10,
            quelle = NaehrwertQuelle.WEBRECHERCHE,
            quelleHinweis = "$QUELLE_WEB_HINWEIS Rotwein-Nährwerte streuen stark nach Sorte/" +
                "Restzuckergehalt/Alkoholgehalt (Quellen nannten 0,8–2,6 g Kohlenhydrate/100 ml, hier " +
                "mit Dichte 0,99 g/ml auf pro 100 g umgerechnet, siehe Kommentar oben) – für die " +
                "konkret verwendete Weinmarke Herstellerangabe verwenden. energieKj/energieKcal sind " +
                "nur noch Referenzwerte (Rohstoffliste); für die Rezeptur-Gesamtenergie zählt seit der " +
                "Anhang-XIV-Korrektur ausschließlich alkoholGramm (~9,56 g, aus 12 %vol abgeleitet) " +
                "zusammen mit Fett/Kohlenhydrate/Eiweiß – ohne alkoholGramm würde der mit Abstand " +
                "größte Energieanteil von Wein (Alkohol) in der Rezeptur rechnerisch verschwinden. " +
                "Laut Nutzer wird konkret Spar-Rotwein im Doppelliter (2-l-Gebinde) verwendet – dafür " +
                "war online keine produktspezifische Nährwertangabe auffindbar (bei einfachen " +
                "\"Doppler\"-Weinen seltener indiziert als bei Flaschenweinen). Da österreichische " +
                "Wein-Kartons den Energiewert i. d. R. selbst aufdrucken, ist ein Blick auf das " +
                "tatsächliche Gebinde hier schneller und verlässlicher als eine weitere Websuche – " +
                "bitte dort ablesen (Energie UND Alkoholgehalt) und ersetzen, der obige Wert bleibt " +
                "bis dahin ein allgemeiner Näherungswert."
        ),

        // ---- Fett/Öl ----
        Rohstoff(
            name = "Sonnenblumenöl", kategorie = "Fett/Öl",
            dichte = 0.919,
            energieKj = 3700.0, energieKcal = 884.0, fett = 100.0, gesaettigteFettsaeuren = 10.3,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Reines Pflanzenöl – Fettwert (100 g/100 g) " +
                "definitorisch, gesättigte Fettsäuren laut USDA-Referenz für linolsäurereiches " +
                "Sonnenblumenöl (\"linoleic\"); High-Oleic-Sonnenblumenöl hat einen anderen Anteil " +
                "gesättigter/einfach ungesättigter Fettsäuren – bei Verwendung Etikett prüfen. " +
                "Dichte 0,919 g/ml (September 2026, Standardwert bei 20 °C) ergänzt."
        ),
        Rohstoff(
            name = "Olivenöl", kategorie = "Fett/Öl",
            dichte = 0.915,
            energieKj = 3699.0, energieKcal = 884.0, fett = 100.0, gesaettigteFettsaeuren = 13.8,
            kohlenhydrate = 0.0, zucker = 0.0, ballaststoffe = 0.0, eiweiss = 0.0, salz = 0.0,
            quelle = NaehrwertQuelle.USDA,
            quelleHinweis = "$QUELLE_USDA_HINWEIS Reines Pflanzenöl – Fettwert definitorisch, " +
                "gesättigte Fettsäuren laut USDA-Referenz für natives/raffiniertes Olivenöl. " +
                "Dichte 0,915 g/ml (September 2026, üblicher Bereich 0,913–0,916 g/ml bei 20 °C) " +
                "ergänzt."
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
