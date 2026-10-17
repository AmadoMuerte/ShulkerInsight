package dev.shulkerinsight.mixin

import dev.shulkerinsight.ShulkerContents
import dev.shulkerinsight.ShulkerInsightConfig
import dev.shulkerinsight.ShulkerRenderHelpers
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.ShulkerBoxBlock
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(GuiGraphicsExtractor::class)
class GuiItemMixin {
    @Inject(
        method = ["item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V"],
        at = [At("TAIL")],
    )
    private fun renderIndicators(
        entity: LivingEntity,
        level: Level,
        stack: ItemStack,
        x: Int,
        y: Int,
        seed: Int,
        ci: CallbackInfo,
    ) {
        if (ShulkerRenderHelpers.RENDERING_BADGE.get() == true) return
        val item = stack.item
        if (item !is BlockItem || item.block !is ShulkerBoxBlock) return

        val graphics = this as Any as GuiGraphicsExtractor
        val analysis = ShulkerContents.of(stack)
        if (ShulkerInsightConfig.inventoryBadge && analysis.isUniform) {
            graphics.fill(x, y, x + 9, y + 9, 0x80000000.toInt())
            graphics.pose().pushMatrix()
            try {
                graphics.pose().translate(x.toFloat(), y.toFloat())
                graphics.pose().scale(0.5f, 0.5f)
                ShulkerRenderHelpers.RENDERING_BADGE.set(true)
                graphics.item(analysis.uniformItem, 0, 0)
            } finally {
                ShulkerRenderHelpers.RENDERING_BADGE.set(false)
                graphics.pose().popMatrix()
            }
        }
        if (ShulkerInsightConfig.fillIndicator && stack.has(DataComponents.CONTAINER)) {
            val ratio = analysis.fillRatio
            graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xFF000000.toInt())
            if (ratio > 0) {
                val width = maxOf(1, Math.round(13.0f * ratio))
                graphics.fill(x + 2, y + 13, x + 2 + width, y + 14, ShulkerRenderHelpers.fillColor(ratio))
            }
        }
    }
}
