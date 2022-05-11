package com.patryk3211.engineeringplus.item;

import com.patryk3211.engineeringplus.StaticConfig;
import com.patryk3211.engineeringplus.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StaticConfig.MOD_ID);

    public static final RegistryObject<Item> shaft = ITEMS.register("shaft", () -> new BlockItem(ModBlocks.shaft.get(), new Item.Properties()));
    public static final RegistryObject<Item> handCrank = ITEMS.register("hand_crank", () -> new BlockItem(ModBlocks.handCrank.get(), new Item.Properties()));
    public static final RegistryObject<Item> gearbox = ITEMS.register("gearbox", () -> new BlockItem(ModBlocks.gearbox.get(), new Item.Properties()));

    public static final RegistryObject<Item> copperPipe = ITEMS.register("copper_pipe", () -> new BlockItem(ModBlocks.copperPipe.get(), new Item.Properties()));
}
