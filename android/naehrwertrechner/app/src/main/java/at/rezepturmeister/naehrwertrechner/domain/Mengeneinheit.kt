package at.rezepturmeister.naehrwertrechner.domain

/**
 * Einheit, in der eine Zutatenmenge bei der Rezeptureingabe erfasst wurde. Für die
 * Berechnung selbst spielt das keine Rolle mehr, sobald die Menge einmal über die
 * Dichte des Rohstoffs in Gramm umgerechnet ist (NaehrwertBerechnung.ZutatMenge
 * führt dafür sowohl die eingegebene Menge/Einheit als auch den kanonischen
 * Gramm-Wert) – diese Einheit dient nur der Anzeige/Nachvollziehbarkeit.
 */
enum class Mengeneinheit(val kuerzel: String) {
    GRAMM("g"),
    MILLILITER("ml")
}
