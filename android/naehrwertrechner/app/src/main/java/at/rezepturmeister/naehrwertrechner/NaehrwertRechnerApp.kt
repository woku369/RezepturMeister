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
        // Ergänzt bei jedem Start fehlende Standard-Rohstoffe (z. B. nach einem
        // App-Update mit neuen SeedData-Einträgen). Der eindeutige Index auf
        // Rohstoff.name sorgt dafür, dass bereits vorhandene oder vom Nutzer
        // selbst korrigierte Einträge dabei NICHT überschrieben werden.
        CoroutineScope(Dispatchers.IO).launch {
            database.rohstoffDao().einfuegenAlle(SeedData.initialeRohstoffe())
        }
    }
}
