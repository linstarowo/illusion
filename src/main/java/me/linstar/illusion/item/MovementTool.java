package me.linstar.illusion.item;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

//TODO: 代码块优化
public class MovementTool extends Item implements IllusionItem {
    public static final String NAME = "movement_tool";
    public static final String STATE = "state";

    public MovementTool() {
        super(new Properties().stacksTo(1));
    }

    public static void change(ItemStack stack){
        int state = stack.getOrCreateTag().getInt("state");
        stack.getOrCreateTag().putInt("state", changeState(state));
        stack.setHoverName(Component.translatable("item.illusion.movement_tool.state" + changeState(state)));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) MovementTool.change(stack);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide){
            return InteractionResult.SUCCESS;
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        BlockPos pos = context.getClickedPos();
        LevelChunk chunk = level.getChunkAt(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null || player == null){
            return InteractionResult.FAIL;
        }
        var cap = chunk.getCapability(Illusion.CHUNK_DATA_CAP);
        if (!cap.isPresent()) return  InteractionResult.FAIL;

        var illusionData = cap.orElseThrow(NullPointerException::new).getData(pos);
        if (illusionData == null) return  InteractionResult.FAIL;

        ItemStack stack = player.getMainHandItem();

        double offset = (player.isShiftKeyDown() ? -0.1 : 0.1);
        Vec3 offsets = illusionData.getOffset();

        switch (stack.getOrCreateTag().getInt(STATE)) {
            case 0 ->
                    offsets = offsets.add(offset, 0, 0);
            case 1 ->
                    offsets = offsets.add(0, offset, 0);
            case 2 ->
                    offsets = offsets.add(0, 0, offset);
        }

        illusionData.setOffset(offsets);
        chunk.setUnsaved(true);

        player.playNotifySound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);

        Network.CHANNEL.send(
                PacketDistributor.PLAYER.with(()-> player),
                new IllusionDataS2CPacket(pos, illusionData)
        );

        return InteractionResult.FAIL;
    }

    private static int changeState(int state){
        if (state == 2){
            state = 0;
            return state;
        }

        state ++;

        return state;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level p_41422_, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("tooltip.illusion.movement_tool"));
    }
}
