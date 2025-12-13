package me.linstar.illusion.client.mixin.compat;

import com.yuushya.modelling.forge.client.ShowBlockModel;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ShowBlockModel.class, remap = false)
public abstract class ShowBlockModelMixin extends com.yuushya.modelling.blockentity.showblock.ShowBlockModel {
    @Unique
    private static final ModelProperty<IllusionData> BASE_BLOCK_ENTITY = new ModelProperty<>();

    public ShowBlockModelMixin(Direction facing) {
        super(facing);
    }

    @Inject(method = "getModelData", at = @At("HEAD"), cancellable = true)
    private void getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData, CallbackInfoReturnable<ModelData> cir) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        var data = blockEntity.getPersistentData();
        if (!data.contains(IllusionData.NAME)) return ;

        cir.setReturnValue(ModelData.builder().with(BASE_BLOCK_ENTITY, new IllusionData(data.getCompound(IllusionData.NAME))).build());
    }

    @Inject(method = "getQuads", at = @At("HEAD"), cancellable = true)
    private void getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType, CallbackInfoReturnable<List<BakedQuad>> cir){
        IllusionData illusionData = data.get(BASE_BLOCK_ENTITY);
        if (illusionData != null) {
            cir.setReturnValue(super.getQuads(state, side, rand, ((IllusionData.YuushayaModelData) illusionData.getModelData()).transformData()));
        }
    }
}
