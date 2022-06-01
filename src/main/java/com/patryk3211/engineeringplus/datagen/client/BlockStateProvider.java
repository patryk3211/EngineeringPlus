package com.patryk3211.engineeringplus.datagen.client;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {
    public BlockStateProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, EngineeringPlusMod.ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
