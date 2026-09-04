package at.rezepturmeister.naehrwertrechner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_rohstoffe_name ON rohstoffe(name)")
    }
}

@Database(
    entities = [Rohstoff::class, Rezeptur::class, RezepturZutat::class],
    version = 2,
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

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naehrwertrechner.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
