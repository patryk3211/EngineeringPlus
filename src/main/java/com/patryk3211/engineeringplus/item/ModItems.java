package com.patryk3211.engineeringplus.item;

import com.patryk3211.engineeringplus.Config;
import com.patryk3211.engineeringplus.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Config.MOD_ID);

    public static final RegistryObject<Item> shaft = ITEMS.register("shaft", () -> new BlockItem(ModBlocks.shaft.get(), new Item.Properties()));
}
