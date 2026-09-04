package at.rezepturmeister.naehrwertrechner

import android.app.Application
import at.rezepturmeister.naehrwertrechner.data.AppDatabase

class NaehrwertRechnerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
}
