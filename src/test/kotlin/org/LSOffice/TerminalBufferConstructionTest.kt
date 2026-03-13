package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TerminalBufferConstructionTest {

    @Test
    fun `dimensions are stored correctly`() {
        val buf = TerminalBuffer(80, 24, 100)
        assertEquals(80, buf.width)
        assertEquals(24, buf.height)
        assertEquals(100, buf.maxScrollback)
    }

    @Test
    fun `screen is initialised with exactly height lines`() {
        val buf = TerminalBuffer(10, 5, 0)
        assertEquals(5, buf.screen.size)
    }

    @Test
    fun `screen lines are all empty on construction`() {
        val buf = TerminalBuffer(4, 3, 0)
        for (row in 0 until buf.height) {
            for (col in 0 until buf.width) {
                assertEquals(Cell.empty, buf.screen[row].getCell(col))
            }
        }
    }

    @Test
    fun `scrollback is empty on construction`() {
        val buf = TerminalBuffer(10, 5, 50)
        assertTrue(buf.scrollback.isEmpty())
    }

    @Test
    fun `width less than 1 throws`() {
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(0, 5, 10) }
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(-1, 5, 10) }
    }

    @Test
    fun `height less than 1 throws`() {
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(10, 0, 10) }
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(10, -1, 10) }
    }

    @Test
    fun `negative maxScrollback throws`() {
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(10, 5, -1) }
    }

    @Test
    fun `maxScrollback zero is valid`() {
        val buf = TerminalBuffer(10, 5, 0)
        assertEquals(0, buf.maxScrollback)
    }

    @Test
    fun `clearScreen resets all cells to empty`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.writeText("hello world abc")
        buf.clearScreen()
        for (row in 0 until buf.height) {
            for (col in 0 until buf.width) {
                assertEquals(Cell.empty, buf.screen[row].getCell(col))
            }
        }
    }

    @Test
    fun `clearScreen resets cursor to (0,0)`() {
        val buf = TerminalBuffer(10, 5, 10)
        buf.setCursorPosition(7, 3)
        buf.clearScreen()
        assertEquals(Pair(0, 0), buf.getCursorPosition())
    }

    @Test
    fun `clearAll clears screen and scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.writeText("hello")
        buf.insertEmptyLineAtBottom()
        buf.clearAll()
        assertTrue(buf.scrollback.isEmpty())
        for (row in 0 until buf.height) {
            for (col in 0 until buf.width) {
                assertEquals(Cell.empty, buf.screen[row].getCell(col))
            }
        }
    }
}
