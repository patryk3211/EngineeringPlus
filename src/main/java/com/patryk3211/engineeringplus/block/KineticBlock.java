package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.blockentity.KineticEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticBlock extends Block implements EntityBlock {
    public KineticBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof KineticEntity) ((KineticEntity) entity).init();
    }
}
