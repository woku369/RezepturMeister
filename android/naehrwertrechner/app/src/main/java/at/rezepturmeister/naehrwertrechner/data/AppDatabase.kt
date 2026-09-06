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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rohstoffe ADD COLUMN alkoholGramm REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE rohstoffe ADD COLUMN organischeSaeuren REAL NOT NULL DEFAULT 0.0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rohstoffe ADD COLUMN vomNutzerBearbeitet INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [Rohstoff::class, Rezeptur::class, RezepturZutat::class],
    version = 4,
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
    }
}
