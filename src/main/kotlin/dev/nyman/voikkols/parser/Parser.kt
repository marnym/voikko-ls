package dev.nyman.voikkols.parser

interface Parser<Out> {
    fun parse(text: String): Out
}
