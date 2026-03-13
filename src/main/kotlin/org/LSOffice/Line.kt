package org.LSOffice

// one row of the terminal grid, fixed width for the lifetime of the line
class Line(val width: Int) {
    private val cells: MutableList<Cell>

    init {
        require(width >= 1) { "width must be >= 1" }
        cells = MutableList(width) { Cell.empty }
    }

    fun getCell(col: Int): Cell {
        if (col !in 0..<width) {
            throw IndexOutOfBoundsException(
                "col $col out of bounds for width $width",
            )
        }
        return cells[col]
    }

    fun setCell(
        col: Int,
        cell: Cell,
    ) {
        if (col !in 0..<width) {
            throw IndexOutOfBoundsException(
                "col $col out of bounds for width $width",
            )
        }
        cells[col] = cell
    }

    // replaces every cell's char and preserves no prior attributes
    fun fill(ch: Char?) {
        for (i in 0 until width) cells[i] = Cell.empty.copy(char = ch)
    }

    // null char renders as space so the string is always exactly width chars
    fun toDisplayString(): String =
        buildString(width) {
            for (cell in cells) append(cell.char ?: ' ')
        }

    // returns a new line at newWidth — truncates on shrink, pads with EMPTY on grow
    fun resizedTo(newWidth: Int): Line {
        val newLine = Line(newWidth)
        val copyUpTo = minOf(width, newWidth)
        for (i in 0 until copyUpTo) newLine.setCell(i, getCell(i))
        return newLine
    }

    // deep copy - which is used before pushing to scrollback
    fun clone(): Line {
        val copy = Line(width)
        for (i in 0 until width) copy.cells[i] = cells[i]
        return copy
    }
}
