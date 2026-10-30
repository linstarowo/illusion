package me.linstar.illusion.client;

import com.mojang.datafixers.util.Either;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.client.render.CopyCrystalClientTooltipComponent;
import me.linstar.illusion.data.IllusionData;
import me.linstar.illusion.item.CopyCrystal;
import me.linstar.illusion.item.MovementTool;
import me.linstar.illusion.network.BlockEntityRequestC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

@EventBusSubscriber(value = Dist.CLIENT)
public class IllusionClient {

    public static final ModelProperty<IllusionData> ILLUSION_DATA_PROPERTY = new ModelProperty<>();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(()-> ItemProperties.register(Illusion.MOVEMENT_TOOL.get(), Objects.requireNonNull(ResourceLocation.tryBuild(Illusion.MOD_ID, MovementTool.STATE)), (itemstack, world, entity, idk) -> itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(MovementTool.STATE)));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event){
        ChunkPos pos = event.getChunk().getPos();
        if (Minecraft.getInstance().getConnection() == null) return;
        PacketDistributor.sendToServer(new BlockEntityRequestC2SPayload(pos));
    }

    @SubscribeEvent
    public static void onTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event){
        event.register(CopyCrystal.CopyComponent.class, CopyCrystalClientTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event){
        var stack = event.getItemStack();
        if (stack.getItem() instanceof CopyCrystal){
            var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag.contains(CopyCrystal.DATA)){
                //noinspection DataFlowIssue
                event.getTooltipElements().add(Either.right(new CopyCrystal.CopyComponent(new IllusionData(tag.getCompound(CopyCrystal.DATA), Minecraft.getInstance().level.registryAccess()))));
            }
        }
    }

}
