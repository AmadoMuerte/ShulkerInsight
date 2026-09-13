package dev.shulkerinsight.mixin;

import dev.shulkerinsight.ShulkerInsightConfig;
import dev.shulkerinsight.ShulkerPreviewTooltipData;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemTooltipImageMixin {
    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void shulkerinsight$previewContents(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (!ShulkerInsightConfig.contentsPreview
                || !(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(items);
        }
        cir.setReturnValue(Optional.of(new ShulkerPreviewTooltipData(items)));
    }
}
