package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.kinetic.EntangledKineticHandler;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GearboxEntity extends KineticEntity {
    private final EntangledKineticHandler.DataStore dataStore = new EntangledKineticHandler.DataStore();
    private final LazyOptional<IKineticHandler> handler1 = LazyOptional.of(() -> new EntangledKineticHandler(dataStore, 1, 0, 1));
    private final LazyOptional<IKineticHandler> handler2 = LazyOptional.of(() -> new EntangledKineticHandler(dataStore, -1, 0, 1));

    public GearboxEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.gearbox.get(), pos, state);
    }

    @Override
    public List<LazyOptional<IKineticHandler>> getHandlersToStore() {
        return List.of(handler1, handler2);
    }

    @Override
    public List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers() {
        return List.of();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.KINETIC) {
            if(side == Direction.NORTH) return handler1.cast();
            else if(side == Direction.SOUTH) return handler2.cast();
            else return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }
}
