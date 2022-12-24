package org.hildan.aoc.day23

import org.hildan.aoc.day23.Direction.*
import org.hildan.aoc.client.inputLines

fun main() {
    val lines = inputLines(day = 23)
    println(Map(lines).part1()) // 15898 too high
    println(Map(lines).part2()) // 1015 too low
}

private class Map(lines: List<String>) {
    private var directions = listOf(NORTH, SOUTH, WEST, EAST)

    private val elfPositions: MutableSet<Position> = lines.flatMapIndexedTo(mutableSetOf()) { row, line ->
        line.mapIndexedNotNull { col, c ->
            Position(row.toLong(), col.toLong()).takeIf { c == '#' }
        }
    }

    fun part1(): Long {
        repeat(10) {
            runTurn()
        }
        val minRow = elfPositions.minOf { it.row }
        val minCol = elfPositions.minOf { it.col }
        val maxRow = elfPositions.maxOf { it.row }
        val maxCol = elfPositions.maxOf { it.col }
        return (maxRow - minRow + 1) * (maxCol - minCol + 1) - elfPositions.size
    }

    fun part2(): Int {
        var i = 1
        while (runTurn()) {
            i++
        }
        return i
    }

    private fun runTurn(): Boolean {
        val proposedMoves = elfPositions.mapNotNull { it.proposeMove() }
        val effectiveMoves = proposedMoves.filterNonDuplicateTargets()
        effectiveMoves.forEach {
            elfPositions.remove(it.current)
            elfPositions.add(it.target)
        }
        rotateDirections()
        return effectiveMoves.isNotEmpty()
    }

    private fun Position.proposeMove(): Move? {
        if (!hasElfAround) {
            return null
        }
        return directions.firstNotNullOfOrNull { dir ->
            proposePosition(dir)?.let { Move(this, it) }
        }
    }

    private fun Position.proposePosition(dir: Direction): Position? {
        if (hasElfAroundDirection(dir)) {
            return null
        }
        return moved(dir)
    }

    private fun List<Move>.filterNonDuplicateTargets(): List<Move> {
        val counts = groupingBy { it.target }.eachCount()
        return filter { counts.getValue(it.target) == 1 }
    }

    private fun rotateDirections() {
        directions = directions.drop(1) + directions.take(1)
    }

    private fun Position.hasElfAroundDirection(dir: Direction): Boolean = neighboursInDirection(dir).any { it.hasElf }

    private val Position.hasElfAround: Boolean get() = neighbours.any { it.hasElf }
    private val Position.hasElf get() = this in elfPositions

    override fun toString(): String = buildString {
        val minRow = elfPositions.minOf { it.row }
        val minCol = elfPositions.minOf { it.col }
        val maxRow = elfPositions.maxOf { it.row }
        val maxCol = elfPositions.maxOf { it.col }
        for (row in minRow..maxRow) {
            for (col in minCol..maxCol) {
                val c = if (Position(row, col) in elfPositions) '#' else '.'
                append(c)
            }
            appendLine()
        }
    }
}

private data class Move(val current: Position, val target: Position) {
    override fun toString(): String = "${format(current)} -> ${format(target)}"
}

private fun format(pos: Position) = "(${pos.row},${pos.col})"

// cannot use value class here because of boxing when used in lists/sets
private typealias Position = Long

internal fun Position(row: Long, col: Long) = row.shl(Int.SIZE_BITS) or (col and 0xFFFFFFFFL)

internal val Position.row: Long get() = shr(Int.SIZE_BITS)
internal val Position.col: Long get() = shl(Int.SIZE_BITS).shr(Int.SIZE_BITS) // respect bit signs

internal fun Position.moved(vararg directions: Direction) = directions.fold(this) { pos, d -> pos.moved(d) }

private fun Position.moved(dir: Direction) = when (dir) {
    NORTH -> Position(row - 1, col)
    SOUTH -> Position(row + 1, col)
    WEST -> Position(row, col - 1)
    EAST -> Position(row, col + 1)
}

private fun Position.neighboursInDirection(dir: Direction) =
    when (dir) {
        NORTH -> listOf(moved(NORTH), moved(NORTH, WEST), moved(NORTH, EAST))
        SOUTH -> listOf(moved(SOUTH), moved(SOUTH, WEST), moved(SOUTH, EAST))
        WEST -> listOf(moved(WEST), moved(WEST, NORTH), moved(WEST, SOUTH))
        EAST -> listOf(moved(EAST), moved(EAST, NORTH), moved(EAST, SOUTH))
    }

private val Position.neighbours
    get() = buildList {
        add(Position(row - 1, col - 1))
        add(Position(row - 1, col))
        add(Position(row - 1, col + 1))
        add(Position(row, col - 1))
        add(Position(row, col + 1))
        add(Position(row + 1, col - 1))
        add(Position(row + 1, col))
        add(Position(row + 1, col + 1))
    }

internal enum class Direction {
    NORTH, SOUTH, WEST, EAST
}
