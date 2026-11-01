package de.jfricker.vokabeltrainer.domain

import java.text.Normalizer
import kotlin.random.Random

/** Zeitformen des Konjugationstrainers. code steckt im Lernstand-Schluessel - nicht aendern. */
enum class Tense(val code: String, val title: String) {
    PRESENT("pres", "Präsens"),
    PAST("past", "Vergangenheit");

    /** "presente", "pretérito perfeito" ... */
    fun targetName(language: Language): String = language.tenseNames[code].orEmpty()

    companion object {
        fun fromCode(code: String): Tense? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Verb mit seinen Formen. key = wordKey des Verbs ("pt-BR:falar").
 * forms[tense][slot]: slot = Index in Language.persons.
 */
data class Verb(
    val key: String,
    val id: String,
    val language: Language,
    val lessonId: String,
    /** wie in der Lektion, z. B. "precisar de" oder "quedarse" */
    val infinitive: String,
    val de: String,
    val note: String?,
    val forms: Map<Tense, List<String>>,
    /** unregelmaessig (eigene Formen, Stammwechsel): jede Form wird einzeln geuebt */
    val irregular: Boolean = false,
) {
    fun form(tense: Tense, slot: Int): String = forms[tense]?.getOrNull(slot).orEmpty()

    /** Konjugationsklasse "ar", "er" oder "ir" (Infinitiv ohne "de" bzw. Reflexiv-"se") */
    val verbClass: String get() = Conjugation.baseInfinitive(this).takeLast(2)
}

/**
 * Konjugationsaufgabe: Verb, Zeit und Person; pronoun ist eines der Pronomen dieser Person.
 * Bei regelmaessigen Verben gilt der Lernstand fuer die Endung (patternKey), das Verb ist zufaellig.
 */
data class ConjItem(
    val verb: Verb,
    val tense: Tense,
    val slot: Int,
    val pronoun: String,
    /** Antwortmoeglichkeiten fuer "Auswählen" */
    val options: List<String>,
    val patternKey: String? = null,
) : PracticeItem {
    override val key: String get() = patternKey ?: Conjugation.itemKey(verb.key, tense, slot)
    val answer: String get() = verb.form(tense, slot)
    val correctIndex: Int get() = options.indexOf(answer)

    /** Wird nach der Antwort vorgelesen, z. B. "nós falamos" */
    val spoken: String get() = "$pronoun $answer"
}

enum class AnswerCheck { CORRECT, ACCENTS_MISSING, WRONG }

object Conjugation {

    /** Lernstand-Schluessel einer Form eines unregelmaessigen Verbs: "pt-BR:ser:pres:2" */
    fun itemKey(verbKey: String, tense: Tense, slot: Int): String = "$verbKey:${tense.code}:$slot"

    /** Lernstand-Schluessel einer Endung regelmaessiger Verben: "pt-BR:@ar:pres:2" */
    fun patternKey(language: Language, verbClass: String, tense: Tense, slot: Int): String =
        itemKey("${language.code}:$PATTERN_MARK$verbClass", tense, slot)

    /** Klasse aus dem verbKey-Teil eines Endungs-Schluessels ("pt-BR:@ar" -> "ar") oder null. */
    fun patternClass(verbKey: String): String? =
        verbKey.substringAfter(':', "").takeIf { it.startsWith(PATTERN_MARK) }?.removePrefix(PATTERN_MARK)

    private const val PATTERN_MARK = "@"

    /** Gegenstueck zu [itemKey]: (verbKey, Zeit, Person) oder null. */
    fun parseKey(key: String): Triple<String, Tense, Int>? {
        val parts = key.split(":")
        if (parts.size < 4) return null
        val slot = parts.last().toIntOrNull() ?: return null
        val tense = Tense.fromCode(parts[parts.size - 2]) ?: return null
        return Triple(parts.dropLast(2).joinToString(":"), tense, slot)
    }

    fun item(verb: Verb, tense: Tense, slot: Int, random: Random, patternKey: String? = null): ConjItem {
        val answer = verb.form(tense, slot)
        // Ablenker: zuerst andere Personen derselben Zeit, dann Formen der anderen Zeit
        val sameTense = verb.forms[tense].orEmpty().filterIndexed { i, _ -> i != slot }.shuffled(random)
        val otherTense = verb.forms.filterKeys { it != tense }.values.flatten().shuffled(random)
        val distractors = (sameTense + otherTense)
            .filter { !it.equals(answer, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(3)
        return ConjItem(
            verb = verb,
            tense = tense,
            slot = slot,
            pronoun = verb.language.persons[slot].random(random),
            options = (distractors + answer).shuffled(random),
            patternKey = patternKey,
        )
    }

    /**
     * Eingetippte Antwort pruefen. Gross/Klein und Leerzeichen sind egal, das Pronomen darf
     * mitgetippt werden. Fehlen nur Akzente (voce statt você), gilt es als richtig, mit Hinweis.
     */
    fun check(item: ConjItem, typed: String): AnswerCheck {
        val answer = normalize(item.answer)
        var t = normalize(typed)
        val pronoun = normalize(item.pronoun) + " "
        if (t.startsWith(pronoun) && !answer.startsWith(pronoun)) t = t.removePrefix(pronoun)
        return checkText(answer, t)
    }

    /** Wie [check], fuer beliebige Antworten (Verb im Satz, Plural). */
    fun checkText(answer: String, typed: String): AnswerCheck {
        val a = normalize(answer)
        val t = normalize(typed)
        return when {
            t == a -> AnswerCheck.CORRECT
            stripAccents(t) == stripAccents(a) -> AnswerCheck.ACCENTS_MISSING
            else -> AnswerCheck.WRONG
        }
    }

    private fun normalize(s: String): String = s.trim().lowercase().replace(Regex("\\s+"), " ")

    fun stripAccents(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

    /**
     * Form in Stamm und Endung zerlegen, damit die Endung hervorgehoben werden kann:
     * "falamos" -> ("fal", "amos"), "me quedo" -> ("me qued", "o").
     * Weicht die Form vom Stamm ab (unregelmaessig, Stammwechsel), wird alles hervorgehoben.
     */
    fun split(verb: Verb, form: String): Pair<String, String> {
        val stem = stem(verb)
        val lastSpace = form.lastIndexOf(' ')
        val prefix = form.substring(0, lastSpace + 1)
        val core = form.substring(lastSpace + 1)
        return if (stem.isNotEmpty() && core.startsWith(stem) && core.length > stem.length) {
            (prefix + stem) to core.substring(stem.length)
        } else {
            prefix to core
        }
    }

    /** "precisar de" -> "precis", "quedarse" -> "qued" */
    private fun stem(verb: Verb): String {
        val inf = baseInfinitive(verb)
        return if (inf.length > 3) inf.dropLast(2) else ""
    }

    /** "precisar de" -> "precisar", "quedarse" -> "quedar" */
    fun baseInfinitive(verb: Verb): String {
        val inf = verb.infinitive.trim().split(" ").first().lowercase()
        return if (verb.language == Language.ES_AR && inf.endsWith("se") && inf.length > 4) inf.dropLast(2) else inf
    }
}
