package dev.shulkerinsight.mixin;

import dev.shulkerinsight.ShulkerWorldCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class ClientLevelMixin {
    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void shulkerinsight$removeWorldCacheEntry(BlockPos pos, CallbackInfo callbackInfo) {
        Level level = (Level) (Object) this;
        if (level.isClientSide() && level.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity) {
            ShulkerWorldCache.remove(pos);
        }
    }
}
