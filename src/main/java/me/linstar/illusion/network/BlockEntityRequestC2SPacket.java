package me.linstar.illusion.network;

import me.linstar.illusion.Illusion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record BlockEntityRequestC2SPacket(ChunkPos chunkPos) {
    public BlockEntityRequestC2SPacket(FriendlyByteBuf buffer) {
        this(buffer.readChunkPos());
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.level().hasChunk(chunkPos.x, chunkPos.z)) return;

            LevelChunk chunk = player.level().getChunk(chunkPos.x, chunkPos.z);  //TODO: 考虑服务端区块加载速度
            chunk.getCapability(Illusion.CHUNK_DATA_CAP).ifPresent(cap -> {
                var packet = new BoundIllusionDataS2CPacket(chunkPos);
                var keys = cap.getKeys();
                if (keys.isEmpty()) return;
                keys.forEach(key -> packet.appendPacket(new IllusionDataS2CPacket(key, cap.getData(key))));

                Network.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        packet
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
