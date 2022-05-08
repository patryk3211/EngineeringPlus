package com.patryk3211.engineeringplus.capabilities;

import com.patryk3211.engineeringplus.EngineeringPlusMod;
import com.patryk3211.engineeringplus.capabilities.element.IElementHandler;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {
    public static Capability<IKineticHandler> KINETIC = CapabilityManager.get(new CapabilityToken<>(){});
    public static Capability<IElementHandler> ELEMENT = CapabilityManager.get(new CapabilityToken<>(){});

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IKineticHandler.class);
        event.register(IElementHandler.class);

        EngineeringPlusMod.LOGGER.info("Capabilities registered!");
    }
}
