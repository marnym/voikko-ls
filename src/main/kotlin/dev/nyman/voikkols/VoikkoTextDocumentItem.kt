package dev.nyman.voikkols

import ca.uqac.lif.textidote.`as`.AnnotatedString
import dev.nyman.voikkols.parser.LatexParser
import dev.nyman.voikkols.parser.Parser
import dev.nyman.voikkols.parser.Sentence
import dev.nyman.voikkols.parser.Word
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.TextDocumentItem

typealias ParseResult = Pair<List<Word>, List<Sentence>>

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

    open fun parse(
        wordParser: Parser<List<Word>>,
        sentenceParser: Parser<List<Sentence>>
    ): ParseResult = Pair(
        wordParser.parse(text),
        sentenceParser.parse(text),
    )

    open fun diagnostics(
        wordParser: Parser<List<Word>>,
        sentenceParser: Parser<List<Sentence>>,
        spellchecker: Spellchecker,
    ): List<Diagnostic> {
        val (words, sentences) = parse(wordParser, sentenceParser)

        val invalidWords = words.filterNot(spellchecker::checkSpelling)
        val grammarErrors = sentences.map(spellchecker::checkGrammar).filter { it.second.isNotEmpty() }

        val wordDiagnostics = invalidWords.map { it.toDiagnostic() }
        val sentenceDiagnostics = grammarErrors.flatMap { it.first.toDiagnostic(it.second) }

        return wordDiagnostics + sentenceDiagnostics
    }
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

    override fun parse(
        wordParser: Parser<List<Word>>,
        sentenceParser: Parser<List<Sentence>>
    ): ParseResult {
        val string = LatexParser.parse(text)
        annotatedString = string
        return Pair(
            wordParser.parse(string.toString()),
            sentenceParser.parse(string.toString())
        )
    }

    override fun diagnostics(
        wordParser: Parser<List<Word>>,
        sentenceParser: Parser<List<Sentence>>,
        spellchecker: Spellchecker
    ): List<Diagnostic> {
        val combined = super.diagnostics(wordParser, sentenceParser, spellchecker)
        for (diagnostic in combined)
            diagnostic.range = LatexParser.mapToSource(annotatedString, diagnostic.range)

        return combined
    }
}
