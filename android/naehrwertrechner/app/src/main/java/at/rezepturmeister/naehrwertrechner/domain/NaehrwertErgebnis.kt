package at.rezepturmeister.naehrwertrechner.domain

/**
 * Aggregiertes Berechnungsergebnis einer Rezeptur. Alle Nährwerte sind auf
 * [bezugsgroesse] normiert (pro 100 g ODER pro 100 ml, siehe dort).
 */
data class NaehrwertErgebnis(
    val energieKj: Double,
    val energieKcal: Double,
    val fett: Double,
    val gesaettigteFettsaeuren: Double,
    val kohlenhydrate: Double,
    val zucker: Double,
    val ballaststoffe: Double,
    val eiweiss: Double,
    val salz: Double,

    /**
     * Alkohol und organische Säuren (g pro Bezugsgröße) – keine eigenen Pflichtangaben der
     * Nährwertdeklaration (Anhang XV), aber Eingangsgrößen der Energieformel
     * (Anhang XIV). Werden hier für die nachvollziehbare Berechnungsdarstellung
     * (z. B. Excel-Export für Behörden) mitgeführt statt nur intern in
     * NaehrwertBerechnung zu verschwinden.
     */
    val alkohol: Double,
    val organischeSaeuren: Double,

    val bezugsgroesse: Bezugsgroesse,

    /** Physisches Gesamtgewicht aller Zutaten in Gramm – unabhängig von [bezugsgroesse]. */
    val gesamtGewichtGramm: Double,

    /**
     * Gemessenes Gesamtvolumen des fertigen Ansatzes in ml – nur gesetzt, wenn
     * [bezugsgroesse] PRO_100_ML ist. Bewusst eine Eingabe, keine aus den
     * Zutatenmengen abgeleitete Summe (Alkohol/Wasser mischen sich nicht additiv).
     */
    val gesamtvolumenMl: Double?,

    /** Zutatenliste absteigend nach Gewicht – Reihenfolge für die Zutatenkennzeichnung (Art. 18 LMIV). */
    val zutatenliste: List<ZutatNaehrwertInfo>,

    /**
     * Rohstoffe mit unvollständigen Nährwertdaten, bei denen das NICHT als
     * vernachlässigbar akzeptiert wurde. Die Deklaration ist erst vollständig, wenn
     * diese Liste leer ist.
     */
    val fehlendeDaten: List<String>,

    /**
     * Rohstoffe mit unvollständigen Nährwertdaten, bei denen die Lücke bewusst als
     * vernachlässigbar markiert wurde (Rohstoff.vernachlaessigbar) – blockiert die
     * Deklaration nicht, wird aber zur Nachvollziehbarkeit weiter ausgewiesen statt
     * stillschweigend zu verschwinden.
     */
    val alsVernachlaessigbarAkzeptiert: List<String> = emptyList()
) {
    val istVollstaendig: Boolean get() = fehlendeDaten.isEmpty()
}

data class ZutatNaehrwertInfo(
    val name: String,
    val gewichtGramm: Double,
    val prozent: Double,
    val hatVollstaendigeNaehrwertdaten: Boolean,
    val alsVernachlaessigbarMarkiert: Boolean = false
)
