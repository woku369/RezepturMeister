@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package at.rezepturmeister.naehrwertrechner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.domain.Bezugsgroesse
import at.rezepturmeister.naehrwertrechner.domain.Mengeneinheit
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung

@Composable
fun RezepturEditorScreen(
    verfuegbareRohstoffe: List<Rohstoff>,
    aktuelleZutaten: List<NaehrwertBerechnung.ZutatMenge>,
    onZutatHinzufuegen: (NaehrwertBerechnung.ZutatMenge) -> Unit,
    onZutatEntfernen: (Int) -> Unit,
    bezugsgroesse: Bezugsgroesse,
    onBezugsgroesseChange: (Bezugsgroesse) -> Unit,
    gesamtvolumenMlText: String,
    onGesamtvolumenMlChange: (String) -> Unit,
    onBerechnen: () -> Unit,
    onSpeichern: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ausgewaehlterRohstoff by remember { mutableStateOf<Rohstoff?>(null) }
    var mengeText by remember { mutableStateOf("") }
    var einheit by remember { mutableStateOf(Mengeneinheit.GRAMM) }
    var dropdownOffen by remember { mutableStateOf(false) }
    var rezepturName by remember { mutableStateOf("") }

    val dichteFehlt = einheit == Mengeneinheit.MILLILITER && ausgewaehlterRohstoff?.dichte == null
    val gesamtvolumenMl = gesamtvolumenMlText.replace(",", ".").toDoubleOrNull()
    val volumenFehlt = bezugsgroesse == Bezugsgroesse.PRO_100_ML && (gesamtvolumenMl == null || gesamtvolumenMl <= 0.0)

    Column(modifier.padding(12.dp)) {
        Text("Zutat hinzufügen", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(expanded = dropdownOffen, onExpandedChange = { dropdownOffen = it }) {
            OutlinedTextField(
                value = ausgewaehlterRohstoff?.name ?: "Rohstoff wählen",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOffen) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            DropdownMenu(expanded = dropdownOffen, onDismissRequest = { dropdownOffen = false }) {
                verfuegbareRohstoffe.forEach { rohstoff ->
                    DropdownMenuItem(
                        text = { Text(rohstoff.name) },
                        onClick = {
                            ausgewaehlterRohstoff = rohstoff
                            dropdownOffen = false
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = mengeText,
                onValueChange = { mengeText = it },
                label = { Text("Menge (${einheit.kuerzel})") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Mengeneinheit.entries.forEach { option ->
                if (einheit == option) {
                    Button(onClick = { einheit = option }) { Text(option.kuerzel) }
                } else {
                    OutlinedButton(onClick = { einheit = option }) { Text(option.kuerzel) }
                }
            }
        }
        if (dichteFehlt) {
            Text(
                "Dichte für \"${ausgewaehlterRohstoff?.name}\" unbekannt – bitte in Gramm eingeben " +
                    "oder im Rohstoff-Editor eine Dichte (g/ml) hinterlegen.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                val rohstoff = ausgewaehlterRohstoff
                val eingegebeneMenge = mengeText.replace(",", ".").toDoubleOrNull()
                val dichte = rohstoff?.dichte
                val mengeGramm = when {
                    rohstoff == null || eingegebeneMenge == null || eingegebeneMenge <= 0.0 -> null
                    einheit == Mengeneinheit.GRAMM -> eingegebeneMenge
                    dichte != null -> eingegebeneMenge * dichte
                    else -> null
                }
                if (rohstoff != null && eingegebeneMenge != null && mengeGramm != null) {
                    onZutatHinzufuegen(
                        NaehrwertBerechnung.ZutatMenge(
                            rohstoff = rohstoff,
                            mengeGramm = mengeGramm,
                            eingegebeneMenge = eingegebeneMenge,
                            eingegebeneEinheit = einheit
                        )
                    )
                    mengeText = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Hinzufügen")
        }

        Text(
            "Zutatenliste",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(aktuelleZutaten.size) { index ->
                val zutat = aktuelleZutaten[index]
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val mengeAnzeige = if (zutat.eingegebeneEinheit == Mengeneinheit.MILLILITER) {
                        "${zutat.eingegebeneMenge} ml (≈ ${"%.1f".format(zutat.mengeGramm)} g)"
                    } else {
                        "${zutat.mengeGramm} g"
                    }
                    Text("${zutat.rohstoff.name}: $mengeAnzeige")
                    TextButton(onClick = { onZutatEntfernen(index) }) { Text("Entfernen") }
                }
            }
        }

        Text(
            "Bezugsgröße der Deklaration",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bezugsgroesse.entries.forEach { option ->
                if (bezugsgroesse == option) {
                    Button(onClick = { onBezugsgroesseChange(option) }) { Text(option.anzeigename) }
                } else {
                    OutlinedButton(onClick = { onBezugsgroesseChange(option) }) { Text(option.anzeigename) }
                }
            }
        }
        if (bezugsgroesse == Bezugsgroesse.PRO_100_ML) {
            OutlinedTextField(
                value = gesamtvolumenMlText,
                onValueChange = onGesamtvolumenMlChange,
                label = { Text("Gesamtvolumen des fertigen Ansatzes (ml, gemessen)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Text(
                "Bewusst eine Eingabe, keine Summe der Zutatenmengen: Alkohol und Wasser " +
                    "mischen sich nicht additiv, das tatsächliche Volumen muss am fertigen " +
                    "Ansatz gemessen werden.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBerechnen, enabled = aktuelleZutaten.isNotEmpty() && !volumenFehlt) {
                Text("Nährwert berechnen")
            }
        }

        OutlinedTextField(
            value = rezepturName,
            onValueChange = { rezepturName = it },
            label = { Text("Rezepturname zum Speichern") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        Button(
            onClick = { if (rezepturName.isNotBlank()) onSpeichern(rezepturName) },
            enabled = aktuelleZutaten.isNotEmpty() && rezepturName.isNotBlank(),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Rezeptur speichern")
        }
    }
}
