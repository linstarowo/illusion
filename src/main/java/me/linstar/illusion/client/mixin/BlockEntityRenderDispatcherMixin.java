package me.linstar.illusion.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void cancelRender(BlockEntityRenderer<T> renderer, T entity, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo info){
        if (entity.getPersistentData().contains(IllusionData.NAME)) info.cancel();
    }
}
