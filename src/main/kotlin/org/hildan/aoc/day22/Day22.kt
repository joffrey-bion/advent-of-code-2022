package org.hildan.aoc.day22

import org.hildan.aoc.client.inputLines

private const val CUBE_SIZE = 50

fun main() {
    val lines = inputLines(day = 22)
    val rows = lines.takeWhile { it.isNotEmpty() }
    val moves = lines.last().parseMoves()
    println(Map(rows, part2 = false).move(moves).password) // 31568
    println(Map(rows, part2 = true).move(moves).password) // 36540
}

private data class State(
    var pos: Position,
    var facingDirection: Direction,
) {
    val password: Int get() = (pos.row + 1) * 1000 + (pos.col + 1) * 4 + facingDirection.code
}

private class Map(
    private val rows: List<String>,
    private val part2: Boolean,
) {
    private val state = State(
        pos = Position(
            row = 0,
            col = rows[0].indexOf('.'),
        ),
        facingDirection = Direction.RIGHT,
    )

    fun move(moves: List<Move>): State {
        moves.forEach { move ->
            when (move) {
                is Move.Turn -> state.facingDirection = move.rotate(state.facingDirection)
                is Move.Walk -> walk(move.distance)
            }
        }
        return state
    }

    private fun walk(distance: Int) {
        repeat(distance) {
            val (newPos, newDir) = state.pos.moved(state.facingDirection).wrapped()
            if (newPos.isWall()) {
                return
            }
            state.pos = newPos
            state.facingDirection = newDir
        }
    }

    private fun Position.isWall(): Boolean = rows[row][col] == '#'

    private fun Position.wrapped(): Pair<Position, Direction> {
        if (isOutOfGrid()) {
            return if (part2) wrappedCube() else wrappedFlat() to state.facingDirection
        }
        return this to state.facingDirection
    }

    private fun Position.isOutOfGrid(): Boolean = row !in rows.indices || col !in rows[row].indices || rows[row][col] == ' '

    private fun Position.wrappedCube(): Pair<Position, Direction> = when (state.facingDirection) {
        Direction.UP -> {
            val subCol = col % CUBE_SIZE
            when (col / CUBE_SIZE) {
                0 -> Position(row = CUBE_SIZE + subCol, col = CUBE_SIZE) to Direction.RIGHT
                1 -> Position(row = CUBE_SIZE * 3 + subCol, col = 0) to Direction.RIGHT
                2 -> Position(row = CUBE_SIZE * 4 - 1, col = subCol) to Direction.UP
                else -> error("Too far right when moving up: $this")
            }
        }

        Direction.DOWN -> {
            val subCol = col % CUBE_SIZE
            when (col / CUBE_SIZE) {
                0 -> Position(row = 0, col = CUBE_SIZE * 2 + subCol) to Direction.DOWN
                1 -> Position(row = CUBE_SIZE * 3 + subCol, col = CUBE_SIZE - 1) to Direction.LEFT
                2 -> Position(row = CUBE_SIZE + subCol, col = CUBE_SIZE * 2 - 1) to Direction.LEFT
                else -> error("Too far right when moving down: $this")
            }
        }

        Direction.LEFT -> {
            val subRow = row % CUBE_SIZE
            when (row / CUBE_SIZE) {
                0 -> Position(row = CUBE_SIZE * 3 - 1 - subRow, col = 0) to Direction.RIGHT
                1 -> Position(row = CUBE_SIZE * 2, col = subRow) to Direction.DOWN
                2 -> Position(row = CUBE_SIZE - 1 - subRow, col = CUBE_SIZE) to Direction.RIGHT
                3 -> Position(row = 0, col = CUBE_SIZE + subRow) to Direction.DOWN
                else -> error("Too far down when moving left: $this")
            }
        }

        Direction.RIGHT -> {
            val subRow = row % CUBE_SIZE
            when (row / CUBE_SIZE) {
                0 -> Position(row = CUBE_SIZE * 3 - 1 - subRow, col = CUBE_SIZE * 2 - 1) to Direction.LEFT
                1 -> Position(row = CUBE_SIZE - 1, col = CUBE_SIZE * 2 + subRow) to Direction.UP
                2 -> Position(row = CUBE_SIZE - 1 - subRow, col = CUBE_SIZE * 3 - 1) to Direction.LEFT
                3 -> Position(row = CUBE_SIZE * 3 - 1, col = CUBE_SIZE + subRow) to Direction.UP
                else -> error("Too far down when moving right: $this")
            }
        }
    }

    private fun Position.wrappedFlat() = when (state.facingDirection) {
        Direction.UP -> Position(lastRowInCol(col), col)
        Direction.DOWN -> Position(firstRowInCol(col), col)
        Direction.LEFT -> Position(row, lastColInRow(row))
        Direction.RIGHT -> Position(row, firstColInRow(row))
    }

    private fun firstRowInCol(c: Int): Int = rows.indexOfFirst { it[c] != ' ' }

    private fun lastRowInCol(c: Int): Int = rows.indexOfLast { c in it.indices && it[c] != ' ' }

    private fun firstColInRow(r: Int): Int = rows[r].indexOfFirst { it != ' ' }

    private fun lastColInRow(r: Int) = rows[r].indexOfLast { it != ' ' }
}

private data class Position(val row: Int, val col: Int)

private fun Position.moved(dir: Direction) = when (dir) {
    Direction.UP -> Position(row - 1, col)
    Direction.DOWN -> Position(row + 1, col)
    Direction.LEFT -> Position(row, col - 1)
    Direction.RIGHT -> Position(row, col + 1)
}

private val regex = Regex("""(\d+)|[LR]""")

private fun String.parseMoves(): List<Move> = regex.findAll(this).map { it.value.parseMove() }.toList()

private fun String.parseMove(): Move = toIntOrNull()?.let { Move.Walk(it) } ?: Move.Turn(single())

private sealed class Move {
    data class Walk(val distance: Int): Move()
    data class Turn(private val rotation: Char): Move() {
        fun rotate(dir: Direction): Direction = when (rotation) {
            'R' -> dir.rotatedRight()
            'L' -> dir.rotatedLeft()
            else -> error("Unknown direction $rotation")
        }
    }
}

private enum class Direction(val code: Int) {
    RIGHT(0), DOWN(1), LEFT(2), UP(3);

    fun rotatedRight() = rotated(1)
    fun rotatedLeft() = rotated(-1)

    private fun rotated(ordinalOffset: Int): Direction = values[(ordinal + ordinalOffset + values.size) % values.size]

    companion object {
        private val values = values()
    }
}
