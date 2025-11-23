package me.linstar.illusion.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import me.linstar.illusion.client.IllusionClient;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Shadow
    public abstract boolean setBlock(BlockPos p_233643_, BlockState p_233644_, int p_233645_, int p_233646_);
    @ModifyArgs(method = "setServerVerifiedBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
    public void modifyState_(Args args){
        illusion$motifyState(args);
//        BlockPos pos = args.get(0);
//        BlockState state = args.get(1);
//        var blockEntity = ((ClientLevel) (Object) this).getBlockEntity(pos);
//        var motifiedState = IllusionClient.getMotifiedState(state);
//
//        if (blockEntity instanceof AbstractFurnaceBlockEntity && motifiedState != null) args.set(1, motifiedState);
    }

    @ModifyArgs(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
    public void modifyState(Args args){
        illusion$motifyState(args);
    }

    @Unique
    private void illusion$motifyState(Args args) {
        BlockPos pos = args.get(0);
        BlockState state = args.get(1);
        var blockEntity = ((ClientLevel) (Object) this).getBlockEntity(pos);
        if (blockEntity == null || !blockEntity.getPersistentData().contains(IllusionData.NAME)) return;

        var sourceState = ((ClientLevel) (Object) this).getBlockState(pos);
        if (!state.getBlock().equals(sourceState.getBlock())) return;

        var motifiedState = IllusionClient.getMotifiedState(state);
        args.set(1, motifiedState);
    }
//    @Redirect(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
//    public boolean onSetBlock(Level instance, BlockPos pos, BlockState state, int blockSnapshot, int old){
//        var blockEntity = instance.getBlockEntity(pos);
//        var motifiedState = IllusionClient.getMotifiedState(state);
//        if (blockEntity instanceof AbstractFurnaceBlockEntity && motifiedState != null) {
//            return instance.setBlock(pos, motifiedState, blockSnapshot, old);
//        }else return instance.setBlock(pos, state, blockSnapshot, old);
//    }
}
