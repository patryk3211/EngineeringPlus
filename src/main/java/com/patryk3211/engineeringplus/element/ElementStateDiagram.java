package com.patryk3211.engineeringplus.element;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class ElementStateDiagram implements IElementStateProvider {
    private final ResourceLocation location;

    public ElementStateDiagram(ResourceLocation location) {
        this.location = location;

        ElementStateDiagrams.add(this);
    }

    public void load(Function<ResourceLocation, JsonObject> resourceRetriever) {
        JsonObject mainFile = resourceRetriever.apply(location);
    }

    @Override
    public Element.State getState(float temperature, float pressure) {
        return Element.State.LIQUID;
    }
}
