package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.blockentity.ValveHandleEntity;
import com.patryk3211.engineeringplus.util.KineticUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ValveHandle extends KineticBlock {
    private static final VoxelShape NORTH = Block.box(2, 2, 0, 14, 14, 7);
    private static final VoxelShape SOUTH = Block.box(2, 2, 9, 14, 14, 16);
    private static final VoxelShape WEST = Block.box(0, 2, 2, 7, 14, 14);
    private static final VoxelShape EAST = Block.box(9, 2, 2, 16, 14, 14);
    private static final VoxelShape DOWN = Block.box(2, 0, 2, 14, 7, 14);
    private static final VoxelShape UP = Block.box(2, 9, 2, 14, 16, 14);


    public ValveHandle() {
        super(Properties.of(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        builder.add(ModBlockProperties.MODEL_PART);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.CONSUME;

        BlockEntity entity = level.getBlockEntity(pos);
        if(!(entity instanceof ValveHandleEntity valveHandle)) return InteractionResult.FAIL;

        valveHandle.kineticHandler.ifPresent(handler -> {
            float targetAngle = player.isShiftKeyDown() ? 0f : 90f;
            float currentAngle = handler.getAngle();

            float angleDistance = KineticUtils.signedAngleDistance(currentAngle, targetAngle);
            float force = handler.calculateForce(angleDistance / 360f * 60f * 20f);
            handler.applyForce(Math.max(Math.min(Math.abs(force) / 1000f, 10f), 6f) * Math.signum(force));
        });

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ValveHandleEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos poa, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case DOWN -> DOWN;
            case UP -> UP;
        };
    }
}
