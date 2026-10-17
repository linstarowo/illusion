package me.linstar.illusion.client.mixin.compat.sable;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public class SubLevelAssemblyHelperMixin {
    @ModifyVariable(method = "moveBlocks", at = @At("STORE"), name = "newBlockEntity")
    private static BlockEntity onNewBlockEntity(BlockEntity newBlockEntity, @Local(name="blockEntity") BlockEntity blockEntity, @Local(name="resultingLevel") ServerLevel resultingLevel, @Local(name="level") ServerLevel level){
        if (blockEntity == null) {
            return newBlockEntity;
        }
        var oldPos = blockEntity.getBlockPos();
        var oldChunkData = level.getChunk(oldPos).getData(Illusion.CHUNK_DATA);
        var oldData = oldChunkData.getData(oldPos);
        var newPos = newBlockEntity.getBlockPos();
        if (oldData != null){
            oldChunkData.deleteData(oldPos);
            resultingLevel.getChunk(newPos).getData(Illusion.CHUNK_DATA).updateData(newPos, oldData);
            var server = resultingLevel.getServer();
            double radius = level.getServer().getPlayerList().getViewDistance() * 16;

            server.tell(new TickTask(server.getTickCount() + 1, () -> Illusion.executeNextTick(() -> PacketDistributor.sendToPlayersNear(
                    resultingLevel,
                    null,
                    newPos.getX(), newPos.getY(), newPos.getZ(),
                    radius,
                    new IllusionDataS2CPacket(newPos, oldData)
            ))));
        }

        return newBlockEntity;
    }
}
