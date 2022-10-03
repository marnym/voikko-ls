package dev.nyman.voikkols.parser

interface Parser<T> {
    fun parse(text: String): T
}
