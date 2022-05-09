package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.block.pipe.Pipe;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.element.BasicElementHandler;
import com.patryk3211.engineeringplus.capabilities.element.IElementHandler;
import com.patryk3211.engineeringplus.element.ElementStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PipeEntity extends BlockEntity {
    private final LazyOptional<BasicElementHandler> elementHandler;
    private int flowLeft;
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

        elementHandler = LazyOptional.of(() -> new BasicElementHandler(pipe.volume) {
            @Override
            public ElementStack insert(ElementStack stack, boolean simulate) {
                ElementStack inserted = super.insert(new ElementStack(stack.element, Math.min(flowLeft, stack.amount), stack.temperature), simulate);
                if(!simulate) flowLeft -= inserted.amount;
                return inserted;
            }

            @Override
            public Collection<ElementStack> extract(int amount, boolean simulate) {
                Collection<ElementStack> extracted = super.extract(Math.min(flowLeft, amount), simulate);
                if(!simulate) extracted.forEach(stack -> flowLeft -= stack.amount);
                return extracted;
            }

            @Override
            public int canInsert(int amount) {
                return Math.min(flowLeft, amount);
            }
        });

        connectable = 0b00111111;
    }

    public void tick() {
        flowLeft = flowPerTick;
        // This makes sure that every direction gets treated equally
        int started = nextProcessedDirection;
        do {
            Direction dir = Direction.from3DDataValue(nextProcessedDirection);
            getCapability(ModCapabilities.ELEMENT, dir).ifPresent(handler -> {
                BlockEntity neighbour = level.getBlockEntity(worldPosition.offset(dir.getNormal()));

                LazyOptional<IElementHandler> lazyHandler;
                if(neighbour == null || !(lazyHandler = neighbour.getCapability(ModCapabilities.ELEMENT, dir.getOpposite())).isPresent()) return;

                IElementHandler neighbourHandler = lazyHandler.orElse(null);
                int difference = handler.getTotalAmount() - neighbourHandler.getTotalAmount();
                if(difference <= 0) return;

                int maxMoveAmount = neighbourHandler.canInsert(difference / 2);
                Collection<ElementStack> extractedElements = handler.extract(maxMoveAmount, false);

                for (ElementStack elementStack : extractedElements) neighbourHandler.insert(elementStack, false);
            });
            if(flowLeft == 0) break;
        } while((nextProcessedDirection = (nextProcessedDirection + 1) % 6) != started);
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

        elementHandler.ifPresent(handler -> handler.deserializeNBT(tag.getList("elements", Tag.TAG_COMPOUND)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        elementHandler.ifPresent(handler -> tag.put("elements", handler.serializeNBT()));
    }
}
