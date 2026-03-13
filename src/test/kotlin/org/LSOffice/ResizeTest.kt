package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResizeTest {

    @Test
    fun `resize wider pads lines with empty cells`() {
        val buf = TerminalBuffer(3, 2, 0)
        buf.writeText("ABC")
        buf.resize(5, 2)
        assertEquals(5, buf.width)
        assertEquals("ABC  ", buf.getLine(0))
    }

    @Test
    fun `resize narrower truncates content`() {
        val buf = TerminalBuffer(5, 2, 0)
        buf.writeText("ABCDE")
        buf.resize(3, 2)
        assertEquals(3, buf.width)
        assertEquals("ABC", buf.getLine(0))
    }

    @Test
    fun `resize taller adds empty rows at bottom`() {
        val buf = TerminalBuffer(3, 2, 0)
        buf.resize(3, 4)
        assertEquals(4, buf.height)
        assertEquals(4, buf.screen.size)
        assertEquals("   ", buf.getLine(3))
    }

    @Test
    fun `resize shorter removes rows from bottom`() {
        val buf = TerminalBuffer(3, 4, 0)
        buf.fillLine(0, 'A')
        buf.fillLine(1, 'B')
        buf.resize(3, 2)
        assertEquals(2, buf.height)
        assertEquals(2, buf.screen.size)
        // top 2 rows survive
        assertEquals("AAA", buf.getLine(0))
        assertEquals("BBB", buf.getLine(1))
    }

    @Test
    fun `cursor outside new bounds is clamped`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(8, 4)
        buf.resize(4, 3)
        assertEquals(Pair(3, 2), buf.getCursorPosition())
    }

    @Test
    fun `cursor within new bounds is unchanged`() {
        val buf = TerminalBuffer(10, 5, 0)
        buf.setCursorPosition(2, 1)
        buf.resize(8, 4)
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }

    @Test
    fun `resize invalid dimensions throw`() {
        val buf = TerminalBuffer(5, 3, 0)
        assertFailsWith<IllegalArgumentException> { buf.resize(0, 3) }
        assertFailsWith<IllegalArgumentException> { buf.resize(5, 0) }
        assertFailsWith<IllegalArgumentException> { buf.resize(-1, 3) }
    }

    @Test
    fun `scrollback lines are resized too`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.writeText("HELLO")
        buf.insertEmptyLineAtBottom()
        buf.resize(3, 2)
        assertEquals(3, buf.scrollback[0].width)
        assertEquals("HEL", buf.getLine(0, fromScrollback = true))
    }

    @Test
    fun `screen always has exactly height lines after resize`() {
        val buf = TerminalBuffer(5, 5, 0)
        buf.resize(5, 3)
        assertEquals(3, buf.screen.size)
        buf.resize(5, 7)
        assertEquals(7, buf.screen.size)
    }

    @Test
    fun `content that fits in new dimensions is preserved`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("AB")
        buf.resize(4, 2)
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
    }

    @Test
    fun `resize to same dimensions is a no-op`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.writeText("ABC")
        buf.setCursorPosition(2, 1)
        buf.resize(5, 3)
        assertEquals("ABC  ", buf.getLine(0))
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }
}
