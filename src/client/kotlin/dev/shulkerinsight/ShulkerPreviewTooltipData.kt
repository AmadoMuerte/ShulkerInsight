package dev.shulkerinsight

import net.minecraft.core.NonNullList
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack

data class ShulkerPreviewTooltipData(val items: NonNullList<ItemStack>) : TooltipComponent
