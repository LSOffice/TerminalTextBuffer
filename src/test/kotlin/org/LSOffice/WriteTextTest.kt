package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WriteTextTest {
    @Test
    fun `writeText places chars starting at cursor`() {
        val buf = TerminalBuffer(10, 3, 0)
        buf.writeText("ABC")
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('C', buf.getChar(2, 0))
    }

    @Test
    fun `writeText advances cursor by text length`() {
        val buf = TerminalBuffer(10, 3, 0)
        buf.writeText("ABC")
        assertEquals(Pair(3, 0), buf.getCursorPosition())
    }

    @Test
    fun `writeText wraps to next row when col reaches width`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("ABCDE") // fills row 0
        buf.writeText("X") // should wrap to row 1 col 0
        assertEquals('X', buf.getChar(0, 1))
        assertEquals(Pair(1, 1), buf.getCursorPosition())
    }

    @Test
    fun `writeText stops at last cell when screen is full`() {
        val buf = TerminalBuffer(3, 2, 0)
        buf.writeText("ABCDEFGH") // more than 3*2=6 cells
        assertEquals(Pair(2, 1), buf.getCursorPosition())
        assertEquals('F', buf.getChar(2, 1))
    }

    @Test
    fun `writeText overwrites existing content`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("AAAAA")
        buf.setCursorPosition(2, 0)
        buf.writeText("B")
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('A', buf.getChar(1, 0))
        assertEquals('B', buf.getChar(2, 0))
        assertEquals('A', buf.getChar(3, 0))
    }

    @Test
    fun `writeText stores current attributes in written cells`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setAttributes(ForegroundColor.Red, BackgroundColor.Blue, bold = true, italic = false, underline = true)
        buf.writeText("X")
        val attrs = buf.getAttributes(0, 0)
        assertEquals(ForegroundColor.Red, attrs.fg)
        assertEquals(BackgroundColor.Blue, attrs.bg)
        assertTrue(attrs.bold)
        assertTrue(attrs.underline)
    }

    @Test
    fun `writeText from mid-row cursor position`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setCursorPosition(3, 1)
        buf.writeText("AB")
        assertEquals('A', buf.getChar(3, 1))
        assertEquals('B', buf.getChar(4, 1))
        assertEquals(Pair(0, 2), buf.getCursorPosition())
    }

    @Test
    fun `writeText empty string is no-op`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setCursorPosition(2, 1)
        buf.writeText("")
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }

    @Test
    fun `fillLine fills all cells with given char and current attributes`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setAttributes(fg = ForegroundColor.Green)
        buf.fillLine(1, 'Z')
        for (col in 0 until buf.width) {
            assertEquals('Z', buf.getChar(col, 1))
            assertEquals(ForegroundColor.Green, buf.getAttributes(col, 1).fg)
        }
    }

    @Test
    fun `fillLine with null clears the row`() {
        val buf = TerminalBuffer(4, 3, 0)
        buf.writeText("ABCD")
        buf.fillLine(0, null)
        for (col in 0 until buf.width) {
            assertEquals(null, buf.getChar(col, 0))
        }
    }

    @Test
    fun `fillLine clamps out-of-range row silently`() {
        val buf = TerminalBuffer(4, 3, 0)
        buf.fillLine(-5, 'X') // should clamp to row 0
        assertEquals('X', buf.getChar(0, 0))
        buf.fillLine(99, 'Y') // should clamp to row 2
        assertEquals('Y', buf.getChar(0, 2))
    }

    @Test
    fun `writeText when cursor already at last cell writes one char and stays`() {
        val buf = TerminalBuffer(3, 2, 0)
        buf.setCursorPosition(2, 1)
        buf.writeText("Z")
        assertEquals('Z', buf.getChar(2, 1))
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }

    @Test
    fun `fillLine does not move cursor`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setCursorPosition(3, 2)
        buf.fillLine(0, 'A')
        assertEquals(Pair(3, 2), buf.getCursorPosition())
    }
}
