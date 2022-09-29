package dev.nyman.voikkols

import dev.nyman.voikkols.parser.SentenceParser
import org.junit.jupiter.api.Test
import org.puimula.libvoikko.Voikko

class ParserTests {
    val voikko = Voikko(LANGUAGE)
    val sentenceParser = SentenceParser(voikko)
    val spellchecker = Spellchecker(voikko)

    @Test
    fun test() {
        val sentence = "Kissa vai ei? Ullakolla on hiiri joka juoksee karkuun. Vaikea sanoa."
        val sentences = SentenceParser(voikko).parse(sentence)
        val sentenceErrors = sentences.map(spellchecker::checkGrammar).filter { it.second.isNotEmpty() }
        println(sentenceErrors)
    }

}