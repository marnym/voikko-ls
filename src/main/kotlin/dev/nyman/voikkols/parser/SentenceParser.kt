package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Position
import org.puimula.libvoikko.Voikko

class SentenceParser(private val voikko: Voikko) : Parser<Sentence>() {
    override fun parseLines(lineNumber: Int, line: String): List<Sentence> {
        val sentences = voikko.sentences(line)
        var characterPosition = 0
        return sentences.map {
            val sentence = Sentence(
                it.text.trim(),
                Position(lineNumber, characterPosition),
            )
            characterPosition += it.text.length
            sentence
        }
    }

}
