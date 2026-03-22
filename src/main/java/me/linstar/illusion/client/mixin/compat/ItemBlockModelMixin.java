package me.linstar.illusion.client.mixin.compat;

import com.yuushya.modelling.blockentity.itemblock.ItemBlockModel;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.client.NeoItemBlockModel;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = NeoItemBlockModel.class, remap = false)
public abstract class ItemBlockModelMixin extends ItemBlockModel {
    @Shadow
    public abstract List<BakedQuad> getQuads(@Nullable Direction side, @NotNull RandomSource rand, List<TransformItemData> transformDatas, @Nullable BlockPos pos);

    @Unique
    private static final ModelProperty<BlockPos> ENTITY_POS = new ModelProperty<>();
    @Unique
    private static final ModelProperty<IllusionData> ILLUSION_DATA_PROPERTY = new ModelProperty<>();

    public ItemBlockModelMixin(Direction facing) {
        super(facing);
    }

    @Inject(method = "getModelData", at = @At("HEAD"), cancellable = true)
    private void getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData, CallbackInfoReturnable<ModelData> cir) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        var data = blockEntity.getPersistentData();
        if (!data.contains(IllusionData.NAME)) return ;

        cir.setReturnValue(ModelData.builder().with(ENTITY_POS, blockEntity.getBlockPos()).with(ILLUSION_DATA_PROPERTY, new IllusionData(data.getCompound(IllusionData.NAME))).build());
    }

    @Inject(method = "getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType, CallbackInfoReturnable<List<BakedQuad>> cir){
        IllusionData illusionData = data.get(ILLUSION_DATA_PROPERTY);
        BlockPos pos = data.get(ENTITY_POS);
        if (pos != null && illusionData != null && illusionData.getType().equals(IllusionData.DataType.CUSTOM_ITEM)) {
            cir.setReturnValue(this.getQuads(side, rand, ((IllusionData.YuushayaItemModelData) illusionData.getModelData()).transformData(), null));
        }
    }
}
