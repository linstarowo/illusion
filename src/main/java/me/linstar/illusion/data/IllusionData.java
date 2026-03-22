package me.linstar.illusion.data;

import com.yuushya.modelling.blockentity.transformData.*;
import com.yuushya.modelling.registries.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class IllusionData {
    public static final String NAME = "IllusionData";

    Vec3 offset;
    final DataType type;
    final ModelData data;

    public IllusionData(CompoundTag tag) {
        var ox = tag.getDouble("oX");
        var oy = tag.getDouble("oY");
        var oz = tag.getDouble("oZ");
        this.offset = new Vec3(ox, oy, oz);

        this.type = DataType.values()[tag.getInt("type")];
        var data = tag.getCompound("data");
        this.data = type.supplier.apply(data);
    }

    public IllusionData(final Vec3 offset, final ModelData data) {
        this.offset = offset;
        this.data = data;
        this.type = DataType.fetch(data);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("oX", offset.x);
        tag.putDouble("oY", offset.y);
        tag.putDouble("oZ", offset.z);

        tag.putInt("type", type.ordinal());
        tag.put("data", data.saveTo());

        return tag;
    }

    public void saveToBlock(CompoundTag tag) {
        tag.put(NAME, this.save());
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
        final Function<CompoundTag, ModelData> supplier;

        DataType(Class<?> clazz ,Function<CompoundTag, ModelData> supplier) {
            this.clazz = clazz;
            this.supplier = supplier;
        }

        public static <T extends ModelData> DataType fetch(T type){
            return Arrays.stream(DataType.values()).filter(dataType -> dataType.clazz == type.getClass()).findFirst().orElse(null);
        }
    }

    public abstract static class ModelData{
        private ModelData(@Nullable CompoundTag tag) {
            if (tag != null) this.onLoad(tag);
        }
        public abstract CompoundTag saveTo();
        protected abstract void onLoad(CompoundTag tag);

        @OnlyIn(Dist.CLIENT)
        public abstract BakedModel getModel();
        @OnlyIn(Dist.CLIENT)
        public abstract BlockState getState();
    }

    public static class YuushayaTextModelData extends ModelData{
        List<TransformTextData> transformData;

        private YuushayaTextModelData(CompoundTag tag) {
            super(tag);
        }

        public YuushayaTextModelData(ItemStack stack){
            super(null);
            this.transformData = new ArrayList<>();
            var tag = Objects.requireNonNull(stack.getTagElement("BlockEntityTag"));
            ITransformTextDataInventory.load(tag, this.transformData);
        }

        @Override
        public CompoundTag saveTo() {
            CompoundTag tag = new CompoundTag();
            ITransformDataInventory.saveAdditional(tag, this.transformData);
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
        protected void onLoad(CompoundTag tag) {
            this.transformData = new ArrayList<>();
            ITransformTextDataInventory.load(tag, this.transformData);
        }

        public List<TransformTextData> transformData() {
            return transformData;
        }
    }

    public static class YuushayaItemModelData extends ModelData{
        List<TransformItemData> transformData;

        private YuushayaItemModelData(CompoundTag tag) {
            super(tag);
        }

        public YuushayaItemModelData(ItemStack stack){
            super(null);
            this.transformData = new ArrayList<>();
            var tag = Objects.requireNonNull(stack.getTagElement("BlockEntityTag"));
            ITransformItemDataInventory.load(tag, this.transformData);
        }

        @Override
        public CompoundTag saveTo() {
            CompoundTag tag = new CompoundTag();
            ITransformDataInventory.saveAdditional(tag, this.transformData);
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
        protected void onLoad(CompoundTag tag) {
            this.transformData = new ArrayList<>();
            ITransformItemDataInventory.load(tag, this.transformData);
        }

        public List<TransformItemData> transformData() {
            return transformData;
        }
    }

    public static class YuushayaModelData extends ModelData{
        List<TransformBlockData> transformData;

        private YuushayaModelData(CompoundTag tag) {
            super(tag);
        }

        public YuushayaModelData(ItemStack stack){
            super(null);
            this.transformData = new ArrayList<>();
            var tag = Objects.requireNonNull(stack.getTagElement("BlockEntityTag"));
            ITransformDataInventory.load(tag, this.transformData);
        }

        @Override
        public CompoundTag saveTo() {
            CompoundTag tag = new CompoundTag();
            ITransformDataInventory.saveAdditional(tag, this.transformData);
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
        protected void onLoad(CompoundTag tag) {
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
            super(null);
            this.block = block;
            this.state = state;
        }

        private BlockModelData(CompoundTag tag) {
            super(tag);
        }

        @Override
        public CompoundTag saveTo() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("state", state);
            var location = ForgeRegistries.BLOCKS.getKey(this.block);
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
        protected void onLoad(CompoundTag tag) {
            var blockId = tag.getString("id");
            var state = tag.getInt("state");

            var block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(blockId));
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
        var blockEntity = getter.getBlockEntity(pos);
        if (blockEntity == null) return false;

        return blockEntity.getPersistentData().contains(IllusionData.NAME);
    }
}
