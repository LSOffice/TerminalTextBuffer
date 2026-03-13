package org.LSOffice

// for testing
fun main() {
    var buf = TerminalBuffer(width = 20, height = 5, maxScrollback = 10)
    var currentAttrs = AttrsState()

    println("TerminalBuffer REPL — 20×5, scrollback 10")
    println("commands: write | insert | move | pos | attr | reset | scroll | clear | clearall | all | new | help | quit")
    println()
    printScreen(buf, currentAttrs)

    while (true) {
        print("> ")
        val line = readLine() ?: break
        val parts = line.trim().split(" ", limit = 2)
        val cmd = parts[0].lowercase()
        val arg = parts.getOrNull(1)?.trim() ?: ""

        when (cmd) {
            "write" -> buf.writeText(arg)
            "insert" -> buf.insertText(arg)
            "move" -> {
                val moveParts = arg.split(" ")
                val dir =
                    when (moveParts[0].lowercase()) {
                        "up" -> CursorDirection.Up
                        "down" -> CursorDirection.Down
                        "left" -> CursorDirection.Left
                        "right" -> CursorDirection.Right
                        else -> null
                    }
                if (dir == null) {
                    println("unknown direction: ${moveParts[0]}")
                    continue
                }
                buf.moveCursor(dir, moveParts.getOrNull(1)?.toIntOrNull() ?: 1)
            }
            "pos" -> {
                val p = arg.split(" ")
                val col = p[0].toIntOrNull()
                val row = p.getOrNull(1)?.toIntOrNull()
                if (col == null || row == null) {
                    println("usage: pos <col> <row>")
                    continue
                }
                buf.setCursorPosition(col, row)
            }
            "attr" -> {
                if (arg.isEmpty()) {
                    println(currentAttrs.describe())
                    continue
                }
                val updated = parseAttr(arg, currentAttrs)
                if (updated == null) {
                    println("unknown attr. try: fg red | bg blue | bold | italic | underline")
                    continue
                }
                currentAttrs = updated
                buf.setAttributes(currentAttrs.fg, currentAttrs.bg, currentAttrs.bold, currentAttrs.italic, currentAttrs.underline)
                println("attrs: ${currentAttrs.describe()}")
                continue
            }
            "reset" -> {
                currentAttrs = AttrsState()
                buf.resetAttributes()
                println("attrs reset")
                continue
            }
            "scroll" -> buf.insertEmptyLineAtBottom()
            "clear" -> buf.clearScreen()
            "clearall" -> buf.clearAll()
            "all" -> {
                println("--- scrollback + screen ---")
                println(buf.getAllContent())
                println("---")
                continue
            }
            "new" -> {
                val p = arg.split(" ")
                val w = p[0].toIntOrNull() ?: 20
                val h = p.getOrNull(1)?.toIntOrNull() ?: 5
                val s = p.getOrNull(2)?.toIntOrNull() ?: 10
                buf = TerminalBuffer(w, h, s)
                currentAttrs = AttrsState()
                println("new buffer $w×$h, scrollback $s")
            }
            "help" -> {
                println("write <text>                    write text at cursor")
                println("insert <text>                   insert text at cursor (shifts existing)")
                println("move <up|down|left|right> [n]   move cursor")
                println("pos <col> <row>                 set cursor position")
                println("attr [fg|bg <color>|bold|italic|underline|no-bold|no-italic|no-underline]")
                println("                                set attributes (no arg = show current)")
                println("  colors: default black red green yellow blue magenta cyan white")
                println("          bright-black bright-red bright-green bright-yellow")
                println("          bright-blue bright-magenta bright-cyan bright-white")
                println("reset                           reset attributes to defaults")
                println("scroll                          insert empty line at bottom")
                println("clear                           clear screen, reset cursor")
                println("clearall                        clear screen + scrollback")
                println("all                             print scrollback + screen")
                println("new [w] [h] [scrollback]        create a new buffer")
                println("quit                            exit")
                continue
            }
            "quit", "exit", "q" -> break
            "" -> continue
            else -> {
                println("unknown command: $cmd  (type help)")
                continue
            }
        }

        printScreen(buf, currentAttrs)
    }
}

// holds the REPL's view of current attributes so we can display them in the prompt
private data class AttrsState(
    val fg: ForegroundColor = ForegroundColor.Default,
    val bg: BackgroundColor = BackgroundColor.Default,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
) {
    fun describe(): String {
        val flags =
            listOfNotNull(
                if (bold) "bold" else null,
                if (italic) "italic" else null,
                if (underline) "underline" else null,
            )
        val flagStr = if (flags.isEmpty()) "" else " ${flags.joinToString("+")}"
        return "fg=${fg.name.lowercase()} bg=${bg.name.lowercase()}$flagStr"
    }
}

private fun parseAttr(
    arg: String,
    current: AttrsState,
): AttrsState? {
    val tokens = arg.lowercase().split(" ")
    return when (tokens[0]) {
        "fg" -> parseFg(tokens.getOrNull(1), current)
        "bg" -> parseBg(tokens.getOrNull(1), current)
        "bold" -> current.copy(bold = true)
        "no-bold" -> current.copy(bold = false)
        "italic" -> current.copy(italic = true)
        "no-italic" -> current.copy(italic = false)
        "underline" -> current.copy(underline = true)
        "no-underline" -> current.copy(underline = false)
        else -> null
    }
}

private fun parseFg(
    name: String?,
    current: AttrsState,
): AttrsState? {
    val color = parseFgColor(name ?: return null) ?: return null
    return current.copy(fg = color)
}

