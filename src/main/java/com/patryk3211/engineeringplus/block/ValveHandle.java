package com.patryk3211.engineeringplus.block;

import com.patryk3211.engineeringplus.blockentity.ValveHandleEntity;
import com.patryk3211.engineeringplus.util.KineticUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ValveHandle extends KineticBlock {
    public ValveHandle() {
        super(Properties.of(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        builder.add(ModBlockProperties.MODEL_PART);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.CONSUME;

        BlockEntity entity = level.getBlockEntity(pos);
        if(!(entity instanceof ValveHandleEntity valveHandle)) return InteractionResult.FAIL;

        valveHandle.kineticHandler.ifPresent(handler -> {
            float targetAngle = player.isShiftKeyDown() ? 0f : 90f;
            float currentAngle = handler.getAngle();
            System.out.println(currentAngle);

            float angleDistance = KineticUtils.signedAngleDistance(currentAngle, targetAngle);
            float force = handler.calculateForce(angleDistance / 360f * 60f * 20f);
            handler.applyForce(Math.min(Math.abs(force), 10f) * Math.signum(force));
        });

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ValveHandleEntity(pos, state);
    }
}
