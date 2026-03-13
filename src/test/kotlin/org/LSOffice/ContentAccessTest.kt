package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ContentAccessTest {
    // fresh 5×3 screen buffer, no scrollback
    private fun screenBuf() = TerminalBuffer(5, 3, 0)

    // 5×2 buffer with one line already in scrollback
    private fun scrollBuf(): TerminalBuffer {
        val buf = TerminalBuffer(5, 2, 10)
        buf.insertEmptyLineAtBottom()
        return buf
    }

    @Test
    fun `getChar returns written char`() {
        val buf = screenBuf()
        buf.writeText("AB")
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(1, 0))
    }

    @Test
    fun `getChar returns null for empty cell`() {
        assertNull(screenBuf().getChar(4, 2))
    }

    @Test
    fun `getChar from scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.writeText("HELLO")
        buf.insertEmptyLineAtBottom()
        assertEquals('H', buf.getChar(0, 0, fromScrollback = true))
        assertEquals('O', buf.getChar(4, 0, fromScrollback = true))
    }

    @Test
    fun `getChar throws on out-of-bounds row`() {
        val buf = screenBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(0, -1) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(0, 3) }
    }

    @Test
    fun `getChar throws on out-of-bounds col`() {
        val buf = screenBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(-1, 0) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(5, 0) }
    }

    @Test
    fun `getChar throws on out-of-bounds row in scrollback`() {
        val buf = scrollBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(0, -1, fromScrollback = true) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(0, 1, fromScrollback = true) }
    }

    @Test
    fun `getChar throws on out-of-bounds col in scrollback`() {
        val buf = scrollBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(-1, 0, fromScrollback = true) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getChar(5, 0, fromScrollback = true) }
    }

    @Test
    fun `getAttributes returns attributes of written cell`() {
        val buf = screenBuf()
        buf.setAttributes(ForegroundColor.Cyan, BackgroundColor.Black, bold = true)
        buf.writeText("X")
        val attrs = buf.getAttributes(0, 0)
        assertEquals(ForegroundColor.Cyan, attrs.fg)
        assertEquals(BackgroundColor.Black, attrs.bg)
        assertEquals(true, attrs.bold)
    }

    @Test
    fun `getAttributes returns default CellAttributes for empty cell`() {
        assertEquals(CellAttributes(), screenBuf().getAttributes(0, 0))
    }

    @Test
    fun `getAttributes from scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setAttributes(ForegroundColor.Yellow, bold = true)
        buf.writeText("X")
        buf.insertEmptyLineAtBottom()
        val attrs = buf.getAttributes(0, 0, fromScrollback = true)
        assertEquals(ForegroundColor.Yellow, attrs.fg)
        assertEquals(true, attrs.bold)
    }

    @Test
    fun `getAttributes throws on out-of-bounds`() {
        val buf = screenBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getAttributes(0, 99) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getAttributes(99, 0) }
    }

    @Test
    fun `getAttributes throws on out-of-bounds in scrollback`() {
        val buf = scrollBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getAttributes(0, 1, fromScrollback = true) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getAttributes(99, 0, fromScrollback = true) }
    }

    @Test
    fun `getLine returns padded display string`() {
        val buf = screenBuf()
        buf.writeText("AB")
        assertEquals("AB   ", buf.getLine(0))
    }

    @Test
    fun `getLine pads empty row with spaces`() {
        assertEquals("    ", TerminalBuffer(4, 2, 0).getLine(0))
    }

    @Test
    fun `getLine throws on out-of-bounds row`() {
        val buf = screenBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getLine(-1) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getLine(3) }
    }

    @Test
    fun `getLine throws on out-of-bounds row in scrollback`() {
        val buf = scrollBuf()
        assertFailsWith<IndexOutOfBoundsException> { buf.getLine(-1, fromScrollback = true) }
        assertFailsWith<IndexOutOfBoundsException> { buf.getLine(1, fromScrollback = true) }
    }

    @Test
    fun `getLine from scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.writeText("WORLD")
        buf.insertEmptyLineAtBottom()
        assertEquals("WORLD", buf.getLine(0, fromScrollback = true))
    }

    @Test
    fun `getScreenContent joins all rows with newline`() {
        val buf = TerminalBuffer(3, 3, 0)
        buf.writeText("ABC")
        val lines = buf.getScreenContent().split("\n")
        assertEquals(3, lines.size)
        assertEquals("ABC", lines[0])
        assertEquals("   ", lines[1])
    }

    @Test
    fun `getAllContent includes scrollback then screen oldest to newest`() {
        val buf = TerminalBuffer(3, 2, 10)
        buf.fillLine(0, 'A')
        buf.insertEmptyLineAtBottom()
        buf.fillLine(0, 'B')
        val all = buf.getAllContent().split("\n")
        assertEquals("AAA", all[0])
        assertEquals("BBB", all[1])
        assertEquals("   ", all[2])
    }
}
