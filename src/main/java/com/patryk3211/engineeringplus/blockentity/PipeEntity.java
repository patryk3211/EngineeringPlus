package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.block.pipe.Pipe;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.element.FlowElementHandler;
import com.patryk3211.engineeringplus.util.ElementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PipeEntity extends BlockEntity {
    private final LazyOptional<FlowElementHandler> elementHandler;
    private final int flowPerTick;
    private final int maxPressure;
    private int nextProcessedDirection;
    private byte connectable;

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

        elementHandler = LazyOptional.of(() -> new FlowElementHandler(pipe.volume, pipe.thermalMass));

        connectable = 0b00111111;
    }

    public void tick() {
        elementHandler.ifPresent(handler -> {
            handler.setFlow(flowPerTick);
            // This makes sure that every direction gets treated equally
            int started = nextProcessedDirection;
            do {
                Direction dir = Direction.from3DDataValue(nextProcessedDirection);
                ElementHelper.flow(this, dir);
            } while((nextProcessedDirection = (nextProcessedDirection + 1) % 6) != started);
        });
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ModCapabilities.ELEMENT) {
            if(side == null || ((connectable >> side.get3DDataValue()) & 1) == 1) return elementHandler.cast();
            else return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        elementHandler.ifPresent(handler -> handler.deserializeNBT(tag.getCompound("element_handler")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        elementHandler.ifPresent(handler -> tag.put("element_handler", handler.serializeNBT()));
    }
}
