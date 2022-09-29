package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.GrammarError

data class Word(val text: String, val range: Range) {
    fun toDiagnostic(): Diagnostic {
        return Diagnostic(range, "Invalid spelling: $text", DiagnosticSeverity.Error, "voikko")
    }
}

data class Sentence(val text: String, val start: Position) {
    val end = Position(start.line, start.character + text.length - 1)

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
    open fun parse(text: String): List<T> {
        val lines = text.lines()
        return lines.flatMapIndexed(this::parseLines)
    }

    protected abstract fun parseLines(lineNumber: Int, line: String): List<T>
}
