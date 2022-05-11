package com.patryk3211.engineeringplus.util;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LevelDataStorage extends SavedData {
    public final CompoundTag tag;

    public LevelDataStorage(CompoundTag root) {
        tag = root.copy();
    }

    public static LevelDataStorage of(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        LevelDataStorage data = storage.get(LevelDataStorage::new, EngineeringPlusMod.ID);
        if(data == null) {
            data = new LevelDataStorage(new CompoundTag());
            storage.set(EngineeringPlusMod.ID, data);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        return tag.copy();
    }
}
