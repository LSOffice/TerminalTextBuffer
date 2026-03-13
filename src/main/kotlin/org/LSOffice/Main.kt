package org.LSOffice

// For testing
fun main() {
    var buf = TerminalBuffer(width = 20, height = 5, maxScrollback = 10)

    println("TerminalBuffer REPL — 20×5, scrollback 10")
    println("commands: write <text> | move <up|down|left|right> [n] | pos <col> <row> |")
    println("          scroll | clear | clearall | screen | all | cursor | help | quit")
    println()

    while (true) {
        print("> ")
        val line = readLine() ?: break
        val parts = line.trim().split(" ", limit = 2)
        val cmd = parts[0].lowercase()
        val arg = parts.getOrNull(1)?.trim() ?: ""

        when (cmd) {
            "write" -> {
                buf.writeText(arg)
                printScreen(buf)
            }
            "move" -> {
                val moveParts = arg.split(" ")
                val dir = when (moveParts[0].lowercase()) {
                    "up"    -> CursorDirection.Up
                    "down"  -> CursorDirection.Down
                    "left"  -> CursorDirection.Left
                    "right" -> CursorDirection.Right
                    else    -> null
                }
                if (dir == null) { println("unknown direction: ${moveParts[0]}"); continue }
                val n = moveParts.getOrNull(1)?.toIntOrNull() ?: 1
                buf.moveCursor(dir, n)
                println("cursor → ${buf.getCursorPosition()}")
            }
            "pos" -> {
                val p = arg.split(" ")
                val col = p[0].toIntOrNull()
                val row = p.getOrNull(1)?.toIntOrNull()
                if (col == null || row == null) { println("usage: pos <col> <row>"); continue }
                buf.setCursorPosition(col, row)
                println("cursor → ${buf.getCursorPosition()}")
            }
            "scroll" -> {
                buf.insertEmptyLineAtBottom()
                printScreen(buf)
            }
            "clear" -> {
                buf.clearScreen()
                printScreen(buf)
            }
            "clearall" -> {
                buf.clearAll()
                printScreen(buf)
            }
            "screen" -> printScreen(buf)
            "all" -> {
                println("--- scrollback + screen ---")
                println(buf.getAllContent())
                println("---")
            }
            "cursor" -> println("cursor → ${buf.getCursorPosition()}")
            "new" -> {
                val p = arg.split(" ")
                val w = p[0].toIntOrNull() ?: 20
                val h = p.getOrNull(1)?.toIntOrNull() ?: 5
                val s = p.getOrNull(2)?.toIntOrNull() ?: 10
                buf = TerminalBuffer(w, h, s)
                println("new buffer ${w}×${h}, scrollback $s")
            }
            "help" -> {
                println("write <text>               write text at cursor")
                println("move <up|down|left|right>  move cursor (optional n steps)")
                println("pos <col> <row>            set cursor position")
                println("scroll                     insert empty line at bottom (scrolls up)")
                println("clear                      clear screen, reset cursor")
                println("clearall                   clear screen + scrollback")
                println("screen                     print current screen")
                println("all                        print scrollback + screen")
                println("cursor                     show cursor position")
                println("new [w] [h] [scrollback]   create a new buffer")
                println("quit                       exit")
            }
            "quit", "exit", "q" -> break
            "" -> {}
            else -> println("unknown command: $cmd  (type help)")
        }
    }
}

private fun printScreen(buf: TerminalBuffer) {
    val (col, row) = buf.getCursorPosition()
    println("┌${"─".repeat(buf.width)}┐")
    for (r in 0 until buf.height) {
        val lineStr = buf.getLine(r, fromScrollback = false)
        if (r == row) {
            // mark cursor position with brackets
            val marked = lineStr.toMutableList()
            marked[col] = if (marked[col] == ' ') '█' else marked[col]
            println("│${marked.joinToString("")}│ ← cursor")
        } else {
            println("│$lineStr│")
        }
    }
    println("└${"─".repeat(buf.width)}┘")
    println("cursor (${col}, ${row})  scrollback: ${buf.scrollback.size}")
}
