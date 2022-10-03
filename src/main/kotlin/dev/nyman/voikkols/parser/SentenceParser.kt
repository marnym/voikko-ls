package dev.nyman.voikkols.parser

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.puimula.libvoikko.GrammarError
import org.puimula.libvoikko.Voikko

class SentenceParser(private val voikko: Voikko) : Parser<List<Sentence>> {
    override fun parse(text: String): List<Sentence> {
        val lines = text.lines()
        val paragraphs = text.split("\n\n")
        val sentences = mutableListOf<Pair<Int, Sentence>>()

        for ((i, p) in paragraphs.withIndex()) {
            for (vs in voikko.sentences(p)) {
                if (sentences.isNotEmpty()) {
                    // General case
                    val previousEnd = sentences.last().second.end

                    val lineAfterPreviousEnd = lines[(previousEnd.line + 1).coerceAtMost(lines.size - 1)]
                    val startPos = if (i != sentences.last().first) {
                        // First sentence in paragraph
                        val startLine = previousEnd.line + 2
                        val startChar = p.takeWhile { it.isWhitespace() }.length
                        Position(startLine, startChar)
                    } else if (previousEnd.line == lines.size - 1 || lineAfterPreviousEnd.isEmpty()) {
                        val startLine = previousEnd.line
                        val startChar = previousEnd.character + lines[previousEnd.line].drop(previousEnd.character + 1)
                            .takeWhile { it.isWhitespace() }.length + 1
                        Position(startLine, startChar)
                    } else {
                        val startLine = previousEnd.line + 1
                        val startChar = 0
                        Position(startLine, startChar)
                    }

                    sentences.add(
                        Pair(
                            i,
                            Sentence(
                                vs.text,
                                startPos,
                            )
                        )
                    )
                } else {
                    // first case
                    sentences.add(
                        Pair(i, parseFirstSentence(paragraphs))
                    )
                }
            }
        }

        return sentences.map { it.second }
    }

    private fun parseFirstSentence(paragraphs: List<String>): Sentence {
        val emptyParagraphsInBeginning = paragraphs.takeWhile { it.isEmpty() }.size
        val whiteSpaceBeforeFirst =
            paragraphs.drop(emptyParagraphsInBeginning).first().takeWhile { !it.isLetterOrDigit() }
        val firstLine =
            if (emptyParagraphsInBeginning == 0) whiteSpaceBeforeFirst.count { it == '\n' } else emptyParagraphsInBeginning * 2
        val firstChar =
            whiteSpaceBeforeFirst.drop((whiteSpaceBeforeFirst.lastIndexOf('\n') + 1).coerceAtLeast(0)).length

        return Sentence(
            voikko.sentences(paragraphs.drop(emptyParagraphsInBeginning).first())
                .first().text.trim(),
            Position(firstLine, firstChar)
        )
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
        val beforeStartPos = text.take(grammarError.startPos)
        val line = start.line + beforeStartPos.count { it == '\n' }
        val charPosition =
            if (line != start.line) beforeStartPos.split("\n").last().length
            else beforeStartPos.length

        return Position(line, charPosition)
    }

    private fun errorEndPos(grammarError: GrammarError, errorStart: Position): Position {
        // FIXME
        val endPos = (grammarError.startPos + grammarError.errorLen).coerceAtMost(text.length - 1)
        val beforeEndPos = text.slice(grammarError.startPos..endPos)
        val line = errorStart.line + beforeEndPos.count { it == '\n' }
        val charPosition =
            if (line != errorStart.line) beforeEndPos.split("\n").last().trim().length
            else errorStart.character + grammarError.errorLen

        return Position(line, charPosition)
    }

    private fun end(): Position {
        val split = text.split("\n")
        return if (split.size == 1) Position(start.line, start.character + text.length - 1)
        else
            Position(
                start.line + split.size - 1,
                0 + split.last().length - 1
            )
    }
}
