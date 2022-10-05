package dev.nyman.voikkols

import ca.uqac.lif.textidote.`as`.AnnotatedString
import dev.nyman.voikkols.parser.*
import org.eclipse.lsp4j.*
import org.puimula.libvoikko.GrammarError

open class VoikkoTextDocumentItem(
    uri: String,
    languageId: String,
    version: Int,
    text: String,
) : TextDocumentItem(uri, languageId, version, text) {
    constructor(
        textDocumentItem: TextDocumentItem,
    ) : this(
        textDocumentItem.uri,
        textDocumentItem.languageId,
        textDocumentItem.version,
        textDocumentItem.text,
    )

    open fun parse(wordParser: WordParser): List<Word> = wordParser.parse(text)

    open fun diagnostics(wordParser: WordParser, spellchecker: Spellchecker): List<Diagnostic> {
        val words = parse(wordParser)

        val spellingErrors = words.filterNot(spellchecker::checkSpelling)
        val grammarErrors = spellchecker.checkGrammar(text)

        val spellingDiagnostics = spellingErrors.map { it.toDiagnostic() }
        val grammarDiagnostics = grammarErrors.map(this::toDiagnostic)

        return spellingDiagnostics + grammarDiagnostics
    }

    fun charPosToPosition(charPos: Int): Position {
        val beforeStart = text.take(charPos)
        val lineNumber = beforeStart.count { it == '\n' }
        val column = beforeStart.takeLastWhile { it != '\n' }.length
        return Position(lineNumber, column)
    }

    private fun toDiagnostic(grammarError: GrammarError): Diagnostic =
        Diagnostic(
            Range(
                charPosToPosition(grammarError.startPos),
                charPosToPosition(grammarError.startPos + grammarError.errorLen),
            ),
            grammarError.shortDescription,
            DiagnosticSeverity.Hint,
            "voikko",
        )
}

class VoikkoLatexDocumentItem(
    uri: String,
    languageId: String,
    version: Int,
    text: String,
    var annotatedString: AnnotatedString,
) : VoikkoTextDocumentItem(uri, languageId, version, text) {
    constructor(
        textDocumentItem: VoikkoTextDocumentItem,
        annotatedString: AnnotatedString,
    ) : this(
        textDocumentItem.uri,
        textDocumentItem.languageId,
        textDocumentItem.version,
        textDocumentItem.text,
        annotatedString,
    )

    constructor(
        textDocumentItem: TextDocumentItem,
        annotatedString: AnnotatedString,
    ) : this(
        textDocumentItem.uri,
        textDocumentItem.languageId,
        textDocumentItem.version,
        textDocumentItem.text,
        annotatedString,
    )

    override fun parse(wordParser: WordParser): List<Word> {
        val string = LatexParser.parse(text)
        annotatedString = string
        return wordParser.parse(string.toString())
    }

    override fun diagnostics(wordParser: WordParser, spellchecker: Spellchecker): List<Diagnostic> {
        val combined = super.diagnostics(wordParser, spellchecker)
        for (diagnostic in combined)
            diagnostic.range = LatexParser.mapToSource(annotatedString, diagnostic.range)

        return combined
    }
}
