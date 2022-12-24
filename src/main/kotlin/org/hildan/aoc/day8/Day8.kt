package org.hildan.aoc.day8

import org.hildan.aoc.client.inputLines

fun main() {
    val inputLines = inputLines(day = 8)
    val heights = inputLines.map { line -> line.map { it.digitToInt() } }
    println(heights.countVisibleTrees())
    println(heights.maxViewingDistance())
}

private fun List<List<Int>>.countVisibleTrees(): Int {
    var count = 0
    forEachIndexed { row, heights ->
        heights.forEachIndexed { col, height ->
            if (isVisible(this, row, col, height)) {
                count++
            }
        }
    }
    return count
}

private fun isVisible(forest: List<List<Int>>, row: Int, col: Int, height: Int): Boolean {
    val treeRow = forest[row]
    if (treeRow.subList(0, col).all { it < height }) {
        return true // visible from left
    }
    if (treeRow.subList(col + 1, treeRow.size).all { it < height }) {
        return true // visible from right
    }
    if (forest.subList(0, row).all { it[col] < height }) {
        return true // visible from above
    }
    if (forest.subList(row + 1, forest.size).all { it[col] < height }) {
        return true // visible from below
    }
    return false
}

private fun List<List<Int>>.maxViewingDistance(): Int {
    return flatMapIndexed { row, heights ->
        heights.mapIndexed { col, height ->
            viewingDistance(this, row, col, height)
        }
    }.max()
}

private fun viewingDistance(forest: List<List<Int>>, row: Int, col: Int, height: Int): Int {
    val treeRow = forest[row]

    val visibleLeft = treeRow.subList(0, col).asReversed().countUntil { it >= height }
    val visibleRight = treeRow.subList(col + 1, treeRow.size).countUntil { it >= height }
    val visibleUp = forest.subList(0, row).asReversed().map { it[col] }.countUntil { it >= height }
    val visibleDown = forest.subList(row + 1, forest.size).map { it[col] }.countUntil { it >= height }

    return visibleLeft * visibleRight * visibleUp * visibleDown
}

private fun List<Int>.countUntil(predicate: (Int) -> Boolean): Int {
    var count = 0
    for (h in this) {
        count++
        if (predicate(h)) {
            break
        }
    }
    return count
}
