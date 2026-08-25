package me.linstar.illusion.client.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {

    @Unique
    @NotNull
    private final ThreadLocal<IllusionData> illusion$currentData = new ThreadLocal<>();

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onGetState(RenderChunkRegion instance, BlockPos pos, Operation<BlockState> original, @Local(argsOnly = true) RenderChunkRegion region){
        this.illusion$currentData.remove();
        BlockState state = original.call(instance, pos);
        if (state.hasBlockEntity()) {
            BlockEntity blockentity = region.getBlockEntity(pos);
            if (blockentity != null && blockentity.getPersistentData().contains(IllusionData.NAME)) {
                this.illusion$currentData.set(new IllusionData(blockentity.getPersistentData().getCompound(IllusionData.NAME), Minecraft.getInstance().level.registryAccess()));
                return illusion$currentData.get().getModelData().getState();
            }
        }
        return state;
    }

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
    private RenderShape onGetRednerShape(BlockState instance, Operation<RenderShape> original, @Local(name = "blockpos2") BlockPos pos){
        if (this.illusion$currentData.get() != null){
            return RenderShape.MODEL;
        }

        return original.call(instance);
    }

    @ModifyVariable(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At("STORE"))
    private BakedModel onGetModel(BakedModel model){
        if (this.illusion$currentData.get() != null){
            return illusion$currentData.get().getModelData().getModel();
        }

        return model;
    }

    @WrapOperation(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"))
    private void onRenderBatched(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack stack, VertexConsumer consumer, boolean culling, RandomSource source, ModelData modelData, RenderType renderType, Operation<Void> original){
        if (this.illusion$currentData.get() != null){
            Vec3 offset = this.illusion$currentData.get().getOffset();
            stack.translate(offset.x, offset.y, offset.z);
            original.call(instance, state, pos, world, stack, consumer, false, source, modelData, renderType);
            return;
        }

        original.call(instance, state, pos, world, stack, consumer, culling, source, modelData, renderType);
    }
}
