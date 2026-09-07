package at.rezepturmeister.naehrwertrechner.domain

/**
 * Bezugsgröße der Nährwertdeklaration (Art. 32 LMIV: pro 100 g ODER pro 100 ml,
 * je nachdem was für das Lebensmittel "üblich" ist – bei Flüssigkeiten/Getränken
 * ist das praktisch immer pro 100 ml).
 */
enum class Bezugsgroesse(val anzeigename: String) {
    PRO_100_G("pro 100 g"),
    PRO_100_ML("pro 100 ml")
}
