package me.linstar.illusion.capability;

import me.linstar.illusion.data.IllusionData;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IllusionChunkData implements IIllusionChunkData {
    final Map<BlockPos, IllusionData> data = new ConcurrentHashMap<>();

    @Override
    public void updateData(BlockPos pos, IllusionData data) {
        this.data.put(pos, data);
    }

    @Override
    public void deleteData(BlockPos pos) {
        this.data.remove(pos);
    }

    @Override
    public IllusionData getData(BlockPos pos) {
        return this.data.get(pos);
    }

    @Override
    public Collection<BlockPos> getKeys() {
        return data.keySet();
    }

}
