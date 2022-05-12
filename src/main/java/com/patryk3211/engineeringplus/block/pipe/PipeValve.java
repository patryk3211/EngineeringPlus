package com.patryk3211.engineeringplus.block.pipe;

import com.patryk3211.engineeringplus.block.KineticBlock;
import com.patryk3211.engineeringplus.block.ModBlockProperties;
import com.patryk3211.engineeringplus.blockentity.PipeValveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public abstract class PipeValve extends KineticBlock {
    public final int maxFlowRate;
    public final int maxPressure;
    public final float volume;

    public PipeValve(Properties properties, int maxFlowRate, int maxPressure, float volume) {
        super(properties);

        this.maxFlowRate = maxFlowRate;
        this.maxPressure = maxPressure;
        this.volume = volume;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AXIS);
        builder.add(ModBlockProperties.ROTATION_4);
        builder.add(ModBlockProperties.MODEL_PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getClickedFace().getAxis();
        Direction[] potentialDirections = context.getNearestLookingDirections();

        int rotation = switch(axis) {
            case X -> {
                for (Direction dir : potentialDirections) {
                    switch(dir.getOpposite()) {
                        case UP: yield 0;
                        case NORTH: yield 1;
                        case DOWN: yield 2;
                        case SOUTH: yield 3;
                    }
                }
                yield -1;
            }
            case Y -> {
                for (Direction dir : potentialDirections) {
                    switch(dir.getOpposite()) {
                        case NORTH: yield 0;
                        case EAST: yield 1;
                        case SOUTH: yield 2;
                        case WEST: yield 3;
                    }
                }
                yield -1;
            }
            case Z -> {
                for (Direction dir : potentialDirections) {
                    switch(dir.getOpposite()) {
                        case UP: yield 0;
                        case EAST: yield 1;
                        case DOWN: yield 2;
                        case WEST: yield 3;
                    }
                }
                yield -1;
            }
        };
        return defaultBlockState()
                .setValue(BlockStateProperties.AXIS, axis)
                .setValue(ModBlockProperties.ROTATION_4, rotation)
                .setValue(ModBlockProperties.MODEL_PART, ModBlockProperties.ModelPart.STATIC);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeValveEntity(pos, state);
    }
}
