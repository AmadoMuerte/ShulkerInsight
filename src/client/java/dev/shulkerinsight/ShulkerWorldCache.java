package dev.shulkerinsight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public final class ShulkerWorldCache {
    private static final Map<BlockPos, ItemStack> ENTRIES = new ConcurrentHashMap<>();

    private ShulkerWorldCache() {}

    public static ItemStack get(BlockPos pos) {
        return ENTRIES.get(pos);
    }

    public static void put(BlockPos pos, ItemStack uniformOrEmpty) {
        ENTRIES.put(pos.immutable(), copyOrEmpty(uniformOrEmpty));
    }

    public static void seedIfUnknown(BlockPos pos, ItemStack uniformOrEmpty) {
        ENTRIES.putIfAbsent(pos.immutable(), copyOrEmpty(uniformOrEmpty));
    }

    public static void remove(BlockPos pos) {
        ENTRIES.remove(pos);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    private static ItemStack copyOrEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }
}
