package dev.shulkerinsight;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public final class ShulkerWorldCache {
    // A replacement or another dimension can have the same position, but never the same entity.
    private static final Map<ShulkerBoxBlockEntity, ItemStack> ENTRIES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private ShulkerWorldCache() {}

    public static ItemStack get(ShulkerBoxBlockEntity blockEntity) {
        return blockEntity.isRemoved() ? null : ENTRIES.get(blockEntity);
    }

    public static void put(ShulkerBoxBlockEntity blockEntity, ItemStack uniformOrEmpty) {
        if (!blockEntity.isRemoved()) {
            ENTRIES.put(blockEntity, copyOrEmpty(uniformOrEmpty));
        }
    }

    public static void seedIfUnknown(ShulkerBoxBlockEntity blockEntity, ItemStack uniformOrEmpty) {
        if (!blockEntity.isRemoved()) {
            ENTRIES.putIfAbsent(blockEntity, copyOrEmpty(uniformOrEmpty));
        }
    }

    public static void clear() {
        ENTRIES.clear();
    }

    private static ItemStack copyOrEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }
}
