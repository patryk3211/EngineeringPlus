package com.patryk3211.engineeringplus.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ModBlockProperties {
    public enum ModelPart implements StringRepresentable {
        STATIC("static"),
        SHAFT("shaft");

        private final String stringName;

        ModelPart(String stringName) {
            this.stringName = stringName;
        }

        @Override
        public String getSerializedName() {
            return stringName;
        }
    }

    public static final EnumProperty<ModelPart> MODEL_PART = EnumProperty.create("model_part", ModelPart.class);
}
