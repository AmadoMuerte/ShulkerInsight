package dev.shulkerinsight;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShulkerAnalyzerTest {
    @Test
    void emptyListIsEmpty() {
        ShulkerAnalyzer.Result result = ShulkerAnalyzer.analyze(List.of(), ShulkerAnalyzer.SLOT_COUNT);

        assertTrue(result.empty());
        assertNull(result.uniformType());
        assertEquals(0, result.fillRatio());
    }

    @Test
    void oneFullStackFillsOneSlot() {
        ShulkerAnalyzer.Result result = analyze(new ShulkerAnalyzer.Slot("stone", 64, 64));

        assertEquals("stone", result.uniformType());
        assertEquals(1f / 27f, result.fillRatio(), 1e-6f);
    }

    @Test
    void sameTypeWithDifferentCountsIsUniform() {
        ShulkerAnalyzer.Result result = analyze(
                new ShulkerAnalyzer.Slot("stone", 1, 64),
                new ShulkerAnalyzer.Slot("stone", 32, 64));

        assertEquals("stone", result.uniformType());
        assertTrue(result.isUniform());
        assertFalse(result.empty());
    }

    @Test
    void differentTypesAreMixed() {
        ShulkerAnalyzer.Result result = analyze(
                new ShulkerAnalyzer.Slot("stone", 64, 64),
                new ShulkerAnalyzer.Slot("cobblestone", 64, 64));

        assertNull(result.uniformType());
        assertFalse(result.empty());
    }

    @Test
    void fullTwentySevenSlotsFillContainer() {
        assertEquals(1f, fullSlots(64, 64).fillRatio(), 1e-6f);
    }

    @Test
    void fullSixteenStackSlotsFillContainer() {
        assertEquals(1f, fullSlots(16, 16).fillRatio(), 1e-6f);
    }

    @Test
    void fullUnstackableSlotsFillContainer() {
        assertEquals(1f, fullSlots(1, 1).fillRatio(), 1e-6f);
    }

    @Test
    void partialStackUsesItsMaxStackSize() {
        assertEquals(0.5f / 27f, analyze(new ShulkerAnalyzer.Slot("stone", 32, 64)).fillRatio(), 1e-6f);
    }

    @Test
    void countsAboveMaxAreCapped() {
        assertEquals(1f / 27f, analyze(new ShulkerAnalyzer.Slot("stone", 100, 64)).fillRatio(), 1e-6f);
    }

    @Test
    void zeroCountSlotsAreIgnored() {
        ShulkerAnalyzer.Result result = analyze(
                new ShulkerAnalyzer.Slot("cobblestone", 0, 64),
                new ShulkerAnalyzer.Slot("stone", 64, 64));

        assertEquals("stone", result.uniformType());
        assertEquals(1f / 27f, result.fillRatio(), 1e-6f);
    }

    private static ShulkerAnalyzer.Result analyze(ShulkerAnalyzer.Slot... slots) {
        return ShulkerAnalyzer.analyze(List.of(slots), ShulkerAnalyzer.SLOT_COUNT);
    }

    private static ShulkerAnalyzer.Result fullSlots(int count, int maxStackSize) {
        ShulkerAnalyzer.Slot[] slots = new ShulkerAnalyzer.Slot[ShulkerAnalyzer.SLOT_COUNT];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ShulkerAnalyzer.Slot("stone", count, maxStackSize);
        }
        return analyze(slots);
    }
}
