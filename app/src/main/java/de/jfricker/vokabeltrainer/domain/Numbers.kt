package de.jfricker.vokabeltrainer.domain

import kotlin.random.Random

/** Art der Zahl beim Hoeren */
enum class NumberKind(val title: String, val inputHint: String) {
    PRICE("Preis", "Betrag in Ziffern"),
    TIME("Uhrzeit", "Uhrzeit, z. B. 14.30"),
    NUMBER("Zahl", "Zahl in Ziffern"),
}

/**
 * Zahlen hoeren (ohne Karteikasten): spoken wird vorgelesen, display ist die Loesung in Ziffern.
 * value: Preis in Hundertsteln, Uhrzeit als HHMM, Zahl als Zahl.
 */
data class NumberItem(
    val kind: NumberKind,
    val spoken: String,
    val display: String,
    val value: Long,
    val language: Language,
    private val id: Int,
) : PracticeItem {
    override val key: String get() = "${language.code}:num:$id"
}

object NumberDrill {

    fun item(language: Language, random: Random): NumberItem {
        val id = random.nextInt(1, Int.MAX_VALUE)
        return when (random.nextInt(10)) {
            in 0..4 -> price(language, random, id)
            in 5..7 -> time(language, random, id, colloquial = random.nextBoolean())
            else -> number(language, random, id)
        }
    }

