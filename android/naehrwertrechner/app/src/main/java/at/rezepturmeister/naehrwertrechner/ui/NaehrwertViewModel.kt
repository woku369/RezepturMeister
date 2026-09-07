package at.rezepturmeister.naehrwertrechner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.rezepturmeister.naehrwertrechner.data.AppDatabase
import at.rezepturmeister.naehrwertrechner.data.Rezeptur
import at.rezepturmeister.naehrwertrechner.data.RezepturZutat
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
import at.rezepturmeister.naehrwertrechner.domain.Bezugsgroesse
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertBerechnung
import at.rezepturmeister.naehrwertrechner.domain.NaehrwertErgebnis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NaehrwertViewModel(private val db: AppDatabase) : ViewModel() {

    val rohstoffe: StateFlow<List<Rohstoff>> =
        db.rohstoffDao().alle().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rezepturen: StateFlow<List<Rezeptur>> =
        db.rezepturDao().alle().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aktuelleZutaten = MutableStateFlow<List<NaehrwertBerechnung.ZutatMenge>>(emptyList())
    val aktuelleZutaten: StateFlow<List<NaehrwertBerechnung.ZutatMenge>> = _aktuelleZutaten

    private val _rohstoffFehler = MutableStateFlow<String?>(null)
    val rohstoffFehler: StateFlow<String?> = _rohstoffFehler

    fun rohstoffFehlerGesehen() {
        _rohstoffFehler.value = null
    }

    fun zutatHinzufuegen(zutat: NaehrwertBerechnung.ZutatMenge) {
        _aktuelleZutaten.value = _aktuelleZutaten.value + zutat
    }

    fun zutatEntfernen(index: Int) {
        _aktuelleZutaten.value = _aktuelleZutaten.value.filterIndexed { i, _ -> i != index }
    }

    /**
     * Bei Bezugsgröße PRO_100_ML muss [gesamtvolumenMl] gesetzt sein (gemessenes
     * Gesamtvolumen, siehe NaehrwertBerechnung.berechne()) – fehlt es, wird hier
     * bewusst null zurückgegeben statt NaehrwertBerechnung.berechne() aufzurufen,
     * dessen require() sonst eine Exception werfen würde (die UI ruft diese Funktion
     * reaktiv bei jeder Zustandsänderung auf, auch während der Nutzer das
     * Gesamtvolumen noch eingibt).
     */
    fun berechneAktuelleRezeptur(
        bezugsgroesse: Bezugsgroesse = Bezugsgroesse.PRO_100_G,
        gesamtvolumenMl: Double? = null
    ): NaehrwertErgebnis? {
        val zutaten = _aktuelleZutaten.value.takeIf { it.isNotEmpty() } ?: return null
        if (bezugsgroesse == Bezugsgroesse.PRO_100_ML && (gesamtvolumenMl == null || gesamtvolumenMl <= 0.0)) {
            return null
        }
        return NaehrwertBerechnung.berechne(zutaten, bezugsgroesse, gesamtvolumenMl)
    }

    fun rezepturSpeichern(name: String, version: String = "1.0") {
        val zutaten = _aktuelleZutaten.value
        if (zutaten.isEmpty()) return
        viewModelScope.launch {
            val rezepturId = db.rezepturDao().einfuegen(Rezeptur(name = name, version = version))
            zutaten.forEach { (rohstoff, menge) ->
                db.rezepturZutatDao().einfuegen(
                    RezepturZutat(rezepturId = rezepturId, rohstoffId = rohstoff.id, mengeGramm = menge)
                )
            }
        }
    }

    /**
     * Legt einen neuen, manuell (ggf. per Foto-Etikett-Erkennung) erfassten Rohstoff an.
     * Gibt zurück, ob das Anlegen erfolgreich war – der Aufrufer soll den Editor nur bei
     * Erfolg schließen, sonst bleibt er offen und zeigt rohstoffFehler an.
     *
     * Prüft vorher per Name, ob bereits ein Rohstoff existiert: einfuegen() nutzt
     * OnConflictStrategy.REPLACE auf dem eindeutigen Name-Index, was bei einem
     * Namenskonflikt die bestehende Zeile löschen und mit neuer Id wieder einfügen würde
     * – das würde alle RezepturZutat-Verknüpfungen auf den alten Rohstoff verwaisen
     * lassen. Bei einem Namenskonflikt daher kein Insert, sondern Fehlermeldung an die UI.
     */
    suspend fun rohstoffAnlegen(rohstoff: Rohstoff): Boolean {
        if (db.rohstoffDao().findeNachName(rohstoff.name) != null) {
            _rohstoffFehler.value = "Ein Rohstoff namens \"${rohstoff.name}\" existiert bereits – " +
                "bitte über die Liste bearbeiten statt neu anlegen, oder einen anderen Namen wählen."
            return false
        }
        db.rohstoffDao().einfuegen(rohstoff.copy(id = 0, vomNutzerBearbeitet = true))
        return true
    }

    /**
     * Aktualisiert einen bestehenden Rohstoff (Id bleibt erhalten, damit vorhandene
     * RezepturZutat-Verknüpfungen gültig bleiben) und markiert ihn als vom Nutzer
     * bearbeitet, damit SeedData.syncSeedDaten() ihn beim nächsten Start nicht überschreibt.
     */
    fun rohstoffAktualisieren(rohstoff: Rohstoff) {
        viewModelScope.launch {
            db.rohstoffDao().aktualisieren(rohstoff.copy(vomNutzerBearbeitet = true))
        }
    }
}
