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

    open fun text(): String = text

    open fun update(params: DidChangeTextDocumentParams): VoikkoTextDocumentItem {
        version = params.textDocument.version
        text = params.contentChanges.first().text
        return this
    }

    open fun diagnostics(spellingErrors: List<Word>, grammarErrors: List<GrammarError>): List<Diagnostic> {
        val spellingDiagnostics = spellingErrors.map { it.toDiagnostic() }
        val grammarDiagnostics = grammarErrors.map(this::toDiagnostic)

        return spellingDiagnostics + grammarDiagnostics
    }

    fun charPosToPosition(charPos: Int): Position {
        val beforeStart = text().take(charPos)
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
) : VoikkoTextDocumentItem(uri, languageId, version, text) {
    private lateinit var annotatedString: AnnotatedString

    constructor(
        textDocumentItem: VoikkoTextDocumentItem,
    ) : this(
        textDocumentItem.uri,
        textDocumentItem.languageId,
        textDocumentItem.version,
        textDocumentItem.text,
    ) {
        annotatedString = LatexParser.parse(text)
    }

    constructor(
        textDocumentItem: TextDocumentItem,
    ) : this(
        textDocumentItem.uri,
        textDocumentItem.languageId,
        textDocumentItem.version,
        textDocumentItem.text,
    ) {
        annotatedString = LatexParser.parse(text)
    }

    override fun text(): String = annotatedString.toString()

    override fun update(params: DidChangeTextDocumentParams): VoikkoTextDocumentItem {
        super.update(params)
        annotatedString = LatexParser.parse(text)
        return this
    }

    override fun diagnostics(spellingErrors: List<Word>, grammarErrors: List<GrammarError>): List<Diagnostic> {
        val combined = super.diagnostics(spellingErrors, grammarErrors)
        for (diagnostic in combined)
            diagnostic.range = LatexParser.mapToSource(annotatedString, diagnostic.range)

        return combined
    }
}
