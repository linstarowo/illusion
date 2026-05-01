package me.linstar.illusion.item;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MovementTool extends Item implements IllusionItem {
    public static final String NAME = "movement_tool";
    public static final String STATE = "state";
    private static final int MAX_STATE = 2;

    public MovementTool() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            int currentState = tag.getInt(STATE);
            int newState = (currentState + 1) % (MAX_STATE + 1);
            tag.putInt(STATE, newState);
            stack.setHoverName(Component.translatable("item.illusion.movement_tool.state" + newState));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        LevelChunk chunk = level.getChunkAt(pos);
        IllusionData illusionData = chunk.getCapability(Illusion.CHUNK_DATA_CAP)
                .map(cap -> cap.getData(pos))
                .orElse(null);

        if (illusionData == null) {
            return InteractionResult.FAIL;
        }

        double offsetAmount = player.isShiftKeyDown() ? -0.1 : 0.1;
        Vec3 baseOffset = illusionData.getOffset();
        int state = player.getMainHandItem().getOrCreateTag().getInt(STATE);
        Vec3 newOffset = switch (state) {
            case 0 -> baseOffset.add(offsetAmount, 0, 0);
            case 1 -> baseOffset.add(0, offsetAmount, 0);
            case 2 -> baseOffset.add(0, 0, offsetAmount);
            default -> baseOffset; // 未知状态不作更改
        };
        illusionData.setOffset(newOffset);

        chunk.setUnsaved(true);
        player.playNotifySound(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);
        Network.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new IllusionDataS2CPacket(pos, illusionData)
        );

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level p_41422_, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("tooltip.illusion.movement_tool"));
    }
}
