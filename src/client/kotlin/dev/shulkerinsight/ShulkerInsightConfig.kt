package dev.shulkerinsight

import com.google.gson.GsonBuilder
import java.io.IOException
import java.lang.Math
import java.nio.file.Files
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object ShulkerInsightConfig {
    private val LOGGER = LoggerFactory.getLogger("shulkerinsight")
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val PATH = FabricLoader.getInstance().getConfigDir().resolve("shulkerinsight.json")

    @JvmField
    var contentsPreview = true

    @JvmField
    var fillIndicator = true

    @JvmField
    var inventoryBadge = true

    @JvmField
    var worldIcon = true

    @JvmField
    var worldIconDistance = 32

    @JvmField
    var worldIconScale = 0.5f

    @JvmStatic
    fun load() {
        reset()
        if (!Files.exists(PATH)) {
            return
        }
        try {
            Files.newBufferedReader(PATH).use { reader ->
                val config = GSON.fromJson(reader, ConfigData::class.java)
                if (config != null) {
                    contentsPreview = config.contentsPreview
                    fillIndicator = config.fillIndicator
                    inventoryBadge = config.inventoryBadge
                    worldIcon = config.worldIcon
                    worldIconDistance = config.worldIconDistance.coerceIn(4, 64)
                    worldIconScale = Math.clamp(config.worldIconScale, 0.25f, 1.0f)
                }
            }
        } catch (exception: IOException) {
            LOGGER.warn("Could not load Shulker Insight config", exception)
            reset()
        } catch (exception: RuntimeException) {
            LOGGER.warn("Could not load Shulker Insight config", exception)
            reset()
        }
    }

    @JvmStatic
    fun save() {
        try {
            Files.createDirectories(PATH.parent)
            Files.newBufferedWriter(PATH).use { writer -> GSON.toJson(ConfigData(), writer) }
        } catch (exception: IOException) {
            LOGGER.warn("Could not save Shulker Insight config", exception)
        }
    }

    @JvmStatic
    fun reset() {
        contentsPreview = true
        fillIndicator = true
        inventoryBadge = true
        worldIcon = true
        worldIconDistance = 32
        worldIconScale = 0.5f
    }

    private class ConfigData {
        var contentsPreview = ShulkerInsightConfig.contentsPreview
        var fillIndicator = ShulkerInsightConfig.fillIndicator
        var inventoryBadge = ShulkerInsightConfig.inventoryBadge
        var worldIcon = ShulkerInsightConfig.worldIcon
        var worldIconDistance = ShulkerInsightConfig.worldIconDistance
        var worldIconScale = ShulkerInsightConfig.worldIconScale
    }
}
