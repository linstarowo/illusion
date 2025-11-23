package me.linstar.illusion.client.wrapper;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Wrap the model to force rendering using the Forge Render API.
public class WrappedBakedModel implements BakedModel {
    final BakedModel model;

    public WrappedBakedModel(@NotNull final BakedModel model) {
        this.model = model;
    }

    public BakedModel getSource(){
        return this.model;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, @NotNull RandomSource randomSource) {
        return model.getQuads(blockState, direction, randomSource);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return model.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return model.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return model.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return model.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return model.getOverrides();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return model.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public boolean useAmbientOcclusion(@NotNull BlockState state) {
        return model.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(@NotNull BlockState state, @NotNull RenderType renderType) {
        return model.useAmbientOcclusion(state, renderType);
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        return model.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return model.getModelData(level, pos, state, modelData);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return model.getParticleIcon(data);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return model.getRenderTypes(state, rand, data);
    }

    @Override
    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
        return model.getRenderTypes(itemStack, fabulous);
    }

    @Override
    public @NotNull List<BakedModel> getRenderPasses(@NotNull ItemStack itemStack, boolean fabulous) {
        return model.getRenderPasses(itemStack, fabulous);
    }
}
