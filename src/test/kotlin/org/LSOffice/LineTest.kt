package org.LSOffice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class LineTest {
    @Test
    fun `cells initialise to EMPTY`() {
        val line = Line(5)
        for (i in 0 until 5) assertEquals(Cell.empty, line.getCell(i))
    }

    @Test
    fun `getCell and setCell round-trip`() {
        val line = Line(4)
        val cell = Cell('A', ForegroundColor.Red)
        line.setCell(2, cell)
        assertEquals(cell, line.getCell(2))
    }

    @Test
    fun `fill sets all cells to given char with default attributes`() {
        val line = Line(3)
        line.fill('X')
        for (i in 0 until 3) {
            assertEquals('X', line.getCell(i).char)
            assertEquals(ForegroundColor.Default, line.getCell(i).fg)
        }
    }

    @Test
    fun `fill with null resets cells to null char`() {
        val line = Line(3)
        line.fill('A')
        line.fill(null)
        for (i in 0 until 3) assertEquals(Cell.empty, line.getCell(i))
    }

    @Test
    fun `toDisplayString returns space for null chars`() {
        val line = Line(4)
        assertEquals("    ", line.toDisplayString())
    }

    @Test
    fun `toDisplayString returns exactly width chars`() {
        val line = Line(3)
        line.fill('B')
        assertEquals("BBB", line.toDisplayString())
        assertEquals(3, line.toDisplayString().length)
    }

    @Test
    fun `clone produces independent copy`() {
        val original = Line(3)
        original.fill('Z')
        val clone = original.clone()
        assertNotSame(original, clone)
        clone.setCell(0, Cell('A'))
        assertEquals('Z', original.getCell(0).char)
        assertEquals('A', clone.getCell(0).char)
    }

    @Test
    fun `constructor throws for width less than 1`() {
        assertFailsWith<IllegalArgumentException> { Line(0) }
        assertFailsWith<IllegalArgumentException> { Line(-1) }
    }

    @Test
    fun `getCell throws for out-of-bounds col`() {
        val line = Line(3)
        assertFailsWith<IndexOutOfBoundsException> { line.getCell(-1) }
        assertFailsWith<IndexOutOfBoundsException> { line.getCell(3) }
    }

    @Test
    fun `setCell throws for out-of-bounds col`() {
        val line = Line(3)
        assertFailsWith<IndexOutOfBoundsException> { line.setCell(-1, Cell.empty) }
        assertFailsWith<IndexOutOfBoundsException> { line.setCell(3, Cell.empty) }
    }
}
