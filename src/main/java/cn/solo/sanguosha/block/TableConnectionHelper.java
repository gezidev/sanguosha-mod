package cn.solo.sanguosha.block;

import cn.solo.sanguosha.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class TableConnectionHelper {
    public static final DirectionProperty FACING = BlockStateProperties.f_61374_;
    public static final BooleanProperty LOCAL_LEFT_CONNECTED = BooleanProperty.m_61465_((String)"west_connected");
    public static final BooleanProperty LOCAL_RIGHT_CONNECTED = BooleanProperty.m_61465_((String)"east_connected");

    private TableConnectionHelper() {
    }

    public static Direction localLeft(Direction facing) {
        return facing.m_122428_();
    }

    public static Direction localRight(Direction facing) {
        return facing.m_122427_();
    }

    public static BlockState stateForNeighbours(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction facing = (Direction)state.m_61143_((Property)FACING);
        return (BlockState)((BlockState)state.m_61124_((Property)LOCAL_LEFT_CONNECTED, (Comparable)Boolean.valueOf(TableConnectionHelper.isTable(level, pos.m_121945_(TableConnectionHelper.localLeft(facing)))))).m_61124_((Property)LOCAL_RIGHT_CONNECTED, (Comparable)Boolean.valueOf(TableConnectionHelper.isTable(level, pos.m_121945_(TableConnectionHelper.localRight(facing)))));
    }

    public static BlockState updateShape(BlockState state, Direction changedSide, LevelAccessor level, BlockPos pos) {
        return changedSide.m_122434_().m_122479_() ? TableConnectionHelper.stateForNeighbours(state, level, pos) : state;
    }

    public static void scheduleAffected(Level level, BlockPos origin) {
        TableConnectionHelper.scheduleAt(level, origin);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            TableConnectionHelper.scheduleAt(level, origin.m_121945_(direction));
        }
    }

    private static void scheduleAt(Level level, BlockPos pos) {
        BlockState state = level.m_8055_(pos);
        if (TableConnectionHelper.isTableNeighbor(state)) {
            level.m_186460_(pos, state.m_60734_(), 1);
        }
    }

    public static void refreshAffected(ServerLevel level, BlockPos origin) {
        TableConnectionHelper.recalculateAt(level, origin);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            TableConnectionHelper.recalculateAt(level, origin.m_121945_(direction));
        }
    }

    private static void recalculateAt(ServerLevel level, BlockPos pos) {
        BlockState oldState = level.m_8055_(pos);
        if (!TableConnectionHelper.isTableNeighbor(oldState)) {
            return;
        }
        BlockState newState = TableConnectionHelper.stateForNeighbours(oldState, (LevelAccessor)level, pos);
        if (oldState != newState) {
            level.m_7731_(pos, newState, 2);
        }
    }

    public static boolean isTableNeighbor(BlockState state) {
        return state.m_60713_((Block)ModBlocks.GAME_TABLE_2.get());
    }

    private static boolean isTable(LevelAccessor level, BlockPos pos) {
        return TableConnectionHelper.isTableNeighbor(level.m_8055_(pos));
    }
}