private fun parseBg(
    name: String?,
    current: AttrsState,
): AttrsState? {
    val color = parseBgColor(name ?: return null) ?: return null
    return current.copy(bg = color)
}

private fun parseFgColor(name: String): ForegroundColor? =
    when (name) {
        "default" -> ForegroundColor.Default
        "black" -> ForegroundColor.Black
        "red" -> ForegroundColor.Red
        "green" -> ForegroundColor.Green
        "yellow" -> ForegroundColor.Yellow
        "blue" -> ForegroundColor.Blue
        "magenta" -> ForegroundColor.Magenta
        "cyan" -> ForegroundColor.Cyan
        "white" -> ForegroundColor.White
        "bright-black" -> ForegroundColor.BrightBlack
        "bright-red" -> ForegroundColor.BrightRed
        "bright-green" -> ForegroundColor.BrightGreen
        "bright-yellow" -> ForegroundColor.BrightYellow
        "bright-blue" -> ForegroundColor.BrightBlue
        "bright-magenta" -> ForegroundColor.BrightMagenta
        "bright-cyan" -> ForegroundColor.BrightCyan
        "bright-white" -> ForegroundColor.BrightWhite
        else -> null
    }

private fun parseBgColor(name: String): BackgroundColor? =
    when (name) {
        "default" -> BackgroundColor.Default
        "black" -> BackgroundColor.Black
        "red" -> BackgroundColor.Red
        "green" -> BackgroundColor.Green
        "yellow" -> BackgroundColor.Yellow
        "blue" -> BackgroundColor.Blue
        "magenta" -> BackgroundColor.Magenta
        "cyan" -> BackgroundColor.Cyan
        "white" -> BackgroundColor.White
        "bright-black" -> BackgroundColor.BrightBlack
        "bright-red" -> BackgroundColor.BrightRed
        "bright-green" -> BackgroundColor.BrightGreen
        "bright-yellow" -> BackgroundColor.BrightYellow
        "bright-blue" -> BackgroundColor.BrightBlue
        "bright-magenta" -> BackgroundColor.BrightMagenta
        "bright-cyan" -> BackgroundColor.BrightCyan
        "bright-white" -> BackgroundColor.BrightWhite
        else -> null
    }

private fun printScreen(
    buf: TerminalBuffer,
    attrs: AttrsState,
) {
    val (cursorCol, cursorRow) = buf.getCursorPosition()
    val reset = "\u001B[0m"
    println("┌${"─".repeat(buf.width)}┐")
    for (r in 0 until buf.height) {
        print("│")
        for (c in 0 until buf.width) {
            val cell = buf.screen[r].getCell(c)
            val isCursor = r == cursorRow && c == cursorCol
            if (isCursor) {
                // render cursor as inverted block
                print("\u001B[7m${cell.char ?: '█'}$reset")
            } else {
                val ansi = ansiCode(cell.fg, cell.bg, cell.bold, cell.italic, cell.underline)
                if (ansi.isEmpty()) {
                    print(cell.char ?: ' ')
                } else {
                    print("$ansi${cell.char ?: ' '}$reset")
                }
            }
        }
        val cursorMarker = if (r == cursorRow) " ← cursor" else ""
        println("│$cursorMarker")
    }
    println("└${"─".repeat(buf.width)}┘")
    println("cursor ($cursorCol, $cursorRow)  scrollback: ${buf.scrollback.size}  attrs: ${attrs.describe()}")
}

private fun ansiCode(
    fg: ForegroundColor,
    bg: BackgroundColor,
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
): String {
    val codes = mutableListOf<Int>()
    if (bold) codes += 1
    if (italic) codes += 3
    if (underline) codes += 4
    fgCode(fg)?.let { codes += it }
    bgCode(bg)?.let { codes += it }
    return if (codes.isEmpty()) "" else "\u001B[${codes.joinToString(";")}m"
}

private fun fgCode(fg: ForegroundColor): Int? =
    when (fg) {
        ForegroundColor.Default -> null
        ForegroundColor.Black -> 30
        ForegroundColor.Red -> 31
        ForegroundColor.Green -> 32
        ForegroundColor.Yellow -> 33
        ForegroundColor.Blue -> 34
        ForegroundColor.Magenta -> 35
        ForegroundColor.Cyan -> 36
        ForegroundColor.White -> 37
        ForegroundColor.BrightBlack -> 90
        ForegroundColor.BrightRed -> 91
        ForegroundColor.BrightGreen -> 92
        ForegroundColor.BrightYellow -> 93
        ForegroundColor.BrightBlue -> 94
        ForegroundColor.BrightMagenta -> 95
        ForegroundColor.BrightCyan -> 96
        ForegroundColor.BrightWhite -> 97
    }

private fun bgCode(bg: BackgroundColor): Int? =
    when (bg) {
        BackgroundColor.Default -> null
        BackgroundColor.Black -> 40
        BackgroundColor.Red -> 41
        BackgroundColor.Green -> 42
        BackgroundColor.Yellow -> 43
        BackgroundColor.Blue -> 44
        BackgroundColor.Magenta -> 45
        BackgroundColor.Cyan -> 46
        BackgroundColor.White -> 47
        BackgroundColor.BrightBlack -> 100
        BackgroundColor.BrightRed -> 101
        BackgroundColor.BrightGreen -> 102
        BackgroundColor.BrightYellow -> 103
        BackgroundColor.BrightBlue -> 104
        BackgroundColor.BrightMagenta -> 105
        BackgroundColor.BrightCyan -> 106
        BackgroundColor.BrightWhite -> 107
    }
