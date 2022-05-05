package com.patryk3211.engineeringplus.kinetic.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.patryk3211.engineeringplus.kinetic.KineticNetwork;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;

import java.nio.Buffer;

@OnlyIn(Dist.CLIENT)
public class ClientKineticNetwork {
    private static void drawCube(BufferBuilder builder, PoseStack stack, float r, float g, float b, float a) {
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, -0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), -0.5f, 0.5f, 0.5f).color(r, g, b, a).endVertex();
        builder.vertex(stack.last().pose(), 0.5f, -0.5f, 0.5f).color(r, g, b, a).endVertex();
    }

    public static void renderBlockHighlight(BufferBuilder builder, PoseStack stack, BlockPos pos, float r, float g, float b, float a) {
        stack.pushPose();
        stack.translate(0.5+pos.getX(), 0.5+pos.getY(), 0.5+pos.getZ());
        stack.scale(1.01f, 1.01f, 1.01f);
        drawCube(builder, stack, r, g, b, a);
        stack.popPose();
    }

    public static void onRenderOverlay(final RenderLevelLastEvent event) {
        PoseStack stack = event.getPoseStack();

        Minecraft instance = Minecraft.getInstance();
        Camera cam = instance.gameRenderer.getMainCamera();

        Vec3 camPos = cam.getPosition();

        stack.pushPose();
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        KineticNetwork.networks.forEach((id, net) -> {
            float r = (id.getLeastSignificantBits() & 0xFF) / 255.0f;
            float g = ((id.getLeastSignificantBits() >> 8) & 0xFF) / 255.0f;
            float b = ((id.getLeastSignificantBits() >> 16) & 0xFF) / 255.0f;

            net.tiles.forEach((pos, dirs) -> {
                renderBlockHighlight(builder, stack, pos, r, g, b, .3f);
            });
        });

        tesselator.end();
        stack.popPose();
    }
}
