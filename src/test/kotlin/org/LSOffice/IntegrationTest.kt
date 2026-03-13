package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {

    @Test
    fun `fill screen and scroll verifies getAllContent ordering`() {
        val buf = TerminalBuffer(3, 3, 10)
        buf.fillLine(0, 'A')
        buf.fillLine(1, 'B')
        buf.fillLine(2, 'C')
        buf.insertEmptyLineAtBottom() // A evicted to scrollback
        buf.insertEmptyLineAtBottom() // B evicted to scrollback

        val lines = buf.getAllContent().split("\n")
        // scrollback oldest→newest: A, B; screen: C, empty, empty
        assertEquals("AAA", lines[0])
        assertEquals("BBB", lines[1])
        assertEquals("CCC", lines[2])
        assertEquals("   ", lines[3])
        assertEquals("   ", lines[4])
    }

    @Test
    fun `attribute fidelity through scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setAttributes(ForegroundColor.Red, bold = true)
        buf.writeText("X")
        buf.insertEmptyLineAtBottom()

        val attrs = buf.getAttributes(0, 0, fromScrollback = true)
        assertEquals(ForegroundColor.Red, attrs.fg)
        assertEquals(true, attrs.bold)
    }

    @Test
    fun `insertText at last col flows to next row`() {
        val buf = TerminalBuffer(3, 3, 0)
        buf.writeText("ABC")
        buf.setCursorPosition(2, 0)
        buf.insertText("X")
        assertEquals('X', buf.getChar(2, 0))
        assertEquals('C', buf.getChar(0, 1))
    }

    @Test
    fun `fillLine then writeText overwrites correctly`() {
        val buf = TerminalBuffer(5, 3, 0)
        buf.fillLine(0, 'Z')
        buf.setCursorPosition(1, 0)
        buf.writeText("AB")
        assertEquals('Z', buf.getChar(0, 0))
        assertEquals('A', buf.getChar(1, 0))
        assertEquals('B', buf.getChar(2, 0))
        assertEquals('Z', buf.getChar(3, 0))
    }

    @Test
    fun `maxScrollback 0 getAllContent equals getScreenContent`() {
        val buf = TerminalBuffer(4, 3, 0)
        buf.fillLine(0, 'X')
        buf.insertEmptyLineAtBottom()
        assertEquals(buf.getScreenContent(), buf.getAllContent())
    }

    @Test
    fun `clearScreen preserves scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.writeText("HELLO")
        buf.insertEmptyLineAtBottom()
        val scrollbackBefore = buf.getLine(0, fromScrollback = true)
        buf.clearScreen()
        assertEquals(1, buf.scrollback.size)
        assertEquals(scrollbackBefore, buf.getLine(0, fromScrollback = true))
    }

    @Test
    fun `full write read round-trip`() {
        val buf = TerminalBuffer(10, 5, 0)
        val positions = listOf(Pair(0, 0), Pair(5, 2), Pair(9, 4))
        val chars = listOf('X', 'Y', 'Z')
        positions.zip(chars).forEach { (pos, ch) ->
            buf.setCursorPosition(pos.first, pos.second)
            buf.writeText(ch.toString())
        }
        positions.zip(chars).forEach { (pos, ch) ->
            assertEquals(ch, buf.getChar(pos.first, pos.second))
        }
    }

    @Test
    fun `screen size invariant holds across many scrolls`() {
        val buf = TerminalBuffer(5, 4, 20)
        repeat(50) { buf.insertEmptyLineAtBottom() }
        assertEquals(4, buf.screen.size)
        assertTrue(buf.scrollback.size <= 20)
    }
}
