package dev.shulkerinsight;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ShulkerPreviewTooltipData(NonNullList<ItemStack> items) implements TooltipComponent {}
