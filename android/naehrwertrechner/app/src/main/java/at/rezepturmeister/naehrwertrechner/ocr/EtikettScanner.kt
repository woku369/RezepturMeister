package at.rezepturmeister.naehrwertrechner.ocr

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Führt die reine Texterkennung (OCR) auf einem fotografierten/ausgewählten Etikett
 * durch – on-device über ML Kit (Google Play Services), kein Upload, keine
 * Internetverbindung zur Laufzeit nötig. Das Parsen des erkannten Texts in einzelne
 * Nährwerte übernimmt EtikettParser (bewusst getrennt, weil der Parser als reiner
 * Kotlin-Code ohne Android-Abhängigkeit per JVM-Unit-Test prüfbar bleiben soll).
 */
object EtikettScanner {
    suspend fun texterkennung(context: Context, bildUri: Uri): String {
        val bild = InputImage.fromFilePath(context, bildUri)
        val erkenner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val ergebnis = erkenner.process(bild).await()
        return ergebnis.text
    }

    /** Erzeugt eine frische, per FileProvider freigegebene Ziel-Uri für ein Kamerafoto. */
    fun neueFotoUri(context: Context): Uri {
        val verzeichnis = File(context.cacheDir, "photos").apply { mkdirs() }
        val datei = File(verzeichnis, "etikett_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", datei)
    }
}
