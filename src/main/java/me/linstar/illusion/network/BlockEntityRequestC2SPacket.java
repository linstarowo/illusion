package me.linstar.illusion.network;

import me.linstar.illusion.Illusion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class BlockEntityRequestC2SPacket {
    private final ChunkPos chunkPos;

    public BlockEntityRequestC2SPacket(FriendlyByteBuf buffer) {
        chunkPos = buffer.readChunkPos();
    }

    public BlockEntityRequestC2SPacket(ChunkPos pos) {
        this.chunkPos = pos;
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.level().hasChunk(chunkPos.x, chunkPos.z)) return;

            LevelChunk chunk = player.level().getChunk(chunkPos.x, chunkPos.z);
            chunk.getCapability(Illusion.CHUNK_DATA_CAP).ifPresent(cap -> {
                var packet = new BoundIllusionDataS2CPacket(chunkPos);
                var keys = cap.getKeys();
                if (keys.isEmpty()) return;
                keys.forEach(key -> packet.appendPacket(new IllusionDataS2CPacket(key, cap.getData(key))));

                Network.CHANNEL.send(
                        PacketDistributor.PLAYER.with(()-> player),
                        packet
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
