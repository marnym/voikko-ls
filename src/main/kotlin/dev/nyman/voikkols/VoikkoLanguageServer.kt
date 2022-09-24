package dev.nyman.voikkols

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class VoikkoLanguageServer : LanguageServer, LanguageClientAware {
    private val textDocumentService = VoikkoTextDocumentService(this)
    private val workspaceService = VoikkoWorkspaceService()
    private val notebookDocumentService = VoikkoNotebookDocumentService()
    var client: LanguageClient? = null
    private var errorCode = 1

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val initializeResult = InitializeResult(ServerCapabilities()).apply {
            capabilities.apply {
                codeActionProvider = Either.forLeft(true)
                textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
            }
        }

        return CompletableFuture.completedFuture(initializeResult)
    }

    override fun shutdown(): CompletableFuture<Any> {
        errorCode = 0
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        exitProcess(errorCode)
    }

    override fun getTextDocumentService() = textDocumentService

    override fun getWorkspaceService() = workspaceService

    override fun getNotebookDocumentService() = notebookDocumentService

    override fun connect(client: LanguageClient?) {
        this.client = client
    }
}
