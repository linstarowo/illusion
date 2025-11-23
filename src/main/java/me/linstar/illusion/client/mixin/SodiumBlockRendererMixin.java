package me.linstar.illusion.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yuushya.modelling.forge.client.ShowBlockModel;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.linstar.illusion.client.wrapper.WrappedBakedModel;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class SodiumBlockRendererMixin {
    @Shadow public abstract void renderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers);

    @Shadow
    @Final
    private BlockOcclusionCache occlusionCache;

    @Inject(method = "renderModel", at = @At("HEAD"))
    public void onRenderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers, CallbackInfo info){
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockEntity blockEntity = level.getBlockEntity(ctx.pos());
        if (blockEntity == null) return;
        var persistentData = blockEntity.getPersistentData();
        if (!persistentData.contains(IllusionData.NAME)) return;

        IllusionData data = new IllusionData(persistentData.getCompound(IllusionData.NAME));

        var modelData = data.getModelData();
        BlockState state = modelData.getState();
        BakedModel model = modelData.getModel();
        BlockPos origin = new BlockPos((int) ctx.origin().x(), (int) ctx.origin().y(), (int) ctx.origin().z());

        ModelData forgeModelData = model.getModelData(ctx.localSlice(), ctx.pos(), state, ModelData.EMPTY);
        var warpedModel = new WrappedBakedModel(model);

        ctx.update(ctx.pos(), origin, state, warpedModel, ctx.seed(), forgeModelData, ctx.renderLayer());
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasOffsetFunction()Z"), remap = true)
    public boolean hasOffsetFunction(BlockState instance, Operation<Boolean> original){
        return true;
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"), remap = true)
    public Vec3 getOffset(BlockState instance, BlockGetter blockGetter, BlockPos pos, Operation<Vec3> original){
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return instance.getOffset(blockGetter, pos);

        CompoundTag tag = blockEntity.getPersistentData();
        if (tag.contains(IllusionData.NAME)){
            IllusionData data = new IllusionData(tag.getCompound(IllusionData.NAME));
            return data.getOffset();
        }

        return instance.getOffset(blockGetter, pos);
    }

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;isFaceVisible(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lnet/minecraft/core/Direction;)Z"))
    public boolean isFaceVisible(BlockRenderer instance, BlockRenderContext ctx, Direction face){
        if(ctx.model() instanceof WrappedBakedModel) return true;
        boolean result = this.occlusionCache.shouldDrawSide(ctx.state(), ctx.localSlice(), ctx.pos(), face);

        BlockPos pos = ctx.pos().offset(new BlockPos(face.getNormal()));
        Level level = Minecraft.getInstance().level;
        if (level == null) return result;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return result;

        if (blockEntity.getPersistentData().contains(IllusionData.NAME)) return true;

        return result;
    }
}
