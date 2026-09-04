package at.rezepturmeister.naehrwertrechner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Rohstoff::class, Rezeptur::class, RezepturZutat::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rohstoffDao(): RohstoffDao
    abstract fun rezepturDao(): RezepturDao
    abstract fun rezepturZutatDao(): RezepturZutatDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: run {
                    lateinit var built: AppDatabase
                    built = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "naehrwertrechner.db"
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch {
                                built.rohstoffDao().einfuegenAlle(SeedData.initialeRohstoffe())
                            }
                        }
                    }).build()
                    instance = built
                    built
                }
            }
    }
}
