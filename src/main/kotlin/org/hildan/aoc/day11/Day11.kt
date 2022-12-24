package org.hildan.aoc.day11

import org.hildan.aoc.client.inputFile
import kotlin.io.path.readText

fun main() {
    val input = inputFile(day = 11).readText()
    // monkeys are mutable, we must read them twice
    println(part1(readMonkeys(input)))
    println(part2(readMonkeys(input)))
}

private fun readMonkeys(input: String) = monkeyRegex.findAll(input).map { it.toMonkey() }.toList()

private fun part1(monkeys: List<Monkey>): Long {
    return monkeys.computeBusinessAfterNRounds(20) { it / 3 }
}

private fun part2(monkeys: List<Monkey>): Long {
    val divisorsGcd = monkeys.map { it.testDivisor }.reduce(Long::times)
    return monkeys.computeBusinessAfterNRounds(10000) { it % divisorsGcd }
}

private fun List<Monkey>.computeBusinessAfterNRounds(nRounds: Int, adjustWorry: (Long) -> Long): Long {
    repeat(nRounds) {
        computeRound(adjustWorry)
    }
    return getMonkeyBusiness()
}

private fun List<Monkey>.computeRound(adjustWorry: (Long) -> Long) {
    forEach { m ->
        m.items.forEach {
            it.worryLevel = adjustWorry(m.increaseWorry(it.worryLevel))
            val destMonkeyIndex = m.findDestMonkeyIndex(it.worryLevel)
            this[destMonkeyIndex].items.add(it)
        }
        m.nInspections += m.items.size
        m.items.clear()
    }
}

private fun List<Monkey>.getMonkeyBusiness() =
    map { it.nInspections }.sortedDescending().take(2).reduce(Long::times)

private class Monkey(
    val index: Int,
    val items: MutableList<Item>,
    val operator: String,
    val opValue: String,
    val testDivisor: Long,
    val destMonkeyTrue: Int,
    val destMonkeyFalse: Int,
) {
    val increaseWorry = operation(operator, opValue)
    var nInspections: Long = 0

    fun findDestMonkeyIndex(worryLevel: Long): Int =
        if (worryLevel % testDivisor == 0L) destMonkeyTrue else destMonkeyFalse

    override fun toString(): String = "Monkey $index: ${items.map { it.worryLevel }}"
}

private fun operation(operator: String, value: String): (Long) -> Long {
    if (value == "old") {
        return when (operator) {
            "+" -> {{ it * 2 }}
            "*" -> {{ it * it }}
            else -> error("Unknown operator $operator")
        }
    }
    val v = value.toLong()
    return when (operator) {
        "+" -> {{ it + v }}
        "*" -> {{ it * v }}
        else -> error("Unknown operator $operator")
    }
}

private class Item(var worryLevel: Long)

@Suppress("RegExpRepeatedSpace")
private val monkeyRegex = Regex("""
    Monkey (?<index>\d+):
      Starting items: (?<items>(\d+, )*\d+)
      Operation: new = old (?<op>[+\-*\\]) (?<opValue>\w+)
      Test: divisible by (?<testDiv>\d+)
        If true: throw to monkey (?<monkeyTrue>\d+)
        If false: throw to monkey (?<monkeyFalse>\d+)
""".trimIndent())

private fun MatchResult.toMonkey(): Monkey = Monkey(
    index = groupValue("index").toInt(),
    items = groupValue("items").split(", ").mapTo(mutableListOf()) { Item(it.toLong()) },
    operator = groupValue("op"),
    opValue = groupValue("opValue"),
    testDivisor = groupValue("testDiv").toLong(),
    destMonkeyTrue = groupValue("monkeyTrue").toInt(),
    destMonkeyFalse = groupValue("monkeyFalse").toInt(),
)

private fun MatchResult.groupValue(name: String) = groups[name]?.value ?: error("missing '$name' group")
