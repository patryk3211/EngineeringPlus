package com.patryk3211.engineeringplus;

import com.patryk3211.engineeringplus.element.Element;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class EngineeringPlusRegistries {
    public static final ResourceLocation ELEMENT_REGISTRY = new ResourceLocation(EngineeringPlusMod.ID, "elements");
    public static IForgeRegistry<Element> RESOLVED_ELEMENT_REGISTRY = null;
}
