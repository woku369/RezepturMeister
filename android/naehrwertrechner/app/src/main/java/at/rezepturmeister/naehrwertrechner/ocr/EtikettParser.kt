package at.rezepturmeister.naehrwertrechner.ocr

/**
 * Ergebnis des Etikett-Parsers: Nährwerte pro 100 g, wie sie aus dem OCR-Rohtext
 * herausgelesen wurden. Ein Feld ist null, wenn der Parser dafür KEINEN Wert im Text
 * gefunden hat – niemals geschätzt oder auf 0 gesetzt.
 *
 * WICHTIG: Dies ist ein Vorschlag, keine gesicherte Nährwertangabe. Etiketten variieren
 * stark in Layout und Formulierung, und OCR macht Lesefehler (v. a. bei Kommas/Punkten
 * und Umlauten). Jeder erkannte Wert muss vor dem Speichern im Rohstoff-Editor geprüft
 * und ggf. korrigiert werden.
 */
data class EtikettErgebnis(
    val energieKj: Double? = null,
    val energieKcal: Double? = null,
    val fett: Double? = null,
    val gesaettigteFettsaeuren: Double? = null,
    val kohlenhydrate: Double? = null,
    val zucker: Double? = null,
    val ballaststoffe: Double? = null,
    val eiweiss: Double? = null,
    val salz: Double? = null,
    val erkannterRohtext: String
) {
    /** Wie viele der 9 Pflichtfelder konnten aus dem Text erkannt werden? */
    fun anzahlErkannt(): Int =
        listOf(energieKj, energieKcal, fett, gesaettigteFettsaeuren, kohlenhydrate, zucker, ballaststoffe, eiweiss, salz)
            .count { it != null }
}

/**
 * Liest aus dem Rohtext einer fotografierten Nährwerttabelle (deutschsprachig, Format
 * nach Anhang XV LMIV: Brennwert/Energie, Fett, davon gesättigte Fettsäuren,
 * Kohlenhydrate, davon Zucker, Ballaststoffe, Eiweiß, Salz) die Werte pro 100 g/100 ml
 * heraus. Reine Textverarbeitung, keine Android-Abhängigkeit – daher direkt mit
 * JVM-Unit-Tests prüfbar, ohne Kamera/ML Kit zu benötigen.
 *
 * Bewusst tolerant gegenüber typischen OCR-Eigenheiten:
 * - Etikett-Zeilenumbrüche zwischen Bezeichnung und Zahl (Bezeichnung und Wert stehen
 *   im Originalbild oft in getrennten Zeilen/Spalten)
 * - Tausenderpunkt bei kJ-Werten ("1.490") und Dezimalkomma statt -punkt ("0,4")
 * - Verlorene Umlaute ("Fettsauren" statt "Fettsäuren", "Eiweiss" statt "Eiweiß")
 * - Spurenmengen mit "<" ("<0,01 g")
 */
object EtikettParser {

    private const val ZAHL = """<?\s*(\d{1,3}(?:\.\d{3})*(?:,\d+)?|\d+(?:,\d+)?)"""

    private fun zahlNach(schluesselwort: Regex, text: String, luecke: Int = 25): Double? {
        val treffer = schluesselwort.find(text) ?: return null
        val ausschnitt = text.substring(treffer.range.last + 1, minOf(text.length, treffer.range.last + 1 + luecke))
        val zahlMatch = Regex(ZAHL).find(ausschnitt) ?: return null
        return zahlMatch.groupValues[1].replace(".", "").replace(",", ".").toDoubleOrNull()
    }

    fun parse(rohtext: String): EtikettErgebnis {
        // Zeilenumbrüche/Mehrfach-Leerzeichen zu einzelnen Leerzeichen zusammenfassen,
        // damit Schlüsselwort und Zahl auch dann gefunden werden, wenn sie im Foto auf
        // getrennten Zeilen standen.
        val text = rohtext.replace(Regex("\\s+"), " ")

        val (energieKj, energieKcal) = parseEnergie(text)

        return EtikettErgebnis(
            energieKj = energieKj,
            energieKcal = energieKcal,
            fett = zahlNach(Regex("""(?i)\bFett\b"""), text),
            gesaettigteFettsaeuren = zahlNach(Regex("""(?i)ges[äa]ttigte[n]?\s+Fetts[äa]uren|davon\s+ges[äa]ttigt"""), text),
            kohlenhydrate = zahlNach(Regex("""(?i)Kohlenhydrate"""), text),
            zucker = zahlNach(Regex("""(?i)\bZucker\b"""), text),
            ballaststoffe = zahlNach(Regex("""(?i)Ballaststoffe"""), text),
            eiweiss = zahlNach(Regex("""(?i)Eiwei(?:ß|ss)"""), text),
            salz = zahlNach(Regex("""(?i)\bSalz\b"""), text),
            erkannterRohtext = rohtext
        )
    }

    private fun parseEnergie(text: String): Pair<Double?, Double?> {
        // Bevorzugt: kombiniertes Muster "... kJ ... kcal" im selben Satzteil (Anhang-XV-
        // Standardformat "1490 kJ/356 kcal"). Fallback: kJ und kcal unabhängig voneinander
        // an ihrer jeweils ersten Fundstelle im Text.
        val kombiniert = Regex("""(?i)(?:Brennwert|Energie)[^0-9]{0,20}$ZAHL\s*kJ[^0-9]{0,15}$ZAHL\s*kcal""")
            .find(text)
        if (kombiniert != null) {
            val kj = kombiniert.groupValues[1].replace(".", "").replace(",", ".").toDoubleOrNull()
            val kcal = kombiniert.groupValues[2].replace(".", "").replace(",", ".").toDoubleOrNull()
            return kj to kcal
        }
        val kj = zahlNach(Regex("""(?i)(?:Brennwert|Energie)"""), text, luecke = 15)
            ?: Regex("""(?i)$ZAHL\s*kJ""").find(text)?.groupValues?.get(1)?.replace(".", "")?.replace(",", ".")?.toDoubleOrNull()
        val kcal = Regex("""(?i)$ZAHL\s*kcal""").find(text)?.groupValues?.get(1)?.replace(".", "")?.replace(",", ".")?.toDoubleOrNull()
        return kj to kcal
    }
}
