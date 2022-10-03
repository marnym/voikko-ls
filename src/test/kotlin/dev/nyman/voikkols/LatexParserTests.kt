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
                    4,
                    0,
                ),
                Position(
                    4,
                    4,
                ),
            ),
            Range(
                Position(
                    4,
                    5,
                ),
                Position(
                    4,
                    7,
                ),
            ),
            Range(
                Position(
                    4,
                    8,
                ),
                Position(
                    4,
                    9,
                ),
            ), Range(
                Position(
                    4,
                    10,
                ),
                Position(
                    4,
                    14,
                ),
            ), Range(
                Position(
                    4,
                    15,
                ),
                Position(
                    4,
                    19
                ),
            ), Range(
                Position(
                    6,
                    0
                ),
                Position(
                    6,
                    4
                ),
            ), Range(
                Position(
                    6,
                    5
                ),
                Position(
                    6,
                    9
                ),
            ), Range(
                Position(
                    6,
                    10
                ),
                Position(
                    6,
                    17
                ),
            ), Range(
                Position(
                    6,
                    18
                ),
                Position(
                    6,
                    20
                ),
            ), Range(
                Position(
                    6,
                    21
                ),
                Position(
                    6,
                    23
                ),
            ), Range(
                Position(
                    6,
                    24
                ),
                Position(
                    6,
                    26
                ),
            ), Range(
                Position(
                    6,
                    27
                ),
                Position(
                    6,
                    33
                ),
            ), Range(
                Position(
                    8,
                    0
                ),
                Position(
                    8,
                    2
                ),
            ), Range(
                Position(
                    8,
                    3
                ),
                Position(
                    8,
                    7
                ),
            ), Range(
                Position(
                    8,
                    8
                ),
                Position(
                    8,
                    16
                ),
            ), Range(
                Position(
                    8,
                    17
                ),
                Position(
                    8,
                    18
                ),
            ), Range(
                Position(
                    8,
                    19
                ),
                Position(
                    8,
                    27
                ),
            ), Range(
                Position(
                    9,
                    0
                ),
                Position(
                    9,
                    5
                ),
            ), Range(
                Position(
                    9,
                    6
                ),
                Position(
                    9,
                    11
                ),
            ), Range(
                Position(
                    9,
                    12
                ),
                Position(
                    9,
                    20
                ),
            ), Range(
                Position(
                    9,
                    21
                ),
                Position(
                    9,
                    26
                ),
            )
        )
        val got = parsed.map { LatexParser.mapToSource(cleaned, it.range) }
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
        val got = parsed.map { LatexParser.mapToSource(cleaned, Range(it.start, it.end)) }
        assertEquals(expected, got)
    }
}