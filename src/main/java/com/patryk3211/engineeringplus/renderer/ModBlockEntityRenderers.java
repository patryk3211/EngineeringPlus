package com.patryk3211.engineeringplus.renderer;

import com.patryk3211.engineeringplus.blockentity.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.Collection;
import java.util.List;

public class ModBlockEntityRenderers {
    private record Registerer(EntityRenderersEvent.RegisterRenderers event) {
        public <T extends BlockEntity> void registerBlockEntity(BlockEntityType<T> type, BlockEntityRendererProvider<T> provider) {
            event.registerBlockEntityRenderer(type, provider);
        }

        public <T extends BlockEntity> void registerBlockEntities(Collection<BlockEntityType<? extends T>> types, BlockEntityRendererProvider<T> provider) {
            for (BlockEntityType<? extends T> type : types)
                event.registerBlockEntityRenderer(type, provider);
        }
    }

    public static void onRendererRegister(final EntityRenderersEvent.RegisterRenderers event) {
        Registerer registry = new Registerer(event);

        registry.registerBlockEntities(List.of(
                ModBlockEntities.shaft.get(),
                ModBlockEntities.pipe_valve.get()
        ), context -> new KineticRenderer());
    }
}
