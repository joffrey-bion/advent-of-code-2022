package org.hildan.aoc.day10

import org.hildan.aoc.client.inputLines
import kotlin.math.abs

fun main() {
    val commands = inputLines(day = 10).map { Command.parse(it) }
    println(part1(commands))
    println(part2(commands))
}

private fun part1(commands: List<Command>): Int {
    val states = commands.xStatesSequence().toList()
    val cycles = listOf(20, 60, 100, 140, 180, 220)
    return cycles.sumOf { c ->
        states[c - 1] * c
    }
}

private fun part2(commands: List<Command>): String {
    val states = commands.xStatesSequence().toList()
    val pixels = states.mapIndexed { zeroBasedCycle, spriteX -> pixelAt(zeroBasedCycle, spriteX) }
    return pixels.chunked(40).joinToString("\n") { line ->
        line.joinToString("")
    }
}

fun pixelAt(zeroBasedCycle: Int, spriteX: Int): String {
    val xPos = zeroBasedCycle % 40
    // the sprite is 3px wide, its position is in the middle
    return if (abs(xPos - spriteX) <= 1) "#" else "."
}

private fun List<Command>.xStatesSequence() = sequence {
    var x = 1
    forEach { cmd ->
        when (cmd) {
            is Command.Noop -> yield(x)
            is Command.AddX -> {
                yield(x)
                yield(x)
                x += cmd.value
            }
        }
    }
}

sealed class Command {
    data class AddX(val value: Int): Command()
    data object Noop: Command()

    companion object {
        fun parse(str: String) = when {
            str == "noop" -> Noop
            str.startsWith("addx ") -> AddX(str.removePrefix("addx ").toInt())
            else -> error("Unknown command '$str'")
        }
    }
}
