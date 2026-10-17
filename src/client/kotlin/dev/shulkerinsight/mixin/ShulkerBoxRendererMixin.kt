package dev.shulkerinsight.mixin

import com.mojang.blaze3d.vertex.PoseStack
import dev.shulkerinsight.ShulkerContents
import dev.shulkerinsight.ShulkerInsightConfig
import dev.shulkerinsight.ShulkerRenderHelpers
import dev.shulkerinsight.ShulkerWorldCache
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
import net.minecraft.world.phys.Vec3
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ShulkerBoxRenderer::class)
class ShulkerBoxRendererMixin {
    @Unique
    private var itemModelResolver: ItemModelResolver? = null

    @Inject(
        method = ["<init>(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider\$Context;)V"],
        at = [At("TAIL")],
    )
    private fun captureItemModelResolver(context: BlockEntityRendererProvider.Context, callbackInfo: CallbackInfo) {
        itemModelResolver = context.itemModelResolver()
    }

    @Inject(method = ["extractRenderState"], at = [At("TAIL")])
    private fun seedWorldCache(
        blockEntity: ShulkerBoxBlockEntity,
        renderState: ShulkerBoxRenderState,
        partialTick: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay,
        callbackInfo: CallbackInfo,
    ) {
        val level: Level? = Minecraft.getInstance().level
        if (!ShulkerInsightConfig.worldIcon || blockEntity.isRemoved || level == null) return
        if (blockEntity.level !== level) return
        if (level.getBlockEntity(blockEntity.blockPos) !== blockEntity) return
        if (ShulkerWorldCache.get(blockEntity) != null) return
        val distance = ShulkerInsightConfig.worldIconDistance
        if (cameraPos.distanceToSqr(
                renderState.blockPos.x + 0.5,
                renderState.blockPos.y + 0.5,
                renderState.blockPos.z + 0.5,
            ) > distance * distance
        ) {
            return
        }
        ShulkerWorldCache.seedIfUnknown(blockEntity, ShulkerContents.of(blockEntity))
    }

    @Inject(
        method = ["submit(Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"],
        at = [At("TAIL")],
    )
    private fun submitWorldIcon(
        renderState: ShulkerBoxRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState,
        callbackInfo: CallbackInfo,
    ) {
        if (!ShulkerInsightConfig.worldIcon) return
        val resolver = itemModelResolver ?: return

        val level: Level = Minecraft.getInstance().level ?: return
        val blockEntity = level.getBlockEntity(renderState.blockPos) as? ShulkerBoxBlockEntity ?: return
        val analysis: ShulkerContents.Analysis = ShulkerWorldCache.get(blockEntity) ?: return
        val icon = analysis.uniformItem
        if (icon.isEmpty) return

        val distance = ShulkerInsightConfig.worldIconDistance
        if (cameraRenderState.pos.distanceToSqr(
                renderState.blockPos.x + 0.5,
                renderState.blockPos.y + 0.5,
                renderState.blockPos.z + 0.5,
            ) > distance * distance
        ) {
            return
        }

        val state = ItemStackRenderState()
        resolver.updateForTopItem(state, icon, ItemDisplayContext.FIXED, level, null, 0)
        if (state.isEmpty) return

        val direction: Direction = renderState.direction ?: Direction.UP
        poseStack.pushPose()
        ShulkerRenderHelpers.applyFaceTransform(poseStack, direction, renderState.progress, ShulkerInsightConfig.worldIconScale)
        state.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0)
        // Reuse the cached detection result and the inventory stage colors: the disc's
        // sweep equals the fill ratio, its color comes from the same fillColor() as the
        // inventory indicator. Cached so servers that only sync contents through the
        // container menu still get the world icon highlight.
        if (ShulkerInsightConfig.worldIconFullOutline && analysis.fillRatio > 0f) {
            ShulkerRenderHelpers.submitFullHighlight(submitNodeCollector, poseStack, analysis.fillRatio)
        }
        poseStack.popPose()
    }
}
