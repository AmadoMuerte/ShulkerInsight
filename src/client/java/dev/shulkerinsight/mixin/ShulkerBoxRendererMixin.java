package dev.shulkerinsight.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.shulkerinsight.ShulkerContents;
import dev.shulkerinsight.ShulkerInsightConfig;
import dev.shulkerinsight.ShulkerWorldCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxRenderer.class)
public class ShulkerBoxRendererMixin {
    @Unique
    private ItemModelResolver shulkerinsight$itemModelResolver;

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;)V", at = @At("TAIL"))
    private void shulkerinsight$captureItemModelResolver(BlockEntityRendererProvider.Context context, CallbackInfo callbackInfo) {
        shulkerinsight$itemModelResolver = context.itemModelResolver();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void shulkerinsight$seedWorldCache(
            ShulkerBoxBlockEntity blockEntity,
            ShulkerBoxRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfo callbackInfo) {
        if (!ShulkerInsightConfig.worldIcon || ShulkerWorldCache.get(renderState.blockPos) != null) {
            return;
        }
        double distance = ShulkerInsightConfig.worldIconDistance;
        if (cameraPos.distanceToSqr(
                        renderState.blockPos.getX() + 0.5,
                        renderState.blockPos.getY() + 0.5,
                        renderState.blockPos.getZ() + 0.5)
                > distance * distance) {
            return;
        }
        ShulkerWorldCache.seedIfUnknown(renderState.blockPos, ShulkerContents.of(blockEntity).uniformItem());
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void shulkerinsight$submitWorldIcon(
            ShulkerBoxRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo callbackInfo) {
        if (!ShulkerInsightConfig.worldIcon || shulkerinsight$itemModelResolver == null) {
            return;
        }

        ItemStack icon = ShulkerWorldCache.get(renderState.blockPos);
        if (icon == null || icon.isEmpty()) {
            return;
        }

        double distance = ShulkerInsightConfig.worldIconDistance;
        if (cameraRenderState.pos.distanceToSqr(
                        renderState.blockPos.getX() + 0.5,
                        renderState.blockPos.getY() + 0.5,
                        renderState.blockPos.getZ() + 0.5)
                > distance * distance) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        ItemStackRenderState state = new ItemStackRenderState();
        shulkerinsight$itemModelResolver.updateForTopItem(state, icon, ItemDisplayContext.FIXED, level, null, 0);
        if (state.isEmpty()) {
            return;
        }

        Direction direction = renderState.direction != null ? renderState.direction : Direction.UP;
        poseStack.pushPose();
        shulkerinsight$applyFaceTransform(
                poseStack, direction, renderState.progress, ShulkerInsightConfig.worldIconScale);
        state.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Unique
    private static void shulkerinsight$applyFaceTransform(PoseStack poseStack, Direction dir, float progress, float scale) {
        double o = 0.01;
        double lid = progress * 0.5;
        switch (dir) {
            case UP -> {
                poseStack.translate(0.5, 1.0 + lid + o, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case DOWN -> {
                poseStack.translate(0.5, -lid - o, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case NORTH -> {
                poseStack.translate(0.5, 0.5, -lid - o);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> poseStack.translate(0.5, 0.5, 1.0 + lid + o);
            case EAST -> {
                poseStack.translate(1.0 + lid + o, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case WEST -> {
                poseStack.translate(-lid - o, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.scale(scale, scale, scale);
    }
}
