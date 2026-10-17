package me.linstar.illusion.network;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.data.IllusionData;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IllusionDataS2CPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<IllusionDataS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Illusion.MOD_ID, "id"));

    public static final StreamCodec<RegistryFriendlyByteBuf, IllusionDataS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.writeTo(buf),
            IllusionDataS2CPacket::new
    );

    private final BlockPos pos;
    private final IllusionData data;

    public IllusionDataS2CPacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.data = new IllusionData(Objects.requireNonNull(buf.readNbt()), buf.registryAccess());
    }

    public IllusionDataS2CPacket(BlockPos pos , IllusionData data) {
        this.pos = pos;
        this.data = data;
    }

    public void writeTo(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(data.save(buf.registryAccess()));
    }

    public static void handle(final IllusionDataS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> payload.execute(true));
    }

    public void execute(boolean withUpdate){
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        LevelChunk chunk = level.getChunkAt(pos);
        BlockEntity blockEntity = chunk.getBlockEntity(pos);
        if (blockEntity == null) return;

        data.saveToBlock(blockEntity.getPersistentData(), level.registryAccess());

//        var sourceState = level.getBlockState(pos);
//        level.setBlock(pos, sourceState, 0);

        if (withUpdate) {
            for (int y = level.getMinSection(); y < level.getMaxSection(); ++y) {
                SodiumWorldRenderer.instance().scheduleRebuildForChunk(chunk.getPos().x, y, chunk.getPos().z, false);
            }
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
