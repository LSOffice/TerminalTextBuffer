package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InsertTextTest {
    @Test
    fun `insertText shifts existing chars right`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("BCDE") // occupies cols 0-3; col 4 is empty
        buf.setCursorPosition(0, 0)
        buf.insertText("A")
        // A inserted, B..E shift right one position, nothing overflows
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('C', buf.getChar(2, 0))
        assertEquals('D', buf.getChar(3, 0))
        assertEquals('E', buf.getChar(4, 0))
    }

    @Test
    fun `insertText overflows last col char to next row`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("BCDEF") // fills entire row 0
        buf.setCursorPosition(0, 0)
        buf.insertText("A")
        // F (old col 4) overflows to row 1 col 0
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('F', buf.getChar(0, 1))
    }

    @Test
    fun `insertText at end of row pushes overflow to next row`() {
        val buf = TerminalBuffer(3, 3, 0)
        buf.writeText("ABC")
        buf.setCursorPosition(2, 0)
        buf.insertText("X")
        // X inserted at col 2, C (old last) overflows to row 1 col 0
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('X', buf.getChar(2, 0))
        assertEquals('C', buf.getChar(0, 1))
    }

    @Test
    fun `insertText overflow on last row is discarded`() {
        val buf = TerminalBuffer(3, 2, 0)
        buf.writeText("ABCDEF") // fills both rows: ABC / DEF
        buf.setCursorPosition(0, 1)
        buf.insertText("X")
        // X inserted, D and E shift right, F is discarded
        assertEquals('X', buf.getChar(0, 1))
        assertEquals('D', buf.getChar(1, 1))
        assertEquals('E', buf.getChar(2, 1))
    }

    @Test
    fun `insertText advances cursor same as writeText`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.insertText("AB")
        assertEquals(Pair(2, 0), buf.getCursorPosition())
    }

    @Test
    fun `insertText wraps cursor to next row at width`() {
        val buf = TerminalBuffer(3, 3, 0)
        buf.setCursorPosition(2, 0)
        buf.insertText("XY")
        // after X: cursor wraps to (0,1); after Y: cursor at (1,1)
        assertEquals(Pair(1, 1), buf.getCursorPosition())
    }

    @Test
    fun `insertText stops at last cell when screen is full`() {
        val buf = TerminalBuffer(2, 2, 0)
        buf.insertText("ABCDE") // 4 cells total, extra discarded
        assertEquals(Pair(1, 1), buf.getCursorPosition())
    }

    @Test
    fun `insertText stores current attributes`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setAttributes(fg = ForegroundColor.Magenta, bold = true)
        buf.insertText("Z")
        val attrs = buf.getAttributes(0, 0)
        assertEquals(ForegroundColor.Magenta, attrs.fg)
        assertEquals(true, attrs.bold)
    }

    @Test
    fun `insertText into empty row leaves rest empty`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.insertText("A")
        assertEquals('A', buf.getChar(0, 0))
        assertNull(buf.getChar(1, 0))
        assertNull(buf.getChar(2, 0))
    }

    @Test
    fun `insertText mid-row shifts only chars from cursor onward`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("ABCD")
        buf.setCursorPosition(2, 0)
        buf.insertText("X")
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
        assertEquals('X', buf.getChar(2, 0))
        assertEquals('C', buf.getChar(3, 0))
        assertEquals('D', buf.getChar(4, 0))
    }

    @Test
    fun `insertText empty string is no-op`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setCursorPosition(2, 1)
        buf.insertText("")
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }
}
