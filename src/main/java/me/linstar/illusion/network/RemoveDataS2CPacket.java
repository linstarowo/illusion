package me.linstar.illusion.network;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveDataS2CPacket(BlockPos pos) {
    public RemoveDataS2CPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos());
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null || !level.isClientSide) return;

            var blockEntity = level.getBlockEntity(this.pos);
            if (blockEntity == null) return;

            var tag = blockEntity.getPersistentData();
            tag.remove(IllusionData.NAME);

            ChunkPos chunkPos = level.getChunkAt(blockEntity.getBlockPos()).getPos();
            for (int y = level.getMinSection(); y < level.getMaxSection(); ++y) {
                SodiumWorldRenderer.instance().scheduleRebuildForChunk(chunkPos.x, y, chunkPos.z, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
