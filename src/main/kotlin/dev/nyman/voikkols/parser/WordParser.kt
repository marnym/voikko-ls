package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.TokenType
import org.puimula.libvoikko.Voikko

class WordParser(private val voikko: Voikko) : Parser<Word>() {
    override fun parseLines(lineNumber: Int, line: String): List<Word> {
        val tokens = voikko.tokens(line)
        val words = tokens
            .filter { it.type == TokenType.WORD }
            .map {
                Word(
                    it.text,
                    Range(Position(lineNumber, it.startOffset), Position(lineNumber, it.endOffset))
                )
            }

        return words
    }
}
