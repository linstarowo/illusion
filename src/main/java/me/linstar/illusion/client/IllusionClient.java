package me.linstar.illusion.client;

import com.mojang.logging.LogUtils;
import me.linstar.illusion.Illusion;
import me.linstar.illusion.client.mixin.BlockStateAccessor;
import me.linstar.illusion.item.MovementTool;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class IllusionClient {
    public static final Map<BlockState, BlockState> MOTIFIED_STATES = new ConcurrentHashMap<>();
    public static final Unsafe UNSAFE;
    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockState getMotifiedState(BlockState state) {
        if (MOTIFIED_STATES.containsKey(state)) return MOTIFIED_STATES.get(state);
        try {
            Class<?> clazz = state.getClass();
            BlockState motifiedState = (BlockState) UNSAFE.allocateInstance(clazz);

            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    if ((field.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0) continue;

                    field.setAccessible(true);
                    long offset = UNSAFE.objectFieldOffset(field);
                    Object value = UNSAFE.getObject(state, offset);
                    UNSAFE.putObject(motifiedState, offset, value);
                }
                clazz = clazz.getSuperclass();
            }

            ((BlockStateAccessor) motifiedState).setCanOcclude(false);
            motifiedState.initCache();
            MOTIFIED_STATES.put(state, motifiedState);
            return motifiedState;
        }catch (Throwable ex) {
            LogUtils.getLogger().error("Error when modify blockstate", ex);
        }

        return null;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(()-> ItemProperties.register(Illusion.MOVEMENT_TOOL.get(), new ResourceLocation(Illusion.MOD_ID, MovementTool.STATE), (itemstack, world, entity, idk) -> itemstack.getOrCreateTag().getInt(MovementTool.STATE)));
        }
    }
}
