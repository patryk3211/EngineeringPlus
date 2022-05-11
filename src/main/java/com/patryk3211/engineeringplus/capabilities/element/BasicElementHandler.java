package com.patryk3211.engineeringplus.capabilities.element;

import com.patryk3211.engineeringplus.element.Element;
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
    private final float volume;
    private int totalAmount = 0;
    private float totalThermalEnergy = 0;
    private boolean tempRecalc = false;
    private int totalPressure = 0;
    private boolean psrRecalc = false;

    public BasicElementHandler(float volume) {
        this.volume = volume;
    }

    @Override
    public ElementStack insert(ElementStack stack, boolean simulate) {
        if(!simulate) {
            // Increase thermal energy
            totalThermalEnergy += stack.temperature * stack.amount * stack.element.getHeatCapacity();

            boolean inserted = false;
            for (ElementStack element : elements) {
                if (element.element == stack.element) {
                    // Combine the amounts
                    element.amount += stack.amount;
                    // Increase total amount
                    totalAmount += stack.amount;
                    inserted = true;
                    break;
                }
            }
            if(!inserted) {
                elements.add(stack.copy());
                totalAmount += stack.amount;
            }

            tempRecalc = true;
            psrRecalc = true;
        }
        return stack;
    }

    @Override
    public Collection<ElementStack> extract(int amount, boolean simulate) {
        List<ElementStack> extracted = new LinkedList<>();

        int finalTotalAmount = totalAmount;
        float extractedThermal = 0;
        for (ElementStack element : elements) {
            // Cannot move a solid.
            if(element.element.getState(element.temperature) == Element.State.SOLID) continue;

            // Recalculate temperature from thermal energy
            if(tempRecalc) element.temperature = totalThermalEnergy * element.amount / totalAmount / (element.amount * element.element.getHeatCapacity());

            ElementStack extractedStack = new ElementStack(element.element, element.amount / totalAmount * amount, element.temperature);
            if(!simulate) element.amount -= extractedStack.amount;
            finalTotalAmount -= extractedStack.amount;
            extracted.add(extractedStack);

            extractedThermal += extractedStack.temperature * extractedStack.amount * extractedStack.element.getHeatCapacity();
        }
        if(!simulate) {
            totalAmount = finalTotalAmount;
            totalThermalEnergy -= extractedThermal;
            psrRecalc = true;
        }

        tempRecalc = false;

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
    public int getPressure() {
        if(psrRecalc) {
            totalPressure = 0;
            for (ElementStack element : elements)
                totalPressure += element.element.getPressure(element.amount, volume, element.temperature);
            psrRecalc = false;
        }
        return totalPressure;
    }

    @Override
    public ListTag serializeNBT() {
        ListTag tag = new ListTag();
        for (ElementStack element : elements) {
            if(tempRecalc) element.temperature = totalThermalEnergy * element.amount / totalAmount / (element.amount * element.element.getHeatCapacity());
            tag.add(element.save());
        }
        tempRecalc = false;
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
