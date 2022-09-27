package dev.nyman.voikkols.parser

import ca.uqac.lif.textidote.`as`.AnnotatedString
import ca.uqac.lif.textidote.cleaning.latex.LatexCleaner
import java.util.*

object LatexParser : Parser<String>() {
    private val cleaner = LatexCleaner()

    /**
     * @return Lines from a LaTeX file cleaned
     */
    override fun parse(text: String): List<String> {
        val annotatedString = AnnotatedString.read(Scanner(text))
        return cleaner.clean(annotatedString).lines
    }

    override fun parseLines(lineNumber: Int, line: String) = emptyList<String>()
}