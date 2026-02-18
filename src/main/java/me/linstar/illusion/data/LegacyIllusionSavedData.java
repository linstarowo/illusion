package me.linstar.illusion.data;

import me.linstar.illusion.Illusion;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class LegacyIllusionSavedData extends SavedData {
    private static final String NAME = "IllusionData";
    private CompoundTag tag = new CompoundTag();

    public LegacyIllusionSavedData(){}
    public LegacyIllusionSavedData(CompoundTag compoundTag){
        tag = compoundTag.getCompound(NAME);
    }

    public void transform(ServerLevel level){
        tag.getAllKeys().forEach(chunkPos -> {
            var chunkData = tag.getCompound(chunkPos);
            var keys = chunkData.getAllKeys();
            LevelChunk chunk = null;
            for(String pos: keys){
                String[] strings = pos.split(", ");
                int x = Integer.parseInt(strings[0]);
                int y = Integer.parseInt(strings[1]);
                int z = Integer.parseInt(strings[2]);
                BlockPos blockPos = new BlockPos(x, y, z);
                if (chunk == null) chunk = level.getChunkAt(blockPos);
                LevelChunk finalChunk = chunk;
                finalChunk.getCapability(Illusion.CHUNK_DATA_CAP).ifPresent(c -> {
                    var newData = new LegacyIllusionBlockData(chunkData.getCompound(pos)).transformToNewData();
                    if (newData == null) return;
                    if (!finalChunk.getBlockState(blockPos).hasBlockEntity()) return;

                    c.updateData(blockPos, newData);
                });
            }
            if (chunk != null) chunk.setUnsaved(true);
            Illusion.LOGGER.info("Finished transforming at {}", chunkPos);
        });

    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put(NAME, tag);
        return compoundTag;
    }

    public static LegacyIllusionSavedData get(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(LegacyIllusionSavedData::new, LegacyIllusionSavedData::new, NAME);
    }
}
