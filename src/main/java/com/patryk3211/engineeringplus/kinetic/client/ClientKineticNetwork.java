package com.patryk3211.engineeringplus.kinetic.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;
import com.patryk3211.engineeringplus.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientKineticNetwork implements IKineticNetwork {
    private static final Map<UUID, ClientKineticNetwork> networks = new HashMap<>();

    private final UUID id;

    private float speed;
    private float angle;
    private float angleLast;

    private float angleChange;

    public ClientKineticNetwork(UUID id) {
        this.id = id;

        this.speed = 0;
        this.angle = 0;

        networks.put(id, this);
    }

    public void setValues(float speed, float angle) {
        this.speed = speed;
        this.angle = angle;
    }

    public static void remove(UUID id) {
        networks.remove(id);
    }

    public static ClientKineticNetwork getNetwork(UUID id) {
        return networks.get(id);
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public float getInertia() {
        return 0;
    }

    @Override
    public void changeSpeed(float amount) {
        speed += amount;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public static void removeAll() {
        networks.clear();
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(ClientKineticNetwork::onRenderOverlay);
        MinecraftForge.EVENT_BUS.addListener(ClientKineticNetwork::onWorldJoin);
        MinecraftForge.EVENT_BUS.addListener(ClientKineticNetwork::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientKineticNetwork::onRenderTick);
    }

    private static void onRenderOverlay(final RenderLevelLastEvent event) {
        // Render only when F3 is open
        if(!Minecraft.getInstance().options.renderDebug) return;

        PoseStack stack = event.getPoseStack();

        RenderUtils.translateToWorld(stack);

        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        networks.forEach((id, net) -> {
            float r = (id.getLeastSignificantBits() & 0xFF) / 255.0f;
            float g = ((id.getLeastSignificantBits() >> 8) & 0xFF) / 255.0f;
            float b = ((id.getLeastSignificantBits() >> 16) & 0xFF) / 255.0f;
            // TODO: [07.05.2022] Fix this...
            /*net.tiles.forEach((pos, dirs) -> {
                RenderUtils.renderBlockHighlight(builder, stack, pos, r, g, b, .3f);
            });*/
        });

        tesselator.end();
        stack.popPose();
    }

    private static void onWorldJoin(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        networks.clear();
    }

    private static void onClientTick(final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) return;
        if(Minecraft.getInstance().isPaused()) return;

        networks.forEach((id, network) -> {
            network.angleLast = (network.angleLast + network.angleChange) % 360; // Advance lastAngle by 1 tick time
            network.angle = network.angleLast; // Synchronize angle and lastAngle
            network.angleChange = network.speed * 0.3f; // speed [rpm] * (1 tick / 60 seconds * 360 degrees) = speed [rpm] * 0.3f
        });
    }

    private static void onRenderTick(final TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.END) return;
        if(Minecraft.getInstance().isPaused()) return;

        networks.forEach((id, network) -> {
            network.angle = network.angleLast + network.angleChange * event.renderTickTime;
        });
    }
}
