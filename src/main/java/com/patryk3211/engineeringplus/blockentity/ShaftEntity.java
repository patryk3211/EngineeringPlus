package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShaftEntity extends KineticEntity {
    private final LazyOptional<IKineticHandler> kineticHandler = LazyOptional.of(() -> KineticNetwork.createHandler(1, 0, 0.5f));

    public ShaftEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.shaft.get(), pos, state);
    }

    @Override
    public void init() {
        EngineeringPlusMod.LOGGER.info("Placed Shaft Entity");
        super.init();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.KINETIC) {
            if(side != null && side.getAxis() == getBlockState().getValue(BlockStateProperties.AXIS))
                return kineticHandler.cast();
            else return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }
}
