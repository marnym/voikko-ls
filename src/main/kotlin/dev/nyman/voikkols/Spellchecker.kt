package dev.nyman.voikkols

import dev.nyman.voikkols.parser.Word
import org.puimula.libvoikko.GrammarError
import org.puimula.libvoikko.Voikko

const val LANGUAGE = "fi"
val IGNORED_CODES = listOf(2, 9)

class Spellchecker(private val voikko: Voikko) {
    fun checkGrammar(text: String): List<GrammarError> =
        voikko.grammarErrors(text, LANGUAGE)
            .filterNot { IGNORED_CODES.contains(it.errorCode) }

    fun checkSpelling(word: Word): Boolean = voikko.spell(word.text)
}