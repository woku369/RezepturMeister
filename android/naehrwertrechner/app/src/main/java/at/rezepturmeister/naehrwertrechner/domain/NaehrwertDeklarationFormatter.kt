package at.rezepturmeister.naehrwertrechner.domain

import java.util.Locale

/**
 * Formatiert ein Berechnungsergebnis in der von Anhang XV LMIV vorgegebenen,
 * verbindlichen Reihenfolge: Brennwert; Fett (davon gesättigte Fettsäuren);
 * Kohlenhydrate (davon Zucker); Ballaststoffe; Eiweiß; Salz.
 */
object NaehrwertDeklarationFormatter {

    fun alsText(ergebnis: NaehrwertErgebnis): String {
        fun z(wert: Double, nachkomma: Int = 1): String =
            String.format(Locale.GERMANY, "%.${nachkomma}f", wert)

        return buildString {
            appendLine("Nährwertdeklaration (pro 100 g)")
            appendLine("Brennwert: ${z(ergebnis.energieKj, 0)} kJ / ${z(ergebnis.energieKcal, 0)} kcal")
            appendLine("Fett: ${z(ergebnis.fett)} g")
            appendLine("  davon gesättigte Fettsäuren: ${z(ergebnis.gesaettigteFettsaeuren)} g")
            appendLine("Kohlenhydrate: ${z(ergebnis.kohlenhydrate)} g")
            appendLine("  davon Zucker: ${z(ergebnis.zucker)} g")
            appendLine("Ballaststoffe: ${z(ergebnis.ballaststoffe)} g")
            appendLine("Eiweiß: ${z(ergebnis.eiweiss)} g")
            appendLine("Salz: ${z(ergebnis.salz)} g")
            if (!ergebnis.istVollstaendig) {
                appendLine()
                appendLine("ACHTUNG – unvollständige Datenbasis, NICHT für die Kennzeichnung verwenden:")
                ergebnis.fehlendeDaten.forEach { appendLine("  - $it: Nährwertdaten unvollständig") }
            }
        }
    }
}
