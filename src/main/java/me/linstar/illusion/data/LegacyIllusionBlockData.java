package me.linstar.illusion.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public record LegacyIllusionBlockData(Vec3 offset, ResourceLocation block, int state){
    public static final String OFFSET_X = "OffsetX";
    public static final String OFFSET_Y = "OffsetY";
    public static final String OFFSET_Z = "OffsetZ";
    public static final String STATE = "State";
    public static final String BLOCK_ID = "id";

    public LegacyIllusionBlockData(CompoundTag tag){
        this(new Vec3(tag.getDouble(OFFSET_X), tag.getDouble(OFFSET_Y), tag.getDouble(OFFSET_Z)), new ResourceLocation(tag.getString(BLOCK_ID)), tag.getInt(STATE));
    }

    public IllusionData transformToNewData(){
        Block block = ForgeRegistries.BLOCKS.getValue(this.block);
        if(block == null) return null;
        return new IllusionData(offset, new IllusionData.BlockModelData(block, this.state));
    }
}
