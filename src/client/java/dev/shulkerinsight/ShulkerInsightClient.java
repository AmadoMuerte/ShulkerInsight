package dev.shulkerinsight;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ShulkerInsightClient implements ClientModInitializer {
    private static final List<ItemStack> SCREEN_CONTENTS = new ArrayList<>(ShulkerAnalyzer.SLOT_COUNT);
    private static BlockPos pendingShulkerPos;

    @Override
    public void onInitializeClient() {
        ShulkerInsightConfig.load();
        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof ShulkerPreviewTooltipData d ? new ShulkerPreviewTooltipComponent(d) : null);

        KeyMapping keybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shulkerinsight.config",
                GLFW.GLFW_KEY_LEFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkerinsight", "general"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybind.consumeClick()) {
                client.setScreenAndShow(new ShulkerInsightConfigScreen());
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ShulkerWorldCache.clear();
            ShulkerInsightConfig.load();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ShulkerWorldCache.clear());

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide()
                    && level.getBlockEntity(hitResult.getBlockPos()) instanceof ShulkerBoxBlockEntity) {
                pendingShulkerPos = hitResult.getBlockPos().immutable();
            }
            return InteractionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ShulkerBoxScreen shulkerScreen)) {
                pendingShulkerPos = null;
                return;
            }
            capture(shulkerScreen);
            ScreenEvents.afterTick(screen).register(ignored -> capture(shulkerScreen));
            ScreenEvents.remove(screen).register(ignored -> {
                capture(shulkerScreen);
                pendingShulkerPos = null;
            });
        });
    }

    private static void capture(ShulkerBoxScreen screen) {
        if (pendingShulkerPos == null) {
            return;
        }
        SCREEN_CONTENTS.clear();
        for (int i = 0; i < ShulkerAnalyzer.SLOT_COUNT && i < screen.getMenu().slots.size(); i++) {
            SCREEN_CONTENTS.add(screen.getMenu().slots.get(i).getItem());
        }
        ShulkerWorldCache.put(pendingShulkerPos, ShulkerContents.of(SCREEN_CONTENTS).uniformItem());
    }
}
