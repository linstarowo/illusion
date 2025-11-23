package me.linstar.illusion.data;

import com.yuushya.modelling.blockentity.ITransformDataInventory;
import com.yuushya.modelling.blockentity.TransformData;
import com.yuushya.modelling.registries.YuushyaRegistries;
import me.linstar.illusion.client.IllusionClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
        this.type = (data instanceof BlockModelData) ? DataType.BLOCK : DataType.CUSTOM;
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
        BLOCK(BlockModelData::new),
        CUSTOM(YuushayaModelData::new);

        final Function<CompoundTag, ModelData> supplier;

        DataType(Function<CompoundTag, ModelData> supplier) {
            this.supplier = supplier;
        }
    }

    public abstract static class ModelData{
        public static final String MODEL_DATA = "data";

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

    public static class YuushayaModelData extends ModelData{
        List<TransformData> transformData;

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
            return YuushyaRegistries.SHOW_BLOCK.get().defaultBlockState();
        }

        @Override
        protected void onLoad(CompoundTag tag) {
            this.transformData = new ArrayList<>();
            ITransformDataInventory.load(tag, this.transformData);
        }

        public List<TransformData> transformData() {
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

            var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
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

    @OnlyIn(Dist.CLIENT)
    public static boolean containsData(BlockPos pos){
        var level = Minecraft.getInstance().level;
        assert level != null;

        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }else {
            return blockEntity.getPersistentData().contains(NAME);
        }
    }
}
