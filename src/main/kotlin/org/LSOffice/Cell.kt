package org.LSOffice

// immutable and every write produces a new cell via copy()
data class Cell(
    val char: Char?,
    val fg: ForegroundColor = ForegroundColor.Default,
    val bg: BackgroundColor = BackgroundColor.Default,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
) {
    fun toAttributes(): CellAttributes =
        CellAttributes(fg, bg, bold, italic, underline)

    companion object {
        // blank cell used to initialise and clear the grid
        val empty = Cell(char = null)
    }
}
