package dev.nyman.voikkols.parser

import ca.uqac.lif.textidote.`as`.AnnotatedString
import ca.uqac.lif.textidote.`as`.Position
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner
import java.util.*
import org.eclipse.lsp4j.Position as LspPosition
import org.eclipse.lsp4j.Range as LspRange

object LatexParser : Parser<AnnotatedString> {
    private val cleaner = LatexCleaner().apply {
        setIgnoreBeforeDocument(false)
    }


    override fun parse(text: String): AnnotatedString {
        val annotatedString = AnnotatedString.read(Scanner(text))
        return cleaner.clean(annotatedString)
    }

    fun mapToSource(string: AnnotatedString, range: LspRange): LspRange {
        val sourceStart = string.getSourcePosition(range.start.toPosition()).toLspPosition()
        val sourceEnd = string.getSourcePosition(range.end.toPosition()).toLspPosition()
        return LspRange(sourceStart, sourceEnd)
    }

    private fun LspPosition.toPosition() = Position(line, character)

    private fun Position.toLspPosition() = LspPosition(line, column)
}