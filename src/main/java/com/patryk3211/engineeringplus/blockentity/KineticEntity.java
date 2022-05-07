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
                    KineticNetwork neighbourNetwork = (KineticNetwork) neighbourNetworkReference.getNetwork();
                    if(handler.getNetworkReference().getNetwork() == null) {
                        // Attach to neighbour's network
                        handler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                        neighbourNetwork.addTile(worldPosition, dir);
                        handler.setNetwork(neighbourNetwork);
                    } else {
                        // Merge our network into the neighbouring one
                        neighbourNetwork.addTile(worldPosition, dir);
                        KineticNetwork oldNetwork = (KineticNetwork) handler.getNetworkReference().getNetwork();
                        handler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                        handler.setNetwork(neighbourNetwork);

                        // Add the inertia
                        neighbourNetwork.addMass(oldNetwork.getInertia(), oldNetwork.getSpeed());

                        // Merge old network
                        oldNetwork.tiles.forEach((position, directions) -> {
                            BlockEntity entity = level.getBlockEntity(position);
                            if(entity == null) return;

                            for (Direction direction : directions) {
                                LazyOptional<IKineticHandler> lazy = entity.getCapability(ModCapabilities.KINETIC, direction);
                                lazy.ifPresent(entityHandler -> {
                                    entityHandler.setNetwork(neighbourNetwork);
                                    entityHandler.getNetworkReference().offset(neighbourNetworkReference.speedMultiplier, neighbourNetworkReference.angleOffset);
                                    neighbourNetwork.addTile(position, direction);
                                });
                            }
                            entity.setChanged();
                            level.sendBlockUpdated(position, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
                        });

                        oldNetwork.remove();
                    }
                    neighbourNetwork.addMass(handler.getInertia() / handler.getNetworkReference().speedMultiplier, 0);
                } else checkHandlers.add(handler);
            });
        }

        for (IKineticHandler handler : checkHandlers) {
            KineticNetwork handlerNetwork = (KineticNetwork) handler.getNetworkReference().getNetwork();
            if(handlerNetwork == null) {
                handlerNetwork = new KineticNetwork(UUID.randomUUID(), level);
                handler.setNetwork(handlerNetwork);
            }
            for (Direction direction : handlerMap.get(handler))
                handlerNetwork.addTile(worldPosition, direction);
            handlerNetwork.addMass(handler.getInertia() / handler.getNetworkReference().speedMultiplier, 0);
        }

        setChanged();
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
        entity.setChanged();
        level.sendBlockUpdated(position, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void setRemoved() {
        if(level.isClientSide) return;

        Map<KineticNetwork, Set<Direction>> tilePools = new HashMap<>();

        handlerMap.forEach((handler, directions) -> {
            KineticNetwork network = (KineticNetwork) handler.getNetworkReference().getNetwork();

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

                KineticNetwork result = new KineticNetwork(UUID.randomUUID(), level);
                tracePos(startPos, network.tiles, result);

                result.setValues(network.getSpeed(), network.getAngle());
                result.syncValues();
            }
            network.remove();
        });
    }

    public abstract List<LazyOptional<IKineticHandler>> getHandlersToStore();
    @OnlyIn(Dist.CLIENT)
    public abstract List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers();

    private IKineticNetwork getSidedNetwork(UUID id) {
        return level.isClientSide ? ClientKineticNetwork.getNetwork(id) : KineticNetwork.getNetwork(id);
    }

    private void handleNetworkTag(CompoundTag networks) {
        List<LazyOptional<IKineticHandler>> handlers = getHandlersToStore();
        for (int i = 0; i < handlers.size(); ++i) {
            CompoundTag network = networks.getCompound(Integer.toString(i));
            if(network.isEmpty()) continue;

            LazyOptional<IKineticHandler> lazyHandler = handlers.get(i);
            lazyHandler.ifPresent(handler -> {
                handler.setNetwork(getSidedNetwork(network.getUUID("id")));
                handler.getNetworkReference().speedMultiplier = network.getFloat("speed");
                handler.getNetworkReference().angleOffset = network.getFloat("angle");
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
                if(handler.getNetworkReference().getNetwork() == null) return;
                network.putUUID("id", handler.getNetworkReference().getNetwork().getId());
                network.putFloat("speed", handler.getNetworkReference().speedMultiplier);
                network.putFloat("angle", handler.getNetworkReference().angleOffset);
            });

            networks.put(Integer.toString(i), network);
        }

        return networks;
    }

    @Override
    public void load(CompoundTag tag) {
        // Load networks
        CompoundTag networks = tag.getCompound("networks");
        handleNetworkTag(networks);

        super.load(tag);
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
        handleNetworkTag(networks);
    }
}
