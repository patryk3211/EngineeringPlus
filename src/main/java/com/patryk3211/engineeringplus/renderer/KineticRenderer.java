package com.patryk3211.engineeringplus.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.patryk3211.engineeringplus.blockentity.KineticEntity;
import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
public class KineticRenderer implements BlockEntityRenderer<KineticEntity> {
    public static void renderShaft(MultiBufferSource buffer, PoseStack stack, BlockState state, Direction.Axis rotationalAxis, float angle, int light, int overlay) {
        // Apply rotation to our model
        Vector3f normal = switch(rotationalAxis) {
            case X -> Vector3f.XP.copy();
            case Y -> Vector3f.YP.copy();
            case Z -> Vector3f.ZP.copy();
        };
        Vector3f plane = new Vector3f((1 - normal.x()) * 0.5f, (1 - normal.y()) * 0.5f, (1 - normal.z()) * 0.5f);

        // Convert degrees to radians
        angle = angle * 0.0174532925f;

        normal.mul(angle);

        stack.translate(plane.x(), plane.y(), plane.z());
        stack.mulPose(Quaternion.fromXYZ(normal));
        stack.translate(-plane.x(), -plane.y(), -plane.z());

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, stack, buffer, light, overlay, EmptyModelData.INSTANCE);
    }

    @Override
    public void render(KineticEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        List<Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis>> handlers = entity.getRenderedHandlers();

        for (Triple<LazyOptional<IKineticHandler>, BlockState, Direction.Axis> handlerStatePair : handlers) {
            AtomicReference<Float> angle = new AtomicReference<>(0f);
            handlerStatePair.getLeft().ifPresent(handler -> angle.set(handler.getAngle()));

            stack.pushPose();

            BlockState state = handlerStatePair.getMiddle();
            renderShaft(buffer, stack, state, handlerStatePair.getRight(), angle.get(), light, overlay);

            stack.popPose();
        }
    }
}
