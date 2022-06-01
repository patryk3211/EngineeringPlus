package com.patryk3211.engineeringplus.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class DirectionUtils {
    public static BooleanProperty directionToProperty(Direction direction) {
        return switch(direction) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case DOWN -> BlockStateProperties.DOWN;
            case UP -> BlockStateProperties.UP;
        };
    }
}
