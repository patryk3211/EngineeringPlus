package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.block.ModBlockProperties;
import com.patryk3211.engineeringplus.block.pipe.PipeValve;
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

public class PipeValveEntity extends KineticEntity {
    private final LazyOptional<IKineticHandler> kineticHandler = LazyOptional.of(() -> new BasicKineticHandler(1));

    private final int maxFlow;
    private final int maxPressure;

    public PipeValveEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.pipe_valve.get(), pos, state);

        if(!(state.getBlock() instanceof PipeValve valve)) {
            maxFlow = 0;
            maxPressure = 0;
            return;
        }

        maxFlow = valve.maxFlowRate;
        maxPressure = valve.maxPressure;
    }

    public Direction getShaftDirection() {
        BlockState state = getBlockState();
        int rot = state.getValue(ModBlockProperties.ROTATION_4);
        return switch(state.getValue(BlockStateProperties.AXIS)) {
            case X -> switch(rot) {
                case 0 -> Direction.UP;
                case 1 -> Direction.NORTH;
                case 2 -> Direction.DOWN;
                case 3 -> Direction.SOUTH;
                default -> throw new IllegalStateException("Cannot have more than 4 rotations");
            };
            case Y -> switch(rot) {
                case 0 -> Direction.NORTH;
                case 1 -> Direction.EAST;
                case 2 -> Direction.SOUTH;
                case 3 -> Direction.WEST;
                default -> throw new IllegalStateException("Cannot have more than 4 rotations");
            };
            case Z -> switch(rot) {
                case 0 -> Direction.UP;
                case 1 -> Direction.EAST;
                case 2 -> Direction.DOWN;
                case 3 -> Direction.WEST;
                default -> throw new IllegalStateException("Cannot have more than 4 rotations");
            };
        };
    }

    @Override
    public List<LazyOptional<IKineticHandler>> getHandlersToStore() {
        return List.of(kineticHandler);
    }

    @Override
    public List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> getRenderedHandlers() {
        return List.of(Triple.of(kineticHandler, getBlockState().setValue(ModBlockProperties.MODEL_PART, ModBlockProperties.ModelPart.SHAFT), getShaftDirection().getAxis()));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.KINETIC) {
            if(side == getShaftDirection()) return kineticHandler.cast();
            else return LazyOptional.empty();
        } else if(cap == ModCapabilities.ELEMENT) {
            // TODO: Return element handler.
            if(side == null || side.getAxis() == getBlockState().getValue(BlockStateProperties.AXIS)) return LazyOptional.empty();
            else return LazyOptional.empty();
        } else return super.getCapability(cap, side);
    }
}
