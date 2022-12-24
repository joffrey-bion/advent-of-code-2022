package org.hildan.aoc.day24

import org.hildan.aoc.client.inputLines

fun main() {
    val lines = inputLines(day = 24)
    println(Map(lines).part1())
    println(Map(lines).part2())
}

private data class Blizzard(
    val position: Position,
    val direction: Direction,
)

private data class Blizzards(
    val blizzards: List<Blizzard>,
    val positions: Set<Position> = blizzards.mapTo(mutableSetOf()) { it.position }
)

private data class State(
    val position: Position,
    val stepsSoFar: Int,
)

private class Map(private val lines: List<String>) {

    private val initialBlizzards = lines.flatMapIndexed { row, line ->
        line.mapIndexedNotNull { col, c ->
            Position(row, col).takeIf { c != '#' && c != '.' }?.let {
                Blizzard(position = it, direction = Direction.fromChar(c))
            }
        }
    }
    private val blizzardsAtTurn = mutableListOf(Blizzards(initialBlizzards))

    fun part1(): Int {
        val initialState = State(
            position = Position(row = 0, col = lines[0].indexOf('.')),
            stepsSoFar = 0,
        )
        val exit = Position(row = lines.size - 1, col = lines.last().indexOf('.'))

        return minStepsToGoal(initialState, exit)
    }

    fun part2(): Int {
        val start = Position(row = 0, col = lines[0].indexOf('.'))
        val exit = Position(row = lines.size - 1, col = lines.last().indexOf('.'))

        val trip1 = minStepsToGoal(State(position = start, stepsSoFar = 0), exit)
        val returnTrip = minStepsToGoal(State(exit, stepsSoFar = trip1), start)
        return minStepsToGoal(State(start, stepsSoFar = returnTrip), exit)
    }

    private fun minStepsToGoal(initialState: State, finalPosition: Position): Int {
        val enqueued = mutableSetOf<State>()
        val queue = ArrayDeque<State>()
        queue.add(
            initialState
        )

        while (queue.isNotEmpty()) {
            val currentState = queue.removeFirst()
            if (currentState.position == finalPosition) {
                return currentState.stepsSoFar
            }
            val nextValidStates = currentState.nextValidStates()
            queue.addAll(nextValidStates.filterNot { it in enqueued })
            enqueued.addAll(nextValidStates)
        }
        error("Exit not found")
    }

    private fun State.nextValidStates(): List<State> {
        val newBlizzards = blizzardPositionsAt(stepsSoFar + 1)
        return buildList {
            // the new states if we move
            Direction.values.mapNotNullTo(this) { dir ->
                position.moved(dir).takeIf { it.isInGrid() && it !in newBlizzards }?.let { p ->
                    State(position = p, stepsSoFar = stepsSoFar + 1)
                }
            }

            // the new state if we don't move
            // /!\ added LAST because with DFS we don't want to go infinitely deep (doing nothing at the entrance)
            if (position !in newBlizzards) {
                add(copy(stepsSoFar = stepsSoFar + 1))
            }
        }
    }

    private fun Position.isInGrid(): Boolean {
        return row in lines.indices && col > 0 && col < lines[row].lastIndex && lines[row][col] != '#'
    }

    private fun blizzardPositionsAt(step: Int): Set<Position> {
        if (step < blizzardsAtTurn.size) {
            return blizzardsAtTurn[step].positions
        }
        if (step > blizzardsAtTurn.size) {
            error("Trying to jump steps?")
        }
        val newBlizzards = Blizzards(blizzardsAtTurn.last().blizzards.map { it.moved() })
        blizzardsAtTurn.add(newBlizzards)
        return newBlizzards.positions
    }

    private fun Blizzard.moved(): Blizzard {
        val blizzardRows = 1..(lines.size - 2)
        val blizzardCols = 1..(lines[0].length - 2)
        return copy(
            position = when (direction) {
                Direction.UP -> Position((position.row - 1).wrapInRange(blizzardRows), position.col)
                Direction.DOWN -> Position((position.row + 1).wrapInRange(blizzardRows), position.col)
                Direction.LEFT -> Position(position.row, (position.col - 1).wrapInRange(blizzardCols))
                Direction.RIGHT -> Position(position.row, (position.col + 1).wrapInRange(blizzardCols))
            },
        )
    }
}

private fun Int.wrapInRange(range: IntRange): Int = when {
    this in range -> this
    this < range.first -> range.last
    this > range.last -> range.first
    else -> error("boom")
}

private data class Position(val row: Int, val col: Int)

private fun Position.moved(dir: Direction) = when (dir) {
    Direction.UP -> Position(row - 1, col)
    Direction.DOWN -> Position(row + 1, col)
    Direction.LEFT -> Position(row, col - 1)
    Direction.RIGHT -> Position(row, col + 1)
}

private enum class Direction {
    RIGHT, DOWN, LEFT, UP;

    companion object {
        fun fromChar(c: Char): Direction = when(c) {
            '>' -> RIGHT
            '<' -> LEFT
            'v' -> DOWN
            '^' -> UP
            else -> error("Unknown blizzard character '$c'")
        }

        val values: List<Direction> = values().toList()
    }
}
