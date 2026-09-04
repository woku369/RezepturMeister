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

    // Alkoholgehalt in % vol – nur zur Kennzeichnung, NICHT zusätzlich in die
    // Energieberechnung eingerechnet (in handelsüblichen Nährwertangaben ist der
    // Energiebeitrag von Alkohol bereits in Energie_kJ/Energie_kcal enthalten).
    val alkoholGehaltVol: Double? = null,

    // Nährwerte pro 100 g
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
