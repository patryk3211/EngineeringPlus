package com.patryk3211.engineeringplus.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ElementStack {
    public static final ElementStack EMPTY = new ElementStack(Elements.EMPTY.get(), 0, 0);

    public final Element element;
    /** Pressure provided in Pa (pascal) **/
    public int pressure;
    /** Temperature provided in K (kelvin) **/
    public float temperature;

    public ElementStack(Element element, int pressure) {
        this.element = element;
        this.pressure = pressure;
        this.temperature = 0;
    }

    public ElementStack(Element element, int pressure, float temperature) {
        this.element = element;
        this.pressure = pressure;
        this.temperature = temperature;
    }

    public ElementStack(CompoundTag tag) {
        this.element = Element.fromLocation(new ResourceLocation(tag.getString("id")));
        this.pressure = tag.getInt("psr");
        this.temperature = tag.getFloat("temp");
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ResourceLocation loc = element.getRegistryName();
        if(loc == null) return tag;

        tag.putString("id", loc.getNamespace() + ":" + loc.getPath());
        tag.putInt("psr", pressure);
        tag.putFloat("temp", temperature);

        return tag;
    }

    public ElementStack copy() {
        return new ElementStack(element, pressure, temperature);
    }
}
