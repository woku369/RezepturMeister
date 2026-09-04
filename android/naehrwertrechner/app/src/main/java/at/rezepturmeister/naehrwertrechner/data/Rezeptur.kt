package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Eine Produktrezeptur, z. B. "Essiggurken sauer-süß" oder "Senf mittelscharf". */
@Entity(tableName = "rezepturen")
data class Rezeptur(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val version: String = "1.0",
    val notiz: String = ""
)
