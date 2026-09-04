package at.rezepturmeister.naehrwertrechner.export

import android.content.Context
import androidx.core.content.FileProvider
import android.net.Uri
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertErgebnis
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportiert ein Berechnungsergebnis als XLSX-Datei (zwei Tabellenblätter:
 * Nährwertdeklaration in Anhang-XV-Reihenfolge und die Zutatenliste).
 *
 * Nutzt FastExcel statt Apache POI, da POI auf Android für einfache
 * Schreibfälle unnötig schwer ist (Größe, bekannte Kompatibilitätsprobleme).
 */
object XlsxExporter {

    fun exportiere(context: Context, ergebnis: NaehrwertErgebnis, rezepturName: String): Uri {
        val exportDir = File(context.cacheDir, "export").apply { mkdirs() }
        val zeitstempel = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(Date())
        val datei = File(exportDir, "naehrwertdeklaration_${zeitstempel}.xlsx")

        FileOutputStream(datei).use { out ->
            val workbook = Workbook(out, "Nährwertrechner", "1.0")

            val deklaration = workbook.newWorksheet("Nährwertdeklaration")
            schreibeDeklaration(deklaration, ergebnis, rezepturName)

            val zutaten = workbook.newWorksheet("Zutatenliste")
            schreibeZutatenliste(zutaten, ergebnis)

            workbook.finish()
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", datei)
    }

    private fun schreibeDeklaration(
        ws: Worksheet,
        ergebnis: NaehrwertErgebnis,
        rezepturName: String
    ) {
        var zeile = 0
        ws.value(zeile, 0, "Nährwertdeklaration")
        ws.style(zeile, 0).bold().set()
        zeile++
        ws.value(zeile, 0, "Rezeptur")
        ws.value(zeile, 1, rezepturName)
        zeile++
        ws.value(zeile, 0, "Gesamtansatz (g)")
        ws.value(zeile, 1, ergebnis.gesamtGewichtGramm)
        zeile++
        ws.value(zeile, 0, "Bezugsgröße")
        ws.value(zeile, 1, "pro 100 g")
        zeile += 2

        ws.value(zeile, 0, "Nährwert")
        ws.value(zeile, 1, "Wert")
        ws.value(zeile, 2, "Einheit")
        ws.range(zeile, 0, zeile, 2).style().bold().set()
        zeile++

        fun reihe(label: String, wert: Double, einheit: String, einrueckung: Boolean = false) {
            ws.value(zeile, 0, if (einrueckung) "  $label" else label)
            ws.value(zeile, 1, wert)
            ws.value(zeile, 2, einheit)
            zeile++
        }

        ws.value(zeile, 0, "Brennwert")
        ws.value(zeile, 1, ergebnis.energieKj)
        ws.value(zeile, 2, "kJ")
        zeile++
        ws.value(zeile, 0, "Brennwert")
        ws.value(zeile, 1, ergebnis.energieKcal)
        ws.value(zeile, 2, "kcal")
        zeile++
        reihe("Fett", ergebnis.fett, "g")
        reihe("davon gesättigte Fettsäuren", ergebnis.gesaettigteFettsaeuren, "g", einrueckung = true)
        reihe("Kohlenhydrate", ergebnis.kohlenhydrate, "g")
        reihe("davon Zucker", ergebnis.zucker, "g", einrueckung = true)
        reihe("Ballaststoffe", ergebnis.ballaststoffe, "g")
        reihe("Eiweiß", ergebnis.eiweiss, "g")
        reihe("Salz", ergebnis.salz, "g")

        if (!ergebnis.istVollstaendig) {
            zeile++
            ws.value(zeile, 0, "ACHTUNG: unvollständige Datenbasis – nicht für die Kennzeichnung verwenden")
            ws.style(zeile, 0).bold().fontColor("FF0000").set()
            zeile++
            ergebnis.fehlendeDaten.forEach { name ->
                ws.value(zeile, 0, "  $name: Nährwertdaten unvollständig")
                zeile++
            }
        }

        ws.width(0, 32.0)
        ws.width(1, 14.0)
        ws.width(2, 10.0)
    }

    private fun schreibeZutatenliste(ws: Worksheet, ergebnis: NaehrwertErgebnis) {
        ws.value(0, 0, "Zutat")
        ws.value(0, 1, "Menge (g)")
        ws.value(0, 2, "Anteil (%)")
        ws.value(0, 3, "Nährwertdaten vollständig")
        ws.range(0, 0, 0, 3).style().bold().set()

        ergebnis.zutatenliste.forEachIndexed { index, zutat ->
            val zeile = index + 1
            ws.value(zeile, 0, zutat.name)
            ws.value(zeile, 1, zutat.gewichtGramm)
            ws.value(zeile, 2, zutat.prozent)
            ws.value(zeile, 3, if (zutat.hatVollstaendigeNaehrwertdaten) "Ja" else "Nein")
        }

        ws.width(0, 32.0)
        ws.width(1, 12.0)
        ws.width(2, 12.0)
        ws.width(3, 22.0)
    }
}
