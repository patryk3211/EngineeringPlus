package com.patryk3211.engineeringplus.network;

import com.patryk3211.engineeringplus.kinetic.client.ClientKineticNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class KineticNetworkPacket {
    public enum Type {
        CREATE_NETWORK,
        DELETE_NETWORK,
        UPDATE_VALUES
    }

    private final UUID networkId;
    private final Type type;
    private final float speed;
    private final float angle;

    public KineticNetworkPacket(UUID networkId, Type type) {
        this.networkId = networkId;
        this.type = type;
        this.speed = 0;
        this.angle = 0;
    }

    public KineticNetworkPacket(UUID networkId, Type type, float speed, float angle) {
        this.networkId = networkId;
        this.type = type;
        this.speed = speed;
        this.angle = angle;
    }

    public static void encode(KineticNetworkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.networkId);
        buffer.writeEnum(packet.type);
        if(packet.type == Type.UPDATE_VALUES) {
            buffer.writeFloat(packet.speed);
            buffer.writeFloat(packet.angle);
        }
    }

    public static KineticNetworkPacket decode(FriendlyByteBuf buffer) {
        UUID id = buffer.readUUID();
        Type type = buffer.readEnum(Type.class);
        float speed = 0;
        float angle = 0;
        if(type == Type.UPDATE_VALUES) {
            speed = buffer.readFloat();
            angle = buffer.readFloat();
        }
        return new KineticNetworkPacket(id, type, speed, angle);
    }

    public static void handle(KineticNetworkPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(KineticNetworkPacket packet) {
        switch(packet.type) {
            case CREATE_NETWORK -> new ClientKineticNetwork(packet.networkId);
            case DELETE_NETWORK -> ClientKineticNetwork.remove(packet.networkId);
            case UPDATE_VALUES -> ClientKineticNetwork.getNetwork(packet.networkId).setValues(packet.speed, packet.angle);
        }
    }
}
