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

public class ValveHandleEntity extends KineticEntity {
    public final LazyOptional<IKineticHandler> kineticHandler = LazyOptional.of(() -> new BasicKineticHandler(0.5f, 0.3f));

    public ValveHandleEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.valveHandle.get(), pos, state);
    }

    @Override
    public List<LazyOptional<IKineticHandler>> getHandlersToStore() {
        return List.of(kineticHandler);
    }

    @Override
    public List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers() {
        return List.of(Triple.of(kineticHandler,
                getBlockState().setValue(ModBlockProperties.MODEL_PART, ModBlockProperties.ModelPart.SHAFT),
                getBlockState().getValue(BlockStateProperties.FACING).getAxis()));
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
}
