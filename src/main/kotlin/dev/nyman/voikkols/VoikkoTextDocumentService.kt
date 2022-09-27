package dev.nyman.voikkols

import dev.nyman.voikkols.parser.*
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.puimula.libvoikko.Voikko

class VoikkoTextDocumentService(private val server: VoikkoLanguageServer) : TextDocumentService {
    private val documents: MutableMap<String, TextDocumentItem> = HashMap()
    private val voikko = Voikko(LANGUAGE)
    private val wordParser = WordParser(voikko)
    private val sentenceParser = SentenceParser(voikko)
    private val spellchecker = Spellchecker(voikko)

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val document = params.textDocument
        documents[document.uri] = document
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val document = documents[uri] ?: return

        val updatedDocument = updateDocument(document, params)
        documents[updatedDocument.uri] = updatedDocument

        server.client?.publishDiagnostics(
            PublishDiagnosticsParams(
                updatedDocument.uri,
                diagnostics(updatedDocument),
                updatedDocument.version
            )
        )
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        val document = params.textDocument
        documents.remove(document.uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        val document = documents[params.textDocument.uri] ?: return

        server.client?.publishDiagnostics(
            PublishDiagnosticsParams(
                document.uri,
                diagnostics(document),
                document.version
            )
        )
    }

    private fun updateDocument(document: TextDocumentItem, params: DidChangeTextDocumentParams): TextDocumentItem {
        document.version = params.textDocument.version
        document.text = params.contentChanges.first().text
        return document
    }

    private fun parse(document: TextDocumentItem): Pair<List<Word>, List<Sentence>> =
        when (document.languageId) {
            "latex", "tex" -> {
                val text = LatexParser.parse(document.text).joinToString("\n")
                Pair(wordParser.parse(text), sentenceParser.parse(text))
            }

            "text" -> Pair(wordParser.parse(document.text), sentenceParser.parse(document.text))


            else -> {
                server.client?.logMessage(
                    MessageParams(
                        MessageType.Error,
                        "Unknown language identifier ${document.languageId}"
                    )
                )
                Pair(emptyList(), emptyList())
            }
        }

    private fun diagnostics(document: TextDocumentItem): List<Diagnostic> {
        val (words, sentences) = parse(document)

        val invalidWords = words.filterNot(spellchecker::checkSpelling)
        val grammarErrors = sentences.map(spellchecker::checkGrammar).filter { it.second.isNotEmpty() }

        val wordDiagnostics = invalidWords.map { it.toDiagnostic() }
        val sentenceDiagnostics = grammarErrors.flatMap { it.first.toDiagnostic(it.second) }

        return wordDiagnostics + sentenceDiagnostics
    }
}
