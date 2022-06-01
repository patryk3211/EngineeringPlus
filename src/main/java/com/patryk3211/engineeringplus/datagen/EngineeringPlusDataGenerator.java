package com.patryk3211.engineeringplus.datagen;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import com.patryk3211.engineeringplus.datagen.client.BlockStateProvider;
import com.patryk3211.engineeringplus.datagen.client.ItemModelProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = EngineeringPlusMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EngineeringPlusDataGenerator {
    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        if(event.includeClient()) {
            // Client data (assets/)
            event.getGenerator().addProvider(new BlockStateProvider(event.getGenerator(), event.getExistingFileHelper()));
            event.getGenerator().addProvider(new ItemModelProvider(event.getGenerator(), event.getExistingFileHelper()));
        }
        if(event.includeServer()) {
            // Server data (data/)
        }
    }
}
