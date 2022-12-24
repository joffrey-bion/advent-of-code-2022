package org.hildan.aoc.day7

import org.hildan.aoc.client.inputLines
import java.math.BigInteger

fun main() {
    println(part1())
    println(part2())
}

private fun part1() = parseInputFileStructure()
    .walkTopDown()
    .filterIsInstance<FileTreeNode.Dir>()
    .map { it.size }
    .filter { it <= 100000.toBigInteger() }
    .sumOf { it }

private fun part2(): BigInteger {
    val rootDir = parseInputFileStructure()
    val currentFreeSpace = 70000000.toBigInteger() - rootDir.size
    val spaceToFree = 30000000.toBigInteger() - currentFreeSpace

    val smallestSuitableDir = rootDir
        .walkTopDown()
        .filterIsInstance<FileTreeNode.Dir>()
        .filter { it.size >= spaceToFree }
        .minBy { it.size }
    return smallestSuitableDir.size
}

private fun parseInputFileStructure() =
    inputLines(day = 7).map { it.parseTerminalLine() }.parseFileStructure()

private fun List<TerminalLine>.parseFileStructure(): FileTreeNode.Dir {
    var currentDir: FileTreeNode.Dir? = null

    forEach { termLine ->
        when (termLine) {
            is TerminalLine.Command.Ls -> Unit // ignore because we just read the files and dirs afterwards
            is TerminalLine.Command.Cd -> currentDir = when (termLine.dirName) {
                "/" -> FileTreeNode.Dir("/")
                ".." -> currentDir!!.parent ?: error("no parent, we're at the root")
                else -> currentDir!!.findSubdir(termLine.dirName) ?: currentDir!!.createNewSubdir(termLine)
            }

            is TerminalLine.Output.File -> currentDir!!.children.add(FileTreeNode.File(termLine.name, termLine.size))
            is TerminalLine.Output.Dir -> {
                val dir = FileTreeNode.Dir(termLine.name, parent = currentDir)
                currentDir!!.children.add(dir)
            }
        }
    }
    return currentDir?.findRoot()?.also { check(it.name == "/") } ?: error("No files were parsed")
}

private fun String.parseTerminalLine(): TerminalLine = when {
    startsWith("$ ls") -> TerminalLine.Command.Ls
    startsWith("$ cd ") -> TerminalLine.Command.Cd(dirName = removePrefix("$ cd "))
    startsWith("dir ") -> TerminalLine.Output.Dir(name = removePrefix("dir "))
    else -> {
        val (size, name) = split(" ")
        TerminalLine.Output.File(name = name, size = size.toBigInteger())
    }
}

sealed class TerminalLine {

    sealed class Command : TerminalLine() {
        data object Ls : Command()
        data class Cd(val dirName: String) : Command()
    }

    sealed class Output : TerminalLine() {
        data class Dir(val name: String) : Output()
        data class File(val name: String, val size: BigInteger) : Output()
    }
}

sealed class FileTreeNode {

    abstract val name: String
    abstract val size: BigInteger
    abstract val children: List<FileTreeNode>

    data class Dir(
        override val name: String,
        val parent: Dir? = null,
        override val children: MutableList<FileTreeNode> = mutableListOf(),
    ) : FileTreeNode() {
        override val size: BigInteger
            get() = children.sumOf { it.size }

        override fun toString(): String = "Dir '$name', size = $size, ${children.size} children"
    }

    data class File(
        override val name: String,
        override val size: BigInteger,
    ) : FileTreeNode() {
        override val children: List<FileTreeNode> = emptyList()
    }

    fun formatTree(): String = buildString {
        if (this@FileTreeNode is Dir)
            append("+ $name (=$size)")
        else
            append("- $name $size")
        if (children.isNotEmpty()) {
            appendLine()
            append(children.joinToString(separator = "\n") { it.formatTree() }.prependIndent("  "))
        }
    }
}

private fun FileTreeNode.Dir.findRoot(): FileTreeNode.Dir = parent?.findRoot() ?: this.also { check(it.name == "/") }

private fun FileTreeNode.Dir.findSubdir(name: String): FileTreeNode.Dir? =
    children.filterIsInstance<FileTreeNode.Dir>().find { it.name == name }

private fun FileTreeNode.Dir.createNewSubdir(termLine: TerminalLine.Command.Cd): FileTreeNode.Dir =
    FileTreeNode.Dir(termLine.dirName, parent = this).also { children.add(it) }

private fun FileTreeNode.walkTopDown(): Sequence<FileTreeNode> = sequence {
    yield(this@walkTopDown)
    yieldAll(children.flatMap { it.walkTopDown() })
}
