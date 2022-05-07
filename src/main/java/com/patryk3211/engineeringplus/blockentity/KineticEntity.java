package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import com.patryk3211.engineeringplus.kinetic.client.ClientKineticNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public abstract class KineticEntity extends BlockEntity {

    public KineticEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void init() {
        if(level.isClientSide) return;

        //Set<IKineticHandler> checkHandlers = new HashSet<>();
        Map<IKineticHandler, Set<Direction>> checkHandlers = new HashMap<>();

        for(Direction dir : Direction.values()) {
            LazyOptional<IKineticHandler> lazyHandler = getCapability(ModCapabilities.KINETIC, dir);
            lazyHandler.ifPresent(handler -> {
                // Get neighbour
                BlockEntity neighbour = level.getBlockEntity(worldPosition.offset(dir.getNormal()));

                // Check if they exist
                if(neighbour == null || !neighbour.getCapability(ModCapabilities.KINETIC, dir.getOpposite()).isPresent()) {
                    // Check later if the network was found
                    if(checkHandlers.containsKey(handler)) checkHandlers.get(handler).add(dir);
                    else {
                        Set<Direction> dirs = new HashSet<>();
                        dirs.add(dir);
                        checkHandlers.put(handler, dirs);
                    }
                    return;
                }

                LazyOptional<IKineticHandler> neighbourLazyHandler = neighbour.getCapability(ModCapabilities.KINETIC, dir.getOpposite());
                IKineticHandler neighbourHandler = neighbourLazyHandler.orElse(null);

                KineticNetwork neighbourNetwork = (KineticNetwork) neighbourHandler.getNetwork();
                if(handler.getNetwork() == null) {
                    // Attach to neighbour's network
                    handler.setParameters(neighbourHandler.getSpeedMultiplier(), neighbourHandler.getAngleOffset());
                    neighbourNetwork.addTile(worldPosition, dir);
                    handler.setNetwork(neighbourNetwork);
                } else if(handler.getNetwork() == neighbourNetwork) {
                    neighbourNetwork.addTile(worldPosition, dir);
                } else {
                    // Merge our network into the neighbouring one
                    neighbourNetwork.addTile(worldPosition, dir);
                    KineticNetwork oldNetwork = (KineticNetwork) handler.getNetwork();

                    float speedMultiplierChange = neighbourHandler.getSpeedMultiplier() / handler.getSpeedMultiplier();
                    float angleOffsetChange = 0;//handler.getAngleOffset();
                    // We don't need to update parameters of this entity because it belongs to the old network
                    // and we are going to multiply every element of it by speedMultiplierChange and add angleOffsetChange
                    handler.setNetwork(neighbourNetwork);

                    // Add the inertia
                    neighbourNetwork.addMass(Math.abs(oldNetwork.getInertia() * handler.getSpeedMultiplier()), oldNetwork.getSpeed());
                    System.out.println("Merged " + oldNetwork.getInertia() + " inertia into (" + neighbourNetwork.getId() + ") mass " + neighbourNetwork.getInertia());

                    // Merge old network
                    oldNetwork.tiles.forEach((position, directions) -> {
                        BlockEntity entity = level.getBlockEntity(position);
                        if(entity == null) return;

                        Set<IKineticHandler> processedHandlers = new HashSet<>();

                        for (Direction direction : directions) {
                            LazyOptional<IKineticHandler> lazy = entity.getCapability(ModCapabilities.KINETIC, direction);
                            lazy.ifPresent(entityHandler -> {
                                entityHandler.setNetwork(neighbourNetwork);
                                neighbourNetwork.addTile(position, direction);

                                if(!processedHandlers.contains(entityHandler)) {
                                    for (IKineticHandler processedHandler : processedHandlers)
                                        if(processedHandler.areConnected(entityHandler)) return;
                                    entityHandler.offsetParameters(speedMultiplierChange, angleOffsetChange);
                                    processedHandlers.add(entityHandler);
                                }
                            });
                        }
                        entity.setChanged();
                        level.sendBlockUpdated(position, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
                    });

                    oldNetwork.remove();
                }
                neighbourNetwork.addMass(Math.abs(handler.getInertia() / handler.getSpeedMultiplier()), 0);
                System.out.println("Added " + handler.getInertia() + " to network (" + neighbourNetwork.getId() + ") mass " + neighbourNetwork.getInertia());
            });
        }

        for (IKineticHandler handler : checkHandlers.keySet()) {
            KineticNetwork handlerNetwork = (KineticNetwork) handler.getNetwork();
            if(handlerNetwork == null) {
                handlerNetwork = new KineticNetwork(UUID.randomUUID(), level);
                handler.setNetwork(handlerNetwork);
            }
            for (Direction direction : checkHandlers.get(handler)) {
                handlerNetwork.addTile(worldPosition, direction);
                handlerNetwork.addMass(Math.abs(handler.getInertia() / handler.getSpeedMultiplier()), 0);
                System.out.println("Added " + handler.getInertia() + " to a network (" + handlerNetwork.getId() + "), current mass " + handlerNetwork.getInertia());
            }
        }

        setChanged();
    }

    private void tracePos(BlockPos position, Map<BlockPos, Set<Direction>> tiles, KineticNetwork resultNetwork) {
        Set<Direction> directions = tiles.remove(position);
        if(directions == null) return;

        BlockEntity entity = level.getBlockEntity(position);
        if(entity == null) return;

        for (Direction direction : directions) {
            entity.getCapability(ModCapabilities.KINETIC, direction).ifPresent(handler -> {
                handler.setNetwork(resultNetwork);
                resultNetwork.addMass(Math.abs(handler.getInertia() / handler.getSpeedMultiplier()), 0);
            });
            resultNetwork.addTile(position, direction);
            tracePos(position.offset(direction.getNormal()), tiles, resultNetwork);
        }
        entity.setChanged();
        level.sendBlockUpdated(position, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void delete() {
        if(level.isClientSide) return;

        Map<KineticNetwork, Set<Direction>> tilePools = new HashMap<>();

        for (Direction direction : Direction.values()) {
            getCapability(ModCapabilities.KINETIC, direction).ifPresent(handler -> {
                KineticNetwork network = (KineticNetwork) handler.getNetwork();

                if(tilePools.containsKey(network)) tilePools.get(network).add(direction);
                else {
                    Set<Direction> dirs = new HashSet<>();
                    dirs.add(direction);
                    tilePools.put(network, dirs);
                    network.tiles.remove(worldPosition);
                }
            });
        }

        tilePools.forEach((network, directions) -> {
            for (Direction direction : directions) {
                BlockPos startPos = worldPosition.offset(direction.getNormal());
                if(!network.tiles.containsKey(startPos)) continue;

                KineticNetwork result = new KineticNetwork(UUID.randomUUID(), level);
                tracePos(startPos, network.tiles, result);

                System.out.println("Traced Network (" + result.getId() + ") mass " + result.getInertia());

                result.setValues(network.getSpeed(), network.getAngle());
                result.syncValues();
            }
            network.remove();
        });
    }

    public abstract List<LazyOptional<IKineticHandler>> getHandlersToStore();
    @OnlyIn(Dist.CLIENT)
    public abstract List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers();

    private Map<IKineticHandler, Set<Direction>> handlerMap() {
        Map<IKineticHandler, Set<Direction>> map = new HashMap<>();

        for (Direction dir : Direction.values()) {
            getCapability(ModCapabilities.KINETIC, dir).ifPresent(handler -> {
                if(map.containsKey(handler)) map.get(handler).add(dir);
                else {
                    Set<Direction> dirs = new HashSet<>();
                    dirs.add(dir);
                    map.put(handler, dirs);
                }
            });
        }

        return map;
    }

    private void handleNetworkTag(CompoundTag networks, Function<UUID, IKineticNetwork> networkGetter) {
        Map<IKineticHandler, Set<Direction>> handlerMap = this.handlerMap();

        List<LazyOptional<IKineticHandler>> handlers = getHandlersToStore();
        for (int i = 0; i < handlers.size(); ++i) {
            CompoundTag network = networks.getCompound(Integer.toString(i));
            if(network.isEmpty()) continue;

            LazyOptional<IKineticHandler> lazyHandler = handlers.get(i);
            lazyHandler.ifPresent(handler -> {
                IKineticNetwork newNet = networkGetter.apply(network.getUUID("id"));
                if(newNet instanceof KineticNetwork knet) {
                    for (Direction direction : handlerMap.get(handler)) {
                        knet.addTile(worldPosition, direction);
                    }
                }
                handler.setNetwork(newNet);
                handler.setParameters(network.getFloat("speed"), network.getFloat("angle"));
            });
        }
    }

    private CompoundTag createNetworkTag() {
        CompoundTag networks = new CompoundTag();

        List<LazyOptional<IKineticHandler>> handlers = getHandlersToStore();
        for (int i = 0; i < handlers.size(); ++i) {
            CompoundTag network = new CompoundTag();

            LazyOptional<IKineticHandler> lazyHandler = handlers.get(i);
            lazyHandler.ifPresent(handler -> {
                if(handler.getNetwork() == null) return;
                network.putUUID("id", handler.getNetwork().getId());
                network.putFloat("speed", handler.getSpeedMultiplier());
                network.putFloat("angle", handler.getAngleOffset());
            });

            networks.put(Integer.toString(i), network);
        }

        return networks;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // Load networks
        CompoundTag networks = tag.getCompound("networks");
        if(level != null && level.isClientSide) handleNetworkTag(networks, ClientKineticNetwork::getNetwork);
        else handleNetworkTag(networks, id -> KineticNetwork.getNetwork(level, id));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        // Save network IDs and their relations
        tag.put("networks", createNetworkTag());

        super.saveAdditional(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag root = new CompoundTag();
        root.put("networks", createNetworkTag());

        return root;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if(pkt.getTag() == null) return;

        CompoundTag networks = pkt.getTag().getCompound("networks");
        handleNetworkTag(networks, ClientKineticNetwork::getNetwork);
    }
}
