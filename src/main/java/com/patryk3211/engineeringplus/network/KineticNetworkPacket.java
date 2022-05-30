package com.patryk3211.engineeringplus.network;

import com.patryk3211.engineeringplus.kinetic.client.ClientKineticNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class KineticNetworkPacket {
    public enum Type {
        CREATE_NETWORK,
        DELETE_NETWORK,
        UPDATE_VALUES,
        DELETE_ALL,
        NETWORKS
    }

    public record Network(UUID id, float speed, float angle) {
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(id);
            buffer.writeFloat(speed);
            buffer.writeFloat(angle);
        }

        public static Network decode(FriendlyByteBuf buffer) {
            return new Network(buffer.readUUID(), buffer.readFloat(), buffer.readFloat());
        }
    }

    private final UUID networkId;
    private final Type type;
    private final float speed;
    private final float angle;
    private final float speedChange;
    private final Collection<Network> networks;

    public KineticNetworkPacket(Type type) {
        this.type = type;
        this.networkId = null;
        this.speed = 0;
        this.angle = 0;
        this.speedChange = 0;
        this.networks = null;
    }

    public KineticNetworkPacket(UUID networkId, Type type) {
        this.networkId = networkId;
        this.type = type;
        this.speed = 0;
        this.angle = 0;
        this.speedChange = 0;
        this.networks = null;
    }

    public KineticNetworkPacket(UUID networkId, Type type, float speed, float angle, float speedChange) {
        this.networkId = networkId;
        this.type = type;
        this.speed = speed;
        this.angle = angle;
        this.speedChange = speedChange;
        this.networks = null;
    }

    public KineticNetworkPacket(Type type, Collection<Network> networks) {
        this.networkId = null;
        this.type = type;
        this.speed = 0;
        this.angle = 0;
        this.speedChange = 0;
        this.networks = networks;
    }

    public static void encode(KineticNetworkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.type);
        switch(packet.type) {
            case DELETE_ALL:
                break;
            case CREATE_NETWORK, DELETE_NETWORK:
                buffer.writeUUID(packet.networkId);
                break;
            case UPDATE_VALUES:
                buffer.writeUUID(packet.networkId);
                buffer.writeFloat(packet.speed);
                buffer.writeFloat(packet.angle);
                buffer.writeFloat(packet.speedChange);
                break;
            case NETWORKS:
                buffer.writeVarInt(packet.networks.size());
                for (Network network : packet.networks) network.encode(buffer);
                break;
        }
    }

    public static KineticNetworkPacket decode(FriendlyByteBuf buffer) {
        Type type = buffer.readEnum(Type.class);
        return switch(type) {
            case DELETE_ALL -> new KineticNetworkPacket(type);
            case CREATE_NETWORK, DELETE_NETWORK -> new KineticNetworkPacket(buffer.readUUID(), type);
            case UPDATE_VALUES -> new KineticNetworkPacket(buffer.readUUID(), type, buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            case NETWORKS -> {
                List<Network> networks = new LinkedList<>();
                int size = buffer.readVarInt();
                for(int i = 0; i < size; ++i) networks.add(Network.decode(buffer));
                yield new KineticNetworkPacket(type, networks);
            }
        };
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
            case UPDATE_VALUES -> ClientKineticNetwork.getNetwork(packet.networkId).setValues(packet.speed, packet.angle, packet.speedChange);
            case DELETE_ALL -> ClientKineticNetwork.removeAll();
            case NETWORKS -> {
                for (Network network : packet.networks) {
                    ClientKineticNetwork knet = new ClientKineticNetwork(network.id);
                    knet.setValues(network.speed, network.angle, packet.speedChange);
                }
            }
        }
    }
}
