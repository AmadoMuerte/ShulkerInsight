package dev.shulkerinsight

import java.util.function.Consumer
import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.options.OptionsSubScreen
import net.minecraft.network.chat.Component

class ShulkerInsightConfigScreen : OptionsSubScreen(
    // No parent screen; mappings mark the parameter non-null but the vanilla
    // constructor only stores it, matching the original Java's literal null.
    null as Screen,
    Minecraft.getInstance().options,
    Component.translatable("shulkerinsight.options.title"),
) {
    override fun addOptions() {
        val list = list ?: return
        list.addSmall(
            booleanOption("shulkerinsight.options.contents_preview", ShulkerInsightConfig.contentsPreview, Consumer { value: Boolean ->
                ShulkerInsightConfig.contentsPreview = value
            }).createButton(options, 0, 0, 150),
            booleanOption("shulkerinsight.options.fill_indicator", ShulkerInsightConfig.fillIndicator, Consumer { value: Boolean ->
                ShulkerInsightConfig.fillIndicator = value
            }).createButton(options, 0, 0, 150),
        )
        list.addSmall(
            booleanOption("shulkerinsight.options.inventory_badge", ShulkerInsightConfig.inventoryBadge, Consumer { value: Boolean ->
                ShulkerInsightConfig.inventoryBadge = value
            }).createButton(options, 0, 0, 150),
            booleanOption("shulkerinsight.options.world_icon", ShulkerInsightConfig.worldIcon, Consumer { value: Boolean ->
                ShulkerInsightConfig.worldIcon = value
            }).createButton(options, 0, 0, 150),
        )
        list.addSmall(
            booleanOption("shulkerinsight.options.world_icon_full_outline", ShulkerInsightConfig.worldIconFullOutline, Consumer { value: Boolean ->
                ShulkerInsightConfig.worldIconFullOutline = value
            }).createButton(options, 0, 0, 150),
            DistanceSlider(),
        )
        list.addBig(ScaleSlider())
    }

    private class DistanceSlider : AbstractSliderButton(
        0, 0, 150, 20, Component.empty(), (ShulkerInsightConfig.worldIconDistance - 4) / 60.0,
    ) {
        init {
            updateMessage()
        }

        override fun updateMessage() {
            message = Component.translatable("shulkerinsight.options.world_icon_distance", ShulkerInsightConfig.worldIconDistance)
        }

        override fun applyValue() {
            ShulkerInsightConfig.worldIconDistance = 4 + Math.round(value * 15.0).toInt() * 4
            value = (ShulkerInsightConfig.worldIconDistance - 4) / 60.0
            ShulkerInsightConfig.save()
        }
    }

    private class ScaleSlider : AbstractSliderButton(
        0, 0, 150, 20, Component.empty(), (ShulkerInsightConfig.worldIconScale * 100.0 - 25.0) / 75.0,
    ) {
        init {
            updateMessage()
        }

        override fun updateMessage() {
            message = Component.translatable(
                "shulkerinsight.options.world_icon_scale",
                Math.round(ShulkerInsightConfig.worldIconScale * 100.0f),
            )
        }

        override fun applyValue() {
            val percent = 25 + Math.round(value * 15.0).toInt() * 5
            ShulkerInsightConfig.worldIconScale = percent / 100.0f
            value = (percent - 25) / 75.0
            ShulkerInsightConfig.save()
        }
    }

    private companion object {
        private fun booleanOption(key: String, initial: Boolean, setter: Consumer<Boolean>): OptionInstance<Boolean> =
            OptionInstance.createBoolean(key, initial) { value: Boolean ->
                setter.accept(value)
                ShulkerInsightConfig.save()
            }
    }
}
