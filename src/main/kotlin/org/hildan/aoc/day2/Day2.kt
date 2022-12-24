package org.hildan.aoc.day2

import org.hildan.aoc.client.inputLines

fun main() {
    val inputLines = inputLines(day = 2)
    println(inputLines.sumOf { matchScorePart1(it) })
    println(inputLines.sumOf { matchScorePart2(it) })
}

private fun matchScorePart1(encodedMatchLine: String): Int {
    val (opponentMoveCode, myMoveCode) = encodedMatchLine.split(" ")
    val opponentMove = opponentMoveCode.toMove()
    val myMove = myMoveCode.toMove()
    return myOutcomePoints(opponentMove, myMove).points + myMove.intrinsicPoints
}

private fun myOutcomePoints(opponentMove: Move, myMove: Move): Outcome = when (opponentMove) {
    Move.ROCK -> when (myMove) {
        Move.ROCK -> Outcome.DRAW
        Move.PAPER -> Outcome.VICTORY
        Move.SCISSORS -> Outcome.DEFEAT
    }

    Move.PAPER -> when (myMove) {
        Move.ROCK -> Outcome.DEFEAT
        Move.PAPER -> Outcome.DRAW
        Move.SCISSORS -> Outcome.VICTORY
    }

    Move.SCISSORS -> when (myMove) {
        Move.ROCK -> Outcome.VICTORY
        Move.PAPER -> Outcome.DEFEAT
        Move.SCISSORS -> Outcome.DRAW
    }
}

private fun matchScorePart2(encodedMatchLine: String): Int {
    val (opponentMoveCode, outcomeCode) = encodedMatchLine.split(" ")
    val opponentMove = opponentMoveCode.toMove()
    val outcome = Outcome.fromCode(outcomeCode)
    return findMove(opponentMove, outcome).intrinsicPoints + outcome.points
}

private fun findMove(opponentMove: Move, outcome: Outcome): Move = when (opponentMove) {
    Move.ROCK -> when (outcome) {
        Outcome.VICTORY -> Move.PAPER
        Outcome.DRAW -> Move.ROCK
        Outcome.DEFEAT -> Move.SCISSORS
    }

    Move.PAPER -> when (outcome) {
        Outcome.VICTORY -> Move.SCISSORS
        Outcome.DRAW -> Move.PAPER
        Outcome.DEFEAT -> Move.ROCK
    }

    Move.SCISSORS -> when (outcome) {
        Outcome.VICTORY -> Move.ROCK
        Outcome.DRAW -> Move.SCISSORS
        Outcome.DEFEAT -> Move.PAPER
    }
}

private enum class Move(val intrinsicPoints: Int) {
    ROCK(1),
    PAPER(2),
    SCISSORS(3),
}

// letters mean moves in part one, but outcomes in part 2
private fun String.toMove(): Move = when (this) {
    "A", "X" -> Move.ROCK
    "B", "Y" -> Move.PAPER
    "C", "Z" -> Move.SCISSORS
    else -> error("unknown move '$this'")
}

private enum class Outcome(val code: String, val points: Int) {
    DEFEAT("X", 0),
    DRAW("Y", 3),
    VICTORY("Z", 6);

    companion object {
        private val outcomeByCode = values().associateBy { it.code }

        fun fromCode(code: String) = outcomeByCode[code] ?: error("unknown outcome code '$code'")
    }
}
