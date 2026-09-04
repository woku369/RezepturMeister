package at.rezepturmeister.naehrwertrechner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.rezepturmeister.naehrwertrechner.data.AppDatabase
import at.rezepturmeister.naehrwertrechner.data.Rezeptur
import at.rezepturmeister.naehrwertrechner.data.RezepturZutat
import at.rezepturmeister.naehrwertrechner.data.Rohstoff
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

    fun zutatHinzufuegen(rohstoff: Rohstoff, mengeGramm: Double) {
        _aktuelleZutaten.value = _aktuelleZutaten.value + NaehrwertBerechnung.ZutatMenge(rohstoff, mengeGramm)
    }

    fun zutatEntfernen(index: Int) {
        _aktuelleZutaten.value = _aktuelleZutaten.value.filterIndexed { i, _ -> i != index }
    }

    fun berechneAktuelleRezeptur(): NaehrwertErgebnis? =
        _aktuelleZutaten.value.takeIf { it.isNotEmpty() }?.let { NaehrwertBerechnung.berechne(it) }

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

    fun rohstoffSpeichern(rohstoff: Rohstoff) {
        viewModelScope.launch { db.rohstoffDao().einfuegen(rohstoff) }
    }
}
