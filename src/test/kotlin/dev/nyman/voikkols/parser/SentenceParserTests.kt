package dev.nyman.voikkols.parser

import dev.nyman.voikkols.LANGUAGE
import dev.nyman.voikkols.Spellchecker
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.puimula.libvoikko.Voikko
import java.io.File
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SentenceParserTests {
    val voikko = Voikko(LANGUAGE)
    private val sentenceParser = SentenceParser(voikko)
    private val spellChecker = Spellchecker(voikko)

    private lateinit var text: String

    @BeforeAll
    fun setup() {
        text = File("src/test/resources/test.txt").readText()
    }

    @Test
    fun parse() {
        val expected = listOf(
            Sentence(
                "This is a test file.",
                Position(0, 0),
            ),
            Sentence(
                "It's only purpose is to be tested.",
                Position(2, 0),
            ),
            Sentence(
                "It also contains\na sentence,\nwhich spans\nmultiple lines.",
                Position(4, 0),
            ),
            Sentence(
                "There can also be multiple sentences.",
                Position(9, 0),
            ),
            Sentence(
                "On a single line.",
                Position(9, 38),
            ),
            Sentence(
                "Sentence\nwith linebreak.",
                Position(11, 0),
            ),
            Sentence(
                "Another sentence after linebreak sentence.",
                Position(12, 16),
            ),
        )

        val sentences = sentenceParser.parse(text)

        assertEquals(expected, sentences)
    }

    @Test
    fun end() {
        val expected = listOf(
            Position(0, 19),
            Position(2, 33),
            Position(7, 14),
            Position(9, 36),
            Position(9, 54),
            Position(12, 14),
            Position(12, 57),
        )

        val got = sentenceParser.parse(text).map { it.end }

        assertEquals(expected, got)
    }

    @Test
    fun ullakolla() {
        val parsed = sentenceParser.parse("Ullakolla on hiiri joka juoksee karkuun.")

        val expected = Range(
            Position(0, 10),
            Position(0, 30),
        )
        val grammarError = spellChecker.checkGrammar(parsed.first())
        val got = grammarError.first.toDiagnostic(grammarError.second)

        assertEquals(expected, got.first().range)
    }

    @Test
    fun ullakolla1() {
        val parsed = sentenceParser.parse("Ullakolla\non hiiri joka juoksee karkuun.")

        val expected = Range(
            Position(1, 0),
            Position(1, 20),
        )
        val grammarError = spellChecker.checkGrammar(parsed.first())
        val got = grammarError.first.toDiagnostic(grammarError.second)

        assertEquals(expected, got.first().range)
    }
}