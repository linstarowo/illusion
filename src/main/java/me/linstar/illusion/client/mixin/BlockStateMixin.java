package me.linstar.illusion.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//确保所所有BlockEntity类型方块都能透光
@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IForgeBlockState{
    @Shadow protected abstract @NotNull BlockState asState();

    protected BlockStateMixin(Block block, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> codec) {
        super(block, values, codec);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockGetter getter, @NotNull BlockPos pos) {
        if (this.asState().hasBlockEntity()){
            return true;
        }
        return super.propagatesSkylightDown(getter, pos);
    }
}
