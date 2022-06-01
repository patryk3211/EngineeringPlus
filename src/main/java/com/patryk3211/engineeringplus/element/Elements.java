package com.patryk3211.engineeringplus.element;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import com.patryk3211.engineeringplus.EngineeringPlusRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Elements {
    public static final DeferredRegister<Element> ELEMENTS = DeferredRegister.create(EngineeringPlusRegistries.ELEMENT_REGISTRY, EngineeringPlusMod.ID);

    public static final RegistryObject<Element> EMPTY = ELEMENTS.register("empty", () -> new Element(Element.Properties.EMPTY, Blocks.AIR, Blocks.AIR, Blocks.AIR));
    public static final RegistryObject<Element> WATER = ELEMENTS.register("water", () -> new Element(Element.Properties.WATER, Blocks.AIR, Blocks.WATER, Blocks.ICE));
}