    /** Eingabe pruefen: Satzzeichen, Leerzeichen und Waehrungszeichen sind egal. */
    fun isCorrect(item: NumberItem, typed: String): Boolean {
        val t = typed.trim().replace(Regex("[R$\\s]"), "")
        if (t.isEmpty()) return false
        return when (item.kind) {
            NumberKind.TIME -> {
                val digits = t.filter { it.isDigit() }
                val parts = t.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }
                val hhmm = when {
                    parts.size == 2 -> parts[0].toIntOrNull()?.let { h -> parts[1].toIntOrNull()?.let { m -> h * 100 + m } }
                    parts.size == 1 && digits.length <= 2 -> digits.toIntOrNull()?.let { it * 100 }
                    else -> digits.toIntOrNull()
                }
                hhmm?.toLong() == item.value
            }
            NumberKind.PRICE -> if (item.language == Language.PT_BR) {
                // 23,50 / 23.50 / 23,5 / 23
                val cents = t.replace(',', '.').toBigDecimalOrNull()?.movePointRight(2)
                cents != null && cents.stripTrailingZeros().scale() <= 0 && cents.toLong() == item.value
            } else {
                // Pesos ohne Centavos; Tausenderpunkt erlaubt
                t.replace(".", "").replace(",", "").toLongOrNull()?.times(100) == item.value
            }
            NumberKind.NUMBER -> t.replace(".", "").toLongOrNull() == item.value
        }
    }

    // ------------------------------------------------------------ Preise

    private fun price(l: Language, r: Random, id: Int): NumberItem = if (l == Language.PT_BR) {
        val reais = if (r.nextInt(3) == 0) r.nextInt(100, 1000) else r.nextInt(1, 100)
        val cents = if (reais >= 100 || r.nextInt(4) == 0) 0 else listOf(5, 10, 20, 25, 30, 40, 45, 50, 60, 75, 80, 90, 95, 99).random(r)
        val spoken = buildString {
            append(pt(reais.toLong(), fem = false))
            append(if (reais == 1) " real" else " reais")
            if (cents > 0) append(" e ").append(pt(cents.toLong(), fem = false)).append(" centavos")
        }
        val display = "R$ $reais," + cents.toString().padStart(2, '0')
        NumberItem(NumberKind.PRICE, spoken, display, reais * 100L + cents, l, id)
    } else {
        // In Argentinien sind Preise vierstellig bis fuenfstellig
        val pesos = if (r.nextBoolean()) r.nextInt(2, 200) * 50 else r.nextInt(20, 200) * 500
        NumberItem(NumberKind.PRICE, es(pesos.toLong(), EsForm.NOUN) + " pesos", "$ " + thousands(pesos.toLong()), pesos * 100L, l, id)
    }

    // ------------------------------------------------------------ Uhrzeiten

    private fun time(l: Language, r: Random, id: Int, colloquial: Boolean): NumberItem {
        val h = r.nextInt(0, 24)
        val m = r.nextInt(0, 12) * 5
        // Mitternacht nie als "zero horas" / "las cero"
        val talk = colloquial || h == 0
        val spoken = if (l == Language.PT_BR) ptTime(h, m, talk) else esTime(h, m, talk)
        val display = "%02d:%02d".format(h, m)
        return NumberItem(NumberKind.TIME, spoken, display, h * 100L + m, l, id)
    }

    private fun ptTime(h: Int, m: Int, colloquial: Boolean): String {
        if (!colloquial) {
            val hours = if (h == 1) "uma hora" else pt(h.toLong(), fem = true) + " horas"
            return if (m == 0) hours else "$hours e ${pt(m.toLong(), fem = false)} minutos"
        }
        val minutes = when (m) {
            0 -> ""
            30 -> " e meia"
            else -> " e " + pt(m.toLong(), fem = false)
        }
        return when (h) {
            0 -> "meia-noite$minutes"
            12 -> "meio-dia$minutes"
            else -> {
                val period = when (h) {
                    in 1..4 -> " da madrugada"
                    in 5..11 -> " da manhã"
                    in 13..18 -> " da tarde"
                    else -> " da noite"
                }
                val h12 = if (h > 12) h - 12 else h
                pt(h12.toLong(), fem = true) + minutes + period
            }
        }
    }

    private fun esTime(h: Int, m: Int, colloquial: Boolean): String {
        if (!colloquial) {
            val hours = if (h == 1) "la una" else "las " + es(h.toLong(), EsForm.FEM)
            return if (m == 0) "$hours en punto" else "$hours y ${es(m.toLong(), EsForm.PLAIN)}"
        }
        val minutes = when (m) {
            0 -> ""
            15 -> " y cuarto"
            30 -> " y media"
            else -> " y " + es(m.toLong(), EsForm.PLAIN)
        }
        return when (h) {
            0 -> "la medianoche$minutes"
            12 -> "el mediodía$minutes"
            else -> {
                val period = when (h) {
                    in 1..4 -> " de la madrugada"
                    in 5..11 -> " de la mañana"
                    in 13..19 -> " de la tarde"
                    else -> " de la noche"
                }
                val h12 = if (h > 12) h - 12 else h
                val hours = if (h12 == 1) "la una" else "las " + es(h12.toLong(), EsForm.FEM)
                hours + minutes + period
            }
        }
    }

    // ------------------------------------------------------------ Zahlen (Zimmer, Bus, Hausnummer)

    private fun number(l: Language, r: Random, id: Int): NumberItem {
        val n = if (r.nextBoolean()) r.nextInt(10, 100) else r.nextInt(100, 1000)
        val spoken = if (l == Language.PT_BR) pt(n.toLong(), fem = false) else es(n.toLong(), EsForm.PLAIN)
        return NumberItem(NumberKind.NUMBER, spoken, n.toString(), n.toLong(), l, id)
    }

    private fun thousands(n: Long): String = String.format(java.util.Locale.ROOT, "%,d", n).replace(',', '.')

    // ------------------------------------------------------------ Zahlwoerter Portugiesisch (Brasilien)

    private val PT_UNITS = listOf(
        "zero", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
        "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
    )
    private val PT_TENS = listOf("", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa")
    private val PT_HUNDREDS = listOf(
        "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos",
    )

    /** fem: "uma", "duas" (für Stunden) */
    fun pt(n: Long, fem: Boolean): String {
        require(n in 0..999_999)
        if (n >= 1000) {
            val t = n / 1000
            val rest = n % 1000
            val th = if (t == 1L) "mil" else pt(t, fem = false) + " mil"
            return when {
                rest == 0L -> th
                rest < 100 || rest % 100 == 0L -> "$th e ${pt(rest, fem)}"
                else -> "$th ${pt(rest, fem)}"
            }
        }
        if (n >= 100) {
            if (n == 100L) return "cem"
            val rest = n % 100
            val h = PT_HUNDREDS[(n / 100).toInt()]
            return if (rest == 0L) h else "$h e ${pt(rest, fem)}"
        }
        if (n >= 20) {
            val rest = n % 10
            val t = PT_TENS[(n / 10).toInt()]
            return if (rest == 0L) t else "$t e ${pt(rest, fem)}"
        }
        if (fem && n == 1L) return "uma"
        if (fem && n == 2L) return "duas"
        return PT_UNITS[n.toInt()]
    }

    // ------------------------------------------------------------ Zahlwoerter Spanisch

    /** PLAIN: uno, veintiuno · NOUN: un, veintiún (vor Nomen und mil) · FEM: una, veintiuna */
    enum class EsForm { PLAIN, NOUN, FEM }

    private val ES_UNITS = listOf(
        "cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve",
        "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis",
        "veintisiete", "veintiocho", "veintinueve",
    )
    private val ES_TENS = listOf("", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")
    private val ES_HUNDREDS = listOf(
        "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos",
        "ochocientos", "novecientos",
    )

    fun es(n: Long, form: EsForm): String {
        require(n in 0..999_999)
        if (n >= 1000) {
            val t = n / 1000
            val rest = n % 1000
            val th = if (t == 1L) "mil" else es(t, EsForm.NOUN) + " mil"
            return if (rest == 0L) th else "$th ${es(rest, form)}"
        }
        if (n >= 100) {
            if (n == 100L) return "cien"
            val rest = n % 100
            val h = ES_HUNDREDS[(n / 100).toInt()]
            return if (rest == 0L) h else "$h ${es(rest, form)}"
        }
        if (n >= 30) {
            val rest = n % 10
            val t = ES_TENS[(n / 10).toInt()]
            return if (rest == 0L) t else "$t y ${es(rest, form)}"
        }
        val word = ES_UNITS[n.toInt()]
        return when {
            n == 1L && form == EsForm.NOUN -> "un"
            n == 1L && form == EsForm.FEM -> "una"
            n == 21L && form == EsForm.NOUN -> "veintiún"
            n == 21L && form == EsForm.FEM -> "veintiuna"
            else -> word
        }
    }
}
