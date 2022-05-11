package com.patryk3211.engineeringplus.block.pipe;

import net.minecraft.world.level.material.Material;

public class CopperValve extends PipeValve {
    public CopperValve() {
        super(Properties.of(Material.METAL), 1000, 1000000, 0.2f);
    }
}
