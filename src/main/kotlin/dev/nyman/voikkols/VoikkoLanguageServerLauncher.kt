package dev.nyman.voikkols

import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.InputStream
import java.io.OutputStream

fun main() {
    startServer(System.`in`, System.out)
}

fun startServer(input: InputStream, output: OutputStream) {
    val server = VoikkoLanguageServer()
    val launcher = LSPLauncher.createServerLauncher(server, input, output)
    server.connect(launcher.remoteProxy)
    launcher.startListening().get()
}