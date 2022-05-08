package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.block.pipe.Pipe;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.element.BasicElementHandler;
import com.patryk3211.engineeringplus.capabilities.element.IElementHandler;
import com.patryk3211.engineeringplus.element.ElementStack;
import com.patryk3211.engineeringplus.util.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PipeEntity extends BlockEntity {
    private final LazyOptional<IElementHandler> elementHandler;
    private int flowLeft;
    private final int flowPerTick;
    private final int maxPressure;

    public PipeEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.pipe.get(), pos, state);

        if(!(state.getBlock() instanceof Pipe pipe)) {
            elementHandler = LazyOptional.empty();
            flowPerTick = 0;
            maxPressure = 0;
            return;
        }

        flowPerTick = pipe.flowRate;
        maxPressure = pipe.maxPressure;
        flowLeft = flowPerTick;

        elementHandler = LazyOptional.of(() -> new BasicElementHandler() {
            @Override
            public ElementStack insert(ElementStack stack, boolean simulate) {
                ElementStack inserted = super.insert(new ElementStack(stack.element, Math.min(flowLeft, stack.pressure), stack.temperature), simulate);
                if(!simulate) flowLeft -= inserted.pressure;
                return inserted;
            }
        });
    }

    public void tick() {

    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.ELEMENT) {
            if(getBlockState().getValue(DirectionUtils.directionToProperty(side))) return elementHandler.cast();
            else return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }
}
