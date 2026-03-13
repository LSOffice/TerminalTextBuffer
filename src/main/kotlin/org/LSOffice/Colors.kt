package org.LSOffice

// standard ansi foreground colors
// default lets the terminal decide
enum class ForegroundColor {
    Default,
    Black, Red, Green, Yellow, Blue, Magenta, Cyan, White,
    BrightBlack, BrightRed, BrightGreen, BrightYellow,
    BrightBlue, BrightMagenta, BrightCyan, BrightWhite
}

enum class BackgroundColor {
    Default,
    Black, Red, Green, Yellow, Blue, Magenta, Cyan, White,
    BrightBlack, BrightRed, BrightGreen, BrightYellow,
    BrightBlue, BrightMagenta, BrightCyan, BrightWhite
}

// snapshot of display attributes
// attached to each cell rather than held globally
data class CellAttributes(
    val fg: ForegroundColor = ForegroundColor.Default,
    val bg: BackgroundColor = BackgroundColor.Default,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
)
