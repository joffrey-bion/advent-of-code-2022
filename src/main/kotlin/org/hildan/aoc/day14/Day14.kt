package org.hildan.aoc.day14

import org.hildan.aoc.client.inputLines

fun main() {
    val paths = inputLines(day = 14).map { it.parsePath() }
    println(part1(paths))
    println(part2(paths))
}

private fun String.parsePath() = split("->").map { it.trim().parsePoint() }

private fun String.parsePoint() = split(",").map { it.toInt() }.let { Position(it[0], it[1]) }

private fun List<Position>.unwrapPath() = windowed(2).flatMap { (p1, p2) ->
    when {
        p1.x == p2.x -> unorientedRange(p1.y, p2.y).map { Position(x = p1.x, y = it) }
        p1.y == p2.y -> unorientedRange(p1.x, p2.x).map { Position(x = it, y = p1.y) }
        else -> error("Path vertices are not aligned: $p1 - $p2")
    }
}

private fun unorientedRange(i: Int, j: Int): IntRange = minOf(i, j)..maxOf(i, j)

private class Cave(
    rockPaths: List<List<Position>>,
    private val part1: Boolean = false
) {
    private val sandStart = Position(x = 500, y = 0)
    private val lowestRockY = rockPaths.maxOf { path -> path.maxOf { it.y } }
    private val blockedPositions = rockPaths.flatMapTo(mutableSetOf()) { it.unwrapPath() }
    private var nRestingBlocks = 0
        private set

    fun addSandUntilInfiniteFlow(): Int {
        while(addSandPart1()) {}
        return nRestingBlocks
    }

    /**
     * Adds one unit of sand and returns true if it comes to rest.
     */
    private fun addSandPart1(): Boolean {
        var pos = sandStart
        while (pos.y <= lowestRockY) {
            val next = pos.nextUnblocked()
            if (next == null) {
                // coming to rest
                blockedPositions.add(pos)
                nRestingBlocks++
                return true
            }
            pos = next
        }
        // lower than any rock, will fall forever
        return false
    }

    fun addSandUntilRestingAt500(): Int {
        while(addSandPart2()) {}
        return nRestingBlocks
    }

    /**
     * Adds one unit of sand and returns true unless the cave is full.
     */
    private fun addSandPart2(): Boolean {
        if (sandStart.isBlocked()) {
            // the cave is full if the start was already blocked
            return false
        }
        var pos = sandStart
        while (true) {
            val next = pos.nextUnblocked()
            if (next == null) {
                // coming to rest
                blockedPositions.add(pos)
                nRestingBlocks++
                return true
            }
            pos = next
        }
    }

    private fun Position.nextUnblocked(): Position? {
        val down = down()
        return down.takeIf { !it.isBlocked() }
            ?: down.left().takeIf { !it.isBlocked() }
            ?: down.right().takeIf { !it.isBlocked() }
    }

    private fun Position.isBlocked() = this in blockedPositions || (!part1 && y >= lowestRockY + 2)
}

private data class Position(
    /** Horizontal position, increasing going right */
    val x: Int,
    /** Vertical position, increasing going down */
    val y: Int,
) {
    fun down() = Position(x, y + 1)
    fun left() = Position(x - 1, y)
    fun right() = Position(x + 1, y)
}

private fun part1(paths: List<List<Position>>): Int = Cave(paths, part1 = true).addSandUntilInfiniteFlow()

private fun part2(paths: List<List<Position>>): Int = Cave(paths, part1 = false).addSandUntilRestingAt500()


