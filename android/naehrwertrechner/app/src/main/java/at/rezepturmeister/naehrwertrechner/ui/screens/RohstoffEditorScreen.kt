@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package at.rezepturmeister.naehrwertrechner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.rezepturmeister.naehrwertrechner.data.NaehrwertQuelle
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.ocr.EtikettErgebnis
import at.rezepturmeister.naehrwertrechner.ocr.EtikettParser
import at.rezepturmeister.naehrwertrechner.ocr.EtikettScanner
import kotlinx.coroutines.launch

/**
 * Anlegen/Bearbeiten eines einzelnen Rohstoffs, inkl. optionaler Vorbefüllung der
 * Nährwertfelder per Foto des Zutatenetiketts (OCR). Die OCR-Erkennung liefert IMMER
 * nur einen Vorschlag in die (weiterhin editierbaren) Textfelder – nichts wird
 * ungeprüft übernommen, erst der explizite "Speichern"-Klick schreibt den Rohstoff.
 */
@Composable
fun RohstoffEditorScreen(
    bearbeiteterRohstoff: Rohstoff?,
    onSpeichern: (Rohstoff) -> Unit,
    onAbbrechen: () -> Unit,
    speicherFehler: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(bearbeiteterRohstoff?.name ?: "") }
    var kategorie by remember { mutableStateOf(bearbeiteterRohstoff?.kategorie ?: "") }
    var energieKj by remember { mutableStateOf(bearbeiteterRohstoff?.energieKj?.toString() ?: "") }
    var energieKcal by remember { mutableStateOf(bearbeiteterRohstoff?.energieKcal?.toString() ?: "") }
    var fett by remember { mutableStateOf(bearbeiteterRohstoff?.fett?.toString() ?: "") }
    var gesFett by remember { mutableStateOf(bearbeiteterRohstoff?.gesaettigteFettsaeuren?.toString() ?: "") }
    var kohlenhydrate by remember { mutableStateOf(bearbeiteterRohstoff?.kohlenhydrate?.toString() ?: "") }
    var zucker by remember { mutableStateOf(bearbeiteterRohstoff?.zucker?.toString() ?: "") }
    var ballaststoffe by remember { mutableStateOf(bearbeiteterRohstoff?.ballaststoffe?.toString() ?: "") }
    var eiweiss by remember { mutableStateOf(bearbeiteterRohstoff?.eiweiss?.toString() ?: "") }
    var salz by remember { mutableStateOf(bearbeiteterRohstoff?.salz?.toString() ?: "") }
    var quelle by remember { mutableStateOf(bearbeiteterRohstoff?.quelle ?: NaehrwertQuelle.UNBEKANNT) }
    var quelleHinweis by remember { mutableStateOf(bearbeiteterRohstoff?.quelleHinweis ?: "") }
    var quelleDropdownOffen by remember { mutableStateOf(false) }

    var ocrLaeuft by remember { mutableStateOf(false) }
    var ocrErgebnis by remember { mutableStateOf<EtikettErgebnis?>(null) }
    var ocrFehler by remember { mutableStateOf<String?>(null) }
    var kameraUri by remember { mutableStateOf<Uri?>(null) }

    fun uebernehmeOcr(rohtext: String) {
        val ergebnis = EtikettParser.parse(rohtext)
        ocrErgebnis = ergebnis
        ergebnis.energieKj?.let { energieKj = it.toString() }
        ergebnis.energieKcal?.let { energieKcal = it.toString() }
        ergebnis.fett?.let { fett = it.toString() }
        ergebnis.gesaettigteFettsaeuren?.let { gesFett = it.toString() }
        ergebnis.kohlenhydrate?.let { kohlenhydrate = it.toString() }
        ergebnis.zucker?.let { zucker = it.toString() }
        ergebnis.ballaststoffe?.let { ballaststoffe = it.toString() }
        ergebnis.eiweiss?.let { eiweiss = it.toString() }
        ergebnis.salz?.let { salz = it.toString() }
        if (quelle == NaehrwertQuelle.UNBEKANNT) quelle = NaehrwertQuelle.HERSTELLERETIKETT
        if (quelleHinweis.isBlank()) {
            quelleHinweis = "Per Foto-Etikett-Erkennung (OCR) vorausgefüllt, ${ergebnis.anzahlErkannt()}/9 " +
                "Werte erkannt – gegen das Etikett geprüft am: ______"
        }
    }

    fun starteOcr(uri: Uri) {
        ocrLaeuft = true
        ocrFehler = null
        scope.launch {
            try {
                val rohtext = EtikettScanner.texterkennung(context, uri)
                uebernehmeOcr(rohtext)
            } catch (e: Exception) {
                ocrFehler = "Texterkennung fehlgeschlagen: ${e.message}"
            } finally {
                ocrLaeuft = false
            }
        }
    }

    val galerieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) starteOcr(uri)
    }
    val kameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { erfolgreich ->
        val uri = kameraUri
        if (erfolgreich && uri != null) starteOcr(uri)
    }
    val kameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { erlaubt ->
        if (erlaubt) {
            val uri = EtikettScanner.neueFotoUri(context)
            kameraUri = uri
            kameraLauncher.launch(uri)
        } else {
            ocrFehler = "Kamera-Berechtigung wurde nicht erteilt."
        }
    }

    fun starteKamera() {
        val hatBerechtigung = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (hatBerechtigung) {
            val uri = EtikettScanner.neueFotoUri(context)
            kameraUri = uri
            kameraLauncher.launch(uri)
        } else {
            kameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun zahl(text: String): Double? = text.replace(",", ".").toDoubleOrNull()

    LazyColumn(modifier = modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                if (bearbeiteterRohstoff == null) "Neuen Rohstoff anlegen" else "Rohstoff bearbeiten",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Column {
                Text("Foto vom Zutatenetikett (optional)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Liest die Nährwerttabelle per Texterkennung aus und füllt die Felder unten vor – " +
                        "bitte danach jeden Wert gegen das Etikett prüfen, bevor Sie speichern.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedButton(onClick = { starteKamera() }, enabled = !ocrLaeuft) { Text("Foto aufnehmen") }
                    OutlinedButton(onClick = { galerieLauncher.launch("image/*") }, enabled = !ocrLaeuft) {
                        Text("Aus Galerie wählen")
                    }
                }
                if (ocrLaeuft) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        Text("Texterkennung läuft …", style = MaterialTheme.typography.bodySmall)
                    }
                }
                ocrFehler?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                ocrErgebnis?.let {
                    Text(
                        "${it.anzahlErkannt()}/9 Werte erkannt. Erkannter Rohtext (zur Kontrolle): \"${it.erkannterRohtext}\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = kategorie, onValueChange = { kategorie = it },
                label = { Text("Kategorie") }, modifier = Modifier.fillMaxWidth()
            )
        }

        item { Text("Nährwerte pro 100 g", style = MaterialTheme.typography.titleMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = energieKj, onValueChange = { energieKj = it },
                    label = { Text("Brennwert (kJ)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = energieKcal, onValueChange = { energieKcal = it },
                    label = { Text("Brennwert (kcal)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fett, onValueChange = { fett = it },
                    label = { Text("Fett (g)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = gesFett, onValueChange = { gesFett = it },
                    label = { Text("davon gesättigt (g)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = kohlenhydrate, onValueChange = { kohlenhydrate = it },
                    label = { Text("Kohlenhydrate (g)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = zucker, onValueChange = { zucker = it },
                    label = { Text("davon Zucker (g)") }, modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
        item {
            OutlinedTextField(
                value = ballaststoffe, onValueChange = { ballaststoffe = it },
                label = { Text("Ballaststoffe (g)") }, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = eiweiss, onValueChange = { eiweiss = it },
                label = { Text("Eiweiß (g)") }, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = salz, onValueChange = { salz = it },
                label = { Text("Salz (g)") }, modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ExposedDropdownMenuBox(expanded = quelleDropdownOffen, onExpandedChange = { quelleDropdownOffen = it }) {
                OutlinedTextField(
                    value = quelle.anzeigename,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Quelle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quelleDropdownOffen) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = quelleDropdownOffen, onDismissRequest = { quelleDropdownOffen = false }) {
                    NaehrwertQuelle.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.anzeigename) },
                            onClick = { quelle = option; quelleDropdownOffen = false }
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = quelleHinweis, onValueChange = { quelleHinweis = it },
                label = { Text("Quellenhinweis (Produkt, Datum, Link, Charge)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        speicherFehler?.let { fehler ->
            item {
                Text(fehler, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(
                    onClick = {
                        onSpeichern(
                            (bearbeiteterRohstoff ?: Rohstoff(name = name)).copy(
                                name = name,
                                kategorie = kategorie,
                                energieKj = zahl(energieKj),
                                energieKcal = zahl(energieKcal),
                                fett = zahl(fett),
                                gesaettigteFettsaeuren = zahl(gesFett),
                                kohlenhydrate = zahl(kohlenhydrate),
                                zucker = zahl(zucker),
                                ballaststoffe = zahl(ballaststoffe),
                                eiweiss = zahl(eiweiss),
                                salz = zahl(salz),
                                quelle = quelle,
                                quelleHinweis = quelleHinweis
                            )
                        )
                    },
                    enabled = name.isNotBlank()
                ) { Text("Speichern") }
                TextButton(onClick = onAbbrechen) { Text("Abbrechen") }
            }
        }
    }
}
