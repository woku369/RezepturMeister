package at.rezepturmeister.naehrwertrechner.export

import android.content.Context
import androidx.core.content.FileProvider
import android.net.Uri
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertErgebnis
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportiert ein Berechnungsergebnis als XLSX-Datei mit drei Tabellenblättern:
 * Nährwertdeklaration (Anhang-XV-Reihenfolge), Berechnungsschlüssel (transparente
 * Herleitung des Brennwerts nach Anhang XIV, Faktor × Menge = Beitrag – für eine
 * Behördenprüfung nachvollziehbar aufbereitet) und Zutatenliste.
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

            val berechnung = workbook.newWorksheet("Berechnungsschlüssel")
            schreibeBerechnungsschluessel(berechnung, ergebnis, rezepturName)

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
        ergebnis.gesamtvolumenMl?.let {
            ws.value(zeile, 0, "Gesamtvolumen (ml, gemessen)")
            ws.value(zeile, 1, it)
            zeile++
        }
        ws.value(zeile, 0, "Bezugsgröße")
        ws.value(zeile, 1, ergebnis.bezugsgroesse.anzeigename)
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

        zeile++
        ws.value(zeile, 0, "Berechnungsgrundlage: siehe Tabellenblatt \"Berechnungsschlüssel\"")
        zeile++

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

        if (ergebnis.alsVernachlaessigbarAkzeptiert.isNotEmpty()) {
            zeile++
            ws.value(zeile, 0, "Unvollständige Daten, bewusst als vernachlässigbar akzeptiert:")
            zeile++
            ergebnis.alsVernachlaessigbarAkzeptiert.forEach { name ->
                ws.value(zeile, 0, "  $name")
                zeile++
            }
        }

        ws.width(0, 40.0)
        ws.width(1, 14.0)
        ws.width(2, 10.0)
    }

    /**
     * Legt die Brennwert-Herleitung offen: Summe je Nährstoff, amtlicher
     * Umrechnungsfaktor (Anhang XIV VO (EU) 1169/2011), Beitrag in kJ/kcal, Summe.
     * Format bewusst an die Darstellung in amtlichen Prüfbescheiden angelehnt
     * (Faktor × Menge = Beitrag, dann Summenzeile), damit eine Behörde die
     * Berechnung ohne Rückfrage nachvollziehen kann.
     */
    private fun schreibeBerechnungsschluessel(
        ws: Worksheet,
        ergebnis: NaehrwertErgebnis,
        rezepturName: String
    ) {
        var zeile = 0
        ws.value(zeile, 0, "Berechnung des Brennwerts nach Anhang XIV VO (EU) Nr. 1169/2011")
        ws.style(zeile, 0).bold().set()
        zeile++
        ws.value(zeile, 0, "Rezeptur")
        ws.value(zeile, 1, rezepturName)
        zeile++
        ws.value(zeile, 0, "Bezugsgröße")
        ws.value(zeile, 1, ergebnis.bezugsgroesse.anzeigename)
        zeile += 2

        val kopfzeile = zeile
        ws.value(zeile, 0, "Nährstoff")
        ws.value(zeile, 1, "Menge (g)")
        ws.value(zeile, 2, "Faktor (kJ/g)")
        ws.value(zeile, 3, "Faktor (kcal/g)")
        ws.value(zeile, 4, "Beitrag (kJ)")
        ws.value(zeile, 5, "Beitrag (kcal)")
        ws.range(kopfzeile, 0, kopfzeile, 5).style().bold().set()
        zeile++

        data class Zeile(val nährstoff: String, val menge: Double, val kjProG: Double, val kcalProG: Double)

        val zeilen = listOf(
            Zeile("Fett", ergebnis.fett, NaehrwertBerechnung.KJ_PRO_G_FETT, NaehrwertBerechnung.KCAL_PRO_G_FETT),
            Zeile("Kohlenhydrate", ergebnis.kohlenhydrate, NaehrwertBerechnung.KJ_PRO_G_KOHLENHYDRATE, NaehrwertBerechnung.KCAL_PRO_G_KOHLENHYDRATE),
            Zeile("Eiweiß", ergebnis.eiweiss, NaehrwertBerechnung.KJ_PRO_G_EIWEISS, NaehrwertBerechnung.KCAL_PRO_G_EIWEISS),
            Zeile("Ballaststoffe", ergebnis.ballaststoffe, NaehrwertBerechnung.KJ_PRO_G_BALLASTSTOFFE, NaehrwertBerechnung.KCAL_PRO_G_BALLASTSTOFFE),
            Zeile("Alkohol", ergebnis.alkohol, NaehrwertBerechnung.KJ_PRO_G_ALKOHOL, NaehrwertBerechnung.KCAL_PRO_G_ALKOHOL),
            Zeile("Organische Säuren", ergebnis.organischeSaeuren, NaehrwertBerechnung.KJ_PRO_G_ORGANISCHE_SAEUREN, NaehrwertBerechnung.KCAL_PRO_G_ORGANISCHE_SAEUREN)
        )

        zeilen.forEach { z ->
            ws.value(zeile, 0, z.nährstoff)
            ws.value(zeile, 1, z.menge)
            ws.value(zeile, 2, z.kjProG)
            ws.value(zeile, 3, z.kcalProG)
            ws.value(zeile, 4, z.menge * z.kjProG)
            ws.value(zeile, 5, z.menge * z.kcalProG)
            zeile++
        }

        ws.value(zeile, 0, "Summe = Brennwert")
        ws.style(zeile, 0).bold().set()
        ws.value(zeile, 4, ergebnis.energieKj)
        ws.value(zeile, 5, ergebnis.energieKcal)
        ws.range(zeile, 4, zeile, 5).style().bold().set()
        zeile += 2

        ws.value(zeile, 0, "Hinweis: Salz ist keine Eingangsgröße der Energieformel (Anhang XIV kennt " +
            "keinen Energiefaktor für Salz) und daher hier nicht aufgeführt, wird aber in der " +
            "Nährwertdeklaration gesondert ausgewiesen.")
        zeile++
        ws.value(zeile, 0, "Zucker und gesättigte Fettsäuren sind bereits in \"Kohlenhydrate\" bzw. " +
            "\"Fett\" enthalten (Unterkategorien ohne eigenen Energiefaktor).")
        zeile++
        ws.value(zeile, 0, "Berechnungsmethode: Art. 31 LMIV – Berechnung anhand bekannter/" +
            "durchschnittlicher Werte der verwendeten Zutaten (keine Laboranalyse des Enderzeugnisses).")

        if (!ergebnis.istVollstaendig) {
            zeile += 2
            ws.value(zeile, 0, "ACHTUNG: mindestens eine Zutat hat unvollständige Nährwertdaten – " +
                "die obigen Summen und damit der Brennwert unterschätzen den tatsächlichen Wert " +
                "entsprechend. Details siehe Tabellenblatt \"Nährwertdeklaration\".")
            ws.style(zeile, 0).bold().fontColor("FF0000").set()
        }

        ws.width(0, 40.0)
        ws.width(1, 12.0)
        ws.width(2, 13.0)
        ws.width(3, 14.0)
        ws.width(4, 13.0)
        ws.width(5, 13.0)
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
            val status = when {
                zutat.hatVollstaendigeNaehrwertdaten -> "Ja"
                zutat.alsVernachlaessigbarMarkiert -> "Nein (als vernachlässigbar akzeptiert)"
                else -> "Nein"
            }
            ws.value(zeile, 3, status)
        }

        ws.width(0, 32.0)
        ws.width(1, 12.0)
        ws.width(2, 12.0)
        ws.width(3, 34.0)
    }
}
