package org.hildan.aoc.day9

import org.hildan.aoc.client.inputLines
import kotlin.math.abs

fun main() {
    val moves = inputLines(day = 9).map { it.parseMove() }
    println(part1(moves))
    println(part2(moves))
}

private fun part1(moves: List<Pair<Direction, Int>>): Int {
    val state = StatePart1()
    moves.forEach { (dir, qty) ->
        repeat(qty) { state.move(dir) }
    }
    return state.nVisitedTailPositions()
}

private fun part2(moves: List<Pair<Direction, Int>>): Int {
    val state = StatePart2()
    moves.forEach { (dir, qty) ->
        repeat(qty) { state.move(dir) }
    }
    return state.nVisitedTailPositions()
}

private fun String.parseMove(): Pair<Direction, Int> {
    val (dirStr, qtyStr) = split(" ")
    return Direction.valueOf(dirStr) to qtyStr.toInt()
}

class StatePart1 {
    private val headPos = Position(0, 0)
    private val tailPos = Position(0, 0)
    private val visitedTailPositions = mutableSetOf(tailPos)

    fun move(dir: Direction) {
        headPos.move(dir)
        tailPos.keepCloseTo(headPos)
        visitedTailPositions.add(tailPos.copy())
    }

    fun nVisitedTailPositions() = visitedTailPositions.size
}

class StatePart2 {
    private var rope = List(10) { Position(0, 0) }
    private val visitedTailPositions = mutableSetOf(rope.last())

    fun move(dir: Direction) {
        rope.first().move(dir)
        rope.windowed(2) { (prev, next) ->
            next.keepCloseTo(prev)
        }
        visitedTailPositions.add(rope.last().copy())
    }

    fun nVisitedTailPositions() = visitedTailPositions.size
}

data class Position(var x: Int, var y: Int) {

    fun move(dir: Direction) {
        x += dir.xOffset
        y += dir.yOffset
    }

    fun keepCloseTo(pos: Position) {
        if (distanceTo(pos) <= 1) {
            return
        }
        val xDiff = pos.x - x
        val yDiff = pos.y - y
        x = when {
            xDiff > 0 -> x + 1
            xDiff < 0 -> x - 1
            else -> x
        }
        y = when {
            yDiff > 0 -> y + 1
            yDiff < 0 -> y - 1
            else -> y
        }
    }

    private fun distanceTo(pos: Position) = maxOf(abs(pos.x - x), abs(pos.y - y))
}

enum class Direction(val xOffset: Int, val yOffset: Int) {
    U(0, +1),
    D(0, -1),
    L(-1, 0),
    R(+1, 0),
}
