package org.hildan.aoc.day1

import org.hildan.aoc.client.inputLines

fun main() {
    val elfCalories = inputLines(day = 1).elvesTotalCalories().toList()
    println(elfCalories.max())
    println(elfCalories.sortedDescending().take(3).sum())
}

private fun List<String>.elvesTotalCalories() = sequence {
    var currentElfCalories = 0
    forEach {
        if (it.isBlank()) {
            yield(currentElfCalories)
            currentElfCalories = 0
        } else {
            currentElfCalories += it.toInt()
        }
    }
    yield(currentElfCalories)
}
