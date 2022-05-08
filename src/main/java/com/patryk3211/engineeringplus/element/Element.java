package com.patryk3211.engineeringplus.element;

import com.patryk3211.engineeringplus.EngineeringPlusRegistries;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class Element extends ForgeRegistryEntry<Element> {
    public enum State { GAS, LIQUID, SOLID }

    private final Properties properties;

    private final Block gasBlock;
    private final Block liquidBlock;
    private final Block solidBlock;

    private String descriptionId;

    public static Element fromLocation(ResourceLocation location) {
        return EngineeringPlusRegistries.RESOLVED_ELEMENT_REGISTRY.getValue(location);
    }

    public Element(Properties properties, Block gasBlock, Block liquidBlock, Block solidBlock) {
        this.properties = properties;
        this.gasBlock = gasBlock;
        this.liquidBlock = liquidBlock;
        this.solidBlock = solidBlock;
    }

    public State getState(float temperature, float pressure) {
        return properties.getState(temperature, pressure);
    }

    public String getDescriptionId() {
        if(descriptionId == null)
            descriptionId = Util.makeDescriptionId("element", getRegistryName());
        return descriptionId;
    }

    public static class Properties {
        public static final Properties EMPTY = new Properties((temp, psr) -> State.GAS);
        public static final Properties WATER = new Properties((temp, psr) -> {
            if(temp < 73 || (psr / (temp - 73)) > 500) return State.SOLID;
            else if((temp * 1000) / psr >= 3.73) return State.GAS;
            else return State.LIQUID;
        });

        private final IElementStateProvider stateProvider;

        public Properties(IElementStateProvider stateProvider) {
            this.stateProvider = stateProvider;
        }

        public State getState(float temperature, float pressure) {
            return stateProvider.getState(temperature, pressure);
        }

        public static Properties of(Properties properties) {
            return new Properties(properties.stateProvider);
        }
    }
}
