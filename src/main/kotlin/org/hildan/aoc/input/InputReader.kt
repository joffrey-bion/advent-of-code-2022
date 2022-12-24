package org.hildan.aoc.input

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.*

private val inputsDir = Path("inputs").also { it.createDirectories() }

internal fun inputLines(day: Int, year: Int = 2022) = inputFile(day, year).readLines()

internal fun inputFile(day: Int, year: Int = 2022): Path {
    val formattedDay = String.format("%02d", day)
    val inputPath = inputsDir.resolve("input-$year-day-${formattedDay}.txt")
    if (!inputPath.exists()) {
        inputPath.writeText(fetchInputText(year, day))
    }
    return inputPath
}

private fun fetchInputText(year: Int, day: Int): String {
    val session = System.getenv("AOC_SESSION")
        ?: error("Please set env var AOC_SESSION to your session cookie value (puzzle input differ by user)")
    return URL("https://adventofcode.com/$year/day/$day/input")
        .openConnection()
        .apply { setRequestProperty("Cookie", "session=$session") }
        .getInputStream()
        .bufferedReader().readText()
}
