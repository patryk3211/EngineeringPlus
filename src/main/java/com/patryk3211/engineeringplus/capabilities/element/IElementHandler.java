package com.patryk3211.engineeringplus.capabilities.element;

import com.patryk3211.engineeringplus.element.ElementStack;

import java.util.Collection;

public interface IElementHandler {
    /**
     * Insert the given amount of an element into this handler
     * @param stack Element stack to insert
     * @param simulate Should the stack be actually inserted
     * @return The stack that was actually inserted
     */
    ElementStack insert(ElementStack stack, boolean simulate);

    /**
     * Extract the given amount of elements from this handler
     * @param amount Pressure to extract
     * @param simulate Should it actually be extracted
     * @return Collection of element stacks that was extracted
     */
    Collection<ElementStack> extract(int amount, boolean simulate);

    /**
     * Get total pressure of all elements in this handler
     * @return Pressure [Pa]
     */
    int getTotalPressure();
}
