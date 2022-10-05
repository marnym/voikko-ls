package dev.nyman.voikkols

import dev.nyman.voikkols.parser.*
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.puimula.libvoikko.Voikko

class VoikkoTextDocumentService(private val server: VoikkoLanguageServer) : TextDocumentService {
    private val documents: MutableMap<String, VoikkoTextDocumentItem> = HashMap()
    private val voikko = Voikko(LANGUAGE)
    private val wordParser = WordParser(voikko)
    private val spellchecker = Spellchecker(voikko)

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val document = params.textDocument
        val voikkoDocument = when (document.languageId) {
            "tex", "latex" -> VoikkoLatexDocumentItem(document)
            else -> VoikkoTextDocumentItem(document)
        }
        documents[document.uri] = voikkoDocument
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val document = documents[uri] ?: return

        val updatedDocument = document.update(params)
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

    fun diagnostics(documentItem: VoikkoTextDocumentItem): List<Diagnostic> {
        val spellingErrors = wordParser.parse(documentItem.text()).filterNot(spellchecker::checkSpelling)
        val grammarErrors = spellchecker.checkGrammar(documentItem.text())

        return documentItem.diagnostics(spellingErrors, grammarErrors)
    }

}
