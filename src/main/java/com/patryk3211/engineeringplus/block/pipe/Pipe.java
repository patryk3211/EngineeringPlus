package com.patryk3211.engineeringplus.block.pipe;

import com.patryk3211.engineeringplus.blockentity.PipeEntity;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.util.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class Pipe extends Block implements EntityBlock {
    private static final VoxelShape CENTER = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH = Block.box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH = Block.box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape WEST = Block.box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape EAST = Block.box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape DOWN = Block.box(5, 0, 5, 11, 5, 11);
    private static final VoxelShape UP = Block.box(5, 11, 5, 11, 16, 11);

    public final int flowRate;
    public final int maxPressure;
    public final float volume;

    public Pipe(Properties properties, int flowRate, int maxPressure, float volume) {
        super(properties);

        this.flowRate = flowRate;
        this.maxPressure = maxPressure;
        this.volume = volume;

        registerDefaultState(defaultBlockState()
                .setValue(BlockStateProperties.NORTH, false)
                .setValue(BlockStateProperties.SOUTH, false)
                .setValue(BlockStateProperties.EAST, false)
                .setValue(BlockStateProperties.WEST, false)
                .setValue(BlockStateProperties.DOWN, false)
                .setValue(BlockStateProperties.UP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH);
        builder.add(BlockStateProperties.SOUTH);
        builder.add(BlockStateProperties.EAST);
        builder.add(BlockStateProperties.WEST);
        builder.add(BlockStateProperties.DOWN);
        builder.add(BlockStateProperties.UP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            BlockEntity neighbour = context.getLevel().getBlockEntity(context.getClickedPos().offset(dir.getNormal()));
            if(neighbour == null) {
                state = state.setValue(DirectionUtils.directionToProperty(dir), false);
                continue;
            }

            boolean neighbourConnects = neighbour.getCapability(ModCapabilities.ELEMENT, dir.getOpposite()).isPresent();
            state = state.setValue(DirectionUtils.directionToProperty(dir), neighbourConnects);
        }
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbour, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighbour, isMoving);

        BlockPos posDir = neighbour.subtract(pos);
        Direction dir = Direction.getNearest(posDir.getX(), posDir.getY(), posDir.getZ());

        BlockEntity currentE = level.getBlockEntity(pos);
        BlockEntity neighbourE = level.getBlockEntity(neighbour);

        boolean connectable = currentE != null && neighbourE != null &&
                currentE.getCapability(ModCapabilities.ELEMENT, dir).isPresent() &&
                neighbourE.getCapability(ModCapabilities.ELEMENT, dir.getOpposite()).isPresent();

        BooleanProperty dirProperty = DirectionUtils.directionToProperty(dir);
        if(state.getValue(dirProperty) != connectable)
            level.setBlock(pos, state.setValue(dirProperty, connectable), Block.UPDATE_CLIENTS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER;
        if(state.getValue(BlockStateProperties.NORTH)) shape = Shapes.joinUnoptimized(shape, NORTH, BooleanOp.OR);
        if(state.getValue(BlockStateProperties.SOUTH)) shape = Shapes.joinUnoptimized(shape, SOUTH, BooleanOp.OR);
        if(state.getValue(BlockStateProperties.EAST)) shape = Shapes.joinUnoptimized(shape, EAST, BooleanOp.OR);
        if(state.getValue(BlockStateProperties.WEST)) shape = Shapes.joinUnoptimized(shape, WEST, BooleanOp.OR);
        if(state.getValue(BlockStateProperties.DOWN)) shape = Shapes.joinUnoptimized(shape, DOWN, BooleanOp.OR);
        if(state.getValue(BlockStateProperties.UP)) shape = Shapes.joinUnoptimized(shape, UP, BooleanOp.OR);
        return shape.optimize();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> _type) {
        if(_level.isClientSide) return null;
        else return (level, pos, state, tile) -> ((PipeEntity) tile).tick();
    }
}
