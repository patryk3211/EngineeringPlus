package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.Config;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Config.MOD_ID);

    public static final RegistryObject<Block> shaft = BLOCKS.register("shaft", Shaft::new);
    public static final RegistryObject<Block> handCrank = BLOCKS.register("hand_crank", HandCrank::new);
}
