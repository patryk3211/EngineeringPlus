package com.patryk3211.engineeringplus.kinetic;

import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.util.*;

public class KineticNetwork {
    public static final Map<UUID, KineticNetwork> networks = new HashMap<>();

    public static class NetworkPosition {
        public BlockPos position;
        public Direction direction;

        public NetworkPosition(BlockPos position, Direction direction) {
            this.position = position;
            this.direction = direction;
        }

        @Override
        public int hashCode() {
            return position.hashCode() ^ (direction != null ? (direction.hashCode() << 1) : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof NetworkPosition) return position == ((NetworkPosition) obj).position && direction == ((NetworkPosition) obj).direction;
            else return false;
        }
    }

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

    public static KineticNetwork getNetwork(UUID id) {
        return networks.get(id);
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldLoad);
    }

    private static void onWorldLoad(final WorldEvent.Load event) {
        networks.clear();
    }

    public static IKineticHandler createHandler(float speedMultiplier, float angleOffset, float inertialMass) {
        return new IKineticHandler() {
            private NetworkReference reference = null;

            @Override
            public float getSpeed() {
                return reference.network.speed * reference.speedMultiplier;
            }

            @Override
            public float getAngle() {
                return ((reference.network.angle * reference.speedMultiplier) + reference.angleOffset) % 360;
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
                float localInertia = reference.network.inertia / reference.speedMultiplier;
                reference.network.speed += force / localInertia;
            }

            @Override
            public float calculateForce(float targetSpeed) {
                float localInertia = reference.network.inertia / reference.speedMultiplier;
                float speedDelta = targetSpeed - reference.network.speed;
                return speedDelta * localInertia;
            }

            @Override
            public void setNetwork(NetworkReference network) {
                reference = network;
            }

            @Override
            public NetworkReference getNetwork() {
                return reference;
            }
        };
    }
}
