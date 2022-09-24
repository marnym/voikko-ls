package dev.nyman.voikkols

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.services.TextDocumentService

class VoikkoTextDocumentService(server: VoikkoLanguageServer) : TextDocumentService {
    private val documents: MutableMap<String, TextDocumentItem> = HashMap()

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

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    private fun updateDocument(document: TextDocumentItem, params: DidChangeTextDocumentParams): TextDocumentItem {
        document.version = params.textDocument.version
        for (changes in params.contentChanges) {
            
        }
        document.text

        return document
    }

}
