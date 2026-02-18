package me.linstar.illusion;

import com.mojang.logging.LogUtils;
import me.linstar.illusion.capability.IIllusionChunkData;
import me.linstar.illusion.command.TransformDataCommand;
import me.linstar.illusion.item.BlockStateTool;
import me.linstar.illusion.item.IllusionCrystal;
import me.linstar.illusion.item.IllusionItem;
import me.linstar.illusion.item.MovementTool;
import me.linstar.illusion.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod(Illusion.MOD_ID)
public class Illusion {
    public static final String MOD_ID = "illusion";
    public static final ResourceLocation EMPTY_LOCATION = new ResourceLocation("");

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Capability<IIllusionChunkData> CHUNK_DATA_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<Item> ILLUSION_CRYSTAL = ITEMS.register(IllusionCrystal.NAME, IllusionCrystal::new);
    public static final RegistryObject<Item> BLOCK_STATE_TOOL = ITEMS.register(BlockStateTool.NAME, BlockStateTool::new);
    public static final RegistryObject<Item> MOVEMENT_TOOL = ITEMS.register(MovementTool.NAME, MovementTool::new);

    private static AtomicBoolean isYuushyaInstalled;

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.illusion_group"))
            .icon(() -> ILLUSION_CRYSTAL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ILLUSION_CRYSTAL.get());
                output.accept(BLOCK_STATE_TOOL.get());
                output.accept(MOVEMENT_TOOL.get());
            }).build());


    public Illusion() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            Network.register();
            DistExecutor.safeRunWhenOn(Dist.CLIENT,
                    (DistExecutor.SafeSupplier<DistExecutor.SafeRunnable>) () -> Network::buildClient);
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER,
                    (DistExecutor.SafeSupplier<DistExecutor.SafeRunnable>) () -> Network::buildServer);
        });
    }

    // Ensure that players do not interact with blocks when using tools
    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event){
        if (event.getSide().isClient()) return;

        if (event.getItemStack().getItem() instanceof IllusionItem){
            event.setUseBlock(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event){
        TransformDataCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event){
        BlockPos pos = event.getPos();
        Level level = event.getPlayer().level();
        if (level.isClientSide) return;

        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getCapability(CHUNK_DATA_CAP).ifPresent(cap -> {
            cap.deleteData(pos);
            chunk.setUnsaved(true);
        });
    }

    public static boolean isYuushyaInstalled(){
        if(isYuushyaInstalled == null){
            isYuushyaInstalled = new AtomicBoolean();
            try{
                var cls = Class.forName("com.yuushya.modelling.Yuushya");
                isYuushyaInstalled.set(true);
            }catch (Exception ignored){}
        }

        return isYuushyaInstalled.get();
    }

}
