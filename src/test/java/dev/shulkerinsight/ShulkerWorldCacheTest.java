package dev.shulkerinsight;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShulkerWorldCacheTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Cache tests need item identities, not data-pack component defaults.
        Items.STONE.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        Items.DIAMOND.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
    }

    @Test
    void replacementAtSamePositionDoesNotInheritOldContents() {
        BlockPos pos = new BlockPos(1, 64, 1);
        ShulkerBoxBlockEntity oldShulker = new ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState());
        ShulkerBoxBlockEntity replacement = new ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState());
        ShulkerWorldCache.clear();
        try {
            ShulkerWorldCache.put(oldShulker, new ItemStack(Items.STONE));
            oldShulker.setRemoved();
            assertNull(ShulkerWorldCache.get(oldShulker));
            assertNull(ShulkerWorldCache.get(replacement));

            // A stale section mesh can render the removed entity before its rebuild finishes.
            ShulkerWorldCache.seedIfUnknown(oldShulker, new ItemStack(Items.STONE));
            ShulkerWorldCache.seedIfUnknown(replacement, new ItemStack(Items.DIAMOND));
            assertEquals(Items.DIAMOND, ShulkerWorldCache.get(replacement).getItem());

            // A still-live entity in another level must not share the entry either.
            ShulkerBoxBlockEntity otherLevel = new ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState());
            ShulkerWorldCache.seedIfUnknown(otherLevel, ItemStack.EMPTY);
            assertEquals(ItemStack.EMPTY, ShulkerWorldCache.get(otherLevel));
            ShulkerWorldCache.put(oldShulker, new ItemStack(Items.STONE));
            assertNull(ShulkerWorldCache.get(oldShulker));
            assertEquals(Items.DIAMOND, ShulkerWorldCache.get(replacement).getItem());
        } finally {
            ShulkerWorldCache.clear();
        }
    }
}
