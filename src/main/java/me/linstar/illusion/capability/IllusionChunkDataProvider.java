package me.linstar.illusion.capability;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.data.IllusionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Mod.EventBusSubscriber
public class IllusionChunkDataProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Illusion.MOD_ID, "chunk_data");

    private final IIllusionChunkData data = new IllusionChunkData();
    private final LazyOptional<IIllusionChunkData> dataOptional = LazyOptional.of(() -> data);
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Illusion.CHUNK_DATA_CAP) {
            return dataOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        ListTag listTag = new ListTag();
        Collection<BlockPos> posSet = data.getKeys();
        posSet.forEach(pos -> {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Pos", pos.asLong());
            tag.put("Data", data.getData(pos).save());
            listTag.add(tag);
        });

        nbt.put("Data", listTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag listTag = nbt.getList("Data", ListTag.TAG_COMPOUND);
        listTag.forEach(tag -> {
            CompoundTag data = (CompoundTag) tag;
            BlockPos pos = BlockPos.of(data.getLong("Pos"));
            IllusionData illusionData = new IllusionData(data.getCompound("Data"));
            this.data.updateData(pos, illusionData);
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(IDENTIFIER, new IllusionChunkDataProvider());
    }
}
