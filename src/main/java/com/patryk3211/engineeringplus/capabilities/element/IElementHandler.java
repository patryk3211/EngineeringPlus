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
     * Checks if this handler can receive a given amount of elements
     * @param amount Amount [kg]
     * @return Amount that can be inserted (can be zero)
     */
    int canInsert(int amount);

    /**
     * Extract the given amount of elements from this handler
     * @param amount Amount to extract
     * @param simulate Should it actually be extracted
     * @return Collection of element stacks that was extracted
     */
    Collection<ElementStack> extract(int amount, boolean simulate);

    /**
     * Get total amount of all elements in this handler
     * @return Amount [kg]
     */
    int getTotalAmount();
}
