/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package cn.solo.sanguosha.block;

import cn.solo.sanguosha.block.GameTable2BlockEntity;
import cn.solo.sanguosha.block.TableBlockSupport;
import cn.solo.sanguosha.block.TableConnectionHelper;
import cn.solo.sanguosha.game.GameRoomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GameTable2Block
extends BaseEntityBlock {
    public static final DirectionProperty FACING = TableConnectionHelper.FACING;
    public static final BooleanProperty EAST_CONNECTED = TableConnectionHelper.LOCAL_RIGHT_CONNECTED;
    public static final BooleanProperty WEST_CONNECTED = TableConnectionHelper.LOCAL_LEFT_CONNECTED;
    private static final VoxelShape COLLISION = Shapes.m_83110_((VoxelShape)Block.m_49796_((double)0.0, (double)14.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0), (VoxelShape)Block.m_49796_((double)6.0, (double)0.0, (double)6.0, (double)10.0, (double)14.0, (double)10.0));

    public GameTable2Block(BlockBehaviour.Properties properties) {
        super(properties);
        this.m_49959_((BlockState)((BlockState)((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_((Property)FACING, (Comparable)Direction.NORTH)).m_61124_((Property)EAST_CONNECTED, (Comparable)Boolean.valueOf(false))).m_61124_((Property)WEST_CONNECTED, (Comparable)Boolean.valueOf(false)));
    }

    public BlockState m_5573_(BlockPlaceContext context) {
        return TableConnectionHelper.stateForNeighbours((BlockState)this.m_49966_().m_61124_((Property)FACING, (Comparable)context.m_8125_()), (LevelAccessor)context.m_43725_(), context.m_8083_());
    }

    public BlockState m_7417_(BlockState state, Direction changedSide, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return TableConnectionHelper.updateShape(state, changedSide, level, pos);
    }

    public void m_6807_(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.m_6807_(state, level, pos, oldState, movedByPiston);
        if (!level.f_46443_ && !oldState.m_60713_(state.m_60734_())) {
            TableConnectionHelper.scheduleAffected(level, pos);
        }
    }

    public void m_6810_(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.m_60713_(state.m_60734_())) {
            TableBlockSupport.dropContents(level, pos);
            super.m_6810_(state, level, pos, newState, movedByPiston);
            if (!level.f_46443_) {
                GameRoomManager.invalidateAt((ServerLevel)level, pos);
                TableConnectionHelper.scheduleAffected(level, pos);
            }
            return;
        }
        super.m_6810_(state, level, pos, newState, movedByPiston);
    }

    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        return new GameTable2BlockEntity(pos, state);
    }

    public RenderShape m_7514_(BlockState state) {
        return RenderShape.MODEL;
    }

    public void m_213897_(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TableConnectionHelper.refreshAffected(level, pos);
    }

    public VoxelShape m_5940_(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        builder.m_61104_(new Property[]{FACING, EAST_CONNECTED, WEST_CONNECTED});
    }
}

