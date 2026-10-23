package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Runder Vorlese-Knopf mit ausreichend grosser Tippflaeche (mind. 48 dp). */
@Composable
fun SpeakButton(onClick: () -> Unit, modifier: Modifier = Modifier, big: Boolean = false) {
    Box(
        modifier
            .size(if (big) 56.dp else 48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClickLabel = "Vorlesen", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("🔊", style = if (big) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium)
    }
}
