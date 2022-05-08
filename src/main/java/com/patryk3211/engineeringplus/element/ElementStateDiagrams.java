package com.patryk3211.engineeringplus.element;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ElementStateDiagrams extends SimpleJsonResourceReloadListener {
    private static final Gson gson = new Gson();
    private static final Set<ElementStateDiagram> diagrams = new HashSet<>();

    public ElementStateDiagrams() {
        super(gson, "element_states");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        diagrams.forEach(diagram -> diagram.load(location -> (JsonObject) files.get(location)));
    }

    public static void add(ElementStateDiagram diagram) {
        diagrams.add(diagram);
    }
}
