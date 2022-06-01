package com.patryk3211.engineeringplus;

import com.mojang.logging.LogUtils;
import com.patryk3211.engineeringplus.block.ModBlocks;
import com.patryk3211.engineeringplus.blockentity.ModBlockEntities;
import com.patryk3211.engineeringplus.capabilities.ModCapabilities;
import com.patryk3211.engineeringplus.element.Element;
import com.patryk3211.engineeringplus.element.Elements;
import com.patryk3211.engineeringplus.item.ModItems;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import com.patryk3211.engineeringplus.kinetic.client.ClientKineticNetwork;
import com.patryk3211.engineeringplus.network.KineticNetworkPacket;
import com.patryk3211.engineeringplus.network.PacketHandler;
import com.patryk3211.engineeringplus.renderer.ModBlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryBuilder;
import org.slf4j.Logger;

@Mod(EngineeringPlusMod.ID)
public class EngineeringPlusMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String ID = "engineeringplus";

    public EngineeringPlusMod() {
        EngineeringPlusConfig.init();
        LOGGER.info("Registered configs!");

        // Register the registries
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        Elements.ELEMENTS.makeRegistry(Element.class, () -> new RegistryBuilder<Element>().onCreate((owner, stage) -> EngineeringPlusRegistries.RESOLVED_ELEMENT_REGISTRY = owner));
        Elements.ELEMENTS.register(modEventBus);

        // Sided setups
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Other events
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(ModBlockEntityRenderers::onRendererRegister));
        modEventBus.addListener(ModCapabilities::registerCapabilities);

        // Register Minecraft event listeners
        KineticNetwork.registerEvents();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketHandler.register(KineticNetworkPacket.class, KineticNetworkPacket::encode, KineticNetworkPacket::decode, KineticNetworkPacket::handle);
        LOGGER.info("Registered packet handlers!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientKineticNetwork.registerEvents();
    }
}
