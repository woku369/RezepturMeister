package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Verknüpft eine Rezeptur mit einem Rohstoff samt eingesetzter Menge in Gramm. */
@Entity(
    tableName = "rezeptur_zutaten",
    foreignKeys = [
        ForeignKey(entity = Rezeptur::class, parentColumns = ["id"], childColumns = ["rezepturId"]),
        ForeignKey(entity = Rohstoff::class, parentColumns = ["id"], childColumns = ["rohstoffId"])
    ]
)
data class RezepturZutat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rezepturId: Long,
    val rohstoffId: Long,
    val mengeGramm: Double
)
