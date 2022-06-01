package com.patryk3211.engineeringplus.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ElementStack {
    public static final ElementStack EMPTY = new ElementStack(Elements.EMPTY.get(), 0, 0);

    public final Element element;
    /** Amount provided in g (kilogram) **/
    public int amount;
    /** Temperature provided in K (kelvin) **/
    public float temperature;

    public ElementStack(Element element, int amount) {
        this.element = element;
        this.amount = amount;
        this.temperature = 0;
    }

    public ElementStack(Element element, int amount, float temperature) {
        this.element = element;
        this.amount = amount;
        this.temperature = temperature;
    }

    public ElementStack(CompoundTag tag) {
        this.element = Element.fromLocation(new ResourceLocation(tag.getString("id")));
        this.amount = tag.getInt("amt");
        this.temperature = tag.getFloat("temp");
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ResourceLocation loc = element.getRegistryName();
        if(loc == null) return tag;

        tag.putString("id", loc.getNamespace() + ":" + loc.getPath());
        tag.putInt("amt", amount);
        tag.putFloat("temp", temperature);

        return tag;
    }

    public ElementStack copy() {
        return new ElementStack(element, amount, temperature);
    }
}
