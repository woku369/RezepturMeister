package at.rezepturmeister.naehrwertrechner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.ui.NaehrwertViewModel
import at.rezepturmeister.naehrwertrechner.ui.NaehrwertViewModelFactory
import at.rezepturmeister.naehrwertrechner.ui.screens.ErgebnisScreen
import at.rezepturmeister.naehrwertrechner.ui.screens.RezepturEditorScreen
import at.rezepturmeister.naehrwertrechner.ui.screens.RohstoffEditorScreen
import at.rezepturmeister.naehrwertrechner.ui.screens.RohstoffListeScreen
import at.rezepturmeister.naehrwertrechner.ui.theme.NaehrwertRechnerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: NaehrwertViewModel by viewModels {
        NaehrwertViewModelFactory((application as NaehrwertRechnerApp).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NaehrwertRechnerTheme {
                NaehrwertRechnerApp(viewModel)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NaehrwertRechnerApp(viewModel: NaehrwertViewModel) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val titel = listOf("Zutaten", "Rohstoffe", "Ergebnis")

    val rohstoffe by viewModel.rohstoffe.collectAsState()
    val aktuelleZutaten by viewModel.aktuelleZutaten.collectAsState()
    var ergebnis by remember { mutableIntStateOf(0) } // Trigger für Neuberechnung
    val letztesErgebnis = remember(ergebnis, aktuelleZutaten) { viewModel.berechneAktuelleRezeptur() }

    // null = Rohstoffliste anzeigen; sonst Editor für einen neuen (Rohstoff mit id==0
    // als Platzhalter reicht hier nicht, deshalb eigenes Flag) oder bestehenden Rohstoff.
    var rohstoffWirdNeuAngelegt by remember { mutableStateOf(false) }
    var bearbeiteterRohstoff by remember { mutableStateOf<Rohstoff?>(null) }
    val rohstoffFehler by viewModel.rohstoffFehler.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = tabIndex) {
                titel.forEachIndexed { index, titelText ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(titelText) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (tabIndex) {
            0 -> RezepturEditorScreen(
                verfuegbareRohstoffe = rohstoffe,
                aktuelleZutaten = aktuelleZutaten,
                onZutatHinzufuegen = viewModel::zutatHinzufuegen,
                onZutatEntfernen = viewModel::zutatEntfernen,
                onBerechnen = { ergebnis++; tabIndex = 2 },
                onSpeichern = { name -> viewModel.rezepturSpeichern(name) },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> {
                if (rohstoffWirdNeuAngelegt || bearbeiteterRohstoff != null) {
                    RohstoffEditorScreen(
                        bearbeiteterRohstoff = bearbeiteterRohstoff,
                        onSpeichern = { rohstoff ->
                            if (bearbeiteterRohstoff == null) {
                                coroutineScope.launch {
                                    val erfolgreich = viewModel.rohstoffAnlegen(rohstoff)
                                    if (erfolgreich) {
                                        rohstoffWirdNeuAngelegt = false
                                        bearbeiteterRohstoff = null
                                    }
                                    // bei Namenskonflikt bleibt der Editor offen, rohstoffFehler
                                    // (siehe unten) zeigt die Fehlermeldung an
                                }
                            } else {
                                viewModel.rohstoffAktualisieren(rohstoff)
                                rohstoffWirdNeuAngelegt = false
                                bearbeiteterRohstoff = null
                            }
                        },
                        onAbbrechen = {
                            viewModel.rohstoffFehlerGesehen()
                            rohstoffWirdNeuAngelegt = false
                            bearbeiteterRohstoff = null
                        },
                        speicherFehler = rohstoffFehler,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    RohstoffListeScreen(
                        rohstoffe = rohstoffe,
                        onRohstoffAuswaehlen = { bearbeiteterRohstoff = it },
                        onNeuerRohstoff = { rohstoffWirdNeuAngelegt = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            2 -> ErgebnisScreen(ergebnis = letztesErgebnis, modifier = Modifier.padding(innerPadding))
        }
    }
}
