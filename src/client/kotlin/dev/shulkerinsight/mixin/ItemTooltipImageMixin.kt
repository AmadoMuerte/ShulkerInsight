package dev.shulkerinsight.mixin

import dev.shulkerinsight.ShulkerInsightConfig
import dev.shulkerinsight.ShulkerPreviewTooltipData
import java.util.Optional
import net.minecraft.core.NonNullList
import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.block.ShulkerBoxBlock
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Item::class)
class ItemTooltipImageMixin {
    @Inject(method = ["getTooltipImage"], at = [At("HEAD")], cancellable = true)
    private fun previewContents(stack: ItemStack, cir: CallbackInfoReturnable<Optional<TooltipComponent>>) {
        if (!ShulkerInsightConfig.contentsPreview) return
        val item = stack.item
        if (item !is BlockItem || item.block !is ShulkerBoxBlock) return
        val items: NonNullList<ItemStack> = NonNullList.withSize(27, ItemStack.EMPTY)
        val contents: ItemContainerContents? = stack.get(DataComponents.CONTAINER)
        contents?.copyInto(items)
        cir.setReturnValue(Optional.of<TooltipComponent>(ShulkerPreviewTooltipData(items)))
    }
}
