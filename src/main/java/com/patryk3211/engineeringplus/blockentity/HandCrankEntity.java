package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.block.ModBlockProperties;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.kinetic.BasicKineticHandler;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HandCrankEntity extends KineticEntity {
    private final LazyOptional<IKineticHandler> kineticHandler = LazyOptional.of(() -> new BasicKineticHandler(0.5f, 1f));

    public HandCrankEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.handCrank.get(), pos, state);
    }

    public void tick() {
        IKineticHandler handler = kineticHandler.orElse(null);
        if(handler.getSpeed() < 360f) handler.applyForce(10);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.KINETIC) {
            if(side == getBlockState().getValue(BlockStateProperties.FACING)) return kineticHandler.cast();
            else return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public List<LazyOptional<IKineticHandler>> getHandlersToStore() {
        return List.of(kineticHandler);
    }

    @Override
    public List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers() {
        BlockState state = getBlockState();
        return List.of(Triple.of(kineticHandler, state.setValue(ModBlockProperties.MODEL_PART, ModBlockProperties.ModelPart.SHAFT), state.getValue(BlockStateProperties.FACING).getAxis()));
    }
}
