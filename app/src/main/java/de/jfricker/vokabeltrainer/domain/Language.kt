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
) {
    PT_BR(
        "pt-BR", "Portugiesisch", "portugiesische", "🇧🇷", "Portugiesisch (Brasilien)",
        listOf("pt-BR"), "pt", listOf("os ", "as ", "o ", "a "),
    ),
    ES_AR(
        "es-AR", "Spanisch", "spanische", "🇦🇷", "Spanisch (Argentinien)",
        // Argentinische Stimmen gibt es selten; dann lateinamerikanisches Spanisch
        listOf("es-AR", "es-US", "es-419", "es-MX", "es-ES"), "es", listOf("los ", "las ", "el ", "la "),
    );

    /** Praefix fuer SQL-LIKE auf wordKey/sentenceKey/itemKey */
    val keyPrefix: String get() = "$code%"

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: PT_BR
    }
}
