package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.StaticConfig;
import com.patryk3211.engineeringplus.block.pipe.CopperPipe;
import com.patryk3211.engineeringplus.block.pipe.CopperValve;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, StaticConfig.MOD_ID);

    public static final RegistryObject<Block> shaft = BLOCKS.register("shaft", Shaft::new);
    public static final RegistryObject<Block> handCrank = BLOCKS.register("hand_crank", HandCrank::new);
    public static final RegistryObject<Block> gearbox = BLOCKS.register("gearbox", Gearbox::new);

    public static final RegistryObject<Block> copperPipe = BLOCKS.register("copper_pipe", CopperPipe::new);

    public static final RegistryObject<Block> copperValve = BLOCKS.register("copper_valve", CopperValve::new);

    public static final RegistryObject<Block> valveHandle = BLOCKS.register("valve_handle", ValveHandle::new);
}
