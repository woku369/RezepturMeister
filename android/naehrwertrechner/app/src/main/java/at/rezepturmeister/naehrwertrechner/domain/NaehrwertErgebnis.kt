package at.rezepturmeister.naehrwertrechner.domain

/** Aggregiertes Berechnungsergebnis einer Rezeptur, Werte pro 100 g Gesamtansatz. */
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

    val gesamtGewichtGramm: Double,

    /** Zutatenliste absteigend nach Gewicht – Reihenfolge für die Zutatenkennzeichnung (Art. 18 LMIV). */
    val zutatenliste: List<ZutatNaehrwertInfo>,

    /** Rohstoffe, bei denen mindestens ein Nährwertfeld fehlt. Die Deklaration ist erst vollständig, wenn diese Liste leer ist. */
    val fehlendeDaten: List<String>
) {
    val istVollstaendig: Boolean get() = fehlendeDaten.isEmpty()
}

data class ZutatNaehrwertInfo(
    val name: String,
    val gewichtGramm: Double,
    val prozent: Double,
    val hatVollstaendigeNaehrwertdaten: Boolean
)
