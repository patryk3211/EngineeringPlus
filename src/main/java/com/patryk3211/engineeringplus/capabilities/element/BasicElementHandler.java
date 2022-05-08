package com.patryk3211.engineeringplus.capabilities.element;

import com.patryk3211.engineeringplus.element.ElementStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BasicElementHandler implements IElementHandler {
    private final List<ElementStack> elements = new ArrayList<>();
    private int totalPressure = 0;

    @Override
    public ElementStack insert(ElementStack stack, boolean simulate) {
        if(!simulate) {
            for (ElementStack element : elements) {
                if (element.element == stack.element) {
                    // Combine the pressures
                    element.pressure += stack.pressure;
                    // Mix the temperatures
                    element.temperature = element.temperature / stack.pressure + stack.temperature / totalPressure;
                    // Increase total pressure
                    totalPressure += stack.pressure;
                }
            }
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
        }
        if(!simulate) totalPressure = finalTotalPressure;

        return extracted;
    }

    @Override
    public int getTotalPressure() {
        return totalPressure;
    }
}
