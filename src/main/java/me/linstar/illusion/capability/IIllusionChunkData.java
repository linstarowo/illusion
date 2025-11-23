package me.linstar.illusion.capability;

import me.linstar.illusion.data.IllusionData;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Collection;

@AutoRegisterCapability
public interface IIllusionChunkData {
    void updateData(BlockPos pos, IllusionData data);
    void deleteData(BlockPos pos);
    IllusionData getData(BlockPos pos);
    Collection<BlockPos> getKeys();
}
