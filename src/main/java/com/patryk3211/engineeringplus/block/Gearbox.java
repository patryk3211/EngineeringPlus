package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.blockentity.GearboxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class Gearbox extends KineticBlock {
    public Gearbox() {
        super(Properties.of(Material.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GearboxEntity(pos, state);
    }
}
