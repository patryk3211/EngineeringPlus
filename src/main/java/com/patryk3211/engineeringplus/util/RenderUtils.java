package com.patryk3211.engineeringplus.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {
    public static void renderCube(BufferBuilder builder, PoseStack stack, float r, float g, float b, float a) {
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
        renderCube(builder, stack, r, g, b, a);
        stack.popPose();
    }

    public static void translateToWorld(PoseStack stack) {
        Minecraft instance = Minecraft.getInstance();
        Camera cam = instance.gameRenderer.getMainCamera();

        Vec3 camPos = cam.getPosition();

        stack.pushPose();
        stack.translate(-camPos.x, -camPos.y, -camPos.z);
    }
}
