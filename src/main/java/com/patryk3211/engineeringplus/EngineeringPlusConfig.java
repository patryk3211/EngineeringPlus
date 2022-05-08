package com.patryk3211.engineeringplus;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class EngineeringPlusConfig {
    public static class Common {
        public final ForgeConfigSpec.BooleanValue debugEnabled;

        private Common(ForgeConfigSpec.Builder builder) {
            this.debugEnabled = builder
                    .comment("Enables debugging feature, some only work when this config is enabled both on the client and the server.")
                    .translation("config.engineeringplus.common.debug_enabled")
                    .define("debug_enabled", true);
        }
    }

    private static Pair<Common, ForgeConfigSpec> commonPair;

    public static void init() {
        commonPair = new ForgeConfigSpec.Builder().configure(Common::new);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonPair.getValue());
    }

    public static Common common() {
        return commonPair.getKey();
    }
}
