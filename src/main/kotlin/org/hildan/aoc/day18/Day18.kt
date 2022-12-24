package org.hildan.aoc.day18

import org.hildan.aoc.client.inputLines
import kotlin.math.abs

fun main() {
    val cubes = inputLines(day = 18).map { it.parsePoint() }
    println(surfaceArea(cubes)) // 4282
    println(outerSurfaceArea(cubes)) // 5180 is too high
}

private data class Cube(val x: Int, val y: Int, val z: Int)

private fun String.parsePoint(): Cube {
    val (x, y, z) = split(",").map { it.toInt() }
    return Cube(x, y, z)
}

private fun surfaceArea(cubes: List<Cube>): Int {
    val totalFaces = cubes.size * 6
    val innerFaces = countInnerFaces(cubes)
    return totalFaces - innerFaces
}

private fun countInnerFaces(cubes: List<Cube>): Int = cubes.sumOf { c1 ->
    cubes.count { c1.touches(it) }
}

private fun Cube.touches(other: Cube): Boolean = manhattanDistanceTo(other) == 1

private fun Cube.manhattanDistanceTo(other: Cube) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

private fun outerSurfaceArea(cubes: List<Cube>): Int {
    val boundingBox = BoundingBox(cubes)
    val lavaCubes = cubes.toSet()

    val cubesToExplore = ArrayDeque<Cube>()
    cubesToExplore.add(boundingBox.corner)

    val seen = mutableSetOf<Cube>()
    var outerArea = 0

    while (cubesToExplore.isNotEmpty()) {
        val c = cubesToExplore.removeFirst()

        val neighbours = c.neighbours().filter { it in boundingBox }
        val (lavaNeighbours, airNeighbours) = neighbours.partition { it in lavaCubes }
        outerArea += lavaNeighbours.size

        val notSeenAirNeighbours = airNeighbours.filter { it !in seen }
        cubesToExplore.addAll(0, notSeenAirNeighbours)
        seen.addAll(notSeenAirNeighbours)
    }

    boundingBox.draw(lavaCubes, seen)
    return outerArea
}

private fun Cube.neighbours(): Set<Cube> = buildSet {
    add(copy(x = x + 1))
    add(copy(x = x - 1))
    add(copy(y = y + 1))
    add(copy(y = y - 1))
    add(copy(z = z + 1))
    add(copy(z = z - 1))
}

private class BoundingBox(cubes: List<Cube>) {
    val xRange = cubes.boundingRangeWithMargin { it.x }
    val yRange = cubes.boundingRangeWithMargin { it.y }
    val zRange = cubes.boundingRangeWithMargin { it.z }

    val corner = Cube(xRange.first, yRange.first, zRange.first)

    operator fun contains(cube: Cube): Boolean = cube.x in xRange && cube.y in yRange && cube.z in zRange
}

private fun List<Cube>.boundingRangeWithMargin(getCoord: (Cube) -> Int): IntRange {
    val min = minOf(getCoord) - 1
    val max = maxOf(getCoord) + 1
    return min..max
}

private fun BoundingBox.draw(lavaCubes: Set<Cube>, explored: Set<Cube>) {
    for (z in zRange) {
        println("z = $z")
        for (y in yRange) {
            for (x in xRange) {
                val c = Cube(x, y, z)
                when (c) {
                    in lavaCubes -> print("#")
                    in explored -> print(".")
                    else -> print(" ")
                }
            }
            println()
        }
        println()
    }
}
