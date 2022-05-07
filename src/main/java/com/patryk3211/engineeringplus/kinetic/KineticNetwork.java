package com.patryk3211.engineeringplus.kinetic;

import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import com.patryk3211.engineeringplus.network.KineticNetworkPacket;
import com.patryk3211.engineeringplus.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class KineticNetwork implements IKineticNetwork {
    public static final Map<UUID, KineticNetwork> networks = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<KineticNetwork>> networksByDims = new HashMap<>();

    public final Map<BlockPos, Set<Direction>> tiles = new HashMap<>();

    private final UUID id;
    private final Level level;

    private float speed;
    private float angle;
    private float inertia;

    public KineticNetwork(UUID id, Level level) {
        this.id = id;
        this.level = level;

        networks.put(id, this);
        if(networksByDims.containsKey(level.dimension())) networksByDims.get(level.dimension()).add(this);
        else {
            Set<KineticNetwork> networks = new HashSet<>();
            networks.add(this);
            networksByDims.put(level.dimension(), networks);
        }

        PacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new KineticNetworkPacket(id, KineticNetworkPacket.Type.CREATE_NETWORK));
    }

    public void remove() {
        networks.remove(id);
        networksByDims.get(level.dimension()).remove(this);
        tiles.clear();

        PacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new KineticNetworkPacket(id, KineticNetworkPacket.Type.DELETE_NETWORK));
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public float getInertia() {
        return inertia;
    }

    @Override
    public void changeSpeed(float amount) {
        speed += amount;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void addTile(BlockPos position, Direction direction) {
        if(tiles.containsKey(position)) tiles.get(position).add(direction);
        else {
            Set<Direction> dirs = new HashSet<>();
            dirs.add(direction);
            tiles.put(position, dirs);
        }
    }

    public void addMass(float mass, float speed) {
        float kineticEnergy = inertia * this.speed + mass * speed;
        inertia += mass;
        this.speed = kineticEnergy / inertia;
    }

    public void removeMass(float mass) {
        inertia -= mass;
    }

    public void setValues(float speed, float angle) {
        this.speed = speed;
        this.angle = angle;
    }

    public void syncValues() {
        // TODO: [06.05.2022] This should only send the packet to players in range of this network.
        PacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new KineticNetworkPacket(id, KineticNetworkPacket.Type.UPDATE_VALUES, speed, angle));
    }

    /* Start of static functions */
    public static KineticNetwork getNetwork(UUID id) {
        return networks.get(id);
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldTick);
    }

    private static void onWorldLoad(final WorldEvent.Load event) {
        networks.clear();
    }

    static int tickCount = 0;
    private static void onWorldTick(final TickEvent.WorldTickEvent event) {
        Set<KineticNetwork> networks = networksByDims.get(event.world.dimension());
        if(networks == null) return;

        for (KineticNetwork network : networks) {
            network.angle = (network.angle + network.speed * 0.05f / 60f) % 360f;
        }
        if(++tickCount >= 20) {
            for (KineticNetwork network : networks) network.syncValues();
            tickCount = 0;
        }
    }

    public static class NetworkStore {
        public IKineticNetwork network;
    }

    public static IKineticHandler createHandler(float speedMultiplier, float angleOffset, float inertialMass) {
        return new IKineticHandler() {
            private IKineticNetwork network;

            private final NetworkReference reference = new NetworkReference(speedMultiplier, angleOffset) {
                @Override
                public IKineticNetwork getNetwork() {
                    return network;
                }
            };

            @Override
            public float getSpeed() {
                if(network == null) return 0;
                return network.getSpeed() * reference.speedMultiplier;
            }

            @Override
            public float getAngle() {
                if(network == null) return 0;
                return ((network.getAngle() * reference.speedMultiplier) + reference.angleOffset) % 360;
            }

            @Override
            public float getSpeedMultiplier() {
                return speedMultiplier;
            }

            @Override
            public float getAngleOffset() {
                return angleOffset;
            }

            @Override
            public float getInertia() {
                return inertialMass;
            }

            @Override
            public void applyForce(float force) {
                if(network == null) return;
                float localInertia = network.getInertia() / reference.speedMultiplier;
                network.changeSpeed(force / localInertia * 0.05f); // Force / mass * 1/20 second (1 tick)
            }

            @Override
            public float calculateForce(float targetSpeed) {
                if(network == null) return 0;
                float localInertia = network.getInertia() / reference.speedMultiplier;
                float speedDelta = targetSpeed - network.getSpeed();
                return speedDelta * localInertia * 20f;
            }

            @Override
            public void setNetwork(IKineticNetwork network) {
                this.network = network;
            }

            @Override
            public NetworkReference getNetworkReference() {
                return reference;
            }
        };
    }

    public static IKineticHandler createHandler(NetworkStore networkStore, float speedMultiplier, float angleOffset, float inertialMass) {
        return new IKineticHandler() {
            private final NetworkStore store = networkStore;

            private final NetworkReference reference = new NetworkReference(speedMultiplier, angleOffset) {
                @Override
                public IKineticNetwork getNetwork() {
                    return store.network;
                }
            };

            @Override
            public float getSpeed() {
                return store.network.getSpeed() * reference.speedMultiplier;
            }

            @Override
            public float getAngle() {
                return ((store.network.getAngle() * reference.speedMultiplier) + reference.angleOffset) % 360;
            }

            @Override
            public float getSpeedMultiplier() {
                return speedMultiplier;
            }

            @Override
            public float getAngleOffset() {
                return angleOffset;
            }

            @Override
            public float getInertia() {
                return inertialMass;
            }

            @Override
            public void applyForce(float force) {
                float localInertia = store.network.getInertia() / reference.speedMultiplier;
                store.network.changeSpeed(force / localInertia * 0.05f);
            }

            @Override
            public float calculateForce(float targetSpeed) {
                float localInertia = store.network.getInertia() / reference.speedMultiplier;
                float speedDelta = targetSpeed - store.network.getSpeed();
                return speedDelta * localInertia * 20f;
            }

            @Override
            public void setNetwork(IKineticNetwork network) {
                store.network = network;
            }

            @Override
            public NetworkReference getNetworkReference() {
                return reference;
            }
        };
    }
}
