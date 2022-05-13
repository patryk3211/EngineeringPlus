package com.patryk3211.engineeringplus.util;

import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.capabilities.element.IElementHandler;
import com.patryk3211.engineeringplus.element.ElementStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Collection;

public class ElementHelper {
    public static void flow(BlockEntity entity, Direction side) {
        entity.getCapability(ModCapabilities.ELEMENT, side).ifPresent(handler -> {
            BlockEntity neighbour = entity.getLevel().getBlockEntity(entity.getBlockPos().offset(side.getNormal()));

            LazyOptional<IElementHandler> lazyHandler;
            if(neighbour == null || !(lazyHandler = neighbour.getCapability(ModCapabilities.ELEMENT, side.getOpposite())).isPresent()) return;

            IElementHandler neighbourHandler = lazyHandler.orElse(null);
            int difference = handler.getTotalAmount() - neighbourHandler.getTotalAmount();
            if(difference <= 0) return;

            int maxMoveAmount = neighbourHandler.canInsert(difference / 2);
            Collection<ElementStack> extractedElements = handler.extract(maxMoveAmount, false);

            for (ElementStack elementStack : extractedElements) neighbourHandler.insert(elementStack, false);

            entity.setChanged();
            neighbour.setChanged();
        });
    }
}
