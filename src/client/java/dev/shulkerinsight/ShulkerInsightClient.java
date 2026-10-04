package dev.shulkerinsight;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.item.ItemStack;

public final class ShulkerInsightClient implements ClientModInitializer {
    private static final List<ItemStack> SCREEN_CONTENTS = new ArrayList<>(ShulkerAnalyzer.SLOT_COUNT);
    private static ShulkerBoxBlockEntity pendingShulker;

    @Override
    public void onInitializeClient() {
        ShulkerInsightConfig.load();
        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof ShulkerPreviewTooltipData d ? new ShulkerPreviewTooltipComponent(d) : null);

        KeyMapping keybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shulkerinsight.config",
                InputConstants.KEY_LEFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkerinsight", "general"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybind.consumeClick()) {
                client.setScreenAndShow(new ShulkerInsightConfigScreen());
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ShulkerWorldCache.clear();
            pendingShulker = null;
            ShulkerInsightConfig.load();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ShulkerWorldCache.clear();
            pendingShulker = null;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide()) {
                pendingShulker = level.getBlockEntity(hitResult.getBlockPos()) instanceof ShulkerBoxBlockEntity shulker
                        ? shulker : null;
            }
            return InteractionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ShulkerBoxScreen shulkerScreen)) {
                pendingShulker = null;
                return;
            }
            ShulkerBoxBlockEntity shulker = pendingShulker;
            ScreenEvents.afterTick(screen).register(ignored -> capture(shulkerScreen, shulker));
            ScreenEvents.remove(screen).register(ignored -> {
                capture(shulkerScreen, shulker);
                if (pendingShulker == shulker) {
                    pendingShulker = null;
                }
            });
        });
    }

    private static void capture(ShulkerBoxScreen screen, ShulkerBoxBlockEntity shulker) {
        if (shulker == null || shulker.isRemoved() || shulker.getLevel() == null
                || shulker.getLevel() != Minecraft.getInstance().level
                || shulker.getLevel().getBlockEntity(shulker.getBlockPos()) != shulker) {
            return;
        }
        SCREEN_CONTENTS.clear();
        for (int i = 0; i < ShulkerAnalyzer.SLOT_COUNT && i < screen.getMenu().slots.size(); i++) {
            SCREEN_CONTENTS.add(screen.getMenu().slots.get(i).getItem());
        }
        ShulkerWorldCache.put(shulker, ShulkerContents.of(SCREEN_CONTENTS).uniformItem());
    }
}
