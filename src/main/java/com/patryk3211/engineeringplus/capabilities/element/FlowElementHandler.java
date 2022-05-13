package com.patryk3211.engineeringplus.capabilities.element;

import com.patryk3211.engineeringplus.element.ElementStack;

import java.util.Collection;

public class FlowElementHandler extends BasicElementHandler {
    private int flowLeft;

    public FlowElementHandler(float volume) {
        super(volume);

        flowLeft = 0;
    }

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

    public void setFlow(int flow) {
        this.flowLeft = flow;
    }
}
