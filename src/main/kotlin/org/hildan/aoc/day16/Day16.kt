package org.hildan.aoc.day16

import org.hildan.aoc.client.inputLines

private const val START_VALVE_NAME = "AA"

fun main() {
    val valves = inputLines(day = 16).map { it.parseValve() }
    val graph = valves.resolveAndRemoveZeroValves()
    println(Graph(graph, part2 = false).maxReleasablePressure()) // 1880
    println(Graph(graph, part2 = true).maxReleasablePressure()) // 2520
}

private val valveRegex = Regex("""Valve ([A-Z]{2}) has flow rate=(\d+); tunnels? leads? to valves? (.+)""")

private fun String.parseValve(): RawValve {
    val match = valveRegex.matchEntire(this) ?: error("input '$this' doesn't match regex")
    return RawValve(
        name = match.groupValues[1],
        flowRate = match.groupValues[2].toInt(),
        connectedValveNames = match.groupValues[3].split(", "),
    )
}

private data class RawValve(
    val name: String,
    val flowRate: Int,
    val connectedValveNames: List<String>,
)

private data class Valve(
    val name: String,
    val flowRate: Int,
    val neighbours: MutableList<Edge>,
) {
    override fun toString(): String = "$name ($flowRate) -> $neighbours"
}

private data class Edge(val destination: Valve, val minutesToWalk: Int) {
    override fun toString(): String = "${destination.name} ($minutesToWalk)"
}

private fun List<RawValve>.resolveAndRemoveZeroValves(): List<Valve> {
    val valvesByName = associate { it.name to Valve(it.name, it.flowRate, mutableListOf()) }

    forEach { valve ->
        val neighbours = valve.connectedValveNames.map {
            Edge(valvesByName.getValue(it), 1)
        }
        valvesByName.getValue(valve.name).neighbours.addAll(neighbours)
    }

    return valvesByName.values.remove0FlowSingleTunnelValves()
}

private fun Collection<Valve>.remove0FlowSingleTunnelValves(): List<Valve> {
    val (uselessValves, okValves) = partition { it.flowRate == 0 && it.name != START_VALVE_NAME }
    uselessValves.forEach { uselessValve ->
        if (uselessValve.neighbours.size == 2) { // just a direct tunnel
            val (prev, next) = uselessValve.neighbours.map { it.destination }
            val totalLength = uselessValve.neighbours.sumOf { it.minutesToWalk }
            prev.neighbours.removeIf { it.destination == uselessValve }
            next.neighbours.removeIf { it.destination == uselessValve }

            prev.addOrReplaceEdgeTo(next, totalLength)
            next.addOrReplaceEdgeTo(prev, totalLength)
        }
    }
    return okValves
}

private fun Valve.addOrReplaceEdgeTo(other: Valve, newLength: Int) {
    val existingEdgeToNext = neighbours.firstOrNull { it.destination.name == other.name }
    if (existingEdgeToNext != null) {
        if (existingEdgeToNext.minutesToWalk <= newLength) {
            return
        }
        neighbours.remove(existingEdgeToNext)
    }
    neighbours.add(Edge(other, newLength))
}

private data class State(
    val currentValveSelf: Valve,
    val currentValveElephant: Valve,
    val minutesElapsedSelf: Int,
    val minutesElapsedElephant: Int,
    val openValves: Set<String>,
    val endTotalPressure: Long,
)

private class Graph(
    private val valves: List<Valve>,
    private val part2: Boolean,
) {
    private val maxDuration = if (part2) 26 else 30
    private val shortestTimes = floydWarshall(valves)
    private val valvesByName = valves.associateBy { it.name }

    fun maxReleasablePressure(): Long {
        val startValve = valvesByName.getValue(START_VALVE_NAME)
        val startState = State(
            currentValveSelf = startValve,
            currentValveElephant = startValve,
            minutesElapsedSelf = 0,
            minutesElapsedElephant = 0,
            openValves = emptySet(),
            endTotalPressure = 0,
        )
        val statesToExplore = ArrayDeque<State>()
        statesToExplore.add(startState)

        var maxPressure = 0L

        while (statesToExplore.isNotEmpty()) {
            val s = statesToExplore.removeFirst()
            val nextStates = s.possibleNextStates()
            if (nextStates.isEmpty()) { // we reached the end of this path
                maxPressure = maxOf(maxPressure, s.endTotalPressure)
            } else {
                // prepend to get DFS behaviour (to limit the memory)
                statesToExplore.addAll(0, nextStates)
            }
        }
        return maxPressure
    }

    enum class Actor { SELF, ELEPHANT }

    private fun State.possibleNextStates() = buildList {
        val actor = if (part2 && minutesElapsedSelf > minutesElapsedElephant) Actor.ELEPHANT else Actor.SELF
        val currentValve = when (actor) {
            Actor.ELEPHANT -> currentValveElephant
            Actor.SELF -> currentValveSelf
        }
        val minutesElapsed = when (actor) {
            Actor.ELEPHANT -> minutesElapsedElephant
            Actor.SELF -> minutesElapsedSelf
        }
        shortestTimes.getValue(currentValve.name).forEach { (destName, walkTime) ->
            if (destName !in openValves && minutesElapsed + walkTime + 1 < maxDuration) {
                add(moveToAndOpen(valvesByName.getValue(destName), walkTime, actor = actor))
            }
        }
    }

    private fun State.moveToAndOpen(dest: Valve, walkTime: Int, actor: Actor): State = when(actor) {
        Actor.SELF -> moveSelfToAndOpen(dest, walkTime)
        Actor.ELEPHANT -> moveElephantToAndOpen(dest, walkTime)
    }

    private fun State.moveSelfToAndOpen(dest: Valve, walkTime: Int): State {
        val timeAfterWalkAndOpen = minutesElapsedSelf + walkTime + 1
        return copy(
            currentValveSelf = dest,
            minutesElapsedSelf = timeAfterWalkAndOpen,
            openValves = openValves + dest.name,
            endTotalPressure = endTotalPressure + dest.flowRate * (maxDuration - timeAfterWalkAndOpen),
        )
    }

    private fun State.moveElephantToAndOpen(dest: Valve, walkTime: Int): State {
        val timeAfterWalkAndOpen = minutesElapsedElephant + walkTime + 1
        return copy(
            currentValveElephant = dest,
            minutesElapsedElephant = timeAfterWalkAndOpen,
            openValves = openValves + dest.name,
            endTotalPressure = endTotalPressure + dest.flowRate * (maxDuration - timeAfterWalkAndOpen),
        )
    }

    override fun toString(): String = valves.joinToString("\n")
}

// https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm
private fun floydWarshall(valves: List<Valve>): Map<String, Map<String, Int>> {
    val times = valves.associateTo(mutableMapOf()) { v ->
        v.name to v.neighbours.associateTo(mutableMapOf()) { it.destination.name to it.minutesToWalk }
    }

    val names = times.keys
    for (k in names) {
        for (i in names) {
            for (j in names) {
                val ij = times[i]!![j] ?: Int.MAX_VALUE
                val ik = times[i]!![k] ?: 10000 // avoid overflow
                val kj = times[k]!![j] ?: 10000
                val timeViaK = ik + kj
                if (timeViaK < ij) {
                    times[i]!![j] = timeViaK
                }
            }
        }
    }
    return times
}
