package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Erklaerung / Hinweis im kleinen Text. Antippen oeffnet einen Dialog, in dem der Text gross
 * und gut lesbar steht (Wunsch des Nutzers: Erklaerungen sind sonst zu klein).
 * Liegt der Text in etwas Antippbarem (Sprechblase, Karte), gewinnt der Text: Antippen zeigt die Erklaerung.
 */
@Composable
fun ExplanationText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    textAlign: TextAlign? = null,
    /** Ueberschrift im Dialog (z. B. Name der Regel) */
    title: String? = null,
) {
    var open by remember { mutableStateOf(false) }
    Text(
        text,
        style = style,
        color = color,
        fontStyle = fontStyle,
        textAlign = textAlign,
        modifier = modifier.clickable(onClickLabel = "Grösser anzeigen") { open = true },
    )
    if (open) ExplanationDialog(text, title, onDismiss = { open = false })
}

/** Erklaerung in grosser Schrift; lange Texte lassen sich scrollen. */
@Composable
fun ExplanationDialog(text: String, title: String? = null, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schliessen") } },
        title = title?.let { { Text(it, style = MaterialTheme.typography.headlineSmall) } },
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, lineHeight = 32.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
    )
}
