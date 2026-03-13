package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse

class CellTest {

    @Test
    fun `EMPTY has null char and default attributes`() {
        val e = Cell.empty
        assertNull(e.char)
        assertEquals(ForegroundColor.Default, e.fg)
        assertEquals(BackgroundColor.Default, e.bg)
        assertFalse(e.bold)
        assertFalse(e.italic)
        assertFalse(e.underline)
    }

    @Test
    fun `equality is structural`() {
        val a = Cell('A', ForegroundColor.Red, BackgroundColor.Blue, bold = true)
        val b = Cell('A', ForegroundColor.Red, BackgroundColor.Blue, bold = true)
        assertEquals(a, b)
    }

    @Test
    fun `copy changes only specified fields`() {
        val original = Cell('X', bold = true)
        val modified = original.copy(char = 'Y', italic = true)
        assertEquals('Y', modified.char)
        assertEquals(true, modified.bold)
        assertEquals(true, modified.italic)
        assertEquals(ForegroundColor.Default, modified.fg)
    }

    @Test
    fun `toAttributes round-trip`() {
        val cell = Cell('Z', ForegroundColor.Green, BackgroundColor.Red, bold = true, italic = false, underline = true)
        val attrs = cell.toAttributes()
        assertEquals(CellAttributes(ForegroundColor.Green, BackgroundColor.Red, bold = true, italic = false, underline = true), attrs)
    }

    @Test
    fun `toAttributes on EMPTY returns default CellAttributes`() {
        assertEquals(CellAttributes(), Cell.empty.toAttributes())
    }
}
