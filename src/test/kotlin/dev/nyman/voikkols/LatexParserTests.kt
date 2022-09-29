package dev.nyman.voikkols

import dev.nyman.voikkols.parser.LatexParser
import dev.nyman.voikkols.parser.SentenceParser
import dev.nyman.voikkols.parser.WordParser
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import org.puimula.libvoikko.Voikko
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LatexParserTests {
    private val voikko = Voikko(LANGUAGE)
    private val wordParser = WordParser(voikko)
    private val sentenceParser = SentenceParser(voikko)

    @Test
    fun mapWordCorrectly() {
        val file = File("src/test/resources/test.tex")
        val cleaned = LatexParser.parse(file.readText())
        val parsed = wordParser.parse(cleaned.toString())
        val expected = listOf(
            Range(
                Position(
                    4, 0
                ),
                Position(
                    4, 19
                )
            ),
            Range(
                Position(
                    6, 0
                ),
                Position(
                    6, 33
                )
            ),

            )
        val got = parsed.map { LatexParser.mapToOriginal(cleaned, it) }
        assertEquals(expected, got)
    }

    @Test
    fun mapSentenceCorrectly() {
        val file = File("src/test/resources/test.tex")
        val cleaned = LatexParser.parse(file.readText())
        val parsed = sentenceParser.parse(cleaned.toString())
        val expected = listOf(
            Range(
                Position(
                    4, 0
                ),
                Position(
                    4, 19
                )
            ),
            Range(
                Position(
                    6, 0
                ),
                Position(
                    6, 33
                )
            ),

        )
        val got = parsed.map { LatexParser.mapToOriginal(cleaned, it) }
        assertEquals(expected, got)
    }
}