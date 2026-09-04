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

    @Query("SELECT id FROM rohstoffe WHERE name = :name LIMIT 1")
    abstract suspend fun findeIdNachName(name: String): Long?

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
     * WICHTIG: Es gibt aktuell keine Bearbeiten-Ansicht für Rohstoffe in der App –
     * SeedData gilt daher als "Wahrheit" und wird bei jedem Start vollständig
     * durchgesetzt. Sobald eine manuelle Bearbeitung von Rohstoffen eingeführt wird,
     * MUSS diese Funktion angepasst werden (z. B. per Flag "vomNutzerBearbeitet"),
     * damit sie vom Nutzer korrigierte Werte nicht bei jedem Start wieder überschreibt.
     */
    suspend fun syncSeedDaten(seedListe: List<Rohstoff>) {
        seedListe.forEach { rohstoff ->
            val vorhandeneId = findeIdNachName(rohstoff.name)
            if (vorhandeneId != null) {
                aktualisieren(rohstoff.copy(id = vorhandeneId))
            } else {
                einfuegen(rohstoff)
            }
        }
    }
}
