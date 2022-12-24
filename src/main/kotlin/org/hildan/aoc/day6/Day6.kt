package org.hildan.aoc.day6

import org.hildan.aoc.client.inputFile
import kotlin.io.path.readText

fun main() {
    val input = inputFile(day = 6).readText()
    println(input.indexOfMarker(4))
    println(input.indexOfMarker(14))
}

private fun String.indexOfMarker(distinctSeqSize: Int): Int = asSequence()
    .withIndex()
    .windowed(distinctSeqSize)
    .first { window -> window.map { it.value }.areDistinct() }
    .last()
    .index + 1

private fun List<Char>.areDistinct() = distinct().size == size
