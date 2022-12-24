package org.hildan.aoc.day21

import org.hildan.aoc.client.inputLines

fun main() {
    val monkeys = inputLines(day = 21).associate { it.parseMonkey() }
    println(Monkeys(monkeys).compute("root"))

    val monkeysWithVar = monkeys + ("humn" to Expr.Human)
    println(Monkeys(monkeysWithVar).computeRootEquation())
}

private class Monkeys(val expressionsByName: Map<String, Expr>) {
    fun computeRootEquation(): Long {
        val expr = expressionsByName.getValue("root") as Expr.Op // root is the equation
        return if (!expr.m2.hasHuman()) {
            computeHumanValue(expr.m1, expectedValue = compute(expr.m2))
        } else {
            computeHumanValue(expr.m2, expectedValue = compute(expr.m1))
        }
    }

    private fun computeHumanValue(name: String, expectedValue: Long): Long {
        return when (val expr = expressionsByName.getValue(name)) {
            is Expr.Human -> expectedValue
            is Expr.Literal -> error("No human here")
            is Expr.Op -> {
                when {
                    expr.m1.hasHuman() -> {
                        val r2 = compute(expr.m2)
                        val m1ExpectedValue = when(expr.op) {
                            "+" -> expectedValue - r2 // expectedValue = m1 + m2
                            "-" -> expectedValue + r2
                            "*" -> expectedValue / r2
                            "/" -> expectedValue * r2
                            else -> error("Unknown operator ${expr.op}")
                        }
                        return computeHumanValue(expr.m1, m1ExpectedValue)
                    }
                    expr.m2.hasHuman() -> {
                        val r1 = compute(expr.m1)
                        val m2ExpectedValue = when(expr.op) {
                            "+" -> expectedValue - r1 // expectedValue = m1 + m2
                            "-" -> r1 - expectedValue // expectedValue = m1 - m2
                            "*" -> expectedValue / r1 // expectedValue = m1 * m2
                            "/" -> r1 / expectedValue // expectedValue = m1 / m2
                            else -> error("Unknown operator ${expr.op}")
                        }
                        return computeHumanValue(expr.m2, m2ExpectedValue)
                    }
                    else -> error("No human here")
                }
            }
        }
    }

    private fun String.hasHuman(): Boolean = expressionsByName.getValue(this).hasHuman()

    private fun Expr.hasHuman(): Boolean = when (this) {
        is Expr.Human -> true
        is Expr.Literal -> false
        is Expr.Op -> m1.hasHuman() || m2.hasHuman()
    }

    fun compute(name: String): Long = when (val expr = expressionsByName.getValue(name)) {
        is Expr.Op -> {
            val result1 = compute(expr.m1)
            val result2 = compute(expr.m2)
            when (expr.op) {
                "+" -> result1 + result2
                "-" -> result1 - result2
                "*" -> result1 * result2
                "/" -> result1 / result2
                else -> error("Unknown operator ${expr.op}")
            }
        }
        is Expr.Literal -> expr.value
        is Expr.Human -> error("unsupported Human in part 1")
    }
}

private fun String.parseMonkey(): Pair<String, Expr> {
    val (monkeyName, expr) = split(": ")
    return monkeyName to expr.parseExpression()
}

private fun String.parseExpression(): Expr {
    val parts = split(" ")
    if (parts.size == 1) {
        return Expr.Literal(parts[0].toLong())
    }
    return Expr.Op(m1 = parts[0], m2 = parts[2], op = parts[1])
}

sealed class Expr {
    data class Op(val m1: String, val m2: String, val op: String): Expr()
    data class Literal(val value: Long): Expr()
    data object Human : Expr()
}
