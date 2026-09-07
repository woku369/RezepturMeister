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
 * Eindeutiger Index auf `name`: dadurch kann RohstoffDao.syncSeedDaten() beim App-Start
 * per Name erkennen, ob ein SeedData-Eintrag bereits existiert (dann aktualisieren,
 * id bleibt erhalten) oder neu ist (dann einfügen). Von dir über den Rohstoff-Editor
 * angelegte/geänderte Rohstoffe (vomNutzerBearbeitet = true) werden dabei nie
 * überschrieben.
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
    val quelleHinweis: String = "",

    // true, sobald dieser Rohstoff über den Rohstoff-Editor angelegt/geändert wurde
    // (manuell oder per Foto-Etikett-Erkennung). Schützt den Datensatz davor, beim
    // nächsten App-Start durch RohstoffDao.syncSeedDaten() wieder mit dem SeedData-Wert
    // überschrieben zu werden – siehe dort.
    val vomNutzerBearbeitet: Boolean = false,

    // true, wenn eine unvollständige Datenbasis bei diesem Rohstoff bewusst als
    // vernachlässigbar akzeptiert wurde (z. B. ein Kräutermazerat ohne eigene
    // Nährwertdaten aber mit bekanntem Alkoholgehalt, oder eine Gewürzmischung in
    // sehr kleiner Einsatzmenge). Ändert NICHT das Ergebnis von istVollstaendig()
    // (das bleibt eine objektive Aussage über die Datenlage), unterdrückt aber die
    // "unvollständig"-Warnung in Rezepturen, die diesen Rohstoff verwenden – siehe
    // erfordertWarnhinweis(). Formalisiert das bisher nur per Freitext in
    // quelleHinweis dokumentierte "vernachlässigt"-Muster als eigenes Feld.
    val vernachlaessigbar: Boolean = false
) {
    /** Hat der Rohstoff alle Pflichtwerte für eine vollständige Deklaration? */
    fun istVollstaendig(): Boolean =
        listOf(energieKj, energieKcal, fett, gesaettigteFettsaeuren, kohlenhydrate, zucker, ballaststoffe, eiweiss, salz)
            .all { it != null }

    /** Soll eine unvollständige Datenbasis dieses Rohstoffs eine Warnung auslösen? */
    fun erfordertWarnhinweis(): Boolean = !istVollstaendig() && !vernachlaessigbar
}
