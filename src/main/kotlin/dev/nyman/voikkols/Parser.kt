package dev.nyman.voikkols

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.GrammarError
import org.puimula.libvoikko.TokenType
import org.puimula.libvoikko.Voikko

data class Word(val text: String, val range: Range) {
    fun toDiagnostic(): Diagnostic {
        return Diagnostic(range, "Invalid spelling: $text", DiagnosticSeverity.Error, "voikko")
    }
}

data class Sentence(val text: String, val start: Position) {
    fun toDiagnostic(grammarErrors: List<GrammarError>): List<Diagnostic> {
        return grammarErrors.map {
            val errorStart = start.character + it.startPos
            val errorEnd = errorStart + it.errorLen
            Diagnostic(
                Range(
                    Position(start.line, errorStart),
                    Position(start.line, errorEnd)
                ),
                "Invalid grammar: ${it.shortDescription}",
                DiagnosticSeverity.Warning,
                "voikko"
            )
        }
    }
}

abstract class Parser<T> {
    fun parse(text: String): List<T> {
        val lines = text.lines()
        return lines.flatMapIndexed(this::parseLines)
    }

    abstract fun parseLines(lineNumber: Int, line: String): List<T>
}

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