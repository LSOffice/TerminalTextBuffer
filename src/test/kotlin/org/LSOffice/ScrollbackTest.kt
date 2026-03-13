package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScrollbackTest {

    @Test
    fun `insertEmptyLineAtBottom adds empty line at bottom of screen`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.writeText("AAAAA")
        buf.insertEmptyLineAtBottom()
        assertEquals("     ", buf.getLine(buf.height - 1))
    }

    @Test
    fun `insertEmptyLineAtBottom evicts top line to scrollback`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.writeText("HELLO")
        buf.insertEmptyLineAtBottom()
        assertEquals(1, buf.scrollback.size)
        assertEquals("HELLO", buf.getLine(0, fromScrollback = true))
    }

    @Test
    fun `scrollback grows up to maxScrollback`() {
        val buf = TerminalBuffer(3, 2, 3)
        repeat(3) { i ->
            buf.fillLine(0, ('A' + i))
            buf.insertEmptyLineAtBottom()
        }
        assertEquals(3, buf.scrollback.size)
    }

    @Test
    fun `scrollback evicts oldest when full`() {
        val buf = TerminalBuffer(3, 2, 2)
        buf.fillLine(0, 'A')
        buf.insertEmptyLineAtBottom()
        buf.fillLine(0, 'B')
        buf.insertEmptyLineAtBottom()
        buf.fillLine(0, 'C')
        buf.insertEmptyLineAtBottom()
        // scrollback holds last 2: B then C
        assertEquals(2, buf.scrollback.size)
        assertEquals("BBB", buf.getLine(0, fromScrollback = true))
        assertEquals("CCC", buf.getLine(1, fromScrollback = true))
    }

    @Test
    fun `maxScrollback 0 keeps scrollback empty`() {
        val buf = TerminalBuffer(5, 2, 0)
        buf.fillLine(0, 'X')
        buf.insertEmptyLineAtBottom()
        assertTrue(buf.scrollback.isEmpty())
    }

    @Test
    fun `screen always has exactly height lines after scroll`() {
        val buf = TerminalBuffer(4, 3, 10)
        repeat(5) { buf.insertEmptyLineAtBottom() }
        assertEquals(3, buf.screen.size)
    }

    @Test
    fun `cursor position is unchanged by insertEmptyLineAtBottom`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.setCursorPosition(3, 1)
        buf.insertEmptyLineAtBottom()
        assertEquals(Pair(3, 1), buf.getCursorPosition())
    }

    @Test
    fun `clearScreen preserves scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.fillLine(0, 'Z')
        buf.insertEmptyLineAtBottom()
        buf.clearScreen()
        assertEquals(1, buf.scrollback.size)
        assertEquals("ZZZZZ", buf.getLine(0, fromScrollback = true))
    }

    @Test
    fun `scrollback ordering oldest to newest`() {
        val buf = TerminalBuffer(1, 2, 5)
        buf.fillLine(0, 'A'); buf.insertEmptyLineAtBottom()
        buf.fillLine(0, 'B'); buf.insertEmptyLineAtBottom()
        buf.fillLine(0, 'C'); buf.insertEmptyLineAtBottom()
        // scrollback[0]=oldest, scrollback[last]=newest
        assertEquals("A", buf.getLine(0, fromScrollback = true))
        assertEquals("B", buf.getLine(1, fromScrollback = true))
        assertEquals("C", buf.getLine(2, fromScrollback = true))
    }
}
