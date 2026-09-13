package dev.shulkerinsight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShulkerInsightConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("shulkerinsight");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("shulkerinsight.json");

    public static boolean contentsPreview = true;
    public static boolean fillIndicator = true;
    public static boolean inventoryBadge = true;
    public static boolean worldIcon = true;
    public static int worldIconDistance = 32;
    public static float worldIconScale = 0.5f;

    private ShulkerInsightConfig() {}

    public static void load() {
        reset();
        if (!Files.exists(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            ConfigData config = GSON.fromJson(reader, ConfigData.class);
            if (config != null) {
                contentsPreview = config.contentsPreview;
                fillIndicator = config.fillIndicator;
                inventoryBadge = config.inventoryBadge;
                worldIcon = config.worldIcon;
                worldIconDistance = clamp(config.worldIconDistance, 4, 64);
                worldIconScale = Math.clamp(config.worldIconScale, 0.25f, 1.0f);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not load Shulker Insight config", exception);
            reset();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(new ConfigData(), writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not save Shulker Insight config", exception);
        }
    }

    public static void reset() {
        contentsPreview = true;
        fillIndicator = true;
        inventoryBadge = true;
        worldIcon = true;
        worldIconDistance = 32;
        worldIconScale = 0.5f;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class ConfigData {
        boolean contentsPreview = ShulkerInsightConfig.contentsPreview;
        boolean fillIndicator = ShulkerInsightConfig.fillIndicator;
        boolean inventoryBadge = ShulkerInsightConfig.inventoryBadge;
        boolean worldIcon = ShulkerInsightConfig.worldIcon;
        int worldIconDistance = ShulkerInsightConfig.worldIconDistance;
        float worldIconScale = ShulkerInsightConfig.worldIconScale;
    }
}
