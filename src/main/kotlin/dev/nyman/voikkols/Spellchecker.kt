package dev.nyman.voikkols

import dev.nyman.voikkols.parser.Sentence
import dev.nyman.voikkols.parser.Word
import org.puimula.libvoikko.GrammarError
import org.puimula.libvoikko.Voikko

const val LANGUAGE = "fi"

class Spellchecker(private val voikko: Voikko) {
    fun checkGrammar(sentence: Sentence): Pair<Sentence, List<GrammarError>> =
        Pair(sentence, voikko.grammarErrors(sentence.text, LANGUAGE))

    fun checkSpelling(word: Word): Boolean = voikko.spell(word.text)
}