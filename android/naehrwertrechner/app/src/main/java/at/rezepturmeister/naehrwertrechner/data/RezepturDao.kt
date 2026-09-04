package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RezepturDao {
    @Query("SELECT * FROM rezepturen ORDER BY name")
    fun alle(): Flow<List<Rezeptur>>

    @Query("SELECT * FROM rezepturen WHERE id = :id")
    suspend fun byId(id: Long): Rezeptur?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun einfuegen(rezeptur: Rezeptur): Long

    @Update
    suspend fun aktualisieren(rezeptur: Rezeptur)

    @Delete
    suspend fun loeschen(rezeptur: Rezeptur)
}

@Dao
interface RezepturZutatDao {
    @Query("SELECT * FROM rezeptur_zutaten WHERE rezepturId = :rezepturId")
    fun fuerRezeptur(rezepturId: Long): Flow<List<RezepturZutat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun einfuegen(zutat: RezepturZutat): Long

    @Delete
    suspend fun loeschen(zutat: RezepturZutat)

    @Query("DELETE FROM rezeptur_zutaten WHERE rezepturId = :rezepturId")
    suspend fun alleFuerRezepturLoeschen(rezepturId: Long)
}
