package me.linstar.illusion.network;

import me.linstar.illusion.Illusion;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {
    public static SimpleChannel CHANNEL;
    public static final String VERSION = "1.0";
    public static void register(){
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Illusion.MOD_ID, "bec"),
                ()-> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
    }

    public static void buildServer(){
        CHANNEL.messageBuilder(BlockEntityRequestC2SPacket.class, 0)
                .encoder(BlockEntityRequestC2SPacket::writeTo)
                .decoder(BlockEntityRequestC2SPacket::new)
                .consumerNetworkThread(BlockEntityRequestC2SPacket::handler).add();

        CHANNEL.messageBuilder(IllusionDataS2CPacket.class, 1)
                .encoder(IllusionDataS2CPacket::writeTo)
                .decoder(IllusionDataS2CPacket::new)
                .consumerNetworkThread((paket, ctx) -> {
                    ctx.get().setPacketHandled(true);   //Do not handle in server side
                }).add();

        CHANNEL.messageBuilder(BoundIllusionDataS2CPacket.class, 2)
                .encoder(BoundIllusionDataS2CPacket::writeTo)
                .decoder(BoundIllusionDataS2CPacket::new)
                .consumerNetworkThread((paket, ctx) -> {
                    ctx.get().setPacketHandled(true);
                }).add();

        CHANNEL.messageBuilder(RemoveDataS2CPacket.class, 3)
                .encoder(RemoveDataS2CPacket::writeTo)
                .decoder(RemoveDataS2CPacket::new)
                .consumerNetworkThread((paket, ctx) -> {
                    ctx.get().setPacketHandled(true);
                }).add();
    }

    public static void buildClient(){
        CHANNEL.messageBuilder(BlockEntityRequestC2SPacket.class, 0)
                .encoder(BlockEntityRequestC2SPacket::writeTo)
                .decoder(BlockEntityRequestC2SPacket::new)
                .consumerNetworkThread(BlockEntityRequestC2SPacket::handler).add();

        CHANNEL.messageBuilder(IllusionDataS2CPacket.class, 1)
                .encoder(IllusionDataS2CPacket::writeTo)
                .decoder(IllusionDataS2CPacket::new)
                .consumerNetworkThread(IllusionDataS2CPacket::handler).add();

        CHANNEL.messageBuilder(BoundIllusionDataS2CPacket.class, 2)
                .encoder(BoundIllusionDataS2CPacket::writeTo)
                .decoder(BoundIllusionDataS2CPacket::new)
                .consumerNetworkThread(BoundIllusionDataS2CPacket::handler).add();

        CHANNEL.messageBuilder(RemoveDataS2CPacket.class, 3)
                .encoder(RemoveDataS2CPacket::writeTo)
                .decoder(RemoveDataS2CPacket::new)
                .consumerNetworkThread(RemoveDataS2CPacket::handler).add();
    }
}
