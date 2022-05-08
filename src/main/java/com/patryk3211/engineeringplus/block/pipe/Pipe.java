package com.patryk3211.engineeringplus.block.pipe;

import com.patryk3211.engineeringplus.blockentity.PipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public abstract class Pipe extends Block implements EntityBlock {
    public final int flowRate;
    public final int maxPressure;

    public Pipe(Properties properties, int flowRate, int maxPressure) {
        super(properties);

        this.flowRate = flowRate;
        this.maxPressure = maxPressure;
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
