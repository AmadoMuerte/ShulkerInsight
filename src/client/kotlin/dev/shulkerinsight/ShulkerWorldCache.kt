package dev.shulkerinsight

import java.util.Collections
import java.util.WeakHashMap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity

object ShulkerWorldCache {
    // A replacement or another dimension can have the same position, but never the same entity.
    private val ENTRIES: MutableMap<ShulkerBoxBlockEntity, ItemStack> =
        Collections.synchronizedMap(WeakHashMap())

    @JvmStatic
    fun get(blockEntity: ShulkerBoxBlockEntity): ItemStack? =
        if (blockEntity.isRemoved) null else ENTRIES[blockEntity]

    @JvmStatic
    fun put(blockEntity: ShulkerBoxBlockEntity, uniformOrEmpty: ItemStack) {
        if (!blockEntity.isRemoved) {
            ENTRIES[blockEntity] = copyOrEmpty(uniformOrEmpty)
        }
    }

    @JvmStatic
    fun seedIfUnknown(blockEntity: ShulkerBoxBlockEntity, uniformOrEmpty: ItemStack) {
        if (!blockEntity.isRemoved) {
            ENTRIES.putIfAbsent(blockEntity, copyOrEmpty(uniformOrEmpty))
        }
    }

    @JvmStatic
    fun clear() {
        ENTRIES.clear()
    }

    private fun copyOrEmpty(stack: ItemStack): ItemStack =
        if (stack.isEmpty) ItemStack.EMPTY else stack.copy()
}
