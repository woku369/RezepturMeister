package at.rezepturmeister.naehrwertrechner.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertErgebnis
import at.rezepturmeister.naehrwertrechner.export.XlsxExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ErgebnisScreen(ergebnis: NaehrwertErgebnis?, rezepturName: String = "Rezeptur", modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize().padding(16.dp)) {
        if (ergebnis == null) {
            Text("Noch keine Berechnung durchgeführt.")
            return@Column
        }

        Text("Nährwertdeklaration (pro 100 g)", style = MaterialTheme.typography.titleLarge)
        Text(
            "Gesamtansatz: ${fmt(ergebnis.gesamtGewichtGramm, 0)} g",
            style = MaterialTheme.typography.bodySmall
        )
        Divider(Modifier.padding(vertical = 8.dp))

        Zeile("Brennwert", "${fmt(ergebnis.energieKj, 0)} kJ / ${fmt(ergebnis.energieKcal, 0)} kcal")
        Zeile("Fett", "${fmt(ergebnis.fett)} g")
        Zeile("  davon gesättigte Fettsäuren", "${fmt(ergebnis.gesaettigteFettsaeuren)} g", einrueckung = true)
        Zeile("Kohlenhydrate", "${fmt(ergebnis.kohlenhydrate)} g")
        Zeile("  davon Zucker", "${fmt(ergebnis.zucker)} g", einrueckung = true)
        Zeile("Ballaststoffe", "${fmt(ergebnis.ballaststoffe)} g")
        Zeile("Eiweiß", "${fmt(ergebnis.eiweiss)} g")
        Zeile("Salz", "${fmt(ergebnis.salz)} g")

        Button(
            onClick = {
                scope.launch {
                    try {
                        val uri = withContext(Dispatchers.IO) {
                            XlsxExporter.exportiere(context, ergebnis, rezepturName)
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Nährwertdeklaration teilen"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Als Excel (.xlsx) exportieren")
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("Zutatenliste (für Kennzeichnung, absteigend sortiert)", style = MaterialTheme.typography.titleMedium)
        ergebnis.zutatenliste.forEach { zutat ->
            Text(
                "${zutat.name} – ${fmt(zutat.prozent)} % " +
                    if (!zutat.hatVollstaendigeNaehrwertdaten) "⚠ Nährwertdaten unvollständig" else "",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!ergebnis.istVollstaendig) {
            Divider(Modifier.padding(vertical = 8.dp))
            Text(
                "Diese Berechnung ist NICHT vollständig und darf nicht für eine amtliche " +
                    "Nährwertdeklaration verwendet werden. Fehlende Rohstoffdaten:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            ergebnis.fehlendeDaten.forEach {
                Text("- $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun fmt(wert: Double, nachkomma: Int = 1): String =
    String.format(Locale.GERMANY, "%.${nachkomma}f", wert)

@Composable
private fun Zeile(label: String, wert: String, einrueckung: Boolean = false) {
    Text(
        "$label: $wert",
        style = if (einrueckung) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge
    )
}
