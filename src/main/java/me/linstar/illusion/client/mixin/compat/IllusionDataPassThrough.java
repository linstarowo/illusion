package me.linstar.illusion.client.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(BlockEntity.class)
public abstract class IllusionDataPassThrough {
    @Shadow
    public abstract CompoundTag getPersistentData();

    @WrapOperation(method = "loadAdditional", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;customPersistentData:Lnet/minecraft/nbt/CompoundTag;", opcode = Opcodes.PUTFIELD))
    private void passIllusionData(BlockEntity instance, CompoundTag value, Operation<Void> original){
        var data = this.getPersistentData();
        if (data.contains(IllusionData.NAME)){
            value.put(IllusionData.NAME, Objects.requireNonNull(data.get(IllusionData.NAME)));
        }
        original.call(instance, value);
    }

}
