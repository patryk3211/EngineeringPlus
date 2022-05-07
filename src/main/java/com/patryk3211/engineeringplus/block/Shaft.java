package com.patryk3211.engineeringplus.block;


import com.google.common.collect.ImmutableMap;
import com.patryk3211.engineeringplus.blockentity.ShaftEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Shaft extends KineticBlock {
    private static final VoxelShape Z = Block.box(5.5, 5.5, 0, 10.5, 10.5, 16);
    private static final VoxelShape Y = Block.box(5.5, 0, 5.5, 10.5, 16, 10.5);
    private static final VoxelShape X = Block.box(0, 5.5, 5.5, 16, 10.5, 10.5);

    public Shaft() {
        super(Properties.of(Material.STONE));
        registerDefaultState(defaultBlockState().setValue(ModBlockProperties.MODEL_PART, ModBlockProperties.ModelPart.STATIC));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AXIS);
        builder.add(ModBlockProperties.MODEL_PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.AXIS, context.getClickedFace().getAxis());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShaftEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext collisionContext) {
        return switch(state.getValue(BlockStateProperties.AXIS)) {
            case X -> X;
            case Y -> Y;
            case Z -> Z;
        };
    }
}
