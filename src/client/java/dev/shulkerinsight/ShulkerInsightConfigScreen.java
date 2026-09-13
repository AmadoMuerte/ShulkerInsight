package dev.shulkerinsight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ShulkerInsightConfigScreen extends OptionsSubScreen {
    public ShulkerInsightConfigScreen() {
        super(null, Minecraft.getInstance().options,
                Component.translatable("shulkerinsight.options.title"));
    }

    @Override
    protected void addOptions() {
        list.addSmall(
                booleanOption("shulkerinsight.options.contents_preview", ShulkerInsightConfig.contentsPreview,
                        value -> ShulkerInsightConfig.contentsPreview = value).createButton(options, 0, 0, 150),
                booleanOption("shulkerinsight.options.fill_indicator", ShulkerInsightConfig.fillIndicator,
                        value -> ShulkerInsightConfig.fillIndicator = value).createButton(options, 0, 0, 150));
        list.addSmall(
                booleanOption("shulkerinsight.options.inventory_badge", ShulkerInsightConfig.inventoryBadge,
                        value -> ShulkerInsightConfig.inventoryBadge = value).createButton(options, 0, 0, 150),
                booleanOption("shulkerinsight.options.world_icon", ShulkerInsightConfig.worldIcon,
                        value -> ShulkerInsightConfig.worldIcon = value).createButton(options, 0, 0, 150));
        list.addSmall(new DistanceSlider(), new ScaleSlider());
    }

    private static OptionInstance<Boolean> booleanOption(String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(key, initial, value -> {
            setter.accept(value);
            ShulkerInsightConfig.save();
        });
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        private DistanceSlider() {
            super(0, 0, 150, 20, Component.empty(), (ShulkerInsightConfig.worldIconDistance - 4) / 60.0D);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("shulkerinsight.options.world_icon_distance", ShulkerInsightConfig.worldIconDistance));
        }

        @Override
        protected void applyValue() {
            ShulkerInsightConfig.worldIconDistance = 4 + (int) Math.round(value * 15.0D) * 4;
            value = (ShulkerInsightConfig.worldIconDistance - 4) / 60.0D;
            ShulkerInsightConfig.save();
        }
    }

    private static final class ScaleSlider extends AbstractSliderButton {
        private ScaleSlider() {
            super(0, 0, 150, 20, Component.empty(), (ShulkerInsightConfig.worldIconScale * 100.0D - 25.0D) / 75.0D);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("shulkerinsight.options.world_icon_scale",
                    Math.round(ShulkerInsightConfig.worldIconScale * 100.0F)));
        }

        @Override
        protected void applyValue() {
            int percent = 25 + (int) Math.round(value * 15.0D) * 5;
            ShulkerInsightConfig.worldIconScale = percent / 100.0F;
            value = (percent - 25) / 75.0D;
            ShulkerInsightConfig.save();
        }
    }
}
