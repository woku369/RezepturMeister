package at.rezepturmeister.naehrwertrechner

import android.app.Application
import at.rezepturmeister.naehrwertrechner.data.AppDatabase
import at.rezepturmeister.naehrwertrechner.data.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NaehrwertRechnerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }

    override fun onCreate() {
        super.onCreate()
        // Gleicht bei jedem Start die Standard-Rohstoffe mit SeedData ab: neue Einträge
        // werden ergänzt, bereits vorhandene auf den aktuellen Stand aktualisiert (z. B.
        // wenn eine Nährwertangabe im Nachhinein korrigiert wurde). Siehe Kommentar bei
        // RohstoffDao.syncSeedDaten() zur aktuell fehlenden Nutzer-Bearbeiten-Ansicht.
        CoroutineScope(Dispatchers.IO).launch {
            database.rohstoffDao().syncSeedDaten(SeedData.initialeRohstoffe())
        }
    }
}
