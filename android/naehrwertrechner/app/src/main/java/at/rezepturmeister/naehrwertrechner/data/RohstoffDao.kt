package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RohstoffDao {
    @Query("SELECT * FROM rohstoffe ORDER BY name")
    abstract fun alle(): Flow<List<Rohstoff>>

    @Query("SELECT * FROM rohstoffe WHERE id = :id")
    abstract suspend fun byId(id: Long): Rohstoff?

    @Query("SELECT * FROM rohstoffe WHERE name = :name LIMIT 1")
    abstract suspend fun findeNachName(name: String): Rohstoff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun einfuegen(rohstoff: Rohstoff): Long

    @Update
    abstract suspend fun aktualisieren(rohstoff: Rohstoff)

    @Delete
    abstract suspend fun loeschen(rohstoff: Rohstoff)

    @Query("SELECT COUNT(*) FROM rohstoffe")
    abstract suspend fun anzahl(): Int

    /**
     * Gleicht die App-eigene Start-Rohstoffliste (SeedData) mit der lokalen DB ab:
     * Ein Rohstoff mit bereits vorhandenem Namen wird auf den aktuellen SeedData-Stand
     * aktualisiert (Id bleibt erhalten, damit bestehende RezepturZutat-Verknüpfungen
     * gültig bleiben), ein neuer Name wird eingefügt.
     *
     * Ausnahme: Wurde der vorhandene Datensatz bereits über den Rohstoff-Editor manuell
     * bearbeitet (vomNutzerBearbeitet = true), wird er NICHT angetastet – sonst würde
     * jede eigene Korrektur/Ergänzung (auch die per Foto-Etikett-Erkennung erfassten
     * Werte) beim nächsten App-Start wieder durch den SeedData-Wert überschrieben.
     */
    suspend fun syncSeedDaten(seedListe: List<Rohstoff>) {
        seedListe.forEach { rohstoff ->
            val vorhandener = findeNachName(rohstoff.name)
            when {
                vorhandener == null -> einfuegen(rohstoff)
                vorhandener.vomNutzerBearbeitet -> Unit
                else -> aktualisieren(rohstoff.copy(id = vorhandener.id))
            }
        }
    }
}
