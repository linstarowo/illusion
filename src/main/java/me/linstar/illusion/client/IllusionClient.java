package me.linstar.illusion.client;

import me.linstar.illusion.Illusion;
import me.linstar.illusion.item.MovementTool;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class IllusionClient {
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(()-> ItemProperties.register(Illusion.MOVEMENT_TOOL.get(), new ResourceLocation(Illusion.MOD_ID, MovementTool.STATE), (itemstack, world, entity, idk) -> itemstack.getOrCreateTag().getInt(MovementTool.STATE)));
        }
    }
}
