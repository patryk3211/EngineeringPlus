package com.patryk3211.engineeringplus.blockentity;

import com.patryk3211.engineeringplus.Config;
import com.patryk3211.engineeringplus.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Config.MOD_ID);

    public static final RegistryObject<BlockEntityType<ShaftEntity>> shaft = BLOCK_ENTITIES.register("shaft", () -> BlockEntityType.Builder.of(ShaftEntity::new, ModBlocks.shaft.get()).build(null));
    public static final RegistryObject<BlockEntityType<HandCrankEntity>> handCrank = BLOCK_ENTITIES.register("hand_crank", () -> BlockEntityType.Builder.of(HandCrankEntity::new, ModBlocks.handCrank.get()).build(null));
}
