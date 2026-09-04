package at.rezepturmeister.naehrwertrechner.data

/**
 * Herkunft eines Nährwert-Datensatzes. Pflichtangabe je Rohstoff, damit die
 * Berechnung nach Art. 31 LMIV (Berechnung anhand bekannter/durchschnittlicher
 * Werte bzw. allgemein anerkannter Daten) im Zweifel nachvollzogen werden kann.
 */
enum class NaehrwertQuelle(val anzeigename: String) {
    HERSTELLERETIKETT("Herstelleretikett/Produktdatenblatt"),
    OENWT("ÖNWT (Österreichische Nährwerttabelle)"),
    BLS("Bundeslebensmittelschlüssel (BLS)"),
    USDA("USDA FoodData Central"),
    LABORANALYSE("Eigene Laboranalyse"),
    BERECHNET("Berechnet (z. B. Anhang XIV LMIV aus Reinstoffgehalt)"),
    WEBRECHERCHE("Websuche/Verbraucherportal (nicht amtlich – vor Verwendung prüfen)"),
    UNBEKANNT("Quelle nicht dokumentiert")
}
