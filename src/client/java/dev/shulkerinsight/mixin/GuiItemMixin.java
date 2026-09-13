package dev.shulkerinsight.mixin;

import dev.shulkerinsight.ShulkerContents;
import dev.shulkerinsight.ShulkerInsightConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiItemMixin {
    private static final ThreadLocal<Boolean> RENDERING_BADGE = ThreadLocal.withInitial(() -> false);

    @Unique
    private static int shulkerinsight$fillColor(float ratio) {
        if (ratio >= 0.9999f) return 0xFF22D3C5; // тускло-бирюзовый: ровно полный
        if (ratio >= 0.75f)   return 0xFF63C64A; // зелёный
        if (ratio >= 0.50f)   return 0xFFE0C93A; // жёлтый
        if (ratio >= 0.25f)   return 0xFFE0862E; // оранжевый
        return 0xFFE05050;                       // красный
    }

    @Inject(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At("TAIL"))
    private void shulkerinsight$renderIndicators(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        if (RENDERING_BADGE.get()
                || !(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        ShulkerContents.Analysis analysis = ShulkerContents.of(stack);
        if (ShulkerInsightConfig.inventoryBadge && analysis.isUniform()) {
            graphics.fill(x, y, x + 9, y + 9, 0x80000000);
            graphics.pose().pushMatrix();
            try {
                graphics.pose().translate(x, y);
                graphics.pose().scale(0.5f, 0.5f);
                RENDERING_BADGE.set(true);
                graphics.item(analysis.uniformItem(), 0, 0);
            } finally {
                RENDERING_BADGE.set(false);
                graphics.pose().popMatrix();
            }
        }
        if (ShulkerInsightConfig.fillIndicator && stack.has(DataComponents.CONTAINER)) {
            float ratio = analysis.fillRatio();
            graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000);
            if (ratio > 0) {
                int width = Math.max(1, Math.round(13.0f * ratio));
                graphics.fill(x + 2, y + 13, x + 2 + width, y + 14, shulkerinsight$fillColor(ratio));
            }
        }
    }

}
