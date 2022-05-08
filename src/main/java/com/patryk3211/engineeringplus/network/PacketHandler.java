package com.patryk3211.engineeringplus.network;

import com.patryk3211.engineeringplus.StaticConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StaticConfig.MOD_ID, "main"),
            () -> StaticConfig.PROTOCOL_VERSION,
            StaticConfig.PROTOCOL_VERSION::equals,
            StaticConfig.PROTOCOL_VERSION::equals);

    private static int id = 0;

    public static <MSG> void register(Class<MSG> packetClass, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(id++, packetClass, encoder, decoder, handler);
    }
}
