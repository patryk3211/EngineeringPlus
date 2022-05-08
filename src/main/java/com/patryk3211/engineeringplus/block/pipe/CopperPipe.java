package com.patryk3211.engineeringplus.block.pipe;

import net.minecraft.world.level.material.Material;

public class CopperPipe extends Pipe {
    public CopperPipe() {
        super(Properties.of(Material.METAL), 100, 1000000);
    }
}
