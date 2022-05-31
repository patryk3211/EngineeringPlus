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

public class BasicElementHandler implements IElementHandler, INBTSerializable<CompoundTag> {
    private final List<ElementStack> elements = new ArrayList<>();
    private final float volume;
    private int totalAmount = 0;

    private float temperature = 0;
    private float thermalMass;
    private int totalPressure = 0;
    private boolean psrRecalc = false;

    public BasicElementHandler(float volume, float thermalMass) {
        this.volume = volume;
        this.thermalMass = thermalMass;
    }

    @Override
    public ElementStack insert(ElementStack stack, boolean simulate) {
        if(!simulate) {
            // Increase thermal energy
            if(thermalMass != 0) {
                thermalMass += stack.element.getHeatCapacity() * stack.amount;
                temperature += (stack.temperature - temperature) * stack.amount * stack.element.getHeatCapacity() / thermalMass;
            } else {
                temperature = stack.temperature;
                thermalMass = stack.element.getHeatCapacity() * stack.amount;
            }

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
            element.temperature = temperature;

            // Cannot move a solid.
            if(element.element.getState(element.temperature) == Element.State.SOLID) continue;

            ElementStack extractedStack = new ElementStack(element.element, element.amount / totalAmount * amount, element.temperature);
            if(!simulate) element.amount -= extractedStack.amount;
            finalTotalAmount -= extractedStack.amount;
            extracted.add(extractedStack);

            extractedThermal += extractedStack.amount * extractedStack.element.getHeatCapacity();
        }
        if(!simulate) {
            totalAmount = finalTotalAmount;
            thermalMass -= extractedThermal;
            psrRecalc = true;
        }

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

    public float getTemperature() {
        return temperature;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag el = new ListTag();
        for (ElementStack element : elements) {
            element.temperature = temperature;
            el.add(element.save());
        }

        tag.put("elements", el);
        tag.putFloat("thermal", thermalMass);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        thermalMass = nbt.getFloat("thermal");
        temperature = 0;

        for (Tag tag : nbt.getList("elements", Tag.TAG_COMPOUND)) {
            if(tag instanceof CompoundTag compoundTag) {
                ElementStack stack = new ElementStack(compoundTag);
                totalAmount += stack.amount;
                elements.add(stack);
            }
        }
        if(elements.size() > 0) temperature = elements.get(0).temperature;
    }
}
