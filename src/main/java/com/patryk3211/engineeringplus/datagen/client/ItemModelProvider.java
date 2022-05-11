package com.patryk3211.engineeringplus.datagen.client;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, EngineeringPlusMod.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}
