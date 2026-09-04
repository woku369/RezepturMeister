package at.rezepturmeister.naehrwertrechner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gruen = Color(0xFF2E6E4E)
private val GruenDunkel = Color(0xFF163D29)

private val LightColors = lightColorScheme(
    primary = Gruen,
    secondary = GruenDunkel
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FC9A0),
    secondary = Color(0xFFB6E2C8)
)

@Composable
fun NaehrwertRechnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
