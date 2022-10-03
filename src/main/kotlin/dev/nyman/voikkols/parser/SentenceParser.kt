package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.GrammarError
import org.puimula.libvoikko.Voikko

class SentenceParser(private val voikko: Voikko) : Parser<List<Sentence>> {
    override fun parse(text: String): List<Sentence> {
        val sentences = voikko.sentences(text)
        var characterPosition = 0
        var lineNumber = 0
        return sentences.map { sentence ->
            val newSentence = Sentence(
                sentence.text.trim(),
                Position(lineNumber, characterPosition)
            )

            characterPosition += sentence.text.split("\n").last().length
            lineNumber += sentence.text.count { it == '\n' }
            if (sentence.text.last() == '\n') characterPosition = 0

            newSentence
        }
    }
}

data class Sentence(val text: String, val start: Position) {
    val end = end()

    fun toDiagnostic(grammarErrors: List<GrammarError>): List<Diagnostic> {
        return grammarErrors.map { grammarError ->
            val errorStart = errorStartPos(grammarError)
            Diagnostic(
                Range(
                    errorStart,
                    errorEndPos(grammarError, errorStart),
                ),
                grammarError.shortDescription,
                DiagnosticSeverity.Hint,
                "voikko"
            )
        }
    }

    private fun errorStartPos(grammarError: GrammarError): Position {
        val beforeStartPos = text.take(grammarError.startPos + newLinesBefore(grammarError.startPos))
        val line = start.line + beforeStartPos.count { it == '\n' }
        val charPosition =
            if (line != start.line) beforeStartPos.split("\n").last().length
            else beforeStartPos.length

        return Position(line, charPosition)
    }

    private fun errorEndPos(grammarError: GrammarError, errorStart: Position): Position {
        val endPos = grammarError.startPos + grammarError.errorLen
        val beforeEndPos = text.slice(grammarError.startPos + newLinesBefore(grammarError.startPos) .. endPos + newLinesBefore(endPos))
        val line = errorStart.line + beforeEndPos.count { it == '\n' }
        val charPosition =
            if (line != errorStart.line) beforeEndPos.split("\n").last().length
            else errorStart.character + grammarError.errorLen
        return Position(line, charPosition)
    }

    private fun end(): Position {
        val split = text.split("\n")
        return if (split.size == 1) {
            Position(start.line, start.character + text.length - 1)
        } else {
            Position(
                start.line + split.size - 1,
                0 + split.last().length - 1
            )
        }
    }

    private fun newLinesBefore(charPos: Int): Int {
        val newLines = text
            .mapIndexed { index, c -> Pair(index, c) }
            .filter { it.second == '\n' }

        return newLines.count { it.first <= charPos }
    }
}
