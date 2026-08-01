/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package cn.solo.sanguosha.block;

import cn.solo.sanguosha.block.TableBlockEntity;
import cn.solo.sanguosha.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class GameTable2BlockEntity
extends TableBlockEntity {
    public GameTable2BlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)ModBlockEntities.GAME_TABLE_2.get(), pos, state);
    }
}

