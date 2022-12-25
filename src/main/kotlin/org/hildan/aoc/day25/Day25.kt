package org.hildan.aoc.day25

import org.hildan.aoc.client.inputLines
import java.lang.StringBuilder
import kotlin.math.pow

fun main() {
    val snafuNumbers = inputLines(day = 25).map { it.parseSnafuLong() }
    println(snafuNumbers.sum().formatSnafu())
}

private fun String.parseSnafuLong(): Long {
    return reversed().mapIndexed { index, c ->
        c.snafuDigitToInt() * 5.0.pow(index).toLong()
    }.sum()
}

private fun Char.snafuDigitToInt() = when (this) {
    '=' -> -2
    '-' -> -1
    '0' -> 0
    '1' -> 1
    '2' -> 2
    else -> error("Unknown SNAFU digit '${this}'")
}

private fun Long.formatSnafu(): String {
    var n = this
    val sb = StringBuilder()
    while (n > 0) {
        val digit = (n % 5).toInt()
        val (formattedDigit, carry) = when (digit) {
            0, 1, 2 -> digit.digitToChar() to false
            3 -> '=' to true
            4 -> '-' to true
            else -> error("Invalid value mod 5: $digit")
        }
        sb.append(formattedDigit)
        n /= 5
        if (carry) {
            n++
        }
    }
    return sb.toString().reversed()
}
