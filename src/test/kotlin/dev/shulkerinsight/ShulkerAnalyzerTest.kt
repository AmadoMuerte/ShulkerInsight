package dev.shulkerinsight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShulkerAnalyzerTest {
    @Test
    fun emptyListIsEmpty() {
        val result = ShulkerAnalyzer.analyze(emptyList(), ShulkerAnalyzer.SLOT_COUNT)

        assertTrue(result.empty)
        assertNull(result.uniformType)
        assertEquals(0f, result.fillRatio)
    }

    @Test
    fun oneFullStackFillsOneSlot() {
        val result = analyze(ShulkerAnalyzer.Slot("stone", 64, 64))

        assertEquals("stone", result.uniformType)
        assertEquals(1f / 27f, result.fillRatio, 1e-6f)
    }

    @Test
    fun sameTypeWithDifferentCountsIsUniform() {
        val result = analyze(
            ShulkerAnalyzer.Slot("stone", 1, 64),
            ShulkerAnalyzer.Slot("stone", 32, 64),
        )

        assertEquals("stone", result.uniformType)
        assertTrue(result.isUniform)
        assertFalse(result.empty)
    }

    @Test
    fun differentTypesAreMixed() {
        val result = analyze(
            ShulkerAnalyzer.Slot("stone", 64, 64),
            ShulkerAnalyzer.Slot("cobblestone", 64, 64),
        )

        assertNull(result.uniformType)
        assertFalse(result.empty)
    }

    @Test
    fun fullTwentySevenSlotsFillContainer() {
        assertEquals(1f, fullSlots(64, 64).fillRatio, 1e-6f)
    }

    @Test
    fun fullSixteenStackSlotsFillContainer() {
        assertEquals(1f, fullSlots(16, 16).fillRatio, 1e-6f)
    }

    @Test
    fun fullUnstackableSlotsFillContainer() {
        assertEquals(1f, fullSlots(1, 1).fillRatio, 1e-6f)
    }

    @Test
    fun partialStackUsesItsMaxStackSize() {
        assertEquals(0.5f / 27f, analyze(ShulkerAnalyzer.Slot("stone", 32, 64)).fillRatio, 1e-6f)
    }

    @Test
    fun countsAboveMaxAreCapped() {
        assertEquals(1f / 27f, analyze(ShulkerAnalyzer.Slot("stone", 100, 64)).fillRatio, 1e-6f)
    }

    @Test
    fun zeroCountSlotsAreIgnored() {
        val result = analyze(
            ShulkerAnalyzer.Slot("cobblestone", 0, 64),
            ShulkerAnalyzer.Slot("stone", 64, 64),
        )

        assertEquals("stone", result.uniformType)
        assertEquals(1f / 27f, result.fillRatio, 1e-6f)
    }

    private fun analyze(vararg slots: ShulkerAnalyzer.Slot): ShulkerAnalyzer.Result =
        ShulkerAnalyzer.analyze(listOf(*slots), ShulkerAnalyzer.SLOT_COUNT)

    private fun fullSlots(count: Int, maxStackSize: Int): ShulkerAnalyzer.Result =
        analyze(*Array(ShulkerAnalyzer.SLOT_COUNT) { ShulkerAnalyzer.Slot("stone", count, maxStackSize) })
}
