package me.linstar.illusion.item;

import com.google.common.collect.ImmutableList;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.Network;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class BlockStateTool extends Item implements IllusionItem {
    public static final String NAME = "block_state_tool";
    public BlockStateTool() {
        super(new Properties());
    }

    //TODO: 变量名优化
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context){
        Level level = context.getLevel();

        if (level.isClientSide) return InteractionResult.SUCCESS;

        var pos = context.getClickedPos();
        var chunk = level.getChunkAt(pos);
        var optional = chunk.getCapability(Illusion.CHUNK_DATA_CAP);
        var blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null || !optional.isPresent()) return InteractionResult.CONSUME;

        var capability = optional.orElseThrow(NullPointerException::new);
        var blockData = capability.getData(pos);
        if (blockData == null) return InteractionResult.CONSUME;

        if (blockData.getType() != IllusionData.DataType.BLOCK) return InteractionResult.CONSUME;

        var modelData = (IllusionData.BlockModelData) blockData.getModelData();
        Block targetBlock = modelData.block();
        ImmutableList<BlockState> blockStates = targetBlock.getStateDefinition().getPossibleStates();

        int state = modelData.state();
        state = (state + 1 == blockStates.size()) ? 0 : state + 1;

        modelData.setState(state);
        chunk.setUnsaved(true);

        ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
        if (serverPlayer == null) return InteractionResult.CONSUME;

        serverPlayer.playNotifySound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);
        serverPlayer.sendSystemMessage(Component.translatable("text.illusion.block_state_tool", state), true);

        Network.CHANNEL.send(
                PacketDistributor.PLAYER.with(()-> serverPlayer),
                new IllusionDataS2CPacket(pos, blockData)
        );
        return InteractionResult.FAIL;
    }
}
