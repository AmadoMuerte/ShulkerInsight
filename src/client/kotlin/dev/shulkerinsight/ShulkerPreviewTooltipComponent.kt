package dev.shulkerinsight

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

class ShulkerPreviewTooltipComponent(private val data: ShulkerPreviewTooltipData) : ClientTooltipComponent {
    override fun getHeight(font: Font): Int = 68

    override fun getWidth(font: Font): Int = WIDTH

    override fun extractImage(
        font: Font,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        graphics: GuiGraphicsExtractor,
    ) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, WIDTH, 7, 256, 256)
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 7, 0f, 17f, WIDTH, 54, 256, 256)
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 61, 0f, 160f, WIDTH, 7, 256, 256)
        for (i in data.items.indices) {
            val stack: ItemStack = data.items[i]
            if (!stack.isEmpty) {
                val itemX = x + 8 + i % COLS * SLOT_SIZE
                val itemY = y + 8 + i / COLS * SLOT_SIZE
                graphics.item(stack, itemX, itemY)
                graphics.itemDecorations(font, stack, itemX, itemY)
            }
        }
    }

    private companion object {
        private const val COLS = 9
        private const val SLOT_SIZE = 18
        private const val WIDTH = 176
        private val TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png")
    }
}
