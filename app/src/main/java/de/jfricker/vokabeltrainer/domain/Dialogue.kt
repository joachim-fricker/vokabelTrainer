package de.jfricker.vokabeltrainer.domain

import kotlin.random.Random

/** Eine Zeile im Dialog. role 0 = Gegenueber, 1 = Du. */
data class DialogueLine(val role: Int, val text: String, val de: String, val note: String = "")

/** Typischer Dialog (assets/dialogues/<sprache>.json), Schluessel "pt-BR:d03". */
data class Dialogue(
    val key: String,
    val id: String,
    val language: Language,
    val title: String,
    val titleTarget: String,
    val situation: String,
    val lessonId: String?,
    /** [Gegenueber, Du] */
    val roles: List<String>,
    val lines: List<DialogueLine>,
) {
    fun roleName(role: Int): String = roles.getOrElse(role) { if (role == 1) "Du" else "?" }
}

object Dialogues {

    /** Stimmhoehe je Rolle, damit man die beiden Personen beim Abspielen unterscheiden kann. */
    fun pitch(role: Int): Float = if (role == 0) 1.15f else 0.85f

    /** Pause zwischen zwei Saetzen beim Abspielen (ms); bei "Langsam" etwas laenger */
    fun pauseMs(slow: Boolean): Long = if (slow) 1800L else 1300L

    /**
     * Drei Antworten fuer "Rolle spielen": die richtige und zwei andere Saetze derselben Rolle,
     * moeglichst aus demselben Dialog (passen zur Situation, kommen aber an anderer Stelle).
     * Rueckgabe: Antworten und Index der richtigen.
     */
    fun options(dialogue: Dialogue, index: Int, all: List<Dialogue>, random: Random = Random): Pair<List<String>, Int> {
        val line = dialogue.lines[index]
        fun norm(s: String) = s.lowercase().trim()
        val wrong = mutableListOf<String>()
        fun addFrom(candidates: List<String>) {
            for (c in candidates.shuffled(random)) {
                if (wrong.size >= 2) return
                if (norm(c) != norm(line.text) && wrong.none { norm(it) == norm(c) }) wrong += c
            }
        }
        addFrom(dialogue.lines.filter { it.role == line.role }.map { it.text })
        addFrom(all.filter { it.key != dialogue.key }.flatMap { d -> d.lines.filter { it.role == line.role }.map { it.text } })
        val opts = (wrong + line.text).shuffled(random)
        return opts to opts.indexOf(line.text)
    }
}
