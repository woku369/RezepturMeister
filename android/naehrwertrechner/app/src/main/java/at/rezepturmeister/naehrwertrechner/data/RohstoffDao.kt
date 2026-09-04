package at.rezepturmeister.naehrwertrechner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RohstoffDao {
    @Query("SELECT * FROM rohstoffe ORDER BY name")
    fun alle(): Flow<List<Rohstoff>>

    @Query("SELECT * FROM rohstoffe WHERE id = :id")
    suspend fun byId(id: Long): Rohstoff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun einfuegen(rohstoff: Rohstoff): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun einfuegenAlle(rohstoffe: List<Rohstoff>)

    @Update
    suspend fun aktualisieren(rohstoff: Rohstoff)

    @Delete
    suspend fun loeschen(rohstoff: Rohstoff)

    @Query("SELECT COUNT(*) FROM rohstoffe")
    suspend fun anzahl(): Int
}
