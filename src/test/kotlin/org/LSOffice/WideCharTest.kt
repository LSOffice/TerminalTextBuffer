package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WideCharTest {
    @Test
    fun `charWidth returns 1 for ascii`() {
        assertEquals(1, charWidth('A'))
        assertEquals(1, charWidth(' '))
    }

    @Test
    fun `charWidth returns 2 for CJK ideograph`() {
        assertEquals(2, charWidth('你')) // U+4F60 CJK unified
        assertEquals(2, charWidth('日')) // U+65E5
        assertEquals(2, charWidth('あ')) // U+3042 hiragana
    }

    @Test
    fun `charWidth returns 2 for fullwidth latin`() {
        assertEquals(2, charWidth('Ａ')) // U+FF21 fullwidth A
    }

    @Test
    fun `writeText wide char places isWide and continuation cells`() {
        val buf = TerminalBuffer(6, 2, 0)
        buf.writeText("你")
        val left = buf.screen[0].getCell(0)
        val right = buf.screen[0].getCell(1)
        assertEquals('你', left.char)
        assertTrue(left.isWide)
        assertFalse(left.isContinuation)
        assertNull(right.char)
        assertFalse(right.isWide)
        assertTrue(right.isContinuation)
    }

    @Test
    fun `writeText wide char advances cursor by 2`() {
        val buf = TerminalBuffer(6, 2, 0)
        buf.writeText("你")
        assertEquals(Pair(2, 0), buf.getCursorPosition())
    }

    @Test
    fun `writeText mixed ascii and wide chars advance correctly`() {
        val buf = TerminalBuffer(8, 2, 0)
        buf.writeText("A你B")
        // A=0, 你=1-2(wide+cont), B=3
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('你', buf.getChar(1, 0))
        assertNull(buf.getChar(2, 0)) // continuation
        assertEquals('B', buf.getChar(3, 0))
        assertEquals(Pair(4, 0), buf.getCursorPosition())
    }

    @Test
    fun `writeText wide char at last column pads and wraps to next row`() {
        val buf = TerminalBuffer(4, 3, 0)
        buf.setCursorPosition(3, 0) // only 1 col left
        buf.writeText("你")
        // col 3 padded with empty, wide char wraps to row 1
        assertEquals(Cell.empty, buf.screen[0].getCell(3))
        assertTrue(buf.screen[1].getCell(0).isWide)
        assertEquals('你', buf.getChar(0, 1))
        assertEquals(Pair(2, 1), buf.getCursorPosition())
    }

    @Test
    fun `writeText wide char at last column of last row stops`() {
        val buf = TerminalBuffer(4, 2, 0)
        buf.setCursorPosition(3, 1) // last col, last row
        buf.writeText("你")
        // can't write — treated same as stop at last cell
        assertEquals(Pair(3, 1), buf.getCursorPosition())
    }

    @Test
    fun `toDisplayString represents continuation as space — length stays width`() {
        val buf = TerminalBuffer(5, 2, 0)
        buf.writeText("你A")
        val s = buf.getLine(0)
        assertEquals(5, s.length)
        assertEquals('你', s[0])
        assertEquals(' ', s[1]) // continuation renders as space
        assertEquals('A', s[2])
    }

    @Test
    fun `wide char attributes are stored on left cell only`() {
        val buf = TerminalBuffer(6, 2, 0)
        buf.setAttributes(ForegroundColor.Red, bold = true)
        buf.writeText("你")
        val left = buf.screen[0].getCell(0)
        val right = buf.screen[0].getCell(1)
        assertEquals(ForegroundColor.Red, left.fg)
        assertTrue(left.bold)
        // continuation carries no attributes
        assertEquals(ForegroundColor.Default, right.fg)
        assertFalse(right.bold)
    }

    @Test
    fun `insertText wide char shifts existing content by 2`() {
        val buf = TerminalBuffer(6, 2, 0)
        buf.writeText("ABCD") // cols 0-3
        buf.setCursorPosition(0, 0)
        buf.insertText("你")
        // 你(wide)+cont at 0-1, ABCD shift to 2-5
        assertTrue(buf.screen[0].getCell(0).isWide)
        assertTrue(buf.screen[0].getCell(1).isContinuation)
        assertEquals('A', buf.getChar(2, 0))
        assertEquals('B', buf.getChar(3, 0))
        assertEquals('C', buf.getChar(4, 0))
        assertEquals('D', buf.getChar(5, 0))
    }

    @Test
    fun `insertText wide char overflows last char to next row`() {
        val buf = TerminalBuffer(4, 2, 0)
        buf.writeText("ABCD") // fills row 0
        buf.setCursorPosition(0, 0)
        buf.insertText("你")
        // 你+cont at 0-1, A B at 2-3; C D overflow
        // C is at width-1 before insert: overflows to row 1 col 0
        assertEquals('你', buf.getChar(0, 0))
        assertEquals('A', buf.getChar(2, 0))
        assertEquals('B', buf.getChar(3, 0))
        assertEquals('C', buf.getChar(0, 1)) // overflow
    }

    @Test
    fun `insertText wide char advances cursor by 2`() {
        val buf = TerminalBuffer(6, 2, 0)
        buf.insertText("你")
        assertEquals(Pair(2, 0), buf.getCursorPosition())
    }
}
