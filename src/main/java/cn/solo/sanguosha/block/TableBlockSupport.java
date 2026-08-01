/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.Containers
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package cn.solo.sanguosha.block;

import cn.solo.sanguosha.block.TableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class TableBlockSupport {
    private TableBlockSupport() {
    }

    public static void dropContents(Level level, BlockPos pos) {
        BlockEntity blockEntity;
        if (level.f_46443_ || !((blockEntity = level.m_7702_(pos)) instanceof TableBlockEntity)) {
            return;
        }
        TableBlockEntity table = (TableBlockEntity)blockEntity;
        for (int slot = 0; slot < 7; ++slot) {
            Containers.m_18992_((Level)level, (double)((double)pos.m_123341_() + 0.5), (double)((double)pos.m_123342_() + 1.0), (double)((double)pos.m_123343_() + 0.5), (ItemStack)table.items().getStackInSlot(slot));
        }
    }
}

