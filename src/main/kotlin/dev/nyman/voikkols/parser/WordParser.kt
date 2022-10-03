package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.TokenType
import org.puimula.libvoikko.Voikko

class WordParser(private val voikko: Voikko) : Parser<List<Word>> {
    override fun parse(text: String): List<Word> {
        val lines = text.lines()
        return lines.flatMapIndexed(this::parseLines)
    }

    private fun parseLines(lineNumber: Int, line: String): List<Word> {
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

data class Word(val text: String, val range: Range) {
    fun toDiagnostic(): Diagnostic {
        return Diagnostic(range, "'$text': mahdollinen kirjoitusvirhe", DiagnosticSeverity.Hint, "voikko")
    }
}
