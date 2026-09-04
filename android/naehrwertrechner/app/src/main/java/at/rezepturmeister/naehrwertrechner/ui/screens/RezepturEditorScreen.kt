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
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung

@Composable
fun RezepturEditorScreen(
    verfuegbareRohstoffe: List<Rohstoff>,
    aktuelleZutaten: List<NaehrwertBerechnung.ZutatMenge>,
    onZutatHinzufuegen: (Rohstoff, Double) -> Unit,
    onZutatEntfernen: (Int) -> Unit,
    onBerechnen: () -> Unit,
    onSpeichern: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ausgewaehlterRohstoff by remember { mutableStateOf<Rohstoff?>(null) }
    var mengeText by remember { mutableStateOf("") }
    var dropdownOffen by remember { mutableStateOf(false) }
    var rezepturName by remember { mutableStateOf("") }

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

        OutlinedTextField(
            value = mengeText,
            onValueChange = { mengeText = it },
            label = { Text("Menge (g)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Button(
            onClick = {
                val rohstoff = ausgewaehlterRohstoff
                val menge = mengeText.replace(",", ".").toDoubleOrNull()
                if (rohstoff != null && menge != null && menge > 0) {
                    onZutatHinzufuegen(rohstoff, menge)
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
                    Text("${zutat.rohstoff.name}: ${zutat.mengeGramm} g")
                    TextButton(onClick = { onZutatEntfernen(index) }) { Text("Entfernen") }
                }
            }
        }

        Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBerechnen, enabled = aktuelleZutaten.isNotEmpty()) {
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
