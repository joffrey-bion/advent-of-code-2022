package org.hildan.aoc.client

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.*

private val inputsDir = Path("inputs").also { it.createDirectories() }
private val answersDir = Path("answers").also { it.createDirectories() }

internal fun inputLines(day: Int, year: Int = 2022) = inputFile(day, year).readLines()

internal fun inputFile(day: Int, year: Int = 2022): Path {
    val formattedDay = String.format("%02d", day)
    val inputPath = inputsDir.resolve("input-$year-day-${formattedDay}.txt")
    if (!inputPath.exists()) {
        inputPath.writeText(AocClient.fetchInputText(year, day))
    }
    return inputPath
}

internal fun submittedCorrectAnswers(day: Int, year: Int = 2022): Answers {
    val formattedDay = String.format("%02d", day)
    val answerFiles = listOf(1, 2).map { part ->
        answersDir.resolve("answer-$year-day-${formattedDay}-part-$part.txt")
    }
    if (answerFiles.any { it.notExists() }) {
        val answers = AocClient.fetchMyAnswers(year, day)
        answers.part1?.let { answerFiles[0].writeText(it) }
        answers.part2?.let { answerFiles[1].writeText(it) }
    }
    return Answers(answerFiles[0].readText(), answerFiles[1].readText())
}

private fun AocClient.fetchInputText(year: Int, day: Int): String =
    fetchText("https://adventofcode.com/$year/day/$day/input")

private val puzzleAnswerRegex = Regex("""<p>Your puzzle answer was <code>(.+)</code>\.</p>""")

private fun AocClient.fetchMyAnswers(year: Int, day: Int): Answers {
    val html = fetchText("https://adventofcode.com/$year/day/$day")
    val answers = puzzleAnswerRegex.findAll(html).map { it.groupValues[1] }.toList()
    return Answers(answers.getOrNull(0), answers.getOrNull(1))
}

data class Answers(
    val part1: String?,
    val part2: String?,
)

private object AocClient {
    private val session = System.getenv("AOC_SESSION")
        ?: error("Please set env var AOC_SESSION to your session cookie value (puzzle input differ by user)")

    fun fetchText(url: String): String = URL(url)
        .openConnection()
        .apply { setRequestProperty("Cookie", "session=$session") }
        .getInputStream()
        .bufferedReader().readText()
}
