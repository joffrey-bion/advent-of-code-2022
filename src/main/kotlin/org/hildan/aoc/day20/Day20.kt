package org.hildan.aoc.day20

import org.hildan.aoc.client.inputLines

fun main() {
    val numbers = inputLines(day = 20).map { it.toLong() }

    println(mix(numbers, nMixingRounds = 1).coordinatesSum()) // 15297
    println(mix(numbers.map { it * 811589153 }, nMixingRounds = 10).coordinatesSum()) // 2897373276210
}

private fun mix(numbers: List<Long>, nMixingRounds: Int): List<Long> {
    val orderedNumsToMix = numbers.map { Wrapper(it) }
    val result = orderedNumsToMix.toMutableList()

    repeat(nMixingRounds) {
        orderedNumsToMix.forEach { w ->
            val i = result.indexOf(w)
            val node = result.removeAt(i)
            result.add((i + w.value).mod(result.size), node)
        }
    }

    return result.map { it.value }
}

private class Wrapper(val value: Long)

private fun List<Long>.coordinatesSum(): Long {
    val zeroIndex = indexOf(0)
    val x = this[(zeroIndex + 1000) % size]
    val y = this[(zeroIndex + 2000) % size]
    val z = this[(zeroIndex + 3000) % size]
    return x + y + z
}
