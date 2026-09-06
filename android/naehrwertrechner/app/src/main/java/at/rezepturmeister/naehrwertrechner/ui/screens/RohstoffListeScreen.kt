package at.rezepturmeister.naehrwertrechner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.rezepturmeister.naehrwertrechner.data.Rohstoff

@Composable
fun RohstoffListeScreen(
    rohstoffe: List<Rohstoff>,
    onRohstoffAuswaehlen: (Rohstoff) -> Unit,
    onNeuerRohstoff: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(onClick = onNeuerRohstoff, modifier = Modifier.fillMaxWidth()) {
                Text("+ Neuen Rohstoff anlegen")
            }
        }
        items(rohstoffe, key = { it.id }) { rohstoff ->
            RohstoffZeile(rohstoff, onClick = { onRohstoffAuswaehlen(rohstoff) })
        }
    }
}

@Composable
private fun RohstoffZeile(rohstoff: Rohstoff, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(rohstoff.name, style = MaterialTheme.typography.titleMedium)
                if (!rohstoff.istVollstaendig()) {
                    Text(
                        "⚠ unvollständig",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(rohstoff.kategorie, style = MaterialTheme.typography.bodySmall)
            Text(
                "${rohstoff.energieKcal?.let { "%.0f".format(it) } ?: "–"} kcal / 100 g · " +
                    "Quelle: ${rohstoff.quelle.anzeigename}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
