package dev.shulkerinsight;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ShulkerPreviewTooltipComponent implements ClientTooltipComponent {
    private static final int COLS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int WIDTH = 176;
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");

    private final ShulkerPreviewTooltipData data;

    public ShulkerPreviewTooltipComponent(ShulkerPreviewTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) {
        return 68;
    }

    @Override
    public int getWidth(Font font) {
        return WIDTH;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, WIDTH, 7, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 7, 0, 17, WIDTH, 54, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 61, 0, 160, WIDTH, 7, 256, 256);
        for (int i = 0; i < data.items().size(); i++) {
            ItemStack stack = data.items().get(i);
            if (!stack.isEmpty()) {
                int itemX = x + 8 + i % COLS * SLOT_SIZE;
                int itemY = y + 8 + i / COLS * SLOT_SIZE;
                graphics.item(stack, itemX, itemY);
                graphics.itemDecorations(font, stack, itemX, itemY);
            }
        }
    }
}
