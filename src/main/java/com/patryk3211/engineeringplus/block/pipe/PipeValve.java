package com.patryk3211.engineeringplus.block.pipe;

import com.patryk3211.engineeringplus.block.KineticBlock;
import com.patryk3211.engineeringplus.block.ModBlockProperties;
import com.patryk3211.engineeringplus.blockentity.PipeValveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class PipeValve extends KineticBlock {
    private static final VoxelShape MAIN_X1 = Shapes.or(Block.box(0, 5, 5, 3, 11, 11),
            Block.box(3, 4, 3, 13, 12, 13),
            Block.box(13, 5, 5, 16, 11, 11));
    private static final VoxelShape MAIN_X2 = Shapes.or(Block.box(0, 5, 5, 3, 11, 11),
            Block.box(3, 3, 4, 13, 13, 12),
            Block.box(13, 5, 5, 16, 11, 11));
    private static final VoxelShape MAIN_Y1 = Shapes.or(Block.box(5, 0, 5, 11, 3, 11),
            Block.box(3, 3, 4, 13, 13, 12),
            Block.box(5, 13, 5, 11, 16, 11));
    private static final VoxelShape MAIN_Y2 = Shapes.or(Block.box(5, 0, 5, 11, 3, 11),
            Block.box(4, 3, 3, 12, 13, 13),
            Block.box(5, 13, 5, 11, 16, 11));
    private static final VoxelShape MAIN_Z1 = Shapes.or(Block.box(5, 5, 0, 11, 11, 3),
            Block.box(3, 4, 3, 13, 12, 13),
            Block.box(5, 5, 13, 11, 11, 16));
    private static final VoxelShape MAIN_Z2 = Shapes.or(Block.box(5, 5, 0, 11, 11, 3),
            Block.box(4, 3, 3, 12, 13, 13),
            Block.box(5, 5, 13, 11, 11, 16));

    private static final VoxelShape SHAFT_N = Block.box(5.5, 5.5, 0, 10.5, 10.5, 4);
    private static final VoxelShape SHAFT_S = Block.box(5.5, 5.5, 12, 10.5, 10.5, 16);
    private static final VoxelShape SHAFT_W = Block.box(0, 5.5, 5.5, 4, 10.5, 10.5);
    private static final VoxelShape SHAFT_E = Block.box(12, 5.5, 5.5, 16, 10.5, 10.5);
    private static final VoxelShape SHAFT_D = Block.box(5.5, 0, 5.5, 10.5, 4, 10.5);
    private static final VoxelShape SHAFT_U = Block.box(5.5, 12, 5.5, 10.5, 16, 10.5);

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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        int rotation = state.getValue(ModBlockProperties.ROTATION_4);

        VoxelShape mainPart = switch(axis) {
            case X -> switch(rotation) {
                case 0, 2 -> MAIN_X1;
                case 1, 3 -> MAIN_X2;
                default -> Shapes.block();
            };
            case Y -> switch(rotation) {
                case 0, 2 -> MAIN_Y1;
                case 1, 3 -> MAIN_Y2;
                default -> Shapes.block();
            };
            case Z -> switch(rotation) {
                case 0, 2 -> MAIN_Z1;
                case 1, 3 -> MAIN_Z2;
                default -> Shapes.block();
            };
        };
        VoxelShape shaftPart = switch(axis) {
            case X -> switch(rotation) {
                case 0 -> SHAFT_U;
                case 1 -> SHAFT_N;
                case 2 -> SHAFT_D;
                case 3 -> SHAFT_S;
                default -> Shapes.block();
            };
            case Y -> switch(rotation) {
                case 0 -> SHAFT_N;
                case 1 -> SHAFT_E;
                case 2 -> SHAFT_S;
                case 3 -> SHAFT_W;
                default -> Shapes.block();
            };
            case Z -> switch(rotation) {
                case 0 -> SHAFT_U;
                case 1 -> SHAFT_E;
                case 2 -> SHAFT_D;
                case 3 -> SHAFT_W;
                default -> Shapes.block();
            };
        };
        return Shapes.or(mainPart, shaftPart);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeValveEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> _type) {
        if(_level.isClientSide) return null;
        else return (level, pos, state, entity) -> ((PipeValveEntity) entity).tick();
    }
}
