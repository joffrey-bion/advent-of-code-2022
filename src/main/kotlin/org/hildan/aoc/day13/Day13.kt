package org.hildan.aoc.day13

import org.hildan.aoc.client.inputLines
import kotlinx.serialization.json.*

fun main() {
    val pairs = inputLines(day = 13).filter { it.isNotEmpty() }.map { Packet.parse(it) }
    println(part1(pairs))
    println(part2(pairs))
}

private sealed class Packet : Comparable<Packet> {

    data class Single(val value: Int) : Packet() {

        fun asComposite() = Composite(listOf(this))

        override fun compareTo(other: Packet): Int = when (other) {
            is Single -> compareValues(value, other.value)
            is Composite -> asComposite().compareTo(other)
        }

        override fun toString(): String = value.toString()
    }

    data class Composite(val packets: List<Packet>) : Packet() {

        override fun compareTo(other: Packet): Int = when (other) {
            is Single -> compareTo(other.asComposite())
            is Composite -> compareLists(packets, other.packets)
        }

        private fun compareLists(list1: List<Packet>, list2: List<Packet>): Int =
            list1.zip(list2, ::compareValues).firstOrNull { it != 0 } ?: compareValues(list1.size, list2.size)

        override fun toString(): String = packets.toString()
    }

    companion object {

        fun parse(s: String): Packet = Json.parseToJsonElement(s).parse()

        private fun JsonElement.parse(): Packet = when (this) {
            is JsonPrimitive -> Single(jsonPrimitive.int)
            is JsonArray -> Composite(jsonArray.map { it.parse() })
            else -> error("Unable to parse $this")
        }
    }
}

private fun part1(packets: List<Packet>): Int = packets
    .chunked(2)
    .mapIndexedNotNull { i, pair -> (i + 1).takeIf { pair[0] <= pair[1] } }
    .sum()

private fun part2(packets: List<Packet>): Int {
    val divider2 = Packet.parse("[[2]]")
    val divider6 = Packet.parse("[[6]]")
    val sorted = (packets + divider2 + divider6).sorted()
    val indexOf2 = sorted.indexOf(divider2)
    val indexOf6 = sorted.indexOf(divider6)
    return (indexOf2 + 1) * (indexOf6 + 1)
}


