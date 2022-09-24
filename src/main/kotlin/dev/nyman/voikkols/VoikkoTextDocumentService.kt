package dev.nyman.voikkols

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.puimula.libvoikko.Voikko

class VoikkoTextDocumentService(val server: VoikkoLanguageServer) : TextDocumentService {
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
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        val document = params.textDocument
        documents.remove(document.uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        val document = documents[params.textDocument.uri] ?: return
        val words = wordParser.parse(document.text)
        val sentences = sentenceParser.parse(document.text)

        val invalidWords = words.filterNot(spellchecker::checkSpelling)
        val grammarErrors = sentences.map(spellchecker::checkGrammar).filter { it.second.isNotEmpty() }

        val wordDiagnostics = invalidWords.map { it.toDiagnostic() }
        val sentenceDiagnostics = grammarErrors.flatMap { it.first.toDiagnostic(it.second) }

        val diagnostics = wordDiagnostics + sentenceDiagnostics

        server.client?.publishDiagnostics(PublishDiagnosticsParams(document.uri, diagnostics, document.version))
    }

    private fun updateDocument(document: TextDocumentItem, params: DidChangeTextDocumentParams): TextDocumentItem {
        document.version = params.textDocument.version
        document.text = params.contentChanges.first().text
        return document
    }

}
