package de.jfricker.vokabeltrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.jfricker.vokabeltrainer.domain.Conjugation
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.Tense
import de.jfricker.vokabeltrainer.domain.Verb

private const val LABEL_WEIGHT = 1.1f

/**
 * Konjugationstabelle: eine Zeile pro Person, eine Spalte pro Zeit; Endungen hervorgehoben.
 * highlight = abgefragte Form (Zeit, Person). Antippen einer Form spielt "Pronomen + Form" ab.
 */
@Composable
fun ConjTable(
    verb: Verb,
    tenses: List<Tense>,
    highlight: Pair<Tense, Int>?,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = verb.language
    val emphasis = MaterialTheme.colorScheme.primary
    Column(modifier) {
        if (tenses.size > 1) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Spacer(Modifier.weight(LABEL_WEIGHT))
                tenses.forEach { t ->
                    Text(
                        t.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        language.personLabels.forEachIndexed { slot, label ->
            val marked = highlight != null && highlight.second == slot && highlight.first in tenses
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(if (marked) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(LABEL_WEIGHT),
                )
                tenses.forEach { t ->
                    val form = verb.form(t, slot)
                    val (stem, ending) = Conjugation.split(verb, form)
                    Text(
                        buildAnnotatedString {
                            append(stem)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasis)) { append(ending) }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSpeak("${language.persons[slot].first()} $form") }
                            .padding(vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/** Farbiges Schild: unregelmässig (Warnfarbe) oder regelmässig mit Endung. */
@Composable
fun VerbBadge(verb: Verb, modifier: Modifier = Modifier) {
    val irregular = verb.irregular
    Surface(
        color = if (irregular) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (irregular) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            if (irregular) "⚠ unregelmässig" else "✓ regelmässig · -${verb.verbClass}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** Reiter "Verben" einer Lektion: alle Verben mit Präsens und Vergangenheit. */
@Composable
fun VerbList(verbs: List<Verb>, onSpeak: (String) -> Unit, bottomSpace: androidx.compose.ui.unit.Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomSpace),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "info") {
            Text(
                verbHint(verbs.firstOrNull()?.language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(verbs, key = { it.key }) { verb ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(verb.infinitive, style = MaterialTheme.typography.titleLarge)
                            Text(verb.de, style = MaterialTheme.typography.bodyMedium)
                            VerbBadge(verb, Modifier.padding(top = 4.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        SpeakButton(onClick = { onSpeak(verb.infinitive) })
                    }
                    verb.note?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        )
                    }
                    ConjTable(verb, Tense.entries, highlight = null, onSpeak = onSpeak, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

private fun verbHint(language: Language?): String = when (language) {
    Language.ES_AR -> "Endungen sind farbig. In Argentinien sagt man vos statt tú (vos hablás, vos tenés); " +
        "ustedes ist der Plural für alle. Eine Form antippen, um sie zu hören."
    else -> "Endungen sind farbig. „a gente“ (wir, umgangssprachlich) hat dieselbe Form wie você/ele/ela; " +
        "tu kommt in Brasilien kaum vor. Eine Form antippen, um sie zu hören."
}
