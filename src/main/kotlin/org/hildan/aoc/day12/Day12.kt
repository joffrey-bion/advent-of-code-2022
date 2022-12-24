package org.hildan.aoc.day12

import org.hildan.aoc.client.inputLines
import java.util.LinkedList

fun main() {
    val graph = inputLines(day = 12).filter { it.isNotEmpty() }.toGraph()
    println(part1(graph))
    println(part2(graph))
}

private fun part1(graph: Graph): Int {
    val s = graph.start
    return graph.shortestPathToEndFrom(s)
}

private fun part2(graph: Graph): Int {
    return graph.allStarts.minOf { graph.shortestPathToEndFrom(it) }
}

private fun Graph.shortestPathToEndFrom(start: Position): Int {
    val seen = mutableSetOf<Position>()
    val queue = LinkedList<State>()
    queue.add(State(start, 0))
    while (queue.isNotEmpty()) {
        val current = queue.poll()
        if (current.position.isEndNode) {
            return current.nStepsSoFar
        }
        val nextPositions = current.position.neighbours().filterNot { it in seen }
        seen.addAll(nextPositions)
        queue.addAll(nextPositions.map { State(position = it, nStepsSoFar = current.nStepsSoFar + 1) })
    }
    return Int.MAX_VALUE
}

private fun List<String>.toGraph(): Graph {
    val width = this[0].length
    val height = this.size
    val data = joinToString("")
    return Graph(data, width, height)
}

private data class State(val position: Position, val nStepsSoFar: Int)

private class Graph(
    val data: String,
    val width: Int,
    val height: Int,
) {
    val start: Position = Position(data.indexOf('S'))
    val allStarts = data.withIndex().filter { it.value == 'a' || it.value == 'S' }.map { Position(it.index) }

    fun Position.neighbours(): List<Position> = listOfNotNull(
        pos(row, col + 1),
        pos(row, col - 1),
        pos(row + 1, col),
        pos(row - 1, col),
    ).filter {
        it.elevation <= elevation + 1
    }

    val Position.isEndNode get() = data[index] == 'E'

    private val Position.elevation get() = when(val r = data[index]) {
        'S' -> 'a'
        'E' -> 'z'
        else -> r
    }
    private val Position.row get() = index / width
    private val Position.col get() = index % width

    private fun pos(row: Int, col: Int) = if (row in 0 until height && col in 0 until width) Position(width * row + col) else null
}

@JvmInline
private value class Position(val index: Int)
