package com.patryk3211.engineeringplus.block.pipe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class Pipe extends Block {
    public Pipe(Properties properties) {
        super(properties);
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


}
