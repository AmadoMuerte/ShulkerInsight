package dev.shulkerinsight.mixin;

import dev.shulkerinsight.ShulkerInsightConfig;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ContainerTooltipSuppressMixin {
    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private <T extends net.minecraft.world.item.component.TooltipProvider> void shulkerinsight$suppressContainerTooltip(
            DataComponentType<T> type,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> consumer,
            TooltipFlag flag,
            CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ShulkerInsightConfig.contentsPreview
                && type == DataComponents.CONTAINER
                && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            ci.cancel();
        }
    }
}
