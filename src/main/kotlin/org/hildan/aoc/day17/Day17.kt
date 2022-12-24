package org.hildan.aoc.day17

import org.hildan.aoc.client.inputFile
import java.math.BigInteger
import kotlin.io.path.readText

private val shapesText = """
####

.#.
###
.#.

..#
..#
###

#
#
#
#

##
##
""".trimIndent()

private val binaryShapePatterns = shapesText.split("\n\n").map {
    it.replace(' ', '0')
        .replace('.', '0')
        .replace('#', '1')
}

fun main() {
    val shapes = binaryShapePatterns.map { Shape(it.lines()) }
    val jetPattern = inputFile(day = 17).readText().trim()
    println(Cave(shapes, jetPattern).heightAfterNRockFall(2022)) // 3081
    println(Cave(shapes, jetPattern).heightAfterNRockFallWithStates(1_000_000_000_000L)) // 1524637681145
}

private fun Cave.heightAfterNRockFall(n: Long): Int {
    (1..n).forEach {
        rockFall()
    }
    return height
}

private fun Cave.heightAfterNRockFallWithStates(n: Long): Long {
    val states = mutableMapOf<State, HistoricalData>()
    for (i in 1..n) {
        rockFall()
        val s = encodedState()
        val historicalData = states[s]
        if (historicalData == null) {
            states[s] = HistoricalData(height, i)
        } else {
            println("we found a loop!")
            val heightDiff = height - historicalData.height
            val loopLength = i - historicalData.iteration
            val remainingIterations = n - i
            val nRemainingFullLoops = remainingIterations / loopLength
            val nFinalIterations = remainingIterations % loopLength
            return heightAfterNRockFall(nFinalIterations) + nRemainingFullLoops * heightDiff
        }
    }
    return height.toLong()
}

private data class HistoricalData(val height: Int, val iteration: Long)

private data class Shape(val binaryShapeRowsTopToBottom: List<String>) {
    /**
     * The shape as a bit field representing multiple concatenated 7-bit rows.
     *
     * The bits from LSB to MSB represent positions in the grid starting at (0,0) (bottom left) for the LSB,
     * and going to the right in each row (when going towards higher bits), then moving up to the left of
     * the above row when hitting the right side of the grid.
     *
     * The L-like piece:
     *
     * ```
     * 2 ..#     2 EFGHIJK
     * 1 ..#     1 789ABCD
     * 0 ###     0 0123456
     *   012       0123456
     * ```
     *
     * is represented as 0000100_0000100_0000111 (underscores for readability).
     */
    val bits = binaryShapeRowsTopToBottom.joinToString("") {
        // higher bits (to the left in the binary number) represent points that are further *right* in the grid
        it.reversed().padStart(7, '0')
    }.toBigInteger(2)

    val height: Int = binaryShapeRowsTopToBottom.size
    val width: Int = binaryShapeRowsTopToBottom.maxOf { it.length }

    override fun toString(): String = bitsToGridString(bits)
}

private fun Shape.atPosition(pos: Position) = PositionedShape(this, pos)

private class PositionedShape(val shape: Shape, val pos: Position) {
    val topRow: Int get() = pos.row + shape.height - 1
    val bottomRow: Int get() = pos.row
    val leftCol: Int get() = pos.col
    val rightCol: Int get() = pos.col + shape.width - 1

    /**
     * The positioned shape as a bit field representing multiple concatenated 7-bit rows.
     *
     * The bits from LSB to MSB represent positions in the grid starting at (0,0) (bottom left) for the LSB,
     * and going to the right in each row (when going towards higher bits), then moving up to the left of
     * the above row when hitting the right side of the grid.
     */
    // We shift the bits left to move pieces to the right in the grid. Shifting [CAVE_WIDTH] bits the to left (towards
    // higher bits) effectively goes one row up because rows are concatenated.
    val bits = shape.bits.shl(CAVE_WIDTH * pos.row + pos.col)

    override fun toString(): String = bitsToGridString(bits)
}

private fun bitsToGridString(mask: BigInteger) = mask.toString(2)
    .padStartToMultipleOf(CAVE_WIDTH, padChar = '0')
    .chunked(CAVE_WIDTH) // take each row, starting at the top (the MSBs), to the bottom (LSB)
    .joinToString("\n") {
        it.reversed() // bring back the LSB to the front, so we see the left column on the left
    }

private fun String.padStartToMultipleOf(n: Int, padChar: Char): String {
    val newLength = (length / n + 1) * n
    return padStart(newLength, padChar)
}

private const val CAVE_WIDTH = 7

private class Cave(
    private val shapes: List<Shape>,
    private val jetPattern: String,
) {
    var height = 0

    private var gridMask = BigInteger.ZERO

    private var nextShapeIndex = 0
    private var nextJetIndex = 0

    fun rockFall() {
        val shape = nextRockShape()
        var pos = Position(col = 2, row = height + 3)
        while (true) {
            val posAfterJet = pos.shiftedWithNextJet()
            if (shape.canFitAtPosition(posAfterJet)) {
                pos = posAfterJet
            }
            val posAfterFall = pos.movedDown()
            if (!shape.canFitAtPosition(posAfterFall)) {
                place(shape, pos)
                return
            }
            pos = posAfterFall
        }
    }

    private fun nextRockShape(): Shape {
        val shape = shapes[nextShapeIndex]
        nextShapeIndex = (nextShapeIndex + 1) % shapes.size
        return shape
    }

    private fun Position.shiftedWithNextJet(): Position {
        val jetOffset = nextJetXOffset()
        return copy(col = col + jetOffset)
    }

    private fun nextJetXOffset(): Int = when (val jet = nextJet()) {
        '>' -> 1
        '<' -> -1
        else -> error("unknown jet character $jet")
    }

    private fun nextJet(): Char {
        val jet = jetPattern[nextJetIndex]
        nextJetIndex = (nextJetIndex + 1) % jetPattern.length
        return jet
    }

    private fun Shape.canFitAtPosition(posAfterJet: Position) = atPosition(posAfterJet).canFit()

    private fun PositionedShape.canFit(): Boolean =
        leftCol >= 0 && rightCol < CAVE_WIDTH && bottomRow >= 0 && bits.and(gridMask) == BigInteger.ZERO

    private fun place(shape: Shape, pos: Position) {
        val positionedShape = shape.atPosition(pos)
        gridMask = gridMask.or(positionedShape.bits)
        height = maxOf(height, positionedShape.topRow + 1)
    }

    fun encodedState() = State(
        nextShapeIndex = nextShapeIndex,
        nextJetIndex = nextJetIndex,
        skyline = skyline(),
    )

    private fun skyline(): List<Int> {
        val heights = List(CAVE_WIDTH) { col -> columnHeight(col) }
        return heights.map { it - heights.min() }
    }

    private fun columnHeight(col: Int): Int {
        val pointMaskInRow = BigInteger.ONE.shl(col)
        for (r in height - 1 downTo 0) {
            val pointMaskInGrid = pointMaskInRow.shl(r * CAVE_WIDTH)
            if (pointMaskInGrid.and(gridMask) != BigInteger.ZERO) {
                return r + 1
            }
        }
        return 0
    }

    override fun toString(): String = bitsToGridString(gridMask)
}

private data class State(
    val nextShapeIndex: Int,
    val nextJetIndex: Int,
    val skyline: List<Int>,
)

private data class Position(val col: Int, val row: Int) {
    override fun toString(): String = "($col, $row)"
}

private fun Position.movedDown() = copy(row = row - 1)
