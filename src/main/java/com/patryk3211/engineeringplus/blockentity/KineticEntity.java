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
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public abstract class KineticEntity extends BlockEntity {
    private final Map<IKineticHandler, Set<Direction>> handlerMap = new HashMap<>();

    public KineticEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void init() {
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

                IKineticHandler.NetworkReference neighbourNetwork = neighbourHandler.getNetwork();
                if(neighbourNetwork != null) {
                    if(handler.getNetwork() == null) {
                        // Attach to neighbour's network
                        IKineticHandler.NetworkReference reference = new IKineticHandler.NetworkReference(neighbourNetwork.network, neighbourNetwork.speedMultiplier * handler.getSpeedMultiplier(), neighbourNetwork.angleOffset + handler.getAngleOffset());
                        reference.network.addTile(worldPosition, dir);
                        handler.setNetwork(reference);
                    } else {
                        // Merge our network into the neighbouring one
                        neighbourNetwork.network.addTile(worldPosition, dir);
                        IKineticHandler.NetworkReference oldNetwork = handler.getNetwork();
                        handler.setNetwork(new IKineticHandler.NetworkReference(neighbourNetwork.network, neighbourNetwork.speedMultiplier * handler.getSpeed(), neighbourNetwork.angleOffset * handler.getAngleOffset()));

                        oldNetwork.network.tiles.forEach((position, directions) -> {
                            BlockEntity entity = level.getBlockEntity(position);
                            if(entity == null) return;

                            for (Direction direction : directions) {
                                LazyOptional<IKineticHandler> lazy = entity.getCapability(ModCapabilities.KINETIC, direction);
                                lazy.ifPresent(entityHandler -> {
                                    entityHandler.getNetwork().network = neighbourNetwork.network;
                                    entityHandler.getNetwork().speedMultiplier *= neighbourNetwork.speedMultiplier;
                                    entityHandler.getNetwork().angleOffset += neighbourNetwork.angleOffset;
                                    neighbourNetwork.network.addTile(position, direction);
                                });
                            }
                        });

                        oldNetwork.network.remove();
                    }
                } else checkHandlers.add(handler);
            });
        }

        for (IKineticHandler handler : checkHandlers) {
            if(handler.getNetwork() == null) handler.setNetwork(new IKineticHandler.NetworkReference(new KineticNetwork(UUID.randomUUID()), 1, 0));
            for (Direction direction : handlerMap.get(handler)) {
                handler.getNetwork().network.addTile(worldPosition, direction);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        // Load networks
        CompoundTag networks = tag.getCompound("networks");
        handlerMap.forEach((handler, dirs) -> {
            IKineticHandler.NetworkReference networkReference = handler.getNetwork();

            CompoundTag network = null;
            for (Direction dir : dirs) {
                network = networks.getCompound(Integer.toString(dir.get3DDataValue()));
                if(network.isEmpty()) break;
            }
            // TODO: [05.05.2022] Instead of returning we should create an empty network or something.
            if(network == null) return;

            if(networkReference == null) {
                networkReference = new IKineticHandler.NetworkReference(null, 0, 0);
                handler.setNetwork(networkReference);
            }

            networkReference.network = KineticNetwork.getNetwork(network.getUUID("id"));
            networkReference.speedMultiplier = network.getFloat("speed");
            networkReference.angleOffset = network.getFloat("angle");
        });
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        // Save network IDs and their relations
        CompoundTag networks = new CompoundTag();
        handlerMap.forEach((handler, dirs) -> {
            IKineticHandler.NetworkReference networkReference = handler.getNetwork();

            CompoundTag network = new CompoundTag();
            network.putUUID("id", networkReference.network.getId());
            network.putFloat("speed", networkReference.speedMultiplier);
            network.putFloat("angle", networkReference.angleOffset);

            Direction firstDir = null;
            for (Direction dir : dirs) {
                firstDir = dir;
                break;
            }
            if(firstDir == null) throw new IllegalStateException("How is there a handler with no directions?");

            networks.put(Integer.toString(firstDir.get3DDataValue()), network);
        });
        tag.put("networks", networks);

        super.saveAdditional(tag);
    }
}
