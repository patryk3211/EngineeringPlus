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
    private int totalAmount = 0;

    @Override
    public ElementStack insert(ElementStack stack, boolean simulate) {
        if(!simulate) {
            boolean inserted = false;
            for (ElementStack element : elements) {
                if (element.element == stack.element) {
                    // Combine the amounts
                    element.amount += stack.amount;
                    // Mix the temperatures
                    element.temperature = element.temperature / stack.amount + stack.temperature / totalAmount;
                    // Increase total amount
                    totalAmount += stack.amount;

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

        int finalTotalAmount = totalAmount;
        for (ElementStack element : elements) {
            ElementStack extractedStack = new ElementStack(element.element, element.amount / totalAmount * amount, element.temperature);
            finalTotalAmount -= extractedStack.amount;
            if(!simulate) element.amount -= extractedStack.amount;
            extracted.add(extractedStack);
        }
        if(!simulate) totalAmount = finalTotalAmount;

        return extracted;
    }

    @Override
    public int getTotalAmount() {
        return totalAmount;
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
                totalAmount += stack.amount;
                elements.add(stack);
            }
        }
    }
}
