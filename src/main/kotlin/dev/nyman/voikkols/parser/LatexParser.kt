package dev.nyman.voikkols.parser

import ca.uqac.lif.textidote.`as`.AnnotatedString
import ca.uqac.lif.textidote.`as`.Position
import ca.uqac.lif.textidote.`as`.Range
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner
import org.eclipse.lsp4j.Range as LspRange
import org.eclipse.lsp4j.Position as LspPosition
import java.util.*

object LatexParser {
    private val cleaner = LatexCleaner()

    fun parse(text: String): AnnotatedString {
        val annotatedString = AnnotatedString.read(Scanner(text))
        return cleaner.clean(annotatedString)
    }

    fun mapToOriginal(string: AnnotatedString, sentence: Sentence): LspRange? {
        val range = string.map[Range(
            Position(sentence.start.line, sentence.start.character), Position(sentence.end.line, sentence.end.character)
        )] ?: return null

        return LspRange(
            LspPosition(range.start.line, range.start.column), LspPosition(range.end.line, range.end.column)
        )
    }

    fun mapToOriginal(string: AnnotatedString, word: Word): LspRange? {
        val range = string.map.keys.find { it.contains(word.range.toRange()) } ?: return null
        return range.toLspRange()
    }

    private fun Range.contains(range: Range) =
        this.start.line <= range.start.line && this.start.column <= range.start.column &&
                range.end.line <= this.end.line && range.end.column <= this.end.column

    private fun LspPosition.toPosition() = Position(line, character)

    private fun LspRange.toRange() = Range(
        start.toPosition(), end.toPosition()
    )

    private fun Position.toLspPosition() = LspPosition(line, column)

    private fun Range.toLspRange() = LspRange(
        start.toLspPosition(), end.toLspPosition()
    )
}