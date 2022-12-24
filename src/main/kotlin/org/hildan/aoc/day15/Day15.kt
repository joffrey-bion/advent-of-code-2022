package org.hildan.aoc.day15

import org.hildan.aoc.client.inputLines
import kotlin.math.abs

fun main() {
    val sensors = inputLines(day = 15).map { it.parseSensor() }
    val map = Map(sensors)
    println(map.countRuledOutPositionsInRow(2_000_000))
    println(map.findSinglePossibleBeaconPositionIn(xRange = 0..4_000_000, yRange = 0..4_000_000).tuningFrequency)
}

private val Position.tuningFrequency: Long
    get() = x * 4_000_000L + y

private class Map(val sensors: List<Sensor>) {

    fun countRuledOutPositionsInRow(scannedY: Int): Int =
        sensors.flatMapTo(mutableSetOf()) { it.nonBeaconXsInRow(scannedY)  }.size

    fun findSinglePossibleBeaconPositionIn(xRange: IntRange, yRange: IntRange): Position {
        for (y in yRange) {
            val ruledOutRangesForRow = ruledOutRangesForRow(y)
            if (ruledOutRangesForRow.start > xRange.first) {
                return Position(xRange.first, y)
            }
            if (ruledOutRangesForRow.end < xRange.last) {
                return Position(xRange.last, y)
            }
            val gap = ruledOutRangesForRow.findGap()
            if (gap != null) {
                return Position(gap.first, y)
            }
        }
        error("Beacon not found")
    }

    private fun ruledOutRangesForRow(scannedY: Int): RangeGroup {
        val ranges = sensors.map { it.ruledOutXsCloserThanBeaconInRow(scannedY) }
        return RangeGroup(ranges)
    }
}

private class RangeGroup(ranges: List<IntRange>) {
    private val sortedRanges = ranges.sortedBy { it.first }
    val start: Int = sortedRanges.first().first
    val end: Int = sortedRanges.maxOf { it.last }

    fun findGap(): IntRange? {
        var currentEnd = sortedRanges.first().last
        sortedRanges.forEach {
            if (it.first > currentEnd) {
                return currentEnd + 1 until it.first
            }
            currentEnd = maxOf(it.last, currentEnd)
        }
        return null
    }
}

private fun Sensor.nonBeaconXsInRow(scannedY: Int): Set<Int> = buildSet {
    addAll(ruledOutXsCloserThanBeaconInRow(scannedY))
    // in part 1, the answer doesn't count the existing known beacons as being ruled out...
    if (closestBeaconPosition.y == scannedY) {
        remove(closestBeaconPosition.x)
    }
}

private fun Sensor.ruledOutXsCloserThanBeaconInRow(scannedY: Int): IntRange {
    val deltaY = abs(position.y - scannedY)
    val maxDeltaX = manhattanToClosestBeacon - deltaY
    if (maxDeltaX < 0) {
        return IntRange.EMPTY
    }
    val minX = position.x - maxDeltaX
    val maxX = position.x + maxDeltaX
    return minX..maxX
}

private val beaconRegex = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")

private fun String.parseSensor(): Sensor {
    val match = beaconRegex.matchEntire(this) ?: error("input doesn't match regex")
    return Sensor(
        position = Position(
            x = match.groupValues[1].toInt(),
            y = match.groupValues[2].toInt(),
        ),
        closestBeaconPosition = Position(
            x = match.groupValues[3].toInt(),
            y = match.groupValues[4].toInt(),
        ),
    )
}

private data class Sensor(
    val position: Position,
    val closestBeaconPosition: Position,
) {
    private val deltaX = abs(closestBeaconPosition.x - position.x)
    private val deltaY = abs(closestBeaconPosition.y - position.y)
    val manhattanToClosestBeacon = deltaX + deltaY
}

private data class Position(val x: Int, val y: Int)
