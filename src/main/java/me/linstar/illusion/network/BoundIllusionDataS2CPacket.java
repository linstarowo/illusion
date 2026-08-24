package me.linstar.illusion.network;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BoundIllusionDataS2CPacket {
    private final ChunkPos chunkPos;
    private final List<IllusionDataS2CPacket> packets = new ArrayList<>();

    public BoundIllusionDataS2CPacket(FriendlyByteBuf buffer) {
        chunkPos = buffer.readChunkPos();
        while (buffer.isReadable()){
            packets.add(new IllusionDataS2CPacket(buffer));
        }
    }

    public BoundIllusionDataS2CPacket(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    public void appendPacket(IllusionDataS2CPacket packet) {
        this.packets.add(packet);
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
        packets.forEach(p -> p.writeTo(buf));
    }

    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(
                ()-> {
                    packets.forEach(packet -> packet.execute(false));
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return;
                    for (int y = level.getMinSection(); y < level.getMaxSection(); ++y) {
                        SodiumWorldRenderer.instance().scheduleRebuildForChunk(chunkPos.x, y, chunkPos.z, false);
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
