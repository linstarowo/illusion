package me.linstar.illusion.client.mixin;

import me.linstar.illusion.data.IllusionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Unique
    private static final VoxelShape EMPTY_SHAPE = Shapes.empty();

    @Shadow
    public abstract boolean hasBlockEntity();

    @Inject(method = "propagatesSkylightDown", at = @At("HEAD"), cancellable = true)
    private void modifyLightPass(BlockGetter getter, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasBlockEntity()){
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void modifyOcclusionShape(BlockGetter getter, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir){
        if (IllusionData.containsData(getter, pos)){
            cir.setReturnValue(EMPTY_SHAPE);
        }
    }

    @Inject(method = "getFaceOcclusionShape", at = @At("HEAD"), cancellable = true)
    private void modifyFaceOcclusionShape(BlockGetter getter, BlockPos pos, Direction p_60658_, CallbackInfoReturnable<VoxelShape> cir){
        if (IllusionData.containsData(getter, pos)){
            cir.setReturnValue(EMPTY_SHAPE);
        }
    }
}
