package com.patryk3211.engineeringplus.capabilities.element;

import com.patryk3211.engineeringplus.element.ElementStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BasicElementHandler implements IElementHandler, INBTSerializable<ListTag> {
    private final List<ElementStack> elements = new ArrayList<>();
    private int totalPressure = 0;

    @Override
    public ElementStack insert(ElementStack stack, boolean simulate) {
        if(!simulate) {
            boolean inserted = false;
            for (ElementStack element : elements) {
                if (element.element == stack.element) {
                    // Combine the pressures
                    element.pressure += stack.pressure;
                    // Mix the temperatures
                    element.temperature = element.temperature / stack.pressure + stack.temperature / totalPressure;
                    // Increase total pressure
                    totalPressure += stack.pressure;

                    inserted = true;
                    break;
                }
            }
            if(!inserted) elements.add(stack.copy());
        }
        return stack;
    }

    @Override
    public Collection<ElementStack> extract(int amount, boolean simulate) {
        List<ElementStack> extracted = new LinkedList<>();

        int finalTotalPressure = totalPressure;
        for (ElementStack element : elements) {
            ElementStack extractedStack = new ElementStack(element.element, element.pressure / totalPressure * amount, element.temperature);
            finalTotalPressure -= extractedStack.pressure;
            if(!simulate) element.pressure -= extractedStack.pressure;
            extracted.add(extractedStack);
        }
        if(!simulate) totalPressure = finalTotalPressure;

        return extracted;
    }

    @Override
    public int getTotalPressure() {
        return totalPressure;
    }

    @Override
    public int canInsert(int amount) {
        return amount;
    }

    @Override
    public ListTag serializeNBT() {
        ListTag tag = new ListTag();
        for (ElementStack element : elements) tag.add(element.save());
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        for (Tag tag : nbt) {
            if(tag instanceof CompoundTag compoundTag) {
                ElementStack stack = new ElementStack(compoundTag);
                totalPressure += stack.pressure;
                elements.add(stack);
            }
        }
    }
}
