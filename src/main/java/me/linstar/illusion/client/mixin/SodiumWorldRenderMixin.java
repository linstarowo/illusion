package me.linstar.illusion.client.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.linstar.illusion.network.BlockEntityRequestC2SPacket;
import me.linstar.illusion.network.Network;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSectionManager.class, remap = false)
public class SodiumWorldRenderMixin {
    @Inject(method = "onChunkAdded", at = @At("HEAD"))
    public void onChunkAdded(int x, int z, CallbackInfo ci){
        Network.CHANNEL.sendToServer(new BlockEntityRequestC2SPacket(new ChunkPos(x, z)));
    }
}
