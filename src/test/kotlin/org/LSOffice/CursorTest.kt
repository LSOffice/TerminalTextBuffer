package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CursorTest {
    @Test
    fun `cursor starts at (0,0)`() {
        val buf = TerminalBuffer(10, 5, 0)
        assertEquals(Pair(0, 0), buf.getCursorPosition())
    }

    @Test
    fun `setCursorPosition sets exact position`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(3, 2)
        assertEquals(Pair(3, 2), buf.getCursorPosition())
    }

    @Test
    fun `setCursorPosition clamps col below 0`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(-5, 2)
        assertEquals(0, buf.getCursorPosition().first)
    }

    @Test
    fun `setCursorPosition clamps col above width-1`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(99, 2)
        assertEquals(9, buf.getCursorPosition().first)
    }

    @Test
    fun `setCursorPosition clamps row below 0`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(3, -1)
        assertEquals(0, buf.getCursorPosition().second)
    }

    @Test
    fun `setCursorPosition clamps row above height-1`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(3, 99)
        assertEquals(4, buf.getCursorPosition().second)
    }

    @Test
    fun `moveCursor up decrements row`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(0, 3)
        buf.moveCursor(CursorDirection.Up, 2)
        assertEquals(1, buf.getCursorPosition().second)
    }

    @Test
    fun `moveCursor down increments row`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(0, 1)
        buf.moveCursor(CursorDirection.Down, 2)
        assertEquals(3, buf.getCursorPosition().second)
    }

    @Test
    fun `moveCursor left decrements col`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(5, 0)
        buf.moveCursor(CursorDirection.Left, 3)
        assertEquals(2, buf.getCursorPosition().first)
    }

    @Test
    fun `moveCursor right increments col`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(2, 0)
        buf.moveCursor(CursorDirection.Right, 4)
        assertEquals(6, buf.getCursorPosition().first)
    }

    @Test
    fun `moveCursor clamps at top edge`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(0, 1)
        buf.moveCursor(CursorDirection.Up, 99)
        assertEquals(0, buf.getCursorPosition().second)
    }

    @Test
    fun `moveCursor clamps at bottom edge`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(0, 3)
        buf.moveCursor(CursorDirection.Down, 99)
        assertEquals(4, buf.getCursorPosition().second)
    }

    @Test
    fun `moveCursor clamps at left edge`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(2, 0)
        buf.moveCursor(CursorDirection.Left, 99)
        assertEquals(0, buf.getCursorPosition().first)
    }

    @Test
    fun `moveCursor clamps at right edge`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(7, 0)
        buf.moveCursor(CursorDirection.Right, 99)
        assertEquals(9, buf.getCursorPosition().first)
    }

    @Test
    fun `moveCursor n=0 is no-op`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(3, 2)
        buf.moveCursor(CursorDirection.Up, 0)
        buf.moveCursor(CursorDirection.Right, 0)
        assertEquals(Pair(3, 2), buf.getCursorPosition())
    }

    @Test
    fun `moveCursor negative n is no-op`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(3, 2)
        buf.moveCursor(CursorDirection.Down, -5)
        assertEquals(Pair(3, 2), buf.getCursorPosition())
    }

    @Test
    fun `setAttributes stores all fields`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setAttributes(ForegroundColor.Red, BackgroundColor.Blue, bold = true, italic = true, underline = true)
        buf.writeText("X")
        val attrs = buf.getAttributes(0, 0)
        assertEquals(ForegroundColor.Red, attrs.fg)
        assertEquals(BackgroundColor.Blue, attrs.bg)
        assertTrue(attrs.bold)
        assertTrue(attrs.italic)
        assertTrue(attrs.underline)
    }

    @Test
    fun `resetAttributes restores defaults`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.setAttributes(ForegroundColor.Green, BackgroundColor.Red, bold = true)
        buf.resetAttributes()
        buf.writeText("X")
        val attrs = buf.getAttributes(0, 0)
        assertEquals(ForegroundColor.Default, attrs.fg)
        assertEquals(BackgroundColor.Default, attrs.bg)
        assertFalse(attrs.bold)
    }
}
