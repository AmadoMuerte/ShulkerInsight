package dev.shulkerinsight

import java.util.Collections
import java.util.WeakHashMap
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity

object ShulkerWorldCache {
    // A replacement or another dimension can have the same position, but never the same entity.
    private val ENTRIES: MutableMap<ShulkerBoxBlockEntity, ShulkerContents.Analysis> =
        Collections.synchronizedMap(WeakHashMap())

    @JvmStatic
    fun get(blockEntity: ShulkerBoxBlockEntity): ShulkerContents.Analysis? =
        if (blockEntity.isRemoved) null else ENTRIES[blockEntity]

    @JvmStatic
    fun put(blockEntity: ShulkerBoxBlockEntity, analysis: ShulkerContents.Analysis) {
        if (!blockEntity.isRemoved) {
            ENTRIES[blockEntity] = analysis
        }
    }

    @JvmStatic
    fun seedIfUnknown(blockEntity: ShulkerBoxBlockEntity, analysis: ShulkerContents.Analysis) {
        if (!blockEntity.isRemoved) {
            ENTRIES.putIfAbsent(blockEntity, analysis)
        }
    }

    @JvmStatic
    fun clear() {
        ENTRIES.clear()
    }
}
