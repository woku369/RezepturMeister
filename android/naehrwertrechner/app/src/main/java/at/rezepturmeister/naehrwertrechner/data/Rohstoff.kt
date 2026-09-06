package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ein Rohstoff/eine Zutat mit Nährwertangaben pro 100 g bzw. 100 ml.
 * Feldbenennung bewusst analog zu Models/Rohstoff.cs im RezepturMeister (.NET),
 * damit beide "Meister"-Anwendungen dieselbe fachliche Struktur verwenden.
 *
 * Alle Nährwertfelder sind nullable: fehlende Werte werden bei der Berechnung
 * als "fehlende Daten" ausgewiesen statt stillschweigend als 0 gerechnet zu werden.
 *
 * Eindeutiger Index auf `name`: dadurch kann die Start-Rohstoffliste (SeedData)
 * bei jedem App-Start gefahrlos erneut eingefügt werden (OnConflictStrategy.IGNORE
 * überspringt bereits vorhandene Namen) – neue Einträge aus einem App-Update
 * erscheinen so automatisch, ohne von dir bereits eingetragene/korrigierte
 * Rohstoffe zu überschreiben.
 */
@Entity(tableName = "rohstoffe", indices = [Index(value = ["name"], unique = true)])
data class Rohstoff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val kategorie: String = "",

    // Dichte in g/ml, für Umrechnung 100 g <-> 100 ml (z. B. bei Flüssigkeiten/Laken)
    val dichte: Double? = null,

    // Alkoholgehalt in % vol – reine Kennzeichnungsangabe (Art. 28 LMIV). Der
    // Energiebeitrag von Alkohol fließt separat über `alkoholGramm` in die
    // Berechnung ein (siehe dort) – NICHT aus diesem %-vol-Wert abgeleitet.
    val alkoholGehaltVol: Double? = null,

    // Alkohol in Gramm pro 100 g Rohstoff – Berechnungsgrundlage für die
    // Energieformel (Anhang XIV: 29 kJ/g bzw. 7 kcal/g), getrennt von
    // `alkoholGehaltVol` (%vol, nur Kennzeichnung). Default 0.0: für die
    // allermeisten Rohstoffe korrekt, nur bei alkoholhaltigen Zutaten (Wein,
    // manche Essigsorten) ungleich null.
    val alkoholGramm: Double = 0.0,

    // Organische Säuren in Gramm pro 100 g Rohstoff – Berechnungsgrundlage für
    // die Energieformel (Anhang XIV: 13 kJ/g bzw. 3 kcal/g). Default 0.0: für
    // die allermeisten Lebensmittel korrekt (Fette, Zucker, Fleisch, die
    // meisten Gemüse enthalten praktisch keine relevanten Mengen), nur bei
    // Essig/sauren Zutaten ungleich null. Bewusst NICHT nullable wie die
    // übrigen Makronährstoffe, weil 0 hier fast immer die fachlich richtige
    // Annahme ist – anders als z. B. bei Fett oder Eiweiß.
    val organischeSaeuren: Double = 0.0,

    // Nährwerte pro 100 g. WICHTIG: energieKj/energieKcal sind reine
    // Referenz-/Anzeigewerte (z. B. für die Rohstoffliste) und werden NICHT
    // mehr direkt in die Rezeptur-Gesamtenergie summiert – siehe
    // NaehrwertBerechnung.berechne() für den Hintergrund (Art. 31/Anhang XIV
    // LMIV verlangt, dass der deklarierte Energiewert aus den deklarierten
    // Fett-/Kohlenhydrat-/Eiweiß-/Ballaststoff-/Alkohol-/Säurewerten
    // RECHNERISCH ABGELEITET wird, nicht aus einer unabhängig bestimmten
    // "gemessenen" Energie – reale Lebensmitteldatenbanken wie USDA weichen
    // davon regelmäßig ab, siehe Kommentar in NaehrwertBerechnung.kt).
    val energieKj: Double? = null,
    val energieKcal: Double? = null,
    val fett: Double? = null,
    val gesaettigteFettsaeuren: Double? = null,
    val kohlenhydrate: Double? = null,
    val zucker: Double? = null,
    val ballaststoffe: Double? = null,
    val eiweiss: Double? = null,
    val salz: Double? = null,

    val quelle: NaehrwertQuelle = NaehrwertQuelle.UNBEKANNT,
    // Freitext: z. B. "Herstelleretikett Essig Marke X, MHD 2027", Datum, Link, Charge
    val quelleHinweis: String = ""
) {
    /** Hat der Rohstoff alle Pflichtwerte für eine vollständige Deklaration? */
    fun istVollstaendig(): Boolean =
        listOf(energieKj, energieKcal, fett, gesaettigteFettsaeuren, kohlenhydrate, zucker, ballaststoffe, eiweiss, salz)
            .all { it != null }
}
