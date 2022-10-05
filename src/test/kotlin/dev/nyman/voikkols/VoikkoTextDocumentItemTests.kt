package dev.nyman.voikkols

import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VoikkoTextDocumentItemTests {
    @Test
    fun simpleSentence() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "This is a sentence.")
        val expected = Position(0, 8)
        val got = voikkoTextDocumentItem.charPosToPosition(8)

        assertEquals(expected, got)
    }

    @Test
    fun newLineBeforeSentence() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "\nThis is a sentence.")
        val expected = Position(1, 8)
        val got = voikkoTextDocumentItem.charPosToPosition(9)

        assertEquals(expected, got)
    }

    @Test
    fun newLineAfterSentence() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "This is a sentence.\n")
        val expected = Position(0, 8)
        val got = voikkoTextDocumentItem.charPosToPosition(8)

        assertEquals(expected, got)
    }

    @Test
    fun twoNewLinesBeforeSentence() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "\n\nThis is a sentence.")
        val expected = Position(2, 8)
        val got = voikkoTextDocumentItem.charPosToPosition(10)

        assertEquals(expected, got)
    }

    @Test
    fun twoSentences() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "Another sentence before. This is a sentence.")
        val expected = Position(0, 33)
        val got = voikkoTextDocumentItem.charPosToPosition(33)

        assertEquals(expected, got)
    }

    @Test
    fun twoSentencesOnSeparateLines() {
        val voikkoTextDocumentItem = VoikkoTextDocumentItem("", "", 0, "Another sentence before.\nThis is a sentence.")
        val expected = Position(1, 8)
        val got = voikkoTextDocumentItem.charPosToPosition(33)

        assertEquals(expected, got)

    }
}