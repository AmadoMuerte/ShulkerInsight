package dev.shulkerinsight

import net.minecraft.SharedConstants
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentMap
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ShulkerWorldCacheTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            // Cache tests need item identities, not data-pack component defaults.
            Items.STONE.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY)
            Items.DIAMOND.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY)
        }

        private fun analysis(uniformItem: ItemStack): ShulkerContents.Analysis =
            ShulkerContents.Analysis(uniformItem, if (uniformItem.isEmpty) 0f else 1f)
    }

    @Test
    fun replacementAtSamePositionDoesNotInheritOldContents() {
        val pos = BlockPos(1, 64, 1)
        val oldShulker = ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState())
        val replacement = ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState())
        ShulkerWorldCache.clear()
        try {
            ShulkerWorldCache.put(oldShulker, analysis(ItemStack(Items.STONE)))
            oldShulker.setRemoved()
            assertNull(ShulkerWorldCache.get(oldShulker))
            assertNull(ShulkerWorldCache.get(replacement))

            // A stale section mesh can render the removed entity before its rebuild finishes.
            ShulkerWorldCache.seedIfUnknown(oldShulker, analysis(ItemStack(Items.STONE)))
            ShulkerWorldCache.seedIfUnknown(replacement, analysis(ItemStack(Items.DIAMOND)))
            assertEquals(Items.DIAMOND, ShulkerWorldCache.get(replacement)!!.uniformItem.item)

            // A still-live entity in another level must not share the entry either.
            val otherLevel = ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState())
            ShulkerWorldCache.seedIfUnknown(otherLevel, analysis(ItemStack.EMPTY))
            assertSame(ItemStack.EMPTY, ShulkerWorldCache.get(otherLevel)!!.uniformItem)
            ShulkerWorldCache.put(oldShulker, analysis(ItemStack(Items.STONE)))
            assertNull(ShulkerWorldCache.get(oldShulker))
            assertEquals(Items.DIAMOND, ShulkerWorldCache.get(replacement)!!.uniformItem.item)
        } finally {
            ShulkerWorldCache.clear()
        }
    }
}
