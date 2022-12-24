package org.hildan.aoc.day3

import org.hildan.aoc.client.inputLines

fun main() {
    val inputLines = inputLines(day = 3)
    println(inputLines.sumOf { findDuplicateType(it).priority() })
    println(inputLines.chunked(3).sumOf { findCommonType(it).priority() })
}

private fun findDuplicateType(contents: String): Char {
    val (left, right) = contents.splitInHalves().map { it.toSet() }
    return (left intersect right).single()
}

private fun String.splitInHalves() = chunked(length / 2)

private fun findCommonType(rucksacks: List<String>): Char {
    return rucksacks.map { it.toSet() }.reduce { acc, bag -> acc intersect bag }.single()
}

private fun Char.priority(): Int = if (isLowerCase()) this - 'a' + 1 else this - 'A' + 27
