package com.patryk3211.engineeringplus.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PipeValveEntity extends BlockEntity {
    public PipeValveEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.pipe_valve.get(), pos, state);
    }
}
