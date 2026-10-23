package de.jfricker.vokabeltrainer.domain

import de.jfricker.vokabeltrainer.data.local.SentenceEntity
import de.jfricker.vokabeltrainer.data.local.WordEntity
import kotlin.random.Random

/** Eine Aufgabe in einer Uebungsrunde. key = Schluessel fuer den Lernstand. */
sealed interface PracticeItem {
    val key: String
}

data class CardItem(val word: WordEntity, val example: SentenceEntity?) : PracticeItem {
    override val key: String get() = word.wordKey
}

data class ClozeItem(
    val sentence: SentenceEntity,
    val before: String,
    val answer: String,
    val after: String,
    val options: List<String>,
) : PracticeItem {
    override val key: String get() = sentence.sentenceKey
    val correctIndex: Int get() = options.indexOf(answer)
}

/**
 * Satz aus Bausteinen zusammensetzen.
 * audioOnly = Hoer-Diktat: kein deutscher Text, Satz wird nur vorgelesen.
 * pool enthaelt bei Bedarf zusaetzliche Ablenker-Woerter.
 */
data class BuildItem(
    val sentence: SentenceEntity,
    val target: List<String>,
    val pool: List<String>,
    val audioOnly: Boolean = false,
) : PracticeItem {
    override val key: String get() = sentence.sentenceKey
}

/** Erzeugt Satz-Uebungen aus den Lektionsdaten. */
object Exercises {

    private val ARTICLES = listOf("os ", "as ", "o ", "a ")

    /**
     * Schreibweisen eines Wortes, wie sie in Saetzen vorkommen koennen:
     * "um, uma" -> [um, uma]; "a mesa" -> [mesa]; "tudo bem?" -> [tudo bem]
     */
    fun forms(w: WordEntity): List<String> =
        w.text.split(",")
            .map { it.trim().trimEnd('?', '!').trim() }
            .map { stripArticle(it) }
            .filter { it.isNotEmpty() }

    private fun stripArticle(s: String): String {
        for (a in ARTICLES) if (s.startsWith(a)) return s.removePrefix(a)
        return s
    }

    /** Position des Wortes im Satz (ganzes Wort, Gross/Klein egal) oder null. */
    fun findInSentence(sentence: String, word: WordEntity): IntRange? {
        for (form in forms(word).sortedByDescending { it.length }) {
            val regex = Regex("(?<!\\p{L})" + Regex.escape(form) + "(?!\\p{L})", RegexOption.IGNORE_CASE)
            regex.find(sentence)?.let { return it.range }
        }
        return null
    }

    /** Woerter des Satzes, die sich als Luecke eignen (unveraendert im Satz enthalten). */
    fun clozeCandidates(sentence: SentenceEntity, linked: List<WordEntity>): List<Pair<WordEntity, IntRange>> =
        linked.mapNotNull { w -> findInSentence(sentence.text, w)?.let { w to it } }

    fun cloze(
        sentence: SentenceEntity,
        linked: List<WordEntity>,
        allWords: List<WordEntity>,
        random: Random,
    ): ClozeItem? {
        val (word, range) = clozeCandidates(sentence, linked).randomOrNull(random) ?: return null
        val text = sentence.text
        val answer = text.substring(range)
        val capital = answer.first().isUpperCase()
        fun adapt(s: String) = if (capital) s.replaceFirstChar { it.titlecase() } else s

        // Ablenker bevorzugt aus derselben Wortart, damit die Aufgabe nicht zu leicht ist
        val others = allWords.filter { it.language == word.language && it.wordKey != word.wordKey }
        val samePos = others.filter { it.pos == word.pos }.shuffled(random)
        val rest = others.filter { it.pos != word.pos }.shuffled(random)
        val distractors = (samePos + rest)
            .mapNotNull { forms(it).firstOrNull() }
            .map { adapt(it) }
            .filter { !it.equals(answer, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(3)

        return ClozeItem(
            sentence = sentence,
            before = text.substring(0, range.first),
            answer = answer,
            after = text.substring(range.last + 1),
            options = (distractors + answer).shuffled(random),
        )
    }

    /** Satz in Bausteine zerlegen, Satzzeichen weglassen. */
    fun tokenize(text: String): List<String> =
        text.split(Regex("\\s+"))
            .map { it.trim(',', '.', '!', '?', ';', ':', '"') }
            .filter { it.isNotEmpty() }

    fun canBuild(sentence: SentenceEntity, minTokens: Int = 3): Boolean = tokenize(sentence.text).size >= minTokens

    fun build(
        sentence: SentenceEntity,
        random: Random,
        distractors: List<String> = emptyList(),
        audioOnly: Boolean = false,
    ): BuildItem {
        val target = tokenize(sentence.text)
        var pool = (target + distractors).shuffled(random)
        var tries = 0
        while (pool == target && tries < 5) {
            pool = (target + distractors).shuffled(random)
            tries++
        }
        return BuildItem(sentence, target, pool, audioOnly)
    }

    /**
     * Ablenker fuers Hoer-Diktat: vor allem Woerter, die einem Wort im Satz aehnlich sind
     * (z. B. obrigado/obrigada, você/vocês), damit man genau hinhoeren muss.
     */
    fun similarDistractors(target: List<String>, candidates: List<String>, random: Random, count: Int = 3): List<String> {
        val targetLower = target.map { it.lowercase() }.toSet()
        val pool = candidates
            .map { it.lowercase() }
            .filter { it !in targetLower }
            .distinct()
        if (pool.isEmpty()) return emptyList()

        val scored = pool
            .map { c -> c to target.minOf { t -> similarity(c, t.lowercase()) } }
            .sortedBy { it.second }
        val similar = scored.take(6).shuffled(random).take(count - 1).map { it.first }
        val extra = pool.filter { it !in similar }.randomOrNull(random)
        return (similar + listOfNotNull(extra)).take(count)
    }

    /** 0 = gleich, 1 = voellig verschieden (Levenshtein-Abstand relativ zur Wortlaenge). */
    private fun similarity(a: String, b: String): Double =
        levenshtein(a, b).toDouble() / maxOf(a.length, b.length, 1)

    private fun levenshtein(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            val tmp = prev
            prev = cur
            cur = tmp
        }
        return prev[b.length]
    }

    fun isBuildCorrect(item: BuildItem, answer: List<String>): Boolean =
        answer.size == item.target.size &&
            answer.zip(item.target).all { (a, b) -> a.equals(b, ignoreCase = true) }
}
