package org.LSOffice

enum class CursorDirection { Up, Down, Left, Right }

class TerminalBuffer(
    var width: Int,
    var height: Int,
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
            val cw = charWidth(ch)
            if (cw == 2) {
                // if only 1 column left, pad it with empty and wrap before writing
                if (cursorCol == width - 1) {
                    screen[cursorRow].setCell(cursorCol, Cell.empty)
                    cursorCol = 0
                    cursorRow++
                    if (cursorRow >= height) {
                        cursorRow = height - 1
                        cursorCol = width - 1
                        return
                    }
                }
                screen[cursorRow].setCell(
                    cursorCol,
                    Cell(
                        ch,
                        currentFg,
                        currentBg,
                        currentBold,
                        currentItalic,
                        currentUnderline,
                        isWide = true,
                    ),
                )
                screen[cursorRow].setCell(cursorCol + 1, Cell(null, isContinuation = true))
                cursorCol += 2
            } else {
                screen[cursorRow].setCell(cursorCol, Cell(ch, currentFg, currentBg, currentBold, currentItalic, currentUnderline))
                cursorCol++
            }
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
            val cw = charWidth(ch)
            if (cw == 2) {
                // if only 1 column left, pad and wrap before inserting
                if (cursorCol == width - 1) {
                    screen[cursorRow].setCell(cursorCol, Cell.empty)
                    cursorCol = 0
                    cursorRow++
                    if (cursorRow >= height) {
                        cursorRow = height - 1
                        cursorCol = width - 1
                        return
                    }
                }
                insertWideCharAt(cursorCol, cursorRow, ch)
                cursorCol += 2
            } else {
                insertCharAt(cursorCol, cursorRow, ch)
                cursorCol++
            }
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
        // skip continuation cells — their wide-char half was already pushed off
        if (row < height - 1 && overflowCell.char != null && !overflowCell.isContinuation) {
            insertCharAt(0, row + 1, overflowCell.char)
        }
    }

    // shift [col..width-3] right by 2; both displaced cells overflow to the next row in order
    private fun insertWideCharAt(
        col: Int,
        row: Int,
        ch: Char,
    ) {
        val line = screen[row]
        // capture both cells that are pushed off the right edge before the shift overwrites them
        val overflow1 = line.getCell(width - 2)
        val overflow2 = line.getCell(width - 1)
        for (i in width - 1 downTo col + 2) line.setCell(i, line.getCell(i - 2))
        line.setCell(col, Cell(ch, currentFg, currentBg, currentBold, currentItalic, currentUnderline, isWide = true))
        line.setCell(col + 1, Cell(null, isContinuation = true))
        if (row < height - 1) {
            // insert in order: overflow1 at 0, then overflow2 at 1 (insertCharAt shifts the row each time)
            if (overflow1.char != null && !overflow1.isContinuation) insertCharAt(0, row + 1, overflow1.char)
            if (overflow2.char != null && !overflow2.isContinuation) insertCharAt(1, row + 1, overflow2.char)
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

    // resize the buffer — truncates or pads lines; clamps cursor to new bounds
    fun resize(
        newWidth: Int,
        newHeight: Int,
    ) {
        require(newWidth >= 1) { "width must be >= 1" }
        require(newHeight >= 1) { "height must be >= 1" }
        for (i in screen.indices) screen[i] = screen[i].resizedTo(newWidth)
        for (i in scrollback.indices) scrollback[i] = scrollback[i].resizedTo(newWidth)
        while (screen.size < newHeight) screen.addLast(Line(newWidth))
        while (screen.size > newHeight) screen.removeLast()
        width = newWidth
        height = newHeight
        cursorCol = cursorCol.coerceIn(0, newWidth - 1)
        cursorRow = cursorRow.coerceIn(0, newHeight - 1)
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

// returns the terminal column width of a BMP character (1 for normal, 2 for CJK/fullwidth)
// note: supplementary characters (emoji above U+FFFF) require String-level handling
internal fun charWidth(ch: Char): Int {
    val cp = ch.code
    return when {
        cp < 0x1100 -> 1
        cp <= 0x115F -> 2 // hangul jamo
        cp == 0x2329 || cp == 0x232A -> 2
        cp in 0x2E80..0x303E -> 2 // CJK radicals, kangxi
        cp in 0x3040..0x33FF -> 2 // hiragana, katakana, CJK compatibility
        cp in 0x3400..0x4DBF -> 2 // CJK extension A
        cp in 0x4E00..0x9FFF -> 2 // CJK unified ideographs
        cp in 0xA000..0xA4CF -> 2 // yi
        cp in 0xAC00..0xD7AF -> 2 // hangul syllables
        cp in 0xF900..0xFAFF -> 2 // CJK compatibility ideographs
        cp in 0xFE10..0xFE19 -> 2 // vertical forms
        cp in 0xFE30..0xFE4F -> 2 // CJK compatibility forms
        cp in 0xFF00..0xFF60 -> 2 // fullwidth forms
        cp in 0xFFE0..0xFFE6 -> 2 // fullwidth signs
        else -> 1
    }
}
