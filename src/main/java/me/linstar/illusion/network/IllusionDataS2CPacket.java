package me.linstar.illusion.network;

import com.mojang.logging.LogUtils;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.linstar.illusion.client.IllusionClient;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class IllusionDataS2CPacket {
    private final BlockPos pos;
    private final IllusionData data;

    public IllusionDataS2CPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.data = new IllusionData(Objects.requireNonNull(buffer.readNbt()));
    }

    public IllusionDataS2CPacket(BlockPos pos , IllusionData data) {
        this.pos = pos;
        this.data = data;
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(data.save());
    }

    public void handler(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> this.execute(true));
        ctx.get().setPacketHandled(true);
    }

    public void execute(boolean withUpdate){
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (blockEntity == null) return;

        data.saveToBlock(blockEntity.getPersistentData());
        ChunkPos chunkPos = level.getChunkAt(blockEntity.getBlockPos()).getPos();

        var sourceState = level.getBlockState(pos);
        level.setBlock(pos, sourceState, 0);
        LogUtils.getLogger().info("motified! in level");
//        if (withUpdate) {
//            for (int y = level.getMinSection(); y < level.getMaxSection(); ++y) {
//                SodiumWorldRenderer.instance().scheduleRebuildForChunk(chunkPos.x, y, chunkPos.z, false);
//            }
//        }
    }
}
