package de.jfricker.vokabeltrainer.domain

/**
 * Lernsprachen. code = Ordner unter assets/lessons und Praefix aller Schluessel
 * (wordKey "es-AR:hola", sentenceKey "es-AR-01:s05") - nicht aendern.
 */
enum class Language(
    val code: String,
    /** "Portugiesisch" */
    val displayName: String,
    /** "portugiesische" (fuer "das portugiesische Wort") */
    val adjective: String,
    val flag: String,
    /** Fuer Meldungen, z. B. "Portugiesisch (Brasilien)" */
    val variantName: String,
    /** Stimmen fuer die Sprachausgabe, in dieser Reihenfolge versucht */
    val ttsLocales: List<String>,
    /** Sprache der YouTube-Untertitel */
    val captionCode: String,
    /** Artikel, die vor Nomen stehen ("a mesa", "la mesa") */
    val articles: List<String>,
    /**
     * Personen fuer die Konjugation; Index = Position der Form in den Verbtabellen.
     * Mehrere Pronomen teilen sich eine Form (você/ele/ela/a gente: fala).
     */
    val persons: List<List<String>>,
    /** Kurze Beschriftung der Personen in Tabellen */
    val personLabels: List<String>,
    /** Name der Zeitformen in der Lernsprache (Tense.code -> Name) */
    val tenseNames: Map<String, String>,
    /** Sonderzeichen als Tasten beim Eintippen */
    val specialChars: List<String>,
    /** Lektion mit Zahlen/Preisen/Uhrzeit: dort gibt es "Zahlen hören" */
    val numbersLessonId: String,
    /** Beispiel fuer Praeposition + Artikel in der Beschreibung */
    val contractionExample: String,
) {
    PT_BR(
        "pt-BR", "Portugiesisch", "portugiesische", "🇧🇷", "Portugiesisch (Brasilien)",
        listOf("pt-BR"), "pt", listOf("os ", "as ", "o ", "a "),
        persons = listOf(listOf("eu"), listOf("você", "ele", "ela", "a gente"), listOf("nós"), listOf("vocês", "eles", "elas")),
        personLabels = listOf("eu", "você / ele / ela", "nós", "vocês / eles"),
        tenseNames = mapOf("pres" to "presente", "past" to "pretérito perfeito"),
        specialChars = listOf("á", "â", "ã", "à", "é", "ê", "í", "ó", "ô", "õ", "ú", "ç"),
        numbersLessonId = "pt-BR-02",
        contractionExample = "em + a = na, de + o = do",
    ),
    ES_AR(
        "es-AR", "Spanisch", "spanische", "🇦🇷", "Spanisch (Argentinien)",
        // Argentinische Stimmen gibt es selten; dann lateinamerikanisches Spanisch
        listOf("es-AR", "es-US", "es-419", "es-MX", "es-ES"), "es", listOf("los ", "las ", "el ", "la "),
        persons = listOf(listOf("yo"), listOf("vos"), listOf("él", "ella", "usted"), listOf("nosotros", "nosotras"), listOf("ellos", "ellas", "ustedes")),
        personLabels = listOf("yo", "vos", "él / ella / usted", "nosotros", "ellos / ustedes"),
        tenseNames = mapOf("pres" to "presente", "past" to "pretérito indefinido"),
        specialChars = listOf("á", "é", "í", "ó", "ú", "ñ", "ü"),
        numbersLessonId = "es-AR-02",
        contractionExample = "a + el = al, de + el = del",
    );

    /** Praefix fuer SQL-LIKE auf wordKey/sentenceKey/itemKey */
    val keyPrefix: String get() = "$code%"

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: PT_BR
    }
}
