package org.LSOffice

enum class CursorDirection { Up, Down, Left, Right }

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int,
) {
    // screen is always exactly height lines; scrollback grows from the top
    val screen: ArrayDeque<Line> = ArrayDeque()
    val scrollback: ArrayDeque<Line> = ArrayDeque()

    private var cursorCol: Int = 0
    private var cursorRow: Int = 0

    // current write attributes applied to new cells
    private var currentFg: ForegroundColor = ForegroundColor.Default
    private var currentBg: BackgroundColor = BackgroundColor.Default
    private var currentBold: Boolean = false
    private var currentItalic: Boolean = false
    private var currentUnderline: Boolean = false

    init {
        require(width >= 1) { "width must be >= 1" }
        require(height >= 1) { "height must be >= 1" }
        require(maxScrollback >= 0) { "maxScrollback must be >= 0" }
        repeat(height) { screen.addLast(Line(width)) }
    }

    // attributes functions

    fun setAttributes(
        fg: ForegroundColor = ForegroundColor.Default,
        bg: BackgroundColor = BackgroundColor.Default,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
    ) {
        currentFg = fg
        currentBg = bg
        currentBold = bold
        currentItalic = italic
        currentUnderline = underline
    }

    fun resetAttributes() = setAttributes()

    // cursor functions

    fun getCursorPosition(): Pair<Int, Int> = Pair(cursorCol, cursorRow)

    fun setCursorPosition(
        col: Int,
        row: Int,
    ) {
        cursorCol = col.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveCursor(
        direction: CursorDirection,
        n: Int = 1,
    ) {
        if (n <= 0) return
        when (direction) {
            CursorDirection.Up -> cursorRow = (cursorRow - n).coerceAtLeast(0)
            CursorDirection.Down -> cursorRow = (cursorRow + n).coerceAtMost(height - 1)
            CursorDirection.Left -> cursorCol = (cursorCol - n).coerceAtLeast(0)
            CursorDirection.Right -> cursorCol = (cursorCol + n).coerceAtMost(width - 1)
        }
    }

    // editing functions

    fun writeText(text: String) {
        for (ch in text) {
            screen[cursorRow].setCell(cursorCol, Cell(ch, currentFg, currentBg, currentBold, currentItalic, currentUnderline))
            cursorCol++
            if (cursorCol >= width) {
                cursorCol = 0
                cursorRow++
                if (cursorRow >= height) {
                    // stop at last cell
                    cursorRow = height - 1
                    cursorCol = width - 1
                    return
                }
            }
        }
    }

    fun insertText(text: String) {
        for (ch in text) {
            insertCharAt(cursorCol, cursorRow, ch)
            cursorCol++
            if (cursorCol >= width) {
                cursorCol = 0
                cursorRow++
                if (cursorRow >= height) {
                    cursorRow = height - 1
                    cursorCol = width - 1
                    return
                }
            }
        }
    }

    // shift [col..width-2] right by 1, overflow spills to next row recursively; last row discards
    private fun insertCharAt(
        col: Int,
        row: Int,
        ch: Char,
    ) {
        val line = screen[row]
        val overflowCell = line.getCell(width - 1)
        for (i in width - 1 downTo col + 1) line.setCell(i, line.getCell(i - 1))
        line.setCell(col, Cell(ch, currentFg, currentBg, currentBold, currentItalic, currentUnderline))
        if (row < height - 1 && overflowCell.char != null) {
            insertCharAt(0, row + 1, overflowCell.char)
        }
    }

    fun fillLine(
        row: Int,
        ch: Char?,
    ) {
        val r = row.coerceIn(0, height - 1)
        screen[r].fill(ch)
        // apply current attributes to each cell
        for (col in 0 until width) {
            screen[r].setCell(col, Cell(ch, currentFg, currentBg, currentBold, currentItalic, currentUnderline))
        }
    }

    // implementing scrollback

    fun insertEmptyLineAtBottom() {
        val evicted = screen.removeFirst()
        scrollback.addLast(evicted.clone())
        if (scrollback.size > maxScrollback) scrollback.removeFirst()
        screen.addLast(Line(width))
        // cursor position unchanged
    }

    // content access functions

    fun getChar(
        col: Int,
        row: Int,
        fromScrollback: Boolean = false,
    ): Char? {
        val source = if (fromScrollback) scrollback else screen
        if (row < 0 || row >= source.size) {
            throw IndexOutOfBoundsException("row $row out of bounds")
        }
        if (col < 0 || col >= width) {
            throw IndexOutOfBoundsException("col $col out of bounds")
        }
        return source[row].getCell(col).char
    }

    fun getAttributes(
        col: Int,
        row: Int,
        fromScrollback: Boolean = false,
    ): CellAttributes {
        val source = if (fromScrollback) scrollback else screen
        if (row < 0 || row >= source.size) {
            throw IndexOutOfBoundsException("row $row out of bounds")
        }
        if (col < 0 || col >= width) {
            throw IndexOutOfBoundsException("col $col out of bounds")
        }
        return source[row].getCell(col).toAttributes()
    }

    fun getLine(
        row: Int,
        fromScrollback: Boolean = false,
    ): String {
        val source = if (fromScrollback) scrollback else screen
        if (row < 0 || row >= source.size) {
            throw IndexOutOfBoundsException("row $row out of bounds")
        }
        return source[row].toDisplayString()
    }

    fun getScreenContent(): String = screen.joinToString("\n") { it.toDisplayString() }

    fun getAllContent(): String = (scrollback + screen).joinToString("\n") { it.toDisplayString() }

    // resets visible area and cursor; scrollback is intentionally untouched
    fun clearScreen() {
        for (line in screen) line.fill(null)
        cursorCol = 0
        cursorRow = 0
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }
}
