package me.linstar.illusion.item;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.attachment.IllusionChunkData;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.RemoveDataS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CopyCrystal extends Item implements IllusionItem {
    public static final String NAME = "copy_crystal";
    public static final String DATA = "copy_data";


    public CopyCrystal() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.remove(DATA);
            ((ServerPlayer) player).sendSystemMessage(Component.translatable("text.illusion.copy_crystal.delete"), true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return InteractionResult.FAIL;
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) {
            return InteractionResult.SUCCESS;
        }

        var chunk = level.getChunkAt(pos);
        IllusionChunkData chunkData = chunk.getData(Illusion.CHUNK_DATA);
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (player.isShiftKeyDown()) {
            IllusionData illusionData = chunkData.getData(pos);
            if (illusionData == null) return InteractionResult.FAIL;

            CompoundTag tag = data.copyTag();
            tag.put(DATA, illusionData.save(level.registryAccess()));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        if (!data.copyTag().contains(DATA)) return InteractionResult.FAIL;
        IllusionData illusionData = new IllusionData(data.copyTag().getCompound(DATA), level.registryAccess());

        chunkData.updateData(pos, illusionData);

        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        PacketDistributor.sendToPlayersNear(
                (ServerLevel) level,
                null,
                pos.getX(), pos.getY(), pos.getZ(),
                Objects.requireNonNull(level.getServer()).getPlayerList().getViewDistance() * 16,
                new IllusionDataS2CPacket(pos, illusionData)
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("tooltip.illusion.copy_crystal"));
    }
}
