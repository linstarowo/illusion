package me.linstar.illusion.item;

import com.yuushya.modelling.registries.YuushyaRegistries;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.network.IllusionDataS2CPacket;
import me.linstar.illusion.network.Network;
import me.linstar.illusion.network.RemoveDataS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class IllusionCrystal extends Item implements IllusionItem {
    public static final String NAME = "illusion_crystal";
    public IllusionCrystal() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        var pos = context.getClickedPos();
        var blockEntity = level.getBlockEntity(pos);
        var player = (ServerPlayer) context.getPlayer();
        if (player == null) return InteractionResult.SUCCESS;

        var chunk = player.level().getChunkAt(pos);

        if (blockEntity == null) return InteractionResult.CONSUME;

        ItemStack stack = player.getOffhandItem();

        if (stack.getItem() instanceof BlockItem){
            var block = ((BlockItem)stack.getItem()).getBlock();
            var illusionData = new IllusionData(Vec3.ZERO, (Illusion.isYuushyaInstalled() && block.equals(YuushyaRegistries.SHOW_BLOCK.get())) ? new IllusionData.YuushayaModelData(stack) : new IllusionData.BlockModelData(block, 0));

            chunk.getCapability(Illusion.CHUNK_DATA_CAP).ifPresent(c -> {
                c.updateData(pos, illusionData);
                chunk.setUnsaved(true);
            });

            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            Network.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), player.getServer().getPlayerList().getViewDistance() * 16, level.dimension())),
                    new IllusionDataS2CPacket(pos, illusionData)
            );

            player.getMainHandItem().shrink(1);
        }else if (stack.isEmpty()){
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            Network.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), player.getServer().getPlayerList().getViewDistance() * 16, level.dimension())),
                    new RemoveDataS2CPacket(pos)
            );
            player.getMainHandItem().shrink(1);
        }

        return InteractionResult.FAIL;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level p_41422_, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("tooltip.illusion.illusion_crystal"));
    }
}
