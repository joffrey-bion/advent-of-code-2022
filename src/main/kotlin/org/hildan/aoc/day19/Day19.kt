package org.hildan.aoc.day19

import org.hildan.aoc.day19.ResourceType.*
import org.hildan.aoc.client.inputLines

fun main() {
    val blueprints = inputLines(day = 19).map { it.parseBlueprint() }
    println(blueprints.sumOf { it.computeQuality(24) }) // 1092
    println(blueprints.take(3).map { it.maxProducedGeodesIn(32) }.reduce(Int::times)) //
}

private val blueprintRegex = Regex("""Blueprint (\d+): Each ore robot costs (\d+) ore. Each clay robot costs (\d+) ore. Each obsidian robot costs (\d+) ore and (\d+) clay. Each geode robot costs (\d+) ore and (\d+) obsidian.""")

private fun String.parseBlueprint(): Blueprint {
    val match = blueprintRegex.matchEntire(this) ?: error("input doesn't match blueprint regex: $this")
    val intGroupValues = match.groupValues.drop(1).map { it.toInt() }
    return Blueprint(
        id = intGroupValues[0],
        oreRobotOreCost = intGroupValues[1],
        clayRobotOreCost = intGroupValues[2],
        obsidianRobotOreCost = intGroupValues[3],
        obsidianRobotClayCost = intGroupValues[4],
        geodeRobotOreCost = intGroupValues[5],
        geodeRobotObsidianCost = intGroupValues[6],
    )
}

private enum class ResourceType {
    ORE, CLAY, OBSIDIAN, GEODE
}

private data class State(
    val elapsedMinutes: Int = 0,
    val resources: Map<ResourceType, Int> = emptyMap(),
    val nRobots: Map<ResourceType, Int> = mapOf(ORE to 1),
)

private data class Blueprint(
    private val id: Int,
    private val oreRobotOreCost: Int,
    private val clayRobotOreCost: Int,
    private val obsidianRobotOreCost: Int,
    private val obsidianRobotClayCost: Int,
    private val geodeRobotOreCost: Int,
    private val geodeRobotObsidianCost: Int,
) {
    private val robotCosts = mapOf(
        ORE to mapOf(ORE to oreRobotOreCost),
        CLAY to mapOf(ORE to clayRobotOreCost),
        OBSIDIAN to mapOf(ORE to obsidianRobotOreCost, CLAY to obsidianRobotClayCost),
        GEODE to mapOf(ORE to geodeRobotOreCost, OBSIDIAN to geodeRobotObsidianCost),
    )

    private val maxCosts = robotCosts.values.flatMap { it.entries }
        .groupBy({ it.key }, { it.value })
        .mapValues { it.value.max() }

    fun computeQuality(nMinutes: Int) = id * maxProducedGeodesIn(nMinutes)

    fun maxProducedGeodesIn(nMinutes: Int): Int {
        val stack = ArrayDeque<State>()
        stack.add(State())
        var maxGeodes = 0
        while (stack.isNotEmpty()) {
            val s = stack.removeFirst()
            if (s.elapsedMinutes == nMinutes) {
                maxGeodes = maxOf(maxGeodes, s.resources[GEODE] ?: 0)
                continue
            }
            stack.addAll(0, s.nextStates(nMinutes))
        }
        return maxGeodes
    }

    private fun State.nextStates(maxMinutes: Int): List<State> =
        ResourceType.values().mapNotNull { type -> waitAndBuyRobotOfType(type, maxMinutes) }

    private fun State.waitAndBuyRobotOfType(type: ResourceType, maxMinutes: Int): State? {
        if (type != GEODE && (nRobots[type] ?: 0) >= maxCosts.getValue(type)) {
            return null // no point in building more of those, we can't consume them in one turn
        }
        val costPerType = robotCosts.getValue(type)
        val minutesToWait = minutesToWaitFor(costPerType) + 1
        val availableAt = elapsedMinutes + minutesToWait
        if (availableAt > maxMinutes) {
            return null
        }
        return State(
            elapsedMinutes = availableAt,
            resources = resources.toMutableMap().apply {
                addAll(nRobots, multiplier = minutesToWait)
                subtractAll(costPerType)
            },
            nRobots = nRobots.toMutableMap().apply {
               merge(type, 1, Int::plus)
            },
        )
    }
}

private fun State.minutesToWaitFor(costs: Map<ResourceType, Int>): Int =
    costs.entries.maxOf { (type, cost) -> minutesToWaitForResource(type, cost) }

private fun State.minutesToWaitForResource(type: ResourceType, cost: Int): Int {
    val available = resources[type] ?: 0
    val remainingToWait = cost - available
    if (remainingToWait <= 0) {
        return 0
    }
    // return technically infinity (we'll never get enough) but avoid overflow with the +1
    val prodPerMin = nRobots[type] ?: return 10000
    return remainingToWait / prodPerMin + (if (remainingToWait % prodPerMin > 0) 1 else 0)
}

private fun MutableMap<ResourceType, Int>.addAll(other: Map<ResourceType, Int>, multiplier: Int = 1) {
    other.forEach { (rType, qty) ->
        merge(rType, qty * multiplier, Int::plus)
    }
}

private fun MutableMap<ResourceType, Int>.subtractAll(other: Map<ResourceType, Int>, multiplier: Int = 1) {
    other.forEach { (rType, qty) ->
        merge(rType, -qty * multiplier, Int::plus)
    }
}
