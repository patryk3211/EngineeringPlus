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

    public State getState(float temperature) {
        return properties.getState(temperature);
    }

    public float getHeatCapacity() {
        return properties.heatCapacity;
    }

    public int getPressure(int amount, float volume, float temperature) {
        return properties.pressureProvider.getPressure(amount, volume, temperature);
    }

    public String getDescriptionId() {
        if(descriptionId == null)
            descriptionId = Util.makeDescriptionId("element", getRegistryName());
        return descriptionId;
    }

    public record Properties(float boilingPoint, float meltingPoint, float heatCapacity, IPressureProvider pressureProvider) {
        public static final Properties EMPTY = new Properties(0, 0, 0, (amount, volume, temperature) -> 0);
        public static final Properties WATER = new Properties(373.15f, 273.15f, 4.182f, (amount, volume, temperature) -> (int)(amount * temperature * 4.182f / volume));

        public State getState(float temperature) {
            if (temperature > boilingPoint) return State.GAS;
            else if (temperature > meltingPoint) return State.LIQUID;
            else return State.SOLID;
        }

        public static Properties of(Properties properties) {
            return new Properties(properties.boilingPoint, properties.meltingPoint, properties.heatCapacity, properties.pressureProvider);
        }
    }
}
