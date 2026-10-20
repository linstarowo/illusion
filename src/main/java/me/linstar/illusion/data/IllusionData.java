package me.linstar.illusion.data;

import com.yuushya.modelling.blockentity.transformData.*;
import com.yuushya.modelling.registries.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class IllusionData {
    public static final String NAME = "IllusionData";

    Vec3 offset;
    final DataType type;
    final ModelData data;

    public IllusionData(CompoundTag tag, HolderLookup.Provider provider) {
        var ox = tag.getDouble("oX");
        var oy = tag.getDouble("oY");
        var oz = tag.getDouble("oZ");
        this.offset = new Vec3(ox, oy, oz);

        this.type = DataType.values()[tag.getInt("type")];
        var data = tag.getCompound("data");
        this.data = type.supplier.apply(data, provider);
    }

    public IllusionData(final Vec3 offset, final ModelData data) {
        this.offset = offset;
        this.data = data;
        this.type = DataType.fetch(data);
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("oX", offset.x);
        tag.putDouble("oY", offset.y);
        tag.putDouble("oZ", offset.z);

        tag.putInt("type", type.ordinal());
        tag.put("data", data.saveTo(provider));

        return tag;
    }

    public void saveToBlock(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put(NAME, this.save(provider));
    }

    public ModelData getModelData() {
        return this.data;
    }

    public DataType getType() {
        return this.type;
    }

    public Vec3 getOffset() {
        return this.offset;
    }

    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    public enum DataType {
        BLOCK(BlockModelData.class ,BlockModelData::new),
        CUSTOM(YuushayaModelData.class ,YuushayaModelData::new),
        CUSTOM_ITEM(YuushayaItemModelData.class, YuushayaItemModelData::new);

        final Class<?> clazz;
        final BiFunction<CompoundTag, HolderLookup.Provider, ModelData> supplier;

        DataType(Class<?> clazz ,BiFunction<CompoundTag, HolderLookup.Provider, ModelData> supplier) {
            this.clazz = clazz;
            this.supplier = supplier;
        }

        public static <T extends ModelData> DataType fetch(T type){
            return Arrays.stream(DataType.values()).filter(dataType -> dataType.clazz == type.getClass()).findFirst().orElse(null);
        }
    }

    public abstract static class ModelData{
        private ModelData(@Nullable CompoundTag tag, @Nullable HolderLookup.Provider provider) {
            if (tag != null) this.onLoad(tag, provider);
        }
        public abstract CompoundTag saveTo(HolderLookup.Provider provider);
        protected abstract void onLoad(CompoundTag tag, @Nullable HolderLookup.Provider provider);

        @OnlyIn(Dist.CLIENT)
        public abstract BakedModel getModel();
        @OnlyIn(Dist.CLIENT)
        public abstract BlockState getState();
    }

    public static class YuushayaTextModelData extends ModelData{
        List<TransformTextData> transformData;

        private YuushayaTextModelData(CompoundTag tag, HolderLookup.Provider provider) {
            super(tag, provider);
        }

        public YuushayaTextModelData(ItemStack stack){
            super(null, null);
            this.transformData = new ArrayList<>();
            var tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            ITransformTextDataInventory.load(tag, this.transformData);
        }

        @Override
        public CompoundTag saveTo(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            ITransformTextDataInventory.saveAdditional(tag, this.transformData, provider);
            return tag;
        }

        @Override
        public BakedModel getModel() {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.getState());
        }

        @Override
        public BlockState getState() {
            return BlockRegistry.TEXT_BLOCK.get().defaultBlockState();
        }

        @Override
        protected void onLoad(CompoundTag tag, @Nullable HolderLookup.Provider provider) {
            this.transformData = new ArrayList<>();
            ITransformTextDataInventory.load(tag, this.transformData);
        }

        public List<TransformTextData> transformData() {
            return transformData;
        }
    }

    public static class YuushayaItemModelData extends ModelData{
        List<TransformItemData> transformData;

        private YuushayaItemModelData(CompoundTag tag, HolderLookup.Provider provider) {
            super(tag, provider);
        }

        public YuushayaItemModelData(ItemStack stack, HolderLookup.Provider provider){
            super(null, null);
            this.transformData = new ArrayList<>();
            var tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            ITransformItemDataInventory.load(tag, this.transformData, provider);
        }

        @Override
        public CompoundTag saveTo(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            ITransformItemDataInventory.saveAdditional(tag, this.transformData, provider);
            return tag;
        }

        @Override
        public BakedModel getModel() {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.getState());
        }

        @Override
        public BlockState getState() {
            return BlockRegistry.ITEM_BLOCK.get().defaultBlockState();
        }

        @Override
        protected void onLoad(CompoundTag tag, @Nullable HolderLookup.Provider provider) {
            this.transformData = new ArrayList<>();
            ITransformItemDataInventory.load(tag, this.transformData, provider);
        }

        public List<TransformItemData> transformData() {
            return transformData;
        }
    }

    public static class YuushayaModelData extends ModelData{
        List<TransformBlockData> transformData;

        private YuushayaModelData(CompoundTag tag, HolderLookup.Provider provider) {
            super(tag, provider);
        }

        public YuushayaModelData(ItemStack stack){
            super(null, null);
            this.transformData = new ArrayList<>();
            var tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            ITransformDataInventory.load(tag, this.transformData);
        }

        @Override
        public CompoundTag saveTo(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            ITransformDataInventory.saveAdditional(tag, this.transformData, provider);
            return tag;
        }

        @Override
        public BakedModel getModel() {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.getState());
        }

        @Override
        public BlockState getState() {
            return BlockRegistry.SHOW_BLOCK.get().defaultBlockState();
        }

        @Override
        protected void onLoad(CompoundTag tag, @Nullable HolderLookup.Provider provider) {
            this.transformData = new ArrayList<>();
            ITransformDataInventory.load(tag, this.transformData);
        }

        public List<TransformBlockData> transformData() {
            return transformData;
        }
    }

    public static class BlockModelData extends ModelData{
        public static final String AIR = "minecraft:air";

        Block block;
        int state;

        public BlockModelData(Block block, int state) {
            super(null, null);
            this.block = block;
            this.state = state;
        }

        private BlockModelData(CompoundTag tag, HolderLookup.Provider provider) {
            super(tag, provider);
        }

        @Override
        public CompoundTag saveTo(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("state", state);
            var location = BuiltInRegistries.BLOCK.getKey(this.block);
            tag.putString("id", (location != null) ? location.toString() : AIR);

            return tag;
        }

        @Override
        public BakedModel getModel() {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.getState());
        }

        @Override
        public BlockState getState() {
            return block.getStateDefinition().getPossibleStates().get(state);
        }

        @Override
        protected void onLoad(CompoundTag tag, @Nullable HolderLookup.Provider provider) {
            var blockId = tag.getString("id");
            var state = tag.getInt("state");

            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockId));
            this.block = (block != null) ? block : Blocks.AIR;
            this.state = state;
        }

        public Block block() {
            return this.block;
        }

        public int state() {
            return this.state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    public static boolean containsData(BlockGetter getter, BlockPos pos){
        if (getter == null) return false;
        try {
            var blockEntity = getter.getBlockEntity(pos);
            if (blockEntity == null) return false;

            return blockEntity.getPersistentData().contains(IllusionData.NAME);
        }catch (Exception e){
            return false;
        }
    }
}
