package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public abstract class KineticEntity extends BlockEntity {
    private final Map<IKineticHandler, Set<Direction>> handlerMap = new HashMap<>();

    public KineticEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void init() {
        if(level.isClientSide) return;

        Set<IKineticHandler> checkHandlers = new HashSet<>();

        for(Direction dir : Direction.values()) {
            LazyOptional<IKineticHandler> lazyHandler = getCapability(ModCapabilities.KINETIC, dir);
            lazyHandler.ifPresent(handler -> {
                if(handlerMap.containsKey(handler)) handlerMap.get(handler).add(dir);
                else {
                    Set<Direction> dirs = new HashSet<>();
                    dirs.add(dir);
                    handlerMap.put(handler, dirs);
                }

                // Get neighbour
                BlockEntity neighbour = level.getBlockEntity(worldPosition.offset(dir.getNormal()));

                // Check if they exist
                if(neighbour == null || !neighbour.getCapability(ModCapabilities.KINETIC, dir.getOpposite()).isPresent()) {
                    // Check later if the network was found
                    checkHandlers.add(handler);
                    return;
                }

                LazyOptional<IKineticHandler> neighbourLazyHandler = neighbour.getCapability(ModCapabilities.KINETIC, dir.getOpposite());
                IKineticHandler neighbourHandler = neighbourLazyHandler.orElse(null);

                IKineticHandler.NetworkReference neighbourNetworkReference = neighbourHandler.getNetworkReference();
                if(neighbourNetworkReference != null) {
                    if(handler.getNetworkReference().getNetwork() == null) {
                        // Attach to neighbour's network
                        handler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                        neighbourNetworkReference.getNetwork().addTile(worldPosition, dir);
                        handler.setNetwork(neighbourNetworkReference.getNetwork());
                    } else {
                        // Merge our network into the neighbouring one
                        neighbourNetworkReference.getNetwork().addTile(worldPosition, dir);
                        KineticNetwork oldNetwork = handler.getNetworkReference().getNetwork();
                        handler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                        handler.setNetwork(neighbourNetworkReference.getNetwork());

                        // Add the inertia
                        neighbourNetworkReference.getNetwork().addMass(oldNetwork.getInertia(), oldNetwork.getSpeed());

                        oldNetwork.tiles.forEach((position, directions) -> {
                            BlockEntity entity = level.getBlockEntity(position);
                            if(entity == null) return;

                            for (Direction direction : directions) {
                                LazyOptional<IKineticHandler> lazy = entity.getCapability(ModCapabilities.KINETIC, direction);
                                lazy.ifPresent(entityHandler -> {
                                    entityHandler.setNetwork(neighbourNetworkReference.getNetwork());
                                    entityHandler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                                    neighbourNetworkReference.getNetwork().addTile(position, direction);
                                });
                            }
                        });

                        oldNetwork.remove();
                    }
                    neighbourNetworkReference.getNetwork().addMass(handler.getInertia() / handler.getNetworkReference().speedMultiplier, 0);
                } else checkHandlers.add(handler);
            });
        }

        for (IKineticHandler handler : checkHandlers) {
            if(handler.getNetworkReference().getNetwork() == null) handler.setNetwork(new KineticNetwork(UUID.randomUUID())); //handler.setNetwork(new IKineticHandler.NetworkReference(new KineticNetwork(UUID.randomUUID()), 1, 0));
            for (Direction direction : handlerMap.get(handler))
                handler.getNetworkReference().getNetwork().addTile(worldPosition, direction);
            handler.getNetworkReference().getNetwork().addMass(handler.getInertia() / handler.getNetworkReference().speedMultiplier, 0);
        }
    }

    private void tracePos(BlockPos position, Map<BlockPos, Set<Direction>> tiles, KineticNetwork resultNetwork) {
        Set<Direction> directions = tiles.remove(position);
        if(directions == null) return;

        BlockEntity entity = level.getBlockEntity(position);
        if(entity == null) return;

        for (Direction direction : directions) {
            entity.getCapability(ModCapabilities.KINETIC, direction).ifPresent(handler -> handler.setNetwork(resultNetwork));
            resultNetwork.addTile(position, direction);
            tracePos(position.offset(direction.getNormal()), tiles, resultNetwork);
        }
    }

    @Override
    public void setRemoved() {
        if(level.isClientSide) return;

        Map<KineticNetwork, Set<Direction>> tilePools = new HashMap<>();

        handlerMap.forEach((handler, directions) -> {
            KineticNetwork network = handler.getNetworkReference().getNetwork();

            if(tilePools.containsKey(network)) tilePools.get(network).addAll(directions);
            else {
                tilePools.put(network, Set.copyOf(directions));
                network.tiles.remove(worldPosition);
            }
        });

        tilePools.forEach((network, directions) -> {
            for (Direction direction : directions) {
                BlockPos startPos = worldPosition.offset(direction.getNormal());
                if(!network.tiles.containsKey(startPos)) continue;

                KineticNetwork result = new KineticNetwork(UUID.randomUUID());
                tracePos(startPos, network.tiles, result);

                result.setValues(network.getSpeed(), network.getAngle());
            }
            network.remove();
        });
    }

    public abstract List<LazyOptional<IKineticHandler>> getHandlersToStore();

    @Override
    public void load(CompoundTag tag) {
        // Load networks
        CompoundTag networks = tag.getCompound("networks");

        List<LazyOptional<IKineticHandler>> handlers = getHandlersToStore();
        for (int i = 0; i < handlers.size(); ++i) {
            CompoundTag network = networks.getCompound(Integer.toString(i));

            LazyOptional<IKineticHandler> lazyHandler = handlers.get(i);
            lazyHandler.ifPresent(handler -> {
                handler.setNetwork(KineticNetwork.getNetwork(network.getUUID("id")));
                handler.getNetworkReference().speedMultiplier = network.getFloat("speed");
                handler.getNetworkReference().angleOffset = network.getFloat("angle");
            });
        }
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        // Save network IDs and their relations
        CompoundTag networks = new CompoundTag();

        List<LazyOptional<IKineticHandler>> handlers = getHandlersToStore();
        for (int i = 0; i < handlers.size(); ++i) {
            CompoundTag network = new CompoundTag();

            LazyOptional<IKineticHandler> lazyHandler = handlers.get(i);
            lazyHandler.ifPresent(handler -> {
                network.putUUID("id", handler.getNetworkReference().getNetwork().getId());
                network.putFloat("speed", handler.getNetworkReference().speedMultiplier);
                network.putFloat("angle", handler.getNetworkReference().angleOffset);
            });

            networks.put(Integer.toString(i), network);
        }
        tag.put("networks", networks);

        super.saveAdditional(tag);
    }
}
