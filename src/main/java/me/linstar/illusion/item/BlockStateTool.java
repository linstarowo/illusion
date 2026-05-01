package me.linstar.illusion.item;

import com.google.common.collect.ImmutableList;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.capability.IIllusionChunkData;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlockStateTool extends Item implements IllusionItem {
    public static final String NAME = "block_state_tool";
    public BlockStateTool() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context){
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        LevelChunk chunk = level.getChunkAt(pos);

        LazyOptional<IIllusionChunkData> capabilityOptional = chunk.getCapability(Illusion.CHUNK_DATA_CAP);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null || !capabilityOptional.isPresent()) {
            return InteractionResult.CONSUME;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        IIllusionChunkData capability = capabilityOptional.resolve().get();
        IllusionData illusionData = capability.getData(pos);

        if (illusionData == null || illusionData.getType() != IllusionData.DataType.BLOCK) {
            return InteractionResult.CONSUME;
        }

        IllusionData.BlockModelData modelData = (IllusionData.BlockModelData) illusionData.getModelData();
        Block targetBlock = modelData.block();
        ImmutableList<BlockState> possibleStates = targetBlock.getStateDefinition().getPossibleStates();

        int currentState = modelData.state();
        int nextState = (currentState + 1) % possibleStates.size();
        modelData.setState(nextState);

        chunk.setUnsaved(true);

        ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
        if (serverPlayer == null) {
            return InteractionResult.CONSUME;
        }

        serverPlayer.playNotifySound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverPlayer.sendSystemMessage(Component.translatable("text.illusion.block_state_tool", nextState), true);

        Network.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new IllusionDataS2CPacket(pos, illusionData)
        );

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level p_41422_, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("tooltip.illusion.block_state_tool"));
    }
}
