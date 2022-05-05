package com.patryk3211.engineeringplus;

import com.mojang.logging.LogUtils;
import com.patryk3211.engineeringplus.block.ModBlocks;
import com.patryk3211.engineeringplus.blockentity.ModBlockEntities;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.item.ModItems;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import com.patryk3211.engineeringplus.kinetic.client.ClientKineticNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Config.MOD_ID)
public class EngineeringPlusMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public EngineeringPlusMod() {
        // Register the registries
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::clientSetup);

        modEventBus.addListener(ModCapabilities::registerCapabilities);

        // Register Minecraft event listeners
        KineticNetwork.registerEvents();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ClientKineticNetwork::onRenderOverlay);
    }
}
