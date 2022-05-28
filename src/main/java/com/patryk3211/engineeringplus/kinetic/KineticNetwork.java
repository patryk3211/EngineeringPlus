package com.patryk3211.engineeringplus.kinetic;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import com.patryk3211.engineeringplus.network.KineticNetworkPacket;
import com.patryk3211.engineeringplus.network.PacketHandler;
import com.patryk3211.engineeringplus.util.LevelDataStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class KineticNetwork implements IKineticNetwork {
    private static final Map<ResourceKey<Level>, Map<UUID, KineticNetwork>> networksByDims = new HashMap<>();

    public final Map<BlockPos, Set<Direction>> tiles = new HashMap<>();

    private final UUID id;
    private final Level level;

    private float speed;
    private float angle;
    private float inertia;
    private float totalFriction;

    public KineticNetwork(UUID id, Level level) {
        this.id = id;
        this.level = level;

        // Put this network into the correct map
        if(networksByDims.containsKey(level.dimension())) networksByDims.get(level.dimension()).put(id, this);
        else {
            Map<UUID, KineticNetwork> networks = new HashMap<>();
            networks.put(id, this);
            networksByDims.put(level.dimension(), networks);
        }

        PacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new KineticNetworkPacket(id, KineticNetworkPacket.Type.CREATE_NETWORK));
    }

    public void remove() {
        networksByDims.get(level.dimension()).remove(id);
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

    public float getFriction() {
        return totalFriction;
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

    public void addFriction(float friction) {
        totalFriction += friction;
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
    public static KineticNetwork getNetwork(Level level, UUID id) {
        if(level != null) {
            // Get network in the provided dimension
            Map<UUID, KineticNetwork> levelNetworks = networksByDims.get(level.dimension());
            if (levelNetworks == null) return null;
            else return levelNetworks.get(id);
        } else {
            // If no dimension was provided we check all of them
            for (Map<UUID, KineticNetwork> dimNets : networksByDims.values()) {
                KineticNetwork net = dimNets.get(id);
                if(net != null) return net;
            }
            return null;
        }
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldSave);
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onPlayerJoin);
        MinecraftForge.EVENT_BUS.addListener(KineticNetwork::onPlayerChangeDimension);
    }

    private static void onWorldLoad(final WorldEvent.Load event) {
        if(event.getWorld().isClientSide() || !(event.getWorld() instanceof ServerLevel level)) return;

        // Remove all networks from dimension
        Map<UUID, KineticNetwork> networksInDim = networksByDims.get(level.dimension());
        if(networksInDim != null) networksInDim.clear();
        else {
            networksInDim = new HashMap<>();
            networksByDims.put(level.dimension(), networksInDim);
        }

        LevelDataStorage storage = LevelDataStorage.of(level);

        Tag networksTag = storage.tag.get("knets");
        if(!(networksTag instanceof ListTag networks)) {
            if(networksTag != null) EngineeringPlusMod.LOGGER.error("Kinetic networks tag is not a list.");
            return;
        }

        for (Tag networkTag : networks) {
            if(networkTag instanceof CompoundTag network) {
                KineticNetwork knet = new KineticNetwork(network.getUUID("id"), level);
                knet.speed = network.getFloat("speed");
                knet.angle = network.getFloat("angle");
                knet.inertia = network.getFloat("mass");
                knet.totalFriction = network.getFloat("friction");
            } else EngineeringPlusMod.LOGGER.warn("Kinetic networks entry not a CompoundTag, skipping.");
        }
    }

    public static void onWorldSave(final WorldEvent.Save event) {
        if(event.getWorld().isClientSide() || !(event.getWorld() instanceof ServerLevel level)) return;

        Map<UUID, KineticNetwork> networksInDim;

        LevelDataStorage storage = LevelDataStorage.of(level);
        if(!networksByDims.containsKey(level.dimension()) ||
                (networksInDim = networksByDims.get(level.dimension())).isEmpty()) {
            // There are no networks in this dimension
            storage.tag.remove("knets");
            storage.setDirty();
            return;
        }

        ListTag networks = new ListTag();
        for (KineticNetwork network : networksInDim.values()) {
            CompoundTag netTag = new CompoundTag();
            netTag.putUUID("id", network.id);
            netTag.putFloat("speed", network.speed);
            netTag.putFloat("angle", network.angle);
            netTag.putFloat("mass", network.inertia);
            netTag.putFloat("friction", network.totalFriction);

            networks.add(netTag);
        }

        storage.tag.put("knets", networks);
        storage.setDirty();
    }

    private static void syncNetworks(ServerPlayer player, ResourceKey<Level> dimension) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new KineticNetworkPacket(KineticNetworkPacket.Type.DELETE_ALL));

        Map<UUID, KineticNetwork> networksInDim = networksByDims.get(dimension);
        if(networksInDim == null || networksInDim.isEmpty()) return;

        List<KineticNetworkPacket.Network> networks = new LinkedList<>();
        for (KineticNetwork network : networksInDim.values()) {
            KineticNetworkPacket.Network net = new KineticNetworkPacket.Network(network.id, network.speed, network.angle);
            networks.add(net);
        }
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new KineticNetworkPacket(KineticNetworkPacket.Type.NETWORKS, networks));
    }

    private static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer player)) return;
        syncNetworks(player, player.level.dimension());
    }

    private static void onPlayerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer player)) return;
        syncNetworks(player, event.getTo());
    }

    static int tickCount = 0;
    private static void onWorldTick(final TickEvent.WorldTickEvent event) {
        Map<UUID, KineticNetwork> networks = networksByDims.get(event.world.dimension());
        if(networks == null) return;

        for (KineticNetwork network : networks.values()) {
            // Apply friction
            // If current network speed is smaller than the speed change from applied friction, stop the network
            float appliedFriction = network.totalFriction / network.inertia * 0.05f;
            if(Math.abs(network.speed) < appliedFriction) network.speed = 0;
            else network.speed -= Math.signum(network.speed) * appliedFriction;

            network.angle = (network.angle + network.speed * 0.05f / 60f * 360f) % 360f;
        }
        if(++tickCount >= 20) {
            for (KineticNetwork network : networks.values()) network.syncValues();
            tickCount = 0;
        }
    }
}
