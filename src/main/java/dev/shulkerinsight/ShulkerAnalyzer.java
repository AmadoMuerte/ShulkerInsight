package dev.shulkerinsight;

import java.util.Objects;

public final class ShulkerAnalyzer {
    public static final int SLOT_COUNT = 27;
    private static final Object MIXED = new Object();

    /** Pure slot view: {@code type} is any identity key (Item in production), tests may use String. */
    public record Slot(Object type, int count, int maxStackSize) {}

    /** uniformType is null when empty OR mixed; fillRatio in [0,1]; empty means no non-empty slot. */
    public record Result(Object uniformType, float fillRatio, boolean empty) {
        public boolean isUniform() {
            return uniformType != null;
        }
    }

    private ShulkerAnalyzer() {}

    /** Single source of truth for uniform detection and fill calculation. */
    public static Result analyze(Iterable<Slot> slots, int slotCount) {
        Object uniformType = null;
        boolean typeSeen = false;
        boolean empty = true;
        float totalContribution = 0;

        for (Slot slot : slots) {
            if (slot == null || slot.count() <= 0) {
                continue;
            }

            empty = false;
            if (!typeSeen) {
                uniformType = slot.type();
                typeSeen = true;
            } else if (uniformType != MIXED && !Objects.equals(uniformType, slot.type())) {
                uniformType = MIXED;
            }

            int maxStackSize = Math.max(1, slot.maxStackSize());
            totalContribution += (float) Math.min(slot.count(), maxStackSize) / maxStackSize;
        }

        if (empty) {
            return new Result(null, 0, true);
        }

        return new Result(
                uniformType == MIXED ? null : uniformType,
                Math.min(1, totalContribution / Math.max(1, slotCount)),
                false);
    }
}
