package com.patryk3211.engineeringplus.kinetic;

import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.util.*;

public class KineticNetwork {
    public static final Map<UUID, KineticNetwork> networks = new HashMap<>();

    public final Map<BlockPos, Set<Direction>> tiles = new HashMap<>();

    private final UUID id;
    private float speed;
    private float angle;
    private float inertia;

    public KineticNetwork(UUID id) {
        this.id = id;

        networks.put(id, this);
    }

    public void remove() {
        networks.remove(this);
        tiles.clear();
    }

    public float getSpeed() {
        return speed;
    }

    public float getAngle() {
        return angle;
    }

    public float getInertia() {
        return inertia;
    }

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

    public static KineticNetwork getNetwork(UUID id) {
        return networks.get(id);
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldLoad);
    }

    private static void onWorldLoad(final WorldEvent.Load event) {
        networks.clear();
    }

    public static class NetworkStore {
        public KineticNetwork network;
    }

    public static IKineticHandler createHandler(float speedMultiplier, float angleOffset, float inertialMass) {
        return new IKineticHandler() {
            private KineticNetwork network;

            private final NetworkReference reference = new NetworkReference(speedMultiplier, angleOffset) {
                @Override
                public KineticNetwork getNetwork() {
                    return network;
                }
            };

            @Override
            public float getSpeed() {
                return network.speed * reference.speedMultiplier;
            }

            @Override
            public float getAngle() {
                return ((network.angle * reference.speedMultiplier) + reference.angleOffset) % 360;
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
                float localInertia = network.inertia / reference.speedMultiplier;
                network.speed += force / localInertia * 0.05f; // Force / mass * 1/20 second (1 tick)
            }

            @Override
            public float calculateForce(float targetSpeed) {
                float localInertia = network.inertia / reference.speedMultiplier;
                float speedDelta = targetSpeed - network.speed;
                return speedDelta * localInertia * 20f;
            }

            @Override
            public void setNetwork(KineticNetwork network) {
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
                public KineticNetwork getNetwork() {
                    return store.network;
                }
            };

            @Override
            public float getSpeed() {
                return store.network.speed * reference.speedMultiplier;
            }

            @Override
            public float getAngle() {
                return ((store.network.angle * reference.speedMultiplier) + reference.angleOffset) % 360;
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
                float localInertia = store.network.inertia / reference.speedMultiplier;
                store.network.speed += force / localInertia * 0.05f;
            }

            @Override
            public float calculateForce(float targetSpeed) {
                float localInertia = store.network.inertia / reference.speedMultiplier;
                float speedDelta = targetSpeed - store.network.speed;
                return speedDelta * localInertia * 20f;
            }

            @Override
            public void setNetwork(KineticNetwork network) {
                store.network = network;
            }

            @Override
            public NetworkReference getNetworkReference() {
                return reference;
            }
        };
    }
}
