package dev.shulkerinsight

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity

class ShulkerInsightClient : ClientModInitializer {
    override fun onInitializeClient() {
        ShulkerInsightConfig.load()
        ClientTooltipComponentCallback.EVENT.register { data ->
            if (data is ShulkerPreviewTooltipData) ShulkerPreviewTooltipComponent(data) else null
        }

        val keybind = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.shulkerinsight.config",
                InputConstants.KEY_LEFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkerinsight", "general")),
            ),
        )
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (keybind.consumeClick()) {
                client.setScreenAndShow(ShulkerInsightConfigScreen())
            }
        }

        ClientPlayConnectionEvents.JOIN.register { handler, sender, client ->
            ShulkerWorldCache.clear()
            pendingShulker = null
            ShulkerInsightConfig.load()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
            ShulkerWorldCache.clear()
            pendingShulker = null
        }

        UseBlockCallback.EVENT.register { player, level, hand, hitResult ->
            if (level.isClientSide) {
                pendingShulker = level.getBlockEntity(hitResult.blockPos) as? ShulkerBoxBlockEntity
            }
            InteractionResult.PASS
        }

        ScreenEvents.AFTER_INIT.register { client, screen, width, height ->
            if (screen !is ShulkerBoxScreen) {
                pendingShulker = null
                return@register
            }
            val shulker = pendingShulker
            ScreenEvents.afterTick(screen).register { capture(screen, shulker) }
            ScreenEvents.remove(screen).register {
                capture(screen, shulker)
                if (pendingShulker === shulker) {
                    pendingShulker = null
                }
            }
        }
    }

    private companion object {
        private val SCREEN_CONTENTS = ArrayList<ItemStack>(ShulkerAnalyzer.SLOT_COUNT)
        private var pendingShulker: ShulkerBoxBlockEntity? = null

        private fun capture(
            screen: ShulkerBoxScreen,
            shulker: ShulkerBoxBlockEntity?,
        ) {
            val level = shulker?.level
            if (level == null || shulker.isRemoved
                || level !== Minecraft.getInstance().level
                || level.getBlockEntity(shulker.blockPos) !== shulker
            ) {
                return
            }
            SCREEN_CONTENTS.clear()
            var i = 0
            while (i < ShulkerAnalyzer.SLOT_COUNT && i < screen.menu.slots.size) {
                SCREEN_CONTENTS.add(screen.menu.slots[i].item)
                i++
            }
            ShulkerWorldCache.put(shulker, ShulkerContents.of(SCREEN_CONTENTS))
        }
    }
}
