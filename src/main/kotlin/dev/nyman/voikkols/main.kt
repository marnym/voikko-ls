package dev.nyman.voikkols

import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.InputStream
import java.io.OutputStream

fun main() {
    val server = VoikkoLanguageServer()
    server.start(System.`in`, System.out)
}

fun VoikkoLanguageServer.start(input: InputStream, output: OutputStream) {
    val launcher = LSPLauncher.createServerLauncher(this, input, output)
    val startListening = launcher.startListening()
    connect(launcher.remoteProxy)
    startListening.get()
}
