package de.jfricker.vokabeltrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import de.jfricker.vokabeltrainer.domain.ConjItem
import de.jfricker.vokabeltrainer.domain.GapItem
import de.jfricker.vokabeltrainer.domain.NumberItem
import androidx.compose.ui.text.input.KeyboardType
import de.jfricker.vokabeltrainer.domain.ItemKind
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.jfricker.vokabeltrainer.domain.BuildItem
import de.jfricker.vokabeltrainer.domain.CardItem
import de.jfricker.vokabeltrainer.domain.ClozeItem
import de.jfricker.vokabeltrainer.domain.Language
import de.jfricker.vokabeltrainer.domain.PracticeMode

/**
 * Abfrage: Karteikarten und Satz-Uebungen.
 * lessonId = null: alle Lektionen. today = gemischte Tagesrunde ("Heute lernen").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    vm: MainViewModel,
    lessonId: String?,
    today: Boolean,
    onBack: () -> Unit,
    /** Runde mit den schwierigen Woertern aus der Statistik */
    difficult: Boolean = false,
) {
    val state by vm.practice.collectAsStateWithLifecycle()
    val slow by vm.slow.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()

    // Nur beim Betreten vorbereiten, nicht z. B. nach dem Drehen des Handys mitten in der Runde
    LaunchedEffect(lessonId, today, difficult) {
        if (vm.practice.value == null) {
            when {
                today -> vm.startToday()
                difficult -> vm.startDifficult()
                else -> vm.preparePractice(lessonId)
            }
        }
    }

    val close = {
        vm.endPractice()
        onBack()
    }
    BackHandler(onBack = close)

    val title = when (val s = state) {
        is PracticeState.Setup -> s.title
        is PracticeState.Asking -> s.mode.label(language)
        else -> "Üben"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = close) { Text("←", style = MaterialTheme.typography.titleLarge) }
                },
                actions = {
                    FilterChip(selected = slow, onClick = { vm.setSlow(!slow) }, label = { Text("Langsam") })
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            when (val s = state) {
                null -> Text("Wird vorbereitet …")
                is PracticeState.Setup -> SetupContent(s, language, vm::startPractice)
                is PracticeState.Asking -> AskingContent(s, vm)
                is PracticeState.Finished -> FinishedContent(
                    s,
                    onRestart = {
                        when {
                            today -> vm.startToday()
                            difficult -> vm.startDifficult()
                            else -> vm.preparePractice(lessonId)
                        }
                    },
                    onMoreNew = { vm.startToday(extraNew = true) },
                    onClose = close,
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Auswahl

@Composable
private fun SetupContent(s: PracticeState.Setup, language: Language, onStart: (PracticeMode) -> Unit) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Wörter", style = MaterialTheme.typography.titleMedium)
        PracticeMode.entries.filter { it.kind == ItemKind.WORD }.forEach { ModeCard(it, s, language, onStart) }
        Spacer(Modifier.height(8.dp))
        Text("Sätze", style = MaterialTheme.typography.titleMedium)
        PracticeMode.entries.filter { it.kind == ItemKind.SENTENCE }.forEach { ModeCard(it, s, language, onStart) }
        Spacer(Modifier.height(8.dp))
        Text("Grammatik", style = MaterialTheme.typography.titleMedium)
        // Nur Uebungsarten, die es in dieser Lektion gibt (z. B. "Zahlen hören" nur in der Zahlen-Lektion)
        PracticeMode.entries
            .filter { it.kind in GRAMMAR_KINDS && s.counts[it]?.exists != false }
            .forEach { ModeCard(it, s, language, onStart) }
        Text(
            "Pro Runde höchstens 20 Aufgaben, davon bis zu 10 neue. Gleich beim ersten Mal richtig: " +
                "kommt nach 8 Tagen noch einmal, danach nicht mehr. Sonst kommt Richtiges nach " +
                "1, 2, 4, 8, 16 und 32 Tagen wieder, Falsches sofort.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModeCard(mode: PracticeMode, s: PracticeState.Setup, language: Language, onStart: (PracticeMode) -> Unit) {
    val c = s.counts[mode]
    val due = c?.due ?: 0
    val new = c?.new ?: 0
    val extra = c?.extra ?: 0
    val hasItems = due + new + extra > 0
    Card(
        onClick = { if (hasItems) onStart(mode) },
        enabled = hasItems,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(mode.label(language), style = MaterialTheme.typography.titleMedium)
            ExplanationText(mode.description(language), style = MaterialTheme.typography.bodySmall, title = mode.label(language))
            Text(
                when {
                    due + new > 0 -> "$due fällig · $new neu"
                    mode == PracticeMode.DICTATION -> "Zufällige längere Sätze · ohne Karteikasten"
                    mode == PracticeMode.NUMBERS -> "Zufällige Preise, Uhrzeiten und Zahlen · ohne Karteikasten"
                    extra > 0 -> "Nichts fällig · freies Üben mit zufälligen regelmässigen Verben"
                    else -> "Heute nichts mehr fällig"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ------------------------------------------------------------------ Aufgabe

@Composable
private fun AskingContent(s: PracticeState.Asking, vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { if (s.total == 0) 0f else s.done.toFloat() / s.total },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "${s.done} / ${s.total}" + if (!s.entry.firstAttempt) "  · Wiederholung" else "",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        when (val item = s.item) {
            is CardItem -> CardContent(s, item, vm)
            is ClozeItem -> ClozeContent(s, item, vm)
            is BuildItem -> BuildContent(s, item, vm)
            is ConjItem -> ConjContent(s, item, vm)
            is GapItem -> GapContent(s, item, vm)
            is NumberItem -> NumberContent(s, item, vm)
        }
    }
}

@Composable
private fun ColumnScope.CardContent(s: PracticeState.Asking, item: CardItem, vm: MainViewModel) {
    val word = item.word
    val ptText = displayText(word)
    val ptFirst = s.mode == PracticeMode.CARD_PT_DE
    // Nach dem Umdrehen steht immer die Lernsprache oben, Deutsch darunter
    val ptOnTop = ptFirst || s.revealed

    // Bei Lernsprache -> Deutsch die Frage gleich vorlesen
    LaunchedEffect(s.step) {
        if (ptFirst) vm.speak(word.text)
    }

    Card(
        Modifier
            .fillMaxWidth()
            .weight(1f),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SpeakableText(
                if (ptOnTop) ptText else word.de,
                speakable = ptOnTop,
                onSpeak = { vm.speak(word.text) },
                big = true,
            )
            if (s.revealed) {
                HorizontalDivider(Modifier.padding(vertical = 20.dp))
                SpeakableText(word.de, speakable = false, onSpeak = {}, big = true)
                word.note?.let {
                    ExplanationText(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                item.example?.let { ex ->
                    Spacer(Modifier.height(24.dp))
                    Text("Beispiel", style = MaterialTheme.typography.labelMedium)
                    SpeakableText(ex.text, speakable = true, onSpeak = { vm.speak(ex.text) }, big = false)
                    Text(ex.de, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    if (!s.revealed) {
        Button(onClick = vm::reveal, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Umdrehen")
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { vm.answerCard(false) },
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text("Nicht gewusst") }
            Button(
                onClick = { vm.answerCard(true) },
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text("Gewusst") }
        }
    }
}

@Composable
private fun ColumnScope.ClozeContent(s: PracticeState.Asking, item: ClozeItem, vm: MainViewModel) {
    Column(
        Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(item.sentence.de, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
        val highlight = MaterialTheme.colorScheme.primary
        val sentence = if (s.result == null) {
            buildAnnotatedString {
                append(item.before)
                append("_____")
                append(item.after)
            }
        } else {
            buildAnnotatedString {
                append(item.before)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlight)) {
                    append(item.answer)
                }
                append(item.after)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(sentence, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f, fill = false))
            if (s.result != null) {
                Spacer(Modifier.width(12.dp))
                SpeakButton(onClick = { vm.speak(item.sentence.text) })
            }
        }
        Spacer(Modifier.height(8.dp))
        ChoiceList(item.options, item.correctIndex, s, vm::choose)
    }
    ResultFooter(s, solution = null, onContinue = vm::continuePractice)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.BuildContent(s: PracticeState.Asking, item: BuildItem, vm: MainViewModel) {
    // Indizes der bereits gelegten Bausteine (aus item.pool), neu fuer jede Aufgabe
    val placed = remember(s.step) { mutableStateListOf<Int>() }
    val answered = s.result != null

    // Hoer-Diktat: Satz gleich vorlesen; danach beliebig oft nochmals anhoeren
    LaunchedEffect(s.step) {
        if (item.audioOnly) vm.speak(item.sentence.text)
    }

    Column(
        Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (item.audioOnly) {
            if (!answered) {
                OutlinedButton(
                    onClick = { vm.speak(item.sentence.text) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                ) {
                    Text("🔊  Nochmals anhören", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (answered) {
                // Loesung: Deutsch und direkt darunter die Lernsprache
                Text(item.sentence.de, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                SpeakableText(item.sentence.text, speakable = true, onSpeak = { vm.speak(item.sentence.text) }, big = false)
            } else {
                Text(
                    "Setze den Satz zusammen, den du hörst. Nicht alle Bausteine gehören dazu.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Text(item.sentence.de, style = MaterialTheme.typography.titleLarge)
        }

        Card(Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
            FlowRow(
                Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                placed.forEach { idx ->
                    SuggestionChip(
                        onClick = { if (!answered) placed.remove(idx) },
                        label = { Text(item.pool[idx], style = MaterialTheme.typography.titleMedium) },
                    )
                }
            }
        }

        if (!answered) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item.pool.forEachIndexed { idx, token ->
                    if (idx !in placed) {
                        SuggestionChip(
                            onClick = { placed.add(idx) },
                            label = { Text(token, style = MaterialTheme.typography.titleMedium) },
                        )
                    }
                }
            }
            if (placed.isNotEmpty()) {
                TextButton(onClick = { placed.clear() }) { Text("Zurücksetzen") }
            }
        }
    }

    if (!answered) {
        Button(
            onClick = { vm.submitBuild(placed.map { item.pool[it] }) },
            enabled = placed.size == item.target.size,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Prüfen") }
    } else {
        // Beim Hoer-Diktat steht die Loesung schon oben unter dem deutschen Satz
        ResultFooter(
            s,
            solution = if (item.audioOnly) null else item.sentence.text,
            onContinue = vm::continuePractice,
            onSpeak = { vm.speak(item.sentence.text) },
        )
    }
}

/**
 * Konjugation: Verb, Zeit und Pronomen werden gezeigt, die Form wird eingetippt oder ausgewählt.
 * Nach der Antwort erscheint die ganze Tabelle der Zeit mit hervorgehobenen Endungen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ConjContent(s: PracticeState.Asking, item: ConjItem, vm: MainViewModel) {
    val typing by vm.conjTyping.collectAsStateWithLifecycle()
    val language = item.verb.language
    val answered = s.result != null
    var input by remember(s.step) { mutableStateOf(TextFieldValue("")) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(s.step, typing) {
        if (typing && !answered) runCatching { focus.requestFocus() }
    }
    fun insert(c: String) {
        val sel = input.selection
        val text = input.text.replaceRange(sel.min, sel.max, c)
        input = TextFieldValue(text, TextRange(sel.min + c.length))
    }

    Column(
        Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${item.tense.title} · ${item.tense.targetName(language)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            FilterChip(selected = typing, onClick = { vm.setConjTyping(!typing) }, label = { Text("Tippen") })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.verb.infinitive, style = MaterialTheme.typography.headlineSmall)
                Text(item.verb.de, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
            }
            Spacer(Modifier.width(12.dp))
            SpeakButton(onClick = { vm.speak(item.verb.infinitive) })
        }

        val highlight = MaterialTheme.colorScheme.primary
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    append(item.pronoun)
                    append("  ")
                    if (answered) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlight)) { append(item.answer) }
                    } else {
                        append("_____")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (answered) {
                Spacer(Modifier.width(12.dp))
                SpeakButton(onClick = { vm.speak(item.spoken) })
            }
        }

        if (typing && !answered) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                label = { Text("Verbform") },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { vm.submitTyped(input.text) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                language.specialChars.forEach { c ->
                    SuggestionChip(onClick = { insert(c) }, label = { Text(c, style = MaterialTheme.typography.titleMedium) })
                }
            }
        }
        if (!typing && s.typed == null) {
            ChoiceList(item.options, item.correctIndex, s, vm::choose)
        }
        if (answered) {
            when {
                s.accentHint -> Text(
                    "Fast perfekt – achte auf die Akzente: ${item.answer} (du: ${s.typed})",
                    style = MaterialTheme.typography.bodyLarge,
                )
                s.result == false && s.typed != null -> Text(
                    "Deine Antwort: ${s.typed}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Card(Modifier.fillMaxWidth()) {
                ConjTable(
                    item.verb,
                    listOf(item.tense),
                    highlight = item.tense to item.slot,
                    onSpeak = vm::speak,
                    modifier = Modifier.padding(8.dp),
                )
            }
            item.verb.note?.let {
                ExplanationText(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            }
        }
    }

    if (!answered && typing) {
        Button(
            onClick = { vm.submitTyped(input.text) },
            enabled = input.text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Prüfen") }
    } else if (answered) {
        ResultFooter(s, solution = item.spoken, onContinue = vm::continuePractice, onSpeak = { vm.speak(item.spoken) })
    }
}

private val GRAMMAR_KINDS = setOf(ItemKind.VERB, ItemKind.EXERCISE, ItemKind.NUMBER)

/**
 * Grammatik-Uebung mit Luecke (Verb im Satz, ser/estar, Artikel, Praeposition + Artikel, Plural).
 * Verbform und Plural koennen eingetippt werden (Schalter "Tippen" wie bei der Konjugation).
 */
@Composable
private fun ColumnScope.GapContent(s: PracticeState.Asking, item: GapItem, vm: MainViewModel) {
    val typingPref by vm.conjTyping.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()
    val ex = item.exercise
    val typing = ex.typeable && typingPref
    val answered = s.result != null
    var input by remember(s.step) { mutableStateOf(TextFieldValue("")) }

    Column(
        Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (ex.typeable) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                FilterChip(selected = typingPref, onClick = { vm.setConjTyping(!typingPref) }, label = { Text("Tippen") })
            }
        }
        if (ex.de.isNotEmpty()) {
            Text(ex.de, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
        }
        val highlight = MaterialTheme.colorScheme.primary
        val muted = MaterialTheme.colorScheme.onSurfaceVariant
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    append(ex.before)
                    if (answered) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlight)) { append(ex.answer) }
                    } else {
                        append("_____")
                        if (ex.hint.isNotEmpty()) {
                            withStyle(SpanStyle(color = muted, fontStyle = FontStyle.Italic)) { append(" " + ex.hint) }
                        }
                    }
                    append(ex.after)
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (answered) {
                Spacer(Modifier.width(12.dp))
                SpeakButton(onClick = { vm.speak(ex.speak) })
            }
        }

        if (typing && !answered) {
            AnswerField(
                step = s.step, value = input, onValueChange = { input = it }, label = "Antwort",
                specialChars = language.specialChars, numeric = false, onSubmit = { vm.submitTyped(input.text) },
            )
        }
        if (!typing && s.typed == null) {
            ChoiceList(item.options, item.correctIndex, s, vm::choose)
        }
        if (answered) {
            when {
                s.accentHint -> Text(
                    "Fast perfekt – achte auf die Akzente: ${ex.answer} (du: ${s.typed})",
                    style = MaterialTheme.typography.bodyLarge,
                )
                s.result == false && s.typed != null -> Text("Deine Antwort: ${s.typed}", style = MaterialTheme.typography.bodyLarge)
            }
            if (ex.note.isNotEmpty() && ex.type != PracticeMode.SER_ESTAR.code) {
                ExplanationText(ex.note, style = MaterialTheme.typography.titleSmall)
            }
            item.rule?.let {
                Card(Modifier.fillMaxWidth()) {
                    ExplanationText(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp), title = "Regel")
                }
            }
        }
    }

    if (!answered && typing) {
        Button(
            onClick = { vm.submitTyped(input.text) },
            enabled = input.text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Prüfen") }
    } else if (answered) {
        val solution = ex.before + ex.answer + ex.after
        ResultFooter(s, solution = solution, onContinue = vm::continuePractice, onSpeak = { vm.speak(ex.speak) })
    }
}

/** Zahlen hoeren: Preis, Uhrzeit oder Zahl wird vorgelesen und in Ziffern eingetippt (ohne Karteikasten). */
@Composable
private fun ColumnScope.NumberContent(s: PracticeState.Asking, item: NumberItem, vm: MainViewModel) {
    val answered = s.result != null
    var input by remember(s.step) { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(s.step) { vm.speak(item.spoken) }

    Column(
        Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("${item.kind.title} – hör genau hin", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = { vm.speak(item.spoken) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
        ) { Text("🔊  Nochmals anhören", style = MaterialTheme.typography.titleMedium) }
        if (!answered) {
            AnswerField(
                step = s.step, value = input, onValueChange = { input = it }, label = item.kind.inputHint,
                specialChars = emptyList(), numeric = true, onSubmit = { vm.submitTyped(input.text) },
            )
        } else {
            Text(
                item.display,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(item.spoken, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
            if (s.result == false) {
                Text("Deine Antwort: ${s.typed}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (!answered) {
        Button(
            onClick = { vm.submitTyped(input.text) },
            enabled = input.text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text("Prüfen") }
    } else {
        ResultFooter(s, solution = null, onContinue = vm::continuePractice)
    }
}

/** Eingabefeld mit Fokus beim Erscheinen und optionalen Sonderzeichen-Tasten. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerField(
    step: Int,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    specialChars: List<String>,
    numeric: Boolean,
    onSubmit: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(step) { runCatching { focus.requestFocus() } }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        textStyle = MaterialTheme.typography.titleLarge,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
    )
    if (specialChars.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            specialChars.forEach { c ->
                SuggestionChip(
                    onClick = {
                        val sel = value.selection
                        val text = value.text.replaceRange(sel.min, sel.max, c)
                        onValueChange(TextFieldValue(text, TextRange(sel.min + c.length)))
                    },
                    label = { Text(c, style = MaterialTheme.typography.titleMedium) },
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Bausteine

/** Antwortmoeglichkeiten; nach der Antwort richtig = gruen, falsch gewaehlt = rot. */
@Composable
private fun ChoiceList(options: List<String>, correctIndex: Int, s: PracticeState.Asking, onChoose: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, option ->
            val container = when {
                s.result == null -> Color.Transparent
                i == correctIndex -> CORRECT_COLOR
                i == s.chosen -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            OutlinedButton(
                onClick = { onChoose(i) },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = container),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                Text(option, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

private val CORRECT_COLOR = Color(0x6643A047)

@Composable
private fun ResultFooter(
    s: PracticeState.Asking,
    solution: String?,
    onContinue: () -> Unit,
    onSpeak: (() -> Unit)? = null,
) {
    val result = s.result ?: return
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (result) "Richtig! ✅" else "Leider falsch ❌",
            style = MaterialTheme.typography.titleLarge,
        )
        if (solution != null && !result) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Richtig: $solution", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                if (onSpeak != null) {
                    Spacer(Modifier.width(12.dp))
                    SpeakButton(onClick = onSpeak)
                }
            }
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Weiter") }
    }
}

@Composable
private fun SpeakableText(text: String, speakable: Boolean, onSpeak: () -> Unit, big: Boolean) {
    val style = if (big) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = style, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
        if (speakable) {
            Spacer(Modifier.width(12.dp))
            SpeakButton(onClick = onSpeak, big = big)
        }
    }
}

@Composable
private fun FinishedContent(
    s: PracticeState.Finished,
    onRestart: () -> Unit,
    onMoreNew: () -> Unit,
    onClose: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when {
            s.today && s.nothingToDo ->
                Text("Für heute ist alles erledigt. 🎉", style = MaterialTheme.typography.titleLarge)
            s.nothingToDo ->
                Text("Hier ist im Moment nichts fällig. 🎉", style = MaterialTheme.typography.titleLarge)
            else -> {
                Text("Runde geschafft! 🎉", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Beim ersten Versuch richtig: ${s.correct} von ${s.correct + s.wrong}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (s.today) {
            if (s.moreToDo) {
                Text("Für heute ist noch etwas offen.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Nächste Runde") }
            } else {
                Text("Tagesziel erreicht ✅", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onMoreNew, modifier = Modifier.fillMaxWidth()) { Text("Trotzdem mehr Neues lernen") }
            }
        } else {
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Weiter üben") }
        }
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Zurück") }
    }
}
