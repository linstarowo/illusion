package me.linstar.illusion;

import com.mojang.logging.LogUtils;
import me.linstar.illusion.attachment.IllusionChunkData;
import me.linstar.illusion.attachment.IllusionChunkDataSerializer;
import me.linstar.illusion.command.TransformDataCommand;
import me.linstar.illusion.item.BlockStateTool;
import me.linstar.illusion.item.IllusionCrystal;
import me.linstar.illusion.item.IllusionItem;
import me.linstar.illusion.item.MovementTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Mod(Illusion.MOD_ID)
public class Illusion {
    public static final String MOD_ID = "illusion";
//    public static final ResourceLocation EMPTY_LOCATION = new ResourceLocation("");

    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final Supplier<AttachmentType<IllusionChunkData>> CHUNK_DATA =
            ATTACHMENT_TYPES.register(IllusionChunkData.NAME,
                    () -> AttachmentType.builder(IllusionChunkData::new).serialize(new IllusionChunkDataSerializer()).build()
            );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<Item, IllusionCrystal> ILLUSION_CRYSTAL = ITEMS.register(IllusionCrystal.NAME, IllusionCrystal::new);
    public static final DeferredHolder<Item, BlockStateTool> BLOCK_STATE_TOOL = ITEMS.register(BlockStateTool.NAME, BlockStateTool::new);
    public static final DeferredHolder<Item, MovementTool> MOVEMENT_TOOL = ITEMS.register(MovementTool.NAME, MovementTool::new);

    private static AtomicBoolean isYuushyaInstalled;
    private static AtomicBoolean isSableInstalled;

    private static final List<Runnable> runnables = new ArrayList<>();

    public static void executeNextTick(Runnable runnable){
        synchronized (runnables) {
            runnables.add(runnable);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event){
        synchronized (runnables){
            runnables.forEach(Runnable::run);
            runnables.clear();
        }
    }

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.illusion_group"))
            .icon(() -> ILLUSION_CRYSTAL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ILLUSION_CRYSTAL.get());
                output.accept(BLOCK_STATE_TOOL.get());
                output.accept(MOVEMENT_TOOL.get());
            }).build());


    public Illusion(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    // Ensure that players do not interact with blocks when using tools
    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickBlock event){
        if (event.getSide().isClient()) return;

        if (event.getItemStack().getItem() instanceof IllusionItem){
            event.setUseBlock(TriState.FALSE);
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
        IllusionChunkData chunkData = chunk.getData(CHUNK_DATA);
        chunkData.deleteData(pos);
        chunk.setUnsaved(true);
    }

    public static boolean isYuushyaInstalled(){
        if(isYuushyaInstalled == null){
            isYuushyaInstalled = new AtomicBoolean();
            try{
                @SuppressWarnings("unused")
                var cls = Class.forName("com.yuushya.modelling.Yuushya");
                isYuushyaInstalled.set(true);
            }catch (Exception ignored){}
        }

        return isYuushyaInstalled.get();
    }

    public static boolean isSableInstalled(){
        if(isSableInstalled == null){
            isSableInstalled = new AtomicBoolean();
            try{
                @SuppressWarnings("unused")
                var cls = Class.forName("dev.ryanhcode.sable.Sable");
                isSableInstalled.set(true);
            }catch (Exception ignored){}
        }

        return isSableInstalled.get();
    }


}
